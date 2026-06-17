package com.tourism.service.impl;

import com.tourism.common.BusinessException;
import com.tourism.dto.RouteManageRequest;
import com.tourism.entity.RouteSpot;
import com.tourism.entity.TravelRoute;
import com.tourism.mapper.RouteSpotMapper;
import com.tourism.mapper.TravelRouteMapper;
import com.tourism.service.AdminRouteService;
import com.tourism.vo.RouteManageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminRouteServiceImpl implements AdminRouteService {

    @Autowired
    private TravelRouteMapper travelRouteMapper;

    @Autowired
    private RouteSpotMapper routeSpotMapper;

    @Override
    public List<RouteManageVO> listRoutes() {
        List<TravelRoute> routes = travelRouteMapper.selectAll();
        List<RouteManageVO> result = new ArrayList<>();
        for (TravelRoute route : routes) {
            RouteManageVO vo = new RouteManageVO();
            vo.setRoute(route);
            vo.setSpots(routeSpotMapper.selectByRouteId(route.getId()));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RouteManageVO saveRoute(RouteManageRequest request) {
        TravelRoute route = request.getRoute();
        travelRouteMapper.insert(route);
        if (request.getSpots() != null) {
            for (RouteSpot spot : request.getSpots()) {
                spot.setRouteId(route.getId());
                routeSpotMapper.insert(spot);
            }
        }
        return buildRouteManageVO(route.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RouteManageVO updateRoute(RouteManageRequest request) {
        TravelRoute route = request.getRoute();
        if (route.getId() == null || travelRouteMapper.selectById(route.getId()) == null) {
            throw new BusinessException("路线不存在");
        }
        travelRouteMapper.updateById(route);
        routeSpotMapper.deleteByRouteId(route.getId());
        if (request.getSpots() != null) {
            for (RouteSpot spot : request.getSpots()) {
                spot.setRouteId(route.getId());
                routeSpotMapper.insert(spot);
            }
        }
        return buildRouteManageVO(route.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoute(Long routeId) {
        routeSpotMapper.deleteByRouteId(routeId);
        travelRouteMapper.deleteById(routeId);
    }

    private RouteManageVO buildRouteManageVO(Long routeId) {
        RouteManageVO vo = new RouteManageVO();
        vo.setRoute(travelRouteMapper.selectById(routeId));
        vo.setSpots(routeSpotMapper.selectByRouteId(routeId));
        return vo;
    }
}
