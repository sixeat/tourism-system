package com.tourism.service.impl;

import com.tourism.common.BusinessException;
import com.tourism.dto.FavoriteRequest;
import com.tourism.entity.UserFavorite;
import com.tourism.mapper.UserFavoriteMapper;
import com.tourism.service.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {

    private static final List<String> SUPPORTED_TYPES = Arrays.asList("ROUTE", "HOTEL", "SCENIC");

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Override
    public List<UserFavorite> list(Long userId) {
        return userFavoriteMapper.selectByUserId(userId);
    }

    @Override
    public void add(Long userId, FavoriteRequest request) {
        if (request == null || isBlank(request.getFavoriteType()) || isBlank(request.getTargetId()) || isBlank(request.getTitle())) {
            throw new BusinessException("收藏信息不完整");
        }
        String favoriteType = request.getFavoriteType().trim().toUpperCase();
        String targetId = request.getTargetId().trim();
        if (!SUPPORTED_TYPES.contains(favoriteType)) {
            throw new BusinessException("不支持的收藏类型");
        }
        if (userFavoriteMapper.selectOne(userId, favoriteType, targetId) != null) {
            return;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setFavoriteType(favoriteType);
        favorite.setTargetId(targetId);
        favorite.setTitle(request.getTitle().trim());
        favorite.setDescription(isBlank(request.getDescription()) ? "" : request.getDescription().trim());
        userFavoriteMapper.insert(favorite);
    }

    @Override
    public void remove(Long userId, String favoriteType, String targetId) {
        if (isBlank(favoriteType) || isBlank(targetId)) {
            throw new BusinessException("收藏信息不完整");
        }
        userFavoriteMapper.deleteOne(userId, favoriteType.trim().toUpperCase(), targetId.trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
