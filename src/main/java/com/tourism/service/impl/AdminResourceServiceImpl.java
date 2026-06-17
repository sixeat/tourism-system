package com.tourism.service.impl;

import com.tourism.common.BusinessException;
import com.tourism.entity.Hotel;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.Ticket;
import com.tourism.entity.User;
import com.tourism.mapper.HotelMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.mapper.TicketMapper;
import com.tourism.mapper.UserMapper;
import com.tourism.service.AdminResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminResourceServiceImpl implements AdminResourceService {

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Autowired
    private HotelMapper hotelMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> listUsers() {
        List<User> users = userMapper.selectAll();
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() == null || userMapper.selectById(user.getId()) == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        userMapper.updateByAdmin(user);
        User updated = userMapper.selectById(user.getId());
        updated.setPassword(null);
        return updated;
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        User user = new User();
        user.setId(id);
        user.setStatus(status == null ? 1 : status);
        userMapper.updateStatus(user);
    }

    @Override
    public List<ScenicSpot> listScenicSpots() {
        return scenicSpotMapper.selectAll();
    }

    @Override
    public ScenicSpot saveScenicSpot(ScenicSpot scenicSpot) {
        scenicSpotMapper.insert(scenicSpot);
        return scenicSpot;
    }

    @Override
    public ScenicSpot updateScenicSpot(ScenicSpot scenicSpot) {
        if (scenicSpot.getId() == null || scenicSpotMapper.selectById(scenicSpot.getId()) == null) {
            throw new BusinessException("景点不存在");
        }
        scenicSpotMapper.updateById(scenicSpot);
        return scenicSpotMapper.selectById(scenicSpot.getId());
    }

    @Override
    public void deleteScenicSpot(Long id) {
        scenicSpotMapper.deleteById(id);
    }

    @Override
    public List<Hotel> listHotels() {
        return hotelMapper.selectAll();
    }

    @Override
    public Hotel saveHotel(Hotel hotel) {
        hotelMapper.insert(hotel);
        return hotel;
    }

    @Override
    public Hotel updateHotel(Hotel hotel) {
        if (hotel.getId() == null || hotelMapper.selectById(hotel.getId()) == null) {
            throw new BusinessException("酒店不存在");
        }
        hotelMapper.updateById(hotel);
        return hotelMapper.selectById(hotel.getId());
    }

    @Override
    public void deleteHotel(Long id) {
        hotelMapper.deleteById(id);
    }

    @Override
    public List<Ticket> listTickets() {
        return ticketMapper.selectAll();
    }

    @Override
    public Ticket saveTicket(Ticket ticket) {
        ticketMapper.insert(ticket);
        return ticket;
    }

    @Override
    public Ticket updateTicket(Ticket ticket) {
        if (ticket.getId() == null || ticketMapper.selectById(ticket.getId()) == null) {
            throw new BusinessException("门票不存在");
        }
        ticketMapper.updateById(ticket);
        return ticketMapper.selectById(ticket.getId());
    }

    @Override
    public void deleteTicket(Long id) {
        ticketMapper.deleteById(id);
    }
}
