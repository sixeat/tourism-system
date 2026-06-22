package com.tourism.mapper;

import com.tourism.entity.Hotel;

import java.util.List;

/**
 * 酒店信息数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（HotelMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口提供酒店信息的完整 CRUD 操作，包括按城市查询、查询全部、按 ID 查询、
 * 新增、修改和删除酒店记录。
 */
public interface HotelMapper {
    
    /**
     * 根据城市名称查询酒店列表
     * 
     * 对应 SQL：select ... from hotel where city = #{city}
     * 通过 #{city} 预编译参数绑定，防止 SQL 注入攻击。
     * 结果按 id 升序排列，保证结果集的稳定性。
     * 
     * @param city 城市名称
     * @return 该城市下的酒店列表
     */
    List<Hotel> selectByCity(String city);

    /**
     * 查询所有酒店信息
     * 
     * 对应 SQL：select ... from hotel order by id asc
     * 查询 hotel 表全部记录，结果按 id 升序排列。
     * 
     * @return 所有酒店信息的列表
     */
    List<Hotel> selectAll();

    /**
     * 根据酒店 ID 查询单个酒店详情
     * 
     * 对应 SQL：select ... from hotel where id = #{id}
     * 通过 #{id} 预编译参数绑定，防止 SQL 注入攻击。
     * 
     * @param id 酒店主键 ID
     * @return 对应的 Hotel 实体对象，若不存在则返回 null
     */
    Hotel selectById(Long id);

    /**
     * 插入一条新的酒店记录
     * 
     * 对应 SQL：insert into hotel (...) values (...)
     * 使用 useGeneratedKeys 自动获取数据库生成的主键值，并回填到实体对象的 id 属性中。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param hotel 酒店实体对象，包含酒店名称、城市、地址、等级、描述、状态等字段
     * @return 受影响的行数，成功插入时通常为 1
     */
    int insert(Hotel hotel);

    /**
     * 根据酒店 ID 更新酒店信息
     * 
     * 对应 SQL：update hotel set ... where id = #{id}
     * 全字段更新，要求传入完整的 Hotel 实体对象。若字段为 null，数据库中对应列也会被更新为 null。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param hotel 包含更新后字段值的酒店实体对象
     * @return 受影响的行数，成功更新时通常为 1
     */
    int updateById(Hotel hotel);

    /**
     * 根据酒店 ID 删除酒店记录
     * 
     * 对应 SQL：delete from hotel where id = #{id}
     * 通过 #{id} 预编译参数绑定，防止 SQL 注入攻击。
     * 
     * @param id 待删除酒店的主键 ID
     * @return 受影响的行数，成功删除时通常为 1
     */
    int deleteById(Long id);
}
