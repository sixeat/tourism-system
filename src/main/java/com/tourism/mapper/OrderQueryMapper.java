package com.tourism.mapper;

import com.tourism.vo.UserOrderVO;

import java.util.List;

public interface OrderQueryMapper {
    List<UserOrderVO> selectHotelOrdersByUserId(Long userId);

    List<UserOrderVO> selectTicketOrdersByUserId(Long userId);
}
