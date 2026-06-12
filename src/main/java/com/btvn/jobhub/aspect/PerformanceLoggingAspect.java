package com.btvn.jobhub.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceLoggingAspect {
    private final ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();

    @Pointcut("within(com.btvn.jobhub.controller..*)")
    public void controllerPointcut() {}

    @Before("controllerPointcut()")
    public void logBeforeExecution(JoinPoint joinPoint) {
        startTimeThreadLocal.set(System.currentTimeMillis());

        String currentPrincipalName = getCurrentUsername();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("[SYSTEM LOG] Người dùng [{}] đang gọi chức năng: {}.{}()",
                currentPrincipalName, className, methodName);
    }


    @AfterReturning("controllerPointcut()")
    public void logAfterSuccess(JoinPoint joinPoint) {
        long executionTime = calculateExecutionTime();
        String currentPrincipalName = getCurrentUsername();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("[PERFORMANCE LOG] Chức năng {}.{}() xử lý thành công bởi [{}] trong: {} ms",
                className, methodName, currentPrincipalName, executionTime);

        startTimeThreadLocal.remove();
    }

    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "exception")
    public void logAfterException(JoinPoint joinPoint, Throwable exception) {
        long executionTime = calculateExecutionTime();
        String currentPrincipalName = getCurrentUsername();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.error("[SYSTEM LOG] Chức năng {}.{}() bị lỗi sau {} ms. Người gọi: [{}]. Chi tiết lỗi: {}",
                className, methodName, executionTime, currentPrincipalName, exception.getMessage());

        startTimeThreadLocal.remove();
    }


    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "ANONYMOUS_USER";
    }

    private long calculateExecutionTime() {
        Long startTime = startTimeThreadLocal.get();
        if (startTime == null) {
            return 0L;
        }
        return System.currentTimeMillis() - startTime;
    }
}