package org.xioamila.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.xioamila.common.jwt.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);

                // 将用户信息存入请求上下文，方便后续使用
                request.setAttribute("username", username);
                request.setAttribute("userId", userId);
                return true;
            }
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write("{\"success\":false,\"message\":\"Token无效或已过期\"}");
        return false;
    }
}