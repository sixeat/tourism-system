package com.tourism.service;

import com.tourism.dto.OrderReviewRequest;
import com.tourism.entity.OrderReview;

import java.util.List;

/**
 * 订单评价服务接口（Service Contract）。
 * <p>
 * 本接口定义了用户对已完成订单进行评价的查询与提交操作。
 * 支持酒店订单（HOTEL）和门票订单（TICKET）两类业务，
 * 评价前需校验订单是否支付完成且当前用户是否拥有该订单。
 * 实现类：{@link com.tourism.service.impl.OrderReviewServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface OrderReviewService {

    /**
     * 查询指定用户的所有评价列表。
     * <p>
     * 根据用户 ID 查询该用户提交过的所有订单评价，用于前端 "我的评价" 页面展示。
     * 评价按时间倒序排列由 Mapper 层 SQL 控制。
     * </p>
     *
     * @param userId 用户主键 ID，不可为 null
     * @return 该用户的 {@link OrderReview} 评价列表
     */
    List<OrderReview> list(Long userId);

    /**
     * 提交订单评价。
     * <p>
     * 业务规则：
     * 1. 请求参数必须完整（订单类型、订单 ID、评价内容不可为空）；
     * 2. 订单类型仅支持 HOTEL 和 TICKET；
     * 3. 评分范围 1-5 星，默认 5 星；
     * 4. 每个订单只能评价一次，重复评价将被拒绝；
     * 5. 订单必须处于 PAID 或 FINISHED 状态才允许评价；
     * 6. 订单必须属于当前登录用户。
     * 若违反规则，实现层应抛出 {@link com.tourism.common.BusinessException}。
     * </p>
     *
     * @param userId  当前登录用户主键 ID，用于权限校验
     * @param request 评价请求对象，包含订单类型、订单 ID、评分、评价内容
     */
    void submit(Long userId, OrderReviewRequest request);
}
