package com.tourism.service;

import com.tourism.vo.UserOrderVO;

import java.util.List;

public interface UserOrderService {
    List<UserOrderVO> listUserOrders(Long userId);
}
