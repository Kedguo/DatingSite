package com.y.datingsite.exception;
import com.y.datingsite.common.BaseResponse;
import com.y.datingsite.common.ErrorCode;
import com.y.datingsite.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * @author : Yuan
 * @date :2024/5/7
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandle {
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandle(BusinessException e){
        log.error("businessException:"+e.getMessage(),e);
        return ResultUtils.error(e.getCode(),e.getMessage(),e.getDescription());
    }
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandle(BusinessException e){
        log.error("runtimeException",e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
    }

}
