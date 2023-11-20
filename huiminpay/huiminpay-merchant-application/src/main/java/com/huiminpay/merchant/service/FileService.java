package com.huiminpay.merchant.service;

import com.huiminpay.common.cache.domain.BusinessException;

/**
 * @ClassName FileService
 * @Author: DevSerenity
 * @CreateDate: 2023/11/20 11:47
 * @UpdateDate: 2023/11/20 11:47
 * @Version: 1.0
 */
public interface FileService {

    /**
     * 上传
     *
     * @param bytes    字节
     * @param fileName 文件名称
     * @return 字符串
     * @throws BusinessException 业务异常
     */
    String upload(byte[] bytes,String fileName) throws BusinessException;
}
