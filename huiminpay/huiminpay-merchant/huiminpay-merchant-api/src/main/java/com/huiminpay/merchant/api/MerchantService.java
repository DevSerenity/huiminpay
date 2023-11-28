package com.huiminpay.merchant.api;

import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.merchant.dto.MerchantDTO;
import com.huiminpay.merchant.dto.StaffDTO;
import com.huiminpay.merchant.dto.StoreDTO;

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


    /**
     \* 商户下新增门店
     \* @param storeDTO
     */
    StoreDTO createStore(StoreDTO storeDTO) throws BusinessException;


    /**
     * 商户新增员工
     * @param staffDTO
     */
    StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException;



    /**
     * 为门店设置管理员
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    void bindStaffToStore(Long storeId, Long staffId) throws BusinessException;


    /**
     * 根据租户id查询商户的信息
     * @param tenantId
     * @return
     */
    public MerchantDTO queryMerchantByTenantId(Long tenantId);
}
