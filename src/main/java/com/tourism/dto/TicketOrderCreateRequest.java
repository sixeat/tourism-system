package com.tourism.dto;

import java.time.LocalDate;

/**
 * 门票订单创建请求数据传输对象（DTO - Data Transfer Object）
 *
 * <p>DTO 模式说明：
 * DTO（数据传输对象）用于封装前端提交到后端的数据，实现接口层与持久层的解耦。
 * 本类封装了用户购买景点门票时提交的订单参数，前端以JSON格式提交，
 * 后端通过@RequestBody反序列化为本DTO，控制器从Session填充userId后传入业务层创建订单。</p>
 *
 * <p>核心字段：
 * <ul>
 *   <li>userId：用户ID（由后端从Session填充，前端通常不传）</li>
 *   <li>ticketId：门票ID（标识购买的票种）</li>
 *   <li>visitDate：游览日期（LocalDate类型，计划哪天去景点）</li>
 *   <li>quantity：购买数量（Integer类型，至少1张）</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class TicketOrderCreateRequest {

    private Long userId;
    // userId：用户唯一标识，表示哪个用户提交了此门票订单
    // 注意：前端表单中通常不传递此字段，由控制器从HttpSession中获取当前登录用户ID并填充
    // 这样设计可防止前端伪造用户ID购买门票，确保订单归属安全和权限正确

    private Long ticketId;
    // ticketId：门票唯一标识，表示购买的是哪种门票
    // 对应数据库 ticket 表的主键，业务层会校验：门票是否存在、所属景点是否开放、库存是否充足
    // 例如：ticketId=1 可能对应"故宫博物院成人票"

    private LocalDate visitDate;
    // visitDate：计划游览日期，java.time.LocalDate 类型
    // 前端以JSON字符串格式提交（如"2024-06-15"），Spring通过Jackson自动反序列化为LocalDate
    // 业务层会校验：游览日期不能早于当天（通常需提前预订），且不能超过最大可预订天数（如30天内）
    // 库存计算也基于此日期：查询 ticket_stock 表中 ticket_id + visit_date 对应的库存记录

    private Integer quantity;
    // quantity：购买数量，Integer 类型，表示购买几张门票
    // 例如：2 表示购买2张。业务层会校验：quantity >= 1 且 quantity <= 剩余库存 且不超过单次购买上限（如10张）
    // 订单总金额 = ticket.price * quantity（由业务层计算）

    public Long getUserId() {
        // 返回用户ID
        return userId;
    }

    public void setUserId(Long userId) {
        // 设置用户ID，通常由控制器从Session中获取后注入
        this.userId = userId;
    }

    public Long getTicketId() {
        // 返回门票ID
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        // 设置门票ID
        this.ticketId = ticketId;
    }

    public LocalDate getVisitDate() {
        // 返回游览日期
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        // 设置游览日期
        this.visitDate = visitDate;
    }

    public Integer getQuantity() {
        // 返回购买数量
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        // 设置购买数量
        this.quantity = quantity;
    }
}
