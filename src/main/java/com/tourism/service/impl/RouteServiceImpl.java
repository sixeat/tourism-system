package com.tourism.service.impl;

import com.tourism.dto.RouteRecommendRequest;
import com.tourism.entity.RouteSpot;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.TravelRoute;
import com.tourism.mapper.RouteSpotMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.mapper.TravelRouteMapper;
import com.tourism.service.RouteService;
import com.tourism.vo.RouteRecommendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 旅游路线推荐服务实现类。
 * <p>
 * 本类由 Spring {@link org.springframework.stereotype.Service} 注解标记，注入三个 Mapper：
 * {@link TravelRouteMapper} 用于查询路线主体，{@link RouteSpotMapper} 用于查询路线与景点关联，
 * {@link ScenicSpotMapper} 用于查询景点信息（用于匹配度计算和备选路线生成）。
 * 职责：根据用户出行偏好（目的地、天数、预算、兴趣）推荐最优旅游路线。
 * 推荐策略为"数据库优先 + 动态备选"：优先匹配系统已有路线，若无匹配则自动生成基于热门景点的备选路线。
 * 本类方法为只读查询，不涉及数据库写操作，因此不声明 {@link org.springframework.transaction.annotation.Transactional}。
 * </p>
 *
 * @author Tourism System
 * @see RouteService
 */
@Service
public class RouteServiceImpl implements RouteService {

    /**
     * 旅游路线主体 Mapper，自动注入。负责 travel_route 表查询。
     */
    @Autowired
    private TravelRouteMapper travelRouteMapper;

    /**
     * 路线景点关联 Mapper，自动注入。负责 route_spot 表查询，按 day_no, sort_no 排序。
     */
    @Autowired
    private RouteSpotMapper routeSpotMapper;

    /**
     * 景点信息 Mapper，自动注入。负责 scenic_spot 表查询，用于景点名称解析和热门景点排序。
     */
    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    /**
     * 根据用户出行偏好推荐旅游路线。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@link #recommendFromDatabase(RouteRecommendRequest)} 尝试从数据库匹配已有路线；
     * 2. 若数据库中有匹配结果（非空），直接返回最多 3 条最优路线；
     * 3. 若数据库无匹配，则进入动态备选路线生成逻辑：
     *    - 确定目标城市：优先使用 destinationCity，若为空则使用 city，若仍为空则视为空字符串；
     *    - 调用 {@code scenicSpotMapper.selectAll()} 查询所有景点（SQL SELECT * FROM scenic_spot）；
     *    - 按目标城市过滤（若城市非空），并按热度（popularity）降序、评分（score）降序排列，取前 3 个热门景点；
     *    - 若目标城市无景点（如用户输入了不存在的城市），使用默认景点列表（西湖、灵隐寺）作为兜底；
     *    - 生成两条备选路线：
     *      a) "城市精华路线"：以热门景点串联为核心，适合首次到该城市的游客；
     *      b) "高性价比路线"：预算降低 15%，注重预算控制，适合周末轻量出行；
     * 4. 返回备选路线列表。
     * 该设计原因：数据库优先策略保证系统录入的路线能被充分利用，动态备选策略保证即使无匹配也能给用户返回有价值的建议。
     * </p>
     *
     * @param request 路线推荐请求对象
     * @return 推荐路线视图对象 {@link RouteRecommendVO} 列表
     */
    @Override
    public List<RouteRecommendVO> recommendRoutes(RouteRecommendRequest request) {
        // 步骤1：优先从数据库匹配已有路线
        List<RouteRecommendVO> databaseRoutes = recommendFromDatabase(request);
        // 步骤2：若数据库有匹配，直接返回，不再生成备选
        if (!databaseRoutes.isEmpty()) {
            return databaseRoutes;
        }

        // 步骤3：动态备选路线生成
        // 确定目标城市：destinationCity 优先，其次 city
        String targetCity = request.getDestinationCity() == null || request.getDestinationCity().trim().isEmpty()
                ? (request.getCity() == null ? "" : request.getCity().trim())
                : request.getDestinationCity().trim();
        // 查询所有景点并按城市过滤、按热度/评分排序，取 Top3 热门景点
        List<String> cityScenicOrder = scenicSpotMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> targetCity.isEmpty() || targetCity.equals(item.getCity()))
                .sorted(Comparator
                        .comparing((ScenicSpot item) -> item.getPopularity() == null ? 0 : item.getPopularity()).reversed()
                        .thenComparing(item -> item.getScore() == null ? BigDecimal.ZERO : item.getScore(), Comparator.reverseOrder()))
                .limit(3)
                .map(ScenicSpot::getScenicName)
                .collect(Collectors.toList());
        // 若目标城市无景点，使用默认景点兜底，避免返回空列表
        if (cityScenicOrder.isEmpty()) {
            cityScenicOrder = Arrays.asList("西湖风景名胜区", "灵隐寺");
        }

