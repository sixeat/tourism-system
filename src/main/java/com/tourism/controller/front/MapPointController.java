package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.entity.MapPoint;
import com.tourism.mapper.MapPointMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map")
public class MapPointController {

    @Autowired
    private MapPointMapper mapPointMapper;

    @GetMapping("/points")
    public ApiResponse<List<MapPoint>> points(@RequestParam(required = false) String pointType) {
        if (pointType == null || pointType.trim().isEmpty() || "ALL".equalsIgnoreCase(pointType)) {
            return ApiResponse.success(mapPointMapper.selectAll());
        }
        return ApiResponse.success(mapPointMapper.selectByType(pointType.trim()));
    }
}
