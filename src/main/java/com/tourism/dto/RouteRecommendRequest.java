package com.tourism.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 路线推荐请求数据传输对象（DTO - Data Transfer Object）
 *
 * <p>DTO 模式说明：
 * DTO（数据传输对象）用于封装前端提交到后端的数据，实现接口层与持久层的解耦。
 * 本类封装了用户请求智能路线推荐时提交的偏好参数，前端以JSON格式提交，
 * 后端通过@RequestBody反序列化为本DTO，传入推荐算法服务进行路线匹配。</p>
 *
 * <p>核心字段：
 * <ul>
 *   <li>originCity：出发城市（规划交通起点）</li>
 *   <li>destinationCity：目的城市（旅行目的地）</li>
 *   <li>city：目标城市（兼容字段，适配不同前端场景）</li>
 *   <li>budget：预算金额（BigDecimal，精确数值计算）</li>
 *   <li>days：旅行天数（Integer，精确数值）</li>
 *   <li>interests：兴趣标签列表（List<String>，支持多兴趣匹配）</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class RouteRecommendRequest {

    private String originCity;
    // originCity：出发城市名称，用于推荐服务考虑交通方案或规划行程起点
    // 例如："北京"、"上海"。可为null，表示不限制出发城市

    private String destinationCity;
    // destinationCity：目的城市名称，用户希望前往旅游的城市
    // 例如："西安"、"成都"。推荐算法会优先筛选途经或位于该城市的路线

    private String city;
    // city：目标城市，与destinationCity功能类似，用于兼容不同前端场景的参数命名
    // 若前端传了city而未传destinationCity，业务层可优先使用city字段
    // 这种冗余设计提升了接口兼容性，避免前端因字段名不一致而报错

    private BigDecimal budget;
    // budget：预算金额，java.math.BigDecimal 类型
    // 使用BigDecimal而非Double/Float，是因为BigDecimal可精确表示小数，避免浮点精度误差
    // 例如：3000.00 表示三千元预算。推荐算法会筛选 estimated_cost <= budget 的路线
    // 若 budget 为 null，表示不限制预算

    private Integer days;
    // days：旅行天数，Integer 类型
    // 例如：5 表示计划旅行5天。推荐算法会筛选 route.days <= 用户天数 或相近天数的路线
    // 若 days 为 null，表示不限制天数

    private List<String> interests;
    // interests：兴趣标签列表，List<String> 类型
    // 例如：["历史", "美食", "自然风光", "摄影"]
    // 推荐算法会计算路线的景点标签与 interests 的重合度，重合度越高匹配分数越高
    // 使用List而非逗号分隔字符串，便于前端直接传数组，Spring自动解析JSON数组

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

    public BigDecimal getBudget() {
        // 返回预算金额
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        // 设置预算金额
        this.budget = budget;
    }

    public Integer getDays() {
        // 返回旅行天数
        return days;
    }

    public void setDays(Integer days) {
        // 设置旅行天数
        this.days = days;
    }

    public List<String> getInterests() {
        // 返回兴趣标签列表
        return interests;
    }

    public void setInterests(List<String> interests) {
        // 设置兴趣标签列表
        this.interests = interests;
    }
}
