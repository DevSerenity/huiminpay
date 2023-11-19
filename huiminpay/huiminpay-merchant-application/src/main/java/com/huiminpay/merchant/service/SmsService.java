package com.huiminpay.merchant.service;

import com.huiminpay.common.cache.domain.BusinessException;

/**
 * @ClassName SmsService
 * @Author: DevSerenity
 * @CreateDate: 2023/11/16 16:51
 * @UpdateDate: 2023/11/16 16:51
 * @Version: 1.0
 */
public interface SmsService {

    /**
     * 发送短信代码
     *
     * @param phone 电话
     * @return 字符串
     */
    String sendSmsCode(String phone) throws BusinessException;


    /**
     * 检查验证码
     *
     * @param verifiyKey  verifiy关键
     * @param verifiyCode verifiy代码
     */
    void checkVerifiyCode(String verifiyKey, String verifiyCode) throws BusinessException;
}
