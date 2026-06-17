package com.tourism.service;

import com.tourism.dto.OrderReviewRequest;
import com.tourism.entity.OrderReview;

import java.util.List;

public interface OrderReviewService {
    List<OrderReview> list(Long userId);

    void submit(Long userId, OrderReviewRequest request);
}
