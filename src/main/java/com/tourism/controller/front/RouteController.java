package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.dto.RouteRecommendRequest;
import com.tourism.service.RouteService;
import com.tourism.vo.RouteRecommendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/route")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping("/recommend")
    public ApiResponse<List<RouteRecommendVO>> recommend(@RequestBody RouteRecommendRequest request) {
        return ApiResponse.success(routeService.recommendRoutes(request));
    }
}
