package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import com.tourism.dto.OrderReviewRequest;
import com.tourism.entity.OrderReview;
import com.tourism.service.OrderReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/user/reviews")
public class OrderReviewController {

    @Autowired
    private OrderReviewService orderReviewService;

    @GetMapping
    public ApiResponse<List<OrderReview>> list(HttpSession session) {
        return ApiResponse.success(orderReviewService.list(currentUserId(session)));
    }

    @PostMapping
    public ApiResponse<String> submit(@RequestBody OrderReviewRequest request, HttpSession session) {
        orderReviewService.submit(currentUserId(session), request);
        return ApiResponse.success("评价已保存", "OK");
    }

    private Long currentUserId(HttpSession session) {
        return Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID)));
    }
}
