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
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 资质申请
     *
     * @param merchantId  商户id
     * @param merchantDTO 商户dto
     * @throws BusinessException 业务异常
     */
    @Override
    @Transactional
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        if(merchantId == null || merchantDTO == null){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //校验merchantId合法性，查询商户表，如果查询不到记录，认为非法
        Merchant merchant = merchantMapper.selectById(merchantId);
        if(merchant == null){
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        //将dto转成entity
        Merchant entity = MerchantConvert.INSTANCE.dto2Entity(merchantDTO);
        //将必要的参数设置到entity
        entity.setId(merchant.getId());
        entity.setMobile(merchant.getMobile());//因为资质申请的时候手机号不让改，还使用数据库中原来的手机号
        entity.setAuditStatus("1");//审核状态1-已申请待审核
        entity.setTenantId(merchant.getTenantId());
        //调用mapper更新商户表
        merchantMapper.updateById(entity);
    }
}
