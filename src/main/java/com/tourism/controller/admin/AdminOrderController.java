package com.tourism.controller.admin;

import com.tourism.common.ApiResponse;
import com.tourism.service.AdminOrderService;
import com.tourism.vo.AdminOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    @Autowired
    private AdminOrderService adminOrderService;

    @GetMapping("/list")
    public ApiResponse<List<AdminOrderVO>> list() {
        return ApiResponse.success(adminOrderService.listAllOrders());
    }

    @PostMapping("/hotel/status")
    public ApiResponse<String> updateHotelStatus(@RequestParam Long id, @RequestParam String orderStatus) {
        adminOrderService.updateHotelOrderStatus(id, orderStatus);
        return ApiResponse.success("酒店订单状态已更新", "OK");
    }

    @PostMapping("/ticket/status")
    public ApiResponse<String> updateTicketStatus(@RequestParam Long id, @RequestParam String orderStatus) {
        adminOrderService.updateTicketOrderStatus(id, orderStatus);
        return ApiResponse.success("门票订单状态已更新", "OK");
    }
}
