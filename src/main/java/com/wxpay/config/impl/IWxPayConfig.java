package com.wxpay.config.impl;

import com.wxpay.config.WXPayConfig;
import com.wxpay.entity.IWXPayDomain;
import com.wxpay.entity.WXPayConstants;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author zwq
 * @version 2.0
 * @date 2020-1-17 12:17:41
 */
@Service
public class IWxPayConfig extends WXPayConfig {

    private byte[] certData;

    public IWxPayConfig() throws Exception {
        // TODO 此处引入微信支付 需要用到的证书  支付、退款都需要
        ClassPathResource classPathResource = new ClassPathResource("cert/apiclient_cert.p12");
        // TODO 获取文件流
        InputStream certStream = classPathResource.getInputStream();
        this.certData = IOUtils.toByteArray(certStream);
        certStream.read(this.certData);
        certStream.close();
    }

    @Override
    public InputStream getCertStream() {
        return new ByteArrayInputStream(this.certData);
    }

    @Override
    public IWXPayDomain getWXPayDomain() {
        IWXPayDomain iwxPayDomain = new IWXPayDomain() {
            @Override
            public void report(String domain, long elapsedTimeMillis, Exception ex) {

            }
            @Override
            public DomainInfo getDomain(WXPayConfig config) {
                return new IWXPayDomain.DomainInfo(WXPayConstants.DOMAIN_API, true);
            }
        };
        return iwxPayDomain;
    }
}
