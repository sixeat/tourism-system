package com.tourism.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Object role = request.getSession().getAttribute(SessionConstants.LOGIN_USER_ROLE);
        if ("ADMIN".equals(String.valueOf(role))) {
            return true;
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail("没有后台管理权限")));
        return false;
    }
}
