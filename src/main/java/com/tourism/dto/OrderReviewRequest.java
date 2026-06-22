package com.tourism.dto;

/**
 * 订单评价请求数据传输对象（DTO - Data Transfer Object）
 *
 * <p>DTO 模式说明：
 * DTO（数据传输对象）用于封装前端提交到后端的数据，实现接口层与持久层的解耦。
 * 本类封装了用户提交订单评价所需的全部参数，前端以JSON格式提交，
 * 后端通过@RequestBody反序列化为本DTO，再调用服务层处理评价保存逻辑。</p>
 *
 * <p>核心字段：
 * <ul>
 *   <li>orderType：订单类型（标识是酒店订单还是门票订单）</li>
 *   <li>orderId：订单ID（被评价的具体订单）</li>
 *   <li>score：评分（通常为1-5星）</li>
 *   <li>content：评价内容（文字描述）</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class OrderReviewRequest {

    private String orderType;
    // orderType：订单类型，标识被评价的是哪种订单
    // 常见取值："hotel"（酒店订单）、"ticket"（门票订单）
    // 使用String类型而非枚举，便于后续扩展其他订单类型（如"route"路线订单）而无需修改枚举定义
    // 业务层会根据orderType路由到对应的订单表，校验订单是否存在且属于当前用户

    private Long orderId;
    // orderId：订单唯一标识，表示要评价的是哪个订单
    // 对应数据库 hotel_order 或 ticket_order 表的主键
    // 业务层会校验：订单是否存在、是否属于当前用户、订单状态是否为"已完成"（通常只有完成后才能评价）

    private Integer score;
    // score：评分分数，通常为1-5的整数（对应1星到5星）
    // 使用Integer类型而非int，是因为该字段可能为null（如用户只提交文字评价而未选星级），
    // 且允许null值更灵活。业务层会校验score范围：1 <= score <= 5

    private String content;
    // content：评价文字内容，用户输入的详细评价描述
    // 例如："酒店位置很好，交通便利，房间干净整洁，服务态度热情。"
    // 业务层通常会校验：内容长度不能过短（如至少5个字符）不能过长（如不超过500字），且过滤敏感词

    public String getOrderType() {
        // 返回订单类型
        return orderType;
    }

    public void setOrderType(String orderType) {
        // 设置订单类型
        this.orderType = orderType;
    }

    public Long getOrderId() {
        // 返回订单ID
        return orderId;
    }

    public void setOrderId(Long orderId) {
        // 设置订单ID
        this.orderId = orderId;
    }

    public Integer getScore() {
        // 返回评分分数
        return score;
    }

    public void setScore(Integer score) {
        // 设置评分分数
        this.score = score;
    }

    public String getContent() {
        // 返回评价文字内容
        return content;
    }

    public void setContent(String content) {
        // 设置评价文字内容
        this.content = content;
    }
}
