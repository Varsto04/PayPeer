package com.paypeer.p2p_transfer_service.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceLoggingAspect.class);

    @Around("execution(* com.paypeer.p2p_transfer_service.service.AccountService.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        String methodName = joinPoint.getSignature().getName();
        logger.info("Starting execution of {}", methodName);

        try {
            Object result = joinPoint.proceed();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            logger.info("Finished execution of {}. Duration: {} ms", methodName, duration);
            return result;
        } catch (Throwable t) {
            logger.error("Error in {}: {}", methodName, t.getMessage());
            throw t;
        }
    }
}