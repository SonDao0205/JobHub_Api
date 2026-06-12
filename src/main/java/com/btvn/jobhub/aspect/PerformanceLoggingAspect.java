package com.btvn.jobhub.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceLoggingAspect {

    @Pointcut("within(com.btvn.jobhub.controller..*)")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object logExecutionTimeAndUser(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "ANONYMOUS_USER";

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("[SYSTEM LOG] Người dùng [{}] đang gọi chức năng: {}.{}()",
                currentPrincipalName, className, methodName);

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("[SYSTEM LOG] Chức năng {}.{}() bị lỗi: {}", className, methodName, throwable.getMessage());
            throw throwable;
        }

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("[PERFORMANCE LOG] Chức năng {}.{}() xử lý thành công bởi [{}] trong: {} ms",
                className, methodName, currentPrincipalName, executionTime);

        return result;
    }
}