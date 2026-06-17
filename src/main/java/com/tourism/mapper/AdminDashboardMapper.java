package com.tourism.mapper;

import java.math.BigDecimal;

public interface AdminDashboardMapper {
    int countUsers();

    int countScenicSpots();

    int countHotels();

    int countTickets();

    int countRoutes();

    int countHotelOrders();

    int countTicketOrders();

    BigDecimal sumValidRevenue();
}