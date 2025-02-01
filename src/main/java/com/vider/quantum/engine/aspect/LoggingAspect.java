package com.vider.quantum.engine.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Before("execution(* com.vider.quantum.engine..*(..))")
    public void logMethodEntry(JoinPoint joinPoint) {
        log.info("Entering method: " + joinPoint.getSignature().toShortString() + " Arguments: " + getArguments(joinPoint));
    }

    private Map<String, String> getArguments(JoinPoint joinPoint) {
        Map<String, String> argumentsMap = new HashMap<>();
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();
        if (parameterNames != null && parameterValues != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterValues[i] != null) {
                    String result = parameterValues[i].toString();
                    if (result.length() > 100) {
                        result = result.substring(0, 100);
                    }
                    argumentsMap.put(parameterNames[i],
                            parameterValues[i] != null ? result : "");
                }
            }

        }
        return argumentsMap;
    }

    @AfterReturning(pointcut = "execution(* com.vider.quantum.engine..*(..))", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        int endIndex = 100;
        if (result != null) {
            if (result.toString().length() <= 100) {
                endIndex = result.toString().length();
            }
            result = result.toString().substring(0, endIndex);
        }
        log.info("Exiting method: " + joinPoint.getSignature().toShortString() + " Execution Result: " + result);
    }

    @AfterThrowing(pointcut = "execution(* com.vider.quantum.engine..*(..))", throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Exception exception) {
        log.error("Exception in: " + joinPoint.getSignature().toShortString(), exception);
    }
}
