package com.tourism.vo;

import com.tourism.entity.RouteSpot;
import com.tourism.entity.TravelRoute;

import java.util.List;

/**
 * 路线管理视图对象（VO - View Object）
 *
 * <p>VO 模式说明：
 * VO（视图对象）是专门为前端展示层设计的对象，用于封装控制器返回给前端的聚合数据。
 * 与Entity（数据库实体）的区别：Entity对应数据库表结构，VO可聚合多个Entity的字段并包含计算属性，
 * 不直接映射数据库表，专为前端展示需求服务。本类聚合了 TravelRoute（路线基本信息）和
 * RouteSpot（路线关联景点）两类实体的数据，为前端路线详情页提供完整的路线数据。</p>
 *
 * <p>本类封装了旅游路线的完整信息（路线+景点列表），适用于路线管理后台的路线编辑页面、
 * 路线详情展示页等场景。与RouteManageRequest结构类似，但VO用于返回数据，DTO用于接收请求。</p>
 *
 * <p>核心结构：
 * <ul>
 *   <li>route：路线基本信息（TravelRoute实体）</li>
 *   <li>spots：路线关联景点列表（RouteSpot实体列表，按dayNumber和orderIndex排序）</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class RouteManageVO {

    private TravelRoute route;
    // route：旅游路线基本信息对象，类型为 TravelRoute 实体类
    // 包含字段：id（路线ID）、routeName（路线名称）、city（城市）、days（天数）、
    // budget（预算）、description（描述）、theme（主题）、difficulty（难度）、
    // createTime（创建时间）、updateTime（更新时间）等
    // 此字段对应数据库 travel_route 表的一条记录，展示路线的整体概况

    private List<RouteSpot> spots;
    // spots：路线关联景点列表，类型为 RouteSpot 实体类列表
    // 每个 RouteSpot 包含字段：id（关联ID）、routeId（路线ID）、spotId（景点ID）、
    // spotName（景点名称，可能来自关联查询）、dayNumber（安排在第几天）、
    // orderIndex（当天游览顺序）、stayDuration（建议停留时长，分钟）、
    // transportToNext（到下景点的交通方式）等
    // 该列表定义了路线的详细行程安排，前端可据此渲染日程时间轴或地图路线
    // 列表通常按 dayNumber 升序、orderIndex 升序排列，确保展示顺序正确

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
