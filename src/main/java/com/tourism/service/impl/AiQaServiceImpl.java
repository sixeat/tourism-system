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

@Service
public class AiQaServiceImpl implements AiQaService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Autowired
    private HotelMapper hotelMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private TravelRouteMapper travelRouteMapper;

    @Autowired
    private RouteSpotMapper routeSpotMapper;

    @Autowired
    private MapPointMapper mapPointMapper;

    @Value("${ai.api.enabled:false}")
    private boolean aiApiEnabled;

    @Value("${ai.api.url:}")
    private String aiApiUrl;

    @Value("${ai.api.key:}")
    private String aiApiKey;

    @Value("${ai.api.model:gpt-4o-mini}")
    private String aiApiModel;

    @Override
    public AiAnswerVO ask(AiAskRequest request) {
        AiAskRequest safeRequest = request == null ? new AiAskRequest() : request;
        String question = safe(safeRequest.getQuestion()).trim();
        if (question.isEmpty()) {
            throw new BusinessException("请输入想咨询的旅游问题");
        }
        if (question.length() > 300) {
            throw new BusinessException("问题太长了，建议控制在 300 字以内");
        }

        List<KnowledgeChunk> chunks = buildKnowledgeBase();
        String enrichedQuery = buildEnrichedQuery(safeRequest);
        String destination = firstNonBlank(safeRequest.getDestinationCity(), safeRequest.getCity());
        List<KnowledgeChunk> topChunks = retrieve(enrichedQuery, destination, chunks, 6);

        String localAnswer = buildAnswer(safeRequest, topChunks);
        String answer = generateWithApi(safeRequest, topChunks, localAnswer);

        AiAnswerVO vo = new AiAnswerVO();
        vo.setAnswer(answer);
        vo.setReferences(topChunks.stream()
                .map(chunk -> "[" + chunk.type + "] " + chunk.title + "：" + chunk.content)
                .collect(Collectors.toList()));
        vo.setSuggestions(buildSuggestions(safeRequest, topChunks));
        vo.setMode(answer.equals(localAnswer) ? "LOCAL_RAG" : "API_RAG");
        return vo;
    }

    private List<KnowledgeChunk> buildKnowledgeBase() {
        List<KnowledgeChunk> chunks = new ArrayList<>();
        List<ScenicSpot> scenicSpots = scenicSpotMapper.selectAll();
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

        for (Hotel hotel : hotelMapper.selectAll()) {
            chunks.add(new KnowledgeChunk(
                    "酒店",
                    hotel.getHotelName(),
                    joinText(hotel.getHotelName(), hotel.getCity(), hotel.getLevel(), hotel.getAddress(), hotel.getDescription())
            ));
        }

        for (Ticket ticket : ticketMapper.selectAll()) {
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

        for (TravelRoute route : travelRouteMapper.selectAll()) {
            List<RouteSpot> routeSpots = routeSpotMapper.selectByRouteId(route.getId());
            String scenicOrder = routeSpots.stream()
                    .sorted(Comparator.comparing(RouteSpot::getDayNo, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(RouteSpot::getSortNo, Comparator.nullsLast(Integer::compareTo)))
                    .map(RouteSpot::getScenicId)
                    .map(scenicMap::get)
                    .filter(Objects::nonNull)
                    .map(ScenicSpot::getScenicName)
                    .collect(Collectors.joining(" → "));
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

        for (MapPoint point : mapPointMapper.selectAll()) {
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

        chunks.add(new KnowledgeChunk("系统规则", "订单规则", "用户可以预订酒店和门票，可以在我的订单中查看订单；已创建订单支持取消和支付，已取消订单不能再次取消。"));
        chunks.add(new KnowledgeChunk("系统规则", "账号角色", "普通用户账号 demo / demo123，可浏览首页、下单和查看订单；管理员账号 admin / admin123，可进入后台维护景点、酒店、门票、路线和订单状态。"));
        chunks.add(new KnowledgeChunk("系统规则", "地图功能", "系统接入高德地图，旅游地图可以展示景点、酒店、美食、机场、火车站；路线规划页面可把景点顺序传到地图页并绘制游玩顺序。"));
        chunks.add(new KnowledgeChunk("系统规则", "房态互斥", "酒店房态按入住日期和离店日期动态计算；同一房型在日期重叠时会被未取消订单锁定，可订数量减少；若可订数量为 0，前端显示满房并禁止选择。"));
        chunks.add(new KnowledgeChunk("系统规则", "AI问答", "AI 行程助手会先检索本地旅游知识库，再把检索资料交给大模型生成；如果外部模型不可用，会自动回退到本地 RAG 回答。"));
        return chunks;
    }

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

    private List<KnowledgeChunk> retrieve(String query, String destination, List<KnowledgeChunk> chunks, int limit) {
        Set<String> queryTerms = tokenize(query);
        List<String> preferredTypes = inferTypes(query);
        List<KnowledgeChunk> scoredChunks = chunks.stream()
                .map(chunk -> chunk.withScore(score(queryTerms, query, destination, preferredTypes, chunk)))
                .filter(chunk -> chunk.score > 0)
                .sorted(Comparator.comparingInt((KnowledgeChunk chunk) -> chunk.score).reversed())
                .limit(limit)
                .collect(Collectors.toList());
        if (!scoredChunks.isEmpty()) {
            return scoredChunks;
        }
        if (!preferredTypes.isEmpty()) {
            List<KnowledgeChunk> fallbackChunks = chunks.stream()
                    .filter(chunk -> preferredTypes.contains(chunk.type))
                    .limit(limit)
                    .collect(Collectors.toList());
            if (!fallbackChunks.isEmpty()) {
                return fallbackChunks;
            }
        }
        return chunks.stream().limit(limit).collect(Collectors.toList());
    }

    private int score(Set<String> queryTerms, String query, String destination, List<String> preferredTypes, KnowledgeChunk chunk) {
        String text = (chunk.type + " " + chunk.title + " " + chunk.content).toLowerCase(Locale.ROOT);
        int score = 0;
        String destinationText = safe(destination).trim().toLowerCase(Locale.ROOT);
        if (!destinationText.isEmpty() && text.contains(destinationText)) {
            score += 80;
        }
        if (!destinationText.isEmpty() && safe(chunk.title).toLowerCase(Locale.ROOT).contains(destinationText)) {
            score += 30;
        }
        if (preferredTypes.contains(chunk.type)) {
            score += 24;
        }
        for (String term : queryTerms) {
            String lowerTerm = term.toLowerCase(Locale.ROOT);
            if (text.contains(lowerTerm)) {
                score += term.length() >= 2 ? 6 : 2;
            }
        }
        if (text.contains(query.toLowerCase(Locale.ROOT))) {
            score += 10;
        }
        return score;
    }

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

    private boolean containsAny(String text, String... keywords) {
        String source = safe(text);
        return Arrays.stream(keywords).anyMatch(source::contains);
    }

    private String buildAnswer(AiAskRequest request, List<KnowledgeChunk> chunks) {
        String destination = firstNonBlank(request.getDestinationCity(), request.getCity(), "目的地");
        String origin = firstNonBlank(request.getOriginCity(), "出发地");
        String days = firstNonBlank(request.getDays(), "未填写");
        String budget = firstNonBlank(request.getBudget(), "未填写");
        String interests = firstNonBlank(request.getInterests(), "未填写");

        StringBuilder answer = new StringBuilder();
        answer.append("我先按你的输入做本地 RAG 检索：")
                .append(origin).append(" → ").append(destination)
                .append("，").append(days).append("天，预算 ").append(budget)
                .append("，兴趣：").append(interests).append("。\n\n");
        if (chunks.isEmpty()) {
            answer.append("当前知识库没有检索到特别匹配的资料，可以换个问法，例如“杭州三日路线怎么安排”“酒店房态怎么判断”。");
            return answer.toString();
        }
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
        answer.append("\n你可以继续点击“进入路线规划”生成结构化路线，再点击“地图看路线”把景点画到地图上。");
        return answer.toString();
    }

    private String generateWithApi(AiAskRequest request, List<KnowledgeChunk> chunks, String fallbackAnswer) {
        String apiKey = resolveApiKey();
        if (!aiApiEnabled || safe(aiApiUrl).isBlank() || apiKey.isBlank()) {
            return fallbackAnswer;
        }
        try {
            URL url = new URL(aiApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(20000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", safe(aiApiModel).isBlank() ? "gpt-4o-mini" : aiApiModel);
            payload.put("temperature", 0.3);
            payload.put("messages", Arrays.asList(
                    message("system", "你是旅游推荐系统的中文问答助手。必须基于提供的 RAG 检索资料回答，不要编造系统里没有的数据。回答要适合学生项目演示，结构清晰，包含路线、酒店、门票、地图或订单建议。"),
                    message("user", buildApiPrompt(request, chunks))
            ));

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(OBJECT_MAPPER.writeValueAsBytes(payload));
            }

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                System.err.println("AI API 调用失败，HTTP " + status + "，响应：" + readResponse(connection));
                return fallbackAnswer;
            }
            JsonNode root = OBJECT_MAPPER.readTree(connection.getInputStream());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            return content.isBlank() ? fallbackAnswer : content.trim();
        } catch (Exception ex) {
            System.err.println("AI API 调用异常：" + ex.getMessage());
            return fallbackAnswer;
        }
    }

    private String resolveApiKey() {
        String configuredKey = safe(aiApiKey).trim();
        if (!configuredKey.isBlank() && !configuredKey.startsWith("${")) {
            return configuredKey;
        }
        return safe(System.getenv("AI_API_KEY")).trim();
    }

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

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

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

    private List<String> buildSuggestions(AiAskRequest request, List<KnowledgeChunk> chunks) {
        LinkedHashSet<String> suggestions = new LinkedHashSet<>();
        String city = firstNonBlank(request.getDestinationCity(), request.getCity(), "杭州");
        suggestions.add(city + "三天两晚怎么安排？");
        suggestions.add(city + "有哪些酒店适合这个预算？");
        suggestions.add(city + "门票和景点怎么搭配？");
        suggestions.add("订单创建后怎么取消或支付？");
        for (KnowledgeChunk chunk : chunks) {
            suggestions.add("详细介绍一下「" + chunk.title + "」");
        }
        return suggestions.stream().limit(5).collect(Collectors.toList());
    }

    private Set<String> tokenize(String query) {
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        String normalized = safe(query).replaceAll("[，。！？、,.!?；;：:\\s]+", " ");
        Arrays.stream(normalized.split(" "))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .forEach(terms::add);
        String compact = normalized.replace(" ", "");
        for (int index = 0; index < compact.length() - 1; index++) {
            terms.add(compact.substring(index, index + 2));
        }
        return terms;
    }

    private String joinText(Object... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .map(String::trim)
                .filter(item -> !item.isEmpty() && !"null".equalsIgnoreCase(item))
                .collect(Collectors.joining("，"));
    }

    private String firstNonBlank(String... values) {
        return Arrays.stream(values)
                .map(this::safe)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .findFirst()
                .orElse("");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeNumber(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String money(BigDecimal value) {
        return value == null ? "-" : "¥" + value.stripTrailingZeros().toPlainString();
    }

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
