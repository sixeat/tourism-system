package com.tourism.mapper;

import com.tourism.entity.Ticket;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TicketMapper {
    List<Ticket> selectAll();

    List<Ticket> selectByScenicId(Long scenicId);

    Ticket selectById(Long id);

    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    int restoreStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    int insert(Ticket ticket);

    int updateById(Ticket ticket);

    int deleteById(Long id);
}
