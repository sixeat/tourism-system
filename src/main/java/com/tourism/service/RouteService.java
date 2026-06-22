package com.tourism.service;

import com.tourism.dto.RouteRecommendRequest;
import com.tourism.vo.RouteRecommendVO;

import java.util.List;

/**
 * 旅游路线推荐服务接口（Service Contract）。
 * <p>
 * 本接口定义了基于用户出行偏好（目的地、天数、预算、兴趣）推荐旅游路线的服务契约。
 * 推荐算法采用数据库优先策略：先匹配系统已有的 TravelRoute 记录并按匹配度排序；
     * 若数据库无匹配路线，则生成动态备选路线（Fallback），基于城市热门景点排序与预算估算。
 * 实现类：{@link com.tourism.service.impl.RouteServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface RouteService {

    /**
     * 根据用户出行偏好推荐旅游路线。
     * <p>
     * 业务逻辑：
     * 1. 优先从数据库查询符合条件的 TravelRoute，按匹配度评分排序（天数差、预算差、主题兴趣、景点描述等维度打分）；
     * 2. 若数据库中有匹配结果，则返回最多 3 条最优路线，每条路线包含景点游览顺序、亮点、匹配原因、预算差等；
     * 3. 若数据库无匹配，则基于目标城市的热门景点自动生成两条备选路线（城市精华路线 + 高性价比路线）。
     * </p>
     *
     * @param request 路线推荐请求对象，包含出发地、目的地、城市、天数、预算、兴趣等字段
     * @return 推荐路线视图对象列表，元素类型为 {@link RouteRecommendVO}
     */
    List<RouteRecommendVO> recommendRoutes(RouteRecommendRequest request);
}
