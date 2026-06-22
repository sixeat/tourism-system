package com.tourism.service;

import com.tourism.entity.ScenicSpot;

import java.util.List;

/**
 * 景点服务接口（Service Contract）。
 * <p>
 * 本接口定义了景点信息查询的基础服务契约，面向 C 端用户（前台）。
 * 提供全量景点列表查询功能，用于前端景点列表页、首页展示、地图点位等场景。
 * 实现类：{@link com.tourism.service.impl.ScenicServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface ScenicService {

    /**
     * 查询所有景点列表。
     * <p>
     * 返回系统中所有旅游景点的完整信息，包括景点名称、城市、分类、评分、价格、标签、描述等。
     * 用于前端景点列表页展示、地图点位数据源等场景。
     * 数据排序由 Mapper 层 SQL 控制（通常按热度或评分降序）。
     * </p>
     *
     * @return 所有景点的 {@link ScenicSpot} 列表
     */
    List<ScenicSpot> listAll();
}
