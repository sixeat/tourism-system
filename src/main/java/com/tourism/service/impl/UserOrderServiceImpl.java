package com.tourism.service.impl;

import com.tourism.mapper.OrderQueryMapper;
import com.tourism.service.UserOrderService;
import com.tourism.vo.UserOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 用户订单查询服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入 {@link OrderQueryMapper}。
 * 职责：为 C 端用户（前台）"我的订单" 页面提供统一的订单查询服务。
 * 将酒店订单和门票订单从各自的表中查询出来，合并为统一的 {@link UserOrderVO} 视图列表，并按时间排序。
 * 方法为只读查询，不涉及数据库写操作，因此不声明 {@link org.springframework.transaction.annotation.Transactional}。
 * 虽然同时读取两个表，但不存在跨表数据一致性要求（两个查询独立），因此无需事务控制。
 * </p>
 *
 * @author Tourism System
 * @see UserOrderService
 */
@Service
public class UserOrderServiceImpl implements UserOrderService {

    /**
     * 订单查询数据访问 Mapper，自动注入。
     * <p>负责跨表查询酒店订单和门票订单的统一视图，将不同表的数据转换为 UserOrderVO。</p>
     */
    @Autowired
    private OrderQueryMapper orderQueryMapper;

    /**
     * 查询指定用户的全部订单列表。
     * <p>
     * 业务逻辑步骤：
     * 1. 创建结果列表 {@link ArrayList}，用于合并两类订单；
     * 2. 调用 {@code orderQueryMapper.selectHotelOrdersByUserId(userId)} 查询该用户的酒店订单视图
     *    （SQL 联表查询 hotel_order + hotel + hotel_room，转换为 UserOrderVO）；
     * 3. 调用 {@code orderQueryMapper.selectTicketOrdersByUserId(userId)} 查询该用户的门票订单视图
     *    （SQL 联表查询 ticket_order + ticket + scenic_spot，转换为 UserOrderVO）；
     * 4. 将两类订单合并到同一列表；
     * 5. 使用 {@link Comparator#comparing} 按 createTime 降序排列，null 值放在最后（{@link Comparator#nullsLast}），
     *    确保最新订单展示在最前面，符合用户"我的订单"页面的阅读习惯；
     * 6. 返回排序后的统一订单列表。
     * 注意：虽然酒店订单和门票订单查询是两次独立的 SQL，但无事务要求，因为两次查询之间没有数据依赖，
     * 即使中间有新订单插入，也不会影响查询结果的正确性（最终一致性可接受）。
     * </p>
     *
     * @param userId 用户主键 ID
     * @return 该用户的统一订单视图对象 {@link UserOrderVO} 列表
     */
    @Override
    public List<UserOrderVO> listUserOrders(Long userId) {
        List<UserOrderVO> results = new ArrayList<>();
        // 步骤2：查询酒店订单视图，Mapper 执行联表 SQL，返回 UserOrderVO 列表
        results.addAll(orderQueryMapper.selectHotelOrdersByUserId(userId));
        // 步骤3：查询门票订单视图，Mapper 执行联表 SQL，返回 UserOrderVO 列表
        results.addAll(orderQueryMapper.selectTicketOrdersByUserId(userId));
        // 步骤5：按创建时间降序排列，null 排最后，最新订单在前
        results.sort(Comparator.comparing(UserOrderVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return results;
    }
}
