package com.valley.amqp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ProjectName: amqp-demo
 * @Package: com.valley.amqp.controller
 * @ClassName: IndexController
 * @Description: 启动默认加载页
 * @Author: wangzq
 * @Date: 2021/5/11 下午2:27
 */
@RestController
public class IndexController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }
}
