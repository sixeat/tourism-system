package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.entity.Ticket;
import com.tourism.service.AdminResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员后台——门票资源管理控制器（Admin Ticket Controller）
 *
 * <p>本控制器提供门票资源的增删改查（CRUD）接口，仅管理员可访问。</p>
 *
 * <p>权限说明：</p>
 * <ul>
 *   <li>所有接口挂载在 <code>/api/admin/ticket</code> 下，受 <code>/api/admin/**</code> 拦截器保护。</li>
 *   <li>管理员通过本接口维护前台展示的门票信息，如门票名称、适用景点、价格、库存、有效期等。</li>
 * </ul>
 *
 * @author Tourism System
 */
@RestController
// @RestController 声明为 REST 控制器，所有方法直接返回 JSON 响应体，不经过视图渲染。
@RequestMapping("/api/admin/ticket")
// 类级 URL 前缀：/api/admin/ticket。/api 表示 REST 接口；/admin 表示管理员后台，受拦截器保护；/ticket 表示门票资源。
public class AdminTicketController {

    @Autowired
    // @Autowired 自动注入 AdminResourceService，该服务层负责门票、景点、酒店、用户等资源的通用管理。
    private AdminResourceService adminResourceService;

    /**
     * 查询所有门票列表。
     *
     * <p>接口地址：<code>GET /api/admin/ticket/list</code></p>
     *
     * <p>HTTP 语义：</p>
     * <ul>
     *   <li>GET 请求用于安全、幂等地获取数据，不修改服务器状态。</li>
     * </ul>
     *
     * @return ApiResponse&lt;List&lt;Ticket&gt;&gt; 统一响应封装，data 为门票实体列表。
     *         Ticket 为门票数据库实体类，对应门票表结构。
     */
    @GetMapping("/list")
    // @GetMapping 映射 GET 请求到 /api/admin/ticket/list，用于查询全部门票数据。
    public ApiResponse<List<Ticket>> list() {
        // 调用服务层获取所有门票记录，并包装成统一成功响应返回给前端。
        return ApiResponse.success(adminResourceService.listTickets());
    }

    /**
     * 新增门票。
     *
     * <p>接口地址：<code>POST /api/admin/ticket/save</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestBody</strong>：将 HTTP 请求体（JSON 格式）自动反序列化为 Ticket 实体对象。
     *       前端需发送 Content-Type: application/json 的请求，Spring 的 HttpMessageConverter（默认 Jackson）负责解析。</li>
     *   <li><strong>@PostMapping</strong>：映射 POST 请求，语义为“创建新资源”，用于保存门票数据。</li>
     * </ul>
     *
     * @param ticket 从请求体 JSON 解析得到的门票实体对象，包含名称、适用景点、价格、库存、有效期等字段。
     * @return ApiResponse&lt;Ticket&gt; 统一响应封装，data 为保存后的门票对象（通常包含数据库自动生成的 id）。
     */
    @PostMapping("/save")
    // @PostMapping 映射 POST 请求到 /api/admin/ticket/save，用于创建门票资源。
    public ApiResponse<Ticket> save(@RequestBody Ticket ticket) {
        // @RequestBody 将前端传来的 JSON 数据自动转换为 Ticket 实体，然后交给服务层持久化到数据库。
        return ApiResponse.success("门票保存成功", adminResourceService.saveTicket(ticket));
    }

    /**
     * 修改门票信息。
     *
     * <p>接口地址：<code>PUT /api/admin/ticket/update</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestBody</strong>：将请求体 JSON 反序列化为 Ticket 对象，要求传入 id 字段以定位待更新记录。</li>
     *   <li><strong>@PutMapping</strong>：映射 PUT 请求，HTTP 语义为“完整更新资源”，多次执行结果一致，具有幂等性。</li>
     * </ul>
     *
     * @param ticket 包含更新字段的门票实体，必须携带 id 以标识要修改的记录。
     * @return ApiResponse&lt;Ticket&gt; 统一响应封装，data 为更新后的门票对象。
     */
    @PutMapping("/update")
    // @PutMapping 映射 PUT 请求到 /api/admin/ticket/update，用于完整更新门票资源。
    public ApiResponse<Ticket> update(@RequestBody Ticket ticket) {
        // 将请求体中的门票数据交给服务层执行更新，返回更新后的实体数据。
        return ApiResponse.success("门票修改成功", adminResourceService.updateTicket(ticket));
    }

    /**
     * 根据 ID 删除门票。
     *
     * <p>接口地址：<code>DELETE /api/admin/ticket/delete/{id}</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@PathVariable</strong>：将 URL 路径中的 {id} 占位符绑定到方法参数上。
     *       例如 DELETE /api/admin/ticket/delete/10，则 id 参数值为 10（Long 类型）。</li>
     *   <li><strong>@DeleteMapping</strong>：映射 DELETE 请求，语义为“删除资源”，幂等操作。</li>
     * </ul>
     *
     * @param id 从 URL 路径中提取的门票 ID，主键，用于唯一标识并删除该门票记录。
     * @return ApiResponse&lt;String&gt; 统一响应封装，data 为 "OK"，表示删除成功。
     */
    @DeleteMapping("/delete/{id}")
    // @DeleteMapping 映射 DELETE 请求到 /api/admin/ticket/delete/{id}，用于删除指定门票。
    // @PathVariable 将路径中的 {id} 提取为 Long 类型参数传入方法。
    public ApiResponse<String> delete(@PathVariable Long id) {
        // 调用服务层根据主键删除门票记录，删除为不可逆操作，建议前端增加二次确认提示。
        adminResourceService.deleteTicket(id);
        return ApiResponse.success("门票删除成功", "OK");
    }
}
