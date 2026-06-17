package com.tourism.service.impl;

import com.tourism.common.BusinessException;
import com.tourism.dto.HotelOrderCreateRequest;
import com.tourism.dto.TicketOrderCreateRequest;
import com.tourism.entity.HotelOrder;
import com.tourism.entity.HotelRoom;
import com.tourism.entity.Ticket;
import com.tourism.entity.TicketOrder;
import com.tourism.mapper.HotelOrderMapper;
import com.tourism.mapper.HotelRoomMapper;
import com.tourism.mapper.TicketMapper;
import com.tourism.mapper.TicketOrderMapper;
import com.tourism.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private HotelRoomMapper hotelRoomMapper;

    @Autowired
    private HotelOrderMapper hotelOrderMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HotelOrder createHotelOrder(HotelOrderCreateRequest request) {
        if (request.getCheckInDate() == null || request.getCheckOutDate() == null
                || !request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("入住日期必须早于离店日期");
        }

        HotelRoom room = hotelRoomMapper.selectById(request.getRoomId());
        if (room == null) {
            throw new IllegalArgumentException("房型不存在");
        }
        if (room.getStock() == null || room.getStock() <= 0) {
            throw new IllegalStateException("该房型库存不足");
        }
        int lockedCount = hotelOrderMapper.countActiveOverlap(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );
        if (lockedCount >= room.getStock()) {
            throw new IllegalStateException("该房型在所选日期已满房，请更换房型或入住日期");
        }

        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal total = room.getPrice().multiply(BigDecimal.valueOf(nights));

        HotelOrder order = new HotelOrder();
        order.setUserId(request.getUserId());
        order.setHotelId(request.getHotelId());
        order.setRoomId(request.getRoomId());
        order.setCheckInDate(request.getCheckInDate());
        order.setCheckOutDate(request.getCheckOutDate());
        order.setTotalAmount(total);
        order.setOrderStatus("CREATED");
        hotelOrderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketOrder createTicketOrder(TicketOrderCreateRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("门票数量必须大于 0");
        }

        Ticket ticket = ticketMapper.selectById(request.getTicketId());
        if (ticket == null) {
            throw new IllegalArgumentException("门票不存在");
        }
        if (ticket.getStock() == null || ticket.getStock() < request.getQuantity()) {
            throw new IllegalStateException("门票库存不足");
        }

        int updated = ticketMapper.deductStock(request.getTicketId(), request.getQuantity());
        if (updated == 0) {
            throw new IllegalStateException("门票库存扣减失败，请稍后重试");
        }

        TicketOrder order = new TicketOrder();
        order.setUserId(request.getUserId());
        order.setTicketId(request.getTicketId());
        order.setVisitDate(request.getVisitDate());
        order.setQuantity(request.getQuantity());
        order.setTotalAmount(ticket.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setOrderStatus("CREATED");
        ticketOrderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelHotelOrder(Long orderId, Long userId) {
        HotelOrder order = hotelOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("酒店订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("没有权限取消该酒店订单");
        }
        if (!"CREATED".equals(order.getOrderStatus())) {
            throw new BusinessException("该酒店订单当前状态不可取消");
        }
        hotelOrderMapper.updateStatus(orderId, "CANCELLED");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTicketOrder(Long orderId, Long userId) {
        TicketOrder order = ticketOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("门票订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("没有权限取消该门票订单");
        }
        if (!"CREATED".equals(order.getOrderStatus())) {
            throw new BusinessException("该门票订单当前状态不可取消");
        }
        ticketOrderMapper.updateStatus(orderId, "CANCELLED");
        ticketMapper.restoreStock(order.getTicketId(), order.getQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payHotelOrder(Long orderId, Long userId) {
        HotelOrder order = hotelOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("酒店订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("没有权限支付该酒店订单");
        }
        if (!"CREATED".equals(order.getOrderStatus())) {
            throw new BusinessException("该酒店订单当前状态不可支付");
        }
        hotelOrderMapper.updateStatus(orderId, "PAID");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payTicketOrder(Long orderId, Long userId) {
        TicketOrder order = ticketOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("门票订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("没有权限支付该门票订单");
        }
        if (!"CREATED".equals(order.getOrderStatus())) {
            throw new BusinessException("该门票订单当前状态不可支付");
        }
        ticketOrderMapper.updateStatus(orderId, "PAID");
    }
}
