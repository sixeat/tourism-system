package com.tourism.service.impl;

import com.tourism.mapper.OrderQueryMapper;
import com.tourism.service.UserOrderService;
import com.tourism.vo.UserOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class UserOrderServiceImpl implements UserOrderService {

    @Autowired
    private OrderQueryMapper orderQueryMapper;

    @Override
    public List<UserOrderVO> listUserOrders(Long userId) {
        List<UserOrderVO> results = new ArrayList<>();
        results.addAll(orderQueryMapper.selectHotelOrdersByUserId(userId));
        results.addAll(orderQueryMapper.selectTicketOrdersByUserId(userId));
        results.sort(Comparator.comparing(UserOrderVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return results;
    }
}
