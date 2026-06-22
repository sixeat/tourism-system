package com.tourism.service;

import com.tourism.entity.Hotel;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.Ticket;
import com.tourism.entity.User;

import java.util.List;

/**
 * 管理后台资源管理服务接口（Service Contract）。
 * <p>
 * 本接口定义了管理后台对核心业务资源（用户、景点、酒店、门票）的 CRUD 操作契约。
 * 管理员可通过此服务维护系统基础数据，包括用户管理、景点信息维护、酒店信息维护、门票信息维护。
 * 实现类：{@link com.tourism.service.impl.AdminResourceServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface AdminResourceService {

    /**
     * 查询所有用户列表。
     * <p>
     * 返回系统中所有注册用户，通常用于后台用户管理页面展示。
     * 出于安全考虑，返回的用户对象不应包含密码字段。
     * </p>
     *
     * @return 所有用户的 {@link User} 列表
     */
    List<User> listUsers();

    /**
     * 更新用户信息。
     * <p>
     * 管理员可修改用户的角色（如 USER / ADMIN）、状态等属性。
     * 若用户不存在，则应在实现层抛出 {@link com.tourism.common.BusinessException}。
     * 返回更新后的用户对象，同样不应包含密码字段。
     * </p>
     *
     * @param user 待更新的用户实体，id 字段必须有效
     * @return 更新后的 {@link User} 实体
     */
    User updateUser(User user);

    /**
     * 更新用户账号状态。
     * <p>
     * 用于启用或禁用用户账号，例如 status=1 表示启用，status=0 表示禁用。
     * 若用户不存在，则应在实现层抛出 {@link com.tourism.common.BusinessException}。
     * </p>
     *
     * @param id     用户主键 ID，不可为 null
     * @param status 目标状态码，若传入 null 则默认为 1（启用）
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 查询所有景点列表。
     * <p>
     * 返回系统中所有旅游景点信息，用于后台景点管理页面展示。
     * </p>
     *
     * @return 所有景点的 {@link ScenicSpot} 列表
     */
    List<ScenicSpot> listScenicSpots();

    /**
     * 新增景点信息。
     * <p>
     * 将景点实体持久化到数据库，通常由管理员在后台录入新的旅游景点。
     * 保存后返回带主键的景点实体。
     * </p>
     *
     * @param scenicSpot 待保存的景点实体
     * @return 保存后的 {@link ScenicSpot} 实体（包含数据库生成的主键）
     */
    ScenicSpot saveScenicSpot(ScenicSpot scenicSpot);

    /**
     * 更新景点信息。
     * <p>
     * 修改已有景点的名称、城市、分类、评分、价格等字段。
     * 若景点不存在，则应在实现层抛出 {@link com.tourism.common.BusinessException}。
     * 返回更新后的完整景点数据。
     * </p>
     *
     * @param scenicSpot 待更新的景点实体，id 字段必须有效
     * @return 更新后的 {@link ScenicSpot} 实体
     */
    ScenicSpot updateScenicSpot(ScenicSpot scenicSpot);

    /**
     * 删除景点信息。
     * <p>
     * 根据景点主键 ID 删除该景点记录。注意：若景点已被关联到路线或其他业务数据，
     * 实现层需考虑外键约束或级联删除策略。
     * </p>
     *
     * @param id 景点主键 ID，不可为 null
     */
    void deleteScenicSpot(Long id);

    /**
     * 查询所有酒店列表。
     * <p>
     * 返回系统中所有酒店信息，用于后台酒店管理页面展示。
     * </p>
     *
     * @return 所有酒店的 {@link Hotel} 列表
     */
    List<Hotel> listHotels();

    /**
     * 新增酒店信息。
     * <p>
     * 将酒店实体持久化到数据库，通常由管理员在后台录入新的酒店。
     * 保存后返回带主键的酒店实体。
     * </p>
     *
     * @param hotel 待保存的酒店实体
     * @return 保存后的 {@link Hotel} 实体（包含数据库生成的主键）
     */
    Hotel saveHotel(Hotel hotel);

    /**
     * 更新酒店信息。
     * <p>
     * 修改已有酒店的名称、城市、等级、地址、描述等字段。
     * 若酒店不存在，则应在实现层抛出 {@link com.tourism.common.BusinessException}。
     * 返回更新后的完整酒店数据。
     * </p>
     *
     * @param hotel 待更新的酒店实体，id 字段必须有效
     * @return 更新后的 {@link Hotel} 实体
     */
    Hotel updateHotel(Hotel hotel);

    /**
     * 删除酒店信息。
     * <p>
     * 根据酒店主键 ID 删除该酒店记录。注意：若酒店已被关联到订单或其他业务数据，
     * 实现层需考虑外键约束或级联删除策略。
     * </p>
     *
     * @param id 酒店主键 ID，不可为 null
     */
    void deleteHotel(Long id);

    /**
     * 查询所有门票列表。
     * <p>
     * 返回系统中所有门票信息，用于后台门票管理页面展示。
     * </p>
     *
     * @return 所有门票的 {@link Ticket} 列表
     */
    List<Ticket> listTickets();

    /**
     * 新增门票信息。
     * <p>
     * 将门票实体持久化到数据库，通常由管理员在后台录入新的门票。
     * 保存后返回带主键的门票实体。
     * </p>
     *
     * @param ticket 待保存的门票实体
     * @return 保存后的 {@link Ticket} 实体（包含数据库生成的主键）
     */
    Ticket saveTicket(Ticket ticket);

    /**
     * 更新门票信息。
     * <p>
     * 修改已有门票的名称、所属景点、价格、库存、可用日期等字段。
     * 若门票不存在，则应在实现层抛出 {@link com.tourism.common.BusinessException}。
     * 返回更新后的完整门票数据。
     * </p>
     *
     * @param ticket 待更新的门票实体，id 字段必须有效
     * @return 更新后的 {@link Ticket} 实体
     */
    Ticket updateTicket(Ticket ticket);

    /**
     * 删除门票信息。
     * <p>
     * 根据门票主键 ID 删除该门票记录。注意：若门票已被关联到订单，
     * 实现层需考虑外键约束或级联删除策略。
     * </p>
     *
     * @param id 门票主键 ID，不可为 null
     */
    void deleteTicket(Long id);
}
