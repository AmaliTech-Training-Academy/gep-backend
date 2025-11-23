package com.example.auth_service.aspect;

import com.example.auth_service.dto.request.AuditLogRequest;
import com.example.auth_service.dto.request.UserLoginRequest;
import com.example.auth_service.enums.AuditStatus;
import com.example.auth_service.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @Pointcut("execution(* com.example.auth_service.controller.AuthController.login(..)) && args(userLoginRequest,..)")
    private void LoginPointcut(UserLoginRequest userLoginRequest) {}

    @AfterReturning(value = "LoginPointcut(userLoginRequest)", argNames = "userLoginRequest")
    public void LoginAudit(UserLoginRequest userLoginRequest) {
        String ipAddress = getIpAddress();
        Instant timestamp = Instant.now();
        AuditLogRequest auditLogRequest = new AuditLogRequest(
                userLoginRequest.email(),
                ipAddress,
                timestamp,
                AuditStatus.SUCCESS
        );
        auditService.save(auditLogRequest);
    }

    @AfterThrowing(pointcut = "LoginPointcut(userLoginRequest)", throwing = "ex", argNames = "userLoginRequest,ex")
    public void loginAuditFailure(UserLoginRequest userLoginRequest, Exception ex) {
        String ipAddress = getIpAddress();
        Instant timestamp = Instant.now();
        AuditLogRequest auditLogRequest = new AuditLogRequest(
                userLoginRequest.email(),
                ipAddress,
                timestamp,
                AuditStatus.FAILED
        );
        auditService.save(auditLogRequest);
    }

    private String getIpAddress(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }



}
