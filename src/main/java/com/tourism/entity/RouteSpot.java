package com.tourism.entity;

/**
 * 路线景点关联实体类（RouteSpot Entity）
 *
 * <p>本类为 POJO（Plain Old Java Object），对应数据库表 <code>route_spot</code>，
 * 是 <strong>多对多关联的中间表（junction/bridge table）</strong>，
 * 用于建立 {@link TravelRoute} 与 {@link ScenicSpot} 之间的关联关系，
 * 同时承载额外的关联属性（如第几天、排序号）。
 * 通过私有无参字段 + 公共 getter/setter 满足 JavaBean 规范，
 * 支持 Spring 与 MyBatis 的反射注入机制。</p>
 *
 * <p>数据库表设计说明：</p>
 * <ul>
 *   <li>表名：route_spot</li>
 *   <li>主键：id（自增 BIGINT），唯一标识一条关联记录</li>
 *   <li>无物理外键，通过 route_id 与 scenic_id 逻辑关联主表</li>
 *   <li>建议为 route_id + day_no + sort_no 建立组合索引，以优化“查询某路线的每日景点列表”</li>
 * </ul>
 *
 * <p>与其他实体的关系：</p>
 * <ul>
 *   <li>逻辑关联 {@link TravelRoute}（通过 routeId）：标识该景点属于哪条旅游路线</li>
 *   <li>逻辑关联 {@link ScenicSpot}（通过 scenicId）：标识该关联指向哪个具体景点</li>
 *   <li>一个 {@link TravelRoute} 可包含多个 {@link ScenicSpot}，一个 {@link ScenicSpot} 可属于多条路线</li>
 * </ul>
 *
 * @author Tourism System
 * @version 1.0
 */
public class RouteSpot {

    /**
     * 关联记录唯一标识（主键）
     *
     * <p>对应数据库列 <code>id</code>，类型 <code>BIGINT</code>，非空，自增。</p>
     * <p>使用 {@link Long} 包装类，避免 NULL 映射为基本类型 0 的歧义。</p>
     */
    private Long id;

    /**
     * 旅游路线 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>route_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>逻辑关联 {@link TravelRoute} 表的 <code>id</code> 主键，表示“该景点属于哪条路线”。
     * 数据库层面建议为 route_id 建立索引，以加速“查询某路线包含的所有景点”的场景。</p>
     */
    private Long routeId;

    /**
     * 景点 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>scenic_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>逻辑关联 {@link ScenicSpot} 表的 <code>id</code> 主键，表示“该关联指向哪个景点”。
     * 与 routeId 共同构成多对多关联的核心字段。</p>
     */
    private Long scenicId;

    /**
     * 第几天
     *
     * <p>对应数据库列 <code>day_no</code>，类型 <code>INT</code>，非空。</p>
     * <p>表示该景点在整条路线中的“第几天”行程，从 1 开始计数。
     * 例如：dayNo = 1 表示该景点为第一天行程，dayNo = 2 表示第二天行程。
     * 使用 {@link Integer} 包装类，便于表达 NULL（未安排天数）状态。</p>
     * <p>业务层需校验：dayNo ≥ 1，且不能超过路线的总天数（route.days）。</p>
     */
    private Integer dayNo;

    /**
     * 排序序号
     *
     * <p>对应数据库列 <code>sort_no</code>，类型 <code>INT</code>，默认值为 0。</p>
     * <p>表示同一天内多个景点的游览顺序，数值越小排序越靠前。
     * 例如：dayNo = 1 且 sortNo = 1 表示第一天第一个游览的景点。
     * 使用 {@link Integer} 包装类，允许 NULL 表示“未指定顺序”。</p>
     * <p>业务层需保证：同一路线 + 同一天 + 同一排序号的组合不重复，否则前端展示顺序不确定。</p>
     */
    private Integer sortNo;

    /**
     * 获取关联记录唯一标识
     *
     * @return 关联记录 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置关联记录唯一标识
     *
     * @param id 关联记录 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取旅游路线 ID
     *
     * @return 路线 ID
     */
    public Long getRouteId() {
        return routeId;
    }

    /**
     * 设置旅游路线 ID
     *
     * @param routeId 路线 ID，逻辑关联 travel_route 表
     */
    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    /**
     * 获取景点 ID
     *
     * @return 景点 ID
     */
    public Long getScenicId() {
        return scenicId;
    }

    /**
     * 设置景点 ID
     *
     * @param scenicId 景点 ID，逻辑关联 scenic_spot 表
     */
    public void setScenicId(Long scenicId) {
        this.scenicId = scenicId;
    }

    /**
     * 获取第几天
     *
     * @return 天数序号，从 1 开始
     */
    public Integer getDayNo() {
        return dayNo;
    }

    /**
     * 设置第几天
     *
     * @param dayNo 天数序号，需 ≥ 1 且不超过路线总天数
     */
    public void setDayNo(Integer dayNo) {
        this.dayNo = dayNo;
    }

    /**
     * 获取排序序号
     *
     * @return 排序序号，数值越小越靠前
     */
    public Integer getSortNo() {
        return sortNo;
    }

    /**
     * 设置排序序号
     *
     * @param sortNo 排序序号，建议同一天内不重复
     */
    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
