package com.tourism.mapper;

import java.math.BigDecimal;

/**
 * 后台管理仪表盘数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（AdminDashboardMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口提供系统运营数据的统计查询，包括用户量、景点数、酒店数、门票数、
 * 路线数、订单数以及有效营收总额等核心指标。
 */
public interface AdminDashboardMapper {
    
    /**
     * 统计系统注册用户总数
     * 
     * 对应 SQL：select count(*) from sys_user
     * 查询 sys_user 表中的所有记录数，返回 int 类型的用户总量
     * 
     * @return 注册用户总数
     */
    int countUsers();

    /**
     * 统计景点总数
     * 
     * 对应 SQL：select count(*) from scenic_spot
     * 查询 scenic_spot 表中的所有记录数，返回 int 类型的景点总量
     * 
     * @return 景点总数
     */
    int countScenicSpots();

    /**
     * 统计酒店总数
     * 
     * 对应 SQL：select count(*) from hotel
     * 查询 hotel 表中的所有记录数，返回 int 类型的酒店总量
     * 
     * @return 酒店总数
     */
    int countHotels();

    /**
     * 统计门票总数
     * 
     * 对应 SQL：select count(*) from ticket
     * 查询 ticket 表中的所有记录数，返回 int 类型的门票总量
     * 
     * @return 门票总数
     */
    int countTickets();

    /**
     * 统计旅游路线总数
     * 
     * 对应 SQL：select count(*) from travel_route
     * 查询 travel_route 表中的所有记录数，返回 int 类型的路线总量
     * 
     * @return 旅游路线总数
     */
    int countRoutes();

    /**
     * 统计酒店订单总数
     * 
     * 对应 SQL：select count(*) from hotel_order
     * 查询 hotel_order 表中的所有记录数，返回 int 类型的酒店订单总量
     * 
     * @return 酒店订单总数
     */
    int countHotelOrders();

    /**
     * 统计门票订单总数
     * 
     * 对应 SQL：select count(*) from ticket_order
     * 查询 ticket_order 表中的所有记录数，返回 int 类型的门票订单总量
     * 
     * @return 门票订单总数
     */
    int countTicketOrders();

    /**
     * 计算有效订单营收总额
     * 
     * 对应 SQL：联合查询 hotel_order 和 ticket_order 两张表，排除状态为 CANCELLED 的订单，
     * 对 total_amount 字段求和。返回 BigDecimal 类型以支持高精度金额计算。
     * 使用 coalesce 函数确保无数据时返回 0 而非 null。
     * 
     * @return 有效订单的营收总额（非取消状态的酒店订单 + 门票订单）
     */
    BigDecimal sumValidRevenue();
}
