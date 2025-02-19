package com.hmall.common.config;

import com.hmall.common.utils.UserContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
@AllArgsConstructor
public class MqConfig {
    @Bean
    public MessageConverter messageConverter(){
        log.info("初始化消息转换器.......");
        // 1.定义消息转换器
        Jackson2JsonMessageConverter jjmc = new Jackson2JsonMessageConverter();
        // 2.配置自动创建消息id，用于识别不同消息，也可以在业务中基于ID判断是否是重复消息
        jjmc.setCreateMessageIds(true);
        return jjmc;
    }


    @Bean
    public MessagePostProcessor messagePostProcessor(){
        log.info("初始化消息头信息.......");
        return message -> {
            message.getMessageProperties().setHeader("user-info", UserContext.getUser());
            return message;
        };
    }

    @Bean
    public MethodInterceptor userContextMessageAdvice(){
        log.info("初始化消息拦截器.....");
        return invocation -> {
            for(Object arg : invocation.getArguments()){
                if(arg instanceof Message){
                    Message message = (Message) arg;
                    UserContext.setUser(message.getMessageProperties().getHeader("user-info"));
                    break;
                }
            }
            return invocation.proceed();
        };
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setBeforePublishPostProcessors(messagePostProcessor());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAdviceChain(userContextMessageAdvice());
        return factory;
    }

}
