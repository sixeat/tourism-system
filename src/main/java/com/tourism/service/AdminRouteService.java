package com.tourism.service;

import com.tourism.dto.RouteManageRequest;
import com.tourism.vo.RouteManageVO;

import java.util.List;

public interface AdminRouteService {
    List<RouteManageVO> listRoutes();

    RouteManageVO saveRoute(RouteManageRequest request);

    RouteManageVO updateRoute(RouteManageRequest request);

    void deleteRoute(Long routeId);
}
