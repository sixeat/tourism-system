package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理员后台仪表盘控制器（Admin Dashboard Controller）
 *
 * <p>本控制器提供管理员后台首页的数据统计与汇总功能，
 * 仅面向具备管理员权限的用户开放。</p>
 *
 * <p>说明：</p>
 * <ul>
 *   <li>所有接口统一挂载在 <code>/api/admin/dashboard</code> 路径下，
 *       与前台接口 <code>/api/**</code> 隔离，便于在拦截器/过滤器中做统一权限校验。</li>
 *   <li>通常项目会在拦截器层拦截 <code>/api/admin/**</code> 前缀的请求，
 *       检查当前用户是否已登录且具有管理员角色，未授权则返回 403 或重定向。</li>
 * </ul>
 *
 * @author Tourism System
 */
@RestController
// @RestController 是 Spring 的复合注解，等价于 @Controller + @ResponseBody。
// 表示该类中所有方法的返回值将直接作为 HTTP 响应体（JSON/XML），无需视图解析。
@RequestMapping("/api/admin/dashboard")
// @RequestMapping 用于映射 HTTP 请求到控制器类或方法上。
// 此处作用于类级别，表示本控制器下所有接口的 URL 前缀为 /api/admin/dashboard。
// 前缀中的 /api 表明这是 REST API 接口；/admin 表明仅管理员可访问，受拦截器保护。
public class AdminDashboardController {

    @Autowired
    // @Autowired 是 Spring 的依赖注入注解，表示 Spring 容器会自动将类型匹配的 Bean 注入到该字段中。
    // 此处注入 AdminDashboardService，用于获取仪表盘所需的统计数据。
    private AdminDashboardService adminDashboardService;

    /**
     * 获取管理员后台仪表盘汇总数据。
     *
     * <p>接口地址：<code>GET /api/admin/dashboard/summary</code></p>
     *
     * <p>返回的数据通常包含以下维度（具体以 AdminDashboardService 实现为准）：</p>
     * <ul>
     *   <li>用户总量、今日新增用户</li>
     *   <li>订单总量、待处理订单数</li>
     *   <li>酒店/景点/门票/路线等资源数量</li>
     *   <li>收入统计等经营指标</li>
     * </ul>
     *
     * @return ApiResponse&lt;Map&lt;String, Object&gt;&gt; 统一封装的成功响应，
     *         data 字段为 key-value 形式的统计汇总数据。
     *         Map 结构灵活，便于后续扩展更多统计项而无需修改接口契约。
     */
    @GetMapping("/summary")
    // @GetMapping 是 @RequestMapping(method = RequestMethod.GET) 的缩写，
    // 表示该方法只处理 HTTP GET 请求，用于获取资源（无副作用，幂等）。
    // 路径 /summary 将拼接在类级别的 /api/admin/dashboard 之后，形成完整 URL。
    public ApiResponse<Map<String, Object>> summary() {
        // 调用服务层获取仪表盘汇总数据，并封装成统一成功响应返回给前端。
        return ApiResponse.success(adminDashboardService.summary());
    }
}
