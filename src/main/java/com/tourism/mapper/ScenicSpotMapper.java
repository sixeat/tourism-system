package com.tourism.mapper;

import com.tourism.entity.ScenicSpot;

import java.util.List;

/**
 * 景点信息数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（ScenicSpotMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口提供景点信息的完整 CRUD 操作，包括查询全部、按 ID 查询、新增、修改和删除景点记录。
 */
public interface ScenicSpotMapper {
    
    /**
     * 查询所有景点信息
     * 
     * 对应 SQL：select ... from scenic_spot
     * 查询 scenic_spot 表全部记录，无额外过滤条件。
     * 
     * @return 所有景点信息的列表
     */
    List<ScenicSpot> selectAll();

    /**
     * 根据景点 ID 查询单个景点详情
     * 
     * 对应 SQL：select ... from scenic_spot where id = #{id}
     * 通过 #{id} 预编译参数绑定，防止 SQL 注入攻击。
     * 
     * @param id 景点主键 ID
     * @return 对应的 ScenicSpot 实体对象，若不存在则返回 null
     */
    ScenicSpot selectById(Long id);

    /**
     * 插入一条新的景点记录
     * 
     * 对应 SQL：insert into scenic_spot (...) values (...)
     * 使用 useGeneratedKeys 自动获取数据库生成的主键值，并回填到实体对象的 id 属性中。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param scenicSpot 景点实体对象，包含景点名称、城市、类别、描述、价格、评分、人气、标签、经纬度等
     * @return 受影响的行数，成功插入时通常为 1
     */
    int insert(ScenicSpot scenicSpot);

    /**
     * 根据景点 ID 更新景点信息
     * 
     * 对应 SQL：update scenic_spot set ... where id = #{id}
     * 全字段更新，要求传入完整的 ScenicSpot 实体对象。若字段为 null，数据库中对应列也会被更新为 null。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param scenicSpot 包含更新后字段值的景点实体对象
     * @return 受影响的行数，成功更新时通常为 1
     */
    int updateById(ScenicSpot scenicSpot);

    /**
     * 根据景点 ID 删除景点记录
     * 
     * 对应 SQL：delete from scenic_spot where id = #{id}
     * 通过 #{id} 预编译参数绑定，防止 SQL 注入攻击。
     * 
     * @param id 待删除景点的主键 ID
     * @return 受影响的行数，成功删除时通常为 1
     */
    int deleteById(Long id);
}
