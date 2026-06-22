package com.tourism.service.impl;

import com.tourism.entity.ScenicSpot;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.service.ScenicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 景点服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入 {@link ScenicSpotMapper}。
 * 职责：为 C 端用户提供景点信息的查询服务，是系统基础数据服务之一。
 * 方法为只读查询，不涉及数据库写操作，因此不声明 {@link org.springframework.transaction.annotation.Transactional}。
 * </p>
 *
 * @author Tourism System
 * @see ScenicService
 */
@Service
public class ScenicServiceImpl implements ScenicService {

    /**
     * 景点数据访问 Mapper，自动注入。负责 scenic_spot 表查询。
     */
    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    /**
     * 查询所有景点列表。
     * <p>
     * 调用 {@code scenicSpotMapper.selectAll()} 执行全表查询（SQL SELECT * FROM scenic_spot），
     * 返回系统中所有旅游景点的完整信息，包括景点名称、城市、分类、评分、价格、标签、描述等。
     * 用于前端景点列表页、地图点位数据等场景。
     * 排序规则由 Mapper 层 SQL 控制（通常按热度或评分降序）。
     * </p>
     *
     * @return 所有景点的 {@link ScenicSpot} 列表
     */
    @Override
    public List<ScenicSpot> listAll() {
        return scenicSpotMapper.selectAll(); // Mapper 执行 SELECT * FROM scenic_spot
    }
}
