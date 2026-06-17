package com.tourism.service;

import com.tourism.dto.RouteRecommendRequest;
import com.tourism.vo.RouteRecommendVO;

import java.util.List;

public interface RouteService {
    List<RouteRecommendVO> recommendRoutes(RouteRecommendRequest request);
}
