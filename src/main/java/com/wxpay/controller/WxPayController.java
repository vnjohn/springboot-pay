package com.wxpay.controller;

import com.wxpay.entity.WxPayBo;
import com.wxpay.entity.WxPayVo;
import com.wxpay.service.WxPayService;
import com.wxpay.utils.ResponseDataUtil;
import com.wxpay.utils.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author zwq
 * @version 2.0
 * @date 2020-4-13 14:11:19
 */
@RestController
@RequestMapping("/wx/")
public class WxPayController {
    @Autowired
    private WxPayService wxPayService;

    @PostMapping("/appletPay")
    @ResponseBody
    public Object wxPay(@RequestBody WxPayBo wxPayBo){
        WxPayVo wxPayVo = wxPayService.wxAppletPay(wxPayBo);
        if(wxPayVo!=null){
            return ResponseDataUtil.success("支付发起成功！",wxPayVo);
        }
        return ResponseDataUtil.failure(500,"支付发起失败！");
    }

    @PostMapping("/appWxPay")
    @ResponseBody
    public Object appWxPay(@RequestBody WxPayBo wxPayBo) {
        //回调地址，需要改成自己的
        Map<String, String> result= wxPayService.wxAppPay(wxPayBo);
        if(result!=null){
            return ResponseDataUtil.success("支付发起成功！",result);
        }
        return ResponseDataUtil.failure(500,"支付发起失败！");
    }

    /**
     *  APP和APPLET的回调接口地址 需要线上或者内网穿透的网络环境下
     */
    @SuppressWarnings({"static-access", "unused"})
    @PostMapping("/notifyApp")
    @ResponseBody
    public Object notifyApp(HttpServletRequest request) {
        String resXml = "";
        try {
            InputStream inputStream = request.getInputStream();
            //将InputStream转换成xmlString
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            resXml = sb.toString();
            String result = wxPayService.notifyApp(resXml);
            return ResponseDataUtil.success("SUCCESS", WXPayUtil.xmlToMap(result));
        } catch (Exception e) {
            System.out.println("微信手机支付失败:" + e.getMessage());
            String result = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
            return ResponseDataUtil.failure(500,"FAIL");
        }
    }

    /** 退款*/
    @PostMapping("/refund")
    @ResponseBody
    public Object refund(@RequestParam String no){
        return wxPayService.refund(no,no,0.01,0.01,null);
    }
}
