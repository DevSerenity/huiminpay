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
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName PlatformParamController
 * @Description TODO
 * @Author 86150
 * @Date 2023/11/21 19:41
 * @Version 1.0
 */
@Slf4j
@RestController
@Api(value = "商户平台-渠道和支付参数相关",tags = "商户平台-渠道和支付参数",description = "商户平台-渠道和支付参数相关")
public class PlatformParamController {
    @Reference
    private PayChannelService payChannelService;

    /**
     * 查询所有平台服务类型
     *
     * @return 列表<平台通道dto>
     */
    @ApiOperation("查询所有平台服务类型")
    @GetMapping("my/platform-channels")
    public List<PlatformChannelDTO> queryPlatformChannel(){
        return payChannelService.queryPlatformChannel();
    }


    /**
     * 根据平台服务类型获取支付渠道列表
     *
     * @param platformChannelCode 平台信道码
     * @return 列表<支付通道dto>
     */
    @ApiOperation("根据平台服务类型获取支付渠道列表")
    @ApiImplicitParam(value = "服务类型编码",name = "platformChannelCode",dataType = "String",paramType = "path")
    @GetMapping("my/pay-channels/platform-channel/{platformChannelCode}")
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(@PathVariable String platformChannelCode){
        return payChannelService.queryPayChannelByPlatformChannel(platformChannelCode);
    }

    /**
     * 商户配置支付渠道参数
     *
     * @param payChannelParamDTO 付费通道参数
     */
    @ApiOperation("商户配置支付渠道参数")
    @ApiImplicitParam(value = "商户配置支付渠道参数",name = "payChannelParamDTO",dataType = "PayChannelParamDTO",
            required = true,paramType = "body")
    @RequestMapping(value = "my/pay-channel-params",method = {RequestMethod.PUT,RequestMethod.POST})
    public void createPayChannelParam(@RequestBody PayChannelParamDTO payChannelParamDTO){
        //获取商户id
        Long merchantId = SecurityUtil.getMerchantId();
        payChannelParamDTO.setMerchantId(merchantId);
        payChannelService.savePayChannelParam(payChannelParamDTO);
    }

    /**
     * 获取指定应用指定服务类型下所包含的原始支付渠道参数列表
     *
     * @param appId           应用id
     * @param platformChannel 服务类型
     * @return 列表<支付通道参数>
     */
    @ApiOperation("获取指定应用指定服务类型下所包含的原始支付渠道参数列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用id", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "platformChannel", value = "服务类型", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping("/my/pay-channel-params/apps/{appId}/platform-channels/{platformChannel}")
    public List<PayChannelParamDTO> queryPayChannelParam(@PathVariable String appId,
                                                         @PathVariable String platformChannel){
        List<PayChannelParamDTO> payChannelParamDTOS = payChannelService.queryPayChannelParamByAppAndPlatform(appId, platformChannel);
        return payChannelParamDTOS;
    }

    /**
     * 获取指定应用指定服务类型下所包含的某个原始支付参数
     *
     * @param appId           应用程序id
     * @param platformChannel 服务类型
     * @param payChannel      支付渠道
     * @return 付费通道参数
     */
    @ApiOperation("获取指定应用指定服务类型下所包含的某个原始支付参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用id", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "platformChannel", value = "平台支付渠道编码", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "payChannel", value = "实际支付渠道编码", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping("/my/pay-channel-params/apps/{appId}/platform-channels/{platformChannel}/pay-channels/{payChannel}")
    public PayChannelParamDTO queryPayChannelParam(@PathVariable String appId,
                                                   @PathVariable String platformChannel,
                                                   @PathVariable String payChannel){
        return payChannelService.queryParamByAppPlatformAndPayChannel(appId,platformChannel,payChannel);
    }

}
