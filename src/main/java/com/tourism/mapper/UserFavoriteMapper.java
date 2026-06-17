package com.tourism.mapper;

import com.tourism.entity.UserFavorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserFavoriteMapper {
    List<UserFavorite> selectByUserId(Long userId);

    UserFavorite selectOne(@Param("userId") Long userId,
                           @Param("favoriteType") String favoriteType,
                           @Param("targetId") String targetId);

    int insert(UserFavorite favorite);

    int deleteOne(@Param("userId") Long userId,
                  @Param("favoriteType") String favoriteType,
                  @Param("targetId") String targetId);
}
