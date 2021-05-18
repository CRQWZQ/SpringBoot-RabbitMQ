package com.valley.amqp.listener;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.valley.amqp.core.AmqpMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.io.IOException;
import java.util.Map;

/**
 *功能说明：该方法为初代方法，现在已经弃用，具体使用方式可参考 consumer包下的方法
 * @ProjectName: amqp-demo
 * @Package: com.valley.amqp.listener
 * @ClassName: WorkReceiverListener
 * @Description: work模型监听器 接收实例 后续针对具有消息接收的情况单独创建一个包去匹配
 * @Author: wangzq
 * @Date: 2021/5/11 上午11:51
 */
@Deprecated
//@Component
public class ReceiveListener {

    //    @RabbitListener(queues = "queue_work", containerFactory = "pointTaskContainerFactory")
    public void receiveMessage(String msg, Channel channel, Message message) {
        //发送的信息
        System.out.println("接收到work消息：" + msg);
        // channel 通道信息
        //message 附加的参数信息


    }

//    @RabbitListener(queues = RabbitConfig.QUEUE_FANOUT)
    public void receiveMessage(Message msg, Channel channel) throws IOException {
        Map<String, Object> headers = msg.getMessageProperties().getHeaders();

        //标志为状态，用来判断确认消息是否回调完成
        boolean success = false;
        try {
            Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
            AmqpMessage amqpMessage = (AmqpMessage) jackson2JsonMessageConverter.fromMessage(msg);

            // 输出 发送的消息信息
            System.out.println(JSONObject.toJSON(amqpMessage));
            success = true;
        } catch (Exception e) {
            //转换异常的日志打印输出
            System.out.println("消费端解析用户发送的数据信息解析失败，失败原因 e=" + e.getMessage());
        }

        if (success) {
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }

    //    @RabbitListener(queues = "queue_topic")
    public void receiveMsgFromTopic(String msg) {
        System.out.println("消费者接收到topic消息：" + msg);
    }

}
