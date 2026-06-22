package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import com.tourism.dto.HotelOrderCreateRequest;
import com.tourism.dto.TicketOrderCreateRequest;
import com.tourism.entity.HotelOrder;
import com.tourism.entity.TicketOrder;
import com.tourism.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 前端订单控制器（前端控制器）
 *
 * <p>该类负责处理订单相关的HTTP请求，包括酒店订单和门票订单的创建、取消、支付等操作。
 * 作为前端Controller层，接收客户端HTTP请求，从用户会话（Session）中提取当前登录用户ID，
 * 调用OrderService业务层处理订单逻辑，并将结果封装为统一API响应返回。</p>
 *
 * <p>核心职责：
 * <ul>
 *   <li>酒店订单创建、取消、支付</li>
 *   <li>门票订单创建、取消、支付</li>
 *   <li>从Session中获取当前登录用户ID，确保订单归属正确</li>
 * </ul></p>
 *
 * <p>Session机制说明：
 * 用户登录成功后，服务器会将用户ID存入HttpSession中（键为 SessionConstants.LOGIN_USER_ID）。
 * 后续请求中，浏览器通过Cookie携带Session ID，服务器据此找到对应的Session对象，
 * 从而获取用户身份信息。这是传统Session-Cookie认证模式的典型应用。</p>
 *
 * @author tourism-system
 * @version 1.0
 */
@RestController
// @RestController 标记该类为RESTful控制器，所有方法返回值直接写入HTTP响应体（JSON格式），
// 它是 @Controller + @ResponseBody 的复合注解，用于前后端分离架构
@RequestMapping("/api/order")
// @RequestMapping 标注在类上，定义该类所有接口的URL前缀为 /api/order
public class OrderController {

    @Autowired
    // @Autowired 自动注入Spring容器中类型匹配的OrderService Bean实例
    // 通过依赖注入获取订单业务逻辑服务对象，控制器无需手动创建实例
    private OrderService orderService;
    // orderService：订单业务逻辑服务接口，封装了酒店订单和门票订单的创建、取消、支付等业务逻辑

