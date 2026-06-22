package com.tourism.dto;

/**
 * 用户登录请求数据传输对象（LoginRequest DTO）。
 * 
 * <p><strong>什么是 DTO（Data Transfer Object）模式？</strong></p>
 * <ul>
 *   <li>DTO 是用于层与层之间传输数据的对象，专门负责封装一次请求所需的数据。</li>
 *   <li>DTO 的核心目的是<strong>解耦</strong>：将前端传入的参数与数据库实体（Entity）分离，避免直接暴露或操作实体类。</li>
 *   <li>前端只需关注需要提交的字段，而不必了解数据库表的完整结构。</li>
 * </ul>
 * 
 * <p><strong>为什么 DTO 与 Entity 分离？</strong></p>
 * <ul>
 *   <li><strong>安全性：</strong>Entity 包含数据库全量字段（如密码、状态、创建时间），直接接收前端数据可能导致恶意字段被注入修改。</li>
 *   <li><strong>灵活性：</strong>登录只需要 username 和 password，不需要 phone、email 等无关字段，DTO 可以精简数据结构。</li>
 *   <li><strong>可维护性：</strong>当接口需求变化时（如增加验证码字段），只需修改 DTO，不影响 Entity 和数据库结构。</li>
 * </ul>
 * 
 * <p>本类用于封装前端登录请求提交的 JSON 数据，包含用户名和密码两个必要字段。
 * 由 {@link org.springframework.web.bind.annotation.RequestBody} 自动将 HTTP 请求体反序列化为该对象。</p>
 */
public class LoginRequest {

    /**
     * 登录用户名，前端提交的身份标识。
     * 必填字段，用于在数据库中定位对应用户。
     */
    private String username;

    /**
     * 登录密码，前端提交的凭证。
     * 必填字段，系统将用此密码与数据库中存储的密码进行比对验证。
     */
    private String password;

    /**
     * 获取登录用户名。
     *
     * @return 用户名字符串
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置登录用户名。
     *
     * @param username 用户名字符串
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取登录密码。
     *
     * @return 密码字符串
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置登录密码。
     *
     * @param password 密码字符串
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
