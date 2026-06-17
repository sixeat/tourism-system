package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.entity.Hotel;
import com.tourism.entity.HotelRoom;
import com.tourism.service.HotelService;
import com.tourism.vo.HotelRoomAvailabilityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hotel")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @GetMapping("/list")
    public ApiResponse<List<Hotel>> list(@RequestParam String city) {
        return ApiResponse.success(hotelService.listByCity(city));
    }

    @GetMapping("/rooms")
    public ApiResponse<List<HotelRoom>> rooms(@RequestParam Long hotelId) {
        return ApiResponse.success(hotelService.listRooms(hotelId));
    }

    @GetMapping("/room-availability")
    public ApiResponse<List<HotelRoomAvailabilityVO>> roomAvailability(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {
        return ApiResponse.success(hotelService.listRooms(hotelId, checkInDate, checkOutDate));
    }
}
