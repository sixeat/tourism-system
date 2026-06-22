package com.tourism.mapper;

import com.tourism.entity.MapPoint;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地图点位数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（MapPointMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口提供地图点位的查询功能，包括查询全部点位和按类型筛选点位。
 */
public interface MapPointMapper {
    
    /**
     * 查询所有有效的地图点位
     * 
     * 对应 SQL：select ... from map_point where status = 1 order by sort_no asc, id asc
     * 仅查询 status = 1（启用状态）的点位，过滤掉已下线或禁用点位。
     * 结果按 sort_no 升序、id 升序排列，确保前端展示顺序可控。
     * 
     * @return 所有有效地图点位的列表
     */
    List<MapPoint> selectAll();

    /**
     * 根据点位类型查询地图点位
     * 
     * 对应 SQL：select ... from map_point where status = 1 and point_type = #{pointType}
     * 通过 @Param("pointType") 指定参数名，确保 XML 映射正确。
     * 仅查询 status = 1（启用状态）的点位，并按 sort_no 升序、id 升序排列。
     * 参数通过 #{pointType} 预编译绑定，防止 SQL 注入攻击。
     * 
     * @param pointType 点位类型（如 SCENIC, HOTEL, RESTAURANT 等）
     * @return 该类型下的有效地图点位列表
     */
    List<MapPoint> selectByType(@Param("pointType") String pointType);
}
