package com.tourism.service;

import com.tourism.dto.FavoriteRequest;
import com.tourism.entity.UserFavorite;

import java.util.List;

public interface UserFavoriteService {
    List<UserFavorite> list(Long userId);

    void add(Long userId, FavoriteRequest request);

    void remove(Long userId, String favoriteType, String targetId);
}
