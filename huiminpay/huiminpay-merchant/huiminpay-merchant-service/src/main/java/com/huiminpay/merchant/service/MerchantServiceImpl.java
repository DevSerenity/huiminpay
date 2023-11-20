package com.huiminpay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.common.cache.domain.CommonErrorCode;
import com.huiminpay.common.cache.util.PhoneUtil;
import com.huiminpay.merchant.api.MerchantService;
import com.huiminpay.merchant.convert.MerchantConvert;
import com.huiminpay.merchant.dto.MerchantDTO;
import com.huiminpay.merchant.entity.Merchant;
import com.huiminpay.merchant.mapper.MerchantMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName MerchantServiceImpl
 * @Author: DevSerenity
 * @CreateDate: 2023/11/15 15:00
 * @UpdateDate: 2023/11/15 15:00
 * @Version: 1.0
 */
@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantMapper merchantMapper;

    /**
     * 按id查询商户
     *
     * @param
     * @return 商人dto
     */
    @Override
    public MerchantDTO queryMerchantById(Long id) throws BusinessException {
        MerchantDTO merchant = MerchantConvert.INSTANCE.
                entity2Dto(merchantMapper.selectById(id));
        return merchant;
    }

    /**
     * 创建商户
     *
     * @param merchantDTO 商人dto
     * @return 商人dto
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {
        if (merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //手机号非空校验
        if (StringUtils.isEmpty(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        //手机号合法性校验
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        //联系人非空校验
        if (StringUtils.isBlank(merchantDTO.getUsername())) {
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        //密码非空校验
        if (StringUtils.isBlank(merchantDTO.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        LambdaQueryWrapper<Merchant> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Merchant::getMobile,merchantDTO.getMobile());
        Integer count  = merchantMapper.selectCount(lqw);
        if(count>0){
            throw  new BusinessException(CommonErrorCode.E_100113);
        }

        Merchant merchant = MerchantConvert.INSTANCE.dto2Entity(merchantDTO);
        //设置审核状态0‐未申请,1‐已申请待审核,2‐审核通过,3‐审核拒绝
        merchant.setAuditStatus("0");
        //保存商户
        merchantMapper.insert(merchant);
        return MerchantConvert.INSTANCE.entity2Dto(merchant);
    }
}
