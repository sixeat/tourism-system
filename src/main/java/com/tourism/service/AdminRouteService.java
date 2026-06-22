package com.tourism.service;

import com.tourism.dto.RouteManageRequest;
import com.tourism.vo.RouteManageVO;

import java.util.List;

/**
 * 管理后台旅游路线管理服务接口（Service Contract）。
 * <p>
 * 本接口定义了管理后台对旅游路线的全生命周期管理操作，包括路线查询、新增、修改和删除。
 * 路线管理涉及 TravelRoute（路线主体）和 RouteSpot（路线关联的景点）两张表，
 * 实现类需保证事务一致性，确保路线与其景点关联同时成功或同时回滚。
 * 实现类：{@link com.tourism.service.impl.AdminRouteServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface AdminRouteService {

    /**
     * 查询所有旅游路线列表。
     * <p>
     * 返回系统中所有旅游路线，每条路线包含路线主体信息及其关联的景点列表（按天/排序号排列）。
     * 用于后台路线管理页面展示。
     * </p>
     *
     * @return 所有路线的管理视图对象列表，元素类型为 {@link RouteManageVO}
     */
    List<RouteManageVO> listRoutes();

    /**
     * 新增旅游路线。
     * <p>
     * 接收包含路线主体和景点列表的请求对象，先保存路线主体，再批量保存路线与景点的关联记录。
     * 若景点列表不为空，则每个 RouteSpot 需关联到刚生成的路线 ID。
     * 返回包含完整信息（含关联景点）的管理视图对象。
     * </p>
     *
     * @param request 路线管理请求对象，包含 {@link com.tourism.entity.TravelRoute} 和景点关联列表
     * @return 保存后的 {@link RouteManageVO} 视图对象
     */
    RouteManageVO saveRoute(RouteManageRequest request);

    /**
     * 更新旅游路线。
     * <p>
     * 接收包含路线主体和景点列表的请求对象，先校验路线是否存在，
     * 然后更新路线主体，再删除旧的景点关联，最后重新插入新的景点关联。
     * 实现类需保证以上操作在同一事务内完成，避免数据不一致。
     * 返回包含完整信息（含关联景点）的管理视图对象。
     * </p>
     *
     * @param request 路线管理请求对象，其中 route.id 必须有效
     * @return 更新后的 {@link RouteManageVO} 视图对象
     */
    RouteManageVO updateRoute(RouteManageRequest request);

    /**
     * 删除旅游路线。
     * <p>
     * 根据路线主键 ID 删除该路线，同时级联删除该路线关联的所有 RouteSpot 记录。
     * 实现类需保证删除路线主体和删除关联景点在同一事务内完成。
     * </p>
     *
     * @param routeId 旅游路线主键 ID，不可为 null
     */
    void deleteRoute(Long routeId);
}
