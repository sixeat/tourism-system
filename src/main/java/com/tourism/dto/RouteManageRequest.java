package com.tourism.dto;

import com.tourism.entity.RouteSpot;
import com.tourism.entity.TravelRoute;

import java.util.List;

/**
 * 路线管理请求数据传输对象（DTO - Data Transfer Object）
 *
 * <p>DTO 模式说明：
 * DTO（数据传输对象）用于封装前端提交到后端的数据，实现接口层与持久层的解耦。
 * 本类封装了创建或更新旅游路线时所需的完整数据，包括路线基本信息和关联的景点列表。
 * 前端以JSON格式提交，后端通过@RequestBody反序列化为本DTO，再调用服务层处理保存逻辑。</p>
 *
 * <p>核心结构：
 * <ul>
 *   <li>route：路线基本信息（TravelRoute实体），包含路线名称、描述、天数、预算等</li>
 *   <li>spots：路线关联景点列表（RouteSpot实体列表），包含每个景点的顺序、停留时间等</li>
 * </ul></p>
 *
 * <p>设计特点：
 * 本DTO采用"主从结构"，将一对多的关系（一条路线包含多个景点）封装为单一对象，
 * 前端只需发送一次请求即可保存完整路线数据，避免多次请求和事务不一致问题。</p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class RouteManageRequest {

    private TravelRoute route;
    // route：旅游路线基本信息对象，类型为 TravelRoute 实体类
    // 包含字段：id（路线ID）、routeName（路线名称）、city（城市）、days（天数）、
    // budget（预算）、description（描述）、theme（主题）、difficulty（难度）等
    // 在创建新路线时，id 可能为 null，由数据库自增生成；
    // 在更新路线时，id 为已有路线的ID，用于定位修改目标。

    private List<RouteSpot> spots;
    // spots：路线关联景点列表，类型为 RouteSpot 实体类列表
    // 每个 RouteSpot 包含字段：id（关联ID）、routeId（路线ID）、spotId（景点ID）、
    // dayNumber（第几天）、orderIndex（当天顺序）、stayDuration（停留时长，分钟）等
    // 该列表定义了路线中景点的顺序、每天安排哪些景点、每个景点停留多久等详细规划
    // 例如：Day1上午参观故宫博物院（停留3小时），下午参观景山公园（停留1小时）

    public TravelRoute getRoute() {
        // 返回路线基本信息对象
        return route;
    }

    public void setRoute(TravelRoute route) {
        // 设置路线基本信息对象
        this.route = route;
    }

    public List<RouteSpot> getSpots() {
        // 返回路线关联景点列表
        return spots;
    }

    public void setSpots(List<RouteSpot> spots) {
        // 设置路线关联景点列表
        this.spots = spots;
    }
}
