package com.huiminpay.merchant.intercept;

import com.huiminpay.common.cache.domain.BusinessException;
import com.huiminpay.common.cache.domain.CommonErrorCode;
import com.huiminpay.common.cache.domain.ErrorCode;
import com.huiminpay.common.cache.domain.RestErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName GlobalExceptionHandler
 * @Author: DevSerenity
 * @CreateDate: 2023/11/19 14:26
 * @UpdateDate: 2023/11/19 14:26
 * @Version: 1.0
 */

@ControllerAdvice  //全局异常配置
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse processExcetion (HttpServletRequest request,
                                              HttpServletResponse response,
                                              Exception e){
        //判断异常的类型 业务异常还是服务异常
        if(e instanceof BusinessException){
            //打印日志查看异常信息
            LOGGER.info(e.getMessage(),e);

            //解析异常
            BusinessException businessException = (BusinessException) e;
            ErrorCode errorCode = businessException.getErrorCode();

            //错误代码
            int code = errorCode.getCode();
            //错误信息
            String desc = errorCode.getDesc();

            return new RestErrorResponse(String.valueOf(code),desc);
        }else {
            LOGGER.error("系统异常",e);
            return new RestErrorResponse(String.valueOf(CommonErrorCode.UNKNOWN.getCode()),
                    CommonErrorCode.UNKNOWN.getDesc());
        }
    }
}
