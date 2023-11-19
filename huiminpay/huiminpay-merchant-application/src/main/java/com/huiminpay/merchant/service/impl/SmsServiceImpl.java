package com.huiminpay.merchant.service.impl;

import com.alibaba.fastjson.JSON;
import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.common.cache.domain.CommonErrorCode;
import com.huiminpay.merchant.config.SmsConfig;
import com.huiminpay.merchant.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName SmsServiceImpl
 * @Author: DevSerenity
 * @CreateDate: 2023/11/16 16:52
 * @UpdateDate: 2023/11/16 16:52
 * @Version: 1.0
 */
@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${sms.url}")
    private String smsUrl;
    @Value("${sms.effectiveTime}")
    private String effectiveTime;

    /**
     * 发送短信代码
     *
     * @param phone 电话
     * @return 字符串
     */
    @Override
    public String sendSmsCode(String phone) throws BusinessException {
        //请求url
        String url = smsUrl + "/generate?name=sms&effectiveTime="+effectiveTime;

        log.info("发送验证码：{}",url);

        //请求体
        HashMap<String, Object> map = new HashMap<>();
        map.put("mobile",phone);

        //请求头
        HttpHeaders headers = new HttpHeaders();
        //设置数据格式
        headers.setContentType(MediaType.APPLICATION_JSON);
        //封装请求参数
        HttpEntity httpEntity = new HttpEntity(map, headers);
        Map responseEntityBody = null;
        //参数1：请求url，请求类型，请求参数，返回值类型
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        //根据responseEntity获取body
        responseEntityBody = responseEntity.getBody();
        //打印日志
        log.info("获取验证码{}",responseEntityBody);

        //判断数据是否为空
        if (responseEntityBody==null && responseEntityBody.get("result") == null) {
            throw new BusinessException(CommonErrorCode.E_100102);
        }
        //获取key
        Map result = (Map) responseEntityBody.get("result");
        String key = result.get("key").toString();
        return key;
    }

    /**
     * 检查验证码
     *
     * @param verifiyKey  verifiy关键
     * @param verifiyCode verifiy代码
     */
    @Override
    public void checkVerifiyCode(String verifiyKey, String verifiyCode) throws BusinessException {
        String url = smsUrl + "/verify?name=sms&verificationCode=" + verifiyCode + "&verificationKey=" + verifiyKey;


        Map responseMap = null;
        try {
            //请求校验验证码
            ResponseEntity<Map> exchange = restTemplate
                    .exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Map.class);
            responseMap = exchange.getBody();
            log.info("校验验证码，响应内容：{}", JSON.toJSONString(responseMap));
        } catch (RestClientException e) {
            e.printStackTrace();
            log.info(e.getMessage(), e);
            throw new BusinessException(CommonErrorCode.E_100102); //验证码错误
        }
        if (responseMap == null || responseMap.get("result") == null || !(Boolean)responseMap.get("result")) {
            throw new BusinessException(CommonErrorCode.E_100103); //验证码错误
        }
    }
}
