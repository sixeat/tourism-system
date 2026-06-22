package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.entity.Hotel;
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
 * 管理员后台——酒店资源管理控制器（Admin Hotel Controller）
 *
 * <p>本控制器提供酒店资源的增删改查（CRUD）接口，仅管理员可访问。</p>
 *
 * <p>权限与安全说明：</p>
 * <ul>
 *   <li>所有接口统一挂在 <code>/api/admin/hotel</code> 路径下。</li>
 *   <li>由于 URL 以 <code>/api/admin/**</code> 开头，项目通常配置了拦截器（如 AdminInterceptor 或 Spring Security），
 *       对请求进行身份认证和权限校验，防止非管理员用户操作后台数据。</li>
 * </ul>
 *
 * @author Tourism System
 */
@RestController
// @RestController = @Controller + @ResponseBody，表明所有方法返回 JSON 数据而非视图页面。
@RequestMapping("/api/admin/hotel")
// @RequestMapping 定义类级 URL 前缀。/api 标识 REST 接口；/admin 标识管理员后台；/hotel 标识酒店资源。
public class AdminHotelController {

    @Autowired
    // @Autowired 自动注入 Spring 容器中的 AdminResourceService Bean。
    // 该服务层负责酒店、景点、门票、用户等资源的通用管理逻辑。
    private AdminResourceService adminResourceService;

    /**
     * 查询所有酒店列表。
     *
     * <p>接口地址：<code>GET /api/admin/hotel/list</code></p>
     *
     * <p>HTTP 方法说明：</p>
     * <ul>
     *   <li>GET 请求用于安全、幂等地获取数据，不应修改服务器状态。</li>
     * </ul>
     *
     * @return ApiResponse&lt;List&lt;Hotel&gt;&gt; 统一响应封装，data 为酒店实体列表。
     *         List&lt;Hotel&gt; 表示返回多个酒店对象的集合。
     */
    @GetMapping("/list")
    // @GetMapping 映射 GET 请求到 /api/admin/hotel/list，用于查询数据。
    public ApiResponse<List<Hotel>> list() {
        // 调用服务层获取全部酒店数据，并包装成统一的成功响应返回。
        return ApiResponse.success(adminResourceService.listHotels());
    }

    /**
     * 新增酒店。
     *
     * <p>接口地址：<code>POST /api/admin/hotel/save</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestBody</strong>：表示将 HTTP 请求体（JSON 格式）中的数据反序列化为 Hotel 对象。
     *       前端需发送 Content-Type: application/json 的请求体，Spring 的 HttpMessageConverter 会自动完成映射。</li>
     *   <li><strong>@PostMapping</strong>：映射 POST 请求，用于在服务器上创建新资源，语义上表示“新增/保存”。</li>
     * </ul>
     *
     * @param hotel 从请求体 JSON 解析得到的酒店实体对象，包含名称、地址、价格、图片等字段。
     * @return ApiResponse&lt;Hotel&gt; 统一响应封装，data 为保存后的酒店对象（通常包含数据库生成的 id）。
     */
    @PostMapping("/save")
    // @PostMapping 映射 POST 请求到 /api/admin/hotel/save，用于创建资源。
    public ApiResponse<Hotel> save(@RequestBody Hotel hotel) {
        // @RequestBody 将前端传来的 JSON 自动转换为 Hotel 实体，然后交给服务层持久化。
        return ApiResponse.success("酒店保存成功", adminResourceService.saveHotel(hotel));
    }

    /**
     * 修改酒店信息。
     *
     * <p>接口地址：<code>PUT /api/admin/hotel/update</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@RequestBody</strong>：将请求体 JSON 反序列化为 Hotel 对象，通常要求传入 id 以定位记录。</li>
     *   <li><strong>@PutMapping</strong>：映射 PUT 请求，HTTP 语义为“更新/替换资源”，具有幂等性，
     *       多次执行相同请求结果一致。</li>
     * </ul>
     *
     * @param hotel 包含更新字段的酒店实体，必须携带 id 以标识待更新记录。
     * @return ApiResponse&lt;Hotel&gt; 统一响应封装，data 为更新后的酒店对象。
     */
    @PutMapping("/update")
    // @PutMapping 映射 PUT 请求到 /api/admin/hotel/update，用于完整更新资源。
    public ApiResponse<Hotel> update(@RequestBody Hotel hotel) {
        // 将请求体中的酒店数据交给服务层执行更新操作。
        return ApiResponse.success("酒店修改成功", adminResourceService.updateHotel(hotel));
    }

    /**
     * 根据 ID 删除酒店。
     *
     * <p>接口地址：<code>DELETE /api/admin/hotel/delete/{id}</code></p>
     *
     * <p>注解说明：</p>
     * <ul>
     *   <li><strong>@PathVariable</strong>：将 URL 路径中的变量绑定到方法参数上。
     *       例如请求 DELETE /api/admin/hotel/delete/5，则 id 参数值为 5（Long 类型）。</li>
     *   <li><strong>@DeleteMapping</strong>：映射 DELETE 请求，HTTP 语义为“删除资源”，具有幂等性。</li>
     * </ul>
     *
     * @param id 从 URL 路径中提取的酒店 ID，主键，用于唯一标识待删除的记录。
     * @return ApiResponse&lt;String&gt; 统一响应封装，data 为简单字符串 "OK"，表示操作成功。
     */
    @DeleteMapping("/delete/{id}")
    // @DeleteMapping 映射 DELETE 请求到 /api/admin/hotel/delete/{id}，用于删除资源。
    // @PathVariable 提取路径中的 {id} 占位符值作为方法参数。
    public ApiResponse<String> delete(@PathVariable Long id) {
        // 调用服务层根据主键删除酒店记录，删除为不可逆操作，通常需配合前端二次确认。
        adminResourceService.deleteHotel(id);
        // 返回成功消息，告知前端删除操作已完成。
        return ApiResponse.success("酒店删除成功", "OK");
    }
}
