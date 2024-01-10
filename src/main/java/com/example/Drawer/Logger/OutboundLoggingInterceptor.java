package com.example.Drawer.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class OutboundLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LogManager.getLogger(OutboundLoggingInterceptor.class);

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

        String requestURI = request.getHeader("X-Request-From");
        logger.info("Answered request to  {}", requestURI);


    }

}