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

@Service
public class RouteServiceImpl implements RouteService {

    @Autowired
    private TravelRouteMapper travelRouteMapper;

    @Autowired
    private RouteSpotMapper routeSpotMapper;

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Override
    public List<RouteRecommendVO> recommendRoutes(RouteRecommendRequest request) {
        List<RouteRecommendVO> databaseRoutes = recommendFromDatabase(request);
        if (!databaseRoutes.isEmpty()) {
            return databaseRoutes;
        }

        String targetCity = request.getDestinationCity() == null || request.getDestinationCity().trim().isEmpty()
                ? (request.getCity() == null ? "" : request.getCity().trim())
                : request.getDestinationCity().trim();
        List<String> cityScenicOrder = scenicSpotMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> targetCity.isEmpty() || targetCity.equals(item.getCity()))
                .sorted(Comparator
                        .comparing((ScenicSpot item) -> item.getPopularity() == null ? 0 : item.getPopularity()).reversed()
                        .thenComparing(item -> item.getScore() == null ? BigDecimal.ZERO : item.getScore(), Comparator.reverseOrder()))
                .limit(3)
                .map(ScenicSpot::getScenicName)
                .collect(Collectors.toList());
        if (cityScenicOrder.isEmpty()) {
            cityScenicOrder = Arrays.asList("\u897f\u6e56\u98ce\u666f\u540d\u80dc\u533a", "\u7075\u9690\u5bfa");
        }

