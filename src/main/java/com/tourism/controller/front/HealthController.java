package com.tourism.controller.front;

import com.tourism.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 前端健康检查控制器（前端控制器）
 *
 * <p>该类提供系统健康状态检查接口，用于监控系统的运行状态。在微服务架构或
 * 分布式系统中，健康检查接口是运维监控（如Nginx、Kubernetes、Prometheus）
 * 判断服务是否存活的关键端点。</p>
 *
 * <p>核心职责：
 * <ul>
 *   <li>暴露系统运行状态信息</li>
 *   <li>供负载均衡器、监控探针、运维人员调用</li>
 *   <li>快速验证后端服务是否可正常响应</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
@RestController
// @RestController 声明该类为RESTful控制器，所有方法返回值直接序列化为JSON写入HTTP响应体
// 复合注解：@Controller（标记为Spring管理的控制器Bean） + @ResponseBody（返回值不走视图解析）
@RequestMapping("/api/health")
// @RequestMapping 标注在类上，定义该类所有接口的URL前缀为 /api/health
// 例如：health() 方法的完整访问路径为 /api/health（当方法不附加路径时，默认与类路径一致）
public class HealthController {

    /**
     * 系统健康检查接口
     *
     * <p>返回当前项目的运行状态、项目名称、所属模块等基本信息。前端或运维系统可通过
     * 调用此接口快速确认后端服务是否正常运行。返回状态码200且包含"status":"UP"表示
     * 服务健康；若服务异常，通常会出现连接超时或HTTP错误码。</p>
     *
     * @return ApiResponse<Map<String, Object>> 统一API响应封装对象，其中 data 字段为
     *         包含项目信息的键值对Map。使用 LinkedHashMap 保证字段按插入顺序输出，
     *         便于前端展示时保持稳定的字段顺序。
     *         Map中各字段含义：
     *         - project：项目名称（tourism-system），标识当前应用
     *         - status：服务状态（UP），表示服务正常启动并可处理请求
     *         - module：模块标识（ssm-skeleton），指示当前代码框架或骨架版本
     */
    @GetMapping
    // @GetMapping 是 @RequestMapping(method = RequestMethod.GET) 的缩写，
    // 用于映射 HTTP GET 请求。GET 请求通常用于"获取/查询"资源，语义为只读操作，
    // 不会对系统状态产生副作用，符合健康检查只读查询的语义。
    // 由于方法上没有额外路径，完整路径 = 类上的前缀 = /api/health
    public ApiResponse<Map<String, Object>> health() {
        // 使用 LinkedHashMap 创建响应数据容器，区别于 HashMap：
        // LinkedHashMap 底层采用双向链表维护插入顺序，遍历输出时顺序与 put 顺序一致，
        // 确保前端收到的 JSON 字段顺序固定：project -> status -> module
        Map<String, Object> data = new LinkedHashMap<>();

        // 向Map中放入项目基本信息，作为健康检查返回的标准字段
        data.put("project", "tourism-system");
        // "project" 字段：标识当前应用名称，在多项目环境中可快速区分服务来源

        data.put("status", "UP");
        // "status" 字段：服务状态，"UP" 是 Spring Boot Actuator 等框架中的标准健康标识，
        // 表示服务正常运行；若后续扩展，可返回 "DOWN"（宕机）或 "OUT_OF_SERVICE"（停止服务）

        data.put("module", "ssm-skeleton");
        // "module" 字段：标识当前使用的技术骨架或模块版本，便于版本追踪和兼容性判断

        // 将构建好的 Map 数据封装到 ApiResponse 统一响应对象中返回
        // ApiResponse.success(data) 会设置默认的成功状态码和提示消息，
        // 保证前后端接口契约一致，便于前端拦截器统一处理
        return ApiResponse.success(data);
    }
}
