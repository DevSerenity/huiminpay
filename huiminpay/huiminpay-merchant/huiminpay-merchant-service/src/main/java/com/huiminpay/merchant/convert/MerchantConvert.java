package com.huiminpay.merchant.convert;

import com.huiminpay.merchant.dto.MerchantDTO;
import com.huiminpay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @ClassName MerchantConvert
 * @Author: DevSerenity
 * @CreateDate: 2023/11/19 11:43
 * @UpdateDate: 2023/11/19 11:43
 * @Version: 1.0
 */
@Mapper
public interface MerchantConvert {
   MerchantConvert INSTANCE = Mappers.getMapper(MerchantConvert.class);


   /**
    * entity2 dto
    *
    * @param merchant 商户
    * @return 商人dto
    */
   MerchantDTO entity2Dto(Merchant merchant);


   /**
    * dto2实体
    *
    * @param merchantDTO
    * @return
    */
   Merchant dto2Entity(MerchantDTO merchantDTO);

   List<MerchantDTO> entityList2DtoList(List<Merchant> merchantList);


   List<Merchant> dtoList2EntityList(List<MerchantDTO> merchantDTOList);

   public static void main(String[] args) {


      //dto转entity
      MerchantDTO merchantDTO=new MerchantDTO();
      merchantDTO.setUsername("测试");
      merchantDTO.setPassword("111");
      Merchant entity=MerchantConvert.INSTANCE.dto2Entity(merchantDTO);
      System.out.println(entity);

   }
}
