package com.tourism.mapper;

import com.tourism.entity.UserFavorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户收藏数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（UserFavoriteMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口提供用户收藏功能的查询、新增和删除操作，支持收藏景点、酒店、门票等不同类型的目标。
 */
public interface UserFavoriteMapper {
    
    /**
     * 根据用户 ID 查询该用户的所有收藏记录
     * 
     * 对应 SQL：select ... from user_favorite where user_id = #{userId}
     * 结果按 create_time 降序、id 降序排列，最新收藏的条目展示在最前面。
     * 参数通过 #{userId} 预编译绑定，防止 SQL 注入攻击。
     * 
     * @param userId 用户主键 ID
     * @return 该用户的收藏列表，包含收藏类型、目标ID、标题、描述等
     */
    List<UserFavorite> selectByUserId(Long userId);

    /**
     * 根据用户 ID、收藏类型和目标 ID 查询单条收藏记录
     * 
     * 对应 SQL：select ... from user_favorite where user_id = #{userId} 
     *          and favorite_type = #{favoriteType} and target_id = #{targetId} limit 1
     * 通过 @Param 注解明确参数名，确保 XML 中的参数映射正确。
     * 使用 limit 1 限制返回一条记录，提高查询效率。
     * 参数通过 #{param} 预编译绑定，防止 SQL 注入攻击。
     * 
     * @param userId 用户主键 ID
     * @param favoriteType 收藏类型（如 SCENIC, HOTEL, TICKET）
     * @param targetId 目标对象 ID
     * @return 匹配的收藏记录，若不存在则返回 null
     */
    UserFavorite selectOne(@Param("userId") Long userId,
                           @Param("favoriteType") String favoriteType,
                           @Param("targetId") String targetId);

    /**
     * 插入一条新的收藏记录
     * 
     * 对应 SQL：insert into user_favorite (...) values (...)
     * 使用 useGeneratedKeys 自动获取数据库生成的主键值，并回填到实体对象的 id 属性中。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param favorite 用户收藏实体对象，包含用户ID、收藏类型、目标ID、标题、描述等
     * @return 受影响的行数，成功插入时通常为 1
     */
    int insert(UserFavorite favorite);

    /**
     * 根据用户 ID、收藏类型和目标 ID 删除单条收藏记录
     * 
     * 对应 SQL：delete from user_favorite where user_id = #{userId} 
     *          and favorite_type = #{favoriteType} and target_id = #{targetId}
     * 通过 @Param 注解明确参数名，确保 XML 中的参数映射正确。
     * 参数通过 #{param} 预编译绑定，防止 SQL 注入攻击。
     * 
     * @param userId 用户主键 ID
     * @param favoriteType 收藏类型（如 SCENIC, HOTEL, TICKET）
     * @param targetId 目标对象 ID
     * @return 受影响的行数，成功删除时通常为 1
     */
    int deleteOne(@Param("userId") Long userId,
                  @Param("favoriteType") String favoriteType,
                  @Param("targetId") String targetId);
}
