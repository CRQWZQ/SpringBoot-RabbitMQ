package com.valley.amqp.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

/**
 * @ProjectName: amqp
 * @Package: com.valley.amqp.consumer
 * @ClassName: BaseConsumer
 * @Description: 基础的消费者配置
 * @Author: wangzq
 * @Date: 2021/5/17 下午9:38
 */
@Configuration
public class ConsumerConfig implements RabbitListenerConfigurer {

    /**
     * 消费者数量，默认是10
     */
    public static final int DEFAULT_CONCURRENT = 10;

    public static final int DEFAULT_PREFETCH_COUNT = 50;


    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(myHandlerMethodFactory());
    }


    /**
     *功能描述: 自己封装的一个一个程序处理方法工厂,可自定义处理的方法名（默认是处理 @RabbitListener）
     * 这个地方其实可以自己定义的，但是源码研究的不是很深，就没有进行相关扩展，基本的其实已经足够使用了
     * @author wangzq
     * @param
     * @return DefaultMessageHandlerMethodFactory
     */
    @Bean
    public DefaultMessageHandlerMethodFactory myHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(new MappingJackson2MessageConverter());
        return factory;
    }

    /**
     * 配置一个并发消费
     *
     * @param connectionFactory
     * @return
     */
    @Bean("rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        //当前消费者数量
        factory.setConcurrentConsumers(1);
//设置拒绝消息时的默认行为 true:则重新发送消息 false:则不会重新发送消息
        factory.setDefaultRequeueRejected(false);
        //最大消费者数量
        factory.setMaxConcurrentConsumers(DEFAULT_CONCURRENT);
        factory.setPrefetchCount(DEFAULT_PREFETCH_COUNT);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        //消费端的标签策略
        /*factory.setConsumerTagStrategy(new ConsumerTagStrategy() {
            @Override
            public String createConsumerTag(String queue) {
                return queue + "_" + UUID.randomUUID();
            }
        });*/
        return factory;
    }

}
