package com.vv.usercenter.exception;

import com.vv.usercenter.common.BaseResponse;
import com.vv.usercenter.common.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author vv先森
 * @create 2024-07-09 14:43
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse baseResponseHandle(BusinessException e) {
        log.error("BusinessException" + e.getMessage(), e);
        return ResultUtil.error(e.getCode(), e.getMessage(), e.getDescription());
    }
}
