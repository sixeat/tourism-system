package com.tourism.vo;

import java.util.List;

/**
 * AI问答回答视图对象（VO - View Object）
 *
 * <p>VO 模式说明：
 * VO（视图对象）是专门为前端展示而设计的对象，用于封装控制器返回给前端的数据。
 * 它与DTO（数据传输对象）和Entity（数据库实体）的区别：
 * <ul>
 *   <li>Entity：对应数据库表，包含ORM映射注解，用于持久化</li>
 *   <li>DTO：用于接收前端请求数据或层间传输，不直接对应数据库表</li>
 *   <li>VO：专门为前端视图层设计，可聚合多个来源的数据，包含前端展示所需的全部字段</li>
 * </ul></p>
 *
 * <p>本类封装了AI问答服务返回的完整回答信息，包括回答文本、参考来源、追问建议、回答模式等，
 * 供前端AI问答组件渲染使用（如聊天界面、推荐卡片等）。</p>
 *
 * <p>核心字段：
 * <ul>
 *   <li>answer：AI生成的回答文本（主内容）</li>
 *   <li>references：参考来源列表（如引用攻略、百科链接）</li>
 *   <li>suggestions：追问建议列表（引导用户继续提问）</li>
 *   <li>mode：回答模式（如chat、recommend、plan等）</li>
 * </ul></p>
 *
 * @author tourism-system
 * @version 1.0
 */
public class AiAnswerVO {

    private String answer;
    // answer：AI生成的回答文本，是返回给前端的核心内容
    // 例如："西安是十三朝古都，推荐游览兵马俑、大雁塔、城墙等景点。建议安排3-4天行程..."
    // 可能包含Markdown格式（如标题、列表、链接），前端需渲染为富文本

    private List<String> references;
    // references：参考来源列表，标识AI回答所依据的信息来源
    // 例如：["https://example.com/guide/xian", "《西安旅游官方攻略》"]
    // 前端可据此展示"参考来源"区域，增强回答可信度，用户可点击链接查看原文
    // 使用List<String>而非单个String，支持多来源引用

    private List<String> suggestions;
    // suggestions：追问建议列表，AI根据当前问题生成的后续提问建议
    // 例如：["西安有哪些美食推荐？", "去西安的最佳季节是什么时候？", "西安交通便利吗？"]
    // 前端可展示为"你可能还想问"的快捷按钮，降低用户输入成本，提升交互体验
    // 使用List<String>支持展示多个建议，通常限制3-5条

    private String mode;
    // mode：回答模式/类型，标识当前回答的类别，便于前端采用不同的渲染样式
    // 常见取值：
    // - "chat"：普通对话问答，使用常规聊天气泡样式
    // - "recommend"：推荐模式，使用卡片列表展示推荐结果（如景点、酒店卡片）
    // - "plan"：行程规划模式，使用时间表或日程卡片样式展示
    // - "compare"：对比模式，使用表格或对比卡片展示
    // 前端根据mode值切换不同的UI组件渲染回答内容

    public String getAnswer() {
        // 返回AI回答文本
        return answer;
    }

    public void setAnswer(String answer) {
        // 设置AI回答文本
        this.answer = answer;
    }

    public List<String> getReferences() {
        // 返回参考来源列表
        return references;
    }

    public void setReferences(List<String> references) {
        // 设置参考来源列表
        this.references = references;
    }

    public List<String> getSuggestions() {
        // 返回追问建议列表
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        // 设置追问建议列表
        this.suggestions = suggestions;
    }

    public String getMode() {
        // 返回回答模式
        return mode;
    }

    public void setMode(String mode) {
        // 设置回答模式
        this.mode = mode;
    }
}
