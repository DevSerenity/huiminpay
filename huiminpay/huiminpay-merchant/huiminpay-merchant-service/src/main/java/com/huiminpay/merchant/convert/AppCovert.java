package com.huiminpay.merchant.convert;

import com.huiminpay.merchant.dto.AppDTO;
import com.huiminpay.merchant.entity.App;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AppCovert {
    AppCovert INSTANCE = Mappers.getMapper(AppCovert.class);

    AppDTO entity2dto(App entity);

    App dto2entity(AppDTO dto);

    List<AppDTO> listEntity2dto(List<App> app);
}