package com.tourism.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AdminInterceptor 后台管理员权限拦截器。
 *
 * <p>设计目的：在 {@link AuthInterceptor} 校验登录状态之后，进一步校验当前用户是否具备管理员（ADMIN）角色，
 * 保护后台管理接口（如用户管理、数据统计、配置修改等）不被普通用户访问，实现基于角色的访问控制（RBAC）。
 *
 * <p>执行顺序说明：在 Spring MVC 配置中，通常将 AdminInterceptor 配置在 AuthInterceptor 之后，
 * 即先由 AuthInterceptor 确保用户已登录，再由 AdminInterceptor 确保用户是管理员。
 * 这样每个拦截器职责单一，符合单一职责原则（SRP）。
 *
 * <p>为什么实现 {@link HandlerInterceptor}：
 * 与 AuthInterceptor 相同，利用 Spring MVC 拦截器机制在 Controller 方法执行前进行权限校验，
 * 无需在每个后台接口方法中重复编写权限判断代码，实现横切关注点的集中管理。
 */
public class AdminInterceptor implements HandlerInterceptor {

    // ObjectMapper 实例，用于将 ApiResponse 序列化为 JSON 字符串后写入响应流。
    // 线程安全且本拦截器作为 Spring 单例 Bean 存在，直接 new 即可满足需求。
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 在目标 Controller 方法执行前校验当前用户是否为管理员角色。
     *
     * <p>执行时机：同 {@link AuthInterceptor}，在 Controller 方法调用之前，
     * 若前置拦截器（如 AuthInterceptor）已放行，则进入本方法的执行。
     *
     * <p>权限校验逻辑：从 Session 中读取 {@code SessionConstants.LOGIN_USER_ROLE} 属性，
     * 使用 {@code "ADMIN".equals(String.valueOf(role))} 进行判断。
     * 为什么用 String.valueOf(role) 而不是直接强转 String：
     * 1. role 可能为 null，强转会抛出 NullPointerException；
     * 2. role 存入 Session 时可能是枚举或其他类型，String.valueOf 可以安全地处理 null（返回 "null" 字符串），
     *    虽然 "null".equals("ADMIN") 仍为 false，但不会抛异常，增强健壮性。
     *
     * <p>为什么手动写入 JSON 响应：
     * 同 AuthInterceptor，拦截器返回 false 后请求处理链中断，
     * Spring 的全局异常处理器（GlobalExceptionHandler）和 Controller 都不会被执行，
     * 因此必须在本方法中直接向 response 写入标准 JSON 错误结构，确保前端收到统一格式的失败响应。
     *
     * @param request  当前 HTTP 请求对象，用于获取 Session 中的角色信息
     * @param response 当前 HTTP 响应对象，用于设置编码、内容类型并写入 JSON 响应体
     * @param handler  即将执行的目标处理器，通常是 Controller 方法，可用于更细粒度的拦截决策
     * @return true 表示用户具备 ADMIN 权限，放行请求；false 表示权限不足，中断请求
     * @throws IOException 当向响应流写入 JSON 发生 IO 异常时抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 从会话中读取当前登录用户的角色信息，登录成功时由登录接口存入 Session
        Object role = request.getSession().getAttribute(SessionConstants.LOGIN_USER_ROLE);

        // 判断角色是否为 ADMIN；使用 String.valueOf 避免 null 或类型不一致导致的异常
        if ("ADMIN".equals(String.valueOf(role))) {
            // 管理员权限校验通过，放行请求到后续拦截器或 Controller
            return true;
        }

        // 权限不足，设置响应字符编码为 UTF-8，防止中文乱码
        response.setCharacterEncoding("UTF-8");
        // 设置响应内容类型为 JSON，并声明 charset=UTF-8，让前端正确识别编码
        response.setContentType("application/json;charset=UTF-8");
        // 将失败响应序列化为 JSON 并写入响应体，提示前端当前用户无后台管理权限
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail("没有后台管理权限")));
        // 返回 false 中断请求，后续 Controller 和拦截器不再执行
        return false;
    }
}
