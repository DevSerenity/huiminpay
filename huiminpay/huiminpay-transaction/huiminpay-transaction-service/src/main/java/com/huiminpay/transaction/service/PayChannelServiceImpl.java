package com.huiminpay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huiminpay.common.cache.Cache;
import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.common.cache.domain.CommonErrorCode;
import com.huiminpay.common.cache.util.RedisUtil;
import com.huiminpay.common.cache.util.StringUtil;
import com.huiminpay.transaction.api.PayChannelService;
import com.huiminpay.transaction.convert.PayChannelParamConvert;
import com.huiminpay.transaction.convert.PlatformChannelConvert;
import com.huiminpay.transaction.dto.PayChannelDTO;
import com.huiminpay.transaction.dto.PayChannelParamDTO;
import com.huiminpay.transaction.dto.PlatformChannelDTO;
import com.huiminpay.transaction.entity.AppPlatformChannel;
import com.huiminpay.transaction.entity.PayChannelParam;
import com.huiminpay.transaction.entity.PlatformChannel;
import com.huiminpay.transaction.mapper.AppPlatformChannelMapper;
import com.huiminpay.transaction.mapper.PayChannelParamMapper;
import com.huiminpay.transaction.mapper.PlatformChannelMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName PayChannelServiceImpl
 * @Description TODO
 * @Author 86150
 * @Date 2023/11/21 19:30
 * @Version 1.0
 */
@Log4j2
@Service
public class PayChannelServiceImpl implements PayChannelService {

    @Autowired
    private PlatformChannelMapper platformChannelMapper;

    @Autowired
    private AppPlatformChannelMapper appPlatformChannelMapper;

    @Autowired
    private PayChannelParamMapper payChannelParamMapper;

    @Resource
    private Cache cache;

    /**
     * 获取平台服务类型
     *
     * @return 列表<平台通道dto>
     * @throws BusinessException 业务异常
     */
    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException {
        List<PlatformChannel> platformChannels = platformChannelMapper.selectList(null);
        //对象转换
        List<PlatformChannelDTO> platformChannelDTOs = PlatformChannelConvert.INSTANCE.listentity2listdto(platformChannels);
        return platformChannelDTOs;
    }

    /**
     * 为应用绑定平台服务类型
     *
     * @param appId                应用程序id
     * @param platformChannelCodes 平台信道码
     * @throws BusinessException 业务异常
     */
    @Override
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException {
        //根据appId和平台服务类型code查询应用是否已经绑定过该服务类型
//        Integer count = appPlatformChannelMapper.selectCount(
//                new LambdaQueryWrapper<AppPlatformChannel>()
//                        .eq(AppPlatformChannel::getAppId, appId)
//                        .eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes));
        int count = queryAppBindPlatformChannel(appId, platformChannelCodes);
        if (count > 0){
            throw new BusinessException(CommonErrorCode.E_300011);//应用已经绑定该服务类型，不用重复绑定
        }
        AppPlatformChannel appPlatformChannel = new AppPlatformChannel();
        appPlatformChannel.setAppId(appId);
        appPlatformChannel.setPlatformChannel(platformChannelCodes);
        //没有绑定，则进行绑定
        appPlatformChannelMapper.insert(appPlatformChannel);

    }

    /**
     * 查询应用绑定的服务类型
     *
     * @param appId           应用程序id
     * @param platformChannel 平台通道
     * @return int
     * @throws BusinessException 业务异常
     */
    @Override
    public int queryAppBindPlatformChannel(String appId, String platformChannel) throws BusinessException {
        Integer count = appPlatformChannelMapper.selectCount(
                new LambdaQueryWrapper<AppPlatformChannel>()
                        .eq(AppPlatformChannel::getAppId, appId)
                        .eq(AppPlatformChannel::getPlatformChannel, platformChannel));
        if (count > 0) {
            return 1;
        }else {
            return 0;
        }
    }

    /**
     * 根据平台服务类型获取支付渠道列表
     *
     * @param platformChannelCode 平台信道码
     * @return 列表<支付通道dto>
     * @throws BusinessException 业务异常
     */
    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException {
        List<PayChannelDTO> payChannelDTOS = platformChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
        return payChannelDTOS;
    }

