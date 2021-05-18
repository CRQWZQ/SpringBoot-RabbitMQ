package com.valley.amqp.consumer;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.valley.amqp.config.RabbitConfig;
import com.valley.amqp.core.AmqpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ProjectName: amqp
 * @Package: com.valley.amqp.consumer
 * @ClassName: TopicConsumer
 * @Description: topic模型 消息消费者
 * @Author: wangzq
 * @Date: 2021/5/14 上午11:02
 */
@Component
public class TopicConsumer {

    private static Logger log = LoggerFactory.getLogger(TopicConsumer.class);

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitConfig.QUEUE_TOPIC, durable = "true"),
            exchange = @Exchange(value = RabbitConfig.EXCHANGE_TOPIC, durable = "true", type = "topic", ignoreDeclarationExceptions = "true"),
            key = "topic.*"), containerFactory = "rabbitListenerContainerFactory")
    /**
     *
     *@RabbitListener 可以标注在类上面，需配合 @RabbitHandler 注解一起使用
     * @RabbitListener 标注在类上面表示当有收到消息的时候，就交给 @RabbitHandler 的方法处理，具体使用哪个方法处理，根据 MessageConverter 转换后的参数类型
     *
     */
//    @RabbitHandler
    public void receiveMessage(Message message, Channel channel) {
        boolean success = false;
        AmqpMessage amqpMessage = null;
        try {
            Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
            amqpMessage = (AmqpMessage) jackson2JsonMessageConverter.fromMessage(message);
            System.out.println("topicConsumer receive message info:" + JSONObject.toJSON(amqpMessage));
            //处理具体的业务逻辑，处理完成后，记得把返回标志位设置成true ，否则消息被重新放到消息队列或者拒绝丢弃掉

            success = true;
        } catch (Exception e) {
            log.error("topic模型消费rabbitmq时，解析数据信息异常：e={} | message={}", e.getMessage(), message);
        }

        if (success != true) {
            //这里是错误消息处理
            System.out.println("错误消息，message=" + message);

            try {
                //消息被nick后一直重新入队列然后一直重复消费
//                channel.basicNack((Long)headers.get(AmqpHeaders.DELIVERY_TAG), false, true);
                //拒绝消息  如果拒绝该消息，消息会被丢弃，不会重新拍到队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("topic模式下消费端拒绝消息异常：e={} | message={}", e.getMessage(), message);
            }
        }
        try {
            /**
             * 这个地方介绍消息接收后如何确认（需要 AcknowledgeMode.MANUAL）
             * deliveryTag（唯一标识 ID）：当一个消费者向 RabbitMQ 注册后，会建立起一个 Channel ，RabbitMQ 会用 basic.deliver 方法向消费者推送消息，这个方法携带了一个 delivery tag，
             * 它代表了 RabbitMQ 向该 Channel 投递的这条消息的唯一标识 ID，是一个单调递增的正整数，delivery tag 的范围仅限于 Channel
             * multiple：为了减少网络流量，手动确认可以被批处理，当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的所有消息
             *
             */
            //这个地方主要是确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (IOException e) {
            log.error("topic模式下消费端消息处理完成，消息确认时异常：e={}| messageId={}", e.getMessage(), amqpMessage.getMessageId());

        }
    }


}
