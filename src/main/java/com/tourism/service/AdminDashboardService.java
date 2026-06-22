package com.tourism.service;

import java.util.Map;

/**
 * 管理后台仪表盘服务接口（Service Contract）。
 * <p>
 * 本接口定义了管理后台仪表盘的数据统计契约，采用 Spring Service 接口隔离模式，
 * 使 Controller 面向接口编程，便于实现替换与单元测试。
 * 实现类：{@link com.tourism.service.impl.AdminDashboardServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface AdminDashboardService {

    /**
     * 获取管理后台仪表盘汇总数据。
     * <p>
     * 统计范围包括：用户总数、景点总数、酒店总数、门票总数、旅游路线总数、
     * 酒店订单数量、门票订单数量、订单总数量以及系统总营收金额。
     * 返回的 Map 中 key 为统计维度标识（如 "userCount"），value 为对应统计值。
     * </p>
     *
     * @return 包含各类统计数据的 {@link Map}，key 为字符串维度标识，value 为统计数值（Integer / BigDecimal）
     */
    Map<String, Object> summary();
}
