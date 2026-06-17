package com.tourism.service;

import com.tourism.entity.Ticket;

import java.util.List;

public interface TicketService {
    List<Ticket> listByScenicId(Long scenicId);
}
