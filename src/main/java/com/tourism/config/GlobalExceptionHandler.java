package com.tourism.config;

import com.tourism.common.ApiResponse;
import com.tourism.common.BusinessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler 全局异常处理器。
 *
 * <p>设计目的：集中捕获并处理 Controller 层抛出的所有异常，
 * 将各类异常（业务异常、参数校验异常、系统异常）统一转换为前端可识别的 {@link ApiResponse} 结构，
 * 避免异常堆栈直接暴露给客户端，同时减少每个 Controller 中重复写 try-catch 的样板代码。
 *
 * <p>{@code @RestControllerAdvice} 的作用机制：
 * 该注解是 {@code @ControllerAdvice} 与 {@code @ResponseBody} 的组合体。
 * Spring 会在所有 Controller 方法执行期间拦截异常，
 * 被 {@code @ExceptionHandler} 标记的方法的返回值将直接作为 HTTP 响应体写入输出流（默认 JSON 格式）。
 * 因此无需在每个 Controller 中手动捕获异常，实现真正的 "横切关注点" 分离。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务自定义异常 {@link BusinessException}。
     *
     * <p>{@code @ExceptionHandler(BusinessException.class)} 告诉 Spring：
     * 当任何 Controller 抛出 BusinessException 或其子类时，由本方法接管处理。
     * 异常对象会作为参数传入，可提取其中的业务错误信息。
     *
     * @param ex 捕获到的 BusinessException 实例，包含业务错误描述
     * @return 封装后的失败响应，code 为 500，message 为异常中的业务提示
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<String> handleBusiness(BusinessException ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    /**
     * 处理非法参数异常 {@link IllegalArgumentException}。
     *
     * <p>场景示例：参数校验失败、枚举值不合法、日期格式错误等。
     * 单独捕获的原因：此类异常通常由用户输入引起，属于可预期的客户端错误，
     * 返回 500 风格的业务失败提示比返回原始 400 更契合本系统统一响应规范。
     *
     * @param ex 捕获到的 IllegalArgumentException 实例
     * @return 封装后的失败响应，message 包含具体的参数错误原因
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    /**
     * 处理非法状态异常 {@link IllegalStateException}。
     *
     * <p>场景示例：在不允许的状态下调用方法，如重复支付、已取消订单再次支付等。
     * 与 IllegalArgumentException 的区别：前者是参数错了，后者是状态不对。
     * 单独捕获可以为前端提供更精准的错误分类，未来也可扩展为不同错误码。
     *
     * @param ex 捕获到的 IllegalStateException 实例
     * @return 封装后的失败响应
     */
    @ExceptionHandler(IllegalStateException.class)
    public ApiResponse<String> handleIllegalState(IllegalStateException ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    /**
     * 兜底异常处理器，捕获所有未被上述方法匹配的异常。
     *
     * <p>为什么放在最后且参数为 {@code Exception.class}：
     * Spring 在选择异常处理方法时，会优先匹配最具体的异常类型，
     * 只有当 BusinessException、IllegalArgumentException、IllegalStateException 都不匹配时，
     * 才会落到本方法。这是防御性编程的最后一道防线，防止未知异常导致前端收到空白页或堆栈泄露。
     *
     * <p>对空消息的处理：某些异常（如 NullPointerException）的 getMessage() 可能为 null，
     * 此时返回固定的 "system error"，避免前端收到 null 消息导致展示异常。
     *
     * @param ex 捕获到的任意异常实例
     * @return 封装后的失败响应，若异常消息为空则使用默认系统错误提示
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception ex) {
        return ApiResponse.fail(ex.getMessage() == null ? "system error" : ex.getMessage());
    }
}
