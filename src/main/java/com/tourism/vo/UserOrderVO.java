package com.tourism.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户订单视图对象（VO - View Object）
 *
 * <p>VO 模式说明：
 * VO（视图对象）是专门为前端展示层设计的对象，用于封装控制器返回给前端的聚合数据。
 * 与Entity（数据库实体）的区别：Entity对应数据库表结构（如HotelOrder对应酒店订单表，
 * TicketOrder对应门票订单表），VO可聚合多个Entity的字段并统一格式，不直接映射单一数据库表。
 * 本类聚合了酒店订单和门票订单的公共字段，为"我的订单"页面提供统一的数据结构。</p>
 *
 * <p>核心设计：
 * 系统中存在两类订单（酒店订单 HotelOrder、门票订单 TicketOrder），分别存储在不同的数据库表中。
 * 若直接返回实体列表，前端需要处理两种不同的数据结构，增加复杂性。
 * UserOrderVO 通过聚合两类订单的公共字段（如订单ID、类型、金额、状态、创建时间）和特有字段
 * （如酒店的入住/退房日期、门票的游览日期/数量），统一为单一结构，使前端只需处理一种数据格式。</p>
 *
 * <p>核心字段：
 * <ul>
 *   <li>id：订单ID</li>
 *   <li>orderType：订单类型（HOTEL/TICKET），前端据此渲染不同卡片样式</li>
 *   <li>itemName：商品名称（酒店名+房型 或 景点名+票型）</li>
 *   <li>totalAmount：订单总金额</li>
 *   <li>orderStatus：订单状态（待支付/已支付/已使用/已取消等）</li>
 *   <li>useDate：使用日期（通用字段，酒店为入住日期，门票为游览日期）</li>
 *   <li>checkInDate/checkOutDate：酒店订单特有的入住/退房日期</li>
 *   <li>visitDate：门票订单特有的游览日期</li>
 *   <li>quantity：门票订单特有的购买数量</li>
 *   <li>createTime：订单创建时间</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class UserOrderVO {

    private Long id;
    // id：订单唯一标识，聚合后的订单ID
    // 注意：酒店订单和门票订单的ID可能来自不同的自增序列，存在重复可能。
    // 实际项目中通常结合 orderType 字段构成唯一标识（如"HOTEL_123"、"TICKET_456"），
    // 或添加额外的全局唯一ID字段（如UUID）。

    private String orderType;
    // orderType：订单类型，标识这是酒店订单还是门票订单
    // 常见取值："HOTEL"（酒店订单）、"TICKET"（门票订单）
    // 前端"我的订单"页面根据此字段渲染不同的订单卡片样式：
    // - HOTEL：展示酒店图片、房型、入住日期、退房日期
    // - TICKET：展示景点图片、票型、游览日期、数量
    // 同时，订单详情页的跳转链接也根据此字段区分：/order/hotel/detail 或 /order/ticket/detail

    private String itemName;
    // itemName：商品名称，聚合了酒店订单和门票订单的商品描述
    // 对于酒店订单：通常为"酒店名称 - 房型名称"，如"北京饭店 - 豪华大床房"
    // 对于门票订单：通常为"景点名称 - 票型名称"，如"故宫博物院 - 成人票"
    // 此字段由业务层在查询时拼接生成，避免前端为展示商品名称而进行多次关联查询

    private BigDecimal totalAmount;
    // totalAmount：订单总金额，java.math.BigDecimal 类型
    // 使用BigDecimal确保金额精度，避免浮点数误差
    // 对于酒店订单：totalAmount = 房间单价 * 住宿天数（checkOutDate - checkInDate）
    // 对于门票订单：totalAmount = 门票单价 * 购买数量（quantity）
    // 前端展示格式通常为：¥599.00，在订单卡片中突出显示

    private String orderStatus;
    // orderStatus：订单当前状态，业务层会将不同实体的状态码统一映射为通用状态
    // 常见取值："PENDING"（待支付）、"PAID"（已支付）、"CONFIRMED"（已确认）、
    // "USED"（已使用/已入住）、"CANCELLED"（已取消）、"REFUNDED"（已退款）
    // 前端根据状态渲染不同颜色的标签和操作按钮：
    // - PENDING：橙色标签 + "去支付"按钮
    // - PAID：蓝色标签 + "查看详情"按钮
    // - USED：绿色标签 + "去评价"按钮
    // - CANCELLED：灰色标签（无操作）

    private LocalDate useDate;
    // useDate：使用日期（通用字段），对两类订单含义不同：
    // - 酒店订单：useDate = checkInDate（入住日期）
    // - 门票订单：useDate = visitDate（游览日期）
    // 提供此通用字段，便于前端统一按"使用日期"排序和展示
    // 例如："2024-06-15"表示酒店入住日或景点游览日

    private LocalDate checkInDate;
    // checkInDate：酒店订单特有的入住日期
    // 仅当 orderType = "HOTEL" 时有效，orderType = "TICKET" 时通常为 null
    // 前端在酒店订单卡片中展示为"入住：2024-06-15"
    // 与 checkOutDate 组合可计算住宿天数：days = checkOutDate - checkInDate

    private LocalDate checkOutDate;
    // checkOutDate：酒店订单特有的退房日期
    // 仅当 orderType = "HOTEL" 时有效
    // 前端在酒店订单卡片中展示为"退房：2024-06-18"
    // 住宿天数 = checkOutDate - checkInDate（如3天2晚）

    private LocalDate visitDate;
    // visitDate：门票订单特有的游览日期
    // 仅当 orderType = "TICKET" 时有效，orderType = "HOTEL" 时通常为 null
    // 前端在门票订单卡片中展示为"游览日期：2024-06-15"
    // 业务层通常会校验：visitDate 不能早于当前日期（过期门票不可使用）

    private Integer quantity;
    // quantity：门票订单特有的购买数量
    // 仅当 orderType = "TICKET" 时有效，如 2 表示购买了2张门票
    // 前端在门票订单卡片中展示为"数量：2张"
    // 对于酒店订单通常为 null 或 1（一间房），因为酒店订单按"间"而非"张"计量
    // 使用Integer而非int，是因为酒店订单可能不设置此字段（保持null）

    private LocalDateTime createTime;
    // createTime：订单创建时间，包含日期和时分秒
    // 使用LocalDateTime精确到秒，用于订单列表排序（最新订单在前）
    // 前端展示格式通常为：2024-06-01 14:30:25
    // 同时用于时效判断：如待支付订单超过30分钟自动取消（通过 createTime 计算是否超时）

    public Long getId() {
        // 返回订单ID
        return id;
    }

    public void setId(Long id) {
        // 设置订单ID
        this.id = id;
    }

    public String getOrderType() {
        // 返回订单类型
        return orderType;
    }

    public void setOrderType(String orderType) {
        // 设置订单类型
        this.orderType = orderType;
    }

    public String getItemName() {
        // 返回商品名称
        return itemName;
    }

    public void setItemName(String itemName) {
        // 设置商品名称
        this.itemName = itemName;
    }

    public BigDecimal getTotalAmount() {
        // 返回订单总金额
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        // 设置订单总金额
        this.totalAmount = totalAmount;
    }

    public String getOrderStatus() {
        // 返回订单状态
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        // 设置订单状态
        this.orderStatus = orderStatus;
    }

    public LocalDate getUseDate() {
        // 返回通用使用日期
        return useDate;
    }

    public void setUseDate(LocalDate useDate) {
        // 设置通用使用日期
        this.useDate = useDate;
    }

    public LocalDate getCheckInDate() {
        // 返回酒店入住日期
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        // 设置酒店入住日期
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        // 返回酒店退房日期
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        // 设置酒店退房日期
        this.checkOutDate = checkOutDate;
    }

    public LocalDate getVisitDate() {
        // 返回门票游览日期
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        // 设置门票游览日期
        this.visitDate = visitDate;
    }

    public Integer getQuantity() {
        // 返回门票购买数量
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        // 设置门票购买数量
        this.quantity = quantity;
    }

    public LocalDateTime getCreateTime() {
        // 返回订单创建时间
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        // 设置订单创建时间
        this.createTime = createTime;
    }
}
