package com.yuexiang.framework.web.handler;

import com.yuexiang.common.enums.ResultCodeEnum;
import com.yuexiang.common.exception.*;
import com.yuexiang.common.pojo.CommonResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadRequestException.class)
    public CommonResult<?> handleBadRequestException(BadRequestException e) {
        log.warn("请求参数错误: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public CommonResult<?> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("未授权异常: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public CommonResult<?> handleInvalidTokenException(InvalidTokenException e) {
        log.warn("Token无效: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public CommonResult<?> handleTokenExpiredException(TokenExpiredException e) {
        log.warn("Token已过期: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public CommonResult<?> handleForbiddenException(ForbiddenException e) {
        log.warn("无权限: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    public CommonResult<?> handleAccountLockedException(AccountLockedException e) {
        log.warn("账户被锁定: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public CommonResult<?> handleNotFoundException(NotFoundException e) {
        log.warn("资源不存在: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ResourceConflictException.class)
    public CommonResult<?> handleResourceConflictException(ResourceConflictException e) {
        log.warn("资源冲突: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RequestTimeoutException.class)
    public CommonResult<?> handleRequestTimeoutException(RequestTimeoutException e) {
        log.warn("请求超时: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(CaptchaErrorException.class)
    public CommonResult<?> handleCaptchaErrorException(CaptchaErrorException e) {
        log.warn("验证码错误: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public CommonResult<?> handleDuplicateRequestException(DuplicateRequestException e) {
        log.warn("重复请求: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public CommonResult<?> handleInternalServerErrorException(InternalServerErrorException e) {
        log.error("服务器内部错误: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public CommonResult<?> handleServiceUnavailableException(ServiceUnavailableException e) {
        log.warn("服务不可用: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public CommonResult<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return CommonResult.error(ResultCodeEnum.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult<?> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return CommonResult.error(ResultCodeEnum.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CommonResult<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMethod());
        return CommonResult.error(ResultCodeEnum.BAD_REQUEST.getCode(),
                "不支持'" + e.getMethod() + "'请求");
    }

    @ExceptionHandler(Exception.class)
    public CommonResult<?> handleException(Exception e) {
        log.error("系统异常", e);
        return CommonResult.error(ResultCodeEnum.INTERNAL_ERROR);
    }
}
