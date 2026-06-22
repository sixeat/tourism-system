package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import com.tourism.dto.LoginRequest;
import com.tourism.dto.ProfileUpdateRequest;
import com.tourism.dto.RegisterRequest;
import com.tourism.service.AuthService;
import com.tourism.vo.LoginUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 前端用户认证控制器（AuthController），负责处理与用户登录、注册、登出、资料查询与更新相关的 HTTP 请求。
 * 
 * <p>本类位于 {@code controller.front} 包下，表示这是<strong>前端（用户端）</strong>接口，
 * 面向普通用户（而非管理员）提供认证相关的 RESTful API。</p>
 * 
 * <p><strong>@RestController 注解说明：</strong></p>
 * <ul>
 *   <li>{@link org.springframework.web.bind.annotation.RestController} 是 Spring MVC 的组合注解，
 *       等同于 {@code @Controller + @ResponseBody}。</li>
 *   <li>表示该类中所有方法返回的对象（如 {@link ApiResponse}）都会直接序列化为 JSON 写入 HTTP 响应体，
 *       而不是解析为视图名称（如 JSP 页面）。</li>
 *   <li>这是构建 RESTful API 的标准方式，前后端分离项目中广泛使用。</li>
 * </ul>
 * 
 * <p><strong>@RequestMapping("/api/auth") 注解说明：</strong></p>
 * <ul>
 *   <li>为该类下的所有接口方法统一设置请求路径前缀 {@code /api/auth}。</li>
 *   <li>例如：{@code login()} 方法的完整路径为 {@code POST /api/auth/login}。</li>
 *   <li>使用 {@code /api} 前缀是为了与静态资源、页面路由区分开，明确表示这是 API 接口。</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * 认证服务接口，通过 Spring 自动注入实现类（AuthServiceImpl）。
     * Controller 层只依赖 Service 接口，不依赖具体实现，符合面向接口编程原则。
     */
    @Autowired
    private AuthService authService;

    /**
     * 用户登录接口。
     * 
     * <p><strong>@PostMapping("/login") 说明：</strong></p>
     * <ul>
     *   <li>表示该方法处理 HTTP POST 请求，路径为 {@code /api/auth/login}。</li>
     *   <li>使用 POST 而非 GET 是因为登录涉及提交敏感信息（密码），
     *       POST 请求体不会暴露在浏览器地址栏和服务器日志中，安全性更高。</li>
     * </ul>
     * 
     * <p><strong>@RequestBody 说明：</strong></p>
     * <ul>
     *   <li>{@link org.springframework.web.bind.annotation.RequestBody} 表示将 HTTP 请求体中的 JSON 数据
     *       自动反序列化为 Java 对象（此处为 {@link LoginRequest}）。</li>
     *   <li>Spring 默认使用 Jackson 库完成 JSON → Java 对象的转换。</li>
     *   <li>前端提交的数据格式应为：{@code {"username":"xxx","password":"xxx"}}。</li>
     * </ul>
     * 
     * <p><strong>HttpSession 使用说明：</strong></p>
     * <ul>
     *   <li>{@link javax.servlet.http.HttpSession} 是 Servlet 规范提供的会话对象，
     *       用于在多次 HTTP 请求之间保持用户状态。</li>
     *   <li>登录成功后，将用户关键信息（userId、username、role）写入 session 属性，
     *       后续请求中服务器可通过 session 识别当前登录用户，无需每次传递用户名密码。</li>
     *   <li>session 数据存储在服务器端（默认内存中），浏览器通过 Cookie 中的 JSESSIONID 与服务器关联。</li>
     * </ul>
     * 
     * <p><strong>session 属性写入说明：</strong></p>
     * <ul>
     *   <li>{@code SessionConstants.LOGIN_USER_ID}：存储用户唯一标识，用于后续“我的订单”等接口识别用户身份。</li>
     *   <li>{@code SessionConstants.LOGIN_USERNAME}：存储用户名，用于前端展示（如页面右上角欢迎语）。</li>
     *   <li>{@code SessionConstants.LOGIN_USER_ROLE}：存储用户角色，用于前端权限控制（如是否显示管理入口）。</li>
     * </ul>
     *
     * @param request 登录请求 DTO，由 @RequestBody 自动从 JSON 反序列化而来
     * @param session HTTP 会话对象，由 Spring 自动注入（每个浏览器对应一个 session）
     * @return 封装了登录成功信息的统一响应对象 {@code ApiResponse<LoginUserVO>}
     */
    @PostMapping("/login")
    public ApiResponse<LoginUserVO> login(@RequestBody LoginRequest request, HttpSession session) {
        // 调用 Service 层完成登录验证，成功后返回用户信息（VO 中已剔除密码）
        LoginUserVO user = authService.login(request);

        // 将登录用户的关键信息写入 session，后续请求通过 session 即可识别用户身份
        session.setAttribute(SessionConstants.LOGIN_USER_ID, user.getId());
        session.setAttribute(SessionConstants.LOGIN_USERNAME, user.getUsername());
        session.setAttribute(SessionConstants.LOGIN_USER_ROLE, user.getRole());

        // 返回统一格式的成功响应，前端据此更新登录状态并展示用户信息
        return ApiResponse.success("login success", user);
    }

    /**
     * 用户注册接口。
     * 
     * <p>处理 HTTP POST 请求，路径为 {@code /api/auth/register}。</p>
     * 
     * <p>注册成功后，与登录接口一样将用户信息写入 session，实现“注册即登录”的流畅体验，
     * 前端无需在注册成功后再次调用登录接口。</p>
     *
     * @param request 注册请求 DTO，包含 username、password、phone、email
     * @param session HTTP 会话对象，用于注册成功后自动登录
     * @return 封装了注册成功信息的统一响应对象 {@code ApiResponse<LoginUserVO>}
     */
    @PostMapping("/register")
    public ApiResponse<LoginUserVO> register(@RequestBody RegisterRequest request, HttpSession session) {
        // 调用 Service 层完成注册，返回新创建的用户信息
        LoginUserVO user = authService.register(request);

        // 注册成功后自动写入 session，实现注册即登录
        session.setAttribute(SessionConstants.LOGIN_USER_ID, user.getId());
        session.setAttribute(SessionConstants.LOGIN_USERNAME, user.getUsername());
        session.setAttribute(SessionConstants.LOGIN_USER_ROLE, user.getRole());

        return ApiResponse.success("注册成功", user);
    }

    /**
     * 用户登出（注销）接口。
     * 
     * <p>处理 HTTP POST 请求，路径为 {@code /api/auth/logout}。</p>
     * 
     * <p><strong>session.invalidate() 说明：</strong></p>
     * <ul>
     *   <li>{@link javax.servlet.http.HttpSession#invalidate()} 方法会立即销毁当前 session，
     *       清除服务器端存储的所有 session 属性（包括 LOGIN_USER_ID、LOGIN_USERNAME 等）。</li>
     *   <li>销毁后，浏览器持有的 JSESSIONID Cookie 将失效，后续请求会创建新的 session。</li>
     *   <li>这是实现“安全退出”的标准做法，确保退出后服务器不再保留用户登录状态。</li>
     * </ul>
     *
     * @param session HTTP 会话对象，调用 invalidate() 销毁
     * @return 封装了登出成功信息的统一响应对象 {@code ApiResponse<String>}
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpSession session) {
        // 销毁当前 session，清除所有登录状态信息
        session.invalidate();
        return ApiResponse.success("logout success", "OK");
    }

    /**
     * 获取当前登录用户信息接口。
     * 
     * <p>处理 HTTP GET 请求，路径为 {@code /api/auth/me}。</p>
     * 
     * <p><strong>为什么 me() 方法直接检查 session 而不是使用拦截器（Interceptor）？</strong></p>
     * <ul>
     *   <li><strong>职责分离：</strong>拦截器通常用于全局性的登录校验（如拦截需要登录才能访问的接口），
     *       而 {@code /api/auth/me} 本身就是用于查询登录状态的接口，需要兼容“未登录”的情况。</li>
     *   <li><strong>未登录场景：</strong>如果用户未登录，本接口应返回“未登录”提示，而不是直接拒绝访问或跳转到登录页。
     *       拦截器在此场景下会过于粗暴，无法提供友好的未登录状态提示。</li>
     *   <li><strong>灵活性：</strong>在方法内直接检查 session，可以根据业务需求返回不同的响应（如未登录时返回 fail 响应，
     *       已登录时返回 success 响应），而拦截器通常只能返回统一的 401 状态码。</li>
     *   <li><strong>简洁性：</strong>对于少数特殊接口（如获取当前状态），在方法内直接检查比配置拦截器规则更直观、更易维护。</li>
     * </ul>
     *
     * @param session HTTP 会话对象，从中读取 LOGIN_USER_ID 判断登录状态
     * @return 如果已登录，返回封装了用户信息的响应；如果未登录，返回失败响应
     */
    @GetMapping("/me")
    public ApiResponse<LoginUserVO> me(HttpSession session) {
        // 从 session 中获取当前登录用户的 ID
        Object userId = session.getAttribute(SessionConstants.LOGIN_USER_ID);

        // 如果 userId 为 null，说明用户未登录或 session 已过期
        if (userId == null) {
            return ApiResponse.fail("not logged in");
        }

        // 已登录，调用 Service 层查询用户完整资料并返回
        // 使用 Long.valueOf(String.valueOf(userId)) 进行安全类型转换：Object → String → Long
        return ApiResponse.success(authService.profile(Long.valueOf(String.valueOf(userId))));
    }

    /**
     * 更新当前登录用户个人资料接口。
     * 
     * <p>处理 HTTP POST 请求，路径为 {@code /api/auth/profile}。</p>
     * 
     * <p>本接口需要用户已登录，因此先从 session 中获取 userId；
     * 如果未登录则返回失败响应。更新成功后同步更新 session 中的 username 和 role，
     * 确保前端展示的信息与最新资料一致。</p>
     *
     * @param request 资料更新请求 DTO，包含 phone、email、oldPassword、newPassword
     * @param session HTTP 会话对象，用于获取当前用户 ID 并更新 session 属性
     * @return 封装了更新后用户信息的统一响应对象 {@code ApiResponse<LoginUserVO>}
     */
    @PostMapping("/profile")
    public ApiResponse<LoginUserVO> updateProfile(@RequestBody ProfileUpdateRequest request, HttpSession session) {
        // 从 session 中获取当前登录用户的 ID
        Object userId = session.getAttribute(SessionConstants.LOGIN_USER_ID);

        // 校验登录状态：如果 userId 为 null，说明未登录，不允许修改资料
        if (userId == null) {
            return ApiResponse.fail("not logged in");
        }

        // 调用 Service 层执行资料更新，传入 userId 和请求参数
        LoginUserVO user = authService.updateProfile(Long.valueOf(String.valueOf(userId)), request);

        // 资料更新后，同步更新 session 中的用户名和角色（尽管角色通常不变，但保持同步是好习惯）
        session.setAttribute(SessionConstants.LOGIN_USERNAME, user.getUsername());
        session.setAttribute(SessionConstants.LOGIN_USER_ROLE, user.getRole());

        return ApiResponse.success("资料已保存", user);
    }
}
