package com.tourism.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门票订单实体类（TicketOrder Entity）
 *
 * <p>本类为 POJO（Plain Old Java Object），对应数据库表 <code>ticket_order</code>，
 * 用于存储用户购买景点门票的订单信息，是 MyBatis 与 Spring 进行 ORM 映射的核心载体。
 * 通过私有无参字段 + 公共 getter/setter 满足 JavaBean 规范，
 * 支持 Spring BeanWrapper 与 MyBatis 反射注入机制。</p>
 *
 * <p>数据库表设计说明：</p>
 * <ul>
 *   <li>表名：ticket_order</li>
 *   <li>主键：id（自增 BIGINT），唯一标识一个门票订单</li>
 *   <li>无物理外键，通过 user_id 与 ticket_id 逻辑关联其他实体</li>
 * </ul>
 *
 * <p>与其他实体的关系：</p>
 * <ul>
 *   <li>逻辑关联 {@link User}（通过 userId）：标识下单用户</li>
 *   <li>逻辑关联 {@link Ticket}（通过 ticketId）：标识购买的门票种类</li>
 *   <li>被 {@link OrderReview} 引用（通过 orderType + orderId）：订单完成后可接受用户评价</li>
 * </ul>
 *
 * @author Tourism System
 * @version 1.0
 */
public class TicketOrder {

    /**
     * 订单唯一标识（主键）
     *
     * <p>对应数据库列 <code>id</code>，类型 <code>BIGINT</code>，非空，自增。</p>
     * <p>使用 {@link Long} 包装类，避免 NULL 映射为基本类型 0 的歧义。</p>
     */
    private Long id;

    /**
     * 下单用户 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>user_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>逻辑关联 {@link User} 表的 <code>id</code> 主键，表示“谁下的订单”。
     * 数据库层面建议为 user_id 建立索引，以加速“查询某用户的所有订单”的场景。</p>
     */
    private Long userId;

    /**
     * 门票 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>ticket_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>逻辑关联 {@link Ticket} 表的 <code>id</code> 主键，表示“购买的是哪种门票”。
     * 通过 ticketId 可进一步获取景点信息（scenicId）、门票名称、单价等。</p>
     */
    private Long ticketId;

    /**
     * 游览日期/使用日期
     *
     * <p>对应数据库列 <code>visit_date</code>，类型 <code>DATE</code>，非空。</p>
     * <p>选用 {@link LocalDate} 原因：</p>
     * <ul>
     *   <li>Java 8 日期时间 API，线程安全，不可变对象</li>
     *   <li>语义明确：仅包含“年-月-日”，表示用户计划入园的日期</li>
     *   <li>MyBatis 3.4.5+ 原生支持 JSR-310 类型映射</li>
     * </ul>
     * <p>业务层需校验：visitDate 必须不早于当日（或根据业务规则允许提前/延后）。</p>
     */
    private LocalDate visitDate;

    /**
     * 购买数量
     *
     * <p>对应数据库列 <code>quantity</code>，类型 <code>INT</code>，非空。</p>
     * <p>表示用户购买的门票张数，通常限制上限（如每人最多 10 张）。
     * 使用 {@link Integer} 包装类，避免基本类型默认 0 张的语义歧义。</p>
     * <p>订单总金额 = ticket.price × quantity。</p>
     */
    private Integer quantity;

    /**
     * 订单总金额
     *
     * <p>对应数据库列 <code>total_amount</code>，类型 <code>DECIMAL(10,2)</code>，非空。</p>
     * <p>选用 {@link BigDecimal} 原因：</p>
     * <ul>
     *   <li>货币金额必须精确计算，double/float 存在二进制浮点精度误差</li>
     *   <li>BigDecimal 支持任意精度十进制运算，精确到“分”</li>
     *   <li>与数据库 DECIMAL(10,2) 类型天然映射，避免类型转换损失</li>
     * </ul>
     * <p>计算逻辑：totalAmount = ticket.price × quantity − 优惠金额。</p>
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态
     *
     * <p>对应数据库列 <code>order_status</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>状态语义（业务约定，建议枚举值）：</p>
     * <ul>
     *   <li><code>"PENDING"</code> — 待支付：订单已创建，尚未完成支付</li>
     *   <li><code>"PAID"</code> — 已支付：用户完成支付，等待出票</li>
     *   <li><code>"ISSUED"</code> — 已出票：电子票已生成，可凭票入园</li>
     *   <li><code>"USED"</code> — 已使用：用户已凭票入园，订单完成</li>
     *   <li><code>"CANCELLED"</code> — 已取消：订单被取消（超时未支付或用户主动取消）</li>
     *   <li><code>"REFUNDED"</code> — 已退款：已支付订单完成退款</li>
     *   <li><code>"EXPIRED"</code> — 已过期：门票在 visitDate 当日未使用，自动过期</li>
     * </ul>
     * <p>使用字符串而非数值，增强可读性，便于前后端直接透传。</p>
     */
    private String orderStatus;

    /**
     * 订单创建时间
     *
     * <p>对应数据库列 <code>create_time</code>，类型 <code>DATETIME</code>，非空。</p>
     * <p>选用 {@link LocalDateTime} 原因：</p>
     * <ul>
     *   <li>精确到“年-月-日 时:分:秒”，记录订单创建的完整时间戳</li>
     *   <li>Java 8 新日期时间 API，线程安全，设计优于旧版 java.util.Date</li>
     *   <li>MyBatis 原生支持映射到数据库 DATETIME 类型</li>
     * </ul>
     * <p>通常由数据库 <code>CURRENT_TIMESTAMP</code> 或业务层在插入时自动填充。</p>
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
     * 获取门票 ID
     *
     * @return 门票 ID
     */
    public Long getTicketId() {
        return ticketId;
    }

    /**
     * 设置门票 ID
     *
     * @param ticketId 门票 ID，逻辑关联 ticket 表
     */
    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * 获取游览日期
     *
     * @return 游览日期（LocalDate，仅含年月日）
     */
    public LocalDate getVisitDate() {
        return visitDate;
    }

    /**
     * 设置游览日期
     *
     * @param visitDate 游览日期，业务层需校验不早于当日
     */
    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    /**
     * 获取购买数量
     *
     * @return 购买张数
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * 设置购买数量
     *
     * @param quantity 购买张数，需 ≥ 1 且不超过业务上限
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
     * @return 订单创建时间（LocalDateTime）
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
