package com.tourism.service.impl;

import com.tourism.common.BusinessException;
import com.tourism.dto.OrderReviewRequest;
import com.tourism.entity.HotelOrder;
import com.tourism.entity.OrderReview;
import com.tourism.entity.TicketOrder;
import com.tourism.mapper.HotelOrderMapper;
import com.tourism.mapper.OrderReviewMapper;
import com.tourism.mapper.TicketOrderMapper;
import com.tourism.service.OrderReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 订单评价服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入三个 Mapper：
 * {@link OrderReviewMapper} 用于评价记录的增删查，{@link HotelOrderMapper} 和 {@link TicketOrderMapper}
 * 用于校验订单状态（只有支付完成的订单才能评价）。
 * 职责：为 C 端用户提供评价查询和提交功能。
 * 提交评价时涉及多步校验：参数完整性、订单类型合法性、评分范围、重复评价、订单所有权、订单支付状态。
 * 若校验均通过，则将评价记录插入数据库。本类为单表插入，不涉及跨表事务，但包含先读后写逻辑，
 * 在高并发场景下可能存在重复评价竞态，当前实现通过数据库唯一索引（user_id + order_type + order_id）兜底。
 * </p>
 *
 * @author Tourism System
 * @see OrderReviewService
 */
@Service
public class OrderReviewServiceImpl implements OrderReviewService {

    /**
     * 订单评价数据访问 Mapper，自动注入。负责 order_review 表的 CRUD。
     */
    @Autowired
    private OrderReviewMapper orderReviewMapper;

    /**
     * 酒店订单数据访问 Mapper，自动注入。用于校验酒店订单的支付状态。
     */
    @Autowired
    private HotelOrderMapper hotelOrderMapper;

    /**
     * 门票订单数据访问 Mapper，自动注入。用于校验门票订单的支付状态。
     */
    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    /**
     * 查询指定用户的所有评价列表。
     * <p>
     * 调用 {@code orderReviewMapper.selectByUserId(userId)} 查询该用户的评价记录（SQL WHERE user_id = ? ORDER BY create_time DESC），
     * 返回列表用于前端 "我的评价" 页面展示。排序规则由 Mapper 层 SQL 控制（通常按创建时间降序）。
     * </p>
     *
     * @param userId 用户主键 ID
     * @return 该用户的 {@link OrderReview} 评价列表
     */
    @Override
    public List<OrderReview> list(Long userId) {
        return orderReviewMapper.selectByUserId(userId); // Mapper 执行 SELECT BY user_id，按时间降序
    }

