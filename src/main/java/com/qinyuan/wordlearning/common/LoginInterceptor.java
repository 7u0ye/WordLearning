package com.qinyuan.wordlearning.common;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 如果不是 Controller 方法（比如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1、从请求头获取 token
        String token = request.getHeader("Authorization");

        // 2、没带 token 或格式不对 → 拒绝
        if (token == null || !token.startsWith("Bearer ")) {
            log.info("请求未携带 token");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"请先登录\",\"data\":null}");
            return false;
        }

        // 3、校验 token
        try {
            String jwt = token.replace("Bearer ", "");
            log.info("jwt 校验: {}", jwt);
            Claims claims = jwtUtil.parseJWT(jwt);
            Long userId = Long.valueOf(claims.getSubject());
            log.info("当前用户 id: {}", userId);
            // 存到 ThreadLocal，后面 Controller 和 Service 都能拿到
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception e) {
            log.error("token 校验失败: {}", e.getMessage());
            response.setContentType("application/json;charset=UTF-8");

            response.getWriter().write("{\"code\":0,\"msg\":\"token无效，请重新登录\",\"data\":null}");
            return false;
        }
    }

    @Override
    public void afterCompletion(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，避免线程复用时的数据污染
        BaseContext.removeCurrentId();
    }
}