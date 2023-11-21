package com.huiminpay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.common.cache.domain.CommonErrorCode;
import com.huiminpay.common.cache.util.RandomUuidUtil;
import com.huiminpay.merchant.api.AppSerivice;
import com.huiminpay.merchant.convert.AppCovert;
import com.huiminpay.merchant.dto.AppDTO;
import com.huiminpay.merchant.entity.App;
import com.huiminpay.merchant.entity.Merchant;
import com.huiminpay.merchant.mapper.AppMapper;
import com.huiminpay.merchant.mapper.MerchantMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName AppServiceImpl
 * @Author: DevSerenity
 * @CreateDate: 2023/11/21 14:12
 * @UpdateDate: 2023/11/21 14:12
 * @Version: 1.0
 */

@Service
public class AppServiceImpl implements AppSerivice {


    @Autowired
    private AppMapper appMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public AppDTO createApp(Long merchantId, AppDTO app) throws BusinessException {
        //校验商户是否通过资质审核
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || merchantId == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        if (!"2".equals(merchant.getAuditStatus())) {
            throw new BusinessException(CommonErrorCode.E_200003);
        }
        if(isExistAppName(app.getAppName())){
            throw new BusinessException(CommonErrorCode.E_200004);
        }

        //保存应用信息
        app.setAppId(RandomUuidUtil.getUUID());
        app.setMerchantId(merchant.getId());
        App entity = AppCovert.INSTANCE.dto2entity(app);
        appMapper.insert(entity);
        return AppCovert.INSTANCE.entity2dto(entity);
    }

    /**
     * 商家查询应用
     *
     * @param merchantId 商人id
     * @return 列表<app dto>
     * @throws BusinessException 业务异常
     */
    @Override
    public List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException {
        List<App> apps = appMapper.selectList(new LambdaQueryWrapper<App>()
                .eq(App::getMerchantId, merchantId));
        //list->dto
        List<AppDTO> appDTOS = AppCovert.INSTANCE.listEntity2dto(apps);
        return appDTOS;
    }

    /**
     * 按id获取应用
     *
     * @param id id
     * @return 应用dto
     * @throws BusinessException 业务异常
     */
    @Override
    public AppDTO getAppById(String id) throws BusinessException {
        App app = appMapper.selectOne(new QueryWrapper<App>().lambda().eq(App::getAppId, id));
        return AppCovert.INSTANCE.entity2dto(app);
    }

    /**
     * 校验应用名是否已被使用
     * @param appName
     * @return
     */
    public Boolean isExistAppName(String appName) {
        Integer count = appMapper.selectCount(new QueryWrapper<App>
                ().lambda().eq(App::getAppName, appName));
        return count.intValue() > 0;
    }
}
