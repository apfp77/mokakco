package com.mokakco.platform.configure;

import com.mokakco.platform.notification.ErrorNotificationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ErrorNotificationAspect {

    private final ErrorNotificationService errorNotificationService;
    private final Logger logger = LoggerFactory.getLogger("errorLogger");

    public ErrorNotificationAspect(@Lazy ErrorNotificationService errorNotificationService) {
        this.errorNotificationService = errorNotificationService;
    }

    /**
     * 에러 발생 시 로그 기록 및 디스코드로 알림
     */
    @Around("execution(* com.mokakco.platform..*(..))")
    public Object handleErrors(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed(); // 원래 메서드 실행
        } catch (Exception e) {
            logger.error(e.getMessage());
            errorNotificationService.sendErrorMessage(e.getMessage());
            return e; // 예외가 발생하면 null 반환 (필요 시 변경 가능)
        }
    }
}
