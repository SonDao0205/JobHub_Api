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
@Slf4j // Sử dụng Logback có sẵn của Spring Boot để log chuẩn chỉ thay vì System.out.println
public class SystemLoggingAspect {

    // Định nghĩa định dạng ngày giờ hiển thị cho dễ nhìn
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 1. Định nghĩa điểm cắt (Pointcut) cho toàn bộ các hàm nằm trong package controller
     */
    @Pointcut("within(com.btvn.jobhub.controller..*)")
    public void controllerPointcut() {}

    /**
     * 2. Định nghĩa điểm cắt (Pointcut) cho toàn bộ các hàm nằm trong package service (bao gồm cả impl)
     */
    @Pointcut("within(com.btvn.jobhub.service..*)")
    public void servicePointcut() {}

    /**
     * 3. Lắng nghe hành động TRƯỚC (Before Advice) khi bất kỳ hàm nào thuộc Controller hoặc Service được kích hoạt
     */
    @Before("controllerPointcut() || servicePointcut()")
    public void logBeforeExecution(JoinPoint joinPoint) {

        // a. Lấy thời gian thực thi hiện tại
        String timestamp = LocalDateTime.now().format(formatter);

        // b. Lấy tên Phương thức (Hàm) và tên Lớp đang chạy
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName + "()";

        // c. Trích xuất Email của người dùng đang đăng nhập từ Spring Security Context
        String executorEmail = "ANONYMOUS (Khách vãng lai)";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal) {
                executorEmail = ((UserPrincipal) principal).getUsername(); // Trả về Email cấu hình trong UserPrincipal
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                executorEmail = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            } else {
                executorEmail = principal.toString();
            }
        }

        // d. In thông tin log ra màn hình Console một cách tường minh, đẹp đẽ
        log.info("[SYSTEM LOG] - Thời gian: {} | Người thực hiện: {} | Kích hoạt: {}",
                timestamp, executorEmail, fullMethodName);
    }
}