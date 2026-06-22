package com.tourism.service.impl;

import com.tourism.common.BusinessException;
import com.tourism.mapper.AdminOrderMapper;
import com.tourism.mapper.HotelOrderMapper;
import com.tourism.mapper.TicketOrderMapper;
import com.tourism.service.AdminOrderService;
import com.tourism.vo.AdminOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 管理后台订单管理服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入三个 Mapper：
 * {@link AdminOrderMapper} 用于联表查询订单视图，{@link HotelOrderMapper} 和 {@link TicketOrderMapper}
 * 用于单表状态更新操作。
 * 职责：为管理员提供全量订单的合并查询与订单状态更新能力。
 * 状态更新前需校验订单存在性，防止因误操作或并发删除导致更新空记录。
 * 本类不涉及跨多个表的事务写操作，因此未显式声明 {@link org.springframework.transaction.annotation.Transactional}，
 * 但单条 updateStatus 操作由 Mapper 的 SQL 保证原子性。
 * </p>
 *
 * @author Tourism System
 * @see AdminOrderService
 */
@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    /**
     * 管理后台订单视图查询 Mapper，自动注入。
     * <p>负责执行联表查询，将酒店订单和门票订单统一转换为 AdminOrderVO 视图。</p>
     */
    @Autowired
    private AdminOrderMapper adminOrderMapper;

    /**
     * 酒店订单 Mapper，自动注入。
     * <p>负责酒店订单的单表查询和状态更新。</p>
     */
    @Autowired
    private HotelOrderMapper hotelOrderMapper;

    /**
     * 门票订单 Mapper，自动注入。
     * <p>负责门票订单的单表查询和状态更新。</p>
     */
    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    /**
     * 查询系统中所有订单列表。
     * <p>
     * 业务逻辑步骤：
     * 1. 创建结果列表 {@link ArrayList}，用于合并两类订单；
     * 2. 调用 {@code adminOrderMapper.selectHotelOrders()} 查询所有酒店订单视图（SQL 联表 hotel_order + hotel + hotel_room）；
     * 3. 调用 {@code adminOrderMapper.selectTicketOrders()} 查询所有门票订单视图（SQL 联表 ticket_order + ticket + scenic_spot）；
     * 4. 将两类订单合并到同一列表；
     * 5. 使用 {@link Comparator#comparing} 按 createTime 降序排列，null 值放在最后（{@link Comparator#nullsLast}），
     *    确保最新订单展示在最前面，方便管理员优先处理新订单；
     * 6. 返回排序后的订单列表。
     * </p>
     *
     * @return 所有订单的视图对象 {@link AdminOrderVO} 列表
     */
    @Override
    public List<AdminOrderVO> listAllOrders() {
        List<AdminOrderVO> results = new ArrayList<>();
        // 步骤2：查询酒店订单视图，Mapper 执行联表 SQL，返回 AdminOrderVO 列表
        results.addAll(adminOrderMapper.selectHotelOrders());
        // 步骤3：查询门票订单视图，Mapper 执行联表 SQL，返回 AdminOrderVO 列表
        results.addAll(adminOrderMapper.selectTicketOrders());
        // 步骤5：按创建时间降序排列，null 排最后，确保最新订单在前
        results.sort(Comparator.comparing(AdminOrderVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return results;
    }

    /**
     * 更新酒店订单状态。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code hotelOrderMapper.selectById(id)} 查询酒店订单（SQL 主键查询），
     *    校验订单是否存在；若不存在，抛出 {@link BusinessException}，提示 "酒店订单不存在"，防止管理员误操作无效订单；
     * 2. 调用 {@code hotelOrderMapper.updateStatus(id, orderStatus)} 更新订单状态（SQL UPDATE 单条记录）。
     * 注意：此处不做状态流转校验（如 CREATED→CONFIRMED→PAID），由业务层或前端控制，
     * 管理员后台允许灵活调整状态。
     * </p>
     *
     * @param id          酒店订单主键 ID
     * @param orderStatus 目标订单状态字符串
     */
    @Override
    public void updateHotelOrderStatus(Long id, String orderStatus) {
        // 步骤1：存在性校验，防止更新不存在的记录
        if (hotelOrderMapper.selectById(id) == null) {
            throw new BusinessException("酒店订单不存在");
        }
        // 步骤2：执行状态更新，Mapper 执行 UPDATE hotel_order SET order_status = ? WHERE id = ?
        hotelOrderMapper.updateStatus(id, orderStatus);
    }

    /**
     * 更新门票订单状态。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code ticketOrderMapper.selectById(id)} 查询门票订单（SQL 主键查询），
     *    校验订单是否存在；若不存在，抛出 {@link BusinessException}，提示 "门票订单不存在"；
     * 2. 调用 {@code ticketOrderMapper.updateStatus(id, orderStatus)} 更新订单状态（SQL UPDATE 单条记录）。
     * 注意：与酒店订单状态更新同理，不做状态流转强校验，由管理员灵活操作。
     * </p>
     *
     * @param id          门票订单主键 ID
     * @param orderStatus 目标订单状态字符串
     */
    @Override
    public void updateTicketOrderStatus(Long id, String orderStatus) {
        // 步骤1：存在性校验，防止更新不存在的记录
        if (ticketOrderMapper.selectById(id) == null) {
            throw new BusinessException("门票订单不存在");
        }
        // 步骤2：执行状态更新，Mapper 执行 UPDATE ticket_order SET order_status = ? WHERE id = ?
        ticketOrderMapper.updateStatus(id, orderStatus);
    }
}
