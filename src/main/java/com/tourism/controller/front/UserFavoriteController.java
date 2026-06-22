package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.common.SessionConstants;
import com.tourism.dto.FavoriteRequest;
import com.tourism.entity.UserFavorite;
import com.tourism.service.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 前端用户收藏控制器（前端控制器）
 *
 * <p>该类负责处理用户收藏相关的HTTP请求，包括查询收藏列表、添加收藏、取消收藏等操作。
 * 作为前端Controller层，接收客户端HTTP请求，从用户会话（Session）中提取当前登录用户ID，
 * 调用UserFavoriteService业务层处理收藏逻辑，并将结果封装为统一API响应返回。</p>
 *
 * <p>核心职责：
 * <ul>
 *   <li>查询当前登录用户的收藏列表</li>
 *   <li>添加新的收藏（景点、酒店、路线等）</li>
 *   <li>取消指定类型的收藏</li>
 *   <li>通过Session获取用户ID，确保只能操作自己的收藏数据</li>
 * </ul></p>
 *
 * <p>Session机制说明：
 * 用户登录后，服务器将用户ID存入HttpSession（键为 SessionConstants.LOGIN_USER_ID）。
 * 浏览器后续请求通过Cookie携带JSESSIONID，Spring据此找到对应Session并注入到HttpSession参数。
 * 这种方式无需前端传递用户ID，防止用户伪造身份操作他人收藏。</p>
 *
 * @author tourism-system
 * @version 1.0
 */
@RestController
// @RestController 标记该类为RESTful控制器，所有方法返回值直接写入HTTP响应体（JSON格式），
// 它是 @Controller + @ResponseBody 的复合注解，用于前后端分离架构
@RequestMapping("/api/user/favorites")
// @RequestMapping 标注在类上，定义该类所有接口的URL前缀为 /api/user/favorites
// 路径前缀体现资源归属：/api/user/... 表示当前登录用户的个人资源操作
public class UserFavoriteController {

    @Autowired
    // @Autowired 自动注入Spring容器中类型匹配的UserFavoriteService Bean实例
    // 通过Spring依赖注入获取用户收藏业务服务对象，控制器无需关心服务对象的创建过程
    private UserFavoriteService userFavoriteService;
    // userFavoriteService：用户收藏业务逻辑服务接口，封装了收藏的查询、添加、删除、校验等业务逻辑

    /**
     * 查询当前用户的收藏列表
     *
     * <p>从当前用户Session中获取用户ID，调用收藏服务查询该用户的所有收藏记录。
     * 适用于"我的收藏"页面，展示用户收藏的景点、酒店、路线等。</p>
     *
     * @param session HttpSession 对象，Spring 自动注入当前请求的会话对象。
     *                通过浏览器Cookie中的JSESSIONID关联到服务器端的Session存储，
     *                从中获取用户登录时存储的用户ID。
     * @return ApiResponse<List<UserFavorite>> 统一API响应，data字段为UserFavorite实体列表。
     *         UserFavorite 通常包含字段：id、userId、favoriteType（收藏类型：scenic/hotel/route）、
     *         targetId（目标ID）、title（标题）、description（描述）、createTime 等。
     *         前端可遍历此列表按类型分组展示收藏卡片。
     */
    @GetMapping
    // @GetMapping 映射 HTTP GET 请求，语义为"查询资源"。此处用于查询收藏列表，符合RESTful规范。
    // 方法未指定额外路径，完整路径 = 类上的前缀 = /api/user/favorites
    public ApiResponse<List<UserFavorite>> list(HttpSession session) {
        // 调用私有工具方法 currentUserId(session) 从Session中获取当前登录用户ID
        // 提取为私有方法避免重复代码，遵循DRY原则（Don't Repeat Yourself）
        Long userId = currentUserId(session);

        // 调用收藏服务查询指定用户的所有收藏记录
        // 业务层查询数据库中 user_id = 当前用户ID 的收藏记录，通常按时间倒序排列
        List<UserFavorite> favoriteList = userFavoriteService.list(userId);

        // 将查询结果封装到统一响应对象返回前端
        // 前端"我的收藏"页面可据此渲染收藏列表，支持按类型筛选或取消收藏操作
        return ApiResponse.success(favoriteList);
    }

