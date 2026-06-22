package com.tourism.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.common.BusinessException;
import com.tourism.dto.AiAskRequest;
import com.tourism.entity.Hotel;
import com.tourism.entity.MapPoint;
import com.tourism.entity.RouteSpot;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.Ticket;
import com.tourism.entity.TravelRoute;
import com.tourism.mapper.HotelMapper;
import com.tourism.mapper.MapPointMapper;
import com.tourism.mapper.RouteSpotMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.mapper.TicketMapper;
import com.tourism.mapper.TravelRouteMapper;
import com.tourism.service.AiQaService;
import com.tourism.vo.AiAnswerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 问答服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入六个 Mapper：
 * {@link ScenicSpotMapper}、{@link HotelMapper}、{@link TicketMapper}、{@link TravelRouteMapper}、
 * {@link RouteSpotMapper}、{@link MapPointMapper}，用于构建本地旅游知识库。
 * 同时通过 {@link Value} 注解从配置文件读取 AI 外部 API 的配置参数（开关、URL、密钥、模型）。
 * 职责：为 C 端用户提供 AI 行程助手（智能问答）服务，采用 RAG（检索增强生成）架构：
 * 1. 构建本地知识库（全量景点、酒店、门票、路线、地图点位、系统规则）；
 * 2. 基于用户输入构建富化查询；
 * 3. 通过分词、匹配度打分、类型推断检索 Top-N 相关片段；
 * 4. 使用本地模板生成初步回答（LOCAL_RAG）；
 * 5. 若外部大模型 API 可用，则将检索资料传入 API 生成更自然的回答（API_RAG）；
 * 6. 返回回答、参考资料、追问建议、当前模式。
 * 本类为只读查询（Mapper 查询）+ 外部 HTTP 调用，不涉及数据库写操作，因此不声明 {@link org.springframework.transaction.annotation.Transactional}。
 * </p>
 *
 * @author Tourism System
 * @see AiQaService
 */
@Service
public class AiQaServiceImpl implements AiQaService {

    /**
     * Jackson 对象映射器，用于序列化/反序列化 JSON（API 请求体和响应体）。
     * 声明为 static final，所有实例共享同一对象，避免重复创建开销。
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 景点数据访问 Mapper，自动注入。用于构建知识库中的景点信息片段。
     */
    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    /**
     * 酒店数据访问 Mapper，自动注入。用于构建知识库中的酒店信息片段。
     */
    @Autowired
    private HotelMapper hotelMapper;

    /**
     * 门票数据访问 Mapper，自动注入。用于构建知识库中的门票信息片段。
     */
    @Autowired
    private TicketMapper ticketMapper;

    /**
     * 旅游路线主体 Mapper，自动注入。用于构建知识库中的路线信息片段。
     */
    @Autowired
    private TravelRouteMapper travelRouteMapper;

    /**
     * 路线景点关联 Mapper，自动注入。用于查询路线对应的景点游览顺序，构建路线知识片段。
     */
    @Autowired
    private RouteSpotMapper routeSpotMapper;

    /**
     * 地图点位 Mapper，自动注入。用于构建知识库中的地图点位信息片段。
     */
    @Autowired
    private MapPointMapper mapPointMapper;

    /**
     * AI 外部 API 是否启用，从配置文件读取，默认 false（未配置时不启用，避免报错）。
     * 若配置为 false 或 URL/密钥未配置，则自动回退到本地 RAG 回答。
     */
    @Value("${ai.api.enabled:false}")
    private boolean aiApiEnabled;

    /**
     * AI 外部 API 地址，从配置文件读取，默认空字符串。
     * 支持 OpenAI 兼容格式的 API 端点（如 GPT-4o-mini、Kimi、通义千问等）。
     */
    @Value("${ai.api.url:}")
    private String aiApiUrl;

    /**
     * AI 外部 API 密钥，从配置文件读取，默认空字符串。
     * 若配置值以 "${" 开头（表示未解析到有效值），则尝试从环境变量 AI_API_KEY 读取。
     */
    @Value("${ai.api.key:}")
    private String aiApiKey;

    /**
     * AI 外部 API 模型名称，从配置文件读取，默认 "gpt-4o-mini"。
     * 若配置为空，则使用默认模型名称。
     */
    @Value("${ai.api.model:gpt-4o-mini}")
    private String aiApiModel;

