package com.tourism.service;

import com.tourism.entity.Ticket;

import java.util.List;

/**
 * 门票服务接口（Service Contract）。
 * <p>
 * 本接口定义了门票信息查询的基础服务契约，面向 C 端用户（前台）。
 * 提供按景点查询门票功能，用于前端景点详情页展示该景点下的可用门票列表。
 * 实现类：{@link com.tourism.service.impl.TicketServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface TicketService {

    /**
     * 按景点查询门票列表。
     * <p>
     * 根据景点主键 ID 查询该景点下所有可用的门票（如成人票、学生票、套票等），
     * 用于前端景点详情页或门票预订页展示。
     * 返回的门票信息包含门票名称、价格、库存、可用日期等字段。
     * </p>
     *
     * @param scenicId 景点主键 ID，不可为 null
     * @return 该景点下的 {@link Ticket} 门票列表
     */
    List<Ticket> listByScenicId(Long scenicId);
}
