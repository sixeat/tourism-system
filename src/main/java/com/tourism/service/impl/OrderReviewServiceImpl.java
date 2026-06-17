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

@Service
public class OrderReviewServiceImpl implements OrderReviewService {

    @Autowired
    private OrderReviewMapper orderReviewMapper;

    @Autowired
    private HotelOrderMapper hotelOrderMapper;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Override
    public List<OrderReview> list(Long userId) {
        return orderReviewMapper.selectByUserId(userId);
    }

    @Override
    public void submit(Long userId, OrderReviewRequest request) {
        if (request == null || isBlank(request.getOrderType()) || request.getOrderId() == null || isBlank(request.getContent())) {
            throw new BusinessException("评价信息不完整");
        }
        String orderType = request.getOrderType().trim().toUpperCase();
        if (!Arrays.asList("HOTEL", "TICKET").contains(orderType)) {
            throw new BusinessException("不支持的订单类型");
        }
        int score = request.getScore() == null ? 5 : request.getScore();
        if (score < 1 || score > 5) {
            throw new BusinessException("评分必须在 1-5 星之间");
        }
        if (orderReviewMapper.selectOne(userId, orderType, request.getOrderId()) != null) {
            throw new BusinessException("该订单已经评价过了");
        }
        validateOrderCanReview(userId, orderType, request.getOrderId());

        OrderReview review = new OrderReview();
        review.setUserId(userId);
        review.setOrderType(orderType);
        review.setOrderId(request.getOrderId());
        review.setScore(score);
        review.setContent(request.getContent().trim());
        orderReviewMapper.insert(review);
    }

    private void validateOrderCanReview(Long userId, String orderType, Long orderId) {
        if ("HOTEL".equals(orderType)) {
            HotelOrder order = hotelOrderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                throw new BusinessException("酒店订单不存在或无权评价");
            }
            if (!"PAID".equals(order.getOrderStatus()) && !"FINISHED".equals(order.getOrderStatus())) {
                throw new BusinessException("酒店订单支付后才可以评价");
            }
            return;
        }
        TicketOrder order = ticketOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("门票订单不存在或无权评价");
        }
        if (!"PAID".equals(order.getOrderStatus()) && !"FINISHED".equals(order.getOrderStatus())) {
            throw new BusinessException("门票订单支付后才可以评价");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
