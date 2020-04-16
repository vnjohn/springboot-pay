package com.wxpay.controller;

import com.wxpay.config.WxGroupConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zwq
 * @version 2.0
 * @date 2020-4-11 11:00:54
 */
@RestController
public class HelloController {
    @Autowired private WxGroupConfig wxGroupConfig;

    @GetMapping("/hello")
    public String hello(){
        return wxGroupConfig.getWxPayKey();
    }
}
