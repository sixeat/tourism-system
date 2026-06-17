package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import com.tourism.dto.HotelOrderCreateRequest;
import com.tourism.dto.TicketOrderCreateRequest;
import com.tourism.entity.HotelOrder;
import com.tourism.entity.TicketOrder;
import com.tourism.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/hotel/create")
    public ApiResponse<HotelOrder> createHotelOrder(@RequestBody HotelOrderCreateRequest request, HttpSession session) {
        request.setUserId(Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID))));
        return ApiResponse.success("\u9152\u5e97\u8ba2\u5355\u521b\u5efa\u6210\u529f", orderService.createHotelOrder(request));
    }

    @PostMapping("/ticket/create")
    public ApiResponse<TicketOrder> createTicketOrder(@RequestBody TicketOrderCreateRequest request, HttpSession session) {
        request.setUserId(Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID))));
        return ApiResponse.success("\u95e8\u7968\u8ba2\u5355\u521b\u5efa\u6210\u529f", orderService.createTicketOrder(request));
    }

    @PostMapping("/hotel/cancel")
    public ApiResponse<String> cancelHotelOrder(@RequestParam Long orderId, HttpSession session) {
        Long userId = Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID)));
        orderService.cancelHotelOrder(orderId, userId);
        return ApiResponse.success("\u9152\u5e97\u8ba2\u5355\u5df2\u53d6\u6d88", "OK");
    }

    @PostMapping("/ticket/cancel")
    public ApiResponse<String> cancelTicketOrder(@RequestParam Long orderId, HttpSession session) {
        Long userId = Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID)));
        orderService.cancelTicketOrder(orderId, userId);
        return ApiResponse.success("\u95e8\u7968\u8ba2\u5355\u5df2\u53d6\u6d88", "OK");
    }

    @PostMapping("/hotel/pay")
    public ApiResponse<String> payHotelOrder(@RequestParam Long orderId, HttpSession session) {
        Long userId = Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID)));
        orderService.payHotelOrder(orderId, userId);
        return ApiResponse.success("\u9152\u5e97\u8ba2\u5355\u652f\u4ed8\u6210\u529f", "OK");
    }

    @PostMapping("/ticket/pay")
    public ApiResponse<String> payTicketOrder(@RequestParam Long orderId, HttpSession session) {
        Long userId = Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID)));
        orderService.payTicketOrder(orderId, userId);
        return ApiResponse.success("\u95e8\u7968\u8ba2\u5355\u652f\u4ed8\u6210\u529f", "OK");
    }
}
