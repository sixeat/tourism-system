package com.tourism.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AuthInterceptor 用户登录认证拦截器。
 *
 * <p>设计目的：在请求到达 Controller 之前，统一校验用户是否已登录，
 * 防止未认证用户访问受保护接口，实现横切关注点的集中管理（AOP 思想）。
 * 所有需要登录才能访问的接口路径，应在 Spring MVC 配置中注册本拦截器。
 *
 * <p>为什么实现 {@link HandlerInterceptor} 而非使用 AOP 注解：
 * 1. HandlerInterceptor 是 Spring MVC 原生支持的拦截机制，与请求映射体系天然集成；
 * 2. 可以方便地访问 HttpServletRequest/HttpServletResponse，进行重定向或写入 JSON；
 * 3. 拦截器配置在 Spring 配置中通过路径规则（addPathPatterns/excludePathPatterns）控制，粒度灵活。
 *
 * <p>生命周期说明：HandlerInterceptor 包含三个回调方法：
 * - preHandle：Controller 方法执行之前调用，返回 true 放行，false 中断请求；
 * - postHandle：Controller 方法执行之后、视图渲染之前调用；
 * - afterCompletion：视图渲染完成之后调用，用于资源清理。
 * 本拦截器只关心认证，因此只需重写 preHandle。
 */
public class AuthInterceptor implements HandlerInterceptor {

    // ObjectMapper 用于将 ApiResponse 对象序列化为 JSON 字符串。
    // 选择直接 new 而非注入 Spring Bean 的原因：拦截器实例化时机较早，
    // 通过构造器注入会增加配置复杂度；ObjectMapper 线程安全且本拦截器为单例，直接持有实例简单高效。
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 在目标 Controller 方法执行前进行登录状态校验。
     *
     * <p>执行时机：Spring DispatcherServlet 找到 Handler 后、实际调用 Controller 方法前。
     * 返回 true 表示继续后续处理链（放行到 Controller）；
     * 返回 false 表示中断请求，此时需要自行向 response 中写入响应内容，否则客户端会收到空白响应。
     *
     * <p>认证逻辑：从当前会话（HttpSession）中获取 {@code SessionConstants.LOGIN_USER_ID} 属性。
     * 若存在，说明用户已登录，直接放行；
     * 若不存在，说明用户未登录或会话已过期，需要拒绝访问并返回 JSON 错误提示。
     *
     * <p>为什么手动写入 JSON 而非抛异常：
     * 1. 拦截器处于 Controller 之前，{@link GlobalExceptionHandler} 无法捕获这里的异常；
     * 2. 返回 false 后请求处理链中断，Spring 不会继续调用 Controller 或全局异常处理器；
     * 3. 直接写入 JSON 是最可靠、最可控的响应方式，确保前端始终收到统一格式的错误信息。
     *
     * @param request  当前 HTTP 请求对象，可获取 Session、Header、Parameter 等
     * @param response 当前 HTTP 响应对象，用于设置状态码、编码、写入响应体
     * @param handler  即将执行的目标处理器（通常是 Controller 中的某个方法），可用于更细粒度的拦截判断
     * @return true 放行请求；false 拦截请求
     * @throws IOException 当向响应流写入 JSON 发生 IO 异常时抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 从会话中读取登录用户ID，如果用户此前成功登录，登录接口会将用户ID存入 Session
        Object userId = request.getSession().getAttribute(SessionConstants.LOGIN_USER_ID);
        if (userId != null) {
            // 用户已登录，继续后续处理链
            return true;
        }
        // 用户未登录，设置响应编码为 UTF-8，防止中文提示乱码
        response.setCharacterEncoding("UTF-8");
        // 设置内容类型为 JSON，并附带 charset=UTF-8，确保前端正确解析
        response.setContentType("application/json;charset=UTF-8");
        // 使用 ObjectMapper 将 ApiResponse 序列化为 JSON 字符串写入响应体
        // 这里返回 500 风格的失败响应，message 为 "not logged in"，提示前端跳转登录页
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail("not logged in")));
        // 返回 false 中断请求，不会执行 Controller 方法
        return false;
    }
}
