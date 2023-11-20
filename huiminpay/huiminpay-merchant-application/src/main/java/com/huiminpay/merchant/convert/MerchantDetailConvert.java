package com.huiminpay.merchant.convert;

import com.huiminpay.merchant.dto.MerchantDTO;
import com.huiminpay.merchant.vo.MerchantDetailVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MerchantDetailConvert {
    MerchantDetailConvert INSTANCE = Mappers.getMapper(MerchantDetailConvert.class);
    //将dto转成vo
    MerchantDetailVO dto2vo(MerchantDTO merchantDTO);
    //将vo转成dto
    MerchantDTO vo2dto(MerchantDetailVO merchantDetailVO);
}