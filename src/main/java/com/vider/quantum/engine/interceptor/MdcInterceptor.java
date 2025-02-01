package com.vider.quantum.engine.interceptor;

import com.vider.quantum.engine.util.RequestCorrelation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

public class MdcInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String correlationId = getCorrelationId();
        MDC.put("CorrelationId", correlationId);
        RequestCorrelation.setId(correlationId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove("CorrelationId");
    }

    private String getCorrelationId() {
        return UUID.randomUUID().toString();
    }
}