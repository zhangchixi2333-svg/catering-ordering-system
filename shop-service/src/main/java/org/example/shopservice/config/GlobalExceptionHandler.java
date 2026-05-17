package org.example.shopservice.config;

import lombok.extern.slf4j.Slf4j;
import org.example.shopservice.common.Result;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        
        log.warn("参数校验失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        
        log.warn("参数绑定失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("约束违反");
        
        log.warn("约束违反: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理唯一约束冲突
     */
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(org.springframework.dao.DuplicateKeyException e) {
        String message = "数据已存在，请勿重复提交";
        
        // 尝试从异常消息中提取更详细的信息
        if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
            String duplicateInfo = e.getMessage().substring(
                    e.getMessage().indexOf("Duplicate entry"),
                    e.getMessage().indexOf("for key")
            );
            message = "数据冲突: " + duplicateInfo;
        }
        
        log.warn("数据重复: {}", e.getMessage());
        return Result.error(409, message);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("业务参数错误: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "系统内部错误: " + e.getMessage());
    }
}
