package com.tourism.service.impl;

import com.tourism.entity.ScenicSpot;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.service.ScenicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScenicServiceImpl implements ScenicService {

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Override
    public List<ScenicSpot> listAll() {
        return scenicSpotMapper.selectAll();
    }
}