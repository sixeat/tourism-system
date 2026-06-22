package com.tourism.entity;

import java.math.BigDecimal;

/**
 * 酒店房型实体类（HotelRoom Entity）
 *
 * <p>本类为 POJO（Plain Old Java Object），对应数据库表 <code>hotel_room</code>，
 * 承载酒店房型信息，作为 MyBatis 与 Spring 进行 ORM 映射的核心对象。
 * 私有无参字段 + 公共 getter/setter 的组合满足 JavaBean 规范，
 * 支持 Spring BeanWrapper 与 MyBatis 反射注入。</p>
 *
 * <p>数据库表设计说明：</p>
 * <ul>
 *   <li>表名：hotel_room</li>
 *   <li>主键：id（自增 BIGINT），唯一标识某一房型</li>
 *   <li>无物理外键，通过 hotel_id 逻辑关联 {@link Hotel} 表</li>
 * </ul>
 *
 * <p>与其他实体的关系：</p>
 * <ul>
 *   <li>多对一关联 {@link Hotel}（通过 hotelId）：一个酒店拥有多个房型，一个房型属于一个酒店</li>
 *   <li>被 {@link HotelOrder} 引用（通过 roomId）：一个房型可出现在多个订单中</li>
 * </ul>
 *
 * @author Tourism System
 * @version 1.0
 */
public class HotelRoom {

    /**
     * 房型唯一标识（主键）
     *
     * <p>对应数据库列 <code>id</code>，类型 <code>BIGINT</code>，非空，自增。</p>
     * <p>使用 {@link Long} 包装类，避免 NULL 映射为基本类型 0 造成的语义歧义。</p>
     */
    private Long id;

    /**
     * 所属酒店 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>hotel_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>逻辑关联 {@link Hotel} 表的 <code>id</code> 主键，表示该房型归属哪家酒店。
     * 数据库层面建议为 hotel_id 建立索引，以加速“查询某酒店下所有房型”的场景。</p>
     */
    private Long hotelId;

    /**
     * 房型名称
     *
     * <p>对应数据库列 <code>room_type</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于前端展示，如“豪华大床房”、“标准双床房”、“海景套房”等。
     * 不同酒店可存在同名的房型，但 hotel_id + room_type 的组合可唯一确定某一房型。</p>
     */
    private String roomType;

    /**
     * 房型单价（每晚）
     *
     * <p>对应数据库列 <code>price</code>，类型 <code>DECIMAL(10,2)</code>，非空。</p>
     * <p>选用 {@link BigDecimal} 原因：</p>
     * <ul>
     *   <li>货币金额必须精确计算，double/float 的二进制浮点表示存在精度误差（如 0.1 + 0.2 ≠ 0.3）</li>
     *   <li>{@link BigDecimal} 支持任意精度的十进制运算，可精确到“分”</li>
     *   <li>与数据库 DECIMAL(10,2) 类型天然映射，保证前后端与数据库三层一致</li>
     * </ul>
     * <p>该价格为每晚单价，订单总金额需结合入住天数计算。</p>
     */
    private BigDecimal price;

    /**
     * 库存数量（可预订房间数）
     *
     * <p>对应数据库列 <code>stock</code>，类型 <code>INT</code>，默认值为 0，非空。</p>
     * <p>表示该房型在特定日期范围内的可售房间数量。
     * 在实际业务中，库存可能按日期分片（如每日库存不同），
     * 此处为简化模型，采用统一库存；若需精细管理，可扩展为独立库存表。</p>
     * <p>使用 {@link Integer} 包装类，便于表达“未设置”或 NULL 状态。</p>
     */
    private Integer stock;

    /**
     * 房型状态
     *
     * <p>对应数据库列 <code>room_status</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>状态语义（业务约定，建议枚举值）：</p>
     * <ul>
     *   <li><code>"AVAILABLE"</code> — 可预订：房型正常对外销售</li>
     *   <li><code>"SOLD_OUT"</code> — 售罄：库存为 0，暂时不可预订</li>
     *   <li><code>"OFF_SHELF"</code> — 下架：业务人员主动下架，不可预订</li>
     *   <li><code>"MAINTENANCE"</code> — 维护中：酒店内部装修或维护，暂停销售</li>
     * </ul>
     * <p>使用字符串存储，增强可读性，方便前后端直接透传。</p>
     */
    private String roomStatus;

    /**
     * 获取房型唯一标识
     *
     * @return 房型 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置房型唯一标识
     *
     * @param id 房型 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取所属酒店 ID
     *
     * @return 酒店 ID
     */
    public Long getHotelId() {
        return hotelId;
    }

    /**
     * 设置所属酒店 ID
     *
     * @param hotelId 酒店 ID，逻辑关联 hotel 表
     */
    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    /**
     * 获取房型名称
     *
     * @return 房型名称字符串
     */
    public String getRoomType() {
        return roomType;
    }

    /**
     * 设置房型名称
     *
     * @param roomType 房型名称，如“豪华大床房”
     */
    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    /**
     * 获取房型单价
     *
     * @return 每晚单价（BigDecimal，精确到分）
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * 设置房型单价
     *
     * @param price 每晚单价，使用 BigDecimal 保证货币精度
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * 获取库存数量
     *
     * @return 可预订房间数量
     */
    public Integer getStock() {
        return stock;
    }

    /**
     * 设置库存数量
     *
     * @param stock 可预订房间数量，负数无意义需校验
     */
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    /**
     * 获取房型状态
     *
     * @return 房型状态字符串，如 "AVAILABLE"、"SOLD_OUT" 等
     */
    public String getRoomStatus() {
        return roomStatus;
    }

    /**
     * 设置房型状态
     *
     * @param roomStatus 房型状态字符串
     */
    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }
}
