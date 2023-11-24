package com.huiminpay.merchant.controller;

import com.huiminpay.merchant.common.util.SecurityUtil;
import com.huiminpay.transaction.api.PayChannelService;
import com.huiminpay.transaction.dto.PayChannelDTO;
import com.huiminpay.transaction.dto.PayChannelParamDTO;
import com.huiminpay.transaction.dto.PlatformChannelDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName PlatformParamController
 * @Author: DevSerenity
 * @CreateDate: 2023/11/21 19:01
 * @UpdateDate: 2023/11/21 19:01
 * @Version: 1.0
 */
@Api(value = "商户平台-渠道和支付参数相关", tags = "商户平台-渠道和支付参数", description = "商户平台-渠道和支付参数相关")
@Slf4j
@RestController
public class PlatformParamController {

    @Reference
    private PayChannelService payChannelService;



    @ApiOperation("获取平台服务类型")
    @GetMapping(value="/my/platform-channels")
    public List<PlatformChannelDTO> queryPlatformChannel(){
        return payChannelService.queryPlatformChannel();
    }

    /**
     * 绑定应用平台
     *
     * @param appId               应用程序id
     * @param platformChannelCode 平台信道码
     */
    @ApiOperation("绑定服务类型")
    @PostMapping(value="/my/apps/{appId}/platform-channels")
    @ApiImplicitParams({@ApiImplicitParam(value = "应用id",name = "appId",dataType = "string",paramType = "path"),
            @ApiImplicitParam(value = "服务类型code",name = "platformChannelCodes",dataType = "string",paramType = "query")
    })
    public void bindPlatformForApp(@PathVariable("appId") String appId,
                                   @RequestParam("platformChannelCode") String platformChannelCode){
        payChannelService.bindPlatformChannelForApp(appId,platformChannelCode);
    }


    /**
     * 查询app绑定平台通道
     *
     * @param appId           应用程序id
     * @param platformChannel 平台通道
     * @return int
     */
    @ApiOperation("查询应用是否绑定了某个服务类型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用appId", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "platformChannel", value = "服务类型", required = true, dataType = "String", paramType = "query")
    })
    @GetMapping("/my/merchants/apps/platformchannels")
    public String queryAppBindPlatformChannel(@RequestParam String appId, @RequestParam String platformChannel){
        return payChannelService.queryAppBindPlatformChannel(appId,platformChannel);
    }


    /**
     * 按平台渠道查询付费渠道
     *
     * @param platformChannelCode 平台信道码
     * @return 列表<支付通道dto>
     */
    @ApiOperation("根据平台服务类型获取支付渠道列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "platformChannelCode", value = "服务类型编码", required =
                    true, dataType = "String", paramType = "path")
    })
    @GetMapping(value="/my/pay-channels/platform-channel/{platformChannelCode}")
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(@PathVariable String platformChannelCode){
        return payChannelService.queryPayChannelByPlatformChannel(platformChannelCode);
    }


    /**
     * 创建付费通道parm
     *
     * @param payChannelParamDTO 付费通道参数
     */
    @ApiOperation("商户配置支付渠道参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payChannelParam", value = "商户配置支付渠道参数",
                    required = true, dataType = "PayChannelParamDTO", paramType = "body")
    })
    @RequestMapping(value = "/my/pay-channel-params",
            method = {RequestMethod.POST,RequestMethod.PUT})
    public void createPayChannelParm(@RequestBody PayChannelParamDTO payChannelParamDTO){
        Long merchantId = SecurityUtil.getMerchantId();
        payChannelParamDTO.setMerchantId(merchantId);
        payChannelService.savePayChannelParm(payChannelParamDTO);
    }


    /**
     * 查询付费通道参数
     *
     * @param appId           应用程序id
     * @param platformChannel 平台通道
     * @return 列表<支付通道参数>
     */
    @ApiOperation("获取指定应用指定服务类型下所包含的原始支付渠道参数列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用id", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "platformChannel", value = "服务类型", required = true, dataType = "String", paramType = "path")})
    @GetMapping(value = "/my/pay-channel-params/apps/{appId}/platform-channels/{platformChannel}")
    public List<PayChannelParamDTO> queryPayChannelParam(@PathVariable String appId, @PathVariable String platformChannel) {
        return payChannelService.queryPayChannelParamByAppAndPlatform(appId, platformChannel);
    }

    /**
     * 查询付费通道参数
     *
     * @param appId           应用程序id
     * @param platformChannel 平台通道
     * @param payChannel      支付通道
     * @return 付费通道参数
     */
    @ApiOperation("获取指定应用指定服务类型下所包含的某个原始支付参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用id", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "platformChannel", value = "平台支付渠道编码", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "payChannel", value = "实际支付渠道编码", required = true, dataType = "String", paramType = "path")})
    @GetMapping(value = "/my/pay-channel-params/apps/{appId}/platform-channels/{platformChannel}/pay-channels/{payChannel}")
    public PayChannelParamDTO queryPayChannelParam(@PathVariable String appId,
                                                   @PathVariable String platformChannel,
                                                   @PathVariable String payChannel) {
        return payChannelService.queryParamByAppPlatformAndPayChannel(appId, platformChannel, payChannel);
    }

}