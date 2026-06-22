package com.tourism.mapper;

import com.tourism.entity.OrderReview;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单评价数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（OrderReviewMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口提供订单评价的查询和新增功能，支持用户查看自己的评价记录以及按用户、订单类型、订单 ID 查询单条评价。
 */
public interface OrderReviewMapper {
    
    /**
     * 根据用户 ID 查询该用户的所有订单评价
     * 
     * 对应 SQL：select ... from order_review where user_id = #{userId}
     * 结果按 create_time 降序、id 降序排列，确保最新评价展示在最前面。
     * 参数通过 #{userId} 预编译绑定，防止 SQL 注入攻击。
     * 
     * @param userId 用户主键 ID
     * @return 该用户的订单评价列表
     */
    List<OrderReview> selectByUserId(Long userId);

    /**
     * 根据用户 ID、订单类型和订单 ID 查询单条评价记录
     * 
     * 对应 SQL：select ... from order_review where user_id = #{userId} 
     *          and order_type = #{orderType} and order_id = #{orderId} limit 1
     * 通过 @Param 注解明确参数名，确保 XML 中的 #{userId}、#{orderType}、#{orderId} 正确映射。
     * 使用 limit 1 限制返回一条记录，提高查询效率。
     * 参数通过 #{param} 预编译绑定，防止 SQL 注入攻击。
     * 
     * @param userId 用户主键 ID
     * @param orderType 订单类型（如 HOTEL, TICKET）
     * @param orderId 订单主键 ID
     * @return 匹配的评价记录，若不存在则返回 null
     */
    OrderReview selectOne(@Param("userId") Long userId,
                          @Param("orderType") String orderType,
                          @Param("orderId") Long orderId);

    /**
     * 插入一条新的订单评价
     * 
     * 对应 SQL：insert into order_review (...) values (...)
     * 使用 useGeneratedKeys 自动获取数据库生成的主键值，并回填到实体对象的 id 属性中。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param review 订单评价实体对象，包含用户ID、订单类型、订单ID、评分、内容等
     * @return 受影响的行数，成功插入时通常为 1
     */
    int insert(OrderReview review);
}
