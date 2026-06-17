package com.tourism.mapper;

import com.tourism.entity.MapPoint;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MapPointMapper {
    List<MapPoint> selectAll();

    List<MapPoint> selectByType(@Param("pointType") String pointType);
}
