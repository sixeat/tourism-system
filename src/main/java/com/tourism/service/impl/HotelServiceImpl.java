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

@Service
public class HotelServiceImpl implements HotelService {

    @Autowired
    private HotelMapper hotelMapper;

    @Autowired
    private HotelRoomMapper hotelRoomMapper;

    @Autowired
    private HotelOrderMapper hotelOrderMapper;

    @Override
    public List<Hotel> listByCity(String city) {
        return hotelMapper.selectByCity(city);
    }

    @Override
    public List<HotelRoom> listRooms(Long hotelId) {
        return hotelRoomMapper.selectByHotelId(hotelId);
    }

    @Override
    public List<HotelRoomAvailabilityVO> listRooms(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        return hotelRoomMapper.selectByHotelId(hotelId).stream()
                .map(room -> buildAvailability(room, checkInDate, checkOutDate))
                .collect(Collectors.toList());
    }

    private HotelRoomAvailabilityVO buildAvailability(HotelRoom room, LocalDate checkInDate, LocalDate checkOutDate) {
        HotelRoomAvailabilityVO vo = new HotelRoomAvailabilityVO();
        vo.setId(room.getId());
        vo.setHotelId(room.getHotelId());
        vo.setRoomType(room.getRoomType());
        vo.setPrice(room.getPrice());
        vo.setStock(room.getStock());
        vo.setRoomStatus(room.getRoomStatus());
        vo.setCheckInDate(checkInDate);
        vo.setCheckOutDate(checkOutDate);

        int stock = room.getStock() == null ? 0 : room.getStock();
        boolean validDateRange = checkInDate != null && checkOutDate != null && checkOutDate.isAfter(checkInDate);
        int lockedCount = validDateRange ? hotelOrderMapper.countActiveOverlap(room.getId(), checkInDate, checkOutDate) : 0;
        int availableCount = Math.max(0, stock - lockedCount);

        vo.setLockedCount(lockedCount);
        vo.setAvailableCount(availableCount);
        vo.setAvailable(availableCount > 0 && "AVAILABLE".equalsIgnoreCase(room.getRoomStatus()));
        if (!validDateRange) {
            vo.setAvailabilityStatus("UNKNOWN");
            vo.setAvailabilityText("选择入住/离店日期后可查看房态");
        } else if (!Boolean.TRUE.equals(vo.getAvailable())) {
            vo.setAvailabilityStatus("FULL");
            vo.setAvailabilityText("所选日期已满房，建议更换日期或房型");
        } else if (availableCount <= 2) {
            vo.setAvailabilityStatus("TIGHT");
            vo.setAvailabilityText("房态紧张，仅余 " + availableCount + " 间");
        } else {
            vo.setAvailabilityStatus("AVAILABLE");
            vo.setAvailabilityText("可订 " + availableCount + " 间");
        }
        return vo;
    }
}