    /**
     * 提交订单评价。
     * <p>
     * 业务逻辑步骤：
     * 1. 参数完整性校验：若 request 为 null、订单类型为空、订单 ID 为 null 或评价内容为空，
     *    抛出 {@link BusinessException} "评价信息不完整"；
     * 2. 订单类型规范化：将订单类型去除首尾空格并转大写，统一为 "HOTEL" 或 "TICKET"；
     * 3. 订单类型合法性校验：若类型不在支持的列表（HOTEL, TICKET）中，抛出 {@link BusinessException} "不支持的订单类型"；
     * 4. 评分范围校验：若 score 为 null，默认 5 星；若 score 不在 1-5 之间，抛出 {@link BusinessException} "评分必须在 1-5 星之间"；
     * 5. 重复评价校验：调用 {@code orderReviewMapper.selectOne(userId, orderType, orderId)} 查询是否已存在评价（SQL 复合条件查询），
     *    若已存在，抛出 {@link BusinessException} "该订单已经评价过了"；
     *    该校验防止用户对同一订单多次提交评价；
     * 6. 订单支付状态校验：调用 {@link #validateOrderCanReview(Long, String, Long)} 验证：
     *    - 订单必须存在且属于当前用户；
     *    - 订单状态必须为 "PAID" 或 "FINISHED"（即支付完成后才可评价）；
     *    若校验失败，分别抛出对应业务异常；
     * 7. 构造 {@link OrderReview} 实体，设置用户 ID、订单类型、订单 ID、评分、评价内容（去除首尾空格）；
     * 8. 调用 {@code orderReviewMapper.insert(review)} 将评价记录持久化到数据库（SQL INSERT）。
     * 注意：步骤5和步骤8之间可能存在并发竞态（两个请求同时通过重复校验），但数据库唯一索引
     * user_id + order_type + order_id 会阻止重复插入，抛出唯一约束异常。
     * </p>
     *
     * @param userId  当前登录用户主键 ID
     * @param request 评价请求对象
     */
    @Override
    public void submit(Long userId, OrderReviewRequest request) {
        // 步骤1：参数完整性校验
        if (request == null || isBlank(request.getOrderType()) || request.getOrderId() == null || isBlank(request.getContent())) {
            throw new BusinessException("评价信息不完整");
        }
        // 步骤2：订单类型规范化，统一为大写，避免大小写不一致问题
        String orderType = request.getOrderType().trim().toUpperCase();
        // 步骤3：订单类型白名单校验，仅支持酒店和门票两类订单
        if (!Arrays.asList("HOTEL", "TICKET").contains(orderType)) {
            throw new BusinessException("不支持的订单类型");
        }
        // 步骤4：评分范围校验，默认 5 星，限制 1-5 防止恶意评分
        int score = request.getScore() == null ? 5 : request.getScore();
        if (score < 1 || score > 5) {
            throw new BusinessException("评分必须在 1-5 星之间");
        }
        // 步骤5：重复评价校验，查询是否已评价过该订单
        if (orderReviewMapper.selectOne(userId, orderType, request.getOrderId()) != null) {
            throw new BusinessException("该订单已经评价过了");
        }
        // 步骤6：订单支付状态校验，只有支付后的订单才能评价
        validateOrderCanReview(userId, orderType, request.getOrderId());

        // 步骤7：构造评价实体
        OrderReview review = new OrderReview();
        review.setUserId(userId);
        review.setOrderType(orderType);
        review.setOrderId(request.getOrderId());
        review.setScore(score);
        review.setContent(request.getContent().trim()); // 去除首尾空格，保持数据整洁
        // 步骤8：持久化评价记录
        orderReviewMapper.insert(review); // Mapper 执行 INSERT INTO order_review
    }

    /**
     * 校验订单是否可以被评价。
     * <p>
     * 私有工具方法，根据订单类型分别查询酒店订单或门票订单，并校验：
     * 1. 订单存在性：若订单为 null，说明订单不存在；
     * 2. 订单所有权：若订单的 userId 与当前 userId 不一致，说明无权评价；
     * 3. 订单支付状态：若订单状态不是 "PAID" 也不是 "FINISHED"，说明订单未支付完成，不可评价。
     * 若任一校验失败，抛出对应的 {@link BusinessException}。
     * 该设计原因：将订单校验逻辑集中在此私有方法，submit 方法只需调用一次，避免重复代码。
     * </p>
     *
     * @param userId  当前登录用户主键 ID
     * @param orderType 订单类型，"HOTEL" 或 "TICKET"
     * @param orderId 订单主键 ID
     */
    private void validateOrderCanReview(Long userId, String orderType, Long orderId) {
        if ("HOTEL".equals(orderType)) {
            // 查询酒店订单（SQL 主键查询）
            HotelOrder order = hotelOrderMapper.selectById(orderId);
            // 校验订单存在性和所有权
            if (order == null || !order.getUserId().equals(userId)) {
                throw new BusinessException("酒店订单不存在或无权评价");
            }
            // 校验订单状态必须为 PAID 或 FINISHED，未支付订单不能评价
            if (!"PAID".equals(order.getOrderStatus()) && !"FINISHED".equals(order.getOrderStatus())) {
                throw new BusinessException("酒店订单支付后才可以评价");
            }
            return; // 酒店订单校验通过，直接返回
        }
        // 查询门票订单（SQL 主键查询）
        TicketOrder order = ticketOrderMapper.selectById(orderId);
        // 校验订单存在性和所有权
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("门票订单不存在或无权评价");
        }
        // 校验订单状态必须为 PAID 或 FINISHED
        if (!"PAID".equals(order.getOrderStatus()) && !"FINISHED".equals(order.getOrderStatus())) {
            throw new BusinessException("门票订单支付后才可以评价");
        }
    }

    /**
     * 判断字符串是否为空白（null、空字符串或仅包含空白字符）。
     * <p>私有工具方法，用于简化参数校验中的空值判断。</p>
     *
     * @param value 待判断的字符串
     * @return true 表示字符串为空白，false 表示有有效内容
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
