package com.tourism.common;

/**
 * SessionConstants 会话常量类。
 *
 * <p>设计目的：将用户会话中存储的属性键名（key）集中管理，避免在业务代码中硬编码字符串，
 * 防止因拼写错误导致 session 取值失败，同时提高代码可维护性和可读性。
 *
 * <p>为什么声明为 {@code final}：该类仅用于承载静态常量，不应被继承，
 * 使用 final 可以禁止子类化，避免子类通过继承破坏常量语义或引入非预期行为。
 *
 * <p>为什么构造器为 {@code private}：工具类/常量类不需要实例化，
 * 私有化构造器可以阻止外部通过 {@code new SessionConstants()} 创建实例，
 * 这是《Effective Java》中推荐的工具类设计模式。
 */
public final class SessionConstants {

    // 会话中存储当前登录用户ID的键名，类型通常为 Long/String
    public static final String LOGIN_USER_ID = "LOGIN_USER_ID";

    // 会话中存储当前登录用户名的键名，用于页面展示或日志记录
    public static final String LOGIN_USERNAME = "LOGIN_USERNAME";

    // 会话中存储当前登录用户角色的键名，用于权限校验（如 ADMIN/USER）
    public static final String LOGIN_USER_ROLE = "LOGIN_USER_ROLE";

    /**
     * 私有化无参构造器，阻止外部实例化。
     *
     * <p>由于该类只包含静态常量，实例化没有任何意义，
     * 私有化构造器是防止误用的防御性编程手段。
     */
    private SessionConstants() {
    }
}
