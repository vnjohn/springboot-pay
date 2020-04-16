package com.wxpay.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author zwq
 * @version 2.0
 * @date 2020-4-13 11:21:59
 */
@Getter
@Setter
public class WxPayBo {
    private BigDecimal payAmount; // 支付金额
    private String openId;//顾客唯一标识
}
