package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.entity.Hotel;
import com.tourism.entity.HotelRoom;
import com.tourism.service.HotelService;
import com.tourism.vo.HotelRoomAvailabilityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 前端酒店控制器（前端控制器）
 *
 * <p>该类负责处理酒店相关的HTTP请求，涵盖酒店列表查询、酒店房间列表查询、
 * 房间可用性查询等功能。作为前端Controller层，接收客户端HTTP请求，调用HotelService
 * 业务层处理数据，并将结果封装为统一API响应返回。</p>
 *
 * <p>核心职责：
 * <ul>
 *   <li>按城市查询酒店列表</li>
 *   <li>查询指定酒店的所有房间类型</li>
 *   <li>查询指定日期范围内指定酒店的房间可用性（库存、价格、锁定状态等）</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
@RestController
// @RestController 标记该类为RESTful控制器，所有方法返回值直接写入HTTP响应体（JSON格式），
// 无需经过视图解析器。它是 @Controller + @ResponseBody 的复合注解，用于前后端分离架构。
@RequestMapping("/api/hotel")
// @RequestMapping 标注在类上，定义该类所有接口的URL前缀为 /api/hotel
// 例如：list() 方法的完整路径为 /api/hotel/list
public class HotelController {

    @Autowired
    // @Autowired 自动注入Spring容器中类型匹配的HotelService Bean实例
    // 注入原理：Spring通过反射将容器中已初始化的HotelService实现类对象赋值给该字段，
    // 实现依赖注入（DI），控制器无需手动 new 服务对象，降低耦合度
    private HotelService hotelService;
    // hotelService：酒店业务逻辑服务接口，封装了酒店查询、房间管理等核心业务逻辑

    /**
     * 按城市查询酒店列表
     *
     * <p>接收前端传入的城市名称，调用酒店服务层查询该城市下的所有酒店信息，
     * 返回酒店实体列表。适用于酒店预订首页的城市筛选场景。</p>
     *
     * @param city 城市名称（中文，如"北京"、"上海"），通过URL查询参数传递。
     *             参数前的 @RequestParam 注解表示：从HTTP请求的URL查询字符串（Query String）
     *             中按名称提取参数值。例如请求 /api/hotel/list?city=北京，则 city 参数值为"北京"。
     *             @RequestParam 默认 required=true，即该参数必须提供，否则Spring抛出异常。
     * @return ApiResponse<List<Hotel>> 统一API响应，data字段为Hotel实体列表，
     *         包含各酒店的ID、名称、地址、星级、图片等详细信息。
     */
    @GetMapping("/list")
    // @GetMapping 映射 HTTP GET 请求，用于获取资源。语义为只读查询，不修改数据。
    // 完整路径：/api/hotel/list
    public ApiResponse<List<Hotel>> list(@RequestParam String city) {
        // 调用 hotelService.listByCity(city) 方法，将城市参数传递给业务层
        // 业务层会查询数据库或缓存，返回该城市下的所有酒店实体对象列表
        List<Hotel> hotelList = hotelService.listByCity(city);

        // 将查询结果封装到 ApiResponse 统一响应对象中返回
        // 前端可通过响应体的 code 字段判断请求是否成功，通过 data 字段获取酒店列表数据
        return ApiResponse.success(hotelList);
    }

    /**
     * 查询指定酒店的所有房间类型
     *
     * <p>接收前端传入的酒店ID，调用酒店服务层查询该酒店下的所有房间类型列表，
     * 返回房间基本信息（房型、基础价格、描述等）。适用于酒店详情页展示房型选项。</p>
     *
     * @param hotelId 酒店唯一标识（Long类型），通过URL查询参数传递。
     *                例如请求 /api/hotel/rooms?hotelId=1，则 hotelId 为 1。
     *                @RequestParam 会自动将String类型的参数值转换为Long类型（Spring类型转换器）。
     * @return ApiResponse<List<HotelRoom>> 统一API响应，data字段为HotelRoom实体列表，
     *         包含各房型的ID、房型名称、基础价格、面积、床型、入住人数等信息。
     */
    @GetMapping("/rooms")
    // @GetMapping 映射 HTTP GET 请求，语义为查询指定酒店的房间资源
    // 完整路径：/api/hotel/rooms
    public ApiResponse<List<HotelRoom>> rooms(@RequestParam Long hotelId) {
        // 调用 hotelService.listRooms(hotelId) 方法，传入酒店ID，
        // 业务层根据酒店ID查询数据库中关联的房间记录，返回房间实体列表
        List<HotelRoom> roomList = hotelService.listRooms(hotelId);

        // 将查询结果封装到统一响应对象中，确保前端接收的数据格式一致
        return ApiResponse.success(roomList);
    }

    /**
     * 查询指定酒店在指定日期范围内的房间可用性
     *
     * <p>接收酒店ID、入住日期、退房日期，查询该时间段内各房型的可用库存、
     * 实际可售数量、锁定状态、价格等信息。这是酒店预订的核心接口，
     * 前端在提交订单前需调用此接口确认房间是否有库存。</p>
     *
     * @param hotelId 酒店唯一标识，通过URL查询参数传递
     * @param checkInDate 入住日期，java.time.LocalDate 类型，通过URL查询参数传递。
     *                    参数前的 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 注解
     *                    用于指定日期字符串的解析格式：ISO.DATE 表示 yyyy-MM-dd 格式
     *                    （如 2024-01-15）。Spring 的格式转换器会自动将请求中的字符串
     *                    转换为 LocalDate 对象。若格式不匹配，Spring 会抛出转换异常。
     * @param checkOutDate 退房日期，格式同 checkInDate，需晚于入住日期，
     *                     业务层通常会校验日期逻辑合法性（如不能晚于当天、不能大于30天等）。
     * @return ApiResponse<List<HotelRoomAvailabilityVO>> 统一API响应，data字段为
     *         房间可用性视图对象列表（VO），包含库存、锁定数、可用数、价格、状态文本等
     *         前端展示所需的丰富信息。VO（View Object）模式用于将多个实体/计算结果
     *         聚合为前端友好的数据结构，避免直接暴露数据库实体字段。
     */
    @GetMapping("/room-availability")
    // @GetMapping 映射 HTTP GET 请求，查询房间可用性信息
    // 完整路径：/api/hotel/room-availability
    public ApiResponse<List<HotelRoomAvailabilityVO>> roomAvailability(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        // 调用 hotelService.listRooms(hotelId, checkInDate, checkOutDate) 重载方法，
        // 业务层会根据三个参数计算各房型在日期范围内的可用库存：
        // 1. 查询总库存（stock）
        // 2. 查询已锁定/已预订数量（lockedCount）
        // 3. 计算可用数量 = stock - lockedCount
        // 4. 封装为 HotelRoomAvailabilityVO 返回
        List<HotelRoomAvailabilityVO> availabilityList = hotelService.listRooms(hotelId, checkInDate, checkOutDate);

        // 将可用性查询结果封装到统一响应对象返回前端
        // 前端可根据 availableCount、available 等字段展示"可预订"/"已售罄"状态
        return ApiResponse.success(availabilityList);
    }
}
