package com.tourism.service.impl;

import com.tourism.entity.Hotel;
import com.tourism.entity.HotelRoom;
import com.tourism.mapper.HotelMapper;
import com.tourism.mapper.HotelOrderMapper;
import com.tourism.mapper.HotelRoomMapper;
import com.tourism.service.HotelService;
import com.tourism.vo.HotelRoomAvailabilityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 酒店服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入三个 Mapper：
 * {@link HotelMapper} 用于酒店主体查询，{@link HotelRoomMapper} 用于房型查询，
 * {@link HotelOrderMapper} 用于计算已锁定订单数量（房态互斥）。
 * 职责：为 C 端用户提供酒店列表查询、房型查询、动态房态查询服务。
 * 本类方法均为只读查询，不涉及数据库写操作，因此不声明 {@link org.springframework.transaction.annotation.Transactional}。
 * 动态房态计算是本系统的核心业务逻辑：房态不预存，而是实时根据"未取消订单的日期重叠"计算可用量。
 * </p>
 *
 * @author Tourism System
 * @see HotelService
 */
@Service
public class HotelServiceImpl implements HotelService {

    /**
     * 酒店数据访问 Mapper，自动注入。负责酒店主体的查询。
     */
    @Autowired
    private HotelMapper hotelMapper;

    /**
     * 房型数据访问 Mapper，自动注入。负责酒店房型的查询。
     */
    @Autowired
    private HotelRoomMapper hotelRoomMapper;

    /**
     * 酒店订单数据访问 Mapper，自动注入。负责统计某房型在日期范围内被未取消订单锁定的数量。
     */
    @Autowired
    private HotelOrderMapper hotelOrderMapper;

    /**
     * 按城市查询酒店列表。
     * <p>
     * 调用 {@code hotelMapper.selectByCity(city)} 执行条件查询（SQL WHERE city = ?），
     * 返回该城市下的所有酒店列表，用于前端酒店列表页展示。
     * 城市匹配规则由 Mapper 层 SQL 决定（通常为精确匹配或 LIKE 模糊匹配）。
     * </p>
     *
     * @param city 目标城市名称
     * @return 该城市下的 {@link Hotel} 列表
     */
    @Override
    public List<Hotel> listByCity(String city) {
        return hotelMapper.selectByCity(city); // Mapper 执行 SELECT * FROM hotel WHERE city = ?
    }

    /**
     * 查询指定酒店下的所有房型列表。
     * <p>
     * 调用 {@code hotelRoomMapper.selectByHotelId(hotelId)} 执行条件查询（SQL WHERE hotel_id = ?），
     * 返回该酒店的所有房型基础信息（不含动态房态计算），用于酒店详情页展示房型列表。
     * </p>
     *
     * @param hotelId 酒店主键 ID
     * @return 该酒店下的 {@link HotelRoom} 房型列表
     */
    @Override
    public List<HotelRoom> listRooms(Long hotelId) {
        return hotelRoomMapper.selectByHotelId(hotelId); // Mapper 执行 SELECT * FROM hotel_room WHERE hotel_id = ?
    }

    /**
     * 查询指定酒店在指定日期范围内的动态房态。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code hotelRoomMapper.selectByHotelId(hotelId)} 查询该酒店下所有房型（SQL WHERE hotel_id = ?）；
     * 2. 对每个房型，调用 {@link #buildAvailability(HotelRoom, LocalDate, LocalDate)} 构造动态房态视图；
     * 3. 使用 Stream 收集结果并返回列表。
     * 动态房态的核心计算逻辑在 buildAvailability 方法中实现。
     * </p>
     *
     * @param hotelId      酒店主键 ID
     * @param checkInDate  计划入住日期
     * @param checkOutDate 计划离店日期
     * @return 各房型的动态房态视图对象 {@link HotelRoomAvailabilityVO} 列表
     */
    @Override
    public List<HotelRoomAvailabilityVO> listRooms(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        return hotelRoomMapper.selectByHotelId(hotelId).stream()
                .map(room -> buildAvailability(room, checkInDate, checkOutDate)) // 逐房型计算动态房态
                .collect(Collectors.toList());
    }

