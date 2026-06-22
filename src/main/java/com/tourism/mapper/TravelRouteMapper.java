package com.tourism.mapper;

import com.tourism.entity.TravelRoute;

import java.util.List;

/**
 * 旅游路线数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（TravelRouteMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口提供旅游路线的完整 CRUD 操作，包括查询全部、按 ID 查询、新增、修改和删除路线记录。
 */
public interface TravelRouteMapper {
    
    /**
     * 查询所有旅游路线
     * 
     * 对应 SQL：select ... from travel_route order by id desc
     * 查询 travel_route 表全部记录，结果按 id 降序排列，最新添加的路线展示在最前面。
     * 
     * @return 所有旅游路线的列表
     */
    List<TravelRoute> selectAll();

    /**
     * 根据路线 ID 查询单个旅游路线详情
     * 
     * 对应 SQL：select ... from travel_route where id = #{id}
     * 通过 #{id} 预编译参数绑定，防止 SQL 注入攻击。
     * 
     * @param id 旅游路线主键 ID
     * @return 对应的 TravelRoute 实体对象，若不存在则返回 null
     */
    TravelRoute selectById(Long id);

    /**
     * 插入一条新的旅游路线
     * 
     * 对应 SQL：insert into travel_route (...) values (...)
     * 使用 useGeneratedKeys 自动获取数据库生成的主键值，并回填到实体对象的 id 属性中。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param travelRoute 旅游路线实体对象，包含路线名称、城市、天数、预算、主题、路线描述等
     * @return 受影响的行数，成功插入时通常为 1
     */
    int insert(TravelRoute travelRoute);

    /**
     * 根据路线 ID 更新旅游路线信息
     * 
     * 对应 SQL：update travel_route set ... where id = #{id}
     * 全字段更新，要求传入完整的 TravelRoute 实体对象。若字段为 null，数据库中对应列也会被更新为 null。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param travelRoute 包含更新后字段值的旅游路线实体对象
     * @return 受影响的行数，成功更新时通常为 1
     */
    int updateById(TravelRoute travelRoute);

    /**
     * 根据路线 ID 删除旅游路线
     * 
     * 对应 SQL：delete from travel_route where id = #{id}
     * 通过 #{id} 预编译参数绑定，防止 SQL 注入攻击。
     * 
     * @param id 待删除旅游路线的主键 ID
     * @return 受影响的行数，成功删除时通常为 1
     */
    int deleteById(Long id);
}
