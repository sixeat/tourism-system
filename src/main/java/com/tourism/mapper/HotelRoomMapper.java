package com.tourism.mapper;

import com.tourism.entity.HotelRoom;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 酒店房间数据访问层 Mapper 接口
 * 
 * 本接口遵循 MyBatis Mapper 接口模式，通过 XML 映射文件（HotelRoomMapper.xml）
 * 中的 namespace 自动扫描并绑定 SQL 语句。因此无需在接口上添加 @Mapper 注解，
 * 依赖 Spring Boot 的 mapper-locations 配置完成扫描注册。
 * 
 * 该接口提供酒店房间的查询、库存扣减与库存恢复等功能。
 */
public interface HotelRoomMapper {
    
    /**
     * 根据酒店 ID 查询该酒店下的所有房间列表
     * 
     * 对应 SQL：select ... from hotel_room where hotel_id = #{hotelId}
     * 通过 #{hotelId} 预编译参数绑定，防止 SQL 注入攻击。
     * 结果按 price 升序排列，方便用户从低价到高价浏览。
     * 
     * @param hotelId 酒店主键 ID
     * @return 该酒店下的房间列表，包含房间类型、价格、库存、状态等信息
     */
    List<HotelRoom> selectByHotelId(Long hotelId);

    /**
     * 根据房间 ID 查询单个房间详情
     * 
     * 对应 SQL：select ... from hotel_room where id = #{id}
     * 通过 #{id} 预编译参数绑定，防止 SQL 注入攻击。
     * 
     * @param id 房间主键 ID
     * @return 对应的 HotelRoom 实体对象，若不存在则返回 null
     */
    HotelRoom selectById(Long id);

    /**
     * 扣减房间库存（预订时调用）
     * 
     * 对应 SQL：update hotel_room set stock = stock - 1 where id = #{id} and stock > 0
     * 通过 @Param("id") 指定参数名，确保 XML 映射正确。
     * 使用乐观锁思想：仅在 stock > 0 时执行扣减，防止超卖。
     * 参数通过 #{id} 预编译绑定，防止 SQL 注入。
     * 
     * @param id 待扣减库存的房间 ID
     * @return 受影响的行数，为 1 表示扣减成功，为 0 表示库存不足
     */
    int deductStock(@Param("id") Long id);

    /**
     * 恢复房间库存（取消订单时调用）
     * 
     * 对应 SQL：update hotel_room set stock = stock + 1 where id = #{id}
     * 通过 @Param("id") 指定参数名，确保 XML 映射正确。
     * 通常在订单取消或退款流程中调用，将房间可预订数量加 1。
     * 参数通过 #{id} 预编译绑定，防止 SQL 注入。
     * 
     * @param id 待恢复库存的房间 ID
     * @return 受影响的行数
     */
    int restoreStock(@Param("id") Long id);
}
