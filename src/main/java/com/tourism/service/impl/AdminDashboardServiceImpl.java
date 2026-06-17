package com.tourism.service.impl;

import com.tourism.mapper.AdminDashboardMapper;
import com.tourism.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired
    private AdminDashboardMapper adminDashboardMapper;

    @Override
    public Map<String, Object> summary() {
        int hotelOrderCount = adminDashboardMapper.countHotelOrders();
        int ticketOrderCount = adminDashboardMapper.countTicketOrders();
        BigDecimal totalRevenue = adminDashboardMapper.sumValidRevenue();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", adminDashboardMapper.countUsers());
        data.put("scenicCount", adminDashboardMapper.countScenicSpots());
        data.put("hotelCount", adminDashboardMapper.countHotels());
        data.put("ticketCount", adminDashboardMapper.countTickets());
        data.put("routeCount", adminDashboardMapper.countRoutes());
        data.put("hotelOrderCount", hotelOrderCount);
        data.put("ticketOrderCount", ticketOrderCount);
        data.put("orderCount", hotelOrderCount + ticketOrderCount);
        data.put("totalRevenue", totalRevenue == null ? BigDecimal.ZERO : totalRevenue);
        return data;
    }
}