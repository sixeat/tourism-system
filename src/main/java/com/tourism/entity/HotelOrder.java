package com.tourism.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 酒店订单实体类（HotelOrder Entity）
 *
 * <p>本类为 POJO（Plain Old Java Object），对应数据库表 <code>hotel_order</code>，
 * 是 MyBatis 与 Spring 框架进行 ORM 映射的核心载体。通过私有无参字段 + 公共 getter/setter
 * 满足 JavaBean 规范，支持 Spring 的 BeanWrapper 以及 MyBatis 的 MetaObject 反射注入。</p>
 *
 * <p>数据库表设计说明：</p>
 * <ul>
 *   <li>表名：hotel_order</li>
 *   <li>主键：id（自增 BIGINT），确保订单全局唯一</li>
 *   <li>无物理外键，所有关联字段（user_id、hotel_id、room_id）均为逻辑外键，
 *       在业务层与数据库索引层保证数据一致性</li>
 * </ul>
 *
 * <p>与其他实体的关系：</p>
 * <ul>
 *   <li>逻辑关联 {@link User}（通过 userId）：标识订单所属用户</li>
 *   <li>逻辑关联 {@link Hotel}（通过 hotelId）：标识订单所属酒店</li>
 *   <li>逻辑关联 {@link HotelRoom}（通过 roomId）：标识订单所预订的具体房型</li>
 * </ul>
 *
 * @author Tourism System
 * @version 1.0
 */
public class HotelOrder {

    /**
     * 订单唯一标识（主键）
     *
     * <p>对应数据库列 <code>id</code>，类型 <code>BIGINT</code>，非空，自增。</p>
     * <p>使用 {@link Long} 包装类，避免 MyBatis 映射 NULL 时基本类型产生 0 的歧义。</p>
     */
    private Long id;

    /**
     * 下单用户 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>user_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>该字段逻辑关联 {@link User} 表的 <code>id</code> 主键，表示“谁下的订单”。
     * 虽然数据库中未设置物理外键约束（便于分库分表与分布式扩展），
     * 但业务层应保证 user_id 对应的用户记录存在，且在数据库中应为该列建立索引以加速查询。</p>
     */
    private Long userId;

    /**
     * 酒店 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>hotel_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>逻辑关联 {@link Hotel} 表的 <code>id</code> 主键，表示“预订的是哪家酒店”。
     * 业务层需校验该酒店是否存在且处于上架（status=1）状态。</p>
     */
    private Long hotelId;

    /**
     * 房型 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>room_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>逻辑关联 {@link HotelRoom} 表的 <code>id</code> 主键，表示“预订的是哪个房型”。
     * 同一酒店下可存在多种房型，该字段精确定位到具体房间类型。</p>
     */
    private Long roomId;

    /**
     * 入住日期
     *
     * <p>对应数据库列 <code>check_in_date</code>，类型 <code>DATE</code>，非空。</p>
     * <p>选用 {@link LocalDate} 而非 {@link java.util.Date} 或 {@link java.sql.Date}，原因如下：</p>
     * <ul>
     *   <li>Java 8 日期时间 API 线程安全，不可变对象，避免并发问题</li>
     *   <li>语义明确：仅包含“年-月-日”，不包含时分秒，符合酒店行业“入住日期”的业务习惯</li>
     *   <li>MyBatis 3.4.5+ 原生支持 JSR-310 类型映射，无需额外 TypeHandler</li>
     * </ul>
     */
    private LocalDate checkInDate;

    /**
     * 退房日期
     *
     * <p>对应数据库列 <code>check_out_date</code>，类型 <code>DATE</code>，非空。</p>
     * <p>与 checkInDate 同类型 {@link LocalDate}，表示客人计划退房的日期。
     * 业务层需校验：退房日期必须晚于入住日期，且入住天数不能超过业务上限。</p>
     */
    private LocalDate checkOutDate;