        // 构建两条备选路线
        List<RouteRecommendVO> fallbackRoutes = new ArrayList<>();
        fallbackRoutes.add(buildFallbackRoute(
                targetCity,
                request.getDays(),
                request.getBudget(),
                "城市精华路线",
                Arrays.asList("热门景点串联", "酒店联动推荐", "门票一站式预订"),
                cityScenicOrder,
                "适合第一次到" + (targetCity.isEmpty() ? "目的地" : targetCity) + "的游客，覆盖高频打卡景点。"
        ));
        fallbackRoutes.add(buildFallbackRoute(
                targetCity,
                request.getDays(),
                request.getBudget() == null ? null : request.getBudget().multiply(new BigDecimal("0.85")),
                "高性价比路线",
                Arrays.asList("预算优先", "周边酒店推荐", "库存不足自动替代"),
                cityScenicOrder,
                "更注重预算控制，适合周末轻量出行。"
        ));
        return fallbackRoutes;
    }

    /**
     * 从数据库已有路线中筛选并排序推荐结果。
     * <p>
     * 业务逻辑步骤：
     * 1. 调用 {@code travelRouteMapper.selectAll()} 查询所有路线（SQL SELECT * FROM travel_route）；
     * 2. 若路线列表为空，直接返回空列表，进入备选路线生成逻辑；
     * 3. 确定目标城市（destinationCity 优先，其次 city）和请求参数（days, budget, interests）；
     * 4. 调用 {@code scenicSpotMapper.selectAll()} 查询所有景点，构建景点 ID 到实体的映射表（Map），用于后续根据 scenicId 查找景点名称；
     * 5. 使用 Stream 过滤和排序：
     *    - 过滤：若城市非空，只保留 city 匹配的路线的（忽略大小写）；
     *    - 排序：按匹配度评分降序（{@link #routeMatchScore}），评分相同则按预算升序（低预算优先）；
     *    - 截取：最多取 3 条路线；
     * 6. 将每条路线转换为 {@link RouteRecommendVO}（{@link #toRecommendVO}），包含景点游览顺序、亮点、匹配原因等；
     * 7. 返回转换后的 VO 列表。
     * </p>
     *
     * @param request 路线推荐请求对象
     * @return 数据库匹配到的 {@link RouteRecommendVO} 列表，若无匹配则返回空列表
     */
    private List<RouteRecommendVO> recommendFromDatabase(RouteRecommendRequest request) {
        List<TravelRoute> routes = travelRouteMapper.selectAll(); // 步骤1：查询所有路线
        if (routes == null || routes.isEmpty()) {
            return new ArrayList<>(); // 步骤2：无路线则直接返回空列表
        }

        // 步骤3：确定城市和请求参数
        String city = request.getDestinationCity() == null || request.getDestinationCity().trim().isEmpty()
                ? (request.getCity() == null ? "" : request.getCity().trim())
                : request.getDestinationCity().trim();
        Integer days = request.getDays();
        BigDecimal budget = request.getBudget();
        String interestText = request.getInterests() == null ? "" : String.join(",", request.getInterests()).toLowerCase(Locale.ROOT);

        // 步骤4：构建景点 ID 映射表，用于 scenicId → ScenicSpot 名称转换
        Map<Long, ScenicSpot> scenicMap = scenicSpotMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ScenicSpot::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        // 步骤5：过滤、排序、截取
        return routes.stream()
                .filter(route -> city.isEmpty() || city.equalsIgnoreCase(route.getCity())) // 城市过滤
                .sorted(Comparator
                        .comparing((TravelRoute route) -> routeMatchScore(route, days, budget, interestText, scenicMap)) // 匹配度评分降序
                        .reversed()
                        .thenComparing(route -> route.getBudget() == null ? BigDecimal.ZERO : route.getBudget())) // 预算升序作为次要排序
                .limit(3) // 最多取 3 条
                .map(route -> toRecommendVO(route, scenicMap, request)) // 步骤6：转换为 VO
                .collect(Collectors.toList());
    }

    /**
     * 计算单条路线与用户请求的匹配度评分。
     * <p>
     * 评分维度（总分无上限，但通常不超过 100+）：
     * 1. 天数匹配：若用户提供了 days，计算 |route.days - days| × 8 的惩罚分，基础分 30，差距越大分越低；
     * 2. 预算匹配：若用户提供了 budget，计算路线预算与用户预算差值的绝对值，每 100 元扣 1 分，基础分 30；
     * 3. 主题兴趣匹配：若路线 theme 非空且用户兴趣文本包含该主题，加 20 分；若主题包含 "精华" 额外加 6 分；
     * 4. 描述丰富度：若路线描述非空，加 8 分（鼓励有详细描述的路线）；
     * 5. 景点兴趣匹配：调用 {@link #scenicMatchScore} 计算路线中各景点与用户兴趣的匹配度之和。
     * 该设计原因：多维度打分，让天数、预算、兴趣、描述都参与排序，避免单一维度主导。
     * </p>
     *
     * @param route       路线实体
     * @param days        用户期望天数
     * @param budget      用户期望预算
     * @param interestText 用户兴趣文本（已小写化）
     * @param scenicMap   景点 ID 映射表
     * @return 匹配度评分，数值越大表示越匹配
     */
    private int routeMatchScore(TravelRoute route, Integer days, BigDecimal budget, String interestText, Map<Long, ScenicSpot> scenicMap) {
        int score = 0;
        // 维度1：天数匹配，差距越小得分越高
        if (days != null && route.getDays() != null) {
            score += Math.max(0, 30 - Math.abs(route.getDays() - days) * 8);
        }
        // 维度2：预算匹配，差距越小得分越高
        if (budget != null && route.getBudget() != null) {
            BigDecimal gap = route.getBudget().subtract(budget).abs();
            score += Math.max(0, 30 - gap.divide(new BigDecimal("100"), 0, RoundingMode.DOWN).intValue());
        }
        // 维度3：主题兴趣匹配
        if (route.getTheme() != null && !route.getTheme().isBlank() && !interestText.isBlank()) {
            String theme = route.getTheme().toLowerCase(Locale.ROOT);
            if (interestText.contains(theme)) {
                score += 20;
            }
            if (theme.contains("精华")) {
                score += 6;
            }
        }
        // 维度4：描述丰富度奖励
        if (route.getRouteDesc() != null && !route.getRouteDesc().isBlank()) {
            score += 8;
        }
        // 维度5：景点兴趣匹配
        score += scenicMatchScore(route, scenicMap, interestText);
        return score;
    }

    /**
     * 计算路线中景点与用户兴趣的匹配度评分。
     * <p>
     * 评分逻辑：
     * 1. 若用户兴趣文本为空，直接返回 0；
     * 2. 查询该路线关联的所有景点（{@code routeSpotMapper.selectByRouteId}，SQL WHERE route_id = ? ORDER BY day_no, sort_no）；
     * 3. 对每个景点，将其 scenicName、category、tags、description 拼接为小写文本；
     * 4. 将用户兴趣文本按逗号、空格等分隔为关键词，逐个匹配景点文本，每命中一个关键词加 10 分；
     * 5. 景点热度加成：popularity / 15 取上限 8 分；
     * 6. 景点评分加成：score × 2 - 6，最低 0 分。
     * 该设计原因：让用户兴趣直接作用于景点匹配，热门和高评分景点获得额外加权，提升推荐质量。
     * </p>
     *
     * @param route       路线实体
     * @param scenicMap   景点 ID 映射表
     * @param interestText 用户兴趣文本（已小写化）
     * @return 景点匹配度评分
     */
    private int scenicMatchScore(TravelRoute route, Map<Long, ScenicSpot> scenicMap, String interestText) {
        if (interestText == null || interestText.isBlank()) {
            return 0; // 无兴趣文本，不参与评分
        }
        return routeSpotMapper.selectByRouteId(route.getId()).stream()
                .map(RouteSpot::getScenicId)
                .map(scenicMap::get)
                .filter(Objects::nonNull)
                .mapToInt(scenic -> {
                    String text = joinLower(scenic.getScenicName(), scenic.getCategory(), scenic.getTags(), scenic.getDescription());
                    int score = 0;
                    // 兴趣关键词匹配，每命中一个加 10 分
                    for (String term : interestText.split("[,，/、\\s]+")) {
                        if (!term.isBlank() && text.contains(term.trim().toLowerCase(Locale.ROOT))) {
                            score += 10;
                        }
                    }
                    // 热度加成： popularity / 15，上限 8 分
                    if (scenic.getPopularity() != null) {
                        score += Math.min(8, scenic.getPopularity() / 15);
                    }
                    // 评分加成：score * 2 - 6，最低 0
                    if (scenic.getScore() != null) {
                        score += Math.max(0, scenic.getScore().multiply(new BigDecimal("2")).intValue() - 6);
                    }
                    return score;
                })
                .sum();
    }

    /**
     * 将路线实体转换为推荐视图对象（RouteRecommendVO）。
     * <p>
     * 转换步骤：
     * 1. 查询该路线关联的所有景点（{@code routeSpotMapper.selectByRouteId}），按 day_no, sort_no 排序；
     * 2. 将景点 ID 转换为景点名称（通过 scenicMap），过滤空值，构建景点游览顺序列表；
     * 3. 构建亮点列表（{@link #buildHighlights}），包含主题、景点串联、兴趣贴合、描述等；
     * 4. 重新计算匹配度评分（用于 VO 中展示）；
     * 5. 计算预算差：routeBudget - requestBudget，用于展示"预算内"或"超预算"；
     * 6. 设置 VO 各字段：城市、天数、路线名、预估费用、亮点、景点顺序、主题、描述、匹配度、预算差、匹配原因；
     * 7. 对城市、天数、预估费用做空安全处理，提供默认值（杭州、3天、1288元）。
     * 该设计原因：VO 是面向前端展示的数据结构，需要包含所有前端渲染所需的字段，且需做空值兜底。
     * </p>
     *
     * @param route     路线实体
     * @param scenicMap 景点 ID 映射表
     * @param request   路线推荐请求对象
     * @return 组装好的 {@link RouteRecommendVO}
     */
    private RouteRecommendVO toRecommendVO(TravelRoute route, Map<Long, ScenicSpot> scenicMap, RouteRecommendRequest request) {
        List<RouteSpot> spots = routeSpotMapper.selectByRouteId(route.getId()); // 步骤1：查询路线关联景点
        List<String> scenicOrder = spots.stream()
                .map(RouteSpot::getScenicId)
                .map(scenicMap::get)
                .filter(Objects::nonNull)
                .map(ScenicSpot::getScenicName)
                .collect(Collectors.toList()); // 步骤2：构建景点游览顺序

        List<String> highlights = buildHighlights(route, scenicOrder, request); // 步骤3：构建亮点
        Integer days = request.getDays();
        BigDecimal budget = request.getBudget();
        String interestText = request.getInterests() == null ? "" : String.join(",", request.getInterests()).toLowerCase(Locale.ROOT);
        int score = routeMatchScore(route, days, budget, interestText, scenicMap); // 步骤4：计算匹配度
        BigDecimal budgetGap = budget == null || route.getBudget() == null ? BigDecimal.ZERO : route.getBudget().subtract(budget); // 步骤5：预算差

        RouteRecommendVO vo = new RouteRecommendVO();
        vo.setCity(route.getCity() == null ? "杭州" : route.getCity()); // 城市默认兜底
        vo.setDays(route.getDays() == null ? 3 : route.getDays()); // 天数默认兜底
        vo.setRouteName(route.getRouteName());
        vo.setEstimatedCost(route.getBudget() == null ? new BigDecimal("1288.00") : route.getBudget()); // 费用默认兜底
        vo.setHighlights(highlights);
        vo.setScenicOrder(scenicOrder);
        vo.setTheme(route.getTheme());
        vo.setRouteDesc(route.getRouteDesc());
        vo.setMatchScore(Math.min(99, Math.max(0, score))); // 匹配度限制在 0-99 之间
        vo.setBudgetGap(budgetGap);
        vo.setMatchReason(buildMatchReason(route, scenicOrder, request, budgetGap)); // 匹配原因文本
        return vo;
    }

    /**
     * 构建路线亮点列表。
     * <p>
     * 亮点生成逻辑：
     * 1. 若路线主题非空，添加 "主题偏向：{theme}"；
     * 2. 若景点顺序非空，添加 "串联景点：{景点1} → {景点2} → ..."；
     * 3. 若用户兴趣非空，添加 "贴合兴趣：{兴趣1} / {兴趣2} ..."；
     * 4. 若路线描述非空，直接添加描述文本；
     * 5. 若以上均无，使用默认亮点兜底（"热门景点串联"、"酒店联动推荐"、"门票一站式预订"）。
     * 该设计原因：亮点是前端卡片展示的关键信息，需优先展示个性化内容，无个性化内容时退回到通用描述。
     * </p>
     *
     * @param route       路线实体
     * @param scenicOrder 景点游览顺序列表
     * @param request     路线推荐请求对象
     * @return 亮点字符串列表
     */
    private List<String> buildHighlights(TravelRoute route, List<String> scenicOrder, RouteRecommendRequest request) {
        List<String> highlights = new ArrayList<>();
        if (route.getTheme() != null && !route.getTheme().isBlank()) {
            highlights.add("主题偏向：" + route.getTheme());
        }
        if (!scenicOrder.isEmpty()) {
            highlights.add("串联景点：" + String.join(" → ", scenicOrder));
        }
        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            highlights.add("贴合兴趣：" + String.join(" / ", request.getInterests()));
        }
        if (route.getRouteDesc() != null && !route.getRouteDesc().isBlank()) {
            highlights.add(route.getRouteDesc());
        }
        if (highlights.isEmpty()) {
            highlights.addAll(Arrays.asList("热门景点串联", "酒店联动推荐", "门票一站式预订"));
        }
        return highlights;
    }

    /**
     * 构建路线匹配原因文本。
     * <p>
     * 匹配原因用于前端展示"为什么推荐这条路线"，生成逻辑：
     * 1. 若用户提供了天数且路线有 days，计算天数差距，完全匹配则显示"天数完全匹配"，否则显示相差天数；
     * 2. 若用户提供了预算且路线有预算，比较两者：路线预算 <= 用户预算则显示"预算内可完成"，否则显示"超预算约 ¥X"；
     * 3. 若用户提供了兴趣，检查路线主题、描述、景点名称中是否包含用户兴趣关键词，命中则显示"兴趣命中：{关键词}"，否则显示"按热门景点补充推荐"；
     * 4. 若以上均不满足，使用默认原因"综合城市、热度和景点评分推荐"。
     * 该设计原因：透明化推荐逻辑，让用户理解为什么看到这条路线，提升信任感和用户体验。
     * </p>
     *
     * @param route       路线实体
     * @param scenicOrder 景点游览顺序列表
     * @param request     路线推荐请求对象
     * @param budgetGap   预算差值
     * @return 匹配原因字符串
     */
    private String buildMatchReason(TravelRoute route, List<String> scenicOrder, RouteRecommendRequest request, BigDecimal budgetGap) {
        List<String> reasons = new ArrayList<>();
        if (request.getDays() != null && route.getDays() != null) {
            int dayGap = Math.abs(route.getDays() - request.getDays());
            reasons.add(dayGap == 0 ? "天数完全匹配" : "天数相差 " + dayGap + " 天");
        }
        if (request.getBudget() != null && route.getBudget() != null) {
            int compare = route.getBudget().compareTo(request.getBudget());
            if (compare <= 0) {
                reasons.add("预算内可完成");
            } else {
                reasons.add("超预算约 ¥" + budgetGap.abs().stripTrailingZeros().toPlainString());
            }
        }
        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            String routeText = joinLower(route.getTheme(), route.getRouteDesc(), String.join(",", scenicOrder));
            List<String> matched = request.getInterests().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .filter(item -> routeText.contains(item.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
            if (!matched.isEmpty()) {
                reasons.add("兴趣命中：" + String.join("、", matched));
            } else {
                reasons.add("按热门景点补充推荐");
            }
        }
        if (reasons.isEmpty()) {
            reasons.add("综合城市、热度和景点评分推荐");
        }
        return String.join(" · ", reasons);
    }

    /**
     * 将多个字符串拼接为小写字符串，用于文本匹配。
     * <p>私有工具方法，过滤 null 值，用逗号拼接，统一转小写，便于兴趣关键词匹配。</p>
     *
     * @param values 多个字符串
     * @return 拼接后的小写字符串
     */
    private String joinLower(String... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","))
                .toLowerCase(Locale.ROOT);
    }

    /**
     * 构建动态备选路线视图对象。
     * <p>
     * 当数据库无匹配路线时，基于城市热门景点自动生成备选方案。
     * 参数做空安全处理：城市为空则用 "杭州"，天数为空则用 3，预算为空则用 1288.00。
     * 固定匹配度 70 分，预算差为 0，匹配原因固定为"无完全匹配路线，按城市默认景点与预算生成备选方案"。
     * 该设计原因：确保用户即使输入了冷门城市或极端条件，也能获得有意义的推荐，而非空结果。
     * </p>
     *
     * @param city        目标城市
     * @param days        计划天数
     * @param budget      计划预算
     * @param routeName   路线名称
     * @param highlights  亮点列表
     * @param scenicOrder 景点游览顺序
     * @param routeDesc   路线描述
     * @return 组装好的 {@link RouteRecommendVO}
     */
    private RouteRecommendVO buildFallbackRoute(String city, Integer days, BigDecimal budget, String routeName,
                                                List<String> highlights, List<String> scenicOrder, String routeDesc) {
        RouteRecommendVO vo = new RouteRecommendVO();
        vo.setCity(city == null || city.isBlank() ? "杭州" : city);
        vo.setDays(days == null ? 3 : days);
        vo.setRouteName(routeName);
        vo.setEstimatedCost(budget == null ? new BigDecimal("1288.00") : budget);
        vo.setHighlights(highlights);
        vo.setScenicOrder(scenicOrder);
        vo.setTheme("经典推荐");
        vo.setRouteDesc(routeDesc);
        vo.setMatchScore(70);
        vo.setBudgetGap(BigDecimal.ZERO);
        vo.setMatchReason("无完全匹配路线，按城市默认景点与预算生成备选方案");
        return vo;
    }
}
