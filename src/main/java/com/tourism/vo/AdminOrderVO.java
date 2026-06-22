package com.tourism.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理员订单视图对象（VO - View Object）
 *
 * <p>VO 模式说明：
 * VO（视图对象）是分层架构中专门用于向视图层（前端）展示数据的对象。
 * 与DTO（数据传输对象）和Entity（数据库实体）的区别：
 * <ul>
 *   <li>Entity：对应数据库表结构，字段与数据库列一一对应，包含持久化注解</li>
 *   <li>DTO：用于层间数据传输，封装前端提交或接口返回的数据，可能包含计算字段</li>
 *   <li>VO：专门为前端展示设计，可聚合多个Entity的字段，或包含格式化后的数据，不直接对应数据库表</li>
 * </ul></p>
 *
 * <p>本类用于管理员后台的订单管理页面，聚合了酒店订单和门票订单的公共字段，
 * 并额外包含用户名信息（来自用户表），避免前端多次查询。</p>
 *
 * <p>核心字段：
 * <ul>
 *   <li>id：订单ID</li>
 *   <li>userId：用户ID</li>
 *   <li>username：用户名（关联用户表查询，方便管理员识别）</li>
 *   <li>orderType：订单类型（HOTEL/TICKET）</li>
 *   <li>itemName：商品名称（酒店名或景点名）</li>
 *   <li>totalAmount：订单总金额</li>
 *   <li>orderStatus：订单状态</li>
 *   <li>useDate：使用日期</li>
 *   <li>createTime：订单创建时间</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class AdminOrderVO {

    private Long id;
    // id：订单唯一标识，聚合后的订单ID
    // 注意：酒店订单和门票订单可能来自不同的数据库表，其ID可能重复，
    // 因此实际项目中可能需要额外标识（如 type+id 组合）来唯一确定一条记录

    private Long userId;
    // userId：下单用户的唯一标识，关联 user 表的主键
    // 管理员可通过此ID查看用户详情或联系用户

    private String username;
    // username：下单用户的用户名/昵称，来自 user 表的关联查询
    // 在VO中直接包含用户名，避免前端为展示"下单用户"列而额外调用用户详情接口
    // 这是VO的典型设计：聚合关联数据，减少前端请求次数

    private String orderType;
    // orderType：订单类型，标识这是酒店订单还是门票订单
    // 常见取值："HOTEL"（酒店订单）、"TICKET"（门票订单）
    // 管理员后台可根据此字段渲染不同的订单详情链接或操作按钮

    private String itemName;
    // itemName：商品名称，对于酒店订单是酒店名称+房型，对于门票订单是景点名称+票型
    // 例如："北京饭店 - 豪华大床房" 或 "故宫博物院 - 成人票"
    // 将商品名称聚合为一个字符串，简化前端展示逻辑

    private BigDecimal totalAmount;
    // totalAmount：订单总金额，java.math.BigDecimal 类型
    // 使用BigDecimal确保金额计算精度，避免浮点误差
    // 例如：599.00 表示五百九十九元。管理员后台可据此进行营收统计

    private String orderStatus;
    // orderStatus：订单当前状态
    // 常见取值："PENDING"（待支付）、"PAID"（已支付）、"USED"（已使用）、"CANCELLED"（已取消）、"REFUNDED"（已退款）
    // 管理员后台可根据状态进行筛选、排序、批量操作（如批量确认、批量退款）

    private LocalDate useDate;
    // useDate：使用日期，即订单的实际消费日期
    // 对于酒店订单是入住日期，对于门票订单是游览日期
    // 使用LocalDate而非LocalDateTime，是因为只关心日期不关心具体时间
    // 管理员后台可据此查询"今天需入住的订单"或"明天需接待的游客"

    private LocalDateTime createTime;
    // createTime：订单创建时间，包含日期和时间
    // 使用LocalDateTime记录精确到秒的创建时间，用于排序（最新订单在前）和时效判断（如30分钟未支付自动取消）
    // 管理员后台展示格式通常为：yyyy-MM-dd HH:mm:ss

    public Long getId() {
        // 返回订单ID
        return id;
    }

    public void setId(Long id) {
        // 设置订单ID
        this.id = id;
    }

    public Long getUserId() {
        // 返回用户ID
        return userId;
    }

    public void setUserId(Long userId) {
        // 设置用户ID
        this.userId = userId;
    }

    public String getUsername() {
        // 返回用户名
        return username;
    }

    public void setUsername(String username) {
        // 设置用户名
        this.username = username;
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
        // 返回使用日期
        return useDate;
    }

    public void setUseDate(LocalDate useDate) {
        // 设置使用日期
        this.useDate = useDate;
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
