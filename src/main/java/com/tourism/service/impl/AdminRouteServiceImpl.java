package com.tourism.service.impl;

import com.tourism.common.BusinessException;
import com.tourism.dto.RouteManageRequest;
import com.tourism.entity.RouteSpot;
import com.tourism.entity.TravelRoute;
import com.tourism.mapper.RouteSpotMapper;
import com.tourism.mapper.TravelRouteMapper;
import com.tourism.service.AdminRouteService;
import com.tourism.vo.RouteManageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理后台旅游路线管理服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入两个 Mapper：
 * {@link TravelRouteMapper} 负责路线主体（travel_route 表）的 CRUD，
 * {@link RouteSpotMapper} 负责路线与景点关联（route_spot 表）的 CRUD。
 * 路线管理的核心业务特点是：路线主体与景点关联必须保持数据一致性，
 * 因此 saveRoute、updateRoute、deleteRoute 三个写方法均声明了
 * {@link Transactional}（rollbackFor = Exception.class），
 * 确保在抛出任何 Exception 时 Spring 会自动回滚事务，避免数据库出现"有路线无景点"或"有景点无路线"的中间状态。
 * 事务传播行为：默认 REQUIRED（若当前无事务则新建，有则加入）。
 * 事务隔离级别：默认数据库默认级别（通常为 READ COMMITTED）。
 * </p>
 *
 * @author Tourism System
 * @see AdminRouteService
 */
@Service
public class AdminRouteServiceImpl implements AdminRouteService {

    /**
     * 旅游路线主体 Mapper，自动注入。负责 travel_route 表的增删改查。
     */
    @Autowired
    private TravelRouteMapper travelRouteMapper;

    /**
     * 路线景点关联 Mapper，自动注入。负责 route_spot 表的增删改查。
     */
    @Autowired
    private RouteSpotMapper routeSpotMapper;

    /**
     * 查询所有旅游路线列表。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code travelRouteMapper.selectAll()} 查询所有路线主体（SQL SELECT * FROM travel_route）；
     * 2. 遍历每条路线，创建 {@link RouteManageVO} 视图对象；
     * 3. 将路线主体设置到 VO 的 route 字段；
     * 4. 调用 {@code routeSpotMapper.selectByRouteId(route.getId())} 查询该路线关联的所有景点（SQL SELECT BY route_id，按 day_no, sort_no 排序）；
     * 5. 将景点关联列表设置到 VO 的 spots 字段；
     * 6. 将 VO 加入结果列表并返回。
     * 注意：此操作为只读，无需事务，N+1 查询在此场景下数据量可控，若路线数量大可考虑联表查询优化。
     * </p>
     *
     * @return 所有路线的管理视图对象 {@link RouteManageVO} 列表
     */
    @Override
    public List<RouteManageVO> listRoutes() {
        List<TravelRoute> routes = travelRouteMapper.selectAll(); // 步骤1：查询路线主体
        List<RouteManageVO> result = new ArrayList<>();
        for (TravelRoute route : routes) {
            RouteManageVO vo = new RouteManageVO(); // 步骤2：创建 VO
            vo.setRoute(route); // 步骤3：设置路线主体
            vo.setSpots(routeSpotMapper.selectByRouteId(route.getId())); // 步骤4：查询关联景点，按 day_no, sort_no 排序
            result.add(vo); // 步骤6：加入结果列表
        }
        return result;
    }

