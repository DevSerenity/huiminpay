package com.huiminpay.common.cache.domain;

import lombok.Data;

/**
 * @ClassName BusinessException
 * @Author: DevSerenity
 * @CreateDate: 2023/11/19 14:13
 * @UpdateDate: 2023/11/19 14:13
 * @Version: 1.0
 */

//自定义异常类
@Data
public class BusinessException extends RuntimeException{

    private ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode){
        this.errorCode = errorCode;
    }
    private BusinessException(){

    }
}
