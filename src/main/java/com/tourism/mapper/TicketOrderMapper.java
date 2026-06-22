package com.tourism.mapper;

import com.tourism.entity.TicketOrder;
import org.apache.ibatis.annotations.Param;

/**
 * 门票订单数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（TicketOrderMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口提供门票订单的创建、查询和状态更新功能。
 */
public interface TicketOrderMapper {
    
    /**
     * 插入一条新的门票订单
     * 
     * 对应 SQL：insert into ticket_order (...) values (...)
     * 使用 useGeneratedKeys 自动获取数据库生成的主键值，并回填到实体对象的 id 属性中。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param order 门票订单实体对象，包含用户ID、门票ID、游玩日期、数量、金额、状态等
     * @return 受影响的行数，成功插入时通常为 1
     */
    int insert(TicketOrder order);

    /**
     * 根据订单 ID 查询门票订单详情
     * 
     * 对应 SQL：select ... from ticket_order where id = #{id}
     * 通过 #{id} 预编译参数绑定，防止 SQL 注入攻击。
     * 
     * @param id 门票订单主键 ID
     * @return 对应的 TicketOrder 实体对象，若不存在则返回 null
     */
    TicketOrder selectById(Long id);

    /**
     * 更新门票订单状态
     * 
     * 对应 SQL：update ticket_order set order_status = #{orderStatus} where id = #{id}
     * 通过 @Param 注解指定参数名，确保 XML 中 #{id} 和 #{orderStatus} 能够正确映射。
     * 参数均为预编译绑定，防止 SQL 注入。
     * 
     * @param id 待更新状态的订单 ID
     * @param orderStatus 新的订单状态（如 CREATED, PAID, FINISHED, CANCELLED）
     * @return 受影响的行数
     */
    int updateStatus(@Param("id") Long id, @Param("orderStatus") String orderStatus);
}
