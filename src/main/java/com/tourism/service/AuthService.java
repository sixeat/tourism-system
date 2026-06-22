package com.tourism.service;

import com.tourism.dto.LoginRequest;
import com.tourism.dto.ProfileUpdateRequest;
import com.tourism.dto.RegisterRequest;
import com.tourism.vo.LoginUserVO;

/**
 * 用户认证服务接口（AuthService Interface），定义了用户认证相关的核心业务操作。
 * 
 * <p><strong>什么是面向接口设计（Interface-Oriented Design）？</strong></p>
 * <ul>
 *   <li>面向接口设计是 Java 企业级开发中的最佳实践，核心思想是“面向接口编程，而非面向实现编程”。</li>
 *   <li>接口层（Service Interface）负责定义业务能力的契约（方法签名、参数、返回值），
 *       而具体的业务逻辑实现由实现类（如 {@link com.tourism.service.impl.AuthServiceImpl}）完成。</li>
 * </ul>
 * 
 * <p><strong>为什么接口与实现要分离？</strong></p>
 * <ul>
 *   <li><strong>解耦：</strong>Controller 层只依赖 AuthService 接口，不依赖具体的实现类，
 *       便于后续替换实现（如更换认证框架、引入缓存、接入第三方登录等）。</li>
 *   <li><strong>便于测试：</strong>单元测试时可以使用 Mockito 等工具直接 mock 接口，无需启动完整的 Spring 容器。</li>
 *   <li><strong>便于 AOP 代理：</strong>Spring 的事务管理（@Transactional）、性能监控、日志记录等横切关注点
 *       通常基于接口生成 JDK 动态代理，接口分离使 AOP 更自然。</li>
 *   <li><strong>团队协作：</strong>多人开发时，可以先约定接口定义，前后端并行开发，实现类后续补齐。</li>
 *   <li><strong>可读性：</strong>接口文件只展示“能做什么”，隐藏“怎么做”的细节，使系统架构更清晰。</li>
 * </ul>
 * 
 * <p>本接口涵盖了登录、注册、查询个人资料、更新资料四个核心认证功能，
 * 所有方法均返回 {@link LoginUserVO}，确保返回给前端的数据经过脱敏和裁剪。</p>
 */
public interface AuthService {

    /**
     * 用户登录。
     * 根据前端提交的 {@link LoginRequest} 验证用户名和密码，返回登录成功的用户信息。
     *
     * @param request 登录请求 DTO，包含 username 和 password
     * @return 登录成功后的用户视图对象 {@link LoginUserVO}，不包含密码等敏感信息
     * @throws IllegalArgumentException 当用户名或密码为空、用户不存在、密码不匹配时抛出
     * @throws IllegalStateException    当用户状态为禁用（status = 0）时抛出
     */
    LoginUserVO login(LoginRequest request);

    /**
     * 用户注册。
     * 根据前端提交的 {@link RegisterRequest} 创建新用户账号，并返回注册成功的用户信息。
     *
     * @param request 注册请求 DTO，包含 username、password、phone、email
     * @return 注册成功后的用户视图对象 {@link LoginUserVO}
     * @throws IllegalArgumentException 当参数校验不通过（如用户名已存在、长度不符合要求）时抛出
     */
    LoginUserVO register(RegisterRequest request);

    /**
     * 查询用户个人资料。
     * 根据用户 ID 查询数据库，返回当前登录用户的详细资料。
     *
     * @param userId 用户唯一标识符
     * @return 用户视图对象 {@link LoginUserVO}
     * @throws IllegalArgumentException 当用户不存在时抛出
     */
    LoginUserVO profile(Long userId);

    /**
     * 更新用户个人资料。
     * 根据用户 ID 和前端提交的 {@link ProfileUpdateRequest} 更新用户资料（手机号、邮箱、密码）。
     *
     * @param userId  用户唯一标识符，用于定位需要更新的用户
     * @param request 资料更新请求 DTO，包含 phone、email、oldPassword、newPassword
     * @return 更新成功后的用户视图对象 {@link LoginUserVO}
     * @throws IllegalArgumentException 当用户不存在、请求为空、旧密码不正确、新密码格式不符合时抛出
     */
    LoginUserVO updateProfile(Long userId, ProfileUpdateRequest request);
}
