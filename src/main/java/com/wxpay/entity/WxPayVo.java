package com.wxpay.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zwq
 * @version 2.0
 * @date 2020-4-13 11:21:59
 */
@Getter
@Setter
public class WxPayVo {
    /** 预支付交易会话标识*/
    private String prepayId;
    /** 预支付交易生产的签名*/
    private String sign;
    /** 调用接口的公众号账号ID*/
    private String appId;
    /** 时间戳*/
    private String timeStamp;
    /** 随机字符串*/
    private String nonceStr;
    /** 微信支付类型：JSAPI、APP、NAVICAT*/
    private String signType;
    /** 支付的生成的订单号 商户订单 退款需要用到 返回给前端*/
    private String orderNumber;
}
