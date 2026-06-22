package com.tourism.entity;

import java.math.BigDecimal;

/**
 * 地图点位实体类（MapPoint Entity）
 *
 * <p>本类为 POJO（Plain Old Java Object），对应数据库表 <code>map_point</code>，
 * 用于存储地图上的各类点位信息（如酒店、景点、餐厅、交通枢纽等）。
 * 通过私有无参字段 + 公共 getter/setter 满足 JavaBean 规范，
 * 支持 Spring 与 MyBatis 的反射注入机制。</p>
 *
 * <p>数据库表设计说明：</p>
 * <ul>
 *   <li>表名：map_point</li>
 *   <li>主键：id（自增 BIGINT），全局唯一标识一个地图点位</li>
 *   <li>无物理外键，为独立维表，可被其他业务表逻辑引用</li>
 * </ul>
 *
 * <p>与其他实体的关系：</p>
 * <ul>
 *   <li>可作为 {@link Hotel}、{@link ScenicSpot} 的地理位置扩展（业务层按需关联）</li>
 *   <li>被 {@link UserFavorite} 逻辑引用（targetId 可指向 map_point.id）</li>
 * </ul>
 *
 * @author Tourism System
 * @version 1.0
 */
public class MapPoint {

    /**
     * 点位唯一标识（主键）
     *
     * <p>对应数据库列 <code>id</code>，类型 <code>BIGINT</code>，非空，自增。</p>
     * <p>使用 {@link Long} 包装类，避免 NULL 映射为基本类型 0 的歧义。</p>
     */
    private Long id;

    /**
     * 点位名称
     *
     * <p>对应数据库列 <code>point_name</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于地图展示与搜索，如“西湖断桥”、“杭州东站”等。</p>
     */
    private String pointName;

    /**
     * 点位类型
     *
     * <p>对应数据库列 <code>point_type</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于区分点位业务类别，常见取值（业务约定）：</p>
     * <ul>
     *   <li><code>"HOTEL"</code> — 酒店</li>
     *   <li><code>"SCENIC"</code> — 景点</li>
     *   <li><code>"RESTAURANT"</code> — 餐厅</li>
     *   <li><code>"TRANSPORT"</code> — 交通枢纽</li>
     *   <li><code>"SHOP"</code> — 购物点</li>
     * </ul>
     * <p>使用字符串而非数值，增强可读性，便于扩展新类型。</p>
     */
    private String pointType;

    /**
     * 所属城市
     *
     * <p>对应数据库列 <code>city</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于按城市筛选地图点位，支持城市级别的地图聚合展示。</p>
     */
    private String city;

    /**
     * 详细地址
     *
     * <p>对应数据库列 <code>address</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于地图定位、导航地址展示，通常与经纬度结合使用。</p>
     */
    private String address;

    /**
     * 点位描述
     *
     * <p>对应数据库列 <code>description</code>，类型 <code>TEXT</code> 或 <code>VARCHAR</code>。</p>
     * <p>用于点位详情页介绍，可包含富文本或纯文本内容。</p>
     */
    private String description;

    /**
     * 人均消费/参考价格
     *
     * <p>对应数据库列 <code>price</code>，类型 <code>DECIMAL(10,2)</code>。</p>
     * <p>选用 {@link BigDecimal} 原因：</p>
     * <ul>
     *   <li>货币金额必须精确到分，浮点类型存在精度误差</li>
     *   <li>与数据库 DECIMAL 类型天然映射，保证三层（DB-Java-前端）一致</li>
     * </ul>
     * <p>可为 null（如免费景点无价格），表示“无参考价格”。</p>
     */
    private BigDecimal price;

    /**
     * 综合评分
     *
     * <p>对应数据库列 <code>score</code>，类型 <code>DECIMAL(3,2)</code>。</p>
     * <p>选用 {@link BigDecimal} 原因：</p>
     * <ul>
     *   <li>评分通常为 0.00 ~ 5.00 或 0.00 ~ 10.00，保留两位小数，需要精确表示</li>
     *   <li>double 类型在累加平均时可能产生微小误差，BigDecimal 保证统计准确性</li>
     * </ul>
     * <p>可为 null，表示“暂无评分”。</p>
     */
    private BigDecimal score;

    /**
     * 标签集合
     *
     * <p>对应数据库列 <code>tags</code>，类型 <code>VARCHAR</code>。</p>
     * <p>以逗号分隔的标签字符串，如“美食,网红,亲子”。
     * 前端可拆分为标签数组进行展示。若标签体系复杂，可考虑独立标签关联表进行规范化。</p>
     */
    private String tags;

    /**
     * 经度
     *
     * <p>对应数据库列 <code>longitude</code>，类型 <code>DECIMAL(10,7)</code>。</p>
     * <p>选用 {@link BigDecimal} 原因：</p>
     * <ul>
     *   <li>地理坐标精度要求极高（7 位小数约对应 1 厘米精度），double 精度虽足够但可能产生微小误差</li>
     *   <li>BigDecimal 可精确保存和传递经纬度，避免在数据库与 Java 之间发生精度截断</li>
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
     * 排序序号
     *
     * <p>对应数据库列 <code>sort_no</code>，类型 <code>INT</code>，默认值为 0。</p>
     * <p>用于前端列表展示时的自定义排序，数值越小排序越靠前。
     * 使用 {@link Integer} 包装类，允许 NULL 表示“未设置排序”从而使用默认逻辑。</p>
     */
    private Integer sortNo;

    /**
     * 点位状态
     *
     * <p>对应数据库列 <code>status</code>，类型 <code>INT</code>，默认值为 1，非空。</p>
     * <p>状态语义（业务约定）：</p>
     * <ul>
     *   <li><code>0</code> — 禁用/隐藏：点位不在地图展示，不可搜索到</li>
     *   <li><code>1</code> — 启用/显示：点位正常展示，可参与搜索和推荐</li>
     * </ul>
     * <p>使用 {@link Integer} 包装类，区分 NULL 与 0 的语义。</p>
     */
    private Integer status;

    /**
     * 获取点位唯一标识
     *
     * @return 点位 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置点位唯一标识
     *
     * @param id 点位 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取点位名称
     *
     * @return 点位名称字符串
     */
    public String getPointName() {
        return pointName;
    }

    /**
     * 设置点位名称
     *
     * @param pointName 点位名称，如“西湖断桥”
     */
    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    /**
     * 获取点位类型
     *
     * @return 点位类型字符串，如 "HOTEL"、"SCENIC" 等
     */
    public String getPointType() {
        return pointType;
    }

    /**
     * 设置点位类型
     *
     * @param pointType 点位类型字符串
     */
    public void setPointType(String pointType) {
        this.pointType = pointType;
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
     * 获取详细地址
     *
     * @return 详细地址字符串
     */
    public String getAddress() {
        return address;
    }

    /**
     * 设置详细地址
     *
     * @param address 详细地址字符串
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 获取点位描述
     *
     * @return 描述文本
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置点位描述
     *
     * @param description 描述文本
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取参考价格
     *
     * @return 参考价格（BigDecimal，精确到分），可能为 null
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * 设置参考价格
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
     * 获取标签集合
     *
     * @return 逗号分隔的标签字符串，如“美食,网红”
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
     * @param sortNo 排序序号
     */
    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    /**
     * 获取点位状态
     *
     * @return 状态码：0=禁用，1=启用
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置点位状态
     *
     * @param status 状态码：0=禁用，1=启用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}
