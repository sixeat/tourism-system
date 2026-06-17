package com.tourism.service.impl;

import com.tourism.common.BusinessException;
import com.tourism.mapper.AdminOrderMapper;
import com.tourism.mapper.HotelOrderMapper;
import com.tourism.mapper.TicketOrderMapper;
import com.tourism.service.AdminOrderService;
import com.tourism.vo.AdminOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    @Autowired
    private AdminOrderMapper adminOrderMapper;

    @Autowired
    private HotelOrderMapper hotelOrderMapper;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Override
    public List<AdminOrderVO> listAllOrders() {
        List<AdminOrderVO> results = new ArrayList<>();
        results.addAll(adminOrderMapper.selectHotelOrders());
        results.addAll(adminOrderMapper.selectTicketOrders());
        results.sort(Comparator.comparing(AdminOrderVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return results;
    }

    @Override
    public void updateHotelOrderStatus(Long id, String orderStatus) {
        if (hotelOrderMapper.selectById(id) == null) {
            throw new BusinessException("酒店订单不存在");
        }
        hotelOrderMapper.updateStatus(id, orderStatus);
    }

    @Override
    public void updateTicketOrderStatus(Long id, String orderStatus) {
        if (ticketOrderMapper.selectById(id) == null) {
            throw new BusinessException("门票订单不存在");
        }
        ticketOrderMapper.updateStatus(id, orderStatus);
    }
}