        List<RouteRecommendVO> fallbackRoutes = new ArrayList<>();
        fallbackRoutes.add(buildFallbackRoute(
                targetCity,
                request.getDays(),
                request.getBudget(),
                "\u57ce\u5e02\u7cbe\u534e\u8def\u7ebf",
                Arrays.asList("\u70ed\u95e8\u666f\u70b9\u4e32\u8054", "\u9152\u5e97\u8054\u52a8\u63a8\u8350", "\u95e8\u7968\u4e00\u7ad9\u5f0f\u9884\u8ba2"),
                cityScenicOrder,
                "\u9002\u5408\u7b2c\u4e00\u6b21\u5230" + (targetCity.isEmpty() ? "\u76ee\u7684\u5730" : targetCity) + "\u7684\u6e38\u5ba2\uff0c\u8986\u76d6\u9ad8\u9891\u6253\u5361\u666f\u70b9\u3002"
        ));
        fallbackRoutes.add(buildFallbackRoute(
                targetCity,
                request.getDays(),
                request.getBudget() == null ? null : request.getBudget().multiply(new BigDecimal("0.85")),
                "\u9ad8\u6027\u4ef7\u6bd4\u8def\u7ebf",
                Arrays.asList("\u9884\u7b97\u4f18\u5148", "\u5468\u8fb9\u9152\u5e97\u63a8\u8350", "\u5e93\u5b58\u4e0d\u8db3\u81ea\u52a8\u66ff\u4ee3"),
                cityScenicOrder,
                "\u66f4\u6ce8\u91cd\u9884\u7b97\u63a7\u5236\uff0c\u9002\u5408\u5468\u672b\u8f7b\u91cf\u51fa\u884c\u3002"
        ));
        return fallbackRoutes;
    }

    private List<RouteRecommendVO> recommendFromDatabase(RouteRecommendRequest request) {
        List<TravelRoute> routes = travelRouteMapper.selectAll();
        if (routes == null || routes.isEmpty()) {
            return new ArrayList<>();
        }

        String city = request.getDestinationCity() == null || request.getDestinationCity().trim().isEmpty()
                ? (request.getCity() == null ? "" : request.getCity().trim())
                : request.getDestinationCity().trim();
        Integer days = request.getDays();
        BigDecimal budget = request.getBudget();
        String interestText = request.getInterests() == null ? "" : String.join(",", request.getInterests()).toLowerCase(Locale.ROOT);

        Map<Long, ScenicSpot> scenicMap = scenicSpotMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ScenicSpot::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        return routes.stream()
                .filter(route -> city.isEmpty() || city.equalsIgnoreCase(route.getCity()))
                .sorted(Comparator
                        .comparing((TravelRoute route) -> routeMatchScore(route, days, budget, interestText, scenicMap))
                        .reversed()
                        .thenComparing(route -> route.getBudget() == null ? BigDecimal.ZERO : route.getBudget()))
                .limit(3)
                .map(route -> toRecommendVO(route, scenicMap, request))
                .collect(Collectors.toList());
    }

    private int routeMatchScore(TravelRoute route, Integer days, BigDecimal budget, String interestText, Map<Long, ScenicSpot> scenicMap) {
        int score = 0;
        if (days != null && route.getDays() != null) {
            score += Math.max(0, 30 - Math.abs(route.getDays() - days) * 8);
        }
        if (budget != null && route.getBudget() != null) {
            BigDecimal gap = route.getBudget().subtract(budget).abs();
            score += Math.max(0, 30 - gap.divide(new BigDecimal("100"), 0, RoundingMode.DOWN).intValue());
        }
        if (route.getTheme() != null && !route.getTheme().isBlank() && !interestText.isBlank()) {
            String theme = route.getTheme().toLowerCase(Locale.ROOT);
            if (interestText.contains(theme)) {
                score += 20;
            }
            if (theme.contains("精华")) {
                score += 6;
            }
        }
        if (route.getRouteDesc() != null && !route.getRouteDesc().isBlank()) {
            score += 8;
        }
        score += scenicMatchScore(route, scenicMap, interestText);
        return score;
    }

    private int scenicMatchScore(TravelRoute route, Map<Long, ScenicSpot> scenicMap, String interestText) {
        if (interestText == null || interestText.isBlank()) {
            return 0;
        }
        return routeSpotMapper.selectByRouteId(route.getId()).stream()
                .map(RouteSpot::getScenicId)
                .map(scenicMap::get)
                .filter(Objects::nonNull)
                .mapToInt(scenic -> {
                    String text = joinLower(scenic.getScenicName(), scenic.getCategory(), scenic.getTags(), scenic.getDescription());
                    int score = 0;
                    for (String term : interestText.split("[,，/、\\s]+")) {
                        if (!term.isBlank() && text.contains(term.trim().toLowerCase(Locale.ROOT))) {
                            score += 10;
                        }
                    }
                    if (scenic.getPopularity() != null) {
                        score += Math.min(8, scenic.getPopularity() / 15);
                    }
                    if (scenic.getScore() != null) {
                        score += Math.max(0, scenic.getScore().multiply(new BigDecimal("2")).intValue() - 6);
                    }
                    return score;
                })
                .sum();
    }

    private RouteRecommendVO toRecommendVO(TravelRoute route, Map<Long, ScenicSpot> scenicMap, RouteRecommendRequest request) {
        List<RouteSpot> spots = routeSpotMapper.selectByRouteId(route.getId());
        List<String> scenicOrder = spots.stream()
                .map(RouteSpot::getScenicId)
                .map(scenicMap::get)
                .filter(Objects::nonNull)
                .map(ScenicSpot::getScenicName)
                .collect(Collectors.toList());

        List<String> highlights = buildHighlights(route, scenicOrder, request);
        Integer days = request.getDays();
        BigDecimal budget = request.getBudget();
        String interestText = request.getInterests() == null ? "" : String.join(",", request.getInterests()).toLowerCase(Locale.ROOT);
        int score = routeMatchScore(route, days, budget, interestText, scenicMap);
        BigDecimal budgetGap = budget == null || route.getBudget() == null ? BigDecimal.ZERO : route.getBudget().subtract(budget);

        RouteRecommendVO vo = new RouteRecommendVO();
        vo.setCity(route.getCity() == null ? "杭州" : route.getCity());
        vo.setDays(route.getDays() == null ? 3 : route.getDays());
        vo.setRouteName(route.getRouteName());
        vo.setEstimatedCost(route.getBudget() == null ? new BigDecimal("1288.00") : route.getBudget());
        vo.setHighlights(highlights);
        vo.setScenicOrder(scenicOrder);
        vo.setTheme(route.getTheme());
        vo.setRouteDesc(route.getRouteDesc());
        vo.setMatchScore(Math.min(99, Math.max(0, score)));
        vo.setBudgetGap(budgetGap);
        vo.setMatchReason(buildMatchReason(route, scenicOrder, request, budgetGap));
        return vo;
    }

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

    private String joinLower(String... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","))
                .toLowerCase(Locale.ROOT);
    }

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
