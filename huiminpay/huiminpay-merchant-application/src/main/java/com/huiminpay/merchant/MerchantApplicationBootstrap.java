package com.huiminpay.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @ClassName MerchantApplicationBootstrap
 * @Author: DevSerenity
 * @CreateDate: 2023/11/15 13:08
 * @UpdateDate: 2023/11/15 13:08
 * @Version: 1.0
 */

@SpringBootApplication
public class MerchantApplicationBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(MerchantApplicationBootstrap.class,args);
    }

    /**
     * 模板工具
     *
     * @return
     */
    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory());
        //通过res调用信息转换
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        //因为我们使用的是String 所以索引为1 设置统一编码格式为utf-8
        messageConverters.set(1,new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }
}