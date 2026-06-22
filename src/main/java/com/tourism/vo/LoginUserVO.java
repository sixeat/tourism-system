package com.tourism.vo;

/**
 * 登录用户视图对象（LoginUserVO），用于封装返回给前端（浏览器/客户端）的用户信息。
 * 
 * <p><strong>什么是 VO（View Object）模式？</strong></p>
 * <ul>
 *   <li>VO（View Object）是专门为视图层（前端）设计的数据对象，只包含前端展示所需的字段。</li>
 *   <li>VO 的核心作用是<strong>数据裁剪与脱敏</strong>：从数据库实体（Entity）中提取必要字段，
 *       剔除敏感或不必要的字段，以 JSON 形式返回给前端。</li>
 *   <li>VO 与 Entity 的区别：Entity 对应数据库完整表结构，VO 对应前端展示需求，两者关注点不同。</li>
 * </ul>
 * 
 * <p><strong>为什么 LoginUserVO 中不包含 password 字段？</strong></p>
 * <ul>
 *   <li><strong>安全原因：</strong>密码（无论明文或密文）属于极高敏感信息，绝对不能返回给客户端，
 *       否则存在被网络截获、浏览器插件窃取、前端代码泄露等风险。</li>
 *   <li><strong>业务原因：</strong>前端登录成功后只需展示用户基本信息（id、用户名、角色、联系方式），
 *       根本不需要密码字段。</li>
 *   <li><strong>设计原则：</strong>遵循“最小权限原则”，只暴露必要数据，降低攻击面。</li>
 * </ul>
 * 
 * <p>本类通常由 {@link com.tourism.service.AuthService} 的登录、注册、查询等方法构建，
 * 经 {@link com.tourism.common.ApiResponse} 包装后以 JSON 格式返回给前端。</p>
 */
public class LoginUserVO {

    /**
     * 用户唯一标识符，前端可用此 id 请求用户专属接口（如“我的订单”、“我的资料”）。
     */
    private Long id;

    /**
     * 用户名，用于前端展示（如页面右上角显示“欢迎，xxx”）。
     */
    private String username;

    /**
     * 用户角色，前端可用此字段进行权限控制（如判断是否为管理员，决定是否显示管理后台入口）。
     * 常见取值："USER"（普通用户）、"ADMIN"（管理员）。
     */
    private String role;

    /**
     * 手机号码，用于前端展示在个人资料页面。
     */
    private String phone;

    /**
     * 电子邮箱，用于前端展示在个人资料页面。
     */
    private String email;

    /**
     * 获取用户唯一标识符。
     *
     * @return 用户 id
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置用户唯一标识符。
     *
     * @param id 用户 id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名字符串
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名。
     *
     * @param username 用户名字符串
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取用户角色。
     *
     * @return 角色字符串，如 "USER" 或 "ADMIN"
     */
    public String getRole() {
        return role;
    }

    /**
     * 设置用户角色。
     *
     * @param role 角色字符串，如 "USER" 或 "ADMIN"
     */
    public void setRole(String role) {
        this.role = role;
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
