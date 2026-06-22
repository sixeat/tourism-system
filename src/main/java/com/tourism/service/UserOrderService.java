package com.tourism.service;

import com.tourism.vo.UserOrderVO;

import java.util.List;

/**
 * 用户订单查询服务接口（Service Contract）。
 * <p>
 * 本接口定义了 C 端用户查询自己订单列表的服务契约，面向前台 "我的订单" 页面。
 * 将用户的酒店订单和门票订单统一查询、合并并按创建时间降序排列，提供统一视图。
 * 实现类：{@link com.tourism.service.impl.UserOrderServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface UserOrderService {

    /**
     * 查询指定用户的全部订单列表。
     * <p>
     * 业务逻辑：
     * 1. 通过 OrderQueryMapper 分别查询该用户的酒店订单和门票订单；
     * 2. 将两类订单合并到同一列表中；
     * 3. 按创建时间降序排列（null 值排在最后），使最新订单显示在最前面。
     * 返回的视图对象包含订单类型、订单号、商品名称、金额、状态、创建时间等统一字段。
     * </p>
     *
     * @param userId 用户主键 ID，不可为 null
     * @return 该用户的订单视图对象列表，元素类型为 {@link UserOrderVO}
     */
    List<UserOrderVO> listUserOrders(Long userId);
}
