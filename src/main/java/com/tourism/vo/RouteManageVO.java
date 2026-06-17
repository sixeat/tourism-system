package com.tourism.vo;

import com.tourism.entity.RouteSpot;
import com.tourism.entity.TravelRoute;

import java.util.List;

public class RouteManageVO {
    private TravelRoute route;
    private List<RouteSpot> spots;

    public TravelRoute getRoute() {
        return route;
    }

    public void setRoute(TravelRoute route) {
        this.route = route;
    }

    public List<RouteSpot> getSpots() {
        return spots;
    }

    public void setSpots(List<RouteSpot> spots) {
        this.spots = spots;
    }
}
