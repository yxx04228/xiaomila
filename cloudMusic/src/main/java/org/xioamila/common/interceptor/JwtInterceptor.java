package org.xioamila.common.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.xioamila.common.context.UserContext;
import org.xioamila.common.utils.SecurityUtil;
import org.xioamila.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 从请求头获取token
        String token = request.getHeader("X-Access-Token");

        if (token != null) {
            // 验证token是否有效
            SecurityUtil.validateToken(token);
            // 解析token并创建User对象
            User user = SecurityUtil.createUserFromToken(token);
            // 将用户信息存入上下文
            UserContext.setUser(user);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        // 请求完成后清除用户信息，防止内存泄漏
        UserContext.clear();
    }
}