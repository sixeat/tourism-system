package com.tourism.mapper;

import com.tourism.entity.Ticket;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 门票信息数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（TicketMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口提供门票信息的完整 CRUD 操作，以及库存扣减与恢复功能。
 */
public interface TicketMapper {
    
    /**
     * 查询所有门票信息
     * 
     * 对应 SQL：select ... from ticket order by id asc
     * 查询 ticket 表全部记录，结果按 id 升序排列。
     * 
     * @return 所有门票信息的列表
     */
    List<Ticket> selectAll();

    /**
     * 根据景点 ID 查询该景点下的所有门票
     * 
     * 对应 SQL：select ... from ticket where scenic_id = #{scenicId}
     * 结果按 available_date 升序排列，方便用户按日期浏览可用门票。
     * 参数通过 #{scenicId} 预编译绑定，防止 SQL 注入攻击。
     * 
     * @param scenicId 景点主键 ID
     * @return 该景点下的门票列表
     */
    List<Ticket> selectByScenicId(Long scenicId);

    /**
     * 根据门票 ID 查询单个门票详情
     * 
     * 对应 SQL：select ... from ticket where id = #{id}
     * 通过 #{id} 预编译参数绑定，防止 SQL 注入攻击。
     * 
     * @param id 门票主键 ID
     * @return 对应的 Ticket 实体对象，若不存在则返回 null
     */
    Ticket selectById(Long id);

    /**
     * 扣减门票库存（购票时调用）
     * 
     * 对应 SQL：update ticket set stock = stock - #{quantity} where id = #{id} and stock >= #{quantity}
     * 通过 @Param 注解明确参数名，确保 XML 中的 #{id} 和 #{quantity} 正确映射。
     * 使用乐观锁思想：仅在 stock >= quantity 时执行扣减，防止超卖。
     * 参数通过 #{param} 预编译绑定，防止 SQL 注入。
     * 
     * @param id 待扣减库存的门票 ID
     * @param quantity 购买数量
     * @return 受影响的行数，为 1 表示扣减成功，为 0 表示库存不足
     */
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 恢复门票库存（取消订单时调用）
     * 
     * 对应 SQL：update ticket set stock = stock + #{quantity} where id = #{id}
     * 通过 @Param 注解明确参数名，确保 XML 中的 #{id} 和 #{quantity} 正确映射。
     * 通常在订单取消或退款流程中调用，将门票库存加回。
     * 参数通过 #{param} 预编译绑定，防止 SQL 注入。
     * 
     * @param id 待恢复库存的门票 ID
     * @param quantity 恢复数量
     * @return 受影响的行数
     */
    int restoreStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 插入一条新的门票记录
     * 
     * 对应 SQL：insert into ticket (...) values (...)
     * 使用 useGeneratedKeys 自动获取数据库生成的主键值，并回填到实体对象的 id 属性中。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param ticket 门票实体对象，包含景点ID、门票名称、价格、库存、可用日期等
     * @return 受影响的行数，成功插入时通常为 1
     */
    int insert(Ticket ticket);

    /**
     * 根据门票 ID 更新门票信息
     * 
     * 对应 SQL：update ticket set ... where id = #{id}
     * 全字段更新，要求传入完整的 Ticket 实体对象。若字段为 null，数据库中对应列也会被更新为 null。
     * 参数通过 #{property} 预编译绑定，防止 SQL 注入。
     * 
     * @param ticket 包含更新后字段值的门票实体对象
     * @return 受影响的行数，成功更新时通常为 1
     */
    int updateById(Ticket ticket);

    /**
     * 根据门票 ID 删除门票记录
     * 
     * 对应 SQL：delete from ticket where id = #{id}
     * 通过 #{id} 预编译参数绑定，防止 SQL 注入攻击。
     * 
     * @param id 待删除门票的主键 ID
     * @return 受影响的行数，成功删除时通常为 1
     */
    int deleteById(Long id);
}
