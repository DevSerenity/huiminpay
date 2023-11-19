package com.huiminpay.merchant.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * @ClassName SmsConfig
 * @Author: DevSerenity
 * @CreateDate: 2023/11/16 16:55
 * @UpdateDate: 2023/11/16 16:55
 * @Version: 1.0
 */

@Configuration
@PropertySource("classpath:application.properties") // 指定你的属性文件的位置
public class SmsConfig {

    @Autowired
    private Environment environment;

    @Bean
    public String smsUrl() {
        return environment.getProperty("sms.url");
    }

    @Bean
    public String effectiveTime() {
        return environment.getProperty("sms.effectiveTime");
    }
}