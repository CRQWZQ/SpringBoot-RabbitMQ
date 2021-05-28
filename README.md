# SpringBoot-RabbitMQ
springboot 搭载rabbitmq 相关案例实战模版demo，里面含盖了相关的注解说明
 
---- 
# 一. RabbitMQ 服务离线搭建

> 本环节可参考我[百度云盘上](https://pan.baidu.com/s/1-ib5FEFiQSaxxcghhp2_qQ) 的相关教程文档进行查看，此处不再进行详细讲解 （提取码:78ce）

# 二. SpringBoot 整合 RabbitMQ.

## 1.导入rabbitmq maven 依赖
```
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-amqp</artifactId>
      <!-- 这个地方填写的版本跟你配置的SpringBoot版本有关，具体情况可以参考maven版本兼容查看 -->
      <version>2.4.5</version> 
</dependency>
```

## 2.在引用的`properties` 或者 `yml` 文件中进行具体配置
> 在`application.properties` 引入对`RabbitMQ`的支持
```
spring.rabbitmq.host=192.168.10.32
# rabbitmq 连接端口（默认5672）
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin  
spring.rabbitmq.password=admin
# 消息开启手动确认 manual :手动确认  auto :自动确认
spring.rabbitmq.listener.direct.acknowledge-mode=manual
```

> 在`yml`文件中引入`RabbitMQ`的支持
```
spring:
  rabbitmq:
    host: 192.168.10.32 
    port: 5672
    user: admin
    password: admin
    listener:
      direct:
        acknowledge-mode: manual
```

## 3. 编写`RabbitConfig`类，类里面可以设置很多个exchange，queue，routingKey, 作为接下来的不同使用场景

1. 在rabbitConfig中引入了需要的队列(queue)和交换机(exchange)，以及路由键(routingKey)的定义，同时进行了相关队列和交换机之间的绑定情况
里面的一些使用参数我以做了详细的备注说明，如果有遗漏请见谅
```
/**
     * 这里添加自己需要使用的队列，交换机或者routingKey
     */
    public static final String QUEUE_WORK = "queue_work";
    public static final String QUEUE_FANOUT = "queue_fanout";
    public static final String QUEUE_TOPIC = "queue_topic";

    public static final String EXCHANGE_FANOUT = "exchange_fanout";
    public static final String EXCHANGE_TOPIC = "exchange_topic";

    @Bean
    public Queue queueWork() {
        return new Queue("queue_work");
    }

    @Bean
    public Queue queueFanout() {
        return new Queue("queue_fanout");
    }

    @Bean
    public FanoutExchange exchangeFanout() {
        return new FanoutExchange("exchange_fanout");
    }

    @Bean
    public Binding bindingExchange() {
        return BindingBuilder.bind(queueFanout()).to(exchangeFanout());
    }
 
    @Bean
    public Queue queueTopic() {
        return new Queue("queue_topic");
    }

    @Bean
    public TopicExchange exchangeTopic() {
        TopicExchange build = ExchangeBuilder.topicExchange(EXCHANGE_TOPIC).durable(true).build();
        return build;
    }

 @Bean
    public Binding bindingTopic() {
        // #表示0个或若干个关键字，*表示一个关键字
        return BindingBuilder.bind(queueTopic()).to(exchangeTopic()).with("topic.#");
    }

```

2.  创建RabbitTemplate（消息模板）

    - 在与Spring AMQP整合的时候进行发送消息的关键类
    - 提供了丰富的发送消息方法，包括可靠性投递消息方法、回调监听消息接口ConfirmCallback、返回值确认接口ReturnCallback等等。
    同样需要注入到Spring容器中，然后直接使用。
    - 其中有个地方值得关注地方就是`template.setMessageConverter(new Jackson2JsonMessageConverter())` 
    这里设置的消息转换器必须跟后面消息消费端的保持一致，否则后面接收消息会出现异常错误信息（可支持自定义消息解析）
```
@Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        /** 配置 默认的RabbitTemplate 该地方可作为拓展点进行扩展*/
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        
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

```
3. 创建 一个缓存连接工厂`connectionFactory` 用于消息发送时的使用

```
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
```

## 4. RabbitProducerService 消息发送生产者服务

> 该类里面具体封装类不同模型的使用方法，后续如果需要可以自行添加，最主要的封装是放在rabbitMapper里面
>在这里我使用另一个封装消息发送的方法进行ID的封装，可以保证每次生成的ID都是唯一的，生成ID的`IdUtils` 工具类说明以附上 
```
    protected void sendData(String exchange, String routingKey, String message) {
        long id = new IdUtils().nextId();
        AmqpMessage amqpMessage = new AmqpMessage();
        amqpMessage.setMessage(message);
        amqpMessage.setMessageId(id);
        CorrelationData correlationData = new CorrelationData(id + "");
        //使用 convertAndSend方法时：输出时没有顺序，不需要等待，直接运行
        rabbitTemplate.convertAndSend(exchange, routingKey, amqpMessage, correlationData);
    }

```

## 5. 消费者接收配置`ConsumerConfig` 实现类`RabbitListenerConfigurer`类，主要是用来注册监听的
1. 这里主要说明的是`SimpleMessageListenerContainer`（简单消息监听容器）
    - 对这个类进行设置，对于消费者的配置项，这个类都可以满足
    - 监听队列（多个队列）、自动启动、自动声明功能
    - 设置事务特性、事务管理器、事务属性、事务容量（并发）、是否开始事务、回滚消息等
    - 设置消费者数量、最大最小数量、批量消费
    - 设置消息确认和自动确认模式、是否重回队列、异常捕获handler函数
    - 设置消费者标签生成策略、是否独占模式、消费者属性等
    - 设置具体的监听器、消息转换器等等
注意：SimpleMessageListenerContainer可以进行动态设置，比如在运行中的应用可以动态的修改其消费者数量的大小、接收消息的模式等
```
    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(myHandlerMethodFactory());
    }

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
            factory.setConcurrentConsumers(1);
            factory.setDefaultRequeueRejected(false);//拒绝消息时，true：重新发送，false：不会重新发送
            factory.setMaxConcurrentConsumers(DEFAULT_CONCURRENT);
            factory.setPrefetchCount(DEFAULT_PREFETCH_COUNT); //
            factory.setMessageConverter(new Jackson2JsonMessageConverter());
            return factory;
        }
```
2.具体的消息消费参考consumer包下的类方法定义
````
@RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitConfig.QUEUE_FANOUT, durable = "true"),
            exchange = @Exchange(value = RabbitConfig.EXCHANGE_FANOUT, durable = "true", type = "fanout", ignoreDeclarationExceptions = "true"))
            , containerFactory = "rabbitListenerContainerFactory")
这个注解里面包含队列和交换机的绑定，以及数据是否需要持久化，绑定的containerFactory
````

## 附加内容可参考相关文档：
[Spring AMQP中文文档](https://www.docs4dev.com/docs/zh/spring-amqp/2.1.2.RELEASE/reference/_reference.html#amqp)

[RabbitMQ 中文文档](http://rabbitmq.mr-ping.com/)

[rabbitmq 官方文档](https://www.rabbitmq.com/documentation.html)

[RABBITMQ整合SPRING AMQP、SPRINGBOOT、SPRING CLOUD STREAM](https://www.freesion.com/article/71141002211/)

[RabbitMQ：@RabbitListener 与 @RabbitHandler 及 消息序列化](https://www.jianshu.com/p/911d987b5f11)

[rabbitmq：publisher confirms](https://blog.csdn.net/weixin_38380858/article/details/93227652)








