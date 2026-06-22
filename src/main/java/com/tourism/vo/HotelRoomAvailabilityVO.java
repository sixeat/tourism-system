package com.tourism.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 酒店房间可用性视图对象（VO - View Object）
 *
 * <p>VO 模式说明：
 * VO（视图对象）是专门为前端展示层设计的对象，用于封装控制器返回给前端的聚合数据。
 * 与Entity（数据库实体）的区别：Entity对应数据库表结构，VO可聚合多个Entity字段并包含计算属性，
 * 不直接映射数据库表，专为前端展示需求服务。例如本类包含的availableCount、available、
 * availabilityStatus、availabilityText等字段均为计算属性，数据库中不存在对应列。</p>
 *
 * <p>本类封装了酒店房间在指定日期范围内的可用性信息，包括库存、价格、锁定状态、
 * 实际可售数量、可用性文本等前端预订页面所需的全部展示数据。相比直接返回HotelRoom实体，
 * VO额外包含了日期范围、计算后的可用数量和状态文本，减少了前端计算逻辑。</p>
 *
 * <p>核心字段：
 * <ul>
 *   <li>id/roomType/price：房间基本信息（来自HotelRoom实体）</li>
 *   <li>stock/lockedCount：库存与锁定数量（用于计算可用性）</li>
 *   <li>availableCount：实际可售数量（计算属性：stock - lockedCount）</li>
 *   <li>available：是否可预订（布尔计算属性）</li>
 *   <li>availabilityStatus/availabilityText：前端展示用的状态码和文本</li>
 *   <li>checkInDate/checkOutDate：查询的日期范围</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class HotelRoomAvailabilityVO {

    private Long id;
    // id：房间唯一标识，对应数据库 hotel_room 表的主键
    // 前端提交订单时，需将此ID作为 roomId 传入 HotelOrderCreateRequest

    private Long hotelId;
    // hotelId：所属酒店ID，对应数据库 hotel 表的主键
    // 用于前端确认房间与酒店的归属关系，防止数据错乱

    private String roomType;
    // roomType：房型名称，如"豪华大床房"、"标准双人间"、"家庭套房"
    // 直接展示在用户预订页面的房型选择区域

    private BigDecimal price;
    // price：房间单价（每晚），java.math.BigDecimal 类型
    // 使用BigDecimal确保金额精度。订单总金额 = price * 住宿天数
    // 前端展示格式通常为：¥599.00/晚

    private Integer stock;
    // stock：房间总库存（该房型在酒店的房间总数）
    // 例如：stock=10 表示酒店有10间该类型的房间

    private String roomStatus;
    // roomStatus：房间状态，来自数据库配置
    // 常见取值："ONLINE"（在售）、"OFFLINE"（下架）、"MAINTENANCE"（维护中）
    // 若状态非"ONLINE"，即使availableCount>0也不允许预订

    private Integer lockedCount;
    // lockedCount：在指定日期范围内已被锁定/预订的房间数量
    // 计算逻辑：查询 hotel_room_lock 或 order 表中日期重叠的已预订/已锁定记录数
    // 例如：2024-06-01至2024-06-03期间，该房型已有5间被预订，则lockedCount=5

    private Integer availableCount;
    // availableCount：实际可售数量，计算属性：availableCount = stock - lockedCount
    // 例如：stock=10，lockedCount=5，则availableCount=5，表示还可预订5间
    // 若availableCount <= 0，前端应显示"已售罄"并禁用预订按钮
    // 此字段不由数据库存储，由业务层查询后计算并设置到VO中

    private Boolean available;
    // available：是否可预订，布尔计算属性
    // 计算逻辑：available = (availableCount > 0) && "ONLINE".equals(roomStatus)
    // 前端可直接根据此布尔值判断预订按钮是否可点击（true-可预订，false-不可预订）
    // 使用Boolean包装类型而非boolean基本类型，允许null值（表示尚未计算）

    private String availabilityStatus;
    // availabilityStatus：可用性状态码，供前端逻辑判断使用
    // 常见取值："AVAILABLE"（充足）、"LIMITED"（紧张，如仅剩1-2间）、"SOLD_OUT"（售罄）
    // 前端可根据不同状态码渲染不同颜色标签（绿色-充足、橙色-紧张、红色-售罄）

    private String availabilityText;
    // availabilityText：可用性状态文本，直接展示给用户
    // 例如："库存充足"、"仅剩2间"、"已售罄"、"房间维护中"
    // 由业务层根据availableCount和roomStatus生成，避免前端拼接字符串

    private LocalDate checkInDate;
    // checkInDate：查询的入住日期，即本次可用性查询的日期范围起始
    // 前端将此日期回显在预订页面，提示用户"以下为2024-06-15至2024-06-18的房间可用性"

    private LocalDate checkOutDate;
    // checkOutDate：查询的退房日期，即本次可用性查询的日期范围结束
    // 与checkInDate共同确定住宿天数：days = checkOutDate - checkInDate
    // 可用性计算需遍历此日期范围内每一天的库存，取最小可用数为最终availableCount

    public Long getId() {
        // 返回房间ID
        return id;
    }

    public void setId(Long id) {
        // 设置房间ID
        this.id = id;
    }

    public Long getHotelId() {
        // 返回酒店ID
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        // 设置酒店ID
        this.hotelId = hotelId;
    }

    public String getRoomType() {
        // 返回房型名称
        return roomType;
    }

    public void setRoomType(String roomType) {
        // 设置房型名称
        this.roomType = roomType;
    }

    public BigDecimal getPrice() {
        // 返回房间单价
        return price;
    }

    public void setPrice(BigDecimal price) {
        // 设置房间单价
        this.price = price;
    }

    public Integer getStock() {
        // 返回总库存
        return stock;
    }

    public void setStock(Integer stock) {
        // 设置总库存
        this.stock = stock;
    }

    public String getRoomStatus() {
        // 返回房间状态
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        // 设置房间状态
        this.roomStatus = roomStatus;
    }

    public Integer getLockedCount() {
        // 返回已锁定/已预订数量
        return lockedCount;
    }

    public void setLockedCount(Integer lockedCount) {
        // 设置已锁定/已预订数量
        this.lockedCount = lockedCount;
    }

    public Integer getAvailableCount() {
        // 返回实际可售数量（计算属性）
        return availableCount;
    }

    public void setAvailableCount(Integer availableCount) {
        // 设置实际可售数量（由业务层计算后注入）
        this.availableCount = availableCount;
    }

    public Boolean getAvailable() {
        // 返回是否可预订（计算属性）
        return available;
    }

    public void setAvailable(Boolean available) {
        // 设置是否可预订（由业务层根据库存和状态计算后注入）
        this.available = available;
    }

    public String getAvailabilityStatus() {
        // 返回可用性状态码
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        // 设置可用性状态码
        this.availabilityStatus = availabilityStatus;
    }

    public String getAvailabilityText() {
        // 返回可用性状态文本
        return availabilityText;
    }

    public void setAvailabilityText(String availabilityText) {
        // 设置可用性状态文本
        this.availabilityText = availabilityText;
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
