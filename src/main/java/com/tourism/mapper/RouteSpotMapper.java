package com.tourism.mapper;

import com.tourism.entity.RouteSpot;

import java.util.List;

public interface RouteSpotMapper {
    List<RouteSpot> selectByRouteId(Long routeId);

    int insert(RouteSpot routeSpot);

    int deleteByRouteId(Long routeId);
}
