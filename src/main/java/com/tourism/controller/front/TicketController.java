package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.entity.Ticket;
import com.tourism.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前端门票控制器（前端控制器）
 *
 * <p>该类负责处理景点门票（Ticket）相关的HTTP请求，提供按景点查询门票列表的功能。
 * 作为前端Controller层，接收客户端HTTP请求，调用TicketService业务层查询门票数据，
 * 并将结果封装为统一API响应返回。适用于景点详情页展示可购买门票类型。</p>
 *
 * <p>核心职责：
 * <ul>
 *   <li>按景点ID查询该景点下的所有门票类型</li>
 *   <li>为前端门票预订页面提供数据支持</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
@RestController
// @RestController 标记该类为RESTful控制器，所有方法返回值直接写入HTTP响应体（JSON格式），
// 它是 @Controller + @ResponseBody 的复合注解，用于前后端分离架构。
// @Controller 将类标记为Spring MVC控制器，由 Spring 容器扫描并管理；
// @ResponseBody 指示方法返回值直接写入HTTP响应体，不再经过视图解析器（如Thymeleaf/JSP）。
@RequestMapping("/api/ticket")
// @RequestMapping 标注在类上，定义该类所有接口的URL前缀为 /api/ticket
// 例如：list() 方法的完整路径为 /api/ticket/list
public class TicketController {

    @Autowired
    // @Autowired 自动注入Spring容器中类型匹配的TicketService Bean实例
    // Spring 的依赖注入（DI）机制会自动扫描容器中 TicketService 类型的 Bean，
    // 通过反射将其赋值给该字段。控制器无需手动 new 服务对象，实现层间解耦。
    // 若容器中存在多个实现类，需使用 @Primary 或 @Qualifier 消除歧义。
    private TicketService ticketService;
    // ticketService：门票业务逻辑服务接口，封装了门票查询、库存管理、价格计算等核心业务逻辑

    /**
     * 按景点查询门票列表
     *
     * <p>接收前端传入的景点ID，查询该景点下所有可售门票类型（如成人票、学生票、家庭套票等），
     * 返回门票实体列表。适用于景点详情页或门票预订页面，展示各票型的价格、库存、使用规则等。</p>
     *
     * @param scenicId 景点唯一标识（Long类型），通过URL查询参数传递。
     *                 @RequestParam 注解表示从HTTP请求的URL查询字符串（Query String）中按名称提取参数值。
     *                 例如请求 /api/ticket/list?scenicId=10，则 scenicId 参数值为 10。
     *                 @RequestParam 默认 required=true，即该参数必须提供，否则Spring抛出异常。
     *                 Spring 的类型转换器会自动将String类型的参数值转换为Long类型。
     * @return ApiResponse<List<Ticket>> 统一API响应，data字段为Ticket实体列表。
     *         Ticket 是门票数据库实体类，通常包含字段：
     *         - id：门票唯一标识
     *         - scenicId：关联景点ID
     *         - ticketType：票型（如"成人票"、"学生票"、"儿童票"）
     *         - price：票价（BigDecimal）
     *         - stock：库存数量
     *         - description：使用说明（如"需提前一天预订"、"包含景区内交通"）
     *         - validDays：有效天数
     *         - refundPolicy：退改规则
     *         前端可据此渲染门票选择卡片，用户选择票型、数量后提交订单。
     */
    @GetMapping("/list")
    // @GetMapping 是 @RequestMapping(method = RequestMethod.GET) 的缩写，
    // 用于映射 HTTP GET 请求。GET 请求语义为"获取/查询资源"，不会对服务器数据产生副作用，
    // 适合查询类接口。此处用于查询门票列表，符合RESTful设计规范。
    // 完整路径：/api/ticket/list
    public ApiResponse<List<Ticket>> list(@RequestParam Long scenicId) {
        // 调用 ticketService.listByScenicId(scenicId) 方法，传入景点ID
        // 业务层会根据 scenicId 查询数据库中 ticket.scenic_id = 传入ID 的门票记录，
        // 可能包含过滤逻辑：如仅返回上架状态、库存大于0的门票类型。
        List<Ticket> ticketList = ticketService.listByScenicId(scenicId);

        // 将查询结果封装到 ApiResponse 统一响应对象中返回
        // ApiResponse.success(ticketList) 会设置 code=200，message="success"，data=ticketList
        // 确保前端接收的数据结构一致，便于前端统一处理响应逻辑和错误提示。
        // 前端门票预订页面可遍历 ticketList 展示各票型信息，供用户选择购买。
        return ApiResponse.success(ticketList);
    }
}
