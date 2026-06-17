package com.tourism.mapper;

import com.tourism.entity.TicketOrder;
import org.apache.ibatis.annotations.Param;

public interface TicketOrderMapper {
    int insert(TicketOrder order);

    TicketOrder selectById(Long id);

    int updateStatus(@Param("id") Long id, @Param("orderStatus") String orderStatus);
}
