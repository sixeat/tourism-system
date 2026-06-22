package com.tourism.entity;

import java.time.LocalDateTime;

/**
 * 用户实体类（User Entity），对应数据库中的 sys_user 表。
 * 
 * <p>该类是系统的核心实体之一，用于封装用户的基础信息，包括身份凭证、联系方式、角色状态等。
 * 在分层架构中，Entity 层负责与数据库表结构一一映射，是数据持久化的最基本单位。</p>
 * 
 * <p><strong>为什么使用 LocalDateTime？</strong></p>
 * <ul>
 *   <li>Java 8 引入的 {@link java.time.LocalDateTime} 是现代化的日期时间 API，比旧的 {@link java.util.Date} 更线程安全、语义清晰。</li>
 *   <li>LocalDateTime 不含时区信息，适合存储数据库中的“本地时间”字段（如 create_time）。</li>
 *   <li>MyBatis 与 Spring 对 LocalDateTime 有良好的原生支持，无需额外类型转换器即可直接映射数据库的 DATETIME/TIMESTAMP 类型。</li>
 * </ul>
 * 
 * <p><strong>关于 sys_user 表的映射说明：</strong></p>
 * <ul>
 *   <li>数据库表名为 sys_user（sys_ 前缀表示系统管理相关表）。</li>
 *   <li>类中的字段名采用驼峰命名（如 createTime），与数据库的下划线命名（create_time）通过 MyBatis 的 resultMap 自动映射。</li>
 *   <li>password 字段在此处存在是因为实体类需要完整映射数据库表列；但在返回给前端的 {@link com.tourism.vo.LoginUserVO} 中，password 会被故意剔除，防止敏感信息泄露。</li>
 * </ul>
 */
public class User {

    /**
     * 用户唯一标识符（主键），对应数据库 sys_user 表的 id 字段。
     * 使用 Long 类型以支持大数据量场景，由数据库自增生成。
     */
    private Long id;

    /**
     * 用户名，登录凭证之一，对应 sys_user 表的 username 字段。
     * 在系统中具有唯一性约束，不允许重复。
     */
    private String username;

    /**
     * 登录密码，对应 sys_user 表的 password 字段。
     * <p><strong>安全说明：</strong>该字段在实体类中存在是为了完成数据库的完整映射，
     * 但在向客户端（前端/浏览器）返回数据时，必须在 VO（View Object）中剔除，
     * 绝对禁止将密码以明文或密文形式返回给前端，防止敏感信息泄露。</p>
     */
    private String password;

    /**
     * 手机号码，用于联系用户或找回密码，对应 sys_user 表的 phone 字段。
     * 允许为空（null），由用户自行填写。
     */
    private String phone;

    /**
     * 电子邮箱，用于联系用户或找回密码，对应 sys_user 表的 email 字段。
     * 允许为空（null），由用户自行填写。
     */
    private String email;

    /**
     * 用户角色，对应 sys_user 表的 role 字段。
     * 常见取值如 "USER"（普通用户）、"ADMIN"（管理员），用于权限控制。
     */
    private String role;

    /**
     * 用户状态，对应 sys_user 表的 status 字段。
     * <p>状态约定：1 表示启用（正常），0 表示禁用（冻结）。
     * 状态为 0 的用户将无法登录系统。</p>
     */
    private Integer status;

    /**
     * 用户账号创建时间，对应 sys_user 表的 create_time 字段。
     * 使用 LocalDateTime 存储日期时间，映射数据库的 DATETIME 类型。
     * 该字段在插入时由数据库自动填充（或通过框架自动设置）。
     */
    private LocalDateTime createTime;

    /**
     * 获取用户唯一标识符。
     *
     * @return 用户 id，可能为 null（如未持久化的新对象）
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置用户唯一标识符。
     *
     * @param id 用户 id，通常由数据库生成，业务层一般不需要手动设置
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
     * @param username 用户名，应保证唯一性且非空
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取用户密码。
     *
     * @return 密码字符串
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置用户密码。
     *
     * @param password 密码字符串，建议业务层在设置前进行加密处理
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取用户手机号码。
     *
     * @return 手机号码，可能为 null
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置用户手机号码。
     *
     * @param phone 手机号码，允许为 null
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取用户电子邮箱。
     *
     * @return 电子邮箱地址，可能为 null
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置用户电子邮箱。
     *
     * @param email 电子邮箱地址，允许为 null
     */
    public void setEmail(String email) {
        this.email = email;
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
     * 获取用户状态。
     *
     * @return 状态码：1 表示启用，0 表示禁用
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置用户状态。
     *
     * @param status 状态码：1 表示启用，0 表示禁用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取用户账号创建时间。
     *
     * @return 创建时间的 LocalDateTime 对象，可能为 null
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置用户账号创建时间。
     *
     * @param createTime 创建时间，通常为 LocalDateTime 类型
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
