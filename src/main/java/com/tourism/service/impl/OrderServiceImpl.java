package com.tourism.service.impl;

import com.tourism.common.BusinessException;
import com.tourism.dto.HotelOrderCreateRequest;
import com.tourism.dto.TicketOrderCreateRequest;
import com.tourism.entity.HotelOrder;
import com.tourism.entity.HotelRoom;
import com.tourism.entity.Ticket;
import com.tourism.entity.TicketOrder;
import com.tourism.mapper.HotelOrderMapper;
import com.tourism.mapper.HotelRoomMapper;
import com.tourism.mapper.TicketMapper;
import com.tourism.mapper.TicketOrderMapper;
import com.tourism.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

/**
 * 订单服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入四个 Mapper：
 * {@link HotelRoomMapper} 用于房型查询，{@link HotelOrderMapper} 用于酒店订单操作，
 * {@link TicketMapper} 用于门票查询与库存扣减，{@link TicketOrderMapper} 用于门票订单操作。
 * 职责：为 C 端用户提供酒店订单和门票订单的创建、取消、支付服务。
 * 所有写操作（create*、cancel*、pay*）均声明 {@link Transactional}（rollbackFor = Exception.class），
 * 原因：订单创建涉及库存检查/扣减与订单插入，取消涉及状态更新与库存恢复，这些操作必须原子化，
 * 任何一步失败都需回滚，避免超售或库存丢失。
 * 事务隔离级别：默认数据库默认级别（通常为 READ COMMITTED）。
 * 事务传播行为：默认 REQUIRED。
 * </p>
 *
 * @author Tourism System
 * @see OrderService
 */
@Service
public class OrderServiceImpl implements OrderService {

    /**
     * 房型数据访问 Mapper，自动注入。用于查询房型信息和库存。
     */
    @Autowired
    private HotelRoomMapper hotelRoomMapper;

    /**
     * 酒店订单数据访问 Mapper，自动注入。用于酒店订单的插入和状态更新。
     */
    @Autowired
    private HotelOrderMapper hotelOrderMapper;

    /**
     * 门票数据访问 Mapper，自动注入。用于门票查询和乐观锁库存扣减/恢复。
     */
    @Autowired
    private TicketMapper ticketMapper;

