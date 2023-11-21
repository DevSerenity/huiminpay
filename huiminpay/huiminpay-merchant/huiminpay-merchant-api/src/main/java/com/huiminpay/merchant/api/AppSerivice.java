package com.huiminpay.merchant.api;

import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.merchant.dto.AppDTO;

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
}
