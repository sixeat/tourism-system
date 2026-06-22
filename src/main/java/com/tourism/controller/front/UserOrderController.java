package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import com.tourism.service.UserOrderService;
import com.tourism.vo.UserOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 前端用户订单控制器（前端控制器）
 *
 * <p>该类负责处理当前登录用户的订单查询请求，聚合展示用户的全部订单（酒店订单+门票订单）。
 * 作为前端Controller层，接收客户端HTTP请求，从用户会话（Session）中提取当前登录用户ID，
 * 调用UserOrderService业务层查询订单数据，并将结果封装为统一API响应返回。</p>
 *
 * <p>核心职责：
 * <ul>
 *   <li>查询当前登录用户的所有订单（聚合酒店订单和门票订单）</li>
 *   <li>从Session中获取用户ID，无需前端传递，防止伪造</li>
 *   <li>未登录时返回失败提示，引导用户登录</li>
 * </ul></p>
 *
 * <p>Session机制说明：
 * 用户登录成功后，服务器将用户ID存入HttpSession（键为 SessionConstants.LOGIN_USER_ID）。
 * 浏览器通过Cookie携带JSESSIONID，Spring据此找到对应Session并注入到HttpSession参数中。
 * 此控制器通过Session获取用户ID，确保只能查询当前登录用户自己的订单数据。</p>
 *
 * <p>VO模式说明：
 * 本接口返回 UserOrderVO（View Object）而非直接返回 HotelOrder/TicketOrder 实体，
 * 是因为需要将两种订单类型聚合为统一结构，并补充前端展示所需的额外字段（如订单类型、商品名称等）。
 * VO 模式实现了视图层与数据层的解耦，提升接口安全性和灵活性。</p>
 *
 * @author tourism-system
 * @version 1.0
 */
@RestController
// @RestController 标记该类为RESTful控制器，所有方法返回值直接写入HTTP响应体（JSON格式），
// 它是 @Controller + @ResponseBody 的复合注解，用于前后端分离架构。
// @Controller 将类标记为Spring MVC控制器，由 Spring 容器扫描并管理；
// @ResponseBody 指示方法返回值直接写入HTTP响应体，不再经过视图解析器。
@RequestMapping("/api/user/orders")
// @RequestMapping 标注在类上，定义该类所有接口的URL前缀为 /api/user/orders
// 路径前缀体现资源归属：/api/user/... 表示当前登录用户的个人资源操作
public class UserOrderController {

    @Autowired
    // @Autowired 自动注入Spring容器中类型匹配的UserOrderService Bean实例
    // Spring 的依赖注入（DI）机制会自动扫描容器中 UserOrderService 类型的 Bean，
    // 通过反射将其赋值给该字段。控制器无需手动创建服务对象，实现层间解耦。
    private UserOrderService userOrderService;
    // userOrderService：用户订单业务逻辑服务接口，封装了用户订单查询、聚合、状态统计等核心业务逻辑

    /**
     * 查询当前用户的所有订单
     *
     * <p>从当前用户Session中获取用户ID，若未登录则返回失败提示；
     * 若已登录，调用用户订单服务查询该用户的所有订单（酒店订单+门票订单的聚合列表），
     * 并返回统一格式的视图对象列表。适用于"我的订单"页面，展示用户的全部历史订单。</p>
     *
     * @param session HttpSession 对象，Spring 自动注入当前请求的会话对象。
     *                通过浏览器Cookie中的JSESSIONID关联到服务器端Session存储。
     *                HttpSession 是 Servlet 标准提供的会话管理接口，Spring MVC 会自动将其绑定到方法参数。
     * @return ApiResponse<List<UserOrderVO>> 统一API响应。
     *         - 未登录时：返回 ApiResponse.fail("not logged in")，code 为失败状态码，前端可跳转登录页。
     *         - 已登录时：返回 ApiResponse.success(orderList)，data 为 UserOrderVO 列表。
     *         UserOrderVO（View Object）是专门为"我的订单"页面设计的视图对象，聚合了酒店订单和门票订单的
     *         公共字段，如：id、orderType（订单类型：HOTEL/TICKET）、itemName（商品名称）、
     *         totalAmount（订单金额）、orderStatus（订单状态）、useDate（使用日期）、
     *         checkInDate/checkOutDate（酒店入住/退房日期）、visitDate（门票游览日期）、
     *         quantity（门票数量）、createTime（订单创建时间）等。
     *         VO 模式将不同实体的字段统一为前端友好的结构，避免前端处理多实体差异的复杂性。
     */
    @GetMapping
    // @GetMapping 是 @RequestMapping(method = RequestMethod.GET) 的缩写，
    // 用于映射 HTTP GET 请求。GET 语义为"获取/查询资源"，适合查询类接口。
    // 方法未指定额外路径，完整路径 = 类上的前缀 = /api/user/orders
    public ApiResponse<List<UserOrderVO>> list(HttpSession session) {
        // 从 HttpSession 中获取用户登录时存储的用户ID属性
        // SessionConstants.LOGIN_USER_ID 是常量字符串，定义了Session中存储用户ID的键名（通常为"loginUserId"）
        Object userId = session.getAttribute(SessionConstants.LOGIN_USER_ID);

        // 判断用户是否已登录：若 Session 中无用户ID，说明用户未登录或Session已过期
        if (userId == null) {
            // 用户未登录，返回失败响应，提示信息为 "not logged in"
            // 前端收到此响应后可判断 code 为失败状态，提示用户登录或跳转登录页面
            return ApiResponse.fail("not logged in");
        }

        // 用户已登录，将 userId 从 Object 类型转换为 Long 类型
        // 转换方式：String.valueOf(userId) 先将 Object 转为 String（防止null），
        // Long.valueOf(...) 再将 String 转为 Long。
        Long uid = Long.valueOf(String.valueOf(userId));

        // 调用用户订单服务查询该用户的所有订单
        // 业务层会聚合查询：同时查询酒店订单表和门票订单表中 user_id = uid 的记录，
        // 将结果统一转换为 UserOrderVO 列表，通常按创建时间倒序排列（最新订单在前）。
        // 聚合查询的优势：前端只需调用一个接口即可展示全部订单，无需分别调用酒店订单和门票订单接口。
        List<UserOrderVO> orderList = userOrderService.listUserOrders(uid);

        // 将订单列表封装到 ApiResponse 统一响应对象中返回
        // ApiResponse.success(orderList) 设置成功状态码和提示信息，将订单列表放入 data 字段
        // 前端"我的订单"页面可遍历 orderList，根据 orderType 渲染不同订单卡片（酒店订单 vs 门票订单）
        return ApiResponse.success(orderList);
    }
}
