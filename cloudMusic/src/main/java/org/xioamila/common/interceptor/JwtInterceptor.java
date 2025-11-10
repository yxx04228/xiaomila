package org.xioamila.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.xioamila.common.context.UserContext;
import org.xioamila.common.utils.SecurityUtil;
import org.xioamila.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 从请求头获取token
        String token = request.getHeader("X-Access-Token");

        // 如果header中没有，尝试从URL参数中获取
        if (token == null || token.trim().isEmpty()) {
            token = request.getParameter("token");
        }

        // 检查是否缺少token
        if (token == null || token.trim().isEmpty()) {
            sendErrorResponse(response, 401, "缺少访问令牌");
            return false;
        }

        try {
            // 验证token是否有效
            SecurityUtil.validateToken(token);
            // 解析token并创建User对象
            User user = SecurityUtil.createUserFromToken(token);
            // 将用户信息存入上下文
            UserContext.setUser(user);
            return true;

        } catch (Exception e) {
            // Token验证失败
            String errorMessage = "Token验证失败";
            if (e.getMessage().contains("Token已过期")) {
                errorMessage = "Token已过期";
            } else if (e.getMessage().contains("Token格式错误")) {
                errorMessage = "Token格式错误";
            } else if (e.getMessage().contains("Token签名验证失败")) {
                errorMessage = "Token签名验证失败";
            }

            sendErrorResponse(response, 401, errorMessage);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        // 请求完成后清除用户信息，防止内存泄漏
        UserContext.clear();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}