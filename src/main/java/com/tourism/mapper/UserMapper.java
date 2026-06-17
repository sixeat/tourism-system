package com.tourism.mapper;

import com.tourism.entity.User;

import java.util.List;

public interface UserMapper {
    User selectByUsername(String username);

    User selectById(Long id);

    List<User> selectAll();

    int insert(User user);

    int updateProfile(User user);

    int updatePassword(User user);

    int updateByAdmin(User user);

    int updateStatus(User user);
}
