package com.tourism.service;

import com.tourism.dto.AiAskRequest;
import com.tourism.vo.AiAnswerVO;

/**
 * AI 问答服务接口（Service Contract）。
 * <p>
 * 本接口定义了 AI 行程助手（智能问答）的核心服务契约。
 * 该服务采用 RAG（检索增强生成）架构：先从本地旅游知识库（景点、酒店、门票、路线、系统规则）
 * 中检索相关内容，再基于检索结果生成回答。当外部大模型 API 可用时，将检索资料传给 API 生成；
 * 当 API 不可用时，自动回退到本地 RAG 生成回答。
 * 实现类：{@link com.tourism.service.impl.AiQaServiceImpl}
 * </p>
 *
 * @author Tourism System
 */
public interface AiQaService {

    /**
     * 处理用户旅游咨询问题并返回 AI 回答。
     * <p>
     * 业务步骤：
     * 1. 校验用户输入（非空、长度限制等）；
     * 2. 构建本地旅游知识库（全量景点、酒店、门票、路线、地图点位、系统规则）；
     * 3. 基于用户问题、出发地、目的地、预算、天数、兴趣等构建富化查询；
     * 4. 通过分词、匹配度打分、类型推断等算法从知识库检索 Top-N 相关片段；
     * 5. 使用本地模板生成初步回答（LOCAL_RAG）；
     * 6. 若外部大模型 API 配置正确且可用，则将检索资料传入 API 生成更自然的回答（API_RAG）；
     * 7. 将回答、参考资料、追问建议、当前模式封装为视图对象返回。
     * </p>
     *
     * @param request 用户提问请求对象，包含问题文本、出发地、目的地、预算、天数、兴趣等字段
     * @return AI 回答视图对象 {@link AiAnswerVO}，包含 answer、references、suggestions、mode 等字段
     */
    AiAnswerVO ask(AiAskRequest request);
}
