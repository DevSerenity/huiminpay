package com.huiminpay.merchant.controller;

import com.huiminpay.merchant.api.MerchantService;
import com.huiminpay.merchant.convert.MerchantRegisterConvert;
import com.huiminpay.merchant.dto.MerchantDTO;
import com.huiminpay.merchant.service.SmsService;
import com.huiminpay.merchant.vo.MerchantRegisterVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName MerchantController
 * @Author: DevSerenity
 * @CreateDate: 2023/11/15 15:02
 * @UpdateDate: 2023/11/15 15:02
 * @Version: 1.0
 */
@RestController
@Slf4j
@Api(value = "商户信息",hidden = true,description = "商户信息模块")
public class MerchantController {

    @Reference
    MerchantService merchantService;

    @Autowired
    private SmsService smsService;



    @GetMapping("/merchant/{id}")
    @ApiOperation(value = "按照id查询商户")
    @ApiImplicitParam(paramType = "path",dataType = "Long",name = "id",value = "按照id查询",required = true)
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

    @GetMapping("/sms")
    public String getSMSCode(@RequestParam String phone) {
        log.info("向手机号:{}发送验证码", phone);
        return smsService.sendSmsCode(phone);
    }

    @ApiOperation("注册商户")
    @ApiImplicitParam(name = "merchantRegister", value = "注册信息", required = true, dataType =
            "MerchantRegisterVo", paramType = "body")
    @PostMapping("/merchants/register")
    public MerchantRegisterVo registerMerchant(@RequestBody MerchantRegisterVo merchantRegister) {
        smsService.checkVerifiyCode(merchantRegister.getVerifiykey(),
                merchantRegister.getVerifiyCode());
        //注册商户
        MerchantDTO merchantDTO = MerchantRegisterConvert.INSTANCE.vo2Dto(merchantRegister);
        merchantService.createMerchant(merchantDTO);
        return merchantRegister;
    }
}