    /**
     * 添加收藏
     *
     * <p>接收前端提交的收藏信息（收藏类型、目标ID、标题、描述），从Session中获取用户ID，
     * 调用收藏服务保存收藏记录。用户可收藏景点、酒店、旅游路线等不同类型的内容。</p>
     *
     * @param request 收藏请求DTO，包含收藏类型（favoriteType：scenic/hotel/route）、目标ID（targetId）、
     *                标题（title）、描述（description）等字段。
     *                @RequestBody 将HTTP请求体（JSON）反序列化为 FavoriteRequest 对象。
     *                DTO（Data Transfer Object）模式：前端传入的数据结构可能与数据库实体不同，
     *                使用DTO封装请求数据，避免实体类直接暴露，提升安全性和灵活性。
     * @param session HttpSession 对象，用于获取当前登录用户ID，确保收藏记录归属正确。
     * @return ApiResponse<String> 统一API响应，data字段为"OK"，消息为"收藏成功"。
     *         前端可据此提示收藏成功，并更新收藏按钮状态（如变为"已收藏"）。
     */
    @PostMapping
    // @PostMapping 映射 HTTP POST 请求，语义为"创建/提交资源"。此处用于添加新收藏，符合RESTful规范。
    // 完整路径：/api/user/favorites
    public ApiResponse<String> add(@RequestBody FavoriteRequest request, HttpSession session) {
        // 从Session获取当前用户ID
        Long userId = currentUserId(session);

        // 调用收藏服务添加收藏，传入用户ID和收藏请求DTO
        // 业务层会校验：是否已收藏过（防止重复收藏）、目标对象是否存在、收藏类型是否合法等
        userFavoriteService.add(userId, request);

        // 返回成功响应，提示"收藏成功"
        return ApiResponse.success("收藏成功", "OK");
    }

    /**
     * 取消收藏
     *
     * <p>接收收藏类型和目标ID，从Session中获取用户ID，调用收藏服务删除对应的收藏记录。
     * 适用于"我的收藏"页面的取消收藏按钮，或景点/酒店详情页的"已收藏"状态取消操作。</p>
     *
     * @param favoriteType 收藏类型（如"scenic"、"hotel"、"route"），通过URL查询参数传递。
     *                     @RequestParam 从URL查询字符串中提取参数值。
     * @param targetId 目标对象ID（如景点ID、酒店ID），通过URL查询参数传递。
     *                 与 favoriteType 组合唯一确定一个收藏记录。
     * @param session HttpSession 对象，用于获取当前登录用户ID。
     * @return ApiResponse<String> 统一API响应，data字段为"OK"，消息为"已取消收藏"。
     *         前端可据此更新UI，将收藏按钮恢复为"收藏"状态。
     */
    @DeleteMapping
    // @DeleteMapping 是 @RequestMapping(method = RequestMethod.DELETE) 的缩写，
    // 用于映射 HTTP DELETE 请求。DELETE 语义为"删除资源"，此处用于删除收藏记录，严格符合RESTful规范。
    // 完整路径：/api/user/favorites
    // 请求示例：DELETE /api/user/favorites?favoriteType=scenic&targetId=100
    public ApiResponse<String> remove(@RequestParam String favoriteType, @RequestParam String targetId, HttpSession session) {
        // 从Session获取当前用户ID
        Long userId = currentUserId(session);

        // 调用收藏服务删除指定收藏，传入用户ID、收藏类型、目标ID
        // 业务层会校验收藏记录是否存在、是否属于当前用户，然后执行删除操作
        userFavoriteService.remove(userId, favoriteType, targetId);

        // 返回成功响应，提示"已取消收藏"
        return ApiResponse.success("已取消收藏", "OK");
    }

    /**
     * 从HttpSession中获取当前登录用户ID（私有工具方法）
     *
     * <p>封装从Session获取用户ID的重复逻辑，避免在多个方法中编写相同的转换代码。
     * 若后续Session存储格式变更（如改为直接存储Long类型），只需修改此一处即可。</p>
     *
     * @param session HttpSession 对象，包含当前请求的用户会话信息。
     * @return Long 当前登录用户的唯一标识（用户ID）。
     *         转换链：Object -> String -> Long。若Session中无用户ID，可能抛出异常。
     */
    private Long currentUserId(HttpSession session) {
        // 从Session中根据常量键 SessionConstants.LOGIN_USER_ID 获取用户ID属性
        Object userIdObj = session.getAttribute(SessionConstants.LOGIN_USER_ID);

        // 将 Object 转为 String，再转为 Long 类型
        return Long.valueOf(String.valueOf(userIdObj));
    }
}
