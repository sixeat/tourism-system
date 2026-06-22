package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.dto.RouteManageRequest;
import com.tourism.service.AdminRouteService;
import com.tourism.vo.RouteManageVO;
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
 * 管理员后台——旅游线路管理控制器（Admin Route Controller）
 *
 * <p>本控制器提供旅游路线的增删改查（CRUD）接口，仅管理员可访问。</p>
 *
 * <p>权限说明：</p>
 * <ul>
 *   <li>所有接口挂载在 <code>/api/admin/route</code> 下，受 <code>/api/admin/**</code> 拦截器保护。</li>
 *   <li>管理员通过本接口维护前台展示的旅游路线数据，包括路线名称、途经景点、行程安排、价格等。</li>
 * </ul>
 *
 * @author Tourism System
 */
@RestController
// @RestController 声明为 REST 控制器，所有方法直接返回 JSON 响应体，不走视图解析。
@RequestMapping("/api/admin/route")
// 类级 URL 前缀：/api/admin/route。/api 表示 REST 接口；/admin 表示管理员后台，受拦截器保护；/route 表示路线资源。
public class AdminRouteController {

    @Autowired
    // @Autowired 自动注入 AdminRouteService，专门处理路线相关的业务逻辑。
    private AdminRouteService adminRouteService;

    /**
     * 查询所有旅游路线列表。
     *
     * <p>接口地址：<code>GET /api/admin/route/list</code></p>
     *
     * <p>HTTP 语义：</p>
     * <ul>
     *   <li>GET 请求用于安全、幂等地获取数据，不修改服务器状态。</li>
     * </ul>
     *
     * <p>返回类型说明：</p>
     * <ul>
     *   <li>RouteManageVO 是路线管理视图对象，通常包含路线基础信息、关联景点列表、行程天数等前端展示所需字段。</li>
     * </ul>
     *
     * @return ApiResponse&lt;List&lt;RouteManageVO&gt;&gt; 统一响应封装，data 为路线视图对象列表。
     */
    @GetMapping("/list")
    // @GetMapping 映射 GET 请求到 /api/admin/route/list，用于查询路线数据。
    public ApiResponse<List<RouteManageVO>> list() {
        // 调用服务层获取所有路线数据，以 VO 列表形式返回，便于前端管理与展示。
        return ApiResponse.success(adminRouteService.listRoutes());
    }

    /**
     * 新增旅游路线。
     *
     * <p>接口地址：<code>POST /api/admin/route/save</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestBody</strong>：将 HTTP 请求体（JSON 格式）中的数据反序列化为 RouteManageRequest 对象。
     *       前端需发送 Content-Type: application/json 的请求，Spring 会自动通过 Jackson 等工具完成映射。</li>
     *   <li><strong>@PostMapping</strong>：映射 POST 请求，语义为“创建资源”，用于保存新路线。</li>
     * </ul>
     *
     * @param request 路线保存请求对象，包含路线名称、行程天数、途经景点 ID 列表、价格等字段。
     * @return ApiResponse&lt;RouteManageVO&gt; 统一响应封装，data 为保存成功后生成的路线视图对象。
     */
    @PostMapping("/save")
    // @PostMapping 映射 POST 请求到 /api/admin/route/save，用于创建新路线资源。
    public ApiResponse<RouteManageVO> save(@RequestBody RouteManageRequest request) {
        // @RequestBody 将前端 JSON 请求体自动转换为 RouteManageRequest 对象，交由服务层处理保存逻辑。
        return ApiResponse.success("路线保存成功", adminRouteService.saveRoute(request));
    }

    /**
     * 修改旅游路线信息。
     *
     * <p>接口地址：<code>PUT /api/admin/route/update</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestBody</strong>：将请求体 JSON 反序列化为 RouteManageRequest 对象，其中必须包含 id 字段以定位记录。</li>
     *   <li><strong>@PutMapping</strong>：映射 PUT 请求，HTTP 语义为“完整更新资源”，具有幂等性。</li>
     * </ul>
     *
     * @param request 包含更新字段的路线请求对象，需携带 id 标识待更新记录。
     * @return ApiResponse&lt;RouteManageVO&gt; 统一响应封装，data 为更新后的路线视图对象。
     */
    @PutMapping("/update")
    // @PutMapping 映射 PUT 请求到 /api/admin/route/update，用于完整更新路线资源。
    public ApiResponse<RouteManageVO> update(@RequestBody RouteManageRequest request) {
        // 将请求体中的路线数据交给服务层执行更新，返回更新后的 VO 数据。
        return ApiResponse.success("路线修改成功", adminRouteService.updateRoute(request));
    }

    /**
     * 根据 ID 删除旅游路线。
     *
     * <p>接口地址：<code>DELETE /api/admin/route/delete/{id}</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@PathVariable</strong>：将 URL 路径中的 {id} 占位符绑定到方法参数上。
     *       例如 DELETE /api/admin/route/delete/3，则 id 参数值为 3。</li>
     *   <li><strong>@DeleteMapping</strong>：映射 DELETE 请求，HTTP 语义为“删除资源”，幂等操作。</li>
     * </ul>
     *
     * @param id 从 URL 路径中提取的路线 ID，主键，用于唯一标识并删除该路线记录。
     * @return ApiResponse&lt;String&gt; 统一响应封装，data 为 "OK"，表示删除操作成功。
     */
    @DeleteMapping("/delete/{id}")
    // @DeleteMapping 映射 DELETE 请求到 /api/admin/route/delete/{id}，用于删除指定路线。
    // @PathVariable 提取路径中的 {id} 值作为 Long 类型的方法参数。
    public ApiResponse<String> delete(@PathVariable Long id) {
        // 调用服务层根据主键删除路线记录，删除操作通常不可逆，建议前端增加二次确认。
        adminRouteService.deleteRoute(id);
        return ApiResponse.success("路线删除成功", "OK");
    }
}