    /**
     * 创建酒店订单
     *
     * <p>接收前端提交的房间预订信息，从当前用户Session中提取用户ID并设置到请求对象中，
     * 调用订单服务创建酒店订单。适用于酒店预订页面的"提交订单"场景。</p>
     *
     * @param request 酒店订单创建请求DTO，包含用户ID（会被覆盖）、酒店ID、房间ID、
     *                入住日期、退房日期等信息。
     *                @RequestBody 注解表示将HTTP请求体（JSON）反序列化为 HotelOrderCreateRequest 对象。
     * @param session HttpSession 对象，Spring 自动注入当前请求的会话对象。
     *                浏览器每次请求时会通过Cookie携带JSESSIONID，Spring根据此ID找到对应的Session。
     *                从Session中可获取用户登录时存储的属性（如用户ID、用户名等）。
     * @return ApiResponse<HotelOrder> 统一API响应，data字段为创建成功的酒店订单实体，
     *         包含订单ID、订单状态、创建时间等完整信息。响应消息为"酒店订单创建成功"。
     */
    @PostMapping("/hotel/create")
    // @PostMapping 映射 HTTP POST 请求，语义为"创建资源"。此处用于创建酒店订单，符合RESTful规范。
    // 完整路径：/api/order/hotel/create
    public ApiResponse<HotelOrder> createHotelOrder(@RequestBody HotelOrderCreateRequest request, HttpSession session) {
        // 从 HttpSession 中获取当前登录用户ID，键为 SessionConstants.LOGIN_USER_ID
        // session.getAttribute(...) 返回 Object 类型，先通过 String.valueOf(...) 转为String，
        // 再通过 Long.valueOf(...) 转为 Long 类型，赋值给DTO的userId字段
        // 这样设计确保订单归属于当前登录用户，防止前端伪造用户ID提交他人订单
        request.setUserId(Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID))));

        // 调用订单服务的酒店订单创建方法，传入填充了用户ID的DTO
        // 业务层会校验：房间是否存在、日期是否合法、库存是否充足、用户余额是否足够等
        HotelOrder createdOrder = orderService.createHotelOrder(request);

        // 将创建的订单实体封装到统一响应对象中返回
        // 第一个参数为提示消息（"酒店订单创建成功"），第二个参数为业务数据（HotelOrder对象）
        // 前端可根据返回的订单ID跳转至订单详情页或支付页面
        return ApiResponse.success("酒店订单创建成功", createdOrder);
    }

    /**
     * 创建门票订单
     *
     * <p>接收前端提交的门票购买信息，从当前用户Session中提取用户ID并设置到请求对象中，
     * 调用订单服务创建门票订单。适用于景点门票预订页面的"提交订单"场景。</p>
     *
     * @param request 门票订单创建请求DTO，包含用户ID（会被覆盖）、门票ID、游览日期、购买数量等信息。
     *                @RequestBody 将HTTP请求体（JSON）反序列化为 TicketOrderCreateRequest 对象。
     * @param session HttpSession 对象，Spring 自动注入，用于获取当前登录用户ID。
     * @return ApiResponse<TicketOrder> 统一API响应，data字段为创建成功的门票订单实体，
     *         包含订单ID、订单状态、创建时间等。响应消息为"门票订单创建成功"。
     */
    @PostMapping("/ticket/create")
    // @PostMapping 映射 HTTP POST 请求，创建门票订单
    // 完整路径：/api/order/ticket/create
    public ApiResponse<TicketOrder> createTicketOrder(@RequestBody TicketOrderCreateRequest request, HttpSession session) {
        // 从Session中获取当前登录用户ID，并设置到请求DTO中，确保订单归属正确
        request.setUserId(Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID))));

        // 调用订单服务创建门票订单
        // 业务层会校验：门票是否存在、游览日期是否合法、库存是否充足、用户余额是否足够等
        TicketOrder createdOrder = orderService.createTicketOrder(request);

        // 封装成功响应返回前端，消息提示为"门票订单创建成功"
        return ApiResponse.success("门票订单创建成功", createdOrder);
    }

    /**
     * 取消酒店订单
     *
     * <p>接收订单ID，从当前用户Session中获取用户ID，调用订单服务取消指定酒店订单。
     * 用户只能取消自己的订单，业务层会校验订单归属和状态（如仅允许取消"待支付"订单）。</p>
     *
     * @param orderId 酒店订单ID，通过URL查询参数传递（@RequestParam）。
     *                例如请求 /api/order/hotel/cancel?orderId=123，则 orderId 为 123。
     * @param session HttpSession 对象，用于获取当前登录用户ID，确保操作权限正确。
     * @return ApiResponse<String> 统一API响应，data字段为简单字符串"OK"，表示操作成功。
     *         响应消息为"酒店订单已取消"。
     */
    @PostMapping("/hotel/cancel")
    // @PostMapping 映射 HTTP POST 请求，语义为"提交操作"。虽然取消涉及状态变更，
    // 但此处使用POST而非DELETE，是因为该操作可能涉及复杂的退款、库存回滚等流程，
    // 而非简单的资源删除。完整路径：/api/order/hotel/cancel
    public ApiResponse<String> cancelHotelOrder(@RequestParam Long orderId, HttpSession session) {
        // 从Session中获取当前登录用户ID，转换为Long类型
        Long userId = Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID)));

        // 调用订单服务取消酒店订单，传入订单ID和用户ID
        // 业务层会校验：订单是否存在、是否属于当前用户、状态是否允许取消（如未支付/已支付）等
        orderService.cancelHotelOrder(orderId, userId);

        // 返回成功响应，消息为"酒店订单已取消"，data为"OK"
        // 前端可据此提示用户"订单已取消成功"，并刷新订单列表
        return ApiResponse.success("酒店订单已取消", "OK");
    }

    /**
     * 取消门票订单
     *
     * <p>接收订单ID，从当前用户Session中获取用户ID，调用订单服务取消指定门票订单。
     * 用户只能取消自己的订单，业务层会校验订单归属和状态。</p>
     *
     * @param orderId 门票订单ID，通过URL查询参数传递。
     * @param session HttpSession 对象，用于获取当前登录用户ID。
     * @return ApiResponse<String> 统一API响应，data字段为"OK"，消息为"门票订单已取消"。
     */
    @PostMapping("/ticket/cancel")
    // @PostMapping 映射 HTTP POST 请求，取消门票订单
    // 完整路径：/api/order/ticket/cancel
    public ApiResponse<String> cancelTicketOrder(@RequestParam Long orderId, HttpSession session) {
        // 从Session中获取当前登录用户ID
        Long userId = Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID)));

        // 调用订单服务取消门票订单，传入订单ID和用户ID
        // 业务层会校验订单归属和状态，执行退款、库存回滚等逻辑
        orderService.cancelTicketOrder(orderId, userId);

        // 返回成功响应，提示"门票订单已取消"
        return ApiResponse.success("门票订单已取消", "OK");
    }

    /**
     * 支付酒店订单
     *
     * <p>接收订单ID，从当前用户Session中获取用户ID，调用订单服务完成酒店订单支付。
     * 支付成功后订单状态通常从"待支付"变为"已支付"，并触发后续通知或确认流程。</p>
     *
     * @param orderId 酒店订单ID，通过URL查询参数传递。
     * @param session HttpSession 对象，用于获取当前登录用户ID。
     * @return ApiResponse<String> 统一API响应，data字段为"OK"，消息为"酒店订单支付成功"。
     */
    @PostMapping("/hotel/pay")
    // @PostMapping 映射 HTTP POST 请求，提交酒店订单支付操作
    // 完整路径：/api/order/hotel/pay
    public ApiResponse<String> payHotelOrder(@RequestParam Long orderId, HttpSession session) {
        // 从Session中获取当前登录用户ID，转换为Long类型
        Long userId = Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID)));

        // 调用订单服务执行酒店订单支付逻辑
        // 业务层会校验：订单是否存在、是否属于当前用户、状态是否为"待支付"、余额是否充足等
        // 实际项目中可能对接第三方支付（微信/支付宝），此处为简化演示的本地支付逻辑
        orderService.payHotelOrder(orderId, userId);

        // 返回成功响应，提示"酒店订单支付成功"
        return ApiResponse.success("酒店订单支付成功", "OK");
    }

    /**
     * 支付门票订单
     *
     * <p>接收订单ID，从当前用户Session中获取用户ID，调用订单服务完成门票订单支付。
     * 支付成功后订单状态更新，通常伴随电子票生成或短信通知。</p>
     *
     * @param orderId 门票订单ID，通过URL查询参数传递。
     * @param session HttpSession 对象，用于获取当前登录用户ID。
     * @return ApiResponse<String> 统一API响应，data字段为"OK"，消息为"门票订单支付成功"。
     */
    @PostMapping("/ticket/pay")
    // @PostMapping 映射 HTTP POST 请求，提交门票订单支付操作
    // 完整路径：/api/order/ticket/pay
    public ApiResponse<String> payTicketOrder(@RequestParam Long orderId, HttpSession session) {
        // 从Session中获取当前登录用户ID
        Long userId = Long.valueOf(String.valueOf(session.getAttribute(SessionConstants.LOGIN_USER_ID)));

        // 调用订单服务执行门票订单支付逻辑
        // 业务层校验订单合法性后执行扣款/状态更新
        orderService.payTicketOrder(orderId, userId);

        // 返回成功响应，提示"门票订单支付成功"
        return ApiResponse.success("门票订单支付成功", "OK");
    }
}
