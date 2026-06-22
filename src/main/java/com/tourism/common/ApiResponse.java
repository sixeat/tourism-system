package com.tourism.common;

/**
 * ApiResponse 统一 API 响应封装类。
 *
 * <p>设计目的：前后端分离架构下，后端需要以统一的数据结构返回结果，
 * 使前端能够标准化地解析响应码、提示信息和业务数据，降低接口对接成本。
 *
 * <p>泛型设计 rationale：使用 {@code <T>} 作为泛型参数，
 * 允许 data 字段承载任意类型的业务数据（如 User、List<Order>、Map 等），
 * 避免为每种返回类型单独定义响应类，提升代码复用率和类型安全性。
 *
 * <p>使用静态工厂方法（success/fail）而非直接 new 的原因：
 * 1. 调用方代码更简洁，语义更清晰；
 * 2. 便于统一默认状态码（200 表示成功，500 表示失败）；
 * 3. 后续若需扩展（如添加 traceId、timestamp），只需修改工厂方法即可全局生效。
 */
public class ApiResponse<T> {

    // HTTP 风格的状态码，200 表示成功，500 表示业务失败，便于前端做条件判断
    private Integer code;

    // 返回给前端阅读的提示信息，成功时可为 "success" 或自定义文案，失败时携带错误原因
    private String message;

    // 泛型业务数据载体，可为任意类型；成功时填充具体数据，失败时通常为 null
    private T data;

    /**
     * 无参构造器。
     *
     * <p>为什么保留：Spring MVC 在反序列化 JSON 为对象时，
     * 以及某些 ORM 或反射框架（如 MyBatis、Jackson）实例化对象时，
     * 需要无参构造器作为入口。虽然本类主要用作响应，但保留无参构造器是 JavaBean 规范的好习惯。
     */
    public ApiResponse() {
    }

    /**
     * 全参构造器，用于一次性构造完整的响应对象。
     *
     * @param code    HTTP 风格状态码，标识操作结果类别
     * @param message 人类可读的结果描述，用于前端展示或日志记录
     * @param data    实际业务数据，使用泛型 T 保证类型安全
     */
    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应的快捷工厂方法，携带业务数据。
     *
     * <p>为什么使用静态泛型方法 {@code <T>}：
     * 允许编译器根据传入的 data 参数自动推断泛型类型，
     * 调用方无需显式书写 {@code ApiResponse.<User>success(user)}。
     *
     * @param data 成功的业务数据，可为实体、集合、DTO 等任意类型
     * @param <T>  泛型参数，保证返回的 ApiResponse 与 data 类型一致
     * @return 封装好的成功响应对象，code 固定为 200，message 为 "success"
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /**
     * 成功响应的快捷工厂方法，支持自定义提示信息并携带业务数据。
     *
     * @param message 自定义成功提示，例如 "创建成功"、"更新完成"，比默认 "success" 更友好
     * @param data    成功的业务数据
     * @param <T>     泛型参数，确保类型推断正确
     * @return 封装好的成功响应对象，code 为 200
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 失败响应的快捷工厂方法，用于业务异常或校验不通过的场景。
     *
     * @param message 失败原因描述，例如 "用户名已存在"、"库存不足"，直接展示给前端用户
     * @param <T>     泛型参数，由于失败时无业务数据，实际类型由调用上下文推断
     * @return 封装好的失败响应对象，code 固定为 500，data 为 null
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(500, message, null);
    }

    /**
     * 提供外部读取 code 的入口，遵循 JavaBean 规范，
     * 便于 Spring 框架反射调用（如 Jackson 序列化、EL 表达式）。
     *
     * @return 当前响应的状态码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 提供外部设置 code 的入口，遵循 JavaBean 规范，
     * 在手动构造响应或反序列化时需要用到。
     *
     * @param code 要设置的状态码
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 提供外部读取 message 的入口，遵循 JavaBean 规范。
     *
     * @return 当前响应的提示信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 提供外部设置 message 的入口，遵循 JavaBean 规范。
     *
     * @param message 要设置的提示信息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 提供外部读取 data 的入口，遵循 JavaBean 规范，
     * 前端框架或模板引擎通过 getter 访问嵌套业务数据。
     *
     * @return 当前响应携带的业务数据，可能为 null
     */
    public T getData() {
        return data;
    }

    /**
     * 提供外部设置 data 的入口，遵循 JavaBean 规范。
     *
     * @param data 要设置的业务数据
     */
    public void setData(T data) {
        this.data = data;
    }
}
