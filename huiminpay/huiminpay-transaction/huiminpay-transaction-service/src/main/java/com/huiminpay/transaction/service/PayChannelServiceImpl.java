package com.huiminpay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huiminpay.common.cache.Cache;
import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.common.cache.domain.CommonErrorCode;
import com.huiminpay.common.cache.util.RedisUtil;
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
import com.huiminpay.transaction.mapper.PayChannelMapper;
import com.huiminpay.transaction.mapper.PayChannelParamMapper;
import com.huiminpay.transaction.mapper.PlatformChannelMapper;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName PayChannelServiceImpl
 * @Author: DevSerenity
 * @CreateDate: 2023/11/21 18:55
 * @UpdateDate: 2023/11/21 18:55
 * @Version: 1.0
 */
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
     * 查询服务类型
     *
     * @return 列表<平台通道dto>
     * @throws BusinessException 业务异常
     */
    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException {

        //查询服务类型列表
        List<PlatformChannel> channelList = platformChannelMapper.selectList(null);
        List<PlatformChannelDTO> platformChannelDTOS = PlatformChannelConvert.INSTANCE.listentity2listdto(channelList);
        return platformChannelDTOS;
    }

    /**
     * 为应用绑定服务类型
     *
     * @param appId               应用程序id
     * @param platformChannelCode 平台信道码
     * @throws BusinessException 业务异常
     */
    @Override
    @Transactional
    public void bindPlatformChannelForApp(String appId, String platformChannelCode) throws BusinessException {
        //根据appId和平台服务类型code查询app_platform_channel
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));

        //判断没有规则时再进行绑定

        if(appPlatformChannel == null){
            appPlatformChannel = new AppPlatformChannel();
            appPlatformChannel.setAppId(appId);
            appPlatformChannel.setPlatformChannel(platformChannelCode);
            appPlatformChannelMapper.insert(appPlatformChannel);
        }
    }

    @Override
    public String queryAppBindPlatformChannel(String appId, String platformChannel) {
        int count = appPlatformChannelMapper.selectCount(
                new QueryWrapper<AppPlatformChannel>().lambda()
                        .eq(AppPlatformChannel::getAppId, appId)
                        .eq(AppPlatformChannel::getPlatformChannel, platformChannel));
        //已存在绑定关系返回1
        if (count > 0) {
            return 1+"已绑定";
        } else {
            return 0 +"未绑定";
        }
    }

    /**
     * 按平台通道查询付费通道
     *
     * @param
     * @return 列表<支付通道dto>
     * @throws BusinessException 业务异常
     */
    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) {
        return platformChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
    }


    /**
     * 保存支付渠道
     *
     * @param
     * @throws BusinessException 业务异常
     */
    @Override
    public void savePayChannelParm(PayChannelParamDTO payChannelParamDTO) throws BusinessException {
        if(payChannelParamDTO == null
                || StringUtils.isBlank(payChannelParamDTO.getAppId())
                || StringUtils.isBlank(payChannelParamDTO.getPlatformChannelCode())
                || StringUtils.isBlank(payChannelParamDTO.getPayChannel())){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //根据appid和服务类型查询应用与服务类型绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(payChannelParamDTO.getAppId(),                                                              payChannelParamDTO.getPlatformChannelCode());
        if(appPlatformChannelId == null){
            //应用未绑定该服务类型不可进行支付渠道参数配置
            throw new BusinessException(CommonErrorCode.E_300010);
        }
        //根据应用与服务类型绑定id和支付渠道查询参数信息
        PayChannelParam payChannelParam = payChannelParamMapper.selectOne(
                new LambdaQueryWrapper<PayChannelParam>()
                        .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId)
                        .eq(PayChannelParam::getPayChannel, payChannelParamDTO.getPayChannel()));
        //更新已有配置
        if (payChannelParam!=null){
            payChannelParam.setChannelName(payChannelParamDTO.getChannelName());
            payChannelParam.setParam(payChannelParamDTO.getParam());
            payChannelParamMapper.updateById(payChannelParam);
        }else{
            //添加新配置
            PayChannelParam entity = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParamDTO);
            entity.setId(null);
            //应用与服务类型绑定id
            entity.setAppPlatformChannelId(appPlatformChannelId);
            payChannelParamMapper.insert(entity);
        }


        updateCache(payChannelParamDTO.getAppId(),payChannelParamDTO.getPlatformChannelCode());
    }
    private void updateCache(String appId, String platformChannel) {
        //处理redis缓存
        //1.key的构建 如：HM_PAY_PARAM:b910da455bc84514b324656e1088320b:huimin_c2b
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        //2.查询redis,检查key是否存在
        Boolean exists = cache.exists(redisKey);
        if (exists) {//存在，则清除
            //删除原有缓存
            cache.del(redisKey);
        }
        //3.从数据库查询应用的服务类型对应的实际支付参数，并重新存入缓存
        List<PayChannelParamDTO> paramDTOS = queryPayChannelParamByAppAndPlatform(appId,platformChannel);
        if (paramDTOS != null) {
            //存入缓存
            cache.set(redisKey, JSON.toJSON(paramDTOS).toString());
        }
    }

    /**
     * 按应用平台和付费渠道查询参数
     *
     * @param appId           应用程序id
     * @param platformChannel 平台通道
     * @param payChannel      支付通道
     * @return 付费通道参数
     * @throws BusinessException 业务异常
     */
    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException {
        List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppAndPlatform(appId, platformChannel);
        for(PayChannelParamDTO payChannelParam:payChannelParamDTOS){
            if(payChannelParam.getPayChannel().equals(payChannel)){
                return payChannelParam;
            }
        }
        return null;
    }

    /**
     * 按应用和平台查询付费通道参数
     *
     * @param appId           应用程序id
     * @param platformChannel 平台通道
     * @return 列表<支付通道参数>
     * @throws BusinessException 业务异常
     */
    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) {
        //先从redis查询，如果有则返回
        //从缓存查询
        //1.key的构建 如：HM_PAY_PARAM:b910da455bc84514b324656e1088320b:huimin_c2b
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        //是否有缓存
        Boolean exists = cache.exists(redisKey);
        if(exists){
            //从redis获取支付渠道参数列表（json串）
            String PayChannelParamDTO_String = cache.get(redisKey);
            //将json串转成 List<PayChannelParamDTO>
            List<PayChannelParamDTO> payChannelParamDTOS = JSON.parseArray(PayChannelParamDTO_String, PayChannelParamDTO.class);
            return payChannelParamDTOS;
        }

        //缓存中没有 则从数据库查询  根据应用和服务类型找到它们绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        if(appPlatformChannelId == null){
            return null;
        }
        //应用和服务类型绑定id查询支付渠道参数记录
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(
                new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
        List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
        //保存到redis
        // updateCache(appId,platformChannel);
        cache.set(redisKey,JSON.toJSONString(payChannelParamDTOS));
        return payChannelParamDTOS;
    }


    /**
     * 根据应用id 、服务类型code 查询应用与服务类型的绑定id
     * @param appId
     * @param platformChannelCode
     * @return
     */
    private Long selectIdByAppPlatformChannel(String appId,String platformChannelCode){
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(
                new LambdaQueryWrapper<AppPlatformChannel>()
                        .eq(AppPlatformChannel::getAppId, appId)
                        .eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));
        if(appPlatformChannel!=null){
            return appPlatformChannel.getId();//应用与服务类型的绑定id
        }
        return null;
    }
}
