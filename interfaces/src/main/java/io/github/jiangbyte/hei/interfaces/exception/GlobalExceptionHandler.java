package io.github.jiangbyte.hei.interfaces.exception;

import io.github.jiangbyte.hei.api.response.R;
import io.github.jiangbyte.hei.domain.core.DomainException;
import io.github.jiangbyte.hei.types.enums.ResponseCode;
import io.github.jiangbyte.hei.types.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：将领域/业务/校验异常映射为统一响应 R。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务可预期异常。
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleBizException(BizException ex) {
        return R.fail(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理领域异常。
     */
    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleDomainException(DomainException ex) {
        return R.fail(ResponseCode.DOMAIN_ERROR, ex.getMessage());
    }

    /**
     * 处理参数校验异常。
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleValidationException(Exception ex) {
        String message = "参数校验失败";
        if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().getFieldError() != null) {
            message = manv.getBindingResult().getFieldError().getDefaultMessage();
        } else if (ex instanceof BindException be && be.getBindingResult().getFieldError() != null) {
            message = be.getBindingResult().getFieldError().getDefaultMessage();
        }
        return R.fail(ResponseCode.VALIDATION_ERROR, message);
    }

    /**
     * 处理未捕获异常。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return R.fail(ResponseCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
    }
}
