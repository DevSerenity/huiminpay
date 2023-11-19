package com.huiminpay.common.cache.domain;

import io.swagger.annotations.Api;
import lombok.Data;

/**
 * @ClassName RestErrorResponse
 * @Author: DevSerenity
 * @CreateDate: 2023/11/19 14:30
 * @UpdateDate: 2023/11/19 14:30
 * @Version: 1.0
 */
@Api(value = "RestErrorResponse",description = "错误响应参数包装")
@Data
public class RestErrorResponse {
    private String errCode;
    private String errMessage;

    public RestErrorResponse(String errCode, String errMessage) {
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    public RestErrorResponse() {
    }


}
