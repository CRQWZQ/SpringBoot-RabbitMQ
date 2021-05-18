package com.valley.amqp.wrapper;

import com.valley.amqp.config.RabbitConfig;
import com.valley.amqp.core.AmqpMessage;
import com.valley.amqp.utils.IdUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ProjectName: amqp-demo
 * @Package: com.valley.amqp.wrapper
 * @ClassName: RabbitMapper
 * @Description: rabbitmqMapper
 * @Author: wangzq
 * @Date: 2021/5/11 上午11:45
 */
@Component
public class RabbitMapper {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 工作模式发消息
     */
    public void sendWork(String message) {

        long id = new IdUtils().nextId();
        AmqpMessage amqpMessage = new AmqpMessage();
        amqpMessage.setMessage(message);
        amqpMessage.setMessageId(id);
        CorrelationData correlationData = new CorrelationData(id + "");
        //使用 convertAndSend方法时：输出时没有顺序，不需要等待，直接运行
        rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_WORK, amqpMessage, correlationData);
    }

    /**
     * 向发布订阅模式里面发送消息
     */
    public void sendPublish(String exchangeFanout, String message) {
        //todo 这里是发布订阅模式里面的发的信息内容

        sendData(exchangeFanout, "", message);
        //使用 convertSendAndReceive 方法时：按照一定的顺序，只有确认消费者接收到消息，才会发送下一个消息，每条消息之间会有间隔
        //rabbitTemplate.convertSendAndReceive("exchange_fanout","" ,"测试发布订阅模型：" + i);
    }

    /**
     * 向topic模型发送消息
     */
    public void sendTopic(String exchangeTopic, String routingKey, String message) {

        sendData(exchangeTopic, routingKey, message);
    }

    /**
     *功能描述: 封装统一消息发送
     * @author wangzq
     * @param exchange
     * @param routingKey
     * @param message
     * @return void
     */
    protected void sendData(String exchange, String routingKey, String message) {
        long id = new IdUtils().nextId();
        AmqpMessage amqpMessage = new AmqpMessage();
        amqpMessage.setMessage(message);
        amqpMessage.setMessageId(id);
        CorrelationData correlationData = new CorrelationData(id + "");
        //使用 convertAndSend方法时：输出时没有顺序，不需要等待，直接运行
        rabbitTemplate.convertAndSend(exchange, routingKey, amqpMessage, correlationData);
    }

}
