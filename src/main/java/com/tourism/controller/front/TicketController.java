package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.entity.Ticket;
import com.tourism.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/list")
    public ApiResponse<List<Ticket>> list(@RequestParam Long scenicId) {
        return ApiResponse.success(ticketService.listByScenicId(scenicId));
    }
}
