package com.wxpay.service;

import com.wxpay.entity.WxPayBo;
import com.wxpay.entity.WxPayVo;

import java.util.Map;

/**
 * @author zwq
 * @version 2.0
 * @date 2020-4-13 11:21:11
 */
public interface WxPayService {
    /**
     * 微信 小程序 支付实现
     */
    WxPayVo wxAppletPay(WxPayBo wxPayBo);

    /**
     *  微信 APP支付实现
     * @param wxPayBo
     * @return
     */
    Map<String, String> wxAppPay(WxPayBo wxPayBo);

    String notifyApp(String resXml);

    boolean refund(String outTradeNo,String outRefundNo,
                   double totalFee,double refundFee,String refundDesc);
}
