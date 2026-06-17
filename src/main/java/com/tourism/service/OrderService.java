package com.tourism.service;

import com.tourism.dto.HotelOrderCreateRequest;
import com.tourism.dto.TicketOrderCreateRequest;
import com.tourism.entity.HotelOrder;
import com.tourism.entity.TicketOrder;

public interface OrderService {
    HotelOrder createHotelOrder(HotelOrderCreateRequest request);

    TicketOrder createTicketOrder(TicketOrderCreateRequest request);

    void cancelHotelOrder(Long orderId, Long userId);

    void cancelTicketOrder(Long orderId, Long userId);

    void payHotelOrder(Long orderId, Long userId);

    void payTicketOrder(Long orderId, Long userId);
}
