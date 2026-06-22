package com.tourism.mapper;

import com.tourism.vo.UserOrderVO;

import java.util.List;

/**
 * 用户订单查询数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（OrderQueryMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口为用户中心提供统一的订单查询视图，将酒店订单和门票订单合并为 UserOrderVO 列表，
 * 支持用户查看自己的历史订单记录。
 */
public interface OrderQueryMapper {
    
    /**
     * 根据用户 ID 查询该用户的酒店订单列表
     * 
     * 对应 SQL：通过 LEFT JOIN 关联 hotel 表，将 hotel_order 中的订单信息
     * 封装为 UserOrderVO。映射字段包括订单类型（固定为 'HOTEL'）、酒店名称、金额、
     * 状态、入住/退房日期、创建时间等。
     * 结果按 create_time 降序排列，最新订单展示在最前面。
     * 参数通过 #{userId} 预编译绑定，防止 SQL 注入攻击。
     * 
     * @param userId 用户主键 ID
     * @return 该用户的酒店订单 UserOrderVO 列表
     */
    List<UserOrderVO> selectHotelOrdersByUserId(Long userId);

    /**
     * 根据用户 ID 查询该用户的门票订单列表
     * 
     * 对应 SQL：通过 LEFT JOIN 关联 ticket 表，将 ticket_order 中的订单信息
     * 封装为 UserOrderVO。映射字段包括订单类型（固定为 'TICKET'）、门票名称、金额、
     * 状态、游玩日期、数量、创建时间等。
     * 结果按 create_time 降序排列，最新订单展示在最前面。
     * 参数通过 #{userId} 预编译绑定，防止 SQL 注入攻击。
     * 
     * @param userId 用户主键 ID
     * @return 该用户的门票订单 UserOrderVO 列表
     */
    List<UserOrderVO> selectTicketOrdersByUserId(Long userId);
}
