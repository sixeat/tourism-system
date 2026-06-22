package com.tourism.service;

import com.tourism.dto.FavoriteRequest;
import com.tourism.entity.UserFavorite;

import java.util.List;

/**
 * 用户收藏服务接口（Service Contract）。
 * <p>
 * 本接口定义了用户对旅游内容（路线、酒店、景点）进行收藏与取消收藏的服务契约。
 * 支持三种收藏类型：ROUTE（路线）、HOTEL（酒店）、SCENIC（景点）。
 * 同一用户对同一收藏对象不可重复收藏。
 * 实现类：{@link com.tourism.service.impl.UserFavoriteServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface UserFavoriteService {

    /**
     * 查询指定用户的收藏列表。
     * <p>
     * 根据用户 ID 查询该用户所有收藏记录，用于前端 "我的收藏" 页面展示。
     * 收藏列表按收藏时间倒序排列由 Mapper 层 SQL 控制。
     * </p>
     *
     * @param userId 用户主键 ID，不可为 null
     * @return 该用户的 {@link UserFavorite} 收藏列表
     */
    List<UserFavorite> list(Long userId);

    /**
     * 添加收藏。
     * <p>
     * 业务规则：
     * 1. 收藏类型、目标 ID、标题不可为空；
     * 2. 收藏类型仅支持 ROUTE、HOTEL、SCENIC 三种；
     * 3. 同一用户对同一目标不可重复收藏，若已存在则静默忽略（幂等）；
     * 4. 描述字段为空时存储空字符串。
     * 若违反规则，实现层应抛出 {@link com.tourism.common.BusinessException}。
     * </p>
     *
     * @param userId  当前登录用户主键 ID
     * @param request 收藏请求对象，包含收藏类型、目标 ID、标题、描述
     */
    void add(Long userId, FavoriteRequest request);

    /**
     * 移除收藏。
     * <p>
     * 根据用户 ID、收藏类型、目标 ID 删除对应的收藏记录。
     * 收藏类型和目标 ID 不可为空，删除前不做存在性校验（不存在亦不影响结果）。
     * 若参数为空，实现层应抛出 {@link com.tourism.common.BusinessException}。
     * </p>
     *
     * @param userId       当前登录用户主键 ID
     * @param favoriteType 收藏类型，如 "ROUTE"、"HOTEL"、"SCENIC"
     * @param targetId     收藏目标 ID，如路线 ID、酒店 ID、景点 ID
     */
    void remove(Long userId, String favoriteType, String targetId);
}