    /**
     * 保存支付渠道参数
     *
     * @param payChannelParamDTO
     * @throws BusinessException 业务异常
     */
    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) throws BusinessException {
        //进行参数校验
        if (payChannelParamDTO == null
                || StringUtil.isBlank(payChannelParamDTO.getAppId())
                || StringUtil.isBlank(payChannelParamDTO.getPlatformChannelCode())
                || StringUtil.isBlank(payChannelParamDTO.getParam())
                || StringUtil.isBlank(payChannelParamDTO.getChannelName())
                || StringUtil.isBlank(payChannelParamDTO.getPayChannel())){
            throw new BusinessException(CommonErrorCode.E_300009);//所需参数不够
        }
        //根据appid和服务类型查询应用与服务类型绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(payChannelParamDTO.getAppId(),payChannelParamDTO.getPlatformChannelCode());
        if (appPlatformChannelId == null){
            throw new BusinessException(CommonErrorCode.E_300010);//应用没有绑定服务类型，不允许配置参数
        }
        //先进行查询。如果存在参数配置，进行更新修改；如果没有参数配置，进行添加
        //根据应用与服务类型绑定id和支付渠道查询参数信息
        PayChannelParam payChannelParam = payChannelParamMapper.selectOne(new LambdaQueryWrapper<PayChannelParam>()
                .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId)
                .eq(PayChannelParam::getPayChannel, payChannelParamDTO.getPayChannel()));
        //已经存在参数配置，进行更新修改
        if (payChannelParam != null){
            payChannelParam.setChannelName(payChannelParamDTO.getChannelName());
            payChannelParam.setParam(payChannelParamDTO.getParam());
            payChannelParamMapper.updateById(payChannelParam);
            //不存在参数配置，进行添加
        }else {
            //对象转换 dto转entity
            PayChannelParam entity = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParamDTO);
            entity.setAppPlatformChannelId(appPlatformChannelId);
            payChannelParamMapper.insert(entity);
        }

        //更新redis中的缓存
        updateCache(payChannelParamDTO.getAppId(),payChannelParamDTO.getPlatformChannelCode());
    }


    /**
     * 更新redis缓存
     *
     * @param appId           应用程序id
     * @param platformChannel 平台通道
     */
    private void updateCache(String appId,String platformChannel){
        //处理redis缓存
        //1.key的构建 如：HM_PAY_PARAM:b910da455bc84514b324656e1088320b:huimin_c2b
        String redissKey = RedisUtil.keyBuilder(appId, platformChannel);
        //2.查询redis，检查key是否存在
        Boolean exists = cache.exists(redissKey);
        if (exists){//存在，进行删除,用于存放更新后的数据
            cache.del(redissKey);
        }
        //3.从数据库中查询应用的服务类型对应的实际支付参数，并重新存入缓存
        List<PayChannelParamDTO> paramDTOS = queryPayChannelParamByAppAndPlatform(appId, platformChannel);
        if (paramDTOS != null){
            //存入缓存，json的形式
            cache.set(redissKey, JSON.toJSON(paramDTOS).toString());
        }
    }

    /**
     * 根据应用和服务类型查询支付渠道参数列表 结果可能是多个(支付宝param 微信param)
     *
     * @param appId           应用程序id
     * @param platformChannel 服务类型
     * @return 列表<支付通道参数>
     * @throws BusinessException 业务异常
     */
    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException {
        //先查询redis,如果没有，再查询数据库
        //1,key的构建 如：HM_PAY_PARAM:b910da455bc84514b324656e1088320b:huimin_c2b
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        //判断是否有缓存
        Boolean exists = cache.exists(redisKey);
        if (exists){
            //从缓存中获取数据
            String s = cache.get(redisKey);
            //将json串转成 List<PayChannelParamDTO>,转换成需要的类型
            List<PayChannelParamDTO> payChannelParamDTOS = JSON.parseArray(s, PayChannelParamDTO.class);
            return payChannelParamDTOS;
        }//如果缓存中没有，则从数据库中查询

        //先查询出app_platform_channel的id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        if (appPlatformChannelId == null) {
            return null;
        }
        //再根据app_platform_channel的id查询所有参数
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>()
                .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
        //对象转换
        List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);

        //将查询到的数据放到缓存中
        cache.set(redisKey,JSON.toJSONString(payChannelParamDTOS));
        return payChannelParamDTOS;
    }

    /**
     * 获取指定应用指定服务类型下所包含的某个原始支付参数
     *
     * @param appId           应用程序id
     * @param platformChannel 服务类型
     * @param payChannel      支付通道
     * @return 付费通道参数
     * @throws BusinessException 业务异常
     */
    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException {
        //通过应用id和平台服务类型获取到所有支付参数
        List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppAndPlatform(appId, platformChannel);
        //遍历判断是否有符合要求的支付参数
        for (PayChannelParamDTO payChannelParamDTO : payChannelParamDTOS) {
            if (payChannelParamDTO.getPayChannel().equals(payChannel)) {
                return payChannelParamDTO;
            }
        }
        return null;
    }

    /**
     * 根据应用id 、服务类型code 查询应用与服务类型的绑定id
     *
     * @param appId               应用id
     * @param platformChannelCode 服务类型编码
     * @return 长
     */
    public Long selectIdByAppPlatformChannel(String appId,String platformChannelCode){
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));
        //校验查询的绑定id是非空
        if (appPlatformChannel != null){
            return appPlatformChannel.getId();
        }
        return null;
    }


}
