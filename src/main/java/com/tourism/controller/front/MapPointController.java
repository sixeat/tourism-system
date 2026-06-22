package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.entity.MapPoint;
import com.tourism.mapper.MapPointMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前端地图点位控制器（前端控制器）
 *
 * <p>该类负责处理地图点位（POI - Point of Interest）相关的HTTP请求，
 * 为前端地图组件提供景点、酒店、餐厅等地理位置数据。作为前端Controller层，
 * 接收客户端HTTP请求，调用数据访问层（Mapper）查询数据，并封装为统一API响应返回。</p>
 *
 * <p>核心职责：
 * <ul>
 *   <li>查询所有地图点位（不分类型）</li>
 *   <li>按类型筛选地图点位（如景点、酒店、餐厅等）</li>
 * </ul></p>
 *
 * <p>设计说明：此类直接使用 Mapper 而非 Service，属于简单查询场景，
 * 无明显业务逻辑，可跳过Service层以减少冗余。若后续增加缓存、权限校验等，
 * 建议补充Service层。</p>
 *
 * @author tourism-system
 * @version 1.0
 */
@RestController
// @RestController 标记该类为RESTful控制器，所有方法返回值直接写入HTTP响应体（JSON格式），
// 它是 @Controller + @ResponseBody 的复合注解，用于前后端分离架构
@RequestMapping("/api/map")
// @RequestMapping 标注在类上，定义该类所有接口的URL前缀为 /api/map
// 例如：points() 方法的完整路径为 /api/map/points
public class MapPointController {

    @Autowired
    // @Autowired 自动注入Spring容器中类型匹配的MapPointMapper Bean实例
    // 注入的是MyBatis Mapper接口的代理对象，由MyBatis-Spring自动生成并注册到Spring容器中
    // 直接使用Mapper属于简单场景，无需额外Service层封装
    private MapPointMapper mapPointMapper;
    // mapPointMapper：地图点位数据访问接口（MyBatis Mapper），定义了点位数据的增删改查SQL操作

    /**
     * 查询地图点位列表
     *
     * <p>接收前端传入的点位类型参数，返回符合条件的地图点位列表。若未指定类型或
     * 指定为"ALL"，则返回全部点位。适用于旅游地图页面，展示景点、酒店、餐厅等
     * 地理标记（Marker）。</p>
     *
     * @param pointType 点位类型（可选参数），如"SCENIC"（景点）、"HOTEL"（酒店）、
     *                  "RESTAURANT"（餐厅）等，通过URL查询参数传递。
     *                  @RequestParam(required = false) 表示该参数不是必须的：
     *                  - 如果请求中未包含 pointType 参数，Spring 将传入 null
     *                  - 如果参数值为空字符串或"ALL"，则返回全部点位
     *                  required = false 常用于筛选条件，提升接口灵活性
     * @return ApiResponse<List<MapPoint>> 统一API响应，data字段为MapPoint实体列表。
     *         MapPoint 通常包含字段：id（点位ID）、name（名称）、lat（纬度）、lng（经度）、
     *         type（类型）、address（地址）、icon（图标URL）等，供前端地图组件渲染使用。
     */
    @GetMapping("/points")
    // @GetMapping 映射 HTTP GET 请求，用于查询地图点位资源
    // 完整路径：/api/map/points
    public ApiResponse<List<MapPoint>> points(@RequestParam(required = false) String pointType) {
        // 判断 pointType 参数是否为空、仅包含空白字符或为"ALL"（忽略大小写）
        // 使用 trim() 去除首尾空白，避免用户误输入空格导致查询失败
        // "ALL".equalsIgnoreCase(pointType) 忽略大小写比较，支持"all"、"All"、"ALL"等多种写法
        if (pointType == null || pointType.trim().isEmpty() || "ALL".equalsIgnoreCase(pointType)) {
            // 条件成立：未指定类型或指定为全部，调用 selectAll() 查询所有地图点位
            // 适用于地图页面首次加载，展示全部点位
            List<MapPoint> allPoints = mapPointMapper.selectAll();
            return ApiResponse.success(allPoints);
        }

        // 条件不成立：指定了具体类型，调用 selectByType(...) 按类型查询点位
        // trim() 去除首尾空格，防止因前后空格导致数据库查询不到结果
        // 例如：pointType="SCENIC"，则查询所有 type='SCENIC' 的点位记录
        List<MapPoint> typedPoints = mapPointMapper.selectByType(pointType.trim());

        // 将查询结果封装到统一响应对象返回
        // 前端地图组件（如高德地图、百度地图、Leaflet）可遍历此列表，为每个点位创建Marker
        return ApiResponse.success(typedPoints);
    }
}
