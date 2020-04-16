package com.wxpay.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author zwq
 * @version 2.0
 * @date 2020-4-11 10:29:06
 */
@Component
@Getter
@Setter
@PropertySource("classpath:wxpay.properties")
@ConfigurationProperties(prefix = "wx.pay",ignoreUnknownFields = false)
public class WxGroupConfig {
    /** 微信支付 小程序APP_ID*/
    private String appletAppId;
    /** 微信支付 APP的APP_ID*/
    private String appId;
    /** 微信支付 key*/
    private String wxPayKey;
    /** 微信支付 商户id*/
    private String wxPayMchId;
    /** 微信支付 支付回调地址*/
    private String callBack;
    /** 微信支付 密钥*/
    private String appSecret;
}
