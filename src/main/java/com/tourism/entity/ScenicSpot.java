package com.tourism.entity;

import java.math.BigDecimal;

/**
 * 景点实体类（ScenicSpot Entity）
 *
 * <p>本类为 POJO（Plain Old Java Object），对应数据库表 <code>scenic_spot</code>，
 * 用于存储旅游景点的核心信息，是 MyBatis 与 Spring 进行 ORM 映射的核心载体。
 * 通过私有无参字段 + 公共 getter/setter 满足 JavaBean 规范，
 * 支持 Spring BeanWrapper 与 MyBatis 反射注入机制。</p>
 *
 * <p>数据库表设计说明：</p>
 * <ul>
 *   <li>表名：scenic_spot</li>
 *   <li>主键：id（自增 BIGINT），唯一标识一个景点</li>
 *   <li>无物理外键，为独立维表，可被其他业务模块逻辑引用</li>
 * </ul>
 *
 * <p>与其他实体的关系：</p>
 * <ul>
 *   <li>被 {@link Ticket} 引用（通过 scenicId）：一个景点可出售多种门票</li>
 *   <li>被 {@link RouteSpot} 引用（通过 scenicId）：一个景点可出现在多条旅游路线中</li>
 *   <li>被 {@link UserFavorite} 逻辑引用（targetId 可指向 scenic_spot.id）</li>
 * </ul>
 *
 * @author Tourism System
 * @version 1.0
 */
public class ScenicSpot {

    /**
     * 景点唯一标识（主键）
     *
     * <p>对应数据库列 <code>id</code>，类型 <code>BIGINT</code>，非空，自增。</p>
     * <p>使用 {@link Long} 包装类，避免 NULL 映射为基本类型 0 的歧义。</p>
     */
    private Long id;

    /**
     * 景点名称
     *
     * <p>对应数据库列 <code>scenic_name</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于前端展示、搜索、列表渲染，如“西湖”、“黄山风景区”。
     * 通常设置唯一约束或组合唯一约束（city + scenic_name），避免同一城市出现同名景点。</p>
     */
    private String scenicName;

    /**
     * 所属城市
     *
     * <p>对应数据库列 <code>city</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于按城市维度筛选景点、生成城市旅游攻略。
     * 统一使用城市全称（如“杭州市”），保证数据一致性。</p>
     */
    private String city;

    /**
     * 景点分类
     *
     * <p>对应数据库列 <code>category</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于前端分类筛选与推荐，常见取值（业务约定）：</p>
     * <ul>
     *   <li><code>"NATURAL"</code> — 自然风光</li>
     *   <li><code>"HISTORICAL"</code> — 历史古迹</li>
     *   <li><code>"CULTURAL"</code> — 人文景观</li>
     *   <li><code>"MODERN"</code> — 现代建筑</li>
     *   <li><code>"ENTERTAINMENT"</code> — 游乐场所</li>
     * </ul>
     * <p>使用字符串存储，便于后续扩展新分类。</p>
     */
    private String category;

    /**
     * 景点描述/简介
     *
     * <p>对应数据库列 <code>description</code>，类型 <code>TEXT</code> 或 <code>VARCHAR</code>。</p>
     * <p>用于景点详情页介绍，可包含历史背景、特色亮点、游玩建议等。
     * 若存储 HTML 富文本，前端需做好 XSS 过滤。</p>
     */
    private String description;

    /**
     * 门票参考价格
     *
     * <p>对应数据库列 <code>price</code>，类型 <code>DECIMAL(10,2)</code>。</p>
     * <p>选用 {@link BigDecimal} 原因：</p>
     * <ul>
     *   <li>货币金额必须精确计算，double/float 的二进制浮点表示存在精度误差</li>
     *   <li>BigDecimal 支持任意精度十进制运算，精确到“分”</li>
     *   <li>与数据库 DECIMAL 类型天然映射，保证数据一致性</li>
     * </ul>
     * <p>可为 null，表示“免费”或“价格待定”。实际售票价格以 {@link Ticket} 表为准。</p>
     */
    private BigDecimal price;

    /**
     * 综合评分
     *
     * <p>对应数据库列 <code>score</code>，类型 <code>DECIMAL(3,2)</code>。</p>
     * <p>选用 {@link BigDecimal} 原因：</p>
     * <ul>
     *   <li>评分通常为 0.00 ~ 5.00，保留两位小数，需要精确表示</li>
     *   <li>double 在累加求平均时可能产生微小误差，BigDecimal 保证统计准确性</li>
     * </ul>
     * <p>可为 null，表示“暂无评分”。评分通常由 {@link OrderReview} 的 score 字段汇总计算得出。</p>
     */
    private BigDecimal score;