    /**
     * 订单总金额
     *
     * <p>对应数据库列 <code>total_amount</code>，类型 <code>DECIMAL(10,2)</code>，非空。</p>
     * <p>选用 {@link BigDecimal} 而非 {@link Double} 或 {@link Float}，原因如下：</p>
     * <ul>
     *   <li>浮点类型（double/float）在二进制表示中存在精度误差，不适合货币计算；
     *       例如 0.1 + 0.2 在 double 中不等于 0.3，会导致财务误差</li>
     *   <li>{@link BigDecimal} 支持任意精度十进制运算，可精确表示金额到“分”</li>
     *   <li>数据库 DECIMAL(10,2) 与 BigDecimal 天然映射，避免类型转换损失</li>
     * </ul>
     * <p>计算逻辑：totalAmount = 房型单价 × 入住天数 × 房间数量 − 优惠金额。</p>
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态
     *
     * <p>对应数据库列 <code>order_status</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>状态语义（业务约定，建议枚举值）：</p>
     * <ul>
     *   <li><code>"PENDING"</code> — 待支付：订单已创建，尚未完成支付</li>
     *   <li><code>"PAID"</code> — 已支付：用户完成支付，等待酒店确认</li>
     *   <li><code>"CONFIRMED"</code> — 已确认：酒店已确认接单，预订生效</li>
     *   <li><code>"COMPLETED"</code> — 已完成：用户已入住并退房</li>
     *   <li><code>"CANCELLED"</code> — 已取消：订单被取消（超时未支付或用户主动取消）</li>
     *   <li><code>"REFUNDED"</code> — 已退款：已支付订单完成退款</li>
     * </ul>
     * <p>使用字符串而非数值，增强可读性，便于前后端直接透传。</p>
     */
    private String orderStatus;

    /**
     * 订单创建时间
     *
     * <p>对应数据库列 <code>create_time</code>，类型 <code>DATETIME</code>（或 <code>TIMESTAMP</code>），非空。</p>
     * <p>选用 {@link LocalDateTime} 原因：</p>
     * <ul>
     *   <li>精确到“年-月-日 时:分:秒”，满足订单创建时间戳的精度需求</li>
     *   <li>Java 8 新日期时间 API，线程安全，设计优于旧版 java.util.Date</li>
     *   <li>MyBatis 原生支持映射到数据库 DATETIME 类型</li>
     * </ul>
     * <p>通常由数据库默认值 <code>CURRENT_TIMESTAMP</code> 或业务层在插入时自动填充。</p>
     */
    private LocalDateTime createTime;

    /**
     * 获取订单唯一标识
     *
     * @return 订单 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置订单唯一标识
     *
     * @param id 订单 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取下单用户 ID
     *
     * @return 用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置下单用户 ID
     *
     * @param userId 用户 ID，逻辑关联 user 表
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取酒店 ID
     *
     * @return 酒店 ID
     */
    public Long getHotelId() {
        return hotelId;
    }

    /**
     * 设置酒店 ID
     *
     * @param hotelId 酒店 ID，逻辑关联 hotel 表
     */
    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    /**
     * 获取房型 ID
     *
     * @return 房型 ID
     */
    public Long getRoomId() {
        return roomId;
    }

    /**
     * 设置房型 ID
     *
     * @param roomId 房型 ID，逻辑关联 hotel_room 表
     */
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    /**
     * 获取入住日期
     *
     * @return 入住日期（LocalDate，仅含年月日）
     */
    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    /**
     * 设置入住日期
     *
     * @param checkInDate 入住日期，业务层需校验不早于当日
     */
    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    /**
     * 获取退房日期
     *
     * @return 退房日期
     */
    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    /**
     * 设置退房日期
     *
     * @param checkOutDate 退房日期，必须晚于入住日期
     */
    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    /**
     * 获取订单总金额
     *
     * @return 订单总金额（BigDecimal，精确到分）
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * 设置订单总金额
     *
     * @param totalAmount 订单总金额，使用 BigDecimal 防止浮点精度误差
     */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * 获取订单状态
     *
     * @return 订单状态字符串，如 "PENDING"、"PAID" 等
     */
    public String getOrderStatus() {
        return orderStatus;
    }

    /**
     * 设置订单状态
     *
     * @param orderStatus 订单状态字符串
     */
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    /**
     * 获取订单创建时间
     *
     * @return 订单创建时间（LocalDateTime，精确到秒）
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置订单创建时间
     *
     * @param createTime 订单创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
