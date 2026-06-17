package com.tourism.mapper;

import com.tourism.vo.AdminOrderVO;

import java.util.List;

public interface AdminOrderMapper {
    List<AdminOrderVO> selectHotelOrders();

    List<AdminOrderVO> selectTicketOrders();
}
