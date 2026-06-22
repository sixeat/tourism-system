package com.tourism.service.impl;

import com.tourism.entity.Ticket;
import com.tourism.mapper.TicketMapper;
import com.tourism.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 门票服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入 {@link TicketMapper}。
 * 职责：为 C 端用户提供按景点查询门票的服务，是景点详情页和门票预订页的基础数据支撑。
 * 方法为只读查询，不涉及数据库写操作，因此不声明 {@link org.springframework.transaction.annotation.Transactional}。
 * </p>
 *
 * @author Tourism System
 * @see TicketService
 */
@Service
public class TicketServiceImpl implements TicketService {

    /**
     * 门票数据访问 Mapper，自动注入。负责 ticket 表查询。
     */
    @Autowired
    private TicketMapper ticketMapper;

    /**
     * 按景点查询门票列表。
     * <p>
     * 调用 {@code ticketMapper.selectByScenicId(scenicId)} 执行条件查询（SQL WHERE scenic_id = ?），
     * 返回该景点下所有可用的门票（如成人票、学生票、套票等），
     * 用于前端景点详情页或门票预订页展示。
     * 返回的门票信息包含门票名称、价格、库存、可用日期等字段。
     * </p>
     *
     * @param scenicId 景点主键 ID
     * @return 该景点下的 {@link Ticket} 门票列表
     */
    @Override
    public List<Ticket> listByScenicId(Long scenicId) {
        return ticketMapper.selectByScenicId(scenicId); // Mapper 执行 SELECT * FROM ticket WHERE scenic_id = ?
    }
}
