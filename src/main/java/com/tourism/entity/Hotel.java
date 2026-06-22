package com.tourism.entity;

/**
 * 酒店实体类（Hotel Entity）
 *
 * <p>本类采用 <strong>POJO（Plain Old Java Object）</strong> 模式，对应数据库表 <code>hotel</code>。
 * 在基于 Spring + MyBatis 的分层架构中，Entity 负责将数据库表的行记录映射为 Java 对象，
 * 实现 ORM（Object-Relational Mapping）的基础层。本类通过私有无参字段 + 公共 getter/setter
 * 的组合满足 JavaBean 规范，从而可以被 Spring 的 {@link org.springframework.beans.BeanWrapper}、
 * MyBatis 的 {@link org.apache.ibatis.reflection.MetaObject} 等反射工具自动注入属性。</p>
 *
 * <p>数据库表设计说明：</p>
 * <ul>
 *   <li>表名：hotel</li>
 *   <li>主键：id（自增 BIGINT，采用数据库原生自增策略，确保全局唯一且顺序递增）</li>
 *   <li>无物理外键约束，所有关联通过逻辑字段（如 user_id）在业务层保证一致性</li>
 * </ul>
 *
 * <p>与其他实体的关系：</p>
 * <ul>
 *   <li>一对多关联 {@link HotelRoom}：一个酒店可拥有多个房型（通过 hotelId 逻辑关联）</li>
 *   <li>一对多关联 {@link HotelOrder}：一个酒店可产生多个订单（通过 hotelId 逻辑关联）</li>
 * </ul>
 *
 * @author Tourism System
 * @version 1.0
 */
public class Hotel {

    /**
     * 酒店唯一标识（主键）
     *
     * <p>对应数据库列 <code>id</code>，类型 <code>BIGINT</code>，非空，自增。</p>
     * <p>选用包装类 {@link Long} 而非基本类型 long，原因如下：
     * <ol>
     *   <li>MyBatis 在结果集映射时，若数据库值为 NULL，基本类型会导致默认值为 0，造成语义歧义；
     *   <li>Spring 表单绑定与 MyBatis 动态 SQL 中，包装类可明确表达“未赋值”状态。</li>
     * </ol>
     */
    private Long id;

    /**
     * 酒店名称
     *
     * <p>对应数据库列 <code>hotel_name</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>该字段为酒店的核心展示属性，用于前端列表、搜索、详情页渲染。
     * 数据库层面通常设置长度限制（如 VARCHAR(128)），业务层应对输入做非空与长度校验。</p>
     */
    private String hotelName;

    /**
     * 所属城市
     *
     * <p>对应数据库列 <code>city</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于按地域维度筛选酒店，支持前端“城市搜索”与后台统计报表。
     * 统一使用城市名称全称（如“北京市”），避免“北京”与“北京市”的歧义。</p>
     */
    private String city;

    /**
     * 详细地址
     *
     * <p>对应数据库列 <code>address</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>存储酒店的具体街道位置，可用于地图定位、导航、配送等场景。
     * 通常长度较大（如 VARCHAR(256)），以容纳完整地址字符串。</p>
     */
    private String address;

    /**
     * 酒店星级/等级
     *
     * <p>对应数据库列 <code>level</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>常见取值如“五星级”、“四星级”、“经济型”等。
     * 使用字符串而非枚举是为了兼容不同业务线对等级的扩展定义（如“民宿”、“精品”等）。</p>
     */
    private String level;

    /**
     * 酒店描述/简介
     *
     * <p>对应数据库列 <code>description</code>，类型 <code>TEXT</code> 或 <code>VARCHAR</code>。</p>
     * <p>用于酒店详情页的图文介绍，可包含富文本或纯文本。
     * 数据库中若存储 HTML 内容，前端需做好 XSS 过滤。</p>
     */
    private String description;

    /**
     * 酒店状态
     *
     * <p>对应数据库列 <code>status</code>，类型 <code>INT</code>，默认值为 1，非空。</p>
     * <p>状态语义（业务约定）：</p>
     * <ul>
     *   <li><code>0</code> — 禁用/下架：酒店不在前端展示，不可预订</li>
     *   <li><code>1</code> — 启用/上架：酒店正常对外展示，可接受预订</li>
     * </ul>
     * <p>使用 {@link Integer} 包装类，确保数据库 NULL 值可被明确识别，避免基本类型默认 0 的误报。</p>
     */
    private Integer status;

    /**
     * 获取酒店唯一标识
     *
     * <p>JavaBean 规范 getter 方法，命名遵循 <code>getXxx</code> 模式。
     * Spring 与 MyBatis 均通过反射调用此方法读取属性值。</p>
     *
     * @return 酒店 ID，若未持久化则返回 null
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置酒店唯一标识
     *
     * <p>JavaBean 规范 setter 方法，命名遵循 <code>setXxx</code> 模式。
     * 在 MyBatis 结果集映射与 Spring 表单绑定中，框架通过反射调用此方法完成属性注入。</p>
     *
     * @param id 酒店唯一标识，通常由数据库生成，新增记录时前端无需传入
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取酒店名称
     *
     * @return 酒店名称字符串
     */
    public String getHotelName() {
        return hotelName;
    }

    /**
     * 设置酒店名称
     *
     * @param hotelName 酒店名称，建议长度不超过数据库 VARCHAR 限制
     */
    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    /**
     * 获取酒店所属城市
     *
     * @return 城市名称字符串
     */
    public String getCity() {
        return city;
    }

    /**
     * 设置酒店所属城市
     *
     * @param city 城市名称，如“杭州市”
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 获取酒店详细地址
     *
     * @return 详细地址字符串
     */
    public String getAddress() {
        return address;
    }

    /**
     * 设置酒店详细地址
     *
     * @param address 详细地址字符串，含街道门牌号
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 获取酒店星级/等级
     *
     * @return 星级描述字符串，如“五星级”
     */
    public String getLevel() {
        return level;
    }

    /**
     * 设置酒店星级/等级
     *
     * @param level 星级描述字符串
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * 获取酒店描述
     *
     * @return 酒店描述/简介文本
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置酒店描述
     *
     * @param description 酒店描述文本，可包含多行或 HTML 标签
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取酒店状态
     *
     * @return 状态码：0=禁用，1=启用
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置酒店状态
     *
     * @param status 状态码：0=禁用，1=启用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}
