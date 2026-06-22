package com.tourism.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 门票实体类（Ticket Entity）
 *
 * <p>本类为 POJO（Plain Old Java Object），对应数据库表 <code>ticket</code>，
 * 用于存储景点门票的详细信息，是 MyBatis 与 Spring 进行 ORM 映射的核心载体。
 * 通过私有无参字段 + 公共 getter/setter 满足 JavaBean 规范，
 * 支持 Spring BeanWrapper 与 MyBatis 反射注入机制。</p>
 *
 * <p>数据库表设计说明：</p>
 * <ul>
 *   <li>表名：ticket</li>
 *   <li>主键：id（自增 BIGINT），唯一标识一种门票</li>
 *   <li>无物理外键，通过 scenic_id 逻辑关联 {@link ScenicSpot} 表</li>
 * </ul>
 *
 * <p>与其他实体的关系：</p>
 * <ul>
 *   <li>逻辑关联 {@link ScenicSpot}（通过 scenicId）：标识该门票属于哪个景点</li>
 *   <li>被 {@link TicketOrder} 引用（通过 ticketId）：一种门票可被多次购买，产生多个订单</li>
 * </ul>
 *
 * @author Tourism System
 * @version 1.0
 */
public class Ticket {

    /**
     * 门票唯一标识（主键）
     *
     * <p>对应数据库列 <code>id</code>，类型 <code>BIGINT</code>，非空，自增。</p>
     * <p>使用 {@link Long} 包装类，避免 NULL 映射为基本类型 0 的歧义。</p>
     */
    private Long id;

    /**
     * 所属景点 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>scenic_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>逻辑关联 {@link ScenicSpot} 表的 <code>id</code> 主键，表示“该门票属于哪个景点”。
     * 数据库层面建议为 scenic_id 建立索引，以加速“查询某景点的所有门票”的场景。</p>
     */
    private Long scenicId;

    /**
     * 门票名称
     *
     * <p>对应数据库列 <code>ticket_name</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>用于前端展示，如“成人票”、“学生票”、“夜场票”、“套票”等。
     * 同一景点下可存在多种门票，通过 ticket_name 区分。</p>
     */
    private String ticketName;

    /**
     * 门票单价
     *
     * <p>对应数据库列 <code>price</code>，类型 <code>DECIMAL(10,2)</code>，非空。</p>
     * <p>选用 {@link BigDecimal} 原因：</p>
     * <ul>
     *   <li>货币金额必须精确到分，double/float 的二进制浮点表示存在精度误差</li>
     *   <li>BigDecimal 支持任意精度十进制运算，保证财务计算准确</li>
     *   <li>与数据库 DECIMAL(10,2) 类型天然映射，避免类型转换损失</li>
     * </ul>
     * <p>该价格为单张门票价格，订单总金额需结合购买数量（quantity）计算。</p>
     */
    private BigDecimal price;

    /**
     * 库存数量（可售张数）
     *
     * <p>对应数据库列 <code>stock</code>，类型 <code>INT</code>，默认值为 0，非空。</p>
     * <p>表示该门票在 available_date 指定日期的可售数量。
     * 在并发场景下，库存扣减需采用数据库乐观锁（version 字段）或悲观锁（SELECT FOR UPDATE）防止超卖。</p>
     * <p>使用 {@link Integer} 包装类，便于表达 NULL（未设置库存）状态。</p>
     */
    private Integer stock;

    /**
     * 可用日期
     *
     * <p>对应数据库列 <code>available_date</code>，类型 <code>DATE</code>，非空。</p>
     * <p>选用 {@link LocalDate} 原因：</p>
     * <ul>
     *   <li>Java 8 日期时间 API，线程安全，不可变对象</li>
     *   <li>语义明确：仅包含“年-月-日”，表示该门票的可用日期（如特定节日的限定票）</li>
     *   <li>MyBatis 3.4.5+ 原生支持 JSR-310 类型映射</li>
     * </ul>
     * <p>若门票长期有效，可用一个特殊日期（如 2099-12-31）表示，或单独设计有效期字段。</p>
     */
    private LocalDate availableDate;

    /**
     * 获取门票唯一标识
     *
     * @return 门票 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置门票唯一标识
     *
     * @param id 门票 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取所属景点 ID
     *
     * @return 景点 ID
     */
    public Long getScenicId() {
        return scenicId;
    }

    /**
     * 设置所属景点 ID
     *
     * @param scenicId 景点 ID，逻辑关联 scenic_spot 表
     */
    public void setScenicId(Long scenicId) {
        this.scenicId = scenicId;
    }

    /**
     * 获取门票名称
     *
     * @return 门票名称字符串，如“成人票”
     */
    public String getTicketName() {
        return ticketName;
    }

    /**
     * 设置门票名称
     *
     * @param ticketName 门票名称字符串
     */
    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    /**
     * 获取门票单价
     *
     * @return 门票单价（BigDecimal，精确到分）
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * 设置门票单价
     *
     * @param price 门票单价，使用 BigDecimal 保证货币精度
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * 获取库存数量
     *
     * @return 可售张数
     */
    public Integer getStock() {
        return stock;
    }

    /**
     * 设置库存数量
     *
     * @param stock 可售张数，需 ≥ 0，并发场景下需做扣减控制
     */
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    /**
     * 获取可用日期
     *
     * @return 可用日期（LocalDate，仅含年月日）
     */
    public LocalDate getAvailableDate() {
        return availableDate;
    }

    /**
     * 设置可用日期
     *
     * @param availableDate 可用日期，表示该门票的有效日期
     */
    public void setAvailableDate(LocalDate availableDate) {
        this.availableDate = availableDate;
    }
}
