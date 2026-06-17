package com.tourism.service.impl;

import com.tourism.entity.Ticket;
import com.tourism.mapper.TicketMapper;
import com.tourism.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketMapper ticketMapper;

    @Override
    public List<Ticket> listByScenicId(Long scenicId) {
        return ticketMapper.selectByScenicId(scenicId);
    }
}
