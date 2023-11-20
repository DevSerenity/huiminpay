package com.huiminpay.merchant.api;

import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.merchant.dto.MerchantDTO;

/**
 * @ClassName MerchantService
 * @Author: DevSerenity
 * @CreateDate: 2023/11/15 14:57
 * @UpdateDate: 2023/11/15 14:57
 * @Version: 1.0
 */
public interface MerchantService {

    /**
     * 按id查询商户
     *
     * @param merchantId 商人id
     * @return 商人dto
     */
    MerchantDTO queryMerchantById(Long id) throws BusinessException;


    /**
     * 创建商户
     *
     * @param merchantDTO 商人dto
     * @return 商人dto
     */
    MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException;


    /**
     * 资质申请接口
     *
     * @param merchantId  商户id
     * @param merchantDTO 商户dto
     * @throws BusinessException 业务异常
     */
    void applyMerchant(Long merchantId,MerchantDTO merchantDTO) throws BusinessException;
}
