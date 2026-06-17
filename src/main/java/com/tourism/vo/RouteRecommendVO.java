package com.tourism.vo;

import java.math.BigDecimal;
import java.util.List;

public class RouteRecommendVO {
    private String routeName;
    private String city;
    private Integer days;
    private BigDecimal estimatedCost;
    private List<String> highlights;
    private List<String> scenicOrder;
    private String theme;
    private String routeDesc;
    private Integer matchScore;
    private String matchReason;
    private BigDecimal budgetGap;

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }

    public List<String> getScenicOrder() {
        return scenicOrder;
    }

    public void setScenicOrder(List<String> scenicOrder) {
        this.scenicOrder = scenicOrder;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getRouteDesc() {
        return routeDesc;
    }

    public void setRouteDesc(String routeDesc) {
        this.routeDesc = routeDesc;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }

    public BigDecimal getBudgetGap() {
        return budgetGap;
    }

    public void setBudgetGap(BigDecimal budgetGap) {
        this.budgetGap = budgetGap;
    }
}
