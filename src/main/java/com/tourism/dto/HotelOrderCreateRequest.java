package com.tourism.dto;

import java.time.LocalDate;

/**
 * 酒店订单创建请求数据传输对象（DTO - Data Transfer Object）
 *
 * <p>DTO 模式说明：
 * DTO（数据传输对象）用于封装前端提交到后端的数据，将接口契约与数据库实体分离。
 * 本类前端提交酒店预订信息，后端通过@RequestBody接收并反序列化，
 * 再转换为数据库实体（HotelOrder）进行持久化。这种分离可避免直接暴露实体字段，
 * 允许接口字段与数据库字段不同（如DTO可包含前端特有的字段，或省略敏感字段）。</p>
 *
 * <p>核心字段：
 * <ul>
 *   <li>userId：用户ID（由后端从Session填充，前端通常不传或传空）</li>
 *   <li>hotelId：酒店ID（标识预订的酒店）</li>
 *   <li>roomId：房间ID（标识预订的具体房型）</li>
 *   <li>checkInDate：入住日期（LocalDate类型，格式 yyyy-MM-dd）</li>
 *   <li>checkOutDate：退房日期（LocalDate类型，格式 yyyy-MM-dd）</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class HotelOrderCreateRequest {

    private Long userId;
    // userId：用户唯一标识，表示哪个用户提交了此订单
    // 注意：前端表单中通常不传递此字段，由控制器从HttpSession中获取当前登录用户ID并填充
    // 这样设计可防止前端伪造用户ID提交他人订单，确保订单归属安全

    private Long hotelId;
    // hotelId：酒店唯一标识，表示预订的是哪家酒店
    // 对应数据库 hotel 表的主键，业务层会校验该酒店是否存在且状态正常

    private Long roomId;
    // roomId：房间唯一标识，表示预订的是哪种房型
    // 对应数据库 hotel_room 表的主键，业务层会校验该房型是否属于指定酒店、库存是否充足

    private LocalDate checkInDate;
    // checkInDate：入住日期，java.time.LocalDate 类型
    // 前端以JSON字符串格式提交（如"2024-06-15"），Spring通过Jackson自动反序列化为LocalDate
    // 业务层会校验：入住日期不能早于当天，且入住日期必须早于退房日期

    private LocalDate checkOutDate;
    // checkOutDate：退房日期，java.time.LocalDate 类型
    // 格式同入住日期，业务层会校验：退房日期必须晚于入住日期，且住宿天数不能超过系统限制（如30天）
    // 实际应付金额 = 房间单价 * 住宿天数（checkOutDate - checkInDate）

    public Long getUserId() {
        // 返回用户ID
        return userId;
    }

    public void setUserId(Long userId) {
        // 设置用户ID，通常由控制器从Session中获取后注入
        this.userId = userId;
    }

    public Long getHotelId() {
        // 返回酒店ID
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        // 设置酒店ID
        this.hotelId = hotelId;
    }

    public Long getRoomId() {
        // 返回房间ID
        return roomId;
    }

    public void setRoomId(Long roomId) {
        // 设置房间ID
        this.roomId = roomId;
    }

    public LocalDate getCheckInDate() {
        // 返回入住日期
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        // 设置入住日期
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        // 返回退房日期
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        // 设置退房日期
        this.checkOutDate = checkOutDate;
    }
}
