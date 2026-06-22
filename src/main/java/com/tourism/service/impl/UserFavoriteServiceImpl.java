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

/**
 * 用户收藏服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入 {@link UserFavoriteMapper}。
 * 职责：为 C 端用户提供收藏查询、添加和移除功能。
 * 支持三种收藏类型：ROUTE（路线）、HOTEL（酒店）、SCENIC（景点），由 {@link #SUPPORTED_TYPES} 白名单控制。
 * 添加收藏时，若同一用户已收藏同一目标，则静默忽略（幂等），避免数据库插入重复记录。
 * 本类为单表操作，不涉及跨表事务，但包含先读后写逻辑，高并发下可能重复插入，
 * 数据库唯一索引（user_id + favorite_type + target_id）兜底保证数据一致性。
 * </p>
 *
 * @author Tourism System
 * @see UserFavoriteService
 */
@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {

    /**
     * 支持的收藏类型白名单。当前仅支持路线、酒店、景点三类收藏。
     * 使用 Arrays.asList 创建不可变列表，防止运行时被修改。
     */
    private static final List<String> SUPPORTED_TYPES = Arrays.asList("ROUTE", "HOTEL", "SCENIC");

    /**
     * 用户收藏数据访问 Mapper，自动注入。负责 user_favorite 表的 CRUD。
     */
    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    /**
     * 查询指定用户的收藏列表。
     * <p>
     * 调用 {@code userFavoriteMapper.selectByUserId(userId)} 查询该用户的收藏记录（SQL WHERE user_id = ? ORDER BY create_time DESC），
     * 返回列表用于前端 "我的收藏" 页面展示。排序规则由 Mapper 层 SQL 控制（通常按收藏时间降序）。
     * </p>
     *
     * @param userId 用户主键 ID
     * @return 该用户的 {@link UserFavorite} 收藏列表
     */
    @Override
    public List<UserFavorite> list(Long userId) {
        return userFavoriteMapper.selectByUserId(userId); // Mapper 执行 SELECT BY user_id，按时间降序
    }

    /**
     * 添加收藏。
     * <p>
     * 业务逻辑步骤：
     * 1. 参数完整性校验：若 request 为 null、收藏类型为空、目标 ID 为空或标题为空，
     *    抛出 {@link BusinessException} "收藏信息不完整"；
     * 2. 收藏类型规范化：去除首尾空格并转大写，统一为 "ROUTE"、"HOTEL"、"SCENIC"；
     * 3. 目标 ID 规范化：去除首尾空格，避免前后端空格不一致导致重复收藏判断失败；
     * 4. 收藏类型白名单校验：若类型不在 SUPPORTED_TYPES 中，抛出 {@link BusinessException} "不支持的收藏类型"；
     * 5. 重复收藏校验：调用 {@code userFavoriteMapper.selectOne(userId, favoriteType, targetId)} 查询是否已存在（SQL 复合条件查询），
     *    若已存在，直接 return 静默忽略，不抛出异常（幂等设计，前端重复点击无害）；
     * 6. 构造 {@link UserFavorite} 实体，设置用户 ID、收藏类型、目标 ID、标题（去除首尾空格）、描述（为空则存空字符串）；
     * 7. 调用 {@code userFavoriteMapper.insert(favorite)} 将收藏记录持久化到数据库（SQL INSERT）。
     * 幂等设计原因：用户可能在前端多次点击收藏按钮，幂等处理避免抛出异常影响用户体验。
     * </p>
     *
     * @param userId  当前登录用户主键 ID
     * @param request 收藏请求对象
     */
    @Override
    public void add(Long userId, FavoriteRequest request) {
        // 步骤1：参数完整性校验
        if (request == null || isBlank(request.getFavoriteType()) || isBlank(request.getTargetId()) || isBlank(request.getTitle())) {
            throw new BusinessException("收藏信息不完整");
        }
        // 步骤2：收藏类型规范化，统一大写，避免大小写不一致
        String favoriteType = request.getFavoriteType().trim().toUpperCase();
        // 步骤3：目标 ID 规范化，去除首尾空格
        String targetId = request.getTargetId().trim();
        // 步骤4：白名单校验，只支持路线、酒店、景点三类
        if (!SUPPORTED_TYPES.contains(favoriteType)) {
            throw new BusinessException("不支持的收藏类型");
        }
        // 步骤5：重复收藏校验，若已存在则静默忽略，保证幂等
        if (userFavoriteMapper.selectOne(userId, favoriteType, targetId) != null) {
            return;
        }
        // 步骤6：构造收藏实体
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setFavoriteType(favoriteType);
        favorite.setTargetId(targetId);
        favorite.setTitle(request.getTitle().trim()); // 标题去除首尾空格
        favorite.setDescription(isBlank(request.getDescription()) ? "" : request.getDescription().trim()); // 描述为空则存空字符串
        // 步骤7：持久化收藏记录
        userFavoriteMapper.insert(favorite); // Mapper 执行 INSERT INTO user_favorite
    }

    /**
     * 移除收藏。
     * <p>
     * 业务逻辑步骤：
     * 1. 参数校验：若 favoriteType 或 targetId 为空，抛出 {@link BusinessException} "收藏信息不完整"；
     * 2. 调用 {@code userFavoriteMapper.deleteOne(userId, favoriteType.trim().toUpperCase(), targetId.trim())} 删除收藏记录
     *    （SQL DELETE WHERE user_id = ? AND favorite_type = ? AND target_id = ?）。
     * 注意：本方法不校验收藏记录是否真实存在，不存在时 delete 语句影响行数为 0，业务上视为无副作用，无需抛出异常。
     * </p>
     *
     * @param userId       当前登录用户主键 ID
     * @param favoriteType 收藏类型
     * @param targetId     收藏目标 ID
     */
    @Override
    public void remove(Long userId, String favoriteType, String targetId) {
        // 步骤1：参数校验
        if (isBlank(favoriteType) || isBlank(targetId)) {
            throw new BusinessException("收藏信息不完整");
        }
        // 步骤2：执行删除，规范化类型和目标 ID
        userFavoriteMapper.deleteOne(userId, favoriteType.trim().toUpperCase(), targetId.trim());
    }

    /**
     * 判断字符串是否为空白（null、空字符串或仅包含空白字符）。
     * <p>私有工具方法，用于简化参数校验中的空值判断。</p>
     *
     * @param value 待判断的字符串
     * @return true 表示字符串为空白，false 表示有有效内容
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
