package com.valley.amqp.consumer;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.valley.amqp.config.RabbitConfig;
import com.valley.amqp.core.AmqpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ProjectName: amqp
 * @Package: com.valley.amqp.consumer
 * @ClassName: WorkConsumer
 * @Description: 工作模型 消息消费者
 * @Author: wangzq
 * @Date: 2021/5/14 上午10:51
 */
@Component
public class WorkConsumer {

    private static Logger log = LoggerFactory.getLogger(FanoutConsumer.class);

    //@RabbitListener 可以标记在类上，需要配合RabbitHandler使用
    //也可以单独标记在方法上，效果等于 两个注解相加
    @RabbitListener(queues = RabbitConfig.QUEUE_WORK, containerFactory = "rabbitListenerContainerFactory")

    public void receiveMessage(Message message, Channel channel) throws IOException {
        System.out.println("workConsumer receive message info: " + message);
        boolean success = false;
        AmqpMessage object = null;
        try {
            Jackson2JsonMessageConverter messageConverter = new Jackson2JsonMessageConverter();
            object  = (AmqpMessage) messageConverter.fromMessage(message);

        }catch (Exception e) {
            log.error("work模型消费端解析消息失败：e={}| message={}", e.getMessage(), message);
        }
        //接收到消息消费处理

        success = true;
        if (success != true) {
            try {
                /**
                 * 这个地方介绍消息接收后如何确认（需要 AcknowledgeMode.MANUAL）
                 * deliveryTag（唯一标识 ID）：当一个消费者向 RabbitMQ 注册后，会建立起一个 Channel ，RabbitMQ 会用 basic.deliver 方法向消费者推送消息，这个方法携带了一个 delivery tag，
                 * 它代表了 RabbitMQ 向该 Channel 投递的这条消息的唯一标识 ID，是一个单调递增的正整数，delivery tag 的范围仅限于 Channel
                 * multiple：为了减少网络流量，手动确认可以被批处理，当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的所有消息
                 *
                 */
                //这个地方主要是重新返回消息队列中
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);

            } catch (IOException e) {
                log.error("work模式下消费端拒绝消息异常：e={} | message={}", e.getMessage(), message);
            }
        }
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (IOException e) {
            log.error("work模式下消费端消息处理完成，消息确认时异常：e={}| message={}", e.getMessage(), JSONObject.toJSON(object));
        }

    }

}
