package com.tourism.entity;

import java.time.LocalDateTime;

/**
 * 用户收藏实体类（UserFavorite Entity）
 *
 * <p>本类为 POJO（Plain Old Java Object），对应数据库表 <code>user_favorite</code>，
 * 用于存储用户的收藏记录，支持收藏多种类型的对象（如酒店、景点、路线等）。
 * 通过私有无参字段 + 公共 getter/setter 满足 JavaBean 规范，
 * 支持 Spring BeanWrapper 与 MyBatis 反射注入机制。</p>
 *
 * <p>数据库表设计说明：</p>
 * <ul>
 *   <li>表名：user_favorite</li>
 *   <li>主键：id（自增 BIGINT），唯一标识一条收藏记录</li>
 *   <li>无物理外键，通过 user_id 与 target_id 逻辑关联其他实体</li>
 *   <li>建议为 user_id + favorite_type + target_id 建立唯一索引，防止用户重复收藏同一对象</li>
 * </ul>
 *
 * <p>与其他实体的关系：</p>
 * <ul>
 *   <li>逻辑关联 {@link User}（通过 userId）：标识收藏人</li>
 *   <li>多态关联其他业务实体（通过 favoriteType + targetId）：
     *   <ul>
     *     <li>favoriteType = "HOTEL" 时，targetId 指向 {@link Hotel} 的 id</li>
     *     <li>favoriteType = "SCENIC" 时，targetId 指向 {@link ScenicSpot} 的 id</li>
     *     <li>favoriteType = "ROUTE" 时，targetId 指向 {@link TravelRoute} 的 id</li>
     *     <li>favoriteType = "MAP_POINT" 时，targetId 指向 {@link MapPoint} 的 id</li>
     *   </ul>
     *   这种“类型+目标ID”的设计是经典的多态关联模式，支持扩展性。
     * </li>
 * </ul>
 *
 * @author Tourism System
 * @version 1.0
 */
public class UserFavorite {

    /**
     * 收藏记录唯一标识（主键）
     *
     * <p>对应数据库列 <code>id</code>，类型 <code>BIGINT</code>，非空，自增。</p>
     * <p>使用 {@link Long} 包装类，避免 NULL 映射为基本类型 0 的歧义。</p>
     */
    private Long id;

    /**
     * 用户 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>user_id</code>，类型 <code>BIGINT</code>，非空。</p>
     * <p>逻辑关联 {@link User} 表的 <code>id</code> 主键，表示“谁收藏的内容”。
     * 数据库层面建议为 user_id 建立索引，以加速“查询某用户的收藏列表”的场景。</p>
     */
    private Long userId;

    /**
     * 收藏类型
     *
     * <p>对应数据库列 <code>favorite_type</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>收藏类型语义（业务约定，多态关联标识）：</p>
     * <ul>
     *   <li><code>"HOTEL"</code> — 收藏酒店，此时 targetId 指向 {@link Hotel} 的 id</li>
     *   <li><code>"SCENIC"</code> — 收藏景点，此时 targetId 指向 {@link ScenicSpot} 的 id</li>
     *   <li><code>"ROUTE"</code> — 收藏路线，此时 targetId 指向 {@link TravelRoute} 的 id</li>
     *   <li><code>"MAP_POINT"</code> — 收藏地图点位，此时 targetId 指向 {@link MapPoint} 的 id</li>
     * </ul>
     * <p>使用字符串存储，增强可读性，便于后续扩展新收藏类型（如 "TICKET" 门票收藏）。</p>
     */
    private String favoriteType;

    /**
     * 目标对象 ID（逻辑外键）
     *
     * <p>对应数据库列 <code>target_id</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>与 favoriteType 联合使用，共同确定被收藏的具体对象。
     * 选用 {@link String} 而非 {@link Long} 的原因：提高通用性，兼容某些目标对象使用非数字主键（如 UUID）的场景。
     * 在现有业务中，targetId 存储的是 Long 的字符串形式（如 "12345"）。</p>
     * <p>例如：favoriteType="HOTEL" 且 targetId="123" 表示收藏了酒店 123。</p>
     */
    private String targetId;

    /**
     * 收藏标题
     *
     * <p>对应数据库列 <code>title</code>，类型 <code>VARCHAR</code>，非空。</p>
     * <p>收藏记录中冗余存储目标对象的标题，用于在用户收藏列表中直接展示，
     * 避免每次查询都 JOIN 目标对象表。例如收藏酒店时，title 存储酒店名称。
     * 这种冗余设计是典型的反范化（denormalization），以空间换时间，提升列表查询性能。</p>
     */
    private String title;

    /**
     * 收藏描述
     *
     * <p>对应数据库列 <code>description</code>，类型 <code>VARCHAR</code> 或 <code>TEXT</code>。</p>
     * <p>收藏记录中冗余存储目标对象的描述，用于列表页摘要展示。
     * 例如收藏景点时，description 存储景点的简短描述或地址信息。
     * 同样属于反范化设计，提升查询性能。</p>
     */
    private String description;

    /**
     * 收藏创建时间
     *
     * <p>对应数据库列 <code>create_time</code>，类型 <code>DATETIME</code>，非空。</p>
     * <p>选用 {@link LocalDateTime} 原因：</p>
     * <ul>
     *   <li>精确到“年-月-日 时:分:秒”，记录收藏操作的完整时间戳</li>
     *   <li>Java 8 新日期时间 API，线程安全，设计优于旧版 java.util.Date</li>
     *   <li>MyBatis 原生支持映射到数据库 DATETIME 类型</li>
     * </ul>
     * <p>通常由数据库 <code>CURRENT_TIMESTAMP</code> 或业务层在插入时自动填充。</p>
     */
    private LocalDateTime createTime;

    /**
     * 获取收藏记录唯一标识
     *
     * @return 收藏记录 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置收藏记录唯一标识
     *
     * @param id 收藏记录 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取用户 ID
     *
     * @return 用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户 ID
     *
     * @param userId 用户 ID，逻辑关联 user 表
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取收藏类型
     *
     * @return 收藏类型字符串，如 "HOTEL"、"SCENIC" 等
     */
    public String getFavoriteType() {
        return favoriteType;
    }

    /**
     * 设置收藏类型
     *
     * @param favoriteType 收藏类型字符串，决定 targetId 指向哪个业务表
     */
    public void setFavoriteType(String favoriteType) {
        this.favoriteType = favoriteType;
    }

    /**
     * 获取目标对象 ID
     *
     * @return 目标对象 ID 字符串
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * 设置目标对象 ID
     *
     * @param targetId 目标对象 ID 字符串，与 favoriteType 联合确定具体对象
     */
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    /**
     * 获取收藏标题
     *
     * @return 收藏标题（冗余字段，用于列表展示）
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置收藏标题
     *
     * @param title 收藏标题，冗余存储目标对象名称
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取收藏描述
     *
     * @return 收藏描述（冗余字段，用于列表摘要）
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置收藏描述
     *
     * @param description 收藏描述，冗余存储目标对象摘要信息
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取收藏创建时间
     *
     * @return 收藏创建时间（LocalDateTime）
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置收藏创建时间
     *
     * @param createTime 收藏创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
