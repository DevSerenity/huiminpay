package com.huiminpay.merchant.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.common.cache.domain.CommonErrorCode;
import com.huiminpay.common.cache.util.PhoneUtil;
import com.huiminpay.merchant.api.MerchantService;
import com.huiminpay.merchant.common.util.SecurityUtil;
import com.huiminpay.merchant.convert.MerchantDetailConvert;
import com.huiminpay.merchant.convert.MerchantRegisterConvert;
import com.huiminpay.merchant.dto.MerchantDTO;
import com.huiminpay.merchant.service.SmsService;
import com.huiminpay.merchant.vo.MerchantDetailVO;
import com.huiminpay.merchant.vo.MerchantRegisterVo;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @ClassName MerchantController
 * @Author: DevSerenity
 * @CreateDate: 2023/11/15 15:02
 * @UpdateDate: 2023/11/15 15:02
 * @Version: 1.0
 */
@RestController
@Slf4j
@Api(value = "商户信息", description = "商户信息模块",tags = "商户平台-商户信息")
public class MerchantController {

    @Reference
    MerchantService merchantService;

    @Autowired
    private SmsService smsService;


    @GetMapping("/merchant/{id}")
    @ApiOperation(value = "按照id查询商户")
    @ApiImplicitParam(paramType = "path", dataType = "Long", name = "id", value = "按照id查询", required = true)
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

        // 1.校验
        if (merchantRegister == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //手机号非空校验
        if (StringUtils.isBlank(merchantRegister.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        //校验手机号的合法性
        if (!PhoneUtil.isMatches(merchantRegister.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        //联系人非空校验
        if (StringUtils.isBlank(merchantRegister.getUsername())) {
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        //密码非空校验
        if (StringUtils.isBlank(merchantRegister.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        //验证码非空校验
        if (StringUtils.isBlank(merchantRegister.getVerifiyCode()) ||
                StringUtils.isBlank(merchantRegister.getVerifiykey())) {
            throw new BusinessException(CommonErrorCode.E_100103);
        }
        smsService.checkVerifiyCode(merchantRegister.getVerifiykey(),
                merchantRegister.getVerifiyCode());
        //注册商户
        MerchantDTO merchantDTO = MerchantRegisterConvert.INSTANCE.vo2Dto(merchantRegister);
        merchantService.createMerchant(merchantDTO);
        return merchantRegister;
    }

    @Autowired
    OSS oss;

    @Value("${oss.aliyun.bucket}")
    private String bucket;
    @Value("${oss.aliyun.domain}")
    private String domain;

    //上传证件照
    @ApiOperation("上传证件照")
    @PostMapping("/upload")
    public String upload(@ApiParam(value = "证件照",required = true)
                         @RequestParam("file") MultipartFile multipartFile)
            throws BusinessException {
        String fileName = System.currentTimeMillis() + ""; //文件名，当前系统时间戳
        String originalFilename = multipartFile.getOriginalFilename(); // 上传文件原文件名
        String ext = originalFilename.substring(originalFilename.indexOf(".")); // 截取获取扩展名
        try {
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucket, fileName + ext, multipartFile.getInputStream());
            oss.putObject(putObjectRequest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_100106);
        }
        return domain + fileName + ext;
    }

    @ApiOperation("资质申请")
    @PostMapping("my/merchants/save")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchantInfo", value = "商户认证资料", required = true, dataType = "MerchantDetailVO", paramType = "body")
    })
    public void saveMerchant(@RequestBody MerchantDetailVO merchantInfo){
        //解析token
        Long merchantId = SecurityUtil.getMerchantId();
        //vo->dto
        MerchantDTO merchantDTO = MerchantDetailConvert.INSTANCE.vo2dto(merchantInfo);
        merchantService.applyMerchant(merchantId,merchantDTO);
    }
}
