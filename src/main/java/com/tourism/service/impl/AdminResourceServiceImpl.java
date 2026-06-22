package com.tourism.service.impl;

import com.tourism.common.BusinessException;
import com.tourism.entity.Hotel;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.Ticket;
import com.tourism.entity.User;
import com.tourism.mapper.HotelMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.mapper.TicketMapper;
import com.tourism.mapper.UserMapper;
import com.tourism.service.AdminResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理后台资源管理服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入四个基础 Mapper：
 * {@link UserMapper}、{@link ScenicSpotMapper}、{@link HotelMapper}、{@link TicketMapper}，
 * 实现对用户、景点、酒店、门票四类核心资源的增删改查。
 * 职责：为管理后台提供基础数据维护能力，所有写操作均包含存在性校验，防止操作无效数据。
 * 本类中 save/insert 操作不涉及跨表事务，单表更新由 Mapper 保证原子性；
 * 涉及多表操作（如 delete）时需注意外键约束，当前实现由数据库层控制级联或拒绝策略。
 * </p>
 *
 * @author Tourism System
 * @see AdminResourceService
 */
@Service
public class AdminResourceServiceImpl implements AdminResourceService {

    /**
     * 景点数据访问 Mapper，自动注入。负责 scenic_spot 表的 CRUD 操作。
     */
    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    /**
     * 酒店数据访问 Mapper，自动注入。负责 hotel 表的 CRUD 操作。
     */
    @Autowired
    private HotelMapper hotelMapper;

    /**
     * 门票数据访问 Mapper，自动注入。负责 ticket 表的 CRUD 操作。
     */
    @Autowired
    private TicketMapper ticketMapper;

