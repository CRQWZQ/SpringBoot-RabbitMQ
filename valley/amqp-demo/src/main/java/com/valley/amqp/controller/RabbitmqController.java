package com.valley.amqp.controller;

import com.valley.amqp.config.RabbitConfig;
import com.valley.amqp.service.RabbitProducerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * @ProjectName: amqp-demo
 * @Package: com.valley.amqp.controller
 * @ClassName: RabbitmqController
 * @Description: rabbitmq消息控制层
 * @Author: wangzq
 * @Date: 2021/5/11 下午1:54
 */
@RestController
public class RabbitmqController {

    @Resource
    private RabbitProducerService rabbitmqService;

    /**
     * 工作模型下发送消息
     * @return
     */
    @RequestMapping("/sendWork")
    public Object sendWork() {
        String message = "我是work模型消息";
        rabbitmqService.sendWork(message);
        return "发送成功....";
    }

    /**
     * 发布模型下发送消息
     * @return
     */
    @GetMapping("/sendPublish")
    public String sendPublish() {
        String message = "我是发布模型（广播）消息";
        rabbitmqService.sendPublish(RabbitConfig.EXCHANGE_FANOUT, message);
        return "发送成功";
    }


}
