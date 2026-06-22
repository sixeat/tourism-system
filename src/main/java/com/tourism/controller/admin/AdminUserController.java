package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.entity.User;
import com.tourism.service.AdminResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员后台——用户管理控制器（Admin User Controller）
 *
 * <p>本控制器提供用户资源的查询、修改及状态启用/禁用功能，仅管理员可访问。</p>
 *
 * <p>权限说明：</p>
 * <ul>
 *   <li>所有接口挂载在 <code>/api/admin/user</code> 下，受 <code>/api/admin/**</code> 拦截器保护。</li>
 *   <li>管理员通过本接口查看全站注册用户、修改用户资料，以及控制用户账号的启用/禁用状态。</li>
 *   <li>注意：本控制器不提供删除用户接口，通常出于数据完整性考虑，采用软删除（禁用）而非物理删除。</li>
 * </ul>
 *
 * @author Tourism System
 */
@RestController
// @RestController 声明为 REST 控制器，所有方法直接返回 JSON 响应体，不经过视图渲染。
@RequestMapping("/api/admin/user")
// 类级 URL 前缀：/api/admin/user。/api 表示 REST 接口；/admin 表示管理员后台，受拦截器保护；/user 表示用户资源。
public class AdminUserController {

    @Autowired
    // @Autowired 自动注入 AdminResourceService，该服务层负责用户、景点、酒店、门票等资源的通用管理。
    private AdminResourceService adminResourceService;

    /**
     * 查询所有用户列表。
     *
     * <p>接口地址：<code>GET /api/admin/user/list</code></p>
     *
     * <p>HTTP 语义：</p>
     * <ul>
     *   <li>GET 请求用于安全、幂等地获取数据，不修改服务器状态。</li>
     * </ul>
     *
     * @return ApiResponse&lt;List&lt;User&gt;&gt; 统一响应封装，data 为用户实体列表。
     *         User 为用户数据库实体类，对应用户表结构，通常包含用户名、手机号、邮箱、角色、状态等字段。
     */
    @GetMapping("/list")
    // @GetMapping 映射 GET 请求到 /api/admin/user/list，用于查询全部用户数据。
    public ApiResponse<List<User>> list() {
        // 调用服务层获取所有用户记录，并包装成统一成功响应返回给前端。
        return ApiResponse.success(adminResourceService.listUsers());
    }

    /**
     * 修改用户信息。
     *
     * <p>接口地址：<code>PUT /api/admin/user/update</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestBody</strong>：将 HTTP 请求体（JSON 格式）自动反序列化为 User 实体对象。
     *       前端需发送 Content-Type: application/json 的请求，Spring 的 HttpMessageConverter（默认 Jackson）负责解析。
     *       请求体中必须包含 id 字段，用于定位待更新的用户记录。</li>
     *   <li><strong>@PutMapping</strong>：映射 PUT 请求，语义为“完整更新资源”，具有幂等性。</li>
     * </ul>
     *
     * @param user 从请求体 JSON 解析得到的用户实体对象，需携带 id 及待更新的字段（如昵称、手机号等）。
     * @return ApiResponse&lt;User&gt; 统一响应封装，data 为更新后的用户对象。
     */
    @PutMapping("/update")
    // @PutMapping 映射 PUT 请求到 /api/admin/user/update，用于完整更新用户资料。
    public ApiResponse<User> update(@RequestBody User user) {
        // @RequestBody 将前端传来的 JSON 自动转换为 User 实体，交由服务层执行更新逻辑。
        return ApiResponse.success("用户资料修改成功", adminResourceService.updateUser(user));
    }

    /**
     * 更新用户账号状态（启用/禁用）。
     *
     * <p>接口地址：<code>POST /api/admin/user/status</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestParam</strong>：从 URL 查询参数（Query String）中提取数据，
     *       例如 <code>?id=1&status=1</code>。
     *       适用于少量简单参数，无需构建完整的 JSON 请求体。</li>
     *   <li><strong>@PostMapping</strong>：映射 POST 请求，此处用于提交状态变更操作（有副作用，非纯查询）。</li>
     * </ul>
     *
     * <p>业务逻辑说明：</p>
     * <ul>
     *   <li>status 为 1 时，表示启用用户账号，返回消息“用户已启用”。</li>
     *   <li>status 为其他值（通常为 0）时，表示禁用用户账号，返回消息“用户已禁用”。</li>
     *   <li>采用状态切换而非物理删除，可保留用户历史数据，同时控制其登录权限。</li>
     * </ul>
     *
     * @param id     用户 ID，从 URL 查询参数中获取，用于定位目标用户。
     * @param status 目标状态，从 URL 查询参数中获取，1 表示启用，其他值表示禁用。
     * @return ApiResponse&lt;String&gt; 统一响应封装，data 为 "OK"，表示状态更新成功。
     */
    @PostMapping("/status")
    // @PostMapping 映射 POST 请求到 /api/admin/user/status，用于提交用户状态变更。
    public ApiResponse<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        // @RequestParam 从 URL 查询参数中解析 id 和 status，调用服务层更新用户状态。
        adminResourceService.updateUserStatus(id, status);
        // 根据 status 值返回不同的中文提示消息：1 为启用，其他为禁用。
        return ApiResponse.success(status != null && status == 1 ? "用户已启用" : "用户已禁用", "OK");
    }
}