    /**
     * 构造单个房型的动态房态视图对象。
     * <p>
     * 业务逻辑步骤：
     * 1. 创建 {@link HotelRoomAvailabilityVO} 对象，将房型基础字段（id, hotelId, roomType, price, stock, roomStatus）复制到 VO；
     * 2. 将入住日期和离店日期设置到 VO，用于前端展示；
     * 3. 提取总库存：若 room.getStock() 为 null，则视为 0（防止空指针异常）；
     * 4. 判断日期范围是否有效：入住日期和离店日期均不为 null，且离店日期晚于入住日期；
     * 5. 若日期有效，调用 {@code hotelOrderMapper.countActiveOverlap(roomId, checkInDate, checkOutDate)} 统计该房型在
     *    指定日期范围内被未取消订单锁定的数量（SQL 查询日期重叠且 order_status != 'CANCELLED' 的订单数）；
     *    若日期无效，则锁定数量视为 0；
     * 6. 计算可用数量：availableCount = max(0, stock - lockedCount)，确保不会为负数；
     * 7. 将锁定数量和可用数量设置到 VO；
     * 8. 判断可用状态：available = availableCount > 0 且 roomStatus 为 "AVAILABLE"（忽略大小写）；
     * 9. 根据可用状态生成前端展示的文本和状态码：
     *    - 若日期范围无效，status = "UNKNOWN"，text = "选择入住/离店日期后可查看房态"；
     *    - 若可用状态为 false（availableCount <= 0 或 roomStatus 非 AVAILABLE），status = "FULL"，text = "所选日期已满房，建议更换日期或房型"；
     *    - 若可用数量 <= 2（紧张但还有少量），status = "TIGHT"，text = "房态紧张，仅余 X 间"；
     *    - 否则，status = "AVAILABLE"，text = "可订 X 间"。
     * 该设计原因：将复杂的房态互斥逻辑封装在 Service 层，前端只需根据 status 和 text 做展示，无需关心业务规则。
     * </p>
     *
     * @param room         房型实体
     * @param checkInDate  计划入住日期
     * @param checkOutDate 计划离店日期
     * @return 组装好的 {@link HotelRoomAvailabilityVO} 动态房态视图
     */
    private HotelRoomAvailabilityVO buildAvailability(HotelRoom room, LocalDate checkInDate, LocalDate checkOutDate) {
        HotelRoomAvailabilityVO vo = new HotelRoomAvailabilityVO();
        // 步骤1：复制基础字段，VO 用于前端展示，Entity 用于数据库存储
        vo.setId(room.getId());
        vo.setHotelId(room.getHotelId());
        vo.setRoomType(room.getRoomType());
        vo.setPrice(room.getPrice());
        vo.setStock(room.getStock());
        vo.setRoomStatus(room.getRoomStatus());
        // 步骤2：设置日期范围
        vo.setCheckInDate(checkInDate);
        vo.setCheckOutDate(checkOutDate);

        // 步骤3：空安全处理，stock 为 null 时视为 0
        int stock = room.getStock() == null ? 0 : room.getStock();
        // 步骤4：判断日期范围是否有效（入住日期 < 离店日期）
        boolean validDateRange = checkInDate != null && checkOutDate != null && checkOutDate.isAfter(checkInDate);
        // 步骤5：若日期有效，查询该房型在日期范围内被未取消订单锁定的数量；否则锁定数为 0
        int lockedCount = validDateRange ? hotelOrderMapper.countActiveOverlap(room.getId(), checkInDate, checkOutDate) : 0;
        // 步骤6：计算可用数量，确保不会小于 0
        int availableCount = Math.max(0, stock - lockedCount);

        // 步骤7：设置锁定和可用数量
        vo.setLockedCount(lockedCount);
        vo.setAvailableCount(availableCount);
        // 步骤8：判断可用状态：必须有可用房间且房型状态为 AVAILABLE
        vo.setAvailable(availableCount > 0 && "AVAILABLE".equalsIgnoreCase(room.getRoomStatus()));
        // 步骤9：根据状态生成前端展示文本
        if (!validDateRange) {
            // 日期范围无效，提示用户选择日期
            vo.setAvailabilityStatus("UNKNOWN");
            vo.setAvailabilityText("选择入住/离店日期后可查看房态");
        } else if (!Boolean.TRUE.equals(vo.getAvailable())) {
            // 无可用房间或房型状态不可用
            vo.setAvailabilityStatus("FULL");
            vo.setAvailabilityText("所选日期已满房，建议更换日期或房型");
        } else if (availableCount <= 2) {
            // 可用但紧张，制造紧迫感促使用户尽快下单
            vo.setAvailabilityStatus("TIGHT");
            vo.setAvailabilityText("房态紧张，仅余 " + availableCount + " 间");
        } else {
            // 充足可用
            vo.setAvailabilityStatus("AVAILABLE");
            vo.setAvailabilityText("可订 " + availableCount + " 间");
        }
        return vo;
    }
}
