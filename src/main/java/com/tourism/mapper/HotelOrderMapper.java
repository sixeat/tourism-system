package com.tourism.mapper;

import com.tourism.entity.HotelOrder;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

public interface HotelOrderMapper {
    int insert(HotelOrder order);

    HotelOrder selectById(Long id);

    int updateStatus(@Param("id") Long id, @Param("orderStatus") String orderStatus);

    int countActiveOverlap(@Param("roomId") Long roomId,
                           @Param("checkInDate") LocalDate checkInDate,
                           @Param("checkOutDate") LocalDate checkOutDate);
}
