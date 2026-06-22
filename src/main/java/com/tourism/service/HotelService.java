package com.tourism.service;

import com.tourism.entity.Hotel;
import com.tourism.entity.HotelRoom;
import com.tourism.vo.HotelRoomAvailabilityVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 酒店服务接口（Service Contract）。
 * <p>
 * 本接口定义了酒店信息查询与房态查询的服务契约，面向 C 端用户（前台）。
 * 提供按城市查询酒店、查询酒店下所有房型、以及按入住/离店日期查询动态房态等功能。
 * 实现类：{@link com.tourism.service.impl.HotelServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface HotelService {

    /**
     * 按城市查询酒店列表。
     * <p>
     * 根据目标城市名称查询该城市下的所有酒店，用于前端酒店列表页展示。
     * 城市匹配通常采用精确匹配或模糊匹配，由 Mapper 层 SQL 决定。
     * </p>
     *
     * @param city 目标城市名称，如 "杭州"、"北京" 等
     * @return 该城市下的 {@link Hotel} 列表
     */
    List<Hotel> listByCity(String city);

    /**
     * 查询指定酒店下的所有房型列表。
     * <p>
     * 根据酒店 ID 查询该酒店的所有房型（如标准间、大床房、套房等），
     * 用于前端酒店详情页展示房型基础信息（不含动态房态）。
     * </p>
     *
     * @param hotelId 酒店主键 ID，不可为 null
     * @return 该酒店下的 {@link HotelRoom} 房型列表
     */
    List<HotelRoom> listRooms(Long hotelId);

    /**
     * 查询指定酒店在指定日期范围内的动态房态。
     * <p>
     * 根据酒店 ID、入住日期、离店日期，查询每个房型的实时可订情况。
     * 房态计算逻辑：总库存 - 日期重叠的未取消订单锁定数 = 可用数量。
     * 返回的视图对象包含可用数量、锁定数量、可用状态文本（如 "可订 5 间"、"房态紧张"、"已满房"）等。
     * </p>
     *
     * @param hotelId      酒店主键 ID，不可为 null
     * @param checkInDate  计划入住日期，不可为 null
     * @param checkOutDate 计划离店日期，必须晚于入住日期
     * @return 各房型的动态房态视图对象列表，元素类型为 {@link HotelRoomAvailabilityVO}
     */
    List<HotelRoomAvailabilityVO> listRooms(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate);
}
