package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import com.tourism.dto.AiAskRequest;
import com.tourism.service.AiQaService;
import com.tourism.vo.AiAnswerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端AI问答控制器（前端控制器）
 *
 * <p>该类负责处理与AI智能问答相关的HTTP请求，属于系统的前端接口层（Controller层）。
 * 在Spring MVC分层架构中，Controller层接收来自客户端（浏览器/移动端）的HTTP请求，
 * 调用Service层业务逻辑，并将处理结果封装为统一响应格式返回。</p>
 *
 * <p>核心职责：
 * <ul>
 *   <li>接收用户提问及相关上下文参数</li>
 *   <li>调用AI问答服务进行智能应答</li>
 *   <li>将AI返回结果封装为统一API响应</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
@RestController
// @RestController是Spring的复合注解，等同于 @Controller + @ResponseBody
// 作用：将该类声明为RESTful风格的控制器，所有方法返回值直接写入HTTP响应体（Response Body），
// 无需通过视图解析器渲染页面，适用于前后端分离架构（如Vue/React前端 + JSON数据交互）
@RequestMapping("/api/ai")
// @RequestMapping用于映射HTTP请求路径到控制器类或方法
// 此处标注在类上，表示该类下所有接口的URL前缀为 /api/ai
// 例如：/api/ai/ask 是 ask() 方法的完整访问路径
public class AiQaController {

    @Autowired
    // @Autowired 是Spring的依赖注入注解，作用：自动将Spring容器中类型匹配的 Bean 实例注入到当前字段
    // 注入原理：Spring容器启动时扫描到 @Autowired，会查找容器中 AiQaService 类型的 Bean，
    // 通过反射赋值给 aiQaService 字段，实现"控制反转（IoC）"，降低类之间的耦合度
    private AiQaService aiQaService;
    // aiQaService：AI问答业务逻辑服务接口，封装了与AI模型交互的复杂逻辑
    // 控制器不直接处理AI调用细节，而是通过服务层解耦，符合"单一职责原则"

    /**
     * AI智能问答接口
     *
     * <p>接收用户通过前端发送的提问请求，调用AI服务进行智能回答，并返回包含回答内容、
     * 参考来源、建议等信息的视图对象。</p>
     *
     * @param request AI提问请求数据传输对象（DTO），包含用户问题、出发城市、目的地城市、
     *                预算、天数、兴趣偏好等上下文信息，用于帮助AI生成更精准的回答。
     *                参数前的 @RequestBody 注解表示：将HTTP请求体（JSON格式）中的数据
     *                通过Spring的HttpMessageConverter（默认使用Jackson）反序列化为
     *                AiAskRequest 对象。仅适用于 POST/PUT 请求，请求Content-Type通常为
     *                application/json。
     * @return ApiResponse<AiAnswerVO> 统一API响应封装对象，其中 data 字段为 AI回答视图对象
     *         （AiAnswerVO），包含回答文本、参考列表、建议列表、回答模式等。
     *         返回格式示例：{"code": 200, "message": "success", "data": {...}}
     */
    @PostMapping("/ask")
    // @PostMapping 是 @RequestMapping(method = RequestMethod.POST) 的缩写，
    // 用于映射 HTTP POST 请求。POST 请求通常用于"创建/提交"资源，
    // 此处用于提交用户提问，语义符合 RESTful 设计规范。
    // 完整路径 = 类上的 @RequestMapping 前缀 + 方法上的路径 = /api/ai/ask
    public ApiResponse<AiAnswerVO> ask(@RequestBody AiAskRequest request) {
        // 调用 aiQaService.ask(request) 方法，将用户的提问请求传递给业务逻辑层处理
        // 业务层会负责调用外部AI模型（如OpenAI/文心一言等）或本地知识库进行问答推理
        AiAnswerVO answerVO = aiQaService.ask(request);

        // 将业务层返回的 AiAnswerVO 对象封装到 ApiResponse 统一响应体中
        // ApiResponse.success(...) 会自动设置成功状态码和提示信息，
        // 确保前端接收到的数据格式一致，便于前端统一处理成功/失败逻辑
        return ApiResponse.success(answerVO);
    }
}
