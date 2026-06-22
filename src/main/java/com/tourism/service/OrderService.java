package com.tourism.service;

import com.tourism.dto.HotelOrderCreateRequest;
import com.tourism.dto.TicketOrderCreateRequest;
import com.tourism.entity.HotelOrder;
import com.tourism.entity.TicketOrder;

/**
 * 订单服务接口（Service Contract）。
 * <p>
 * 本接口定义了 C 端用户订单创建、取消、支付的核心服务契约，涵盖酒店订单和门票订单两类业务。
 * 酒店订单涉及房态互斥检查（入住日期范围内的库存锁定），门票订单涉及库存扣减。
 * 所有写操作（创建、取消、支付）应在实现层通过事务控制，确保数据一致性。
 * 实现类：{@link com.tourism.service.impl.OrderServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface OrderService {

    /**
     * 创建酒店订单。
     * <p>
     * 业务校验：
     * 1. 入住日期必须早于离店日期；
     * 2. 房型必须存在且库存大于 0；
     * 3. 所选日期范围内的已锁定数量必须小于总库存（房态互斥）。
     * 金额计算：总价 = 房型单价 × 入住天数（ChronoUnit.DAYS.between）。
     * 订单初始状态为 "CREATED"。
     * 整个操作应在事务中完成，避免并发超售。
     * </p>
     *
     * @param request 酒店订单创建请求对象，包含用户 ID、酒店 ID、房型 ID、入住/离店日期
     * @return 创建成功的 {@link HotelOrder} 实体（包含数据库生成的主键和订单号）
     */
    HotelOrder createHotelOrder(HotelOrderCreateRequest request);

    /**
     * 创建门票订单。
     * <p>
     * 业务校验：
     * 1. 门票数量必须大于 0；
     * 2. 门票必须存在且库存大于等于购买数量；
     * 3. 乐观锁扣减库存（deductStock），若扣减失败说明库存不足或并发冲突。
     * 金额计算：总价 = 门票单价 × 数量。
     * 订单初始状态为 "CREATED"。
     * 整个操作应在事务中完成，确保库存扣减与订单创建同时成功或回滚。
     * </p>
     *
     * @param request 门票订单创建请求对象，包含用户 ID、门票 ID、游览日期、数量
     * @return 创建成功的 {@link TicketOrder} 实体（包含数据库生成的主键和订单号）
     */
    TicketOrder createTicketOrder(TicketOrderCreateRequest request);

    /**
     * 取消酒店订单。
     * <p>
     * 业务规则：
     * 1. 订单必须存在；
     * 2. 当前用户必须是订单所有者；
     * 3. 订单状态必须为 "CREATED"（未支付）才可取消；
     * 4. 取消后订单状态更新为 "CANCELLED"。
     * 酒店订单取消无需恢复库存（因为酒店房态是动态计算未取消订单的）。
     * 整个操作应在事务中完成。
     * </p>
     *
     * @param orderId 酒店订单主键 ID
     * @param userId  当前登录用户主键 ID，用于权限校验
     */
    void cancelHotelOrder(Long orderId, Long userId);

    /**
     * 取消门票订单。
     * <p>
     * 业务规则：
     * 1. 订单必须存在；
     * 2. 当前用户必须是订单所有者；
     * 3. 订单状态必须为 "CREATED"（未支付）才可取消；
     * 4. 取消后订单状态更新为 "CANCELLED"；
     * 5. 同时恢复该订单对应的门票库存（restoreStock）。
     * 整个操作应在事务中完成，确保订单状态与库存恢复同时成功或回滚。
     * </p>
     *
     * @param orderId 门票订单主键 ID
     * @param userId  当前登录用户主键 ID，用于权限校验
     */
    void cancelTicketOrder(Long orderId, Long userId);

    /**
     * 支付酒店订单。
     * <p>
     * 业务规则：
     * 1. 订单必须存在；
     * 2. 当前用户必须是订单所有者；
     * 3. 订单状态必须为 "CREATED"（未支付）才可支付；
     * 4. 支付后订单状态更新为 "PAID"。
     * 整个操作应在事务中完成。
     * </p>
     *
     * @param orderId 酒店订单主键 ID
     * @param userId  当前登录用户主键 ID，用于权限校验
     */
    void payHotelOrder(Long orderId, Long userId);

    /**
     * 支付门票订单。
     * <p>
     * 业务规则：
     * 1. 订单必须存在；
     * 2. 当前用户必须是订单所有者；
     * 3. 订单状态必须为 "CREATED"（未支付）才可支付；
     * 4. 支付后订单状态更新为 "PAID"。
     * 整个操作应在事务中完成。
     * </p>
     *
     * @param orderId 门票订单主键 ID
     * @param userId  当前登录用户主键 ID，用于权限校验
     */
    void payTicketOrder(Long orderId, Long userId);
}
