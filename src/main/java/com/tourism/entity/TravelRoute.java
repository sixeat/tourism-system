package com.tourism.entity;

import java.math.BigDecimal;

/**
 * 旅游路线实体类（TravelRoute Entity）
 *
 * <p>本类为 POJO（Plain Old Java Object），对应数据库表 <code>travel_route</code>，
 * 用于存储系统推荐的旅游路线信息，是 MyBatis 与 Spring 进行 ORM 映射的核心载体。
 * 通过私有无参字段 + 公共 getter/setter 满足 JavaBean 规范，
 * 支持 Spring BeanWrapper 与 MyBatis 反射注入机制。</p>
 *
 * <p>数据库表设计说明：</p>
 * <ul>
 *   <li>表名：travel_route</li>
 *   <li>主键：id（自增 BIGINT），唯一标识一条旅游路线</li>
 *   <li>无物理外键，为独立维表，与其他实体通过逻辑字段关联</li>
 * </ul>
 *
 * <p>与其他实体的关系：</p>
 * <ul>
 *   <li>通过 {@link RouteSpot} 多对多关联 {@link ScenicSpot}：
     *   一条路线包含多个景点，一个景点可出现在多条路线中</li>
 *   <li>被 {@link UserFavorite} 逻辑引用（targetId 可指向 travel_route.id）</li>
 * </ul>
 *
 * @author Tourism System
 * @version 1.0
 */
public class TravelRoute {

    /**
     * 路线唯一标识（主键）
     *
     * <p>对应数据库列 <code>id</code>，类型 <code>BIGINT</code>，非空，自增。</p>
     * <p>使用 {@link Long} 包装类，避免 NULL 映射为基本类型 0 的歧义。</p>
     */
    private Long id;

    /**
     * 路线名称
     *
     * <p>对应数据库列 <code>route_name</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于前端展示与搜索，如“杭州三日游经典路线”、“西湖一日游”。
     * 建议设置长度限制（如 VARCHAR(128)），并在数据库层面建立唯一约束防止重复。</p>
     */
    private String routeName;

    /**
     * 所属城市
     *
     * <p>对应数据库列 <code>city</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于按城市筛选路线，如“杭州市”下的所有推荐路线。
     * 统一使用城市全称，保证数据一致性。</p>
     */
    private String city;

    /**
     * 行程天数
     *
     * <p>对应数据库列 <code>days</code>，类型 <code>INT</code>，非空。</p>
     * <p>表示该路线建议游玩的天数，从 1 开始计数。
     * 使用 {@link Integer} 包装类，避免基本类型默认 0 的语义歧义（0 天无业务意义）。</p>
     * <p>业务层需校验：days ≥ 1，且与 {@link RouteSpot} 中 dayNo 的最大值保持一致。</p>
     */
    private Integer days;

    /**
     * 预估预算
     *
     * <p>对应数据库列 <code>budget</code>，类型 <code>DECIMAL(10,2)</code>。</p>
     * <p>选用 {@link BigDecimal} 原因：</p>
     * <ul>
     *   <li>货币金额必须精确计算，double/float 存在二进制浮点精度误差</li>
     *   <li>BigDecimal 支持任意精度十进制运算，精确到“分”</li>
     *   <li>与数据库 DECIMAL 类型天然映射，保证数据一致性</li>
     * </ul>
     * <p>表示该路线的参考人均消费（如门票+交通+餐饮+住宿），可为 null 表示“未提供预算”。</p>
     */
    private BigDecimal budget;

    /**
     * 路线主题
     *
     * <p>对应数据库列 <code>theme</code>，类型 <code>VARCHAR</code>。</p>
     * <p>用于前端分类推荐，常见取值（业务约定）：</p>
     * <ul>
     *   <li><code>"CLASSIC"</code> — 经典路线</li>
     *   <li><code>"FOODIE"</code> — 美食之旅</li>
     *   <li><code>"CULTURE"</code> — 文化深度游</li>
     *   <li><code>"FAMILY"</code> — 亲子游</li>
     *   <li><code>"ROMANTIC"</code> — 情侣浪漫游</li>
     *   <li><code>"ADVENTURE"</code> — 户外探险</li>
     * </ul>
     * <p>使用字符串存储，便于扩展新主题。</p>
     */
    private String theme;

    /**
     * 路线描述/攻略详情
     *
     * <p>对应数据库列 <code>route_desc</code>，类型 <code>TEXT</code> 或 <code>VARCHAR</code>。</p>
     * <p>用于路线详情页展示，可包含行程安排、交通建议、注意事项等。
     * 若存储 HTML 富文本，前端需做好 XSS 过滤。</p>
     */
    private String routeDesc;

    /**
     * 获取路线唯一标识
     *
     * @return 路线 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置路线唯一标识
     *
     * @param id 路线 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取路线名称
     *
     * @return 路线名称字符串
     */
    public String getRouteName() {
        return routeName;
    }

    /**
     * 设置路线名称
     *
     * @param routeName 路线名称，如“杭州三日游经典路线”
     */
    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    /**
     * 获取所属城市
     *
     * @return 城市名称字符串
     */
    public String getCity() {
        return city;
    }

    /**
     * 设置所属城市
     *
     * @param city 城市名称，如“杭州市”
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 获取行程天数
     *
     * @return 行程天数（≥ 1）
     */
    public Integer getDays() {
        return days;
    }

    /**
     * 设置行程天数
     *
     * @param days 行程天数，需 ≥ 1
     */
    public void setDays(Integer days) {
        this.days = days;
    }

    /**
     * 获取预估预算
     *
     * @return 预估人均预算（BigDecimal，精确到分），可能为 null
     */
    public BigDecimal getBudget() {
        return budget;
    }

    /**
     * 设置预估预算
     *
     * @param budget 预估人均预算，使用 BigDecimal 保证精度
     */
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    /**
     * 获取路线主题
     *
     * @return 主题字符串，如 "CLASSIC"、"FOODIE" 等
     */
    public String getTheme() {
        return theme;
    }

    /**
     * 设置路线主题
     *
     * @param theme 主题字符串
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * 获取路线描述
     *
     * @return 路线描述/攻略文本
     */
    public String getRouteDesc() {
        return routeDesc;
    }

    /**
     * 设置路线描述
     *
     * @param routeDesc 路线描述/攻略文本
     */
    public void setRouteDesc(String routeDesc) {
        this.routeDesc = routeDesc;
    }
}