    /**
     * 门票订单数据访问 Mapper，自动注入。用于门票订单的插入和状态更新。
     */
    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    /**
     * 创建酒店订单。
     * <p>
     * 业务逻辑步骤（均在事务内执行）：
     * 1. 校验日期合法性：入住日期和离店日期均不可为 null，且离店日期必须晚于入住日期；
     *    若校验失败，抛出 {@link IllegalArgumentException}，提示 "入住日期必须早于离店日期"；
     * 2. 调用 {@code hotelRoomMapper.selectById(request.getRoomId())} 查询房型（SQL 主键查询），
     *    若房型不存在，抛出 {@link IllegalArgumentException} "房型不存在"；
     * 3. 校验房型库存：若 stock 为 null 或小于等于 0，抛出 {@link IllegalStateException} "该房型库存不足"；
     * 4. 调用 {@code hotelOrderMapper.countActiveOverlap(request.getRoomId(), checkInDate, checkOutDate)} 统计该房型在
     *    入住日期与离店日期之间已被未取消订单锁定的数量（SQL 查询日期范围重叠的订单，即 check_in_date < 离店日期 AND check_out_date > 入住日期）；
     *    若 lockedCount >= stock，说明所选日期已满房，抛出 {@link IllegalStateException} "该房型在所选日期已满房，请更换房型或入住日期"；
     *    该机制为"房态互斥"核心逻辑：酒店房态不预扣库存，而是实时计算已锁定订单的日期重叠量；
     * 5. 计算入住天数：使用 {@link ChronoUnit#DAYS} 计算 checkInDate 到 checkOutDate 之间的天数；
     * 6. 计算订单总金额：total = roomPrice × nights（{@link BigDecimal} 运算）；
     * 7. 构造 {@link HotelOrder} 实体，设置用户 ID、酒店 ID、房型 ID、入住/离店日期、总金额、初始状态 "CREATED"；
     * 8. 调用 {@code hotelOrderMapper.insert(order)} 将订单持久化到数据库（SQL INSERT），主键回填；
     * 9. 返回创建的订单实体。
     * 事务说明：若步骤8失败（如数据库异常），前面所有校验和计算都不会产生实际影响，事务回滚。
     * </p>
     *
     * @param request 酒店订单创建请求对象
     * @return 创建成功的 {@link HotelOrder} 实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：日期校验、库存检查、订单插入必须原子化，防止并发超售
    public HotelOrder createHotelOrder(HotelOrderCreateRequest request) {
        // 步骤1：日期合法性校验，入住日期必须早于离店日期
        if (request.getCheckInDate() == null || request.getCheckOutDate() == null
                || !request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("入住日期必须早于离店日期");
        }

        // 步骤2：查询房型存在性
        HotelRoom room = hotelRoomMapper.selectById(request.getRoomId());
        if (room == null) {
            throw new IllegalArgumentException("房型不存在");
        }
        // 步骤3：房型库存基础校验，若库存为 0 或 null 直接拒绝
        if (room.getStock() == null || room.getStock() <= 0) {
            throw new IllegalStateException("该房型库存不足");
        }
        // 步骤4：计算日期重叠的已锁定订单数量，判断房态互斥
        int lockedCount = hotelOrderMapper.countActiveOverlap(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );
        // 若已锁定数量 >= 总库存，说明所选日期已满房
        if (lockedCount >= room.getStock()) {
            throw new IllegalStateException("该房型在所选日期已满房，请更换房型或入住日期");
        }

        // 步骤5：计算入住天数
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        // 步骤6：计算订单总金额
        BigDecimal total = room.getPrice().multiply(BigDecimal.valueOf(nights));

        // 步骤7：构造订单实体
        HotelOrder order = new HotelOrder();
        order.setUserId(request.getUserId());
        order.setHotelId(request.getHotelId());
        order.setRoomId(request.getRoomId());
        order.setCheckInDate(request.getCheckInDate());
        order.setCheckOutDate(request.getCheckOutDate());
        order.setTotalAmount(total);
        order.setOrderStatus("CREATED"); // 初始状态：已创建，未支付
        // 步骤8：持久化订单
        hotelOrderMapper.insert(order); // Mapper 执行 INSERT，主键回填
        return order;
    }

    /**
     * 创建门票订单。
     * <p>
     * 业务逻辑步骤（均在事务内执行）：
     * 1. 校验购买数量：quantity 必须为大于 0 的整数；若校验失败，抛出 {@link IllegalArgumentException} "门票数量必须大于 0"；
     * 2. 调用 {@code ticketMapper.selectById(request.getTicketId())} 查询门票（SQL 主键查询），
     *    若门票不存在，抛出 {@link IllegalArgumentException} "门票不存在"；
     * 3. 校验门票库存：若 stock 为 null 或小于购买数量，抛出 {@link IllegalStateException} "门票库存不足"；
     * 4. 调用 {@code ticketMapper.deductStock(request.getTicketId(), request.getQuantity())} 执行乐观锁库存扣减（SQL UPDATE SET stock = stock - ? WHERE id = ? AND stock >= ?），
     *    返回影响行数；若 updated == 0，说明库存不足或并发冲突导致扣减失败，抛出 {@link IllegalStateException} "门票库存扣减失败，请稍后重试"；
     *    乐观锁机制：不先查询再更新，而是在 UPDATE 的 WHERE 条件中校验 stock >= quantity，避免并发条件下的超售；
     * 5. 计算订单总金额：total = ticketPrice × quantity（{@link BigDecimal} 运算）；
     * 6. 构造 {@link TicketOrder} 实体，设置用户 ID、门票 ID、游览日期、数量、总金额、初始状态 "CREATED"；
     * 7. 调用 {@code ticketOrderMapper.insert(order)} 将订单持久化到数据库（SQL INSERT），主键回填；
     * 8. 返回创建的订单实体。
     * 事务说明：步骤4的库存扣减和步骤7的订单插入必须在同一事务中，若订单插入失败，库存扣减应回滚，避免库存丢失。
     * </p>
     *
     * @param request 门票订单创建请求对象
     * @return 创建成功的 {@link TicketOrder} 实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：库存扣减与订单插入必须原子化，防止并发超售或库存丢失
    public TicketOrder createTicketOrder(TicketOrderCreateRequest request) {
        // 步骤1：数量合法性校验
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("门票数量必须大于 0");
        }

        // 步骤2：查询门票存在性
        Ticket ticket = ticketMapper.selectById(request.getTicketId());
        if (ticket == null) {
            throw new IllegalArgumentException("门票不存在");
        }
        // 步骤3：库存基础校验
        if (ticket.getStock() == null || ticket.getStock() < request.getQuantity()) {
            throw new IllegalStateException("门票库存不足");
        }

        // 步骤4：乐观锁库存扣减，WHERE 条件中 stock >= quantity 确保不会超售
        int updated = ticketMapper.deductStock(request.getTicketId(), request.getQuantity());
        if (updated == 0) {
            // 扣减失败可能原因：并发下其他请求已扣减，导致当前库存不足
            throw new IllegalStateException("门票库存扣减失败，请稍后重试");
        }

        // 步骤5：计算订单总金额
        BigDecimal total = ticket.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        // 步骤6：构造订单实体
        TicketOrder order = new TicketOrder();
        order.setUserId(request.getUserId());
        order.setTicketId(request.getTicketId());
        order.setVisitDate(request.getVisitDate());
        order.setQuantity(request.getQuantity());
        order.setTotalAmount(total);
        order.setOrderStatus("CREATED"); // 初始状态：已创建，未支付
        // 步骤7：持久化订单
        ticketOrderMapper.insert(order); // Mapper 执行 INSERT，主键回填
        return order;
    }

    /**
     * 取消酒店订单。
     * <p>
     * 业务逻辑步骤（均在事务内执行）：
     * 1. 调用 {@code hotelOrderMapper.selectById(orderId)} 查询酒店订单（SQL 主键查询），
     *    若订单不存在，抛出 {@link BusinessException} "酒店订单不存在"；
     * 2. 校验订单所有权：若 order.getUserId() 不等于当前 userId，抛出 {@link BusinessException} "没有权限取消该酒店订单"；
     * 3. 校验订单状态：只有 "CREATED"（已创建，未支付）状态的订单才允许取消，
     *    若状态不符，抛出 {@link BusinessException} "该酒店订单当前状态不可取消"；
     *    该规则防止对已支付或已完成的订单进行误取消；
     * 4. 调用 {@code hotelOrderMapper.updateStatus(orderId, "CANCELLED")} 更新订单状态为已取消（SQL UPDATE）。
     * 注意：酒店订单取消无需恢复库存，因为酒店房态是动态计算未取消订单的日期重叠量，
     * 取消后订单状态变为 CANCELLED，countActiveOverlap 会自动排除该订单。
     * 事务说明：查询和更新在同一事务中，防止并发下读取到中间状态。
     * </p>
     *
     * @param orderId 酒店订单主键 ID
     * @param userId  当前登录用户主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：查询与状态更新原子化，防止并发状态冲突
    public void cancelHotelOrder(Long orderId, Long userId) {
        // 步骤1：查询订单存在性
        HotelOrder order = hotelOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("酒店订单不存在");
        }
        // 步骤2：所有权校验，防止用户取消他人订单
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("没有权限取消该酒店订单");
        }
        // 步骤3：状态校验，只有未支付订单允许取消
        if (!"CREATED".equals(order.getOrderStatus())) {
            throw new BusinessException("该酒店订单当前状态不可取消");
        }
        // 步骤4：更新状态为 CANCELLED
        hotelOrderMapper.updateStatus(orderId, "CANCELLED"); // Mapper 执行 UPDATE SET order_status = 'CANCELLED'
    }

    /**
     * 取消门票订单。
     * <p>
     * 业务逻辑步骤（均在事务内执行）：
     * 1. 调用 {@code ticketOrderMapper.selectById(orderId)} 查询门票订单（SQL 主键查询），
     *    若订单不存在，抛出 {@link BusinessException} "门票订单不存在"；
     * 2. 校验订单所有权：若 order.getUserId() 不等于当前 userId，抛出 {@link BusinessException} "没有权限取消该门票订单"；
     * 3. 校验订单状态：只有 "CREATED" 状态的订单才允许取消，若状态不符，抛出 {@link BusinessException} "该门票订单当前状态不可取消"；
     * 4. 调用 {@code ticketOrderMapper.updateStatus(orderId, "CANCELLED")} 更新订单状态为已取消；
     * 5. 调用 {@code ticketMapper.restoreStock(order.getTicketId(), order.getQuantity())} 恢复库存（SQL UPDATE SET stock = stock + ?），
     *    将订单对应的门票数量加回库存，保证库存守恒。
     * 事务说明：步骤4和步骤5必须在同一事务中，若库存恢复失败但订单状态已更新，则会导致库存丢失；
     * 事务回滚可保证两者同时成功或同时失败。
     * </p>
     *
     * @param orderId 门票订单主键 ID
     * @param userId  当前登录用户主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：状态更新与库存恢复必须原子化，防止库存丢失
    public void cancelTicketOrder(Long orderId, Long userId) {
        // 步骤1：查询订单存在性
        TicketOrder order = ticketOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("门票订单不存在");
        }
        // 步骤2：所有权校验
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("没有权限取消该门票订单");
        }
        // 步骤3：状态校验
        if (!"CREATED".equals(order.getOrderStatus())) {
            throw new BusinessException("该门票订单当前状态不可取消");
        }
        // 步骤4：更新订单状态为 CANCELLED
        ticketOrderMapper.updateStatus(orderId, "CANCELLED");
        // 步骤5：恢复门票库存，保证库存守恒
        ticketMapper.restoreStock(order.getTicketId(), order.getQuantity());
    }

    /**
     * 支付酒店订单。
     * <p>
     * 业务逻辑步骤（均在事务内执行）：
     * 1. 调用 {@code hotelOrderMapper.selectById(orderId)} 查询酒店订单（SQL 主键查询），
     *    若订单不存在，抛出 {@link BusinessException} "酒店订单不存在"；
     * 2. 校验订单所有权：若 order.getUserId() 不等于当前 userId，抛出 {@link BusinessException} "没有权限支付该酒店订单"；
     * 3. 校验订单状态：只有 "CREATED" 状态的订单才允许支付，若状态不符，抛出 {@link BusinessException} "该酒店订单当前状态不可支付"；
     *    防止对已取消或已支付的订单重复支付；
     * 4. 调用 {@code hotelOrderMapper.updateStatus(orderId, "PAID")} 更新订单状态为已支付（SQL UPDATE）。
     * 注意：本系统采用模拟支付，不接入真实支付网关，状态更新即视为支付成功。
     * 事务说明：查询和更新在同一事务中，防止并发重复支付。
     * </p>
     *
     * @param orderId 酒店订单主键 ID
     * @param userId  当前登录用户主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：查询与状态更新原子化，防止并发重复支付
    public void payHotelOrder(Long orderId, Long userId) {
        // 步骤1：查询订单存在性
        HotelOrder order = hotelOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("酒店订单不存在");
        }
        // 步骤2：所有权校验
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("没有权限支付该酒店订单");
        }
        // 步骤3：状态校验，只有 CREATED 状态允许支付
        if (!"CREATED".equals(order.getOrderStatus())) {
            throw new BusinessException("该酒店订单当前状态不可支付");
        }
        // 步骤4：更新状态为 PAID
        hotelOrderMapper.updateStatus(orderId, "PAID"); // Mapper 执行 UPDATE SET order_status = 'PAID'
    }

    /**
     * 支付门票订单。
     * <p>
     * 业务逻辑步骤（均在事务内执行）：
     * 1. 调用 {@code ticketOrderMapper.selectById(orderId)} 查询门票订单（SQL 主键查询），
     *    若订单不存在，抛出 {@link BusinessException} "门票订单不存在"；
     * 2. 校验订单所有权：若 order.getUserId() 不等于当前 userId，抛出 {@link BusinessException} "没有权限支付该门票订单"；
     * 3. 校验订单状态：只有 "CREATED" 状态的订单才允许支付，若状态不符，抛出 {@link BusinessException} "该门票订单当前状态不可支付"；
     * 4. 调用 {@code ticketOrderMapper.updateStatus(orderId, "PAID")} 更新订单状态为已支付（SQL UPDATE）。
     * 注意：与酒店订单支付同理，本系统采用模拟支付。
     * 事务说明：查询和更新在同一事务中，防止并发重复支付。
     * </p>
     *
     * @param orderId 门票订单主键 ID
     * @param userId  当前登录用户主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：查询与状态更新原子化，防止并发重复支付
    public void payTicketOrder(Long orderId, Long userId) {
        // 步骤1：查询订单存在性
        TicketOrder order = ticketOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("门票订单不存在");
        }
        // 步骤2：所有权校验
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("没有权限支付该门票订单");
        }
        // 步骤3：状态校验
        if (!"CREATED".equals(order.getOrderStatus())) {
            throw new BusinessException("该门票订单当前状态不可支付");
        }
        // 步骤4：更新状态为 PAID
        ticketOrderMapper.updateStatus(orderId, "PAID"); // Mapper 执行 UPDATE SET order_status = 'PAID'
    }
}
