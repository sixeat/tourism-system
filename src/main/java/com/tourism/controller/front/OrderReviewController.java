package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import com.tourism.dto.OrderReviewRequest;
import com.tourism.entity.OrderReview;
import com.tourism.service.OrderReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 前端订单评价控制器（前端控制器）
 *
 * <p>该类负责处理用户订单评价相关的HTTP请求，包括查询当前用户的评价列表和提交新评价。
 * 作为前端Controller层，接收客户端HTTP请求，从用户会话（Session）中提取当前登录用户ID，
 * 调用OrderReviewService业务层处理评价逻辑，并将结果封装为统一API响应返回。</p>
 *
 * <p>核心职责：
 * <ul>
 *   <li>查询当前登录用户的所有订单评价</li>
 *   <li>提交新的订单评价（评分、文字内容）</li>
 *   <li>从Session中安全获取用户ID，确保评价数据归属正确</li>
 * </ul></p>
 *
 * <p>Session机制说明：
 * 用户登录后，其用户ID被存储在HttpSession中（键为 SessionConstants.LOGIN_USER_ID）。
 * 控制器通过 HttpSession 参数获取用户ID，无需前端显式传递，防止用户伪造他人身份提交评价。
 * Spring 会自动将当前请求的 Session 对象注入到控制器方法的 HttpSession 参数中。</p>
 *
 * @author tourism-system
 * @version 1.0
 */
@RestController
// @RestController 标记该类为RESTful控制器，所有方法返回值直接写入HTTP响应体（JSON格式），
// 它是 @Controller + @ResponseBody 的复合注解，用于前后端分离架构
@RequestMapping("/api/user/reviews")
// @RequestMapping 标注在类上，定义该类所有接口的URL前缀为 /api/user/reviews
// 路径设计体现资源归属：/api/user/... 表示用户相关的个人资源操作
public class OrderReviewController {

    @Autowired
    // @Autowired 自动注入Spring容器中类型匹配的OrderReviewService Bean实例
    // 依赖注入使控制器无需关心服务对象的创建和生命周期管理，由Spring IoC容器统一维护
    private OrderReviewService orderReviewService;
    // orderReviewService：订单评价业务逻辑服务接口，封装了评价的查询、保存、校验等业务逻辑

    /**
     * 查询当前用户的订单评价列表
     *
     * <p>从当前用户Session中获取用户ID，调用评价服务查询该用户提交的所有订单评价。
     * 适用于"我的评价"页面，展示用户对酒店/门票订单的历史评价记录。</p>
     *
     * @param session HttpSession 对象，Spring 自动注入当前请求的会话对象。
     *                通过浏览器Cookie中的JSESSIONID关联到服务器端的Session存储，
     *                从中可获取用户登录时存储的用户ID等属性。
     * @return ApiResponse<List<OrderReview>> 统一API响应，data字段为OrderReview实体列表。
     *         OrderReview 通常包含字段：id、userId、orderId、orderType、score（评分1-5）、
     *         content（评价内容）、createTime 等，供前端展示评价卡片列表。
     */
    @GetMapping
    // @GetMapping 映射 HTTP GET 请求，语义为"查询资源"。此处用于查询评价列表，符合RESTful规范。
    // 方法未指定额外路径，因此完整路径 = 类上的前缀 = /api/user/reviews
    public ApiResponse<List<OrderReview>> list(HttpSession session) {
        // 调用本类私有方法 currentUserId(session) 从Session中获取当前登录用户ID
        // 提取为私有方法避免重复代码，遵循"DRY原则（Don't Repeat Yourself）"
        Long userId = currentUserId(session);

        // 调用评价服务查询指定用户的所有评价记录
        // 业务层会查询数据库中 user_id = 当前用户ID 的评价记录，可能按时间倒序排列
        List<OrderReview> reviewList = orderReviewService.list(userId);

        // 将查询结果封装到统一响应对象返回
        // 前端"我的评价"页面可遍历此列表渲染每条评价的评分、内容、时间等信息
        return ApiResponse.success(reviewList);
    }

    /**
     * 提交订单评价
     *
     * <p>接收前端提交的评价内容（订单类型、订单ID、评分、文字评价），从Session中获取用户ID，
     * 调用评价服务保存评价。用户只能评价自己的订单，且通常需在订单完成后才能评价。</p>
     *
     * @param request 订单评价请求DTO，包含订单类型（hotel/ticket）、订单ID、评分（1-5星）、评价内容。
     *                @RequestBody 将HTTP请求体（JSON）反序列化为 OrderReviewRequest 对象。
     *                DTO（Data Transfer Object）模式：用于封装前端传入的数据，解耦前端字段与数据库实体。
     * @param session HttpSession 对象，用于获取当前登录用户ID，确保评价归属正确。
     * @return ApiResponse<String> 统一API响应，data字段为"OK"，消息为"评价已保存"。
     *         前端提交成功后可关闭评价弹窗或跳转至订单列表。
     */
    @PostMapping
    // @PostMapping 映射 HTTP POST 请求，语义为"创建/提交资源"。此处用于提交新评价，符合RESTful规范。
    // 完整路径：/api/user/reviews
    public ApiResponse<String> submit(@RequestBody OrderReviewRequest request, HttpSession session) {
        // 从Session获取当前用户ID
        Long userId = currentUserId(session);

        // 调用评价服务提交评价，传入用户ID和评价请求DTO
        // 业务层会校验：订单是否属于该用户、订单是否已完成、是否已评价过（防止重复评价）等
        orderReviewService.submit(userId, request);

        // 返回成功响应，消息为"评价已保存"，提示前端操作成功
        return ApiResponse.success("评价已保存", "OK");
    }

    /**
     * 从HttpSession中获取当前登录用户ID（私有工具方法）
     *
     * <p>封装从Session获取用户ID的重复逻辑，避免在每个方法中编写相同的转换代码。
     * 提取私有方法后，若后续Session存储格式变更（如改为Long直接存储），
     * 只需修改此一处即可，提升可维护性。</p>
     *
     * @param session HttpSession 对象，包含当前请求的用户会话信息。
     * @return Long 当前登录用户的唯一标识（用户ID）。
     *         转换过程：session.getAttribute(...) 返回 Object -> String.valueOf(...) 转为 String ->
     *         Long.valueOf(...) 转为 Long。若Session中无用户ID，会抛出 NullPointerException/NumberFormatException。
     */
    private Long currentUserId(HttpSession session) {
        // 从Session中根据常量键 SessionConstants.LOGIN_USER_ID 获取用户ID属性
        // 该属性在用户登录成功时由登录控制器设置到Session中
        Object userIdObj = session.getAttribute(SessionConstants.LOGIN_USER_ID);

        // 将 Object 转为 String，再转为 Long 类型
        // 注意：若用户未登录（Session中无该属性），String.valueOf(null) 返回 "null" 字符串，
        // Long.valueOf("null") 会抛出 NumberFormatException。实际项目中应增加登录校验拦截器。
        return Long.valueOf(String.valueOf(userIdObj));
    }
}
