package com.huiminpay.transaction.api;

import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.transaction.dto.PayChannelDTO;
import com.huiminpay.transaction.dto.PayChannelParamDTO;
import com.huiminpay.transaction.dto.PlatformChannelDTO;

import java.util.List;

/**
 * @ClassName PayChannelService
 * @Author: DevSerenity
 * @CreateDate: 2023/11/21 18:51
 * @UpdateDate: 2023/11/21 18:51
 * @Version: 1.0
 */
public interface PayChannelService {


    /**
     * 查询平台通道
     *
     * @return 列表<平台通道dto>
     * @throws BusinessException 业务异常
     */
    List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException;


    /**
     * 为应用绑定平台通道
     *
     * @param appId               应用程序id
     * @param platformChannelCode 平台信道码
     * @throws BusinessException 业务异常
     */
    void bindPlatformChannelForApp(String appId,String platformChannelCode) throws BusinessException;


    /**
     * 查询app绑定服务类型
     *
     * @param appId           应用程序id
     * @param platformChannel 平台通道
     * @return int
     * @throws BusinessException 业务异常
     */
    int queryAppBindPlatformChannel(String appId,String platformChannel) throws BusinessException;


    /**
     * 按平台服务类型获取支付渠道
     *
     * @param PayChannelCode 付费频道代码
     * @return 列表<支付通道dto>
     * @throws BusinessException 业务异常
     */
    List<PayChannelDTO> queryPayChannelByPlatformChannel(String PayChannelCode) throws  BusinessException;


    /**
     * 保存支付渠道参数
     *
     * @param channelParam
     * @throws BusinessException 业务异常
     */
    void savePayChannelParam(PayChannelParamDTO channelParam) throws BusinessException;


    /**
     * 按应用平台和付费渠道查询参数
     *
     * @param appId           应用程序id
     * @param platformChannel 平台通道
     * @param payChannel      支付通道
     * @return 付费通道参数
     * @throws BusinessException 业务异常
     */
    PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel,String payChannel) throws BusinessException;


    /**
     * 按应用和平台查询付费通道参数
     *
     * @param appId           应用程序id
     * @param platformChannel 平台通道
     * @return 列表<支付通道参数>
     * @throws BusinessException 业务异常
     */
    List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException;
}
