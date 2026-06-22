package com.tourism.service.impl;

import com.tourism.dto.LoginRequest;
import com.tourism.dto.ProfileUpdateRequest;
import com.tourism.dto.RegisterRequest;
import com.tourism.entity.User;
import com.tourism.mapper.UserMapper;
import com.tourism.service.AuthService;
import com.tourism.vo.LoginUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户认证服务实现类（AuthServiceImpl），实现了 {@link AuthService} 接口定义的所有业务方法。
 * 
 * <p>该类是认证模块的核心业务逻辑层，负责处理用户登录、注册、资料查询与更新等功能。
 * 所有方法均遵循“先校验、再处理、后返回”的执行流程，确保数据的正确性和安全性。</p>
 * 
 * <p>类上标注了 {@link org.springframework.stereotype.Service} 注解，
 * 表示这是一个 Spring 管理的业务层 Bean，Spring 容器启动时会自动扫描并实例化该类。</p>
 */
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * 用户数据访问层 Mapper，通过 Spring 自动注入。
     * 用于执行数据库的增删改查操作，所有 SQL 定义在 UserMapper.xml 中。
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录业务逻辑。
     * 
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>校验请求参数：确保 request、username、password 均不为 null。</li>
     *   <li>根据 username 查询数据库，获取用户实体。</li>
     *   <li>校验用户存在性：如果查询结果为 null，说明用户名不存在。</li>
     *   <li>校验用户状态：如果 status == 0，说明账号被禁用，拒绝登录。</li>
     *   <li>校验密码：比对前端提交的密码与数据库存储的密码是否一致。</li>
     *   <li>所有校验通过后，调用 {@link #toLoginUserVO(User)} 将实体转换为 VO 并返回。</li>
     * </ol>
     * 
     * <p><strong>错误处理策略：为什么抛出异常而不是返回错误码？</strong></p>
     * <ul>
     *   <li>抛出异常（IllegalArgumentException / IllegalStateException）是 Java 的惯用做法，
     *       利用异常机制中断正常流程，使错误信息携带堆栈上下文，便于调试。</li>
     *   <li>Spring 的 {@code @RestControllerAdvice} 或全局异常处理器可以统一捕获这些运行时异常，
     *       将其转换为统一的 JSON 错误响应（如 {@code ApiResponse.fail("xxx")}），
     *       避免在业务方法中混杂返回码判断逻辑，保持代码简洁。</li>
     *   <li>返回错误码（如 int 返回值）容易导致调用方遗漏判断，且无法携带详细的错误描述。</li>
     * </ul>
     *
     * @param request 登录请求 DTO，包含 username 和 password
     * @return 登录成功后的用户视图对象 {@link LoginUserVO}
     * @throws IllegalArgumentException 当用户名或密码为空、用户不存在、密码不匹配时抛出
     * @throws IllegalStateException    当用户被禁用时抛出
     */
    @Override
    public LoginUserVO login(LoginRequest request) {
        // 第一步：参数非空校验，防止前端未传参数或传了 null 导致后续空指针异常
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Username or password is empty");
        }

        // 第二步：根据用户名从数据库查询用户实体
        User user = userMapper.selectByUsername(request.getUsername());

        // 第三步：校验用户是否存在
        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        // 第四步：校验用户状态是否被禁用（status == 0 表示禁用）
        // 先判断 getStatus() != null 是为了避免数据库中 status 字段为 null 时引发空指针异常
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalStateException("User is disabled");
        }

        // 第五步：校验密码是否匹配（注意：生产环境应对密码进行加密比对，如 BCrypt）
        if (!request.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Username or password is incorrect");
        }

        // 第六步：所有校验通过，转换为 VO 并返回（VO 中已剔除 password 字段）
        return toLoginUserVO(user);
    }

    /**
     * 用户注册业务逻辑。
     * 
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>参数校验：request 不能为空，用户名和密码不能为空（使用 isBlank 判断空白字符串）。</li>
     *   <li>去除首尾空格：对 username 和 password 调用 trim()，防止用户误输入空格。</li>
     *   <li>长度校验：用户名 3-20 位，密码 6-30 位，防止过短或过长影响安全与体验。</li>
     *   <li>唯一性校验：查询数据库确认用户名是否已存在。</li>
     *   <li>构建 User 实体：设置默认值（role="USER"，status=1），phone 和 email 使用 trimToNull 处理空值。</li>
     *   <li>插入数据库：调用 userMapper.insert，成功后 user 对象的 id 会被自动回填。</li>
     *   <li>转换为 VO 返回。</li>
     * </ol>
     *
     * @param request 注册请求 DTO，包含 username、password、phone、email
     * @return 注册成功后的用户视图对象 {@link LoginUserVO}
     * @throws IllegalArgumentException 当参数校验不通过时抛出
     */
    @Override
    public LoginUserVO register(RegisterRequest request) {
        // 第一步：参数非空校验，使用 isBlank 同时检查 null 和空白字符串（如 "   "）
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        // 第二步：去除用户名和密码的首尾空格，防止用户误输入空格导致后续问题
        String username = request.getUsername().trim();
        String password = request.getPassword().trim();

        // 第三步：用户名长度校验，3-20 个字符，确保既有辨识度又不会过长
        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("用户名长度应为 3-20 个字符");
        }

        // 第四步：密码长度校验，6-30 个字符，确保基本安全性
        if (password.length() < 6 || password.length() > 30) {
            throw new IllegalArgumentException("密码长度应为 6-30 个字符");
        }

        // 第五步：用户名唯一性校验，防止重复注册
        if (userMapper.selectByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在，请换一个用户名");
        }

        // 第六步：构建 User 实体对象，填充注册信息
        User user = new User();
        user.setUsername(username);          // 设置处理后的用户名（已 trim）
        user.setPassword(password);          // 设置密码（生产环境建议在此处加密）
        user.setPhone(trimToNull(request.getPhone()));   // 手机号去除空白后若为 "" 则设为 null
        user.setEmail(trimToNull(request.getEmail()));   // 邮箱去除空白后若为 "" 则设为 null
        user.setRole("USER");                // 默认角色为普通用户
        user.setStatus(1);                   // 默认状态为启用

        // 第七步：插入数据库，useGeneratedKeys 会自动将生成的主键回填到 user.getId()
        userMapper.insert(user);

        // 第八步：转换为 VO 返回给前端
        return toLoginUserVO(user);
    }

    /**
     * 查询用户个人资料。
     * 
     * <p>根据用户 ID 查询数据库，并转换为 VO 返回。
     * 常用于“我的资料”页面或登录后获取当前用户信息。</p>
     *
     * @param userId 用户唯一标识符
     * @return 用户视图对象 {@link LoginUserVO}
     * @throws IllegalArgumentException 当用户不存在时抛出
     */
    @Override
    public LoginUserVO profile(Long userId) {
        // 根据 userId 查询数据库
        User user = userMapper.selectById(userId);

        // 校验用户是否存在
        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        // 转换为 VO 返回
        return toLoginUserVO(user);
    }

    /**
     * 更新用户个人资料。
     * 
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>查询用户是否存在。</li>
     *   <li>校验 request 是否为空。</li>
     *   <li>更新基础资料：phone、email 使用 trimToNull 处理空值后保存。</li>
     *   <li>判断是否需要修改密码：如果 newPassword 不为空，则进入密码修改流程。</li>
     *   <li>密码修改流程：验证 oldPassword 是否正确，验证 newPassword 长度，然后更新密码。</li>
     *   <li>重新查询数据库并返回最新资料（确保前端拿到的是更新后的数据）。</li>
     * </ol>
     *
     * @param userId  用户唯一标识符
     * @param request 资料更新请求 DTO，包含 phone、email、oldPassword、newPassword
     * @return 更新成功后的用户视图对象 {@link LoginUserVO}
     * @throws IllegalArgumentException 当用户不存在、请求为空、旧密码不正确、新密码过短时抛出
     */
    @Override
    public LoginUserVO updateProfile(Long userId, ProfileUpdateRequest request) {
        // 第一步：查询并校验用户存在性
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        // 第二步：校验请求对象是否为空
        if (request == null) {
            throw new IllegalArgumentException("Profile request is empty");
        }

        // 第三步：更新基础资料（phone、email），使用 trimToNull 将空白字符串转为 null
        user.setPhone(trimToNull(request.getPhone()));
        user.setEmail(trimToNull(request.getEmail()));
        userMapper.updateProfile(user);

        // 第四步：判断是否需要修改密码（newPassword 非空表示用户想修改密码）
        if (!isBlank(request.getNewPassword())) {
            // 4.1 校验旧密码：必须提供旧密码，且旧密码必须与数据库中存储的密码一致
            if (isBlank(request.getOldPassword()) || !request.getOldPassword().equals(user.getPassword())) {
                throw new IllegalArgumentException("原密码不正确");
            }

            // 4.2 校验新密码长度，至少 6 位
            if (request.getNewPassword().trim().length() < 6) {
                throw new IllegalArgumentException("新密码至少 6 位");
            }

            // 4.3 设置新密码（去除首尾空格）并更新到数据库
            user.setPassword(request.getNewPassword().trim());
            userMapper.updatePassword(user);
        }

        // 第五步：重新查询数据库获取最新数据，确保返回给前端的是更新后的完整信息
        return toLoginUserVO(userMapper.selectById(userId));
    }

    /**
     * 判断字符串是否为空白（null 或仅包含空白字符）。
     * 
     * <p><strong>工具方法说明：</strong></p>
     * <ul>
     *   <li>该方法用于统一处理“空值”判断逻辑，避免在多处重复写 {@code value == null || value.trim().isEmpty()}。</li>
     *   <li>Java 标准库中的 {@link org.springframework.util.StringUtils#isEmpty} 或 Apache Commons 的 
     *       {@code StringUtils.isBlank} 也提供类似功能，但为了避免引入额外依赖，本项目使用私有工具方法。</li>
     *   <li>空白字符串（如 "   "、"\t"）在实际业务中通常等同于空值，因此需要 trim 后再判断。</li>
     * </ul>
     *
     * @param value 待判断的字符串
     * @return 如果字符串为 null 或 trim 后为空，返回 true；否则返回 false
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 去除字符串首尾空白，如果结果为空白则返回 null。
     * 
     * <p><strong>工具方法说明：</strong></p>
     * <ul>
     *   <li>该方法用于将前端可能传来的空白字符串（如 "  "）统一转换为 null，
     *       以便数据库存储时字段保持为 NULL，而不是存入无意义的空字符串。</li>
     *   <li>保持数据库字段为 null 有助于：减少存储空间、避免空字符串与 null 的歧义、
     *       使 SQL 中的 IS NULL 判断更可靠。</li>
     *   <li>如果传入 null，直接返回 null，避免 NullPointerException。</li>
     * </ul>
     *
     * @param value 原始字符串，可能为 null 或包含首尾空格
     * @return trim 后的字符串；如果 trim 后为空，则返回 null
     */
    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 将 User 实体对象转换为 LoginUserVO 视图对象。
     * 
     * <p><strong>转换方法说明：</strong></p>
     * <ul>
     *   <li>该方法负责完成 Entity → VO 的转换，是数据脱敏和裁剪的关键步骤。</li>
     *   <li>转换过程中<strong>故意排除了 password 字段</strong>，确保密码不会泄露到前端。</li>
     *   <li>如果后续需要向前端返回更多字段（如 createTime），只需在此方法中添加对应 set 调用即可。</li>
     *   <li>集中管理转换逻辑，避免在 login、register、profile 等多个方法中重复编写相同的拷贝代码。</li>
     * </ul>
     *
     * @param user 数据库查询得到的 User 实体对象，包含完整字段（包括密码）
     * @return 前端展示用的 LoginUserVO 对象，已剔除敏感字段
     */
    private LoginUserVO toLoginUserVO(User user) {
        LoginUserVO vo = new LoginUserVO();
        vo.setId(user.getId());              // 拷贝用户 ID
        vo.setUsername(user.getUsername());  // 拷贝用户名
        vo.setRole(user.getRole());          // 拷贝角色（用于前端权限判断）
        vo.setPhone(user.getPhone());        // 拷贝手机号
        vo.setEmail(user.getEmail());        // 拷贝邮箱
        // 注意：此处不拷贝 password 字段，确保返回给前端的数据安全
        return vo;
    }
}
