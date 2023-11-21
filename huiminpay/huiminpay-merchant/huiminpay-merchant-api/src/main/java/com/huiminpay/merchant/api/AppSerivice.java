package com.huiminpay.merchant.api;

import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.merchant.dto.AppDTO;

import java.util.List;

/**
 * @ClassName AppSerivice
 * @Author: DevSerenity
 * @CreateDate: 2023/11/21 14:10
 * @UpdateDate: 2023/11/21 14:10
 * @Version: 1.0
 */

public interface AppSerivice {


    /**
     * 创建应用程序
     *
     * @param merchantId 商户id
     * @param appDTO     AppDTO
     * @return 应用dto
     * @throws BusinessException 业务异常
     */
    AppDTO createApp(Long merchantId,AppDTO appDTO) throws BusinessException;


    /**
     * 商家查询应用
     *
     * @param merchantId 商人id
     * @return 列表<app dto>
     * @throws BusinessException 业务异常
     */
    List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException;


    /**
     * 按id获取应用
     *
     * @param id id
     * @return 应用dto
     * @throws BusinessException 业务异常
     */
    AppDTO getAppById(String id) throws BusinessException;
}
