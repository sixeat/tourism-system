package com.tourism.service;

import com.tourism.entity.Hotel;
import com.tourism.entity.HotelRoom;
import com.tourism.vo.HotelRoomAvailabilityVO;

import java.time.LocalDate;
import java.util.List;

public interface HotelService {
    List<Hotel> listByCity(String city);

    List<HotelRoom> listRooms(Long hotelId);

    List<HotelRoomAvailabilityVO> listRooms(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate);
}
