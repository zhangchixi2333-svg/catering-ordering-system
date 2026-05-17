package org.example.menuservice.config;

import lombok.extern.slf4j.Slf4j;
import org.example.menuservice.common.Result;
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
            String errorMsg = e.getMessage();
            // 提取重复的值
            int startIdx = errorMsg.indexOf("Duplicate entry '");
            int endIdx = errorMsg.indexOf("' for key");
            
            if (startIdx != -1 && endIdx != -1) {
                String duplicateValue = errorMsg.substring(startIdx + 18, endIdx);
                
                // 根据不同的唯一键给出友好的提示
                if (errorMsg.contains("uk_shop_category")) {
                    message = "该店铺下已存在相同的分类名称，请使用其他名称";
                } else {
                    message = "数据冲突: " + duplicateValue + " 已存在";
                }
            }
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