    /**
     * 热度/人气值
     *
     * <p>对应数据库列 <code>popularity</code>，类型 <code>INT</code>，默认值为 0。</p>
     * <p>用于景点排序与推荐，数值越大表示人气越高。
     * 热度计算可由业务层定时任务根据浏览量、订单量、收藏量等综合计算后更新。
     * 使用 {@link Integer} 包装类，允许 NULL 表示“未计算热度”。</p>
     */
    private Integer popularity;

    /**
     * 标签集合
     *
     * <p>对应数据库列 <code>tags</code>，类型 <code>VARCHAR</code>。</p>
     * <p>以逗号分隔的标签字符串，如“5A景区,世界遗产,山水”。
     * 前端可拆分为标签数组进行展示。若标签体系复杂，可扩展为独立标签关联表。</p>
     */
    private String tags;

    /**
     * 经度
     *
     * <p>对应数据库列 <code>longitude</code>，类型 <code>DECIMAL(10,7)</code>。</p>
     * <p>选用 {@link BigDecimal} 原因：</p>
     * <ul>
     *   <li>地理坐标精度要求极高（7 位小数约 1 厘米精度），BigDecimal 精确保存</li>
     *   <li>避免 double 在数据库交互过程中的精度截断</li>
     * </ul>
     * <p>取值范围：-180.0000000 ~ +180.0000000。</p>
     */
    private BigDecimal longitude;

    /**
     * 纬度
     *
     * <p>对应数据库列 <code>latitude</code>，类型 <code>DECIMAL(10,7)</code>。</p>
     * <p>与经度同类型 {@link BigDecimal}，确保地图定位精度。</p>
     * <p>取值范围：-90.0000000 ~ +90.0000000。</p>
     */
    private BigDecimal latitude;

    /**
     * 获取景点唯一标识
     *
     * @return 景点 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置景点唯一标识
     *
     * @param id 景点 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取景点名称
     *
     * @return 景点名称字符串
     */
    public String getScenicName() {
        return scenicName;
    }

    /**
     * 设置景点名称
     *
     * @param scenicName 景点名称，如“西湖”
     */
    public void setScenicName(String scenicName) {
        this.scenicName = scenicName;
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
     * 获取景点分类
     *
     * @return 分类字符串，如 "NATURAL"、"HISTORICAL" 等
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置景点分类
     *
     * @param category 分类字符串
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取景点描述
     *
     * @return 描述文本
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置景点描述
     *
     * @param description 描述文本
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取门票参考价格
     *
     * @return 参考价格（BigDecimal，精确到分），可能为 null
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * 设置门票参考价格
     *
     * @param price 参考价格，使用 BigDecimal 保证精度
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * 获取综合评分
     *
     * @return 评分（BigDecimal，保留两位小数），可能为 null
     */
    public BigDecimal getScore() {
        return score;
    }

    /**
     * 设置综合评分
     *
     * @param score 评分值，使用 BigDecimal 精确表示
     */
    public void setScore(BigDecimal score) {
        this.score = score;
    }

    /**
     * 获取热度/人气值
     *
     * @return 人气值，数值越大表示越热门
     */
    public Integer getPopularity() {
        return popularity;
    }

    /**
     * 设置热度/人气值
     *
     * @param popularity 人气值，由业务层定时统计更新
     */
    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    /**
     * 获取标签集合
     *
     * @return 逗号分隔的标签字符串
     */
    public String getTags() {
        return tags;
    }

    /**
     * 设置标签集合
     *
     * @param tags 逗号分隔的标签字符串
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * 获取经度
     *
     * @return 经度（BigDecimal，7 位小数精度）
     */
    public BigDecimal getLongitude() {
        return longitude;
    }

    /**
     * 设置经度
     *
     * @param longitude 经度值，范围 -180.0000000 ~ +180.0000000
     */
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    /**
     * 获取纬度
     *
     * @return 纬度（BigDecimal，7 位小数精度）
     */
    public BigDecimal getLatitude() {
        return latitude;
    }

    /**
     * 设置纬度
     *
     * @param latitude 纬度值，范围 -90.0000000 ~ +90.0000000
     */
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
}