    /**
     * 用户数据访问 Mapper，自动注入。负责 user 表的查询和更新操作。
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 查询所有用户列表。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code userMapper.selectAll()} 查询所有用户（SQL SELECT * FROM user）；
     * 2. 遍历结果，将每个用户的 password 字段置为 null，防止敏感信息泄露到前端；
     * 3. 返回脱敏后的用户列表。
     * 注意：脱敏操作在内存中完成，不修改数据库中的密码。
     * </p>
     *
     * @return 脱敏后的 {@link User} 列表
     */
    @Override
    public List<User> listUsers() {
        List<User> users = userMapper.selectAll(); // Mapper 执行全表查询
        // 脱敏处理：将密码置空，防止在后台列表中暴露用户密码
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    /**
     * 更新用户信息。
     * <p>
     * 业务逻辑步骤：
     * 1. 校验用户 ID 是否有效，若 ID 为 null 或数据库中不存在该用户，抛出 {@link BusinessException} "用户不存在"；
     * 2. 若角色字段为空或空白，则默认设置为 "USER"（普通用户角色），保证数据完整性；
     * 3. 若状态字段为 null，则默认设置为 1（启用状态），保证数据完整性；
     * 4. 调用 {@code userMapper.updateByAdmin(user)} 执行管理员视角的更新（SQL UPDATE，只更新允许管理员修改的字段）；
     * 5. 重新查询更新后的用户记录（SQL SELECT BY ID），确保返回最新数据库状态；
     * 6. 对返回结果进行密码脱敏（password 置 null）；
     * 7. 返回更新后的用户实体。
     * 注意：管理员更新时不应修改用户密码，因此 updateByAdmin 通常忽略 password 字段。
     * </p>
     *
     * @param user 待更新的用户实体
     * @return 更新后的 {@link User} 实体（已脱敏）
     */
    @Override
    public User updateUser(User user) {
        // 步骤1：存在性校验，防止更新幽灵数据
        if (user.getId() == null || userMapper.selectById(user.getId()) == null) {
            throw new BusinessException("用户不存在");
        }
        // 步骤2：角色默认值处理，防止管理员遗漏角色设置导致权限异常
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        // 步骤3：状态默认值处理，防止状态字段为 null 导致前端展示异常
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        // 步骤4：执行管理员更新，Mapper 执行 UPDATE 只更新非敏感字段
        userMapper.updateByAdmin(user);
        // 步骤5：重新查询确保返回最新数据，避免缓存或脏读问题
        User updated = userMapper.selectById(user.getId());
        // 步骤6：密码脱敏，后台不应展示密码
        updated.setPassword(null);
        return updated;
    }

    /**
     * 更新用户账号状态。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code userMapper.selectById(id)} 查询用户（SQL 主键查询），校验用户是否存在；
     *    若不存在，抛出 {@link BusinessException} "用户不存在"；
     * 2. 构造新的 User 对象，只设置 id 和 status 字段，避免影响其他字段；
     * 3. 若 status 参数为 null，默认设置为 1（启用），保证数据完整性；
     * 4. 调用 {@code userMapper.updateStatus(user)} 执行状态更新（SQL UPDATE 单条记录）。
     * 注意：只更新状态字段，不影响用户名、密码、角色等其他属性。
     * </p>
     *
     * @param id     用户主键 ID
     * @param status 目标状态码
     */
    @Override
    public void updateUserStatus(Long id, Integer status) {
        // 步骤1：存在性校验
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        // 步骤2：构造最小更新对象，只携带需要修改的字段
        User user = new User();
        user.setId(id);
        // 步骤3：做空安全处理，若前端未传状态则默认启用
        user.setStatus(status == null ? 1 : status);
        // 步骤4：执行状态更新，Mapper 执行 UPDATE user SET status = ? WHERE id = ?
        userMapper.updateStatus(user);
    }

    /**
     * 查询所有景点列表。
     * <p>
     * 调用 {@code scenicSpotMapper.selectAll()} 执行全表查询（SQL SELECT * FROM scenic_spot）。
     * 返回的景点列表可直接用于后台景点管理表格展示。
     * </p>
     *
     * @return 所有景点的 {@link ScenicSpot} 列表
     */
    @Override
    public List<ScenicSpot> listScenicSpots() {
        return scenicSpotMapper.selectAll(); // Mapper 执行全表查询
    }

    /**
     * 新增景点信息。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code scenicSpotMapper.insert(scenicSpot)} 插入景点记录（SQL INSERT）；
     * 2. MyBatis 自动将数据库生成的主键回填到 scenicSpot 对象的 id 字段；
     * 3. 返回带主键的景点实体，便于前端在保存成功后获取新记录 ID。
     * 注意：若景点名称重复或必填字段缺失，应由数据库约束或前端校验拦截，此处不做重复校验。
     * </p>
     *
     * @param scenicSpot 待保存的景点实体
     * @return 保存后的 {@link ScenicSpot} 实体（包含主键）
     */
    @Override
    public ScenicSpot saveScenicSpot(ScenicSpot scenicSpot) {
        scenicSpotMapper.insert(scenicSpot); // Mapper 执行 INSERT，主键回填
        return scenicSpot;
    }

    /**
     * 更新景点信息。
     * <p>
     * 业务逻辑步骤：
     * 1. 校验景点 ID 是否有效，若 ID 为 null 或数据库中不存在，抛出 {@link BusinessException} "景点不存在"；
     * 2. 调用 {@code scenicSpotMapper.updateById(scenicSpot)} 执行全字段更新（SQL UPDATE BY ID）；
     * 3. 重新查询更新后的记录（SQL SELECT BY ID），确保返回最新数据库状态；
     * 4. 返回更新后的景点实体。
     * 注意：updateById 通常更新所有非 null 字段，因此前端需回显完整数据后再提交，防止遗漏字段被置空。
     * </p>
     *
     * @param scenicSpot 待更新的景点实体
     * @return 更新后的 {@link ScenicSpot} 实体
     */
    @Override
    public ScenicSpot updateScenicSpot(ScenicSpot scenicSpot) {
        // 步骤1：存在性校验
        if (scenicSpot.getId() == null || scenicSpotMapper.selectById(scenicSpot.getId()) == null) {
            throw new BusinessException("景点不存在");
        }
        // 步骤2：执行全字段更新，Mapper 执行 UPDATE scenic_spot SET ... WHERE id = ?
        scenicSpotMapper.updateById(scenicSpot);
        // 步骤3：重新查询返回最新数据
        return scenicSpotMapper.selectById(scenicSpot.getId());
    }

    /**
     * 删除景点信息。
     * <p>
     * 调用 {@code scenicSpotMapper.deleteById(id)} 执行物理删除（SQL DELETE FROM scenic_spot WHERE id = ?）。
     * 注意：若该景点已被关联到旅游路线（travel_route → route_spot），数据库外键约束可能阻止删除，
     * 或级联删除关联记录。当前实现依赖数据库层的 ON DELETE 策略或外键约束。
     * </p>
     *
     * @param id 景点主键 ID
     */
    @Override
    public void deleteScenicSpot(Long id) {
        scenicSpotMapper.deleteById(id); // Mapper 执行 DELETE BY ID
    }

    /**
     * 查询所有酒店列表。
     * <p>
     * 调用 {@code hotelMapper.selectAll()} 执行全表查询（SQL SELECT * FROM hotel）。
     * 返回的酒店列表可直接用于后台酒店管理表格展示。
     * </p>
     *
     * @return 所有酒店的 {@link Hotel} 列表
     */
    @Override
    public List<Hotel> listHotels() {
        return hotelMapper.selectAll(); // Mapper 执行全表查询
    }

    /**
     * 新增酒店信息。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code hotelMapper.insert(hotel)} 插入酒店记录（SQL INSERT）；
     * 2. MyBatis 自动将数据库生成的主键回填到 hotel 对象的 id 字段；
     * 3. 返回带主键的酒店实体，便于前端获取新记录 ID。
     * </p>
     *
     * @param hotel 待保存的酒店实体
     * @return 保存后的 {@link Hotel} 实体（包含主键）
     */
    @Override
    public Hotel saveHotel(Hotel hotel) {
        hotelMapper.insert(hotel); // Mapper 执行 INSERT，主键回填
        return hotel;
    }

    /**
     * 更新酒店信息。
     * <p>
     * 业务逻辑步骤：
     * 1. 校验酒店 ID 是否有效，若 ID 为 null 或数据库中不存在，抛出 {@link BusinessException} "酒店不存在"；
     * 2. 调用 {@code hotelMapper.updateById(hotel)} 执行全字段更新（SQL UPDATE BY ID）；
     * 3. 重新查询更新后的记录（SQL SELECT BY ID），确保返回最新数据库状态；
     * 4. 返回更新后的酒店实体。
     * </p>
     *
     * @param hotel 待更新的酒店实体
     * @return 更新后的 {@link Hotel} 实体
     */
    @Override
    public Hotel updateHotel(Hotel hotel) {
        // 步骤1：存在性校验
        if (hotel.getId() == null || hotelMapper.selectById(hotel.getId()) == null) {
            throw new BusinessException("酒店不存在");
        }
        // 步骤2：执行全字段更新
        hotelMapper.updateById(hotel);
        // 步骤3：重新查询返回最新数据
        return hotelMapper.selectById(hotel.getId());
    }

    /**
     * 删除酒店信息。
     * <p>
     * 调用 {@code hotelMapper.deleteById(id)} 执行物理删除（SQL DELETE FROM hotel WHERE id = ?）。
     * 注意：若该酒店已被关联到订单（hotel_order），数据库外键约束可能阻止删除。
     * </p>
     *
     * @param id 酒店主键 ID
     */
    @Override
    public void deleteHotel(Long id) {
        hotelMapper.deleteById(id); // Mapper 执行 DELETE BY ID
    }

    /**
     * 查询所有门票列表。
     * <p>
     * 调用 {@code ticketMapper.selectAll()} 执行全表查询（SQL SELECT * FROM ticket）。
     * 返回的门票列表可直接用于后台门票管理表格展示。
     * </p>
     *
     * @return 所有门票的 {@link Ticket} 列表
     */
    @Override
    public List<Ticket> listTickets() {
        return ticketMapper.selectAll(); // Mapper 执行全表查询
    }

    /**
     * 新增门票信息。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code ticketMapper.insert(ticket)} 插入门票记录（SQL INSERT）；
     * 2. MyBatis 自动将数据库生成的主键回填到 ticket 对象的 id 字段；
     * 3. 返回带主键的门票实体。
     * </p>
     *
     * @param ticket 待保存的门票实体
     * @return 保存后的 {@link Ticket} 实体（包含主键）
     */
    @Override
    public Ticket saveTicket(Ticket ticket) {
        ticketMapper.insert(ticket); // Mapper 执行 INSERT，主键回填
        return ticket;
    }

    /**
     * 更新门票信息。
     * <p>
     * 业务逻辑步骤：
     * 1. 校验门票 ID 是否有效，若 ID 为 null 或数据库中不存在，抛出 {@link BusinessException} "门票不存在"；
     * 2. 调用 {@code ticketMapper.updateById(ticket)} 执行全字段更新（SQL UPDATE BY ID）；
     * 3. 重新查询更新后的记录（SQL SELECT BY ID），确保返回最新数据库状态；
     * 4. 返回更新后的门票实体。
     * </p>
     *
     * @param ticket 待更新的门票实体
     * @return 更新后的 {@link Ticket} 实体
     */
    @Override
    public Ticket updateTicket(Ticket ticket) {
        // 步骤1：存在性校验
        if (ticket.getId() == null || ticketMapper.selectById(ticket.getId()) == null) {
            throw new BusinessException("门票不存在");
        }
        // 步骤2：执行全字段更新
        ticketMapper.updateById(ticket);
        // 步骤3：重新查询返回最新数据
        return ticketMapper.selectById(ticket.getId());
    }

    /**
     * 删除门票信息。
     * <p>
     * 调用 {@code ticketMapper.deleteById(id)} 执行物理删除（SQL DELETE FROM ticket WHERE id = ?）。
     * 注意：若该门票已被关联到订单（ticket_order），数据库外键约束可能阻止删除。
     * </p>
     *
     * @param id 门票主键 ID
     */
    @Override
    public void deleteTicket(Long id) {
        ticketMapper.deleteById(id); // Mapper 执行 DELETE BY ID
    }
}
