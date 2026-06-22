package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.entity.ScenicSpot;
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
 * 管理员后台——景点资源管理控制器（Admin Scenic Controller）
 *
 * <p>本控制器提供景点资源的增删改查（CRUD）接口，仅管理员可访问。</p>
 *
 * <p>权限说明：</p>
 * <ul>
 *   <li>所有接口挂载在 <code>/api/admin/scenic</code> 下，受 <code>/api/admin/**</code> 拦截器保护。</li>
 *   <li>管理员通过本接口维护前台展示的景点信息，如景点名称、描述、图片、地理位置、门票价格等。</li>
 * </ul>
 *
 * @author Tourism System
 */
@RestController
// @RestController 声明为 REST 控制器，所有方法直接返回 JSON 响应体，不经过视图解析器。
@RequestMapping("/api/admin/scenic")
// 类级 URL 前缀：/api/admin/scenic。/api 表示 REST API；/admin 表示管理员专属，受拦截器保护；/scenic 表示景点资源。
public class AdminScenicController {

    @Autowired
    // @Autowired 自动注入 AdminResourceService，该服务层负责景点、酒店、门票、用户等资源的通用管理。
    private AdminResourceService adminResourceService;

    /**
     * 查询所有景点列表。
     *
     * <p>接口地址：<code>GET /api/admin/scenic/list</code></p>
     *
     * <p>HTTP 语义：</p>
     * <ul>
     *   <li>GET 请求用于安全、幂等地获取数据，不修改服务器状态。</li>
     * </ul>
     *
     * @return ApiResponse&lt;List&lt;ScenicSpot&gt;&gt; 统一响应封装，data 为景点实体列表。
     *         ScenicSpot 为景点数据库实体类，对应景点表结构。
     */
    @GetMapping("/list")
    // @GetMapping 映射 GET 请求到 /api/admin/scenic/list，用于查询全部景点数据。
    public ApiResponse<List<ScenicSpot>> list() {
        // 调用服务层获取所有景点记录，并包装成统一成功响应返回给前端。
        return ApiResponse.success(adminResourceService.listScenicSpots());
    }

    /**
     * 新增景点。
     *
     * <p>接口地址：<code>POST /api/admin/scenic/save</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestBody</strong>：将 HTTP 请求体（JSON 格式）自动反序列化为 ScenicSpot 实体对象。
     *       前端需发送 Content-Type: application/json 的请求，Spring 的 HttpMessageConverter（默认 Jackson）负责解析。</li>
     *   <li><strong>@PostMapping</strong>：映射 POST 请求，语义为“创建新资源”，用于保存景点数据。</li>
     * </ul>
     *
     * @param scenicSpot 从请求体 JSON 解析得到的景点实体对象，包含名称、简介、地址、图片 URL、门票价格等字段。
     * @return ApiResponse&lt;ScenicSpot&gt; 统一响应封装，data 为保存后的景点对象（通常包含数据库自动生成的 id）。
     */
    @PostMapping("/save")
    // @PostMapping 映射 POST 请求到 /api/admin/scenic/save，用于创建景点资源。
    public ApiResponse<ScenicSpot> save(@RequestBody ScenicSpot scenicSpot) {
        // @RequestBody 将前端传来的 JSON 数据自动转换为 ScenicSpot 实体，然后交给服务层持久化到数据库。
        return ApiResponse.success("景点保存成功", adminResourceService.saveScenicSpot(scenicSpot));
    }

    /**
     * 修改景点信息。
     *
     * <p>接口地址：<code>PUT /api/admin/scenic/update</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestBody</strong>：将请求体 JSON 反序列化为 ScenicSpot 对象，要求传入 id 字段以定位待更新记录。</li>
     *   <li><strong>@PutMapping</strong>：映射 PUT 请求，HTTP 语义为“完整更新资源”，多次执行结果一致，具有幂等性。</li>
     * </ul>
     *
     * @param scenicSpot 包含更新字段的景点实体，必须携带 id 以标识要修改的记录。
     * @return ApiResponse&lt;ScenicSpot&gt; 统一响应封装，data 为更新后的景点对象。
     */
    @PutMapping("/update")
    // @PutMapping 映射 PUT 请求到 /api/admin/scenic/update，用于完整更新景点资源。
    public ApiResponse<ScenicSpot> update(@RequestBody ScenicSpot scenicSpot) {
        // 将请求体中的景点数据交给服务层执行更新，返回更新后的实体数据。
        return ApiResponse.success("景点修改成功", adminResourceService.updateScenicSpot(scenicSpot));
    }

    /**
     * 根据 ID 删除景点。
     *
     * <p>接口地址：<code>DELETE /api/admin/scenic/delete/{id}</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@PathVariable</strong>：将 URL 路径中的 {id} 占位符绑定到方法参数上。
     *       例如 DELETE /api/admin/scenic/delete/7，则 id 参数值为 7（Long 类型）。</li>
     *   <li><strong>@DeleteMapping</strong>：映射 DELETE 请求，语义为“删除资源”，幂等操作。</li>
     * </ul>
     *
     * @param id 从 URL 路径中提取的景点 ID，主键，用于唯一标识并删除该景点记录。
     * @return ApiResponse&lt;String&gt; 统一响应封装，data 为 "OK"，表示删除成功。
     */
    @DeleteMapping("/delete/{id}")
    // @DeleteMapping 映射 DELETE 请求到 /api/admin/scenic/delete/{id}，用于删除指定景点。
    // @PathVariable 将路径中的 {id} 提取为 Long 类型参数传入方法。
    public ApiResponse<String> delete(@PathVariable Long id) {
        // 调用服务层根据主键删除景点记录，删除为不可逆操作，建议前端增加二次确认提示。
        adminResourceService.deleteScenicSpot(id);
        return ApiResponse.success("景点删除成功", "OK");
    }
}
