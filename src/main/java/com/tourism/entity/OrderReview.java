package com.tourism.entity;

import java.time.LocalDateTime;

/**
 * 订单评价实体类（OrderReview Entity）
 *
 * <p>本类为 POJO（Plain Old Java Object），对应数据库表 <code>order_review</code>，
 * 用于存储用户对已完成订单的评价信息（如酒店订单评价、门票订单评价等）。
 * 通过私有无参字段 + 公共 getter/setter 满足 JavaBean 规范，
 * 支持 Spring 与 MyBatis 的反射注入机制。</p>
 *
 * <p>数据库表设计说明：</p>
 * <ul>
 *   <li>表名：order_review</li>
 *   <li>主键：id（自增 BIGINT），唯一标识一条评价记录</li>
 *   <li>无物理外键，通过 user_id 与 order_id 逻辑关联其他实体</li>
 *   <li>order_type + order_id 的组合设计，支持多种订单类型的统一评价（ polymorphic association ）</li>
 * </ul>
 *
 * <p>与其他实体的关系：</p>
 * <ul>
 *   <li>逻辑关联 {@link User}（通过 userId）：标识评价人</li>
 *   <li>逻辑关联 {@link HotelOrder} 或 {@link TicketOrder}（通过 orderType + orderId）：
     *   <ul>
     *     <li>当 orderType = "HOTEL" 时，orderId 指向 hotel_order.id</li>
     *     <li>当 orderType = "TICKET" 时，orderId 指向 ticket_order.id</li>
     *   </ul>
     *   这种设计为多态关联，避免为每种订单类型单独建评价表。
     * </li>
 * </ul>
 *
 * @author Tourism System
 * @version 1.0
 */
public class OrderReview {

    /**
     * 评价唯一标识（主键）
     *
     * <p>对应数据库列 <code>id</code>，类型 <code>BIGINT</code>，非空，自增。</p>
     * <p>使用 {@link Long} 包装类，避免 NULL 映射为基本类型 0 的歧义。</p>
     */
    private Long id;

    /**
     * 评价用户 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>user_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>逻辑关联 {@link User} 表的 <code>id</code> 主键，表示“谁发表的评价”。
     * 业务层需保证：一个用户只能对同一订单评价一次，防止重复评价。
     * 数据库层面建议为 user_id + order_id 的组合建立唯一索引。</p>
     */
    private Long userId;

    /**
     * 订单类型
     *
     * <p>对应数据库列 <code>order_type</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>订单类型语义（业务约定，多态关联标识）：</p>
     * <ul>
     *   <li><code>"HOTEL"</code> — 酒店订单评价，此时 orderId 指向 hotel_order 表</li>
     *   <li><code>"TICKET"</code> — 门票订单评价，此时 orderId 指向 ticket_order 表</li>
     * </ul>
     * <p>使用字符串存储，便于后续扩展新的订单类型（如 "ROUTE" 线路订单）。</p>
     */
    private String orderType;

    /**
     * 订单 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>order_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>与 orderType 联合使用，共同确定被评价的具体订单。
     * 例如：orderType="HOTEL" 且 orderId=123 表示对酒店订单 123 的评价。
     * 这种“类型+ID”的多态关联模式避免了为每种订单类型单独建立评价表，提高了代码复用率。</p>
     */
    private Long orderId;

    /**
     * 评分
     *
     * <p>对应数据库列 <code>score</code>，类型 <code>INT</code>，非空。</p>
     * <p>通常采用 1 ~ 5 分的五星评分体系，也可扩展为 1 ~ 10 分。
     * 使用 {@link Integer} 包装类，避免基本类型默认 0 分导致的语义歧义（0 分可能表示“未评价”而非“最低分”）。</p>
     * <p>业务层需校验：score 必须在 1 到 5 之间（或 1 到 10 之间）。</p>
     */
    private Integer score;

    /**
     * 评价内容
     *
     * <p>对应数据库列 <code>content</code>，类型 <code>TEXT</code> 或 <code>VARCHAR</code>。</p>
     * <p>用户填写的文字评价，如“房间很干净，服务很周到”。
     * 前端提交时应对长度做限制（如最多 500 字），后端需做敏感词过滤与 XSS 防护。</p>
     */
    private String content;

    /**
     * 评价创建时间
     *
     * <p>对应数据库列 <code>create_time</code>，类型 <code>DATETIME</code>，非空。</p>
     * <p>选用 {@link LocalDateTime} 原因：</p>
     * <ul>
     *   <li>精确到“年-月-日 时:分:秒”，记录评价提交的完整时间戳</li>
     *   <li>Java 8 新日期时间 API，线程安全，设计优于旧版 java.util.Date</li>
     *   <li>MyBatis 原生支持映射到数据库 DATETIME 类型</li>
     * </ul>
     * <p>通常由数据库 <code>CURRENT_TIMESTAMP</code> 或业务层在插入时自动填充。</p>
     */
    private LocalDateTime createTime;

    /**
     * 获取评价唯一标识
     *
     * @return 评价 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置评价唯一标识
     *
     * @param id 评价 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取评价用户 ID
     *
     * @return 用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置评价用户 ID
     *
     * @param userId 用户 ID，逻辑关联 user 表
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取订单类型
     *
     * @return 订单类型字符串，如 "HOTEL"、"TICKET"
     */
    public String getOrderType() {
        return orderType;
    }

    /**
     * 设置订单类型
     *
     * @param orderType 订单类型字符串，决定 orderId 指向哪个业务表
     */
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    /**
     * 获取订单 ID
     *
     * @return 订单 ID
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * 设置订单 ID
     *
     * @param orderId 订单 ID，与 orderType 联合确定具体订单
     */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /**
     * 获取评分
     *
     * @return 评分值，如 1 ~ 5 的整数
     */
    public Integer getScore() {
        return score;
    }

    /**
     * 设置评分
     *
     * @param score 评分值，需在业务允许范围内
     */
    public void setScore(Integer score) {
        this.score = score;
    }

    /**
     * 获取评价内容
     *
     * @return 评价文本内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置评价内容
     *
     * @param content 评价文本内容，需做长度与敏感词校验
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取评价创建时间
     *
     * @return 评价创建时间（LocalDateTime）
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置评价创建时间
     *
     * @param createTime 评价创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
