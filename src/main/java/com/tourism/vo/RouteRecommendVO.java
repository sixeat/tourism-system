package com.tourism.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 路线推荐视图对象（VO - View Object）
 *
 * <p>VO 模式说明：
 * VO（视图对象）是专门为前端展示层设计的对象，用于封装控制器返回给前端的聚合数据。
 * 与Entity（数据库实体）的区别：Entity对应数据库表结构，VO可聚合多个Entity的字段、
 * 包含计算属性和格式化文本，不直接映射数据库表。本类包含的 matchScore、matchReason、
 * budgetGap 等字段均为推荐算法计算后的结果，数据库中不存在对应列。</p>
 *
 * <p>本类封装了智能路线推荐服务返回的推荐结果，包含路线基本信息、预估费用、匹配度、
 * 推荐理由等前端展示所需的全部字段。适用于"旅行规划"页面的推荐结果列表、
 * 路线卡片展示等场景。</p>
 *
 * <p>核心字段：
 * <ul>
 *   <li>routeName/city/days/estimatedCost：路线基本信息与预估费用</li>
 *   <li>highlights/scenicOrder/theme/routeDesc：路线亮点与详细描述</li>
 *   <li>matchScore/matchReason：匹配度与推荐理由（算法计算属性）</li>
 *   <li>budgetGap：预算差额（帮助用户判断预算是否充足）</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class RouteRecommendVO {

    private String routeName;
    // routeName：推荐路线的名称，如"西安古都三日游"、"成都美食五日行"
    // 前端展示为路线卡片的主标题，是用户识别路线的首要信息

    private String city;
    // city：路线所在城市或主要城市，如"西安"、"成都"
    // 用于前端筛选、分类展示，或作为路线卡片的副标题/标签

    private Integer days;
    // days：路线建议天数，如3、5、7
    // 前端展示为"3天2晚"、"5天4晚"，帮助用户快速了解行程时长
    // 推荐算法会优先匹配用户输入天数相近的路线

    private BigDecimal estimatedCost;
    // estimatedCost：路线预估总费用，java.math.BigDecimal 类型
    // 使用BigDecimal确保金额精度。包含酒店、门票、交通、餐饮等预估费用
    // 前端展示格式为：¥2,999起 或 ¥3,500/人
    // 推荐算法会将此值与用户预算对比，计算 budgetGap

    private List<String> highlights;
    // highlights：路线亮点列表，每条亮点是一个简短描述
    // 例如：["兵马俑深度讲解", "回民街美食探店", "城墙骑行体验"]
    // 前端展示为路线卡片下的标签列表或图文亮点区域，吸引用户关注
    // 使用List<String>支持多条亮点，增强信息丰富度

    private List<String> scenicOrder;
    // scenicOrder：景点游览顺序列表，按推荐游览顺序排列
    // 例如：["Day1: 兵马俑 -> 华清宫", "Day2: 大雁塔 -> 大唐不夜城", "Day3: 城墙 -> 钟鼓楼"]
    // 前端可据此渲染日程时间轴或地图路线，直观展示行程安排
    // 每个元素通常包含"天数+景点名称+箭头"的格式，便于前端直接渲染

    private String theme;
    // theme：路线主题分类，如"历史文化"、"美食探索"、"自然风光"、"亲子休闲"
    // 前端展示为路线卡片的主题标签，用户可据此快速筛选感兴趣的路线类型
    // 推荐算法会根据用户兴趣标签（interests）与theme的匹配度提升推荐优先级

    private String routeDesc;
    // routeDesc：路线详细描述，对行程的文字介绍
    // 例如："本路线带您深入体验西安古都魅力，第一天参观世界第八大奇迹兵马俑..."
    // 前端展示在路线详情页的介绍区域，帮助用户全面了解行程内容
    // 通常限制字数（如200字以内），过长时前端展示"展开/收起"功能

    private Integer matchScore;
    // matchScore：匹配度分数，推荐算法根据用户偏好与路线特征计算出的匹配分数
    // 通常为0-100的整数，分数越高表示越符合用户需求
    // 例如：用户兴趣为"历史+美食"，路线主题为"历史文化"且包含美食亮点，则matchScore较高
    // 前端展示为"匹配度：95%"、"非常符合您的需求"等文案，或按分数排序推荐列表
    // 此字段为计算属性，由推荐算法服务生成，不存储于数据库

    private String matchReason;
    // matchReason：匹配原因说明，向用户解释为什么推荐此路线
    // 例如："该路线包含您感兴趣的历史景点和美食体验，预算符合您的预期"
    // 前端展示在路线卡片下方或推荐详情页，增强推荐的可解释性和用户信任度
    // 由推荐算法根据匹配维度生成，属于计算属性，不存储于数据库

    private BigDecimal budgetGap;
    // budgetGap：预算差额，java.math.BigDecimal 类型，表示用户预算与路线预估费用的差值
    // 计算逻辑：budgetGap = userBudget - estimatedCost
    // - 正值（如 500）：表示预算充足，还可剩余500元
    // - 负值（如 -300）：表示超预算300元，需调整计划或选择其他路线
    // 前端可据此展示"预算内"、"超预算¥300"等提示，帮助用户做决策
    // 此字段为计算属性，由推荐算法服务生成，不存储于数据库

    public String getRouteName() {
        // 返回路线名称
        return routeName;
    }

    public void setRouteName(String routeName) {
        // 设置路线名称
        this.routeName = routeName;
    }

    public String getCity() {
        // 返回城市
        return city;
    }

    public void setCity(String city) {
        // 设置城市
        this.city = city;
    }

    public Integer getDays() {
        // 返回建议天数
        return days;
    }

    public void setDays(Integer days) {
        // 设置建议天数
        this.days = days;
    }

    public BigDecimal getEstimatedCost() {
        // 返回预估费用
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        // 设置预估费用
        this.estimatedCost = estimatedCost;
    }

    public List<String> getHighlights() {
        // 返回亮点列表
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        // 设置亮点列表
        this.highlights = highlights;
    }

    public List<String> getScenicOrder() {
        // 返回景点游览顺序列表
        return scenicOrder;
    }

    public void setScenicOrder(List<String> scenicOrder) {
        // 设置景点游览顺序列表
        this.scenicOrder = scenicOrder;
    }

    public String getTheme() {
        // 返回主题
        return theme;
    }

    public void setTheme(String theme) {
        // 设置主题
        this.theme = theme;
    }

    public String getRouteDesc() {
        // 返回路线描述
        return routeDesc;
    }

    public void setRouteDesc(String routeDesc) {
        // 设置路线描述
        this.routeDesc = routeDesc;
    }

    public Integer getMatchScore() {
        // 返回匹配度分数（计算属性）
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        // 设置匹配度分数（由推荐算法计算后注入）
        this.matchScore = matchScore;
    }

    public String getMatchReason() {
        // 返回匹配原因（计算属性）
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        // 设置匹配原因（由推荐算法生成后注入）
        this.matchReason = matchReason;
    }

    public BigDecimal getBudgetGap() {
        // 返回预算差额（计算属性）
        return budgetGap;
    }

    public void setBudgetGap(BigDecimal budgetGap) {
        // 设置预算差额（由推荐算法计算后注入）
        this.budgetGap = budgetGap;
    }
}
