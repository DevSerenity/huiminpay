package com.huiminpay.merchant.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName MybatisPlusConﬁg
 * @Author: DevSerenity
 * @CreateDate: 2023/11/15 14:56
 * @UpdateDate: 2023/11/15 14:56
 * @Version: 1.0
 */
@Configuration
@MapperScan("com.huiminpay.**.mapper")
public class MybatisPlusConfig {

    /**
     * 分页插件，自动识别数据库类型
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * 启用性能分析插件
     */
    @Bean
    public PerformanceInterceptor performanceInterceptor(){
        return new PerformanceInterceptor();
    }
}
