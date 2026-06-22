package com.tourism.mapper;

import com.tourism.entity.HotelOrder;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 酒店订单数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（HotelOrderMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口提供酒店订单的创建、查询、状态更新以及入住日期冲突检测等功能。
 */
public interface HotelOrderMapper {
    
    /**
     * 插入一条新的酒店订单
     * 
     * 对应 SQL：insert into hotel_order (...) values (...)
     * 使用 useGeneratedKeys 自动获取数据库生成的主键值，并回填到实体对象的 id 属性中。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param order 酒店订单实体对象，包含用户ID、酒店ID、房间ID、入住/退房日期、金额、状态等
     * @return 受影响的行数，成功插入时通常为 1
     */
    int insert(HotelOrder order);

    /**
     * 根据订单 ID 查询酒店订单详情
     * 
     * 对应 SQL：select ... from hotel_order where id = #{id}
     * 通过 #{id} 预编译参数绑定，防止 SQL 注入攻击。
     * 
     * @param id 酒店订单主键 ID
     * @return 对应的 HotelOrder 实体对象，若不存在则返回 null
     */
    HotelOrder selectById(Long id);

    /**
     * 更新酒店订单状态
     * 
     * 对应 SQL：update hotel_order set order_status = #{orderStatus} where id = #{id}
     * 通过 @Param 注解指定参数名，确保 XML 中 #{id} 和 #{orderStatus} 能够正确映射。
     * 参数均为预编译绑定，防止 SQL 注入。
     * 
     * @param id 待更新状态的订单 ID
     * @param orderStatus 新的订单状态（如 CREATED, PAID, FINISHED, CANCELLED）
     * @return 受影响的行数
     */
    int updateStatus(@Param("id") Long id, @Param("orderStatus") String orderStatus);

    /**
     * 统计指定房间在指定日期范围内的活跃订单冲突数量
     * 
     * 对应 SQL：select count(*) from hotel_order where room_id = #{roomId} 
     *          and order_status in (...) and check_in_date < #{checkOutDate} and check_out_date > #{checkInDate}
     * 通过 @Param 注解指定参数名，确保 XML 中参数映射正确。
     * 使用区间重叠判断逻辑：[checkIn, checkOut) 与已有订单日期范围存在交集即视为冲突。
     * 仅统计 CREATED、PAID、FINISHED 等有效订单，排除已取消订单。
     * 参数通过 #{param} 预编译绑定，防止 SQL 注入。
     * 
     * @param roomId 房间 ID
     * @param checkInDate 拟入住日期
     * @param checkOutDate 拟退房日期
     * @return 冲突的订单数量，大于 0 表示该日期段已被预订
     */
    int countActiveOverlap(@Param("roomId") Long roomId,
                           @Param("checkInDate") LocalDate checkInDate,
                           @Param("checkOutDate") LocalDate checkOutDate);
}
