package com.tourism.mapper;

import com.tourism.entity.HotelRoom;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HotelRoomMapper {
    List<HotelRoom> selectByHotelId(Long hotelId);

    HotelRoom selectById(Long id);

    int deductStock(@Param("id") Long id);

    int restoreStock(@Param("id") Long id);
}
