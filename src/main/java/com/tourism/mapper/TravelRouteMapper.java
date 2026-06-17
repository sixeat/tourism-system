package com.tourism.mapper;

import com.tourism.entity.TravelRoute;

import java.util.List;

public interface TravelRouteMapper {
    List<TravelRoute> selectAll();

    TravelRoute selectById(Long id);

    int insert(TravelRoute travelRoute);

    int updateById(TravelRoute travelRoute);

    int deleteById(Long id);
}
