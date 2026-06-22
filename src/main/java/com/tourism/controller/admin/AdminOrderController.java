package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.service.AdminOrderService;
import com.tourism.vo.AdminOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员后台——订单管理控制器（Admin Order Controller）
 *
 * <p>本控制器提供订单查询与状态更新功能，涵盖酒店订单和门票订单，仅管理员可访问。</p>
 *
 * <p>权限说明：</p>
 * <ul>
 *   <li>所有接口挂载在 <code>/api/admin/order</code> 下，受 <code>/api/admin/**</code> 拦截器保护。</li>
 *   <li>管理员可通过本接口查看全站订单并修改订单状态（如确认、取消、完成等）。</li>
 * </ul>
 *
 * @author Tourism System
 */
@RestController
// @RestController 表明该类为 REST 控制器，所有方法直接返回 JSON 数据。
@RequestMapping("/api/admin/order")
// 类级 URL 前缀：/api/admin/order。前缀中的 /admin 用于拦截器识别管理员专属接口。
public class AdminOrderController {

    @Autowired
    // @Autowired 自动注入 AdminOrderService，用于处理订单相关的业务逻辑。
    private AdminOrderService adminOrderService;

    /**
     * 查询所有订单列表。
     *
     * <p>接口地址：<code>GET /api/admin/order/list</code></p>
     *
     * <p>说明：</p>
     * <ul>
     *   <li>GET 请求用于无状态、幂等的数据查询，适合获取列表。</li>
     *   <li>返回的 AdminOrderVO（Value Object）是面向展示层的视图对象，通常包含用户昵称、商品名称等扩展字段，
     *       比原始实体更适合前端表格展示。</li>
     * </ul>
     *
     * @return ApiResponse&lt;List&lt;AdminOrderVO&gt;&gt; 统一响应封装，data 为订单视图对象列表。
     */
    @GetMapping("/list")
    // @GetMapping 映射 GET 请求到 /api/admin/order/list，用于获取全站订单数据。
    public ApiResponse<List<AdminOrderVO>> list() {
        // 调用服务层获取所有订单，并以 VO 列表形式返回，便于前端展示。
        return ApiResponse.success(adminOrderService.listAllOrders());
    }

    /**
     * 更新酒店订单状态。
     *
     * <p>接口地址：<code>POST /api/admin/order/hotel/status</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestParam</strong>：用于从 URL 查询参数（Query String）中提取数据，
     *       例如 <code>?id=1&orderStatus=CONFIRMED</code>。
     *       与 @PathVariable 不同，它不要求参数嵌入在路径中，而是暴露在 URL 问号后面，适合少量简单参数。</li>
     *   <li><strong>@PostMapping</strong>：映射 POST 请求，此处用于提交状态变更操作（非纯查询，有一定副作用）。</li>
     * </ul>
     *
     * @param id          订单 ID，从 URL 查询参数中获取，用于定位酒店订单记录。
     * @param orderStatus 目标状态字符串，如 PENDING、CONFIRMED、CANCELLED、COMPLETED 等，由查询参数传入。
     * @return ApiResponse&lt;String&gt; 统一响应封装，data 为 "OK"，表示状态更新成功。
     */
    @PostMapping("/hotel/status")
    // @PostMapping 映射 POST 请求到 /api/admin/order/hotel/status，用于提交酒店订单状态变更。
    public ApiResponse<String> updateHotelStatus(@RequestParam Long id, @RequestParam String orderStatus) {
        // @RequestParam 从请求 URL 的查询参数中解析 id 和 orderStatus，交给服务层更新酒店订单状态。
        adminOrderService.updateHotelOrderStatus(id, orderStatus);
        return ApiResponse.success("酒店订单状态已更新", "OK");
    }

    /**
     * 更新门票订单状态。
     *
     * <p>接口地址：<code>POST /api/admin/order/ticket/status</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestParam</strong>：从 URL 查询参数中获取 id 和 orderStatus，适合简单表单提交。</li>
     * </ul>
     *
     * @param id          门票订单 ID，从查询参数获取。
     * @param orderStatus 目标状态字符串，从查询参数获取。
     * @return ApiResponse&lt;String&gt; 统一响应封装，data 为 "OK"，表示状态更新成功。
     */
    @PostMapping("/ticket/status")
    // @PostMapping 映射 POST 请求到 /api/admin/order/ticket/status，用于提交门票订单状态变更。
    public ApiResponse<String> updateTicketStatus(@RequestParam Long id, @RequestParam String orderStatus) {
        // 调用服务层更新门票订单状态。
        adminOrderService.updateTicketOrderStatus(id, orderStatus);
        return ApiResponse.success("门票订单状态已更新", "OK");
    }
}
