package com.tourism.service.impl;

import com.tourism.mapper.AdminDashboardMapper;
import com.tourism.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理后台仪表盘服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，作为 Spring 容器管理的 Bean，
 * 通过 {@link org.springframework.beans.factory.annotation.Autowired} 自动注入依赖的 Mapper，实现控制反转（IoC）。
 * 职责：聚合 AdminDashboardMapper 提供的各类统计 SQL 查询结果，封装为统一的 Map 数据结构返回给 Controller。
 * 本类方法均为只读查询，不涉及事务写操作，因此无需显式声明 {@link org.springframework.transaction.annotation.Transactional}。
 * </p>
 *
 * @author Tourism System
 * @see AdminDashboardService
 */
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    /**
     * 仪表盘数据访问 Mapper，自动注入。
     * <p>
     * 负责执行各类统计 SQL：用户数、景点数、酒店数、门票数、路线数、订单数、营收总额等。
     * </p>
     */
    @Autowired
    private AdminDashboardMapper adminDashboardMapper;

    /**
     * 获取管理后台仪表盘汇总数据。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code adminDashboardMapper.countHotelOrders()} 查询酒店订单总数；
     * 2. 调用 {@code adminDashboardMapper.countTicketOrders()} 查询门票订单总数；
     * 3. 调用 {@code adminDashboardMapper.sumValidRevenue()} 查询有效营收总额（通常排除已取消订单），注意处理 null（首次部署无订单时返回 null）；
     * 4. 使用 {@link LinkedHashMap} 保持 key 的插入顺序，便于前端按固定顺序展示；
     * 5. 依次放入各类统计指标：用户、景点、酒店、门票、路线、酒店订单、门票订单、订单合计、总营收；
     * 6. 营收字段做 null 安全处理：若 sumValidRevenue 为 null 则返回 {@link BigDecimal#ZERO}，避免前端空指针。
     * </p>
     *
     * @return 包含各类统计数据的 {@link Map}，key 为维度标识，value 为统计数值
     */
    @Override
    public Map<String, Object> summary() {
        // 步骤1：统计酒店订单数量（Mapper 执行 SELECT COUNT(*)  FROM hotel_order）
        int hotelOrderCount = adminDashboardMapper.countHotelOrders();
        // 步骤2：统计门票订单数量（Mapper 执行 SELECT COUNT(*)  FROM ticket_order）
        int ticketOrderCount = adminDashboardMapper.countTicketOrders();
        // 步骤3：统计有效营收总额（Mapper 执行 SUM(total_amount)  WHERE order_status IN ('PAID','FINISHED')），
        // 首次部署无订单时返回 null，需做空判断
        BigDecimal totalRevenue = adminDashboardMapper.sumValidRevenue();

        // 步骤4：使用 LinkedHashMap 保持插入顺序，前端仪表盘可按固定顺序渲染
        Map<String, Object> data = new LinkedHashMap<>();
        // 步骤5：依次放入各统计指标，key 命名遵循前端约定
        data.put("userCount", adminDashboardMapper.countUsers());       // Mapper 执行 SELECT COUNT(*) FROM user
        data.put("scenicCount", adminDashboardMapper.countScenicSpots()); // Mapper 执行 SELECT COUNT(*) FROM scenic_spot
        data.put("hotelCount", adminDashboardMapper.countHotels());       // Mapper 执行 SELECT COUNT(*) FROM hotel
        data.put("ticketCount", adminDashboardMapper.countTickets());     // Mapper 执行 SELECT COUNT(*) FROM ticket
        data.put("routeCount", adminDashboardMapper.countRoutes());       // Mapper 执行 SELECT COUNT(*) FROM travel_route
        data.put("hotelOrderCount", hotelOrderCount);                     // 酒店订单数
        data.put("ticketOrderCount", ticketOrderCount);                   // 门票订单数
        // 订单合计 = 酒店订单 + 门票订单，用于前端展示总订单量
        data.put("orderCount", hotelOrderCount + ticketOrderCount);
        // 总营收做空安全处理，避免 null 导致前端 JSON 序列化异常
        data.put("totalRevenue", totalRevenue == null ? BigDecimal.ZERO : totalRevenue);
        return data;
    }
}
