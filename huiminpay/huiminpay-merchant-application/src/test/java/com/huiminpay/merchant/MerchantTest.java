package com.huiminpay.merchant;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MerchantTest
 * @Author: DevSerenity
 * @CreateDate: 2023/11/16 15:39
 * @UpdateDate: 2023/11/16 15:39
 * @Version: 1.0
 */

@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
public class MerchantTest {


    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void getHtml(){
        String url = "https://www.baidu.com";
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        String body = forEntity.getBody();
        log.info("网页内容"+body);
    }


    /**
     * 测试发送短信代码
     */
    @Test
    public void testGetSmsCode(){
        //请求url
        String url = "http://localhost:56085/sailing/generate?effectiveTime=600&name=sms";
        String phoneNum="18339918463";
        //请求体
        HashMap<String, Object> map = new HashMap<>();
        map.put("mobile",phoneNum);

        //请求体
        HttpHeaders headers = new HttpHeaders();
        //设置数据格式
        headers.setContentType(MediaType.APPLICATION_JSON);
        //封装请求参数
        HttpEntity httpEntity = new HttpEntity(map, headers);

        //参数1：请求url，请求类型，请求参数，返回值类型
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        //根据responseEntity获取body
        Map responseEntityBody = responseEntity.getBody();
        //打印日志
        log.info("获取验证码{}",responseEntityBody);

        //判断数据是否为空
        if (responseEntityBody!=null && responseEntityBody.get("result") != null) {
            //获取key
            Map result = (Map) responseEntityBody.get("result");
            String key = result.get("key").toString();
            System.out.println(key);
        }

    }

}
