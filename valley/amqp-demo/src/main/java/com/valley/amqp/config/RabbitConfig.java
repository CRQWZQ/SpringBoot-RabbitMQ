package com.valley.amqp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.impl.AMQChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @ProjectName: amqp-demo
 * @Package: com.valley.amqp.config
 * @ClassName: RabbitmqConfig
 * @Description: rabbitmqConfig
 * @Author: wangzq
 * @Date: 2021/5/11 下午1:47
 * <p>
 * 部分参数讲解说明
 * Broker:它提供一种传输服务,它的角色就是维护一条从生产者到消费者的路线，保证数据能按照指定的方式进行传输,
 * Exchange：消息交换机,它指定消息按什么规则,路由到哪个队列。
 * Queue:消息的载体,每个消息都会被投到一个或多个队列。
 * Binding:绑定，它的作用就是把exchange和queue按照路由规则绑定起来.
 * Routing Key:路由关键字,exchange根据这个关键字进行消息投递。
 * vhost:虚拟主机,一个broker里可以有多个vhost，用作不同用户的权限分离。
 * Producer:消息生产者,就是投递消息的程序.
 * Consumer:消息消费者,就是接受消息的程序.
 * Channel:消息通道,在客户端的每个连接里,可建立多个channel.
 */
@Configuration
public class RabbitConfig {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.enableConfirm}")
    private boolean enableConfirm;

    @Value("${spring.rabbitmq.enableReturn}")
    private boolean enableReturn;

    /**
     * 这里添加自己需要使用的队列，交换机或者routingKey
     */
    public static final String QUEUE_WORK = "queue_work";
    public static final String QUEUE_FANOUT = "queue_fanout";
    public static final String QUEUE_TOPIC = "queue_topic";

    public static final String EXCHANGE_FANOUT = "exchange_fanout";
    public static final String EXCHANGE_TOPIC = "exchange_topic";

    /**
     * 创建一个缓存连接工厂
     *
     * @return
     */
    @Bean("connectionFactory")
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        //这个地方是设置虚拟机，如果后期开发中涉及到多个场景的时候，这个配置的时候可作具体配置处理
        connectionFactory.setVirtualHost("/");
        //使用CachingConnectionFactory 是必须要设置
        connectionFactory.setPublisherConfirms(enableConfirm);
        //根据配置决定是否开启 Return 机制
        connectionFactory.setPublisherReturns(enableReturn);
        return connectionFactory;
    }

    /**
     * 配置一个工作模型队列
     *
     * @return
     */
    @Bean
    public Queue queueWork() {
        return new Queue("queue_work", true);
    }

    /**
     * 发布订阅模式
     *
     * @return
     */
    @Bean
    public Queue queueFanout() {
        return new Queue("queue_fanout",true);
    }

    /**
     * 准备一个交换机
     *
     * @return
     */
    @Bean
    public FanoutExchange exchangeFanout() {
        return new FanoutExchange("exchange_fanout", true, false);
    }

    /**
     * 将 fanout 交换机与队列进行绑定
     * fanout 交换机绑定的时候不需要填写routingKey 因为不会生效
     *
     * @return
     */
    @Bean
    public Binding bindingExchange() {
        return BindingBuilder.bind(queueFanout()).to(exchangeFanout());
    }

    /**
     * topic 模型
     *   @param name 队列名称
     * 	 @param durable 如果我们声明一个持久队列，则为true（该队列将在服务器重启后保留下来）
     * 	 @param exclusive 如果我们声明一个独占队列，则为true（该队列仅由声明者的连接使用）
     * 	 @param autoDelete 如果服务器应该在不再使用队列时删除队列，则为true
     *
     *
     * @return
     */
    @Bean
    public Queue queueTopic() {
        return new Queue("queue_topic", true, false, false);
    }

    @Bean
    public TopicExchange exchangeTopic() {
        TopicExchange build = ExchangeBuilder.topicExchange(EXCHANGE_TOPIC).durable(true).build();
        return build;
    }

    /**
     * topic模型 binding将交换机和相应队列连接起来
     * topic 模型 进行绑定的时候是需要routingKey的设置的
     * 创建exchange, 可以创建TopicExchange(*、#模糊匹配routing key，routing key必须包含".")，DirectExchange，FanoutExchange(无routing key概念)
     *
     * @return
     */
    @Bean
    public Binding bindingTopic() {
        // #表示0个或若干个关键字，*表示一个关键字
        return BindingBuilder.bind(queueTopic()).to(exchangeTopic()).with("topic.#");
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        /** 配置 默认的RabbitTemplate 该地方可作为拓展点进行扩展*/
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        //其他属性值设置

        /**
         *功能描述: 设置消息发送转换器
         * 发送消息的时候先将自定义的消息类序列化成json格式，再转成byte构造 Message
         * ⚠️注意：这个地方是个坑，你在发送消息的时候需要设置下消息转换器
         * （可自定义转换器进行数据封装处理，但是消费的时候必须跟发送时的消息转换器保持一致，否则无法正常消费）
         */
        template.setMessageConverter(new Jackson2JsonMessageConverter());

        //如果启用 Confirm 机制，设置 ConfirmCallback
        if (enableConfirm) {
            template.setConfirmCallback(confirmCallback());
        }
        //如果启用 Return 机制，设置 ReturnCallback，及打开 Mandatory
        if (enableReturn) {
            template.setReturnCallback(returnCallback());
            template.setMandatory(true);
        }
        return template;
    }

    /**
     * 确认回调 （该功能可提供后期排查问题的时候查看具体的消息信息）
     * <p>
     * ConfirmCallback:每一条发送到rabbitmq server 的消息都会调一次 confirm 方法。
     * 通过查看源码也可以了解到 confirmCallback 如何被调用 入口路径为：
     * {@link AMQChannel} -->>method: handleCompleteInboundCommand  -->> method: processAsync
     * 最后追踪到 processAsync(command) 这个方法，在该方法中可以看到：
     * ConfirmCallback在收到Basic.Ack或Basic.Nack时调用
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = RabbitTemplate.ConfirmCallback.class)
    public RabbitTemplate.ConfirmCallback confirmCallback() {
        return new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                // do something ...
                log.info("----confirmCallback message begin executing---");
                if (ack) {
                    log.info("消息id为: " + correlationData + "的消息，已经被ack成功");
                } else {
                    log.info("消息id为: " + correlationData + "的消息，消息nack，失败原因是：" + cause);
                }
            }
        };
    }

    /**
     * 返回回调 （该功能可在测试中查看具体的消息信息）
     * 在 processAsync(command) 这个方法，可以看到：
     * ReturnCallback在收到Basic.Return时调用
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = RabbitTemplate.ReturnCallback.class)
    public RabbitTemplate.ReturnCallback returnCallback() {
        return new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                // do something ...
                log.info("----returnCallback message begin executing: routingKey={} | exchange={} " +
                        "| replyCode= {} | replayText= {} | message={}", routingKey, exchange, replyCode, replyText, message);

            }
        };
    }

    /**
     * 功能描述:
     *
     * @param objectMapper json序列化实现类
     * @return org.springframework.amqp.support.converter.MessageConverter
     * @author wangzq
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
