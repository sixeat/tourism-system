package com.tourism.common;

/**
 * BusinessException 业务异常类。
 *
 * <p>设计目的：在分层架构中区分 "业务规则错误" 与 "系统技术错误"。
 * 例如：库存不足、重复提交、权限不足等属于业务异常，
 * 而数据库连接失败、空指针等属于系统异常。通过自定义业务异常，
 * 上层可以统一捕获并友好地返回给前端，避免堆栈信息暴露。
 *
 * <p>为什么继承 {@link RuntimeException} 而非 {@link Exception}：
 * 1. 业务异常通常不需要在方法签名中强制声明（checked exception 会污染方法签名）；
 * 2. Service 层可以随时随地抛出，无需每层都添加 throws 声明；
 * 3. Spring 的声明式事务默认只对 RuntimeException 回滚，便于保持数据一致性。
 */
public class BusinessException extends RuntimeException {

    /**
     * 构造业务异常实例。
     *
     * @param message 异常描述信息，通常为人类可读的业务错误提示，
     *                例如 "该景点已下架"、"订单状态不允许取消"，
     *                最终会被 {@link com.tourism.config.GlobalExceptionHandler} 捕获并包装为 ApiResponse 返回给前端
     */
    public BusinessException(String message) {
        super(message);
    }
}
