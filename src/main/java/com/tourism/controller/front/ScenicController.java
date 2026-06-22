package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.entity.ScenicSpot;
import com.tourism.service.ScenicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前端景点控制器（前端控制器）
 *
 * <p>该类负责处理景点（ScenicSpot）相关的HTTP请求，提供景点列表查询功能。
 * 作为前端Controller层，接收客户端HTTP请求，调用ScenicService业务层查询景点数据，
 * 并将结果封装为统一API响应返回。适用于旅游网站首页或景点浏览页面展示景点卡片列表。</p>
 *
 * <p>核心职责：
 * <ul>
 *   <li>查询系统中所有景点列表</li>
 *   <li>为前端景点展示页面提供数据支持</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
@RestController
// @RestController 标记该类为RESTful控制器，所有方法返回值直接写入HTTP响应体（JSON格式），
// 它是 @Controller + @ResponseBody 的复合注解，用于前后端分离架构。
// @Controller 将类标记为Spring管理的控制器Bean，@ResponseBody 确保返回值不走视图解析器，
// 直接由 HttpMessageConverter（如Jackson）序列化为JSON写入响应体。
@RequestMapping("/api/scenic")
// @RequestMapping 标注在类上，定义该类所有接口的URL前缀为 /api/scenic
// 例如：list() 方法的完整路径为 /api/scenic/list
public class ScenicController {

    @Autowired
    // @Autowired 自动注入Spring容器中类型匹配的ScenicService Bean实例
    // Spring 通过类型匹配（byType）查找容器中 ScenicService 接口的实现类，
    // 通过反射将其注入到该字段，实现"依赖注入（DI）"，降低控制器与服务层的耦合度。
    // 若存在多个 ScenicService 实现类，需配合 @Qualifier 指定具体Bean名称。
    private ScenicService scenicService;
    // scenicService：景点业务逻辑服务接口，封装了景点查询、景点详情、景点分类等核心业务逻辑

    /**
     * 查询所有景点列表
     *
     * <p>调用景点服务层查询系统中所有景点信息，返回景点实体列表。
     * 适用于首页"热门景点"、景点浏览页等场景，前端可遍历列表渲染景点卡片，
     * 展示景点名称、图片、评分、价格、地址等信息。</p>
     *
     * @return ApiResponse<List<ScenicSpot>> 统一API响应，data字段为ScenicSpot实体列表。
     *         ScenicSpot 是景点数据库实体类，通常包含字段：
     *         - id：景点唯一标识
     *         - name：景点名称
     *         - description：景点描述
     *         - location：地址/位置
     *         - price：门票价格
     *         - rating：评分（如4.8）
     *         - imageUrl：封面图片URL
     *         - category：分类（如"自然风光"、"历史古迹"）
     *         - lat/lng：经纬度坐标（供地图展示）
     *         返回此类实体列表给前端，便于前端组件直接绑定数据渲染。
     */
    @GetMapping("/list")
    // @GetMapping 是 @RequestMapping(method = RequestMethod.GET) 的缩写，
    // 用于映射 HTTP GET 请求。GET 请求语义为"获取/查询资源"，不会对服务器数据产生副作用，
    // 适合用于查询场景。此处用于查询所有景点列表，符合RESTful设计规范。
    // 完整路径：/api/scenic/list
    public ApiResponse<List<ScenicSpot>> list() {
        // 调用 scenicService.listAll() 方法，查询数据库中所有景点记录
        // 业务层可能包含：查询数据库、缓存读取（如Redis）、数据权限过滤（如仅展示上架景点）等逻辑
        List<ScenicSpot> scenicList = scenicService.listAll();

        // 将查询结果封装到 ApiResponse 统一响应对象中
        // ApiResponse.success(...) 会设置成功状态码（如200）、提示消息（"success"），
        // 并将查询列表放入 data 字段，确保前端接收的数据格式一致，便于统一处理。
        // 前端可据此渲染景点列表，每个景点卡片展示图片、名称、评分、价格等。
        return ApiResponse.success(scenicList);
    }
}
