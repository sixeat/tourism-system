package com.tourism.dto;

/**
 * AI问答请求数据传输对象（DTO - Data Transfer Object）
 *
 * <p>DTO 模式说明：
 * DTO（数据传输对象）是一种设计模式，用于封装层与层之间传输的数据。
 * 它通常是一个纯JavaBean（只有字段、getter/setter，无业务逻辑），
 * 目的是将前端请求数据、外部接口数据与数据库实体（Entity）解耦，
 * 避免直接暴露数据库实体结构，提升系统安全性和灵活性。</p>
 *
 * <p>本类封装了用户向AI提问时所需的所有上下文参数，前端将这些问题信息以JSON格式
 * 提交到后端，Spring通过@RequestBody注解将其反序列化为本DTO对象。</p>
 *
 * <p>核心字段：
 * <ul>
 *   <li>question：用户直接输入的问题文本</li>
 *   <li>originCity：出发城市（用于规划交通）</li>
 *   <li>destinationCity：目的城市（旅行目的地）</li>
 *   <li>city：目标城市（冗余兼容字段，适配不同场景）</li>
 *   <li>budget：预算范围（字符串形式，如"3000-5000"）</li>
 *   <li>days：旅行天数（字符串形式，如"3-5"）</li>
 *   <li>interests：兴趣偏好（逗号分隔字符串，如"历史,美食,自然风光"）</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class AiAskRequest {

    private String question;
    // question：用户向AI提出的直接问题文本
    // 例如："我想去西安玩三天，有什么推荐？"、"北京到上海的交通方式有哪些？"

    private String originCity;
    // originCity：出发城市名称，用于AI规划交通路线或推荐行程起点
    // 例如："北京"、"上海"

    private String destinationCity;
    // destinationCity：目的城市名称，用户想去旅游的目标城市
    // 例如："西安"、"成都"

    private String city;
    // city：目标城市，与destinationCity功能类似，用于兼容不同前端场景的参数命名
    // 某些接口可能只传city而不传destinationCity，业务层可判断取值优先级

    private String budget;
    // budget：预算范围，以字符串形式存储，支持灵活表述
    // 例如："3000"（固定预算）、"3000-5000"（预算区间）、"不限"（无预算限制）
    // 使用String而非BigDecimal，是为了兼容前端灵活输入（如区间、文本描述）

    private String days;
    // days：旅行天数，以字符串形式存储，支持灵活表述
    // 例如："3"（固定天数）、"3-5"（天数范围）、"周末"（文本描述）
    // 使用String而非Integer，是为了兼容前端灵活输入

    private String interests;
    // interests：兴趣偏好，以逗号分隔的字符串形式存储
    // 例如："历史,美食,自然风光"、"摄影,徒步,古镇"
    // AI可据此推荐匹配用户兴趣的景点和活动

    public String getQuestion() {
        // 返回用户提问文本
        return question;
    }

    public void setQuestion(String question) {
        // 设置用户提问文本
        this.question = question;
    }

    public String getOriginCity() {
        // 返回出发城市
        return originCity;
    }

    public void setOriginCity(String originCity) {
        // 设置出发城市
        this.originCity = originCity;
    }

    public String getDestinationCity() {
        // 返回目的城市
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        // 设置目的城市
        this.destinationCity = destinationCity;
    }

    public String getCity() {
        // 返回目标城市（兼容字段）
        return city;
    }

    public void setCity(String city) {
        // 设置目标城市（兼容字段）
        this.city = city;
    }

    public String getBudget() {
        // 返回预算范围
        return budget;
    }

    public void setBudget(String budget) {
        // 设置预算范围
        this.budget = budget;
    }

    public String getDays() {
        // 返回旅行天数
        return days;
    }

    public void setDays(String days) {
        // 设置旅行天数
        this.days = days;
    }

    public String getInterests() {
        // 返回兴趣偏好
        return interests;
    }

    public void setInterests(String interests) {
        // 设置兴趣偏好
        this.interests = interests;
    }
}
