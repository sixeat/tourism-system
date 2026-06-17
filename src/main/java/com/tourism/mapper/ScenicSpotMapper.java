package com.tourism.mapper;

import com.tourism.entity.ScenicSpot;

import java.util.List;

public interface ScenicSpotMapper {
    List<ScenicSpot> selectAll();

    ScenicSpot selectById(Long id);

    int insert(ScenicSpot scenicSpot);

    int updateById(ScenicSpot scenicSpot);

    int deleteById(Long id);
}
