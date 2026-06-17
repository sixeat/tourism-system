package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import com.tourism.service.UserOrderService;
import com.tourism.vo.UserOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/user/orders")
public class UserOrderController {

    @Autowired
    private UserOrderService userOrderService;

    @GetMapping
    public ApiResponse<List<UserOrderVO>> list(HttpSession session) {
        Object userId = session.getAttribute(SessionConstants.LOGIN_USER_ID);
        if (userId == null) {
            return ApiResponse.fail("not logged in");
        }
        return ApiResponse.success(userOrderService.listUserOrders(Long.valueOf(String.valueOf(userId))));
    }
}
