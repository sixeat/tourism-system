package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.entity.Ticket;
import com.tourism.service.AdminResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ticket")
public class AdminTicketController {

    @Autowired
    private AdminResourceService adminResourceService;

    @GetMapping("/list")
    public ApiResponse<List<Ticket>> list() {
        return ApiResponse.success(adminResourceService.listTickets());
    }

    @PostMapping("/save")
    public ApiResponse<Ticket> save(@RequestBody Ticket ticket) {
        return ApiResponse.success("门票保存成功", adminResourceService.saveTicket(ticket));
    }

    @PutMapping("/update")
    public ApiResponse<Ticket> update(@RequestBody Ticket ticket) {
        return ApiResponse.success("门票修改成功", adminResourceService.updateTicket(ticket));
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        adminResourceService.deleteTicket(id);
        return ApiResponse.success("门票删除成功", "OK");
    }
}
