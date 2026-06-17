package com.tourism.service;

import com.tourism.vo.AdminOrderVO;

import java.util.List;

public interface AdminOrderService {
    List<AdminOrderVO> listAllOrders();

    void updateHotelOrderStatus(Long id, String orderStatus);

    void updateTicketOrderStatus(Long id, String orderStatus);
}
