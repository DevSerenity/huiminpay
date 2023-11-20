package com.huiminpay.merchant.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName Config
 * @Author: DevSerenity
 * @CreateDate: 2023/11/20 14:03
 * @UpdateDate: 2023/11/20 14:03
 * @Version: 1.0
 */
@Configuration
public class Config {
    @Value("${oss.aliyun.endpoint}")
    private String endpoint;
    @Value("${oss.aliyun.accessKeyId}")
    private String accessKeyId;
    @Value("${oss.aliyun.accessKeySecret}")
    private String accessKeySecret;

    /**
     * 阿里云OSS
     * @return
     */
    @Bean
    public OSS oss(){
        return new OSSClientBuilder().build(endpoint,accessKeyId,accessKeySecret);
    }
}
