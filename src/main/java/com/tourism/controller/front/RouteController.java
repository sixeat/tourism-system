package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.dto.RouteRecommendRequest;
import com.tourism.service.RouteService;
import com.tourism.vo.RouteRecommendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前端旅游路线控制器（前端控制器）
 *
 * <p>该类负责处理旅游路线相关的HTTP请求，核心功能包括根据用户偏好（出发城市、目的地、预算、天数、兴趣等）
 * 智能推荐个性化旅游路线。作为前端Controller层，接收客户端HTTP请求，调用RouteService业务层处理推荐逻辑，
 * 并将结果封装为统一API响应返回。</p>
 *
 * <p>核心职责：
 * <ul>
 *   <li>接收用户旅行偏好参数（预算、天数、兴趣等）</li>
 *   <li>调用智能推荐服务匹配最适合的旅游路线</li>
 *   <li>返回包含路线详情、预估费用、匹配度等信息的视图对象列表</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
@RestController
// @RestController 标记该类为RESTful控制器，所有方法返回值直接写入HTTP响应体（JSON格式），
// 它是 @Controller + @ResponseBody 的复合注解，用于前后端分离架构
@RequestMapping("/api/route")
// @RequestMapping 标注在类上，定义该类所有接口的URL前缀为 /api/route
// 例如：recommend() 方法的完整路径为 /api/route/recommend
public class RouteController {

    @Autowired
    // @Autowired 自动注入Spring容器中类型匹配的RouteService Bean实例
    // Spring IoC 容器负责创建和管理 RouteService 对象的生命周期，
    // 控制器通过依赖注入获得服务引用，无需手动 new，实现控制反转（IoC）
    private RouteService routeService;
    // routeService：旅游路线业务逻辑服务接口，封装了路线推荐、路线管理、路线匹配等核心业务逻辑

    /**
     * 智能推荐旅游路线
     *
     * <p>接收前端提交的旅行偏好请求（出发城市、目的地城市、预算、天数、兴趣标签等），
     * 调用路线推荐服务，基于算法（如规则匹配、协同过滤、内容推荐等）为用户推荐最匹配的旅游路线。
     * 适用于"旅行规划"页面的路线推荐功能，帮助用户快速发现感兴趣的行程。</p>
     *
     * @param request 路线推荐请求DTO，包含以下字段：
     *                - originCity：出发城市（可选，如"北京"）
     *                - destinationCity：目的城市（如"西安"）
     *                - city：目标城市（与destinationCity可能冗余，用于兼容不同场景）
     *                - budget：预算金额（BigDecimal类型，如 3000.00）
     *                - days：旅行天数（Integer类型，如 5）
     *                - interests：兴趣标签列表（List<String>，如 ["历史", "美食", "自然风光"]）
     *                @RequestBody 注解表示将HTTP请求体（JSON格式）通过Jackson反序列化为
     *                RouteRecommendRequest 对象。请求Content-Type应为 application/json。
     *                DTO（Data Transfer Object）模式用于封装前端传入的多字段数据，
     *                避免直接将前端参数映射到实体类，实现层间解耦。
     * @return ApiResponse<List<RouteRecommendVO>> 统一API响应，data字段为推荐路线视图对象列表。
     *         RouteRecommendVO（View Object）是专门为前端展示设计的聚合对象，包含：
     *         - routeName：路线名称
     *         - city：所在城市
     *         - days：天数
     *         - estimatedCost：预估费用
     *         - highlights：亮点列表
     *         - scenicOrder：景点顺序
     *         - theme：主题
     *         - routeDesc：路线描述
     *         - matchScore：匹配度分数（用于排序）
     *         - matchReason：匹配原因说明
     *         - budgetGap：预算差额
     *         VO 模式将多个数据库实体的字段或计算结果聚合为前端友好的结构，
     *         避免直接暴露数据库实体（Entity），提升接口安全性和灵活性。
     */
    @PostMapping("/recommend")
    // @PostMapping 映射 HTTP POST 请求，语义为"提交查询/创建资源"。
    // 路线推荐虽然本质是查询，但请求参数较复杂（多字段+列表），使用POST可避免URL过长，
    // 同时参数放在请求体中更安全。完整路径：/api/route/recommend
    public ApiResponse<List<RouteRecommendVO>> recommend(@RequestBody RouteRecommendRequest request) {
        // 调用路线服务的推荐方法，传入用户偏好请求DTO
        // 业务层推荐算法可能涉及：
        // 1. 筛选符合条件的路线（如天数 <= 用户预算天数、预估费用 <= 用户预算）
        // 2. 计算兴趣标签匹配度（如路线包含的景点标签与用户兴趣的重合度）
        // 3. 计算综合匹配分数并排序
        // 4. 生成推荐理由（如"该路线包含您感兴趣的历史景点"）
        List<RouteRecommendVO> recommendList = routeService.recommendRoutes(request);

        // 将推荐结果封装到统一响应对象返回
        // 前端可根据 matchScore 对结果进行高亮展示，或根据 budgetGap 提示用户预算是否充足
        return ApiResponse.success(recommendList);
    }
}
