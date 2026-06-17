package com.tourism.service;

import com.tourism.entity.Hotel;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.Ticket;
import com.tourism.entity.User;

import java.util.List;

public interface AdminResourceService {
    List<User> listUsers();

    User updateUser(User user);

    void updateUserStatus(Long id, Integer status);

    List<ScenicSpot> listScenicSpots();

    ScenicSpot saveScenicSpot(ScenicSpot scenicSpot);

    ScenicSpot updateScenicSpot(ScenicSpot scenicSpot);

    void deleteScenicSpot(Long id);

    List<Hotel> listHotels();

    Hotel saveHotel(Hotel hotel);

    Hotel updateHotel(Hotel hotel);

    void deleteHotel(Long id);

    List<Ticket> listTickets();

    Ticket saveTicket(Ticket ticket);

    Ticket updateTicket(Ticket ticket);

    void deleteTicket(Long id);
}
