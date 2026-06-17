package com.tourism.mapper;

import com.tourism.entity.Hotel;

import java.util.List;

public interface HotelMapper {
    List<Hotel> selectByCity(String city);

    List<Hotel> selectAll();

    Hotel selectById(Long id);

    int insert(Hotel hotel);

    int updateById(Hotel hotel);

    int deleteById(Long id);
}
