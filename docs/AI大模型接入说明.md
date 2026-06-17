# AI 大模型接入说明

## 1. 当前实现

系统已经实现 AI 旅游问答模块，接口为：

`POST /api/ai/ask`

问答流程：

1. 后端先从本系统景点、酒店、门票、路线和订单规则中构建本地知识库。
2. 根据用户问题进行 RAG 检索。
3. 如果外部大模型配置可用，则把检索资料交给模型生成回答。
4. 如果外部接口未启用或调用失败，自动回退到本地 RAG 回答。

因此，即使外部大模型接口不可用，系统演示也不会中断。

## 2. 配置位置

配置文件：

`src/main/resources/application.properties`

当前配置项：

```properties
ai.api.enabled=${AI_API_ENABLED:false}
ai.api.url=${AI_API_URL:https://api.deepseek.com/chat/completions}
ai.api.key=${AI_API_KEY:}
ai.api.model=${AI_API_MODEL:deepseek-v4-flash}
```

## 3. Tomcat 启动环境变量

如需启用外部大模型，可在 Tomcat 启动前设置：

```bat
set AI_API_ENABLED=true
set AI_API_URL=https://api.deepseek.com/chat/completions
set AI_API_MODEL=deepseek-v4-flash
set AI_API_KEY=你的API密钥
```

注意：不要把真实密钥写到 JSP 页面或前端 JavaScript 中。

## 4. DeepSeek 当前接入结果

当前已成功接入 DeepSeek OpenAI 兼容接口：

```properties
AI_API_ENABLED=true
AI_API_URL=https://api.deepseek.com/chat/completions
AI_API_MODEL=deepseek-v4-flash
AI_API_KEY=你的DeepSeek密钥
```

联调结果：

1. 直连 `https://api.deepseek.com/chat/completions` 返回 `200 OK`
2. 项目接口 `POST /api/ai/ask` 返回 `mode=API_RAG`
3. 页面会显示 `API + RAG 生成`

示例问题：

`北京三日路线怎么安排？`

系统会先检索本地路线资料，再把资料交给 DeepSeek 生成回答。

## 5. Kimi For Coding 当前测试结果

用户提供的 Kimi For Coding 网关：

`https://api.kimi.com/coding`

按 OpenAI Chat Completions 兼容路径测试：

`https://api.kimi.com/coding/v1/chat/completions`

当前直接从本项目后端调用返回：

`403 Forbidden`

说明该 Kimi For Coding 网关可能只允许特定 Coding Agent 环境或白名单来源调用，不能作为普通 Web 后端直接调用。

同时测试 Moonshot 通用 OpenAI 兼容地址：

1. `https://api.moonshot.cn/v1/chat/completions`
2. `https://api.moonshot.ai/v1/chat/completions`

使用该 Kimi For Coding 密钥返回：

`401 Unauthorized`

说明该密钥不是 Moonshot 通用 Chat Completions API 密钥。

## 6. 项目兜底策略

当外部接口失败时，`AiQaServiceImpl` 会：

1. 记录接口失败状态到 Tomcat 日志
2. 自动返回本地 RAG 答案
3. 前端显示模式为 `LOCAL_RAG`

当外部接口成功时，前端会显示：

`API + RAG 生成`

## 7. 推荐后续方案

如果需要真正接入线上大模型，建议准备以下任一方式：

1. 获取 Moonshot/Kimi 官方开放平台 Chat API Key
2. 获取可用于普通 HTTPS 后端调用的 OpenAI 兼容 API Key
3. 确认 Kimi For Coding 密钥是否支持服务器端 `chat/completions`
4. 如果 Kimi For Coding 需要白名单，需要在对应平台开启调用权限

拿到可用 Chat API Key 后，只需要修改 Tomcat 环境变量，无需改代码。
