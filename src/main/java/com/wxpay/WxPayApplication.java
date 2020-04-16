package com.wxpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zwq
 * @version 2.0
 * @date 2020-4-11 10:56:00
 */
@SpringBootApplication
public class WxPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(WxPayApplication.class,args);
        System.out.println("启动成功");
    }
}
