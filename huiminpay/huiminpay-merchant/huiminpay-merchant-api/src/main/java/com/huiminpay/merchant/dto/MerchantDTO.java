package com.huiminpay.merchant.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName MerchantDTO
 * @Author: DevSerenity
 * @CreateDate: 2023/11/15 14:59
 * @UpdateDate: 2023/11/15 14:59
 * @Version: 1.0
 */

@ApiModel("商户基本信息")
@Data
public class MerchantDTO implements Serializable {
    /**
     * 主键
     */
//    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 商户名称
     */
    @ApiModelProperty(name = "merchantName",value = "商户名称",example = "黄焖鸡")
    private String merchantName;

    /**
     * 企业编号
     */
    @ApiModelProperty(name = "merchantNo",value = "企业编号",example = "10000")
    private String merchantNo;

    /**
     * 企业地址
     */
    private String merchantAddress;

    /**
     * 商户类型
     */
    private String merchantType;

    /**
     * 营业执照（企业证明）
     */
    private String businessLicensesImg;

    /**
     * 法人身份证正面照片
     */
    private String idCardFrontImg;

    /**
     * 法人身份证反面照片
     */
    private String idCardAfterImg;

    /**
     * 联系人姓名
     */
    private String username;

    /**
     * 联系人手机号(关联统一账号)
     */
    private String mobile;

    /**
     * 联系人地址
     */
    private String contactsAddress;

    /**
     * 审核状态 0-未申请,1-已申请待审核,2-审核通过,3-审核拒绝
     */
    private String auditStatus;

    /**
     * 租户ID,关联统一用户
     */
    private Long tenantId;

    private String password;
}
