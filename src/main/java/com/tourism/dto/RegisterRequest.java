package com.tourism.dto;

/**
 * 用户注册请求数据传输对象（RegisterRequest DTO）。
 * 
 * <p><strong>DTO 模式说明：</strong></p>
 * <ul>
 *   <li>DTO（Data Transfer Object）是专用于客户端与服务端之间数据传输的对象，
 *       目的是将前端请求参数与数据库实体（Entity）解耦。</li>
 *   <li>注册场景下，前端需要提交用户名、密码、手机号、邮箱等信息，
 *       这些信息与数据库 {@link com.tourism.entity.User} 实体字段部分重合，
 *       但 DTO 只包含注册流程所需的字段，剔除了如 id、createTime 等不应由前端控制的字段。</li>
 *   <li>分离 DTO 与 Entity 可以避免“质量赋值（Mass Assignment）”漏洞，
 *       即防止前端恶意提交本不应修改的字段（如 role、status）。</li>
 * </ul>
 * 
 * <p>本类封装了用户注册时需要提交的所有信息，由 {@link org.springframework.web.bind.annotation.RequestBody}
 * 自动将 HTTP 请求体中的 JSON 数据绑定为此对象。</p>
 */
public class RegisterRequest {

    /**
     * 注册用户名，必填。
     * 将作为用户登录系统的唯一标识，系统会校验其唯一性（不能与其他用户重复）。
     */
    private String username;

    /**
     * 注册密码，必填。
     * 用户登录时使用的凭证，建议业务层在存入数据库前进行加密（如 MD5、BCrypt）。
     */
    private String password;

    /**
     * 手机号码，选填。
     * 用于后续联系用户或找回密码，允许为空（null）。
     */
    private String phone;

    /**
     * 电子邮箱，选填。
     * 用于后续联系用户或找回密码，允许为空（null）。
     */
    private String email;

    /**
     * 获取注册用户名。
     *
     * @return 用户名字符串
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置注册用户名。
     *
     * @param username 用户名字符串
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取注册密码。
     *
     * @return 密码字符串
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置注册密码。
     *
     * @param password 密码字符串
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取手机号码。
     *
     * @return 手机号码字符串，可能为 null
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号码。
     *
     * @param phone 手机号码字符串，允许为 null
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取电子邮箱。
     *
     * @return 电子邮箱字符串，可能为 null
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置电子邮箱。
     *
     * @param email 电子邮箱字符串，允许为 null
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
