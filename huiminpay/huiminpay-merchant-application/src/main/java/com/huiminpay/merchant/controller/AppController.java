package com.huiminpay.merchant.controller;

import com.huiminpay.merchant.api.AppSerivice;
import com.huiminpay.merchant.common.util.SecurityUtil;
import com.huiminpay.merchant.dto.AppDTO;
import com.huiminpay.transaction.api.PayChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName AppController
 * @Author: DevSerenity
 * @CreateDate: 2023/11/21 14:33
 * @UpdateDate: 2023/11/21 14:33
 * @Version: 1.0
 */



@Api(value = "商户平台-应用管理",tags = "商户平台-应用相关",description = "商户平台-应用相关")
@RestController
public class AppController {

    @Reference
    private AppSerivice appSerivice;

    @Reference
    private PayChannelService payChannelService;

    @ApiOperation("商户创建应用")
    @ApiImplicitParams({@ApiImplicitParam(name = "app", value = "应用信息", required = true, dataType = "AppDTO", paramType = "body")})
    @PostMapping("/my/apps")
    public AppDTO createApp(@RequestBody AppDTO app){

        //需要获取merchantId
        Long merchantId = SecurityUtil.getMerchantId();
        //将查询到的结果返回
        return appSerivice.createApp(merchantId,app);
    }


    /**
     * 查询我的应用
     *
     * @return 列表<app dto>
     */
    @ApiOperation("查询商户下的应用列表信息")
    @GetMapping("/my/apps")
    public List<AppDTO> queryMyApps(){
        Long merchantId = SecurityUtil.getMerchantId();
        return appSerivice.queryAppByMerchant(merchantId);
    }

    @ApiOperation("根据appid获取应用的详细信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "appId", value = "商户应用id", required = true, dataType = "String", paramType = "path")})
    @GetMapping(value = "/my/apps/{appId}")
    public AppDTO getApp(@PathVariable String appId) {
        return appSerivice.getAppById(appId);
    }



    /**
     * 绑定应用和平台支付类型
     *
     * @param appId                应用程序id
     * @param platformChannelCodes 平台信道码
     */
    @ApiOperation("绑定服务类型")
    @PostMapping("my/apps/{appId}/platform-channels")
    public void bindPlatformForApp(@PathVariable("appId")String appId,
                                   @RequestParam("platformChannelCodes") String platformChannelCodes){
        //调用方法进行应用和支付平台类型的绑定
        payChannelService.bindPlatformChannelForApp(appId,platformChannelCodes);
    }

    @ApiOperation("查询应用是否绑定了某个服务类型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用appId", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "platformChannel", value = "服务类型", required = true, dataType = "String", paramType = "query")
    })
    @GetMapping("my/merchants/apps/platformchannels")
    public int queryAppBindPlatformChannel(@RequestParam String appId,@RequestParam String platformChannel){
        return payChannelService.queryAppBindPlatformChannel(appId,platformChannel);
    }
}
