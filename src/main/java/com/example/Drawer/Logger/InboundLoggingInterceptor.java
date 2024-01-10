package com.example.Drawer.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;

public class InboundLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LogManager.getLogger(InboundLoggingInterceptor.class);


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("Received request from {} to {}",
                request.getHeader("X-Request-From"),
                request.getRequestURI());

        return true;
    }
}