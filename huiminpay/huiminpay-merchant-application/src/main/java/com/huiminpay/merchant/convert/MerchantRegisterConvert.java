package com.huiminpay.merchant.convert;

import com.huiminpay.merchant.dto.MerchantDTO;
import com.huiminpay.merchant.vo.MerchantRegisterVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @ClassName MerchantRegisterConvert
 * @Author: DevSerenity
 * @CreateDate: 2023/11/19 12:01
 * @UpdateDate: 2023/11/19 12:01
 * @Version: 1.0
 */
@Mapper
public interface MerchantRegisterConvert {

    MerchantRegisterConvert INSTANCE =  Mappers.getMapper(MerchantRegisterConvert.class);

    MerchantDTO vo2Dto(MerchantRegisterVo vo);

    MerchantRegisterVo dto2Vo(MerchantDTO merchantDTO);
}
