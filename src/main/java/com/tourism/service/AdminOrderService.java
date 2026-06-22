package com.tourism.service;

import com.tourism.vo.AdminOrderVO;

import java.util.List;

/**
 * 管理后台订单管理服务接口（Service Contract）。
 * <p>
 * 本接口定义了管理后台对全量订单的查询与状态管理操作，涵盖酒店订单和门票订单两类业务。
 * 管理员可通过此服务查看所有用户订单、更新订单状态（如确认、完成、取消等）。
 * 实现类：{@link com.tourism.service.impl.AdminOrderServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface AdminOrderService {

    /**
     * 查询系统中所有订单列表（包括酒店订单和门票订单）。
     * <p>
     * 将酒店订单和门票订单统一查询后合并，并按创建时间降序排列，
     * 使管理员可在后台一览全量订单，方便客服和运营处理。
     * </p>
     *
     * @return 所有订单的视图对象列表，元素类型为 {@link AdminOrderVO}
     */
    List<AdminOrderVO> listAllOrders();

    /**
     * 更新酒店订单状态。
     * <p>
     * 管理员根据业务需要修改酒店订单的状态（如将 CREATED 改为 CONFIRMED 或 CANCELLED）。
     * 若订单不存在，则应在实现层抛出 {@link com.tourism.common.BusinessException}。
     * </p>
     *
     * @param id          酒店订单主键 ID，不可为 null
     * @param orderStatus 目标订单状态字符串，如 "CONFIRMED"、"CANCELLED" 等
     */
    void updateHotelOrderStatus(Long id, String orderStatus);

    /**
     * 更新门票订单状态。
     * <p>
     * 管理员根据业务需要修改门票订单的状态（如将 CREATED 改为 PAID 或 CANCELLED）。
     * 若订单不存在，则应在实现层抛出 {@link com.tourism.common.BusinessException}。
     * </p>
     *
     * @param id          门票订单主键 ID，不可为 null
     * @param orderStatus 目标订单状态字符串，如 "PAID"、"CANCELLED" 等
     */
    void updateTicketOrderStatus(Long id, String orderStatus);
}
