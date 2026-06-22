package com.tourism.mapper;

import com.tourism.entity.RouteSpot;

import java.util.List;

/**
 * 旅游路线与景点关联数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（RouteSpotMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口管理旅游路线（travel_route）与景点（scenic_spot）之间的多对多关联关系，
 * 提供按路线查询关联景点、新增关联记录以及按路线删除关联记录等功能。
 */
public interface RouteSpotMapper {
    
    /**
     * 根据旅游路线 ID 查询该路线包含的所有景点关联信息
     * 
     * 对应 SQL：select ... from route_spot where route_id = #{routeId}
     * 结果按 day_no 升序、sort_no 升序排列，确保按游玩天数和每日游玩顺序展示。
     * 参数通过 #{routeId} 预编译绑定，防止 SQL 注入攻击。
     * 
     * @param routeId 旅游路线主键 ID
     * @return 该路线关联的景点列表，包含 day_no（第几天）和 sort_no（当天顺序）等字段
     */
    List<RouteSpot> selectByRouteId(Long routeId);

    /**
     * 插入一条路线与景点的关联记录
     * 
     * 对应 SQL：insert into route_spot (...) values (...)
     * 使用 useGeneratedKeys 自动获取数据库生成的主键值，并回填到实体对象的 id 属性中。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param routeSpot 路线景点关联实体对象，包含 route_id、scenic_id、day_no、sort_no 等
     * @return 受影响的行数，成功插入时通常为 1
     */
    int insert(RouteSpot routeSpot);

    /**
     * 根据旅游路线 ID 删除该路线下的所有景点关联记录
     * 
     * 对应 SQL：delete from route_spot where route_id = #{routeId}
     * 通常在更新路线或删除路线时调用，先清除旧关联再重新建立新关联。
     * 参数通过 #{routeId} 预编译绑定，防止 SQL 注入攻击。
     * 
     * @param routeId 待删除关联的路线主键 ID
     * @return 受影响的行数
     */
    int deleteByRouteId(Long routeId);
}