    /**
     * 处理用户旅游咨询问题并返回 AI 回答。
     * <p>
     * 业务逻辑步骤：
     * 1. 请求空安全处理：若 request 为 null，则创建空对象，避免后续 NullPointerException；
     * 2. 问题文本校验：去除首尾空格后，若为空则抛出 {@link BusinessException} "请输入想咨询的旅游问题"；
     *    若长度超过 300 字，抛出 {@link BusinessException} "问题太长了，建议控制在 300 字以内"；
     *    该校验防止用户输入空问题或过长问题，保护后端处理性能；
     * 3. 构建本地知识库：调用 {@link #buildKnowledgeBase()} 加载全量景点、酒店、门票、路线、地图点位、系统规则；
     * 4. 构建富化查询：调用 {@link #buildEnrichedQuery(AiAskRequest)} 将问题、出发地、目的地、城市、预算、天数、兴趣拼接为完整查询文本；
     * 5. 确定目标城市：优先 destinationCity，其次 city，用于提升检索相关性；
     * 6. 知识检索：调用 {@link #retrieve(String, String, List, int)} 从知识库中检索最相关的 Top 6 片段；
     *    检索算法包括：分词、类型推断、城市匹配、关键词匹配、综合打分、降序排序；
     * 7. 生成本地回答：调用 {@link #buildAnswer(AiAskRequest, List)} 基于检索结果使用本地模板拼接回答；
     * 8. 调用外部 API：调用 {@link #generateWithApi(AiAskRequest, List, String)} 尝试使用外部大模型生成回答；
     *    若 API 不可用或调用失败，则返回本地回答作为 fallback；
     * 9. 构造 {@link AiAnswerVO} 返回：设置 answer、references（参考资料列表）、suggestions（追问建议）、mode（LOCAL_RAG 或 API_RAG）。
     * 模式判断：若最终 answer 与本地回答相同，说明 API 未生效或失败，返回 "LOCAL_RAG"；否则返回 "API_RAG"。
     * </p>
     *
     * @param request 用户提问请求对象
     * @return AI 回答视图对象 {@link AiAnswerVO}
     */
    @Override
    public AiAnswerVO ask(AiAskRequest request) {
        // 步骤1：请求空安全处理，避免 NullPointerException
        AiAskRequest safeRequest = request == null ? new AiAskRequest() : request;
        // 步骤2：问题文本校验
        String question = safe(safeRequest.getQuestion()).trim();
        if (question.isEmpty()) {
            throw new BusinessException("请输入想咨询的旅游问题");
        }
        if (question.length() > 300) {
            throw new BusinessException("问题太长了，建议控制在 300 字以内");
        }

        // 步骤3：构建本地知识库
        List<KnowledgeChunk> chunks = buildKnowledgeBase();
        // 步骤4：构建富化查询文本
        String enrichedQuery = buildEnrichedQuery(safeRequest);
        // 步骤5：确定目标城市，用于检索阶段的城市匹配加权
        String destination = firstNonBlank(safeRequest.getDestinationCity(), safeRequest.getCity());
        // 步骤6：检索 Top 6 相关片段
        List<KnowledgeChunk> topChunks = retrieve(enrichedQuery, destination, chunks, 6);

        // 步骤7：使用本地模板生成回答
        String localAnswer = buildAnswer(safeRequest, topChunks);
        // 步骤8：尝试调用外部大模型 API，若失败则返回本地回答
        String answer = generateWithApi(safeRequest, topChunks, localAnswer);

        // 步骤9：构造返回视图对象
        AiAnswerVO vo = new AiAnswerVO();
        vo.setAnswer(answer);
        // 将检索片段格式化为参考资料列表，如 "[景点] 西湖：描述内容"
        vo.setReferences(topChunks.stream()
                .map(chunk -> "[" + chunk.type + "] " + chunk.title + "：" + chunk.content)
                .collect(Collectors.toList()));
        // 生成追问建议，引导用户继续交互
        vo.setSuggestions(buildSuggestions(safeRequest, topChunks));
        // 判断当前模式：若 API 返回结果与本地回答相同，说明 API 未生效
        vo.setMode(answer.equals(localAnswer) ? "LOCAL_RAG" : "API_RAG");
        return vo;
    }

