package com.wxpay.service.impl;

import com.wxpay.config.WxGroupConfig;
import com.wxpay.config.impl.IWxPayConfig;
import com.wxpay.entity.WXPay;
import com.wxpay.entity.WXPayConstants;
import com.wxpay.entity.WxPayBo;
import com.wxpay.entity.WxPayVo;
import com.wxpay.enums.PaymentType;
import com.wxpay.service.WxPayService;
import com.wxpay.utils.WXPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Service
public class WxPayServiceImpl  implements WxPayService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static final String WX_APPLET_PAY="wxAppletPay";
    private static final String WX_APP_PAY="wxAppPay";
    @Autowired
    private IWxPayConfig iWxPayConfig;
    @Autowired
    private WxGroupConfig wxGroupConfig;

    /**
     * 微信小程序 支付
     * @param wxPayBo
     * @return
     */
    @Override
    public WxPayVo wxAppletPay(WxPayBo wxPayBo) {
        WxPayVo resp = new WxPayVo();
        logger.info("transactionDo start, param={}", wxPayBo.getPayAmount());
        /* 前端传入的支付金额 */
        final BigDecimal payAmount = wxPayBo.getPayAmount();
        //查询到对应的机构信息
        String orderNumber =WX_APPLET_PAY+System.currentTimeMillis();
        //设置订单编号
        // 发起微信支付
        WXPay wxpay;
        try {
            // *** 注入自己实现的微信配置类, 创建WXPay核心类, WXPay 包括统一下单接口
            wxpay = new WXPay(iWxPayConfig);
            /* sp 1. 拼装请求参数 */
            Map<String, String> data = new HashMap<>();
            data.put("appid", wxGroupConfig.getAppletAppId());
            data.put("mch_id", wxGroupConfig.getWxPayMchId());
            data.put("body", "JSAPI支付测试");
            data.put("out_trade_no", orderNumber);
            data.put("total_fee", String.valueOf(payAmount.multiply(new BigDecimal(100)).intValue()));
            data.put("spbill_create_ip", getHostIp());
            data.put("openid", wxPayBo.getOpenId());
            data.put("notify_url", wxGroupConfig.getCallBack()); // 订单回调接口
            data.put("trade_type", PaymentType.JSAPI.toString());
            data.put("nonce_str", WXPayUtil.generateNonceStr());   // 随机字符串小于32位
            /*  sp 2. 调用微信支付下单接口 */
            System.out.println("requestApply:"+data.toString());
            logger.info("发起微信支付下单接口, request={}", data);
            Map<String, String> response = wxpay.unifiedOrder(data);
            logger.info("微信支付下单成功, 返回值 response={}", response);
            /*  sp 3. 对支付结果进行判断 */
            String returnCode = response.get("return_code");
            if (!WXPayConstants.SUCCESS.equals(returnCode)) {//调用微信下单成功
                return null;
            }
            String resultCode = response.get("result_code");
            if (!WXPayConstants.SUCCESS.equals(resultCode)) {
                return null;
            }
            String prepay_id = response.get("prepay_id");
            if (prepay_id == null) {
                return null;
            }
            /* sp 4. 处理下单结果内容，生成前端调起微信支付的sign */
            final String nonceStr = UUID.randomUUID().toString().replaceAll("-", "");
            final String packages = "prepay_id=" + prepay_id;
            Map<String, String> wxPayMap = new HashMap<>();
            wxPayMap.put("appId", wxGroupConfig.getAppletAppId());
            wxPayMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
            wxPayMap.put("nonceStr", nonceStr);
            wxPayMap.put("package", packages);
            wxPayMap.put("signType", "MD5");
            String sign = WXPayUtil.generateSignature(wxPayMap, wxGroupConfig.getWxPayKey());
            resp.setTimeStamp(String.valueOf(WXPayUtil.getCurrentTimestampMs()));
            resp.setNonceStr(nonceStr);
            resp.setSignType("MD5");
            resp.setPrepayId(prepay_id);
            resp.setSign(sign);
            resp.setAppId(wxGroupConfig.getAppId());
            /* sp 5. 前端需要把该订单返给我作为订单表里面的字段值 */
            resp.setOrderNumber(orderNumber);
        } catch (Exception e) {
            logger.error("transactionDo error", e);
            throw new RuntimeException("系统繁忙,请稍后重试!");
        }
        return resp;
    }

    /**
     *  微信APP支付
     * @param wxPayBo
     * @return
     */
    @Override
    public Map<String, String> wxAppPay(WxPayBo wxPayBo) {
        final BigDecimal payAmount = wxPayBo.getPayAmount();
        WXPay wxpay;
        Map<String, String> returnMap = null;
        try {
            //生成商户订单号，不可重复
            String out_trade_no = WX_APP_PAY+ System.currentTimeMillis();
            wxpay = new WXPay(iWxPayConfig);
            /* sp 1. 拼装请求下单接口 参数 */
            Map<String, String> data = new HashMap<>();
            data.put("appid", wxGroupConfig.getAppId());
            data.put("mch_id", wxGroupConfig.getWxPayMchId());
            data.put("fee_type", "CNY");
            data.put("body", "APP支付测试");
            data.put("out_trade_no", out_trade_no);
            data.put("total_fee", String.valueOf(payAmount.multiply(new BigDecimal(100)).intValue()));
            data.put("spbill_create_ip", getHostIp());
            data.put("notify_url", wxGroupConfig.getCallBack()); // 订单回调接口
            data.put("trade_type", "APP");
            data.put("nonce_str", WXPayUtil.generateNonceStr());   // 随机字符串小于32位
            /*  sp 2.设置签名*/
            String sign = WXPayUtil.generateSignature(data, wxGroupConfig.getWxPayKey());
            data.put("sign", sign);
            //在方法中已经设置过一次签名  无须再次设置签名
            returnMap = wxpay.unifiedOrder(data);
            /*  sp 3. 对支付结果进行判断 */
            if (returnMap==null){
                return null;
            }
            String returnCode = returnMap.get("return_code");
            if (!WXPayConstants.SUCCESS.equals(returnCode)) {//调用微信下单成功
                return null;
            }
            String resultCode = returnMap.get("result_code");
            if (!WXPayConstants.SUCCESS.equals(resultCode)) {
                return null;
            }
            /*  sp 4. 调起支付接口  封装请求参数*/
            Map<String, String> paramMap = null;
            paramMap.put("appid",returnMap.get("appid"));
            paramMap.put("partnerId",returnMap.get("mch_id"));
            paramMap.put("nonceStr",returnMap.get("nonce_str"));
            paramMap.put("prepayId",returnMap.get("prepay_id"));
            paramMap.put("package","Sign=WXPay");
            paramMap.put("timeStamp",String.valueOf(WXPayUtil.getCurrentTimestampMs()));
            // TODO     参加调起支付的签名字段有且只能是6个，分别为appid、partnerid、prepayid、package、noncestr和timestamp，而且都必须是小写
            // TODO     超级坑  sign 不是拿微信返回的 sign，而是自己再签一次，返回给客户端
            //此处的签名不是上次生成的签名 需要重新生成一个签名
            paramMap.put("sign",WXPayUtil.generateSignature(paramMap, wxGroupConfig.getWxPayKey()));
            return paramMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
            //系统等其他错误的时候
        }
    }

    /**
     * 得到本地机器的IP
     *
     * @return
     */
    private static String getHostIp() {
        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }

    @Override
    public String notifyApp(String resXml)  {
        String xmlBack = "";
        try {
            WXPay wxpay = new WXPay(iWxPayConfig);
            Map<String, String> notifyMap = null;
            notifyMap = WXPayUtil.xmlToMap(resXml);         // 调用官方SDK转换成map类型数据
            if (wxpay.isPayResultNotifySignatureValid(notifyMap)) {//验证签名是否有效，有效则进一步处理
                String return_code = notifyMap.get("return_code");//状态
                String out_trade_no = notifyMap.get("out_trade_no");//商户订单号
                if (return_code.equals("SUCCESS")) {
                    if (out_trade_no != null) {
                        // 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户的订单状态从退款改成支付成功
                        // 注意特殊情况：微信服务端同样的通知可能会多次发送给商户系统，所以数据持久化之前需要检查是否已经处理过了，处理了直接返回成功标志
                        //业务数据持久化
                        logger.info("微信手机支付回调成功订单号:{}", out_trade_no);
                        xmlBack = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>" + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
                    } else {
                        logger.info("微信手机支付回调失败订单号:{}", out_trade_no);
                        xmlBack = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
                    }
                }
                return xmlBack;
            } else {
                // 签名错误，如果数据里没有sign字段，也认为是签名错误
                //失败的数据要不要存储？
                logger.error("手机支付回调通知签名错误");
                xmlBack = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
                return xmlBack;
            }
        } catch (Exception e) {
            logger.error("手机支付回调通知失败", e);
            xmlBack = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
        }
        return xmlBack;
    }

    /**
     *  退款通道
     *  拿到对应的商户号和APPId 以及对应的个人信息
     * @param outTradeNo 商户订单号
     * @param outRefundNo 商户退款单号
     * @param refundDesc 退款原因
     * @return
     */
    public boolean refund(String outTradeNo,String outRefundNo,
                          double totalFee,double refundFee,String refundDesc){
        WXPay wxPay=null;
        // 1.0 拼凑微信退款需要的参数
        String app_id = wxGroupConfig.getAppletAppId(); // 微信公众号的appid
        String mch_id = wxGroupConfig.getWxPayMchId(); // 商户号
        String nonce_str = WXPayUtil.generateNonceStr(); // 生成随机数
        String total_fee =String.valueOf(totalFee);//订单金额
        String refund_fee = String.valueOf(refundFee);//退款金额
        // 2.0 生成map集合
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", app_id); // 微信公众号的appid
        packageParams.put("mch_id", mch_id); // 商务号
        packageParams.put("nonce_str", nonce_str); // 随机生成后数字，保证安全性
        packageParams.put("out_trade_no", outTradeNo);//商户订单号
        packageParams.put("out_refund_no", outRefundNo);//商户退款单号
        packageParams.put("total_fee", total_fee);//退款总额
        packageParams.put("refund_fee", refund_fee);//退款金额
        packageParams.put("refund_desc",refundDesc);//退款事由
        try {
            wxPay=new WXPay(iWxPayConfig);
            // 3.0 利用上面的参数，先去生成自己的签名
            String sign = WXPayUtil.generateSignature(packageParams, wxGroupConfig.getWxPayKey());
            // 4.0 将签名再放回map中，它也是一个参数
            packageParams.put("sign", sign);
            Map<String, String> returnMap = wxPay.refund(packageParams);
            if (returnMap.get("return_code").equals("SUCCESS")) {
                // 退款成功
                logger.info("returnMap为:" + returnMap);
            }
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
