package com.hmall.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.domain.OrderDetailDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.UserContext;
import com.hmall.item.api.client.ItemClient;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.trade.config.TradeProperties;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.dto.OrderFormDTO;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.domain.po.OrderDetail;
import com.hmall.trade.mapper.OrderMapper;
import com.hmall.trade.service.IOrderDetailService;
import com.hmall.trade.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final IOrderDetailService detailService;
    private final IOrderDetailService orderDetailService;
    private final ItemClient itemClient;
    private final RabbitTemplate rabbitTemplate;
    private final TradeProperties tradeProperties;

    @Override
    @GlobalTransactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        // 1.订单数据
        Order order = new Order();
        // 1.1.查询商品
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        log.info(detailDTOS.toString());
        // 1.2.获取商品id和数量的Map
        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        // 1.3.查询商品
        List<ItemDTO> items = itemClient.queryItemsByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }
        log.info("查询到商品信息，总条数：{}", items.size());
        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        // 3.清理购物车商品
        try {
            rabbitTemplate.convertAndSend("trade.topic", "order.create", itemIds);
        } catch (Exception e) {
            log.error("发送购物车消息失败");
        }

        // 4.扣减库存
        try {
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new RuntimeException("库存异常！");
        }

        log.info("延迟消息发送时间：{}", tradeProperties.getDelayTime());
        // 5:发送延迟消息，检测订单支付状态
        rabbitTemplate.convertAndSend(MQConstants.DELAY_EXCHANGE_NAME, MQConstants.DELAY_ORDER_KEY, order.getId(), new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(tradeProperties.getDelayTime());
                return message;
            }
        });


        return order.getId();
    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        // UPDATE `order` SET status = ? , pay_time = ? WHERE id = ? AND status = 1
        lambdaUpdate()
                .set(Order::getStatus, 2)
                .set(Order::getPayTime, LocalDateTime.now())
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, 1)
                .update();
    }

    @Override
    public void cancelOrder(Long orderId) {
        // UPDATE `order` SET status = ? , close_time = ? WHERE id = ? AND status = 1
        lambdaUpdate()
                .set(Order::getStatus, 5)
                .set(Order::getCloseTime, LocalDateTime.now())
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, 1)
                .update();
        List<OrderDetail> itemlist = orderDetailService.lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId)
                .list();


        List<OrderDetailDTO> list = BeanUtils.copyList(itemlist, OrderDetailDTO.class);
        list.forEach(orderDetailDTO -> orderDetailDTO.setNum(-1 * orderDetailDTO.getNum()));
        itemClient.deductStock(list);

    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }
}
