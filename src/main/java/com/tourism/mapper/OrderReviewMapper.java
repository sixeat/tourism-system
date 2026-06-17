package com.tourism.mapper;

import com.tourism.entity.OrderReview;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderReviewMapper {
    List<OrderReview> selectByUserId(Long userId);

    OrderReview selectOne(@Param("userId") Long userId,
                          @Param("orderType") String orderType,
                          @Param("orderId") Long orderId);

    int insert(OrderReview review);
}
