package com.tourism.mapper;

import com.tourism.entity.User;

import java.util.List;

/**
 * 用户数据访问层 Mapper 接口（UserMapper），定义了对 sys_user 表的所有数据库操作。
 * 
 * <p><strong>MyBatis Mapper 接口模式说明：</strong></p>
 * <ul>
 *   <li>MyBatis 是 Java 生态中流行的持久层框架，采用“接口 + XML”的方式实现 SQL 与 Java 代码的分离。</li>
 *   <li>Mapper 接口中只定义方法签名，不写任何 SQL 语句；具体的 SQL 写在同名的 XML 映射文件中（如 UserMapper.xml）。</li>
 *   <li>MyBatis 在运行时会动态生成该接口的代理实现类，将接口方法调用转换为对应的 SQL 执行。</li>
 *   <li>这种方式的好处是：SQL 独立维护（便于 DBA 调优），Java 代码保持整洁（只关注业务逻辑）。</li>
 * </ul>
 * 
 * <p><strong>为什么本接口没有使用 @Mapper 注解？</strong></p>
 * <ul>
 *   <li>在 Spring 与 MyBatis 的整合中，Mapper 接口的扫描有两种常见方式：</li>
 *   <li>方式一：在接口上添加 {@code @Mapper} 注解，配合 {@code @MapperScan} 扫描。</li>
 *   <li>方式二：在 Spring 配置文件（如 spring-mybatis.xml）中配置 {@code <mybatis:scan>} 或
 *       {@code MapperScannerConfigurer}，指定扫描包路径（如 {@code com.tourism.mapper}），
 *       自动将包下的所有接口注册为 Spring Bean。</li>
 *   <li>本项目采用了方式二：在 spring-mybatis.xml 中配置了 Mapper 包扫描，
 *       因此接口上无需额外添加 {@code @Mapper} 注解，Spring 容器启动时会自动识别并代理这些接口。</li>
 * </ul>
 * 
 * <p>本接口中所有方法均对应 UserMapper.xml 中同 id 的 SQL 语句，
 * 方法名必须与 XML 中 {@code <select> / <insert> / <update>} 的 id 属性完全一致。</p>
 */
public interface UserMapper {

    /**
     * 根据用户名查询单个用户。
     * 对应 XML 中的 {@code <select id="selectByUsername">}。
     *
     * @param username 用户名，非空
     * @return 查询到的 User 实体；如果用户名不存在，返回 null
     */
    User selectByUsername(String username);

    /**
     * 根据用户 ID 查询单个用户。
     * 对应 XML 中的 {@code <select id="selectById">}。
     *
     * @param id 用户唯一标识符，非空
     * @return 查询到的 User 实体；如果 ID 不存在，返回 null
     */
    User selectById(Long id);

    /**
     * 查询所有用户列表。
     * 对应 XML 中的 {@code <select id="selectAll">}。
     * 通常用于后台管理系统的用户列表展示。
     *
     * @return 所有用户的列表；如果表为空，返回空列表（非 null）
     */
    List<User> selectAll();

    /**
     * 插入一条新用户记录。
     * 对应 XML 中的 {@code <insert id="insert">}。
     * 使用了 {@code useGeneratedKeys="true"}，插入成功后 user 对象的 id 字段会被自动赋值为数据库生成的主键。
     *
     * @param user 待插入的用户实体，包含 username、password、phone、email、role、status 等字段
     * @return 受影响的行数，正常插入成功时返回 1
     */
    int insert(User user);

    /**
     * 更新用户的基础资料（手机号、邮箱）。
     * 对应 XML 中的 {@code <update id="updateProfile">}。
     * 不修改密码、角色、状态等字段。
     *
     * @param user 包含 id、phone、email 字段的用户实体
     * @return 受影响的行数，更新成功时返回 1
     */
    int updateProfile(User user);

    /**
     * 更新用户密码。
     * 对应 XML 中的 {@code <update id="updatePassword">}。
     * 单独提供此方法是为了在修改密码时与其他资料更新分离，增强安全性。
     *
     * @param user 包含 id 和 password（新密码）字段的用户实体
     * @return 受影响的行数，更新成功时返回 1
     */
    int updatePassword(User user);

    /**
     * 管理员更新用户全部信息（包括角色、状态）。
     * 对应 XML 中的 {@code <update id="updateByAdmin">}。
     * 权限较高，一般由后台管理接口调用，普通用户不可使用。
     *
     * @param user 包含 id、phone、email、role、status 字段的用户实体
     * @return 受影响的行数，更新成功时返回 1
     */
    int updateByAdmin(User user);

    /**
     * 更新用户状态（启用/禁用）。
     * 对应 XML 中的 {@code <update id="updateStatus">}。
     * 常用于管理员冻结或解冻用户账号。
     *
     * @param user 包含 id 和 status 字段的用户实体
     * @return 受影响的行数，更新成功时返回 1
     */
    int updateStatus(User user);
}
