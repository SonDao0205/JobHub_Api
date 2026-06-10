package com.btvn.jobhub.aspect;

import com.btvn.jobhub.security.principal.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
@Slf4j
public class SystemLoggingAspect {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Pointcut("within(com.btvn.jobhub.controller..*)")
    public void controllerPointcut() {}


    @Pointcut("within(com.btvn.jobhub.service..*)")
    public void servicePointcut() {}

    @Before("controllerPointcut() || servicePointcut()")
    public void logBeforeExecution(JoinPoint joinPoint) {

        String timestamp = LocalDateTime.now().format(formatter);

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName + "()";

        String executorEmail = "ANONYMOUS (Khách vãng lai)";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal) {
                executorEmail = ((UserPrincipal) principal).getUsername();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                executorEmail = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            } else {
                executorEmail = principal.toString();
            }
        }

        log.info("[SYSTEM LOG] - Thời gian: {} | Người thực hiện: {} | Kích hoạt: {}",
                timestamp, executorEmail, fullMethodName);
    }
}