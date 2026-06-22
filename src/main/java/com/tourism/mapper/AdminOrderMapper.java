package com.tourism.mapper;

import com.tourism.vo.AdminOrderVO;

import java.util.List;

/**
 * 后台管理订单查询数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（AdminOrderMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口为管理员提供统一的订单视图，将酒店订单和门票订单合并为 AdminOrderVO 列表，
 * 支持后台订单列表展示、订单管理等功能。
 */
public interface AdminOrderMapper {
    
    /**
     * 查询所有酒店订单（管理员视图）
     * 
     * 对应 SQL：通过 LEFT JOIN 关联 sys_user 和 hotel 表，将 hotel_order 中的订单信息
     * 封装为 AdminOrderVO。字段映射包括用户名、酒店名称、订单金额、状态等。
     * 结果按 create_time 降序排列，确保最新订单展示在最前面。
     * 
     * @return 酒店订单的 AdminOrderVO 列表，包含用户信息及酒店信息
     */
    List<AdminOrderVO> selectHotelOrders();

    /**
     * 查询所有门票订单（管理员视图）
     * 
     * 对应 SQL：通过 LEFT JOIN 关联 sys_user 和 ticket 表，将 ticket_order 中的订单信息
     * 封装为 AdminOrderVO。字段映射包括用户名、门票名称、订单金额、状态等。
     * 结果按 create_time 降序排列，确保最新订单展示在最前面。
     * 
     * @return 门票订单的 AdminOrderVO 列表，包含用户信息及门票信息
     */
    List<AdminOrderVO> selectTicketOrders();
}
