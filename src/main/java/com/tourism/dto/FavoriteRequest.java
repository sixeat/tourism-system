package com.tourism.dto;

/**
 * 收藏请求数据传输对象（DTO - Data Transfer Object）
 *
 * <p>DTO 模式说明：
 * DTO（数据传输对象）是一种经典的分层架构设计模式，用于封装前端提交到后端的数据。
 * 它与数据库实体（Entity）分离，允许前后端数据结构与数据库表结构不完全一致，
 * 实现关注点分离：DTO负责传输，Entity负责持久化，VO负责展示。</p>
 *
 * <p>本类封装了用户添加收藏时所需的参数，前端以JSON格式提交到后端，
 * Spring的@RequestBody将请求体反序列化为本DTO对象。</p>
 *
 * <p>核心字段：
 * <ul>
 *   <li>favoriteType：收藏类型（标识收藏的是什么：景点、酒店、路线）</li>
 *   <li>targetId：目标对象ID（被收藏的具体对象标识）</li>
 *   <li>title：收藏标题（前端展示用，如景点名称、酒店名称）</li>
 *   <li>description：收藏描述（补充信息，如地址、简介）</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class FavoriteRequest {

    private String favoriteType;
    // favoriteType：收藏类型，标识用户收藏的对象类别
    // 常见取值："scenic"（景点）、"hotel"（酒店）、"route"（旅游路线）
    // 使用String类型而非枚举，便于前端灵活扩展新类型而无需修改后端代码

    private String targetId;
    // targetId：目标对象ID，被收藏对象在数据库中的唯一标识
    // 使用String类型而非Long，是为了兼容不同实体ID类型（有的可能为UUID或其他格式）
    // 例如：收藏景点ID为10，则targetId="10"

    private String title;
    // title：收藏标题，通常为被收藏对象的名称
    // 例如：景点名称"故宫博物院"、酒店名称"北京饭店"
    // 存储标题可避免前端每次展示收藏列表时都要联表查询目标对象名称

    private String description;
    // description：收藏描述，补充信息如地址、简介、标签等
    // 例如："北京市东城区景山前街4号"、"五星级豪华酒店，距天安门广场步行10分钟"
    // 存储描述可提升"我的收藏"页面加载速度，减少关联查询

    public String getFavoriteType() {
        // 返回收藏类型
        return favoriteType;
    }

    public void setFavoriteType(String favoriteType) {
        // 设置收藏类型
        this.favoriteType = favoriteType;
    }

    public String getTargetId() {
        // 返回目标对象ID
        return targetId;
    }

    public void setTargetId(String targetId) {
        // 设置目标对象ID
        this.targetId = targetId;
    }

    public String getTitle() {
        // 返回收藏标题
        return title;
    }

    public void setTitle(String title) {
        // 设置收藏标题
        this.title = title;
    }

    public String getDescription() {
        // 返回收藏描述
        return description;
    }

    public void setDescription(String description) {
        // 设置收藏描述
        this.description = description;
    }
}