    /**
     * 新增旅游路线。
     * <p>
     * 业务逻辑步骤：
     * 1. 从请求对象中提取路线主体 {@link TravelRoute}；
     * 2. 调用 {@code travelRouteMapper.insert(route)} 插入路线主体（SQL INSERT），
     *    MyBatis 自动将数据库生成的主键回填到 route.getId()；
     * 3. 若请求中包含景点关联列表（request.getSpots() != null），遍历每个 {@link RouteSpot}：
     *    - 将 routeSpot.setRouteId(route.getId()) 设置为刚生成的路线 ID，建立外键关联；
     *    - 调用 {@code routeSpotMapper.insert(spot)} 插入关联记录（SQL INSERT INTO route_spot）；
     * 4. 调用 {@link #buildRouteManageVO(Long)} 构造包含完整信息的视图对象返回。
     * 事务边界：整个方法在 @Transactional 内执行，若步骤2成功但步骤3中某条景点关联插入失败，
     * 或后续 VO 构造抛出异常，则 Spring 会回滚整个事务，travel_route 和 route_spot 表都不会产生脏数据。
     * </p>
     *
     * @param request 路线管理请求对象，包含路线主体和景点关联列表
     * @return 保存后的 {@link RouteManageVO} 视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：路线与景点关联必须同时成功，Exception 触发回滚
    public RouteManageVO saveRoute(RouteManageRequest request) {
        TravelRoute route = request.getRoute(); // 步骤1：提取路线主体
        travelRouteMapper.insert(route); // 步骤2：插入路线，主键回填到 route.getId()
        if (request.getSpots() != null) { // 步骤3：处理景点关联
            for (RouteSpot spot : request.getSpots()) {
                spot.setRouteId(route.getId()); // 建立外键关联
                routeSpotMapper.insert(spot); // 插入路线-景点关联记录
            }
        }
        return buildRouteManageVO(route.getId()); // 步骤4：构造 VO 返回
    }

    /**
     * 更新旅游路线。
     * <p>
     * 业务逻辑步骤：
     * 1. 从请求对象中提取路线主体 {@link TravelRoute}；
     * 2. 校验路线 ID 是否有效，若 ID 为 null 或数据库中不存在，抛出 {@link BusinessException} "路线不存在"；
     * 3. 调用 {@code travelRouteMapper.updateById(route)} 更新路线主体（SQL UPDATE BY ID）；
     * 4. 调用 {@code routeSpotMapper.deleteByRouteId(route.getId())} 删除该路线原有的所有景点关联（SQL DELETE WHERE route_id = ?），
     *    原因：采用"先删后插"策略，简化景点顺序变更（增删改）的处理逻辑，避免逐条对比差异；
     * 5. 若请求中包含新的景点关联列表，遍历每个 {@link RouteSpot}：
     *    - 将 routeSpot.setRouteId(route.getId()) 设置为当前路线 ID；
     *    - 调用 {@code routeSpotMapper.insert(spot)} 插入新的关联记录；
     * 6. 调用 {@link #buildRouteManageVO(Long)} 构造包含最新信息的视图对象返回。
     * 事务边界：整个方法在 @Transactional 内执行，步骤3、4、5 要么全部成功，要么全部回滚，
     * 保证不会出现路线更新了但景点关联未更新（或部分更新）的不一致状态。
     * </p>
     *
     * @param request 路线管理请求对象，其中 route.id 必须有效
     * @return 更新后的 {@link RouteManageVO} 视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：保证路线主体与景点关联的一致性
    public RouteManageVO updateRoute(RouteManageRequest request) {
        TravelRoute route = request.getRoute(); // 步骤1：提取路线主体
        // 步骤2：存在性校验，防止更新不存在的路线
        if (route.getId() == null || travelRouteMapper.selectById(route.getId()) == null) {
            throw new BusinessException("路线不存在");
        }
        travelRouteMapper.updateById(route); // 步骤3：更新路线主体
        routeSpotMapper.deleteByRouteId(route.getId()); // 步骤4：删除旧关联，采用"先删后插"策略简化逻辑
        if (request.getSpots() != null) { // 步骤5：插入新关联
            for (RouteSpot spot : request.getSpots()) {
                spot.setRouteId(route.getId());
                routeSpotMapper.insert(spot);
            }
        }
        return buildRouteManageVO(route.getId()); // 步骤6：构造 VO 返回
    }

    /**
     * 删除旅游路线。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code routeSpotMapper.deleteByRouteId(routeId)} 先删除该路线关联的所有景点记录（SQL DELETE WHERE route_id = ?），
     *    原因：避免删除路线主体后，route_spot 表残留无效外键；数据库若配置了 ON DELETE CASCADE 可省略此步骤，
     *    但显式删除更可控，且兼容无外键级联的数据库；
     * 2. 调用 {@code travelRouteMapper.deleteById(routeId)} 删除路线主体（SQL DELETE BY ID）。
     * 事务边界：两个删除操作在同一事务中，要么同时成功，要么同时回滚，不会出现 route_spot 已删但 travel_route 未删的异常状态。
     * </p>
     *
     * @param routeId 旅游路线主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：级联删除必须同时成功
    public void deleteRoute(Long routeId) {
        // 步骤1：先删除关联景点，避免残留外键或触发数据库外键约束异常
        routeSpotMapper.deleteByRouteId(routeId);
        // 步骤2：删除路线主体
        travelRouteMapper.deleteById(routeId);
    }

    /**
     * 构建路线管理视图对象（RouteManageVO）。
     * <p>
     * 私有工具方法，供 saveRoute、updateRoute 复用，避免重复代码。
     * 步骤：
     * 1. 创建 {@link RouteManageVO} 对象；
     * 2. 调用 {@code travelRouteMapper.selectById(routeId)} 查询路线主体（SQL 主键查询）；
     * 3. 调用 {@code routeSpotMapper.selectByRouteId(routeId)} 查询关联景点列表（SQL SELECT BY route_id）；
     * 4. 组装 VO 并返回。
     * </p>
     *
     * @param routeId 路线主键 ID
     * @return 组装好的 {@link RouteManageVO} 视图对象
     */
    private RouteManageVO buildRouteManageVO(Long routeId) {
        RouteManageVO vo = new RouteManageVO();
        vo.setRoute(travelRouteMapper.selectById(routeId)); // 查询路线主体
        vo.setSpots(routeSpotMapper.selectByRouteId(routeId)); // 查询关联景点
        return vo;
    }
}