    /**
     * 构建本地旅游知识库。
     * <p>
     * 知识库是 RAG 架构的核心，包含系统中所有旅游相关数据和系统规则。
     * 构建步骤：
     * 1. 查询所有景点（{@code scenicSpotMapper.selectAll()}），为每个景点生成知识片段，
     *    内容包含名称、城市、分类、评分、价格、热度、标签、描述；
     *    同时构建 scenicMap（ID → ScenicSpot），用于后续门票和路线关联景点名称解析；
     * 2. 查询所有酒店（{@code hotelMapper.selectAll()}），为每个酒店生成知识片段，
     *    内容包含名称、城市、等级、地址、描述；
     * 3. 查询所有门票（{@code ticketMapper.selectAll()}），为每个门票生成知识片段，
     *    内容包含名称、所属景点、城市、价格、库存、可订日期；
     *    通过 scenicMap 获取门票关联的景点名称和城市；
     * 4. 查询所有路线（{@code travelRouteMapper.selectAll()}），为每条路线生成知识片段，
     *    内容包含名称、城市、天数、主题、预算、景点游览顺序（通过 routeSpotMapper 查询并按 dayNo、sortNo 排序）、描述；
     * 5. 查询所有地图点位（{@code mapPointMapper.selectAll()}），为每个点位生成知识片段，
     *    内容包含名称、类型、城市、地址、价格、评分、标签、描述；
     * 6. 添加系统规则知识片段：订单规则、账号角色、地图功能、房态互斥、AI 问答机制。
     *    这些规则帮助 AI 回答系统操作类问题（如"怎么取消订单""账号密码是多少"）。
     * 该设计原因：将数据库中的结构化数据转化为文本化的知识片段，便于检索算法进行文本匹配；
     * 系统规则片段让 AI 能够回答超出业务实体范围的问题。
     * </p>
     *
     * @return 知识片段列表，每个片段包含 type、title、content
     */
    private List<KnowledgeChunk> buildKnowledgeBase() {
        List<KnowledgeChunk> chunks = new ArrayList<>();
        // 步骤1：查询所有景点并构建 ID 映射表，同时生成景点知识片段
        List<ScenicSpot> scenicSpots = scenicSpotMapper.selectAll(); // Mapper 执行 SELECT * FROM scenic_spot
        Map<Long, ScenicSpot> scenicMap = scenicSpots.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ScenicSpot::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        for (ScenicSpot scenic : scenicSpots) {
            chunks.add(new KnowledgeChunk(
                    "景点",
                    scenic.getScenicName(),
                    joinText(
                            scenic.getScenicName(),
                            scenic.getCity(),
                            scenic.getCategory(),
                            "评分" + safeNumber(scenic.getScore()),
                            "参考价格" + money(scenic.getPrice()),
                            "热度" + safeNumber(scenic.getPopularity()),
                            scenic.getTags(),
                            scenic.getDescription()
                    )
            ));
        }

        // 步骤2：查询所有酒店并生成知识片段
        for (Hotel hotel : hotelMapper.selectAll()) { // Mapper 执行 SELECT * FROM hotel
            chunks.add(new KnowledgeChunk(
                    "酒店",
                    hotel.getHotelName(),
                    joinText(hotel.getHotelName(), hotel.getCity(), hotel.getLevel(), hotel.getAddress(), hotel.getDescription())
            ));
        }

        // 步骤3：查询所有门票并生成知识片段，关联景点名称和城市
        for (Ticket ticket : ticketMapper.selectAll()) { // Mapper 执行 SELECT * FROM ticket
            ScenicSpot scenic = scenicMap.get(ticket.getScenicId());
            chunks.add(new KnowledgeChunk(
                    "门票",
                    ticket.getTicketName(),
                    joinText(
                            ticket.getTicketName(),
                            scenic == null ? "" : scenic.getScenicName(),
                            scenic == null ? "" : scenic.getCity(),
                            "价格" + money(ticket.getPrice()),
                            "库存" + ticket.getStock(),
                            "可订日期" + ticket.getAvailableDate()
                    )
            ));
        }

        // 步骤4：查询所有路线并生成知识片段，包含景点游览顺序
        for (TravelRoute route : travelRouteMapper.selectAll()) { // Mapper 执行 SELECT * FROM travel_route
            List<RouteSpot> routeSpots = routeSpotMapper.selectByRouteId(route.getId()); // Mapper 执行 SELECT BY route_id，按 day_no, sort_no 排序
            String scenicOrder = routeSpots.stream()
                    .sorted(Comparator.comparing(RouteSpot::getDayNo, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(RouteSpot::getSortNo, Comparator.nullsLast(Integer::compareTo)))
                    .map(RouteSpot::getScenicId)
                    .map(scenicMap::get)
                    .filter(Objects::nonNull)
                    .map(ScenicSpot::getScenicName)
                    .collect(Collectors.joining(" → ")); // 景点游览顺序用 "→" 连接
            chunks.add(new KnowledgeChunk(
                    "路线",
                    route.getRouteName(),
                    joinText(
                            route.getRouteName(),
                            route.getCity(),
                            route.getDays() == null ? "" : route.getDays() + "天",
                            route.getTheme(),
                            "预算" + money(route.getBudget()),
                            scenicOrder,
                            route.getRouteDesc()
                    )
            ));
        }

        // 步骤5：查询所有地图点位并生成知识片段
        for (MapPoint point : mapPointMapper.selectAll()) { // Mapper 执行 SELECT * FROM map_point
            chunks.add(new KnowledgeChunk(
                    firstNonBlank(point.getPointType(), "地图点位"),
                    point.getPointName(),
                    joinText(
                            point.getPointName(),
                            point.getPointType(),
                            point.getCity(),
                            point.getAddress(),
                            "参考价格" + money(point.getPrice()),
                            "评分" + safeNumber(point.getScore()),
                            point.getTags(),
                            point.getDescription()
                    )
            ));
        }

        // 步骤6：添加系统规则知识片段，让 AI 能够回答系统操作类问题
        chunks.add(new KnowledgeChunk("系统规则", "订单规则", "用户可以预订酒店和门票，可以在我的订单中查看订单；已创建订单支持取消和支付，已取消订单不能再次取消。"));
        chunks.add(new KnowledgeChunk("系统规则", "账号角色", "普通用户账号 demo / demo123，可浏览首页、下单和查看订单；管理员账号 admin / admin123，可进入后台维护景点、酒店、门票、路线和订单状态。"));
        chunks.add(new KnowledgeChunk("系统规则", "地图功能", "系统接入高德地图，旅游地图可以展示景点、酒店、美食、机场、火车站；路线规划页面可把景点顺序传到地图页并绘制游玩顺序。"));
        chunks.add(new KnowledgeChunk("系统规则", "房态互斥", "酒店房态按入住日期和离店日期动态计算；同一房型在日期重叠时会被未取消订单锁定，可订数量减少；若可订数量为 0，前端显示满房并禁止选择。"));
        chunks.add(new KnowledgeChunk("系统规则", "AI问答", "AI 行程助手会先检索本地旅游知识库，再把检索资料交给大模型生成；如果外部模型不可用，会自动回退到本地 RAG 回答。"));
        return chunks;
    }

    /**
     * 构建富化查询文本。
     * <p>
     * 将用户问题、出发地、目的地、城市、预算、天数、兴趣拼接为单一查询字符串，
     * 用于后续检索阶段的知识片段匹配。富化查询的目的是让检索不仅基于问题文本，
     * 还包含用户提供的结构化条件，从而提升检索精准度。
     * 例如：用户输入 "推荐路线" + 目的地 "杭州" + 天数 "3天" + 兴趣 "美食"，
     * 富化查询后包含所有这些关键词，能更好地匹配到杭州相关路线和美食相关景点。
     * </p>
     *
     * @param request 用户提问请求对象
     * @return 拼接后的富化查询文本
     */
    private String buildEnrichedQuery(AiAskRequest request) {
        return joinText(
                request.getQuestion(),
                request.getOriginCity(),
                request.getDestinationCity(),
                request.getCity(),
                request.getBudget(),
                request.getDays(),
                request.getInterests()
        );
    }

    /**
     * 从知识库中检索与查询最相关的 Top-N 片段。
     * <p>
     * 检索算法步骤：
     * 1. 对查询文本进行分词（{@link #tokenize}），得到查询词集合；
     * 2. 推断查询意图类型（{@link #inferTypes}），如包含"路线"则推断类型为路线、景点、酒店、门票；
     * 3. 对每个知识片段计算匹配度评分（{@link #score}），评分维度包括：
     *    - 目的地匹配：文本包含目标城市加 80 分，标题包含加 30 分；
     *    - 类型偏好：匹配推断类型加 24 分；
     *    - 关键词匹配：每个查询词命中加 6 分（2字以上）或 2 分（单字）；
     *    - 全句匹配：完整查询文本命中加 10 分；
     * 4. 过滤掉评分为 0 的片段；
     * 5. 按评分降序排序，取前 limit 个；
     * 6. 若排序后结果为空，但存在推断类型，则退回到按类型过滤取前 limit 个；
     * 7. 若仍为空，则直接取知识库前 limit 个作为兜底（保证始终有返回）。
     * 该设计原因：多维度打分确保高相关性片段优先，多级 fallback 保证不返回空结果。
     * </p>
     *
     * @param query       富化查询文本
     * @param destination 目标城市
     * @param chunks      知识库片段列表
     * @param limit       返回的最大片段数量
     * @return 排序后的相关片段列表
     */
    private List<KnowledgeChunk> retrieve(String query, String destination, List<KnowledgeChunk> chunks, int limit) {
        Set<String> queryTerms = tokenize(query); // 步骤1：分词
        List<String> preferredTypes = inferTypes(query); // 步骤2：推断类型
        List<KnowledgeChunk> scoredChunks = chunks.stream()
                .map(chunk -> chunk.withScore(score(queryTerms, query, destination, preferredTypes, chunk))) // 步骤3：计算评分
                .filter(chunk -> chunk.score > 0) // 步骤4：过滤无效片段
                .sorted(Comparator.comparingInt((KnowledgeChunk chunk) -> chunk.score).reversed()) // 步骤5：按评分降序
                .limit(limit)
                .collect(Collectors.toList());
        if (!scoredChunks.isEmpty()) {
            return scoredChunks;
        }
        // 步骤6：按类型过滤 fallback
        if (!preferredTypes.isEmpty()) {
            List<KnowledgeChunk> fallbackChunks = chunks.stream()
                    .filter(chunk -> preferredTypes.contains(chunk.type))
                    .limit(limit)
                    .collect(Collectors.toList());
            if (!fallbackChunks.isEmpty()) {
                return fallbackChunks;
            }
        }
        // 步骤7：兜底取前 limit 个
        return chunks.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 计算单个知识片段与查询的匹配度评分。
     * <p>
     * 评分维度：
     * 1. 目的地匹配（80分）：将 type + title + content 拼接为小写文本，若包含目标城市则加 80 分；
     * 2. 标题匹配（30分）：若 title 包含目标城市，额外加 30 分（标题匹配更精准）；
     * 3. 类型偏好（24分）：若片段类型在推断类型列表中，加 24 分；
     * 4. 关键词匹配（6分/2分）：每个查询词在文本中命中，2字以上加 6 分，单字加 2 分；
     * 5. 全句匹配（10分）：若完整查询文本在文本中命中，加 10 分。
     * 该设计原因：目的地权重最高（80分），确保城市优先；标题和类型次之；关键词和全句作为补充。
     * </p>
     *
     * @param queryTerms     查询词集合
     * @param query          完整查询文本
     * @param destination    目标城市
     * @param preferredTypes 推断类型列表
     * @param chunk          知识片段
     * @return 匹配度评分
     */
    private int score(Set<String> queryTerms, String query, String destination, List<String> preferredTypes, KnowledgeChunk chunk) {
        String text = (chunk.type + " " + chunk.title + " " + chunk.content).toLowerCase(Locale.ROOT);
        int score = 0;
        String destinationText = safe(destination).trim().toLowerCase(Locale.ROOT);
        // 维度1：目的地匹配，文本中包含目标城市
        if (!destinationText.isEmpty() && text.contains(destinationText)) {
            score += 80;
        }
        // 维度2：标题匹配，标题中包含目标城市更精准
        if (!destinationText.isEmpty() && safe(chunk.title).toLowerCase(Locale.ROOT).contains(destinationText)) {
            score += 30;
        }
        // 维度3：类型偏好
        if (preferredTypes.contains(chunk.type)) {
            score += 24;
        }
        // 维度4：关键词匹配
        for (String term : queryTerms) {
            String lowerTerm = term.toLowerCase(Locale.ROOT);
            if (text.contains(lowerTerm)) {
                score += term.length() >= 2 ? 6 : 2; // 2字以上权重更高
            }
        }
        // 维度5：全句匹配
        if (text.contains(query.toLowerCase(Locale.ROOT))) {
            score += 10;
        }
        return score;
    }

    /**
     * 根据查询文本推断用户意图类型。
     * <p>
     * 推断规则：
     * 1. 若包含"路线"、"行程"、"几天"、"天"、"玩法"、"规划"、"安排"，推断类型为路线、景点、酒店、门票；
     * 2. 若包含"酒店"、"住宿"、"入住"、"房态"、"满房"，推断类型为酒店、系统规则；
     * 3. 若包含"景点"、"哪里玩"、"打卡"、"美食"、"夜景"、"文化"，推断类型为景点；
     * 4. 若包含"门票"、"票"、"价格"、"库存"，推断类型为门票；
     * 5. 若包含"订单"、"取消"、"账号"、"登录"、"客服"、"权限"，推断类型为系统规则。
     * 该设计原因：根据关键词推断用户关注的内容类型，在检索时对匹配类型加分，提升相关性。
     * </p>
     *
     * @param query 查询文本
     * @return 推断的类型列表
     */
    private List<String> inferTypes(String query) {
        List<String> types = new ArrayList<>();
        if (containsAny(query, "路线", "行程", "几天", "天", "玩法", "规划", "安排")) {
            types.add("路线");
            types.add("景点");
            types.add("酒店");
            types.add("门票");
        }
        if (containsAny(query, "酒店", "住宿", "入住", "房态", "满房")) {
            types.add("酒店");
            types.add("系统规则");
        }
        if (containsAny(query, "景点", "哪里玩", "打卡", "美食", "夜景", "文化")) {
            types.add("景点");
        }
        if (containsAny(query, "门票", "票", "价格", "库存")) {
            types.add("门票");
        }
        if (containsAny(query, "订单", "取消", "账号", "登录", "客服", "权限")) {
            types.add("系统规则");
        }
        return types.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 判断文本中是否包含任一关键词。
     * <p>私有工具方法，用于简化类型推断中的关键词匹配。</p>
     *
     * @param text     源文本
     * @param keywords 多个关键词
     * @return true 表示至少包含一个关键词
     */
    private boolean containsAny(String text, String... keywords) {
        String source = safe(text);
        return Arrays.stream(keywords).anyMatch(source::contains);
    }

    /**
     * 使用本地模板基于检索结果生成回答。
     * <p>
     * 生成逻辑：
     * 1. 提取用户的出发地、目的地、天数、预算、兴趣，为空则显示"未填写"；
     * 2. 开头说明 "我先按你的输入做本地 RAG 检索"，并展示提取出的条件，增强用户感知；
     * 3. 若检索结果为空，提示用户换个问法，并给出示例；
     * 4. 若检索结果非空，按类型分类展示：路线、酒店、景点、门票，每条结果前加序号；
     * 5. 结尾提示用户可以继续点击"进入路线规划"和"地图看路线"，引导后续操作。
     * 该设计原因：本地模板回答结构清晰、可控，即使外部 API 不可用也能给用户提供有价值的回答；
     * 同时引导用户进入系统的其他功能模块，提升转化率。
     * </p>
     *
     * @param request 用户提问请求对象
     * @param chunks  检索到的知识片段列表
     * @return 本地生成的回答文本
     */
    private String buildAnswer(AiAskRequest request, List<KnowledgeChunk> chunks) {
        String destination = firstNonBlank(request.getDestinationCity(), request.getCity(), "目的地");
        String origin = firstNonBlank(request.getOriginCity(), "出发地");
        String days = firstNonBlank(request.getDays(), "未填写");
        String budget = firstNonBlank(request.getBudget(), "未填写");
        String interests = firstNonBlank(request.getInterests(), "未填写");

        StringBuilder answer = new StringBuilder();
        // 展示检索条件，增强透明度和用户信任
        answer.append("我先按你的输入做本地 RAG 检索：")
                .append(origin).append(" → ").append(destination)
                .append("，").append(days).append("天，预算 ").append(budget)
                .append("，兴趣：").append(interests).append("。\n\n");
        if (chunks.isEmpty()) {
            // 无检索结果时的兜底提示
            answer.append("当前知识库没有检索到特别匹配的资料，可以换个问法，例如“杭州三日路线怎么安排”“酒店房态怎么判断”。");
            return answer.toString();
        }
        // 有检索结果时，按类型分类展示
        answer.append("建议方案：\n");
        for (int index = 0; index < chunks.size(); index++) {
            KnowledgeChunk chunk = chunks.get(index);
            answer.append(index + 1).append(". ");
            if ("路线".equals(chunk.type)) {
                answer.append("路线参考《").append(chunk.title).append("》：").append(chunk.content);
            } else if ("酒店".equals(chunk.type)) {
                answer.append("住宿参考《").append(chunk.title).append("》：").append(chunk.content);
            } else if ("景点".equals(chunk.type)) {
                answer.append("景点参考《").append(chunk.title).append("》：").append(chunk.content);
            } else if ("门票".equals(chunk.type)) {
                answer.append("门票参考《").append(chunk.title).append("》：").append(chunk.content);
            } else {
                answer.append(chunk.content);
            }
            answer.append("\n");
        }
        // 引导用户进入系统其他功能
        answer.append("\n你可以继续点击“进入路线规划”生成结构化路线，再点击“地图看路线”把景点画到地图上。");
        return answer.toString();
    }

    /**
     * 调用外部大模型 API 生成回答。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@link #resolveApiKey()} 解析 API 密钥（优先配置值，其次环境变量）；
     * 2. 校验 API 可用性：若 aiApiEnabled 为 false、URL 为空、密钥为空，则直接返回本地回答（fallback）；
     * 3. 构造 HTTP POST 请求：
     *    - 设置请求方法 POST、连接超时 8 秒、读取超时 20 秒；
     *    - 设置 Content-Type 为 application/json; charset=UTF-8；
     *    - 设置 Authorization 为 Bearer + API 密钥；
     * 4. 构造请求体 JSON：包含 model、temperature（0.3，控制回答随机性，旅游推荐应稳定）、
     *    messages（system 提示词 + user 提示词）；
     * 5. 发送请求并读取响应；
     * 6. 若 HTTP 状态码非 2xx，打印错误日志并返回本地回答；
     * 7. 解析 JSON 响应，提取 choices[0].message.content；
     * 8. 若 content 为空，返回本地回答；否则返回 API 生成的回答（trim 处理）。
     * 异常处理：任何异常（网络超时、JSON 解析失败、IO 异常）都捕获并打印日志，返回本地回答，保证系统稳定性。
     * 该设计原因：外部 API 是不可控依赖，必须做好降级和异常处理，确保 API 故障时用户体验不受影响。
     * </p>
     *
     * @param request      用户提问请求对象
     * @param chunks       检索到的知识片段列表
     * @param fallbackAnswer 本地回答，作为 API 不可用时的降级方案
     * @return API 生成的回答或本地回答
     */
    private String generateWithApi(AiAskRequest request, List<KnowledgeChunk> chunks, String fallbackAnswer) {
        String apiKey = resolveApiKey(); // 步骤1：解析密钥
        // 步骤2：API 可用性校验，任一条件不满足则降级
        if (!aiApiEnabled || safe(aiApiUrl).isBlank() || apiKey.isBlank()) {
            return fallbackAnswer;
        }
        try {
            URL url = new URL(aiApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(8000); // 连接超时 8 秒
            connection.setReadTimeout(20000);   // 读取超时 20 秒
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);

            // 步骤4：构造请求体
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", safe(aiApiModel).isBlank() ? "gpt-4o-mini" : aiApiModel);
            payload.put("temperature", 0.3); // 低温度，回答更稳定、确定性更高
            payload.put("messages", Arrays.asList(
                    message("system", "你是旅游推荐系统的中文问答助手。必须基于提供的 RAG 检索资料回答，不要编造系统里没有的数据。回答要适合学生项目演示，结构清晰，包含路线、酒店、门票、地图或订单建议。"),
                    message("user", buildApiPrompt(request, chunks))
            ));

            // 步骤5：发送请求体
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(OBJECT_MAPPER.writeValueAsBytes(payload));
            }

            // 步骤6：检查 HTTP 状态码
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                System.err.println("AI API 调用失败，HTTP " + status + "，响应：" + readResponse(connection));
                return fallbackAnswer;
            }
            // 步骤7：解析 JSON 响应
            JsonNode root = OBJECT_MAPPER.readTree(connection.getInputStream());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            return content.isBlank() ? fallbackAnswer : content.trim();
        } catch (Exception ex) {
            // 异常降级：任何网络或解析异常都返回本地回答，确保系统不崩溃
            System.err.println("AI API 调用异常：" + ex.getMessage());
            return fallbackAnswer;
        }
    }

    /**
     * 解析 API 密钥。
     * <p>
     * 解析顺序：
     * 1. 使用配置文件中配置的 aiApiKey；
     * 2. 若配置值非空且不以 "${" 开头（说明已被正确解析），则使用配置值；
     * 3. 否则尝试从环境变量 AI_API_KEY 读取。
     * 该设计原因：支持多环境部署（开发环境用配置文件，生产环境用环境变量或密钥管理服务），
     * 同时避免未解析的占位符（如 ${AI_API_KEY}）被误用。
     * </p>
     *
     * @return 解析后的 API 密钥，若不可用则返回空字符串
     */
    private String resolveApiKey() {
        String configuredKey = safe(aiApiKey).trim();
        if (!configuredKey.isBlank() && !configuredKey.startsWith("${")) {
            return configuredKey;
        }
        return safe(System.getenv("AI_API_KEY")).trim();
    }

    /**
     * 读取 HTTP 响应内容（包括错误响应）。
     * <p>
     * 若 HTTP 状态码非 2xx，优先读取 errorStream；否则读取 inputStream。
     * 用于 API 调用失败时打印详细错误信息，便于排查问题。
     * </p>
     *
     * @param connection HTTP 连接对象
     * @return 响应文本，若无法读取则返回错误信息
     */
    private String readResponse(HttpURLConnection connection) {
        try (InputStream inputStream = connection.getErrorStream() == null ? connection.getInputStream() : connection.getErrorStream()) {
            if (inputStream == null) {
                return "";
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "无法读取错误响应：" + ex.getMessage();
        }
    }

    /**
     * 构造 OpenAI 兼容格式的 message 对象。
     * <p>私有工具方法，用于构造 API 请求体中的 messages 数组元素。</p>
     *
     * @param role    消息角色，如 "system"、"user"
     * @param content 消息内容
     * @return 包含 role 和 content 的 Map
     */
    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    /**
     * 构建发送给外部 API 的 prompt 文本。
     * <p>
     * 结构：
     * 1. 用户输入：出发地、目的地、预算、天数、兴趣、问题；
     * 2. RAG 检索资料：按序号列出每个知识片段的 type、title、content；
     * 3. 指令：请基于资料回答，给出路线建议、酒店/门票建议、下一步按钮提示，资料不足时明确说明。
     * 该设计原因：将检索资料显式注入 prompt，让大模型基于事实回答，减少幻觉（hallucination）；
     * 同时给出结构化输出要求，使回答更可控。
     * </p>
     *
     * @param request 用户提问请求对象
     * @param chunks  检索到的知识片段列表
     * @return 构建好的 API prompt 文本
     */
    private String buildApiPrompt(AiAskRequest request, List<KnowledgeChunk> chunks) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户输入：\n")
                .append("出发地：").append(firstNonBlank(request.getOriginCity(), "未填写")).append("\n")
                .append("目的地：").append(firstNonBlank(request.getDestinationCity(), request.getCity(), "未填写")).append("\n")
                .append("预算：").append(firstNonBlank(request.getBudget(), "未填写")).append("\n")
                .append("天数：").append(firstNonBlank(request.getDays(), "未填写")).append("\n")
                .append("兴趣：").append(firstNonBlank(request.getInterests(), "未填写")).append("\n")
                .append("问题：").append(request.getQuestion()).append("\n\n");
        prompt.append("RAG 检索资料：\n");
        for (int index = 0; index < chunks.size(); index++) {
            KnowledgeChunk chunk = chunks.get(index);
            prompt.append(index + 1)
                    .append(". [")
                    .append(chunk.type)
                    .append("] ")
                    .append(chunk.title)
                    .append("：")
                    .append(chunk.content)
                    .append("\n");
        }
        prompt.append("\n请基于资料回答，并给出：1）路线建议；2）酒店/门票建议；3）下一步按钮提示。资料不足时要明确说明。 ");
        return prompt.toString();
    }

    /**
     * 构建追问建议列表。
     * <p>
     * 生成逻辑：
     * 1. 确定城市（优先 destinationCity，其次 city，默认 "杭州"）；
     * 2. 添加通用追问：城市三天两晚安排、酒店推荐、门票搭配、订单操作；
     * 3. 遍历检索到的知识片段，为每个片段添加"详细介绍一下「title」"；
     * 4. 使用 {@link LinkedHashSet} 去重并保持顺序；
     * 5. 限制最多返回 5 条建议。
     * 该设计原因：追问建议引导用户继续与 AI 交互，同时展示系统功能的丰富度，提升用户体验。
     * </p>
     *
     * @param request 用户提问请求对象
     * @param chunks  检索到的知识片段列表
     * @return 追问建议字符串列表
     */
    private List<String> buildSuggestions(AiAskRequest request, List<KnowledgeChunk> chunks) {
        LinkedHashSet<String> suggestions = new LinkedHashSet<>();
        String city = firstNonBlank(request.getDestinationCity(), request.getCity(), "杭州");
        // 通用追问建议
        suggestions.add(city + "三天两晚怎么安排？");
        suggestions.add(city + "有哪些酒店适合这个预算？");
        suggestions.add(city + "门票和景点怎么搭配？");
        suggestions.add("订单创建后怎么取消或支付？");
        // 基于检索结果的个性化追问
        for (KnowledgeChunk chunk : chunks) {
            suggestions.add("详细介绍一下「" + chunk.title + "」");
        }
        return suggestions.stream().limit(5).collect(Collectors.toList());
    }

    /**
     * 对查询文本进行分词。
     * <p>
     * 分词逻辑：
     * 1. 将文本中的标点符号和空格替换为单个空格；
     * 2. 按空格分割，过滤空值，加入词集合；
     * 3. 将文本去除所有空格后，按双字滑动窗口提取所有连续两字组合，加入词集合。
     * 例如："杭州旅游" → 分词结果包含 "杭州"、"旅游"、"杭州旅游"（双字滑动）。
     * 该设计原因：中文分词不依赖外部 NLP 库，通过双字滑动和空格分割兼顾简单性和召回率。
     * </p>
     *
     * @param query 查询文本
     * @return 去重后的查询词集合
     */
    private Set<String> tokenize(String query) {
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        String normalized = safe(query).replaceAll("[，。！？、,.!?；;：:\\s]+", " ");
        Arrays.stream(normalized.split(" "))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .forEach(terms::add);
        String compact = normalized.replace(" ", "");
        for (int index = 0; index < compact.length() - 1; index++) {
            terms.add(compact.substring(index, index + 2)); // 双字滑动窗口
        }
        return terms;
    }

    /**
     * 拼接多个值为文本，过滤 null 和空值。
     * <p>私有工具方法，用于将多个字段拼接为知识片段内容，null 和空值被自动过滤。</p>
     *
     * @param values 多个对象值
     * @return 拼接后的文本，用 "，" 分隔
     */
    private String joinText(Object... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .map(String::trim)
                .filter(item -> !item.isEmpty() && !"null".equalsIgnoreCase(item))
                .collect(Collectors.joining("，"));
    }

    /**
     * 获取第一个非空字符串。
     * <p>私有工具方法，从左到右扫描，返回第一个非 null 且非空白的字符串，若全部为空则返回空字符串。</p>
     *
     * @param values 多个字符串
     * @return 第一个非空字符串，或空字符串
     */
    private String firstNonBlank(String... values) {
        return Arrays.stream(values)
                .map(this::safe)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .findFirst()
                .orElse("");
    }

    /**
     * 空安全处理字符串。
     * <p>私有工具方法，若值为 null 则返回空字符串，避免 NullPointerException。</p>
     *
     * @param value 字符串值
     * @return 原值或空字符串
     */
    private String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * 空安全处理数字对象。
     * <p>私有工具方法，若值为 null 则返回 "-"，用于知识片段中展示。</p>
     *
     * @param value 对象值
     * @return 字符串表示或 "-"
     */
    private String safeNumber(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    /**
     * 格式化金额显示。
     * <p>私有工具方法，若值为 null 则返回 "-"，否则返回 "¥" + 去除末尾零的字符串。</p>
     *
     * @param value 金额值
     * @return 格式化后的金额字符串
     */
    private String money(BigDecimal value) {
        return value == null ? "-" : "¥" + value.stripTrailingZeros().toPlainString();
    }

    /**
     * 知识片段内部类，不可变对象，用于存储检索单元。
     * <p>
     * 每个知识片段包含：
     * - type：知识类型（如"景点"、"酒店"、"系统规则"）；
     * - title：知识标题（如景点名称、酒店名称）；
     * - content：知识内容（拼接后的文本描述）；
     * - score：匹配度评分（检索阶段动态计算）。
     * 声明为不可变对象（字段 private final，无 setter），保证线程安全。
     * withScore 方法返回新实例，不修改原实例，符合函数式编程风格。
     * </p>
     */
    private static class KnowledgeChunk {
        private final String type;
        private final String title;
        private final String content;
        private final int score;

        private KnowledgeChunk(String type, String title, String content) {
            this(type, title, content, 0);
        }

        private KnowledgeChunk(String type, String title, String content, int score) {
            this.type = type;
            this.title = title;
            this.content = content;
            this.score = score;
        }

        private KnowledgeChunk withScore(int score) {
            return new KnowledgeChunk(type, title, content, score);
        }
    }
}
