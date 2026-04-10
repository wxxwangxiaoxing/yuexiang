

# AI 探店助手 — 后端接口设计（修订版 v2.0）

## 页面交互分析

```plain
┌────────────────────────────────────────────────────────┐
│  🤖 AI探店助手                                         │
│                                                        │
│  ┌──────────────────┐                                  │
│  │ 🤖 欢迎语         │               ── ① 初始化会话    │
│  └──────────────────┘                                  │
│                                                        │
│  [附近好吃的] [人均50聚餐] [安静约会]   ── ② 热门问题    │
│                                                        │
│           ┌──────────────┐                             │
│           │ 用户输入消息   │            ── ③ 发送消息    │
│           └──────────────┘                             │
│                                                        │
│  ┌──────────────────────────┐                          │
│  │ 🤖 推荐 3 家店铺           │                          │
│  │   ⭐评分 📍距离 💰价格      │         ── ④ AI推荐结果  │
│  │   💡推荐理由 (基于笔记分析)  │                          │
│  │   [查看详情▶]              │         ── ⑤ 嵌入式跳转  │
│  │                          │                          │
│  │ 💡 要不要看看优惠券？       │         ── ⑥ 主动追问    │
│  └──────────────────────────┘                          │
│                                                        │
│  ┌──────────────────────────┐                          │
│  │ 🤖 展示优惠券卡片          │                          │
│  │   🎫 [立即领取] [限时秒杀]  │         ── ⑦ 券信息+领取 │
│  └──────────────────────────┘                          │
│                                                        │
│  [ 输入框 ]                  🎤 发送    ── ⑧ 多轮对话    │
└────────────────────────────────────────────────────────┘
```

---

## 数据库变更清单

> 以下 DDL 需在原 `yuexiang` 库基础上执行，开发前必须先完成。

```sql
-- ① tb_ai_message 新增卡片快照字段（修复 #4：历史消息卡片丢失）
ALTER TABLE `tb_ai_message`
  ADD COLUMN `cards_data` JSON DEFAULT NULL
  COMMENT '卡片完整数据快照(JSON数组,含shop_card/voucher_card/blog_card)';

-- ② tb_shop 新增空间索引辅助列（修复 #6：距离查询性能）
ALTER TABLE `tb_shop`
  ADD INDEX `idx_shop_geo` (`longitude`, `latitude`);

-- ③ 预置菜系标签数据（修复 #1：菜系搜索依赖标签体系）
-- 确保 tb_tag 中 type=0（商户标签）包含常见菜系
INSERT IGNORE INTO `tb_tag` (`name`, `type`, `sort`) VALUES
  ('川菜',   0, 100), ('火锅',   0, 99), ('日料',   0, 98),
  ('西餐',   0, 97),  ('粤菜',   0, 96), ('烧烤',   0, 95),
  ('湘菜',   0, 94),  ('东北菜', 0, 93), ('韩餐',   0, 92),
  ('海鲜',   0, 91),  ('小吃',   0, 90), ('咖啡',   0, 89),
  ('甜品',   0, 88),  ('自助餐', 0, 87), ('素食',   0, 86);

-- ④ 预置 AI 相关系统配置（修复 #11：频率限制配置化）
INSERT IGNORE INTO `tb_sys_config`
  (`config_key`, `config_value`, `config_type`, `group_name`, `description`) VALUES
  ('ai.chat.rate_limit.per_minute',    '10',    'NUMBER',  'ai', '每人每分钟最大消息数'),
  ('ai.chat.rate_limit.per_day',       '200',   'NUMBER',  'ai', '每人每日最大消息数'),
  ('ai.chat.max_tokens_per_session',   '8000',  'NUMBER',  'ai', '单会话最大Token数'),
  ('ai.chat.context_summary_threshold','6000',  'NUMBER',  'ai', '触发上下文摘要的Token阈值'),
  ('ai.chat.max_history_turns',        '20',    'NUMBER',  'ai', '保留最近对话轮次'),
  ('ai.chat.search_radius_default',    '3000',  'NUMBER',  'ai', '默认搜索半径(米)'),
  ('ai.chat.hot_questions', '[{"id":1,"text":"附近有啥好吃的","icon":"🍜"},{"id":2,"text":"人均50聚餐推荐","icon":"💰"},{"id":3,"text":"安静约会餐厅","icon":"🌹"},{"id":4,"text":"今日秒杀推荐","icon":"⏰"},{"id":5,"text":"适合带娃的餐厅","icon":"👶"},{"id":6,"text":"深夜食堂推荐","icon":"🌙"}]', 'JSON', 'ai', 'AI助手热门问题列表');
```

---

## 错误码体系

> 所有接口（含 SSE `event: error`）统一使用以下错误码。

| 错误码               | HTTP 状态码 | 说明                       | 前端处理建议             |
| -------------------- | ----------- | -------------------------- | ------------------------ |
| `SUCCESS`            | 200         | 成功                       | 正常处理                 |
| `PARAM_INVALID`      | 400         | 参数校验失败               | 提示修改                 |
| `UNAUTHORIZED`       | 401         | 未登录或 Token 过期        | 跳登录页                 |
| `SESSION_NOT_FOUND`  | 404         | 会话不存在                 | 引导创建新会话           |
| `SESSION_EXPIRED`    | 410         | 会话已过期（Redis TTL）    | 引导创建新会话           |
| `RATE_LIMITED`       | 429         | 频率限制（分钟/日级）      | 显示倒计时               |
| `SENSITIVE_CONTENT`  | 451         | 消息包含违规内容（拦截级） | 提示修改后重试           |
| `MESSAGE_PROCESSING` | 409         | 上一条消息仍在处理中       | 禁用发送按钮，等待       |
| `NO_RESULTS`         | 200         | 未找到匹配商户（非错误）   | 显示 AI 引导追问         |
| `TOKEN_EXCEEDED`     | 403         | 单会话 Token 超限          | 引导创建新会话           |
| `LLM_UNAVAILABLE`    | 503         | AI 服务不可用              | 展示降级推荐 + 提示稍后  |
| `SERVER_ERROR`       | 500         | 服务器内部错误             | 通用错误提示             |

标准错误响应格式（REST 接口）：

```json
{
  "code": 429,
  "msg": "请求过于频繁，请1分钟后再试",
  "errorCode": "RATE_LIMITED",
  "data": null
}
```

SSE 错误事件格式：

```
event: error
data: {"errorCode": "SENSITIVE_CONTENT", "msg": "消息包含违规内容，请修改后重试"}
```

---

## 接口总览

| #    | 方法   | 路径                               | 说明                       | 登录     |
| ---- | ------ | ---------------------------------- | -------------------------- | -------- |
| 1    | POST   | `/api/ai/chat/session`             | 创建会话                   | **必须** |
| 2    | GET    | `/api/ai/chat/hot-questions`       | 热门问题列表               | 可选     |
| 3    | POST   | `/api/ai/chat/send`                | 发送消息（SSE 流式）       | **必须** |
| 4    | GET    | `/api/ai/chat/history/{sessionId}` | 会话历史                   | **必须** |
| 5    | DELETE | `/api/ai/chat/session/{sessionId}` | 删除会话                   | **必须** |
| 6    | GET    | `/api/ai/chat/sessions`            | 会话列表（分页）           | **必须** |

---

## 接口 1 — 创建会话

### 基本信息

```plain
POST /api/ai/chat/session
Content-Type: application/json
Authorization: Bearer <token>
```

### 请求体

```json
{
  "longitude": 116.4812,
  "latitude": 39.9961
}
```

### 参数说明

| 字段        | 类型   | 必填 | 说明                 |
| ----------- | ------ | ---- | -------------------- |
| `longitude` | Double | 否   | 经度（用于附近推荐） |
| `latitude`  | Double | 否   | 纬度                 |

### 响应

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "sessionId": "s_20260321_a8f3b2c1",
    "greeting": "你好！我是AI探店助手 🤖 告诉我你想吃什么，我来帮你找到最合适的店～",
    "createdTime": "2026-03-21T14:00:00"
  }
}
```

### 业务流程

```plain
客户端 POST /api/ai/chat/session
          │
          ▼
┌──────────────────────────────────┐
│ ① 生成唯一会话ID                  │
│    s_{date}_{uuid8}              │
└──────────────┬───────────────────┘
               │
               ▼
┌──────────────────────────────────┐
│ ② 初始化会话上下文 → Redis         │
│                                  │
│ Key: chat:session:{sessionId}    │
│ TTL: 24小时                       │
│                                  │
│ Value (Hash):                    │
│ {                                │
│   userId: 8001,                  │
│   longitude: 116.4812,           │
│   latitude: 39.9961,             │
│   messages: [],                  │  ← 对话历史(LLM上下文)
│   extractedEntities: {},         │  ← 累积提取的实体
│   totalTokens: 0,                │  ← 累计Token用量
│   turnCount: 0,                  │  ← 轮次计数
│   createdTime: ...               │
│ }                                │
└──────────────┬───────────────────┘
               │
               ▼
┌──────────────────────────────────┐
│ ③ 注入系统 Prompt (不返回)         │
│    定义 AI 角色与能力边界           │
└──────────────┬───────────────────┘
               │
               ▼
┌──────────────────────────────────┐
│ ④ 持久化到 MySQL                  │
│    INSERT tb_ai_session          │
│    INSERT tb_ai_message          │
│    (role=assistant, 欢迎语)       │
└──────────────────────────────────┘
```

### System Prompt 模板

```
你是「悦享生活」APP 的 AI 探店助手。

【核心能力】
1. 根据用户的口味偏好、预算、位置、场景，推荐合适的商户
2. 基于真实的探店笔记和评价数据提供推荐理由
3. 查询并展示商户的优惠券信息
4. 搜索相关探店笔记供用户参考
5. 进行自然的多轮对话，主动追问细化需求

【行为规范】
1. 只推荐数据库中真实存在的商户，不编造
2. 推荐理由必须基于真实笔记和评价数据
3. 价格信息使用"人均XX元"格式（数据库存分，需转换）
4. 距离信息基于用户坐标或指定地点计算
5. 每次推荐 2~5 家商户
6. 回答简洁友好，适当使用 emoji
7. 主动询问是否需要查看优惠券、更多详情等
8. 非探店相关问题，礼貌引导回探店话题

【输出格式】
使用指定 JSON 结构返回，包含 text(文本) + cards(结构化卡片)

【实体修正规则】
当用户说"更便宜的""换个近一点的"时，
保留已有实体并调整对应字段，不重置整个查询。

【地点处理规则】
当用户提到具体地名（如"望京""国贸"）时，
调用 search_shops 函数并传入 location 字段，
由后端负责地理编码转坐标。
```

---

## 接口 2 — 热门问题列表

### 基本信息

```plain
GET /api/ai/chat/hot-questions
Authorization: Bearer <token>          # 可选
```

### 请求参数（Query）

| 参数   | 类型    | 必填 | 默认 | 说明     |
| ------ | ------- | ---- | ---- | -------- |
| `size` | Integer | 否   | 6    | 返回条数 |

### 响应

```json
{
  "code": 200,
  "msg": "ok",
  "data": [
    { "id": 1, "text": "附近有啥好吃的",     "icon": "🍜" },
    { "id": 2, "text": "人均50聚餐推荐",     "icon": "💰" },
    { "id": 3, "text": "安静约会餐厅",       "icon": "🌹" },
    { "id": 4, "text": "今日秒杀推荐",       "icon": "⏰" },
    { "id": 5, "text": "适合带娃的餐厅",     "icon": "👶" },
    { "id": 6, "text": "深夜食堂推荐",       "icon": "🌙" }
  ]
}
```

### 数据来源

```sql
-- 优先从系统配置表获取（运营可配置）
SELECT config_value FROM tb_sys_config
WHERE config_key = 'ai.chat.hot_questions';

-- 如配置为空，降级从热搜词动态生成
SELECT keyword, search_count FROM tb_hot_search
WHERE status = 1
ORDER BY is_manual DESC, sort DESC, search_count DESC
LIMIT #{size};
```

---

## 接口 3 — 发送消息（核心接口）

**SSE 流式响应**，逐步返回 AI 回复，支持富文本卡片。

### 基本信息

```plain
POST /api/ai/chat/send
Content-Type: application/json
Accept: text/event-stream
Authorization: Bearer <token>
```

### 请求体

```json
{
  "sessionId": "s_20260321_a8f3b2c1",
  "message": "我想吃人均80的川菜，望京附近",
  "retryMessageId": null
}
```

### 参数说明

| 字段             | 类型   | 必填 | 校验规则                             |
| ---------------- | ------ | ---- | ------------------------------------ |
| `sessionId`      | String | 是   | 会话必须存在且属于当前用户           |
| `message`        | String | 是   | 1~500 字符                           |
| `retryMessageId` | String | 否   | 断线重连恢复用，传入上次未完成的消息ID |

### SSE 事件类型定义

| 事件类型       | 说明           | 前端处理             |
| -------------- | -------------- | -------------------- |
| `thinking`     | 思考中状态     | 显示加载提示动画     |
| `text_chunk`   | 文本片段       | 逐字追加显示         |
| `shop_card`    | 商户推荐卡片   | 渲染商户卡片组件     |
| `voucher_card` | 优惠券卡片     | 渲染优惠券卡片组件   |
| `blog_card`    | 笔记推荐卡片   | 渲染笔记卡片组件     |
| `error`        | 错误信息       | 显示错误提示         |
| `done`         | 完成           | 结束加载状态         |

### SSE 流式响应示例（SHOP_RECOMMEND 场景）

```plain
event: thinking
data: {"status": "checking", "msg": "正在理解你的需求..."}

event: thinking
data: {"status": "searching", "msg": "正在搜索望京附近川菜馆..."}

event: thinking
data: {"status": "analyzing_reviews", "msg": "正在分析326篇探店笔记..."}

event: text_chunk
data: {"content": "为你找到"}

event: text_chunk
data: {"content": "3家"}

event: text_chunk
data: {"content": "望京附近的川菜推荐：\n\n"}

event: shop_card
data: {
  "type": "shop_card",
  "shop": {
    "id": 5001,
    "name": "川味小馆",
    "typeName": "美食",
    "score": 4.8,
    "distance": 350,
    "distanceText": "350m",
    "avgPrice": 8500,
    "avgPriceText": "人均85元",
    "coverImage": "/imgs/shop/5001/cover.jpg",
    "reason": "326篇笔记中好评率95%，招牌水煮鱼被提及186次",
    "blogCount": 326,
    "positiveRate": 95,
    "hotDish": "水煮鱼",
    "tags": ["川菜", "网红打卡"]
  }
}

event: shop_card
data: {
  "type": "shop_card",
  "shop": {
    "id": 5015,
    "name": "蜀香园",
    "typeName": "美食",
    "score": 4.6,
    "distance": 600,
    "distanceText": "600m",
    "avgPrice": 7500,
    "avgPriceText": "人均75元",
    "coverImage": "/imgs/shop/5015/cover.jpg",
    "reason": "性价比高，回头客多，毛血旺是招牌",
    "blogCount": 189,
    "positiveRate": 91,
    "hotDish": "毛血旺",
    "tags": ["川菜", "人均50以下"]
  }
}

event: shop_card
data: {
  "type": "shop_card",
  "shop": {
    "id": 5023,
    "name": "麻辣空间",
    "typeName": "美食",
    "score": 4.5,
    "distance": 850,
    "distanceText": "850m",
    "avgPrice": 8000,
    "avgPriceText": "人均80元",
    "coverImage": "/imgs/shop/5023/cover.jpg",
    "reason": "环境新潮，适合朋友聚餐拍照",
    "blogCount": 142,
    "positiveRate": 89,
    "hotDish": "香辣蟹",
    "tags": ["川菜", "适合聚餐"]
  }
}

event: text_chunk
data: {"content": "\n\n💡 要不要我帮你看看这几家店的优惠券？"}

event: done
data: {"messageId": "msg_001", "totalTokens": 580}
```

### SSE 流式响应示例（VOUCHER_QUERY 场景）

```plain
event: thinking
data: {"status": "searching", "msg": "正在查询川味小馆的优惠活动..."}

event: text_chunk
data: {"content": "川味小馆目前有2张券：\n\n"}

event: voucher_card
data: {
  "type": "voucher_card",
  "voucher": {
    "id": 2001,
    "title": "满100减20",
    "payValue": 0,
    "payValueText": "免费领取",
    "actualValue": 2000,
    "actualValueText": "抵扣20元",
    "shopId": 5001,
    "shopName": "川味小馆",
    "voucherType": 0,
    "voucherTypeText": "普通券",
    "validEndTime": "2026-04-30T23:59:59",
    "actionText": "立即领取",
    "actionType": "GRAB",
    "actionUrl": "/pages/voucher/detail?id=2001"
  }
}

event: voucher_card
data: {
  "type": "voucher_card",
  "voucher": {
    "id": 2005,
    "title": "双人套餐",
    "payValue": 12800,
    "payValueText": "128元",
    "actualValue": 19800,
    "actualValueText": "抵扣198元",
    "shopId": 5001,
    "shopName": "川味小馆",
    "voucherType": 1,
    "voucherTypeText": "秒杀券",
    "stock": 38,
    "beginTime": "2026-03-21T10:00:00",
    "endTime": "2026-03-21T22:00:00",
    "actionText": "限时秒杀",
    "actionType": "SECKILL",
    "actionUrl": "/pages/voucher/seckill?id=2005"
  }
}

event: text_chunk
data: {"content": "\n\n满100减20是免费领的，双人套餐现在秒杀只要128元（原价198元），还剩38张～要不要下手？😋"}

event: done
data: {"messageId": "msg_003", "totalTokens": 320}
```

### SSE 流式响应示例（BLOG_RECOMMEND 场景）

```plain
event: thinking
data: {"status": "searching", "msg": "正在搜索川味小馆的探店笔记..."}

event: text_chunk
data: {"content": "找到几篇热门探店笔记，供你参考：\n\n"}

event: blog_card
data: {
  "type": "blog_card",
  "blog": {
    "id": 8001,
    "title": "望京最好吃的川菜！水煮鱼绝了🔥",
    "coverImage": "/imgs/blog/8001/0.jpg",
    "userId": 1023,
    "userName": "美食达人小王",
    "userAvatar": "/imgs/user/avatar_1023.jpg",
    "likeCount": 326,
    "commentCount": 45,
    "shopId": 5001,
    "shopName": "川味小馆"
  }
}

event: blog_card
data: {
  "type": "blog_card",
  "blog": {
    "id": 8045,
    "title": "川味小馆隐藏菜单，本地人才知道",
    "coverImage": "/imgs/blog/8045/0.jpg",
    "userId": 2056,
    "userName": "吃货日记",
    "userAvatar": "/imgs/user/avatar_2056.jpg",
    "likeCount": 189,
    "commentCount": 23,
    "shopId": 5001,
    "shopName": "川味小馆"
  }
}

event: text_chunk
data: {"content": "\n\n还想了解什么？可以问我某道菜的评价、营业时间等～"}

event: done
data: {"messageId": "msg_004", "totalTokens": 280}
```

---

### 核心业务流程

```plain
客户端 POST /api/ai/chat/send
  │  { sessionId, message, retryMessageId? }
  │
  ▼
┌─────────────────────────────────────────────────────────┐
│ ① 前置校验链                                             │
│                                                         │
│ ┌─ 1a. 断线恢复判断 ─────────────────────────────────┐   │
│ │  if retryMessageId != null:                        │   │
│ │    从 Redis/MySQL 查找该消息                         │   │
│ │    ├─ 已完整生成 → 直接重推完整 SSE 事件流 → return  │   │
│ │    └─ 生成中     → 返回 error: MESSAGE_PROCESSING   │   │
│ └────────────────────────────────────────────────────┘   │
│                                                         │
│ ┌─ 1b. 会话校验 ─────────────────────────────────────┐   │
│ │  · 校验 sessionId 存在且属于当前用户                  │   │
│ │  · 不存在 → SESSION_NOT_FOUND                       │   │
│ │  · Redis 已过期 → SESSION_EXPIRED                   │   │
│ └────────────────────────────────────────────────────┘   │
│                                                         │
│ ┌─ 1c. 会话级分布式锁 ──────────────────────────────┐    │
│ │  Key: lock:chat:session:{sessionId}               │    │
│ │  TTL: 60s                                         │    │
│ │  获取失败 → error: MESSAGE_PROCESSING              │    │
│ └────────────────────────────────────────────────────┘   │
│                                                         │
│ ┌─ 1d. 频率限制（配置化） ───────────────────────────┐    │
│ │  读取 tb_sys_config:                               │    │
│ │    ai.chat.rate_limit.per_minute → 10              │    │
│ │    ai.chat.rate_limit.per_day    → 200             │    │
│ │  Redis 计数器:                                     │    │
│ │    rate:ai:min:{userId}  INCR + EXPIRE 60s         │    │
│ │    rate:ai:day:{userId}  INCR + EXPIRE 86400s      │    │
│ │  超限 → error: RATE_LIMITED                        │    │
│ └────────────────────────────────────────────────────┘   │
│                                                         │
│ ┌─ 1e. Token 额度检查 ──────────────────────────────┐    │
│ │  读取 session.totalTokens                          │    │
│ │  > ai.chat.max_tokens_per_session → TOKEN_EXCEEDED │    │
│ └────────────────────────────────────────────────────┘   │
│                                                         │
│ ┌─ 1f. 敏感词检测 ──────────────────────────────────┐    │
│ │  加载 tb_sensitive_word (status=1) → 构建过滤器      │    │
│ │  (DFA / Aho-Corasick，启动时加载，变更时刷新)        │    │
│ │                                                    │    │
│ │  扫描 message:                                     │    │
│ │  ├─ 命中 level=3（拦截）                            │    │
│ │  │   → error: SENSITIVE_CONTENT                    │    │
│ │  │   → 记录 tb_operation_log                       │    │
│ │  ├─ 命中 level=2（替换）                            │    │
│ │  │   → message 中对应文本替换为 replace_to          │    │
│ │  │   → 继续后续流程                                 │    │
│ │  └─ 命中 level=1（提醒）                            │    │
│ │      → 记录日志，继续后续流程                        │    │
│ └────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│ ② 意图识别 + 实体提取 (LLM Function Calling)             │
│                                                         │
│ 发送给 LLM:                                              │
│ · system prompt                                         │
│ · 历史消息（上下文窗口管理后）                              │
│ · 当前 message                                           │
│ · functions 定义                                         │
│                                                         │
│ SSE 推送: event: thinking {"status":"understanding"}     │
│                                                         │
│ LLM 返回 function_call 或 直接文本回复                    │
│ ┌─────────────────────────────────────────────────────┐  │
│ │ function_call: search_shops({                       │  │
│ │   cuisine: "川菜",                                   │  │
│ │   budget_max: 8000,                                 │  │
│ │   location: "望京",                                  │  │
│ │   occasion: null                                    │  │
│ │ })                                                  │  │
│ └─────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│ ③ 意图路由 → 执行对应数据检索                              │
│                                                         │
│ ┌───────────────────┬─────────────────────────────────┐ │
│ │ function 名称      │ 路由到的处理流程                  │ │
│ ├───────────────────┼─────────────────────────────────┤ │
│ │ search_shops      │ → ④ 商户推荐流程                 │ │
│ │ query_vouchers    │ → ⑤ 优惠券查询流程               │ │
│ │ get_shop_detail   │ → ⑥ 商户详情查询                 │ │
│ │ search_blogs      │ → ⑦ 笔记搜索流程                 │ │
│ │ (无 function_call) │ → ⑧ 直接流式输出文本             │ │
│ └───────────────────┴─────────────────────────────────┘ │
└──────────────────────────┬──────────────────────────────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
         search_shops  query_vouchers  ...
              │            │
              ▼            ▼
         步骤 ④         步骤 ⑤
```

---

### ④ 商户推荐流程（search_shops）

```plain
┌─────────────────────────────────────────────────────────┐
│ ④-A 地理编码解析                                         │
│                                                         │
│ 输入: location = "望京"                                   │
│                                                         │
│ 优先级:                                                  │
│ ┌─ P1: 查 tb_shop 按 area 聚合获取区域中心坐标 ─────────┐ │
│ │                                                     │ │
│ │  SELECT AVG(longitude) AS lng, AVG(latitude) AS lat │ │
│ │  FROM tb_shop                                       │ │
│ │  WHERE area LIKE '%望京%'                            │ │
│ │    AND deleted = 0                                  │ │
│ │    AND longitude IS NOT NULL                        │ │
│ │                                                     │ │
│ │  结果: lng=116.4812, lat=40.0020                    │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                         │
│ ┌─ P2: 查 tb_region 匹配地区名称 ───────────────────────┐ │
│ │  SELECT * FROM tb_region                             │ │
│ │  WHERE name LIKE '%望京%' AND status = 1             │ │
│ │  (需预置区域中心坐标，或配合外部API)                     │ │
│ └──────────────────────────────────────────────────────┘ │
│                                                         │
│ ┌─ P3: 外部地理编码 API (兜底) ─────────────────────────┐ │
│ │  高德/百度地理编码: "望京" → (116.4812, 40.0020)       │ │
│ │  结果缓存: Redis geo:code:望京 TTL=7d                 │ │
│ └──────────────────────────────────────────────────────┘ │
│                                                         │
│ ┌─ P4: 无地点信息 → 使用会话创建时的用户定位坐标 ──────────┐ │
│ │  session.longitude, session.latitude                  │ │
│ └──────────────────────────────────────────────────────┘ │
│                                                         │
│ 最终得到搜索中心: searchLng, searchLat                     │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│ ④-B 数据库检索匹配商户（标签体系匹配菜系）                   │
│                                                         │
│ SSE 推送: event: thinking {"status":"searching",         │
│           "msg":"正在搜索望京附近川菜馆..."}                │
│                                                         │
│ -- 计算矩形边界框（先粗筛再精算，利用索引）                  │
│ -- Java 侧预计算:                                        │
│ --   minLng = searchLng - deltaLng(3000m)                │
│ --   maxLng = searchLng + deltaLng(3000m)                │
│ --   minLat = searchLat - deltaLat(3000m)                │
│ --   maxLat = searchLat + deltaLat(3000m)                │
│                                                         │
│ SELECT s.*, st.name AS type_name,                       │
│   ST_Distance_Sphere(                                   │
│     POINT(s.longitude, s.latitude),                     │
│     POINT(#{searchLng}, #{searchLat})                   │
│   ) AS distance                                         │
│ FROM tb_shop s                                          │
│ JOIN tb_shop_type st ON st.id = s.type_id               │
│ JOIN tb_tag_relation tr                                  │
│   ON tr.biz_type = 1 AND tr.biz_id = s.id              │
│ JOIN tb_tag t                                            │
│   ON t.id = tr.tag_id AND t.type = 0                    │
│ WHERE s.deleted = 0                                     │
│   AND t.name = '川菜'                 ← 标签精确匹配     │
│   AND s.avg_price <= #{budgetMax}     ← 预算过滤        │
│   AND s.score >= 3.5                  ← 基础质量线      │
│   AND s.longitude BETWEEN #{minLng} AND #{maxLng}       │
│   AND s.latitude  BETWEEN #{minLat} AND #{maxLat}       │
│ HAVING distance <= #{radius}          ← 精确距离过滤     │
│ ORDER BY s.score DESC, distance ASC                     │
│ LIMIT 10                              ← 候选池          │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│ ④-C 笔记 + 评价数据分析（为每个候选商户）                    │
│                                                         │
│ SSE 推送: event: thinking {"status":"analyzing_reviews", │
│           "msg":"正在分析326篇探店笔记..."}                │
│                                                         │
│ -- 笔记统计                                              │
│ SELECT shop_id,                                         │
│   COUNT(*) AS blog_count,                               │
│   SUM(CASE WHEN like_count >= 50                        │
│            THEN 1 ELSE 0 END) AS hot_blog_count         │
│ FROM tb_blog                                            │
│ WHERE shop_id IN (#{candidateIds})                      │
│   AND status = 1 AND deleted = 0                        │
│ GROUP BY shop_id;                                       │
│                                                         │
│ -- 评价统计                                              │
│ SELECT shop_id,                                         │
│   COUNT(*) AS review_count,                             │
│   AVG(score) AS avg_score,                              │
│   SUM(CASE WHEN score >= 4                              │
│            THEN 1 ELSE 0 END) * 100.0                   │
│     / COUNT(*) AS positive_rate                         │
│ FROM tb_review                                          │
│ WHERE shop_id IN (#{candidateIds})                      │
│   AND status = 1 AND deleted = 0                        │
│ GROUP BY shop_id;                                       │
│                                                         │
│ -- 商户关联标签                                           │
│ SELECT tr.biz_id AS shop_id, t.name AS tag_name         │
│ FROM tb_tag_relation tr                                  │
│ JOIN tb_tag t ON t.id = tr.tag_id                        │
│ WHERE tr.biz_type = 1                                   │
│   AND tr.biz_id IN (#{candidateIds});                   │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│ ④-D 高频菜品 / 关键词提取                                 │
│                                                         │
│ -- 从笔记正文中提取高频提及的菜品                           │
│ SELECT content FROM tb_blog                             │
│ WHERE shop_id = #{shopId}                               │
│   AND status = 1 AND deleted = 0                        │
│ ORDER BY like_count DESC                                │
│ LIMIT 20;                                               │
│                                                         │
│ → 策略 A（推荐）: 使用预计算标签热度                       │
│   SELECT t.name, t.hot FROM tb_tag t                    │
│   JOIN tb_tag_relation tr ON tr.tag_id = t.id           │
│   WHERE tr.biz_type = 1 AND tr.biz_id = #{shopId}      │
│   ORDER BY t.hot DESC LIMIT 5;                          │
│                                                         │
│ → 策略 B: 笔记内容送入 LLM 提取高频菜品名                  │
│   (仅在策略A数据不足时使用，控制Token消耗)                  │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│ ④-E 综合排序 → 选出 TOP 3                                │
│                                                         │
│ 排序公式:                                                │
│ finalScore =                                            │
│     score          × 0.30    评分权重                    │
│   + positiveRate   × 0.25    好评率权重                   │
│   + blogHeat       × 0.20    笔记热度(归一化)             │
│   + distanceScore  × 0.15    距离分(近=高, 归一化)        │
│   + priceMatch     × 0.10    价格匹配度                   │
│                                                         │
│ → 取 TOP 3（若不足3家，降低过滤条件重查或返回实际数量）       │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│ ④-F 生成推荐理由 (LLM)                                   │
│                                                         │
│ 将检索到的结构化数据回注 LLM (role: function):             │
│                                                         │
│ {                                                       │
│   "role": "function",                                   │
│   "name": "search_shops",                               │
│   "content": "[                                         │
│     {                                                   │
│       \"id\": 5001,                                     │
│       \"name\": \"川味小馆\",                             │
│       \"score\": 4.8,                                   │
│       \"avgPrice\": 8500,                               │
│       \"avgPriceYuan\": \"人均85元\",                     │
│       \"distance\": 350,                                │
│       \"distanceText\": \"350m\",                        │
│       \"blogCount\": 326,                               │
│       \"positiveRate\": 95,                             │
│       \"hotDish\": \"水煮鱼(被提及186次)\",                │
│       \"tags\": [\"川菜\", \"网红打卡\"]                  │
│     },                                                  │
│     ...                                                 │
│   ]"                                                    │
│ }                                                       │
│                                                         │
│ LLM 基于真实数据 → 流式生成推荐文案 + 推荐理由              │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│ ④-G SSE 流式输出 + 持久化                                │
│                                                         │
│ → event: text_chunk   (逐字推送文案)                     │
│ → event: shop_card ×3 (结构化商户卡片)                    │
│ → event: text_chunk   (追问引导语)                       │
│ → event: done         (完成标记)                         │
│                                                         │
│ 异步:                                                    │
│ · 更新 Redis 上下文 (messages + entities)                │
│ · 持久化 tb_ai_message (含 cards_data JSON 快照)         │
│ · 更新 tb_ai_session.message_count + shop_ids           │
│ · 释放会话级分布式锁                                      │
└─────────────────────────────────────────────────────────┘
```

---

### ⑤ 优惠券查询流程（query_vouchers）

```plain
上下文: last_recommended_shops[0] = {id:5001, name:"川味小馆"}
用户: "第一家有优惠券吗"
        │
        ▼
┌──────────────────────────────────────┐
│ LLM 识别意图 → function_call:        │
│ query_vouchers({shop_id: 5001})      │
│                                      │
│ "第一家" 的解析依赖上下文中的          │
│ last_recommended_shops 列表           │
└───────────────────┬──────────────────┘
                    │
                    ▼
┌──────────────────────────────────────┐
│ 查询该商户可用优惠券                   │
│                                      │
│ SELECT v.*, sv.stock, sv.total_stock,│
│   sv.begin_time AS sk_begin,         │
│   sv.end_time AS sk_end              │
│ FROM tb_voucher v                    │
│ LEFT JOIN tb_seckill_voucher sv      │
│   ON sv.voucher_id = v.id            │
│ WHERE v.shop_id = 5001               │
│   AND v.status = 0                   │
│   AND v.deleted = 0                  │
│   AND (v.valid_end_time IS NULL      │
│        OR v.valid_end_time > NOW())  │
│ ORDER BY v.type DESC,               │
│   (v.actual_value - v.pay_value) DESC│
└───────────────────┬──────────────────┘
                    │
                    ▼
┌──────────────────────────────────────┐
│ 将结果回注 LLM (role: function)      │
│                                      │
│ LLM 流式生成文案 + 推送 voucher_card │
│ (同 ④-G 模式)                        │
└──────────────────────────────────────┘
```

---

### ⑥ 商户详情查询流程（get_shop_detail）

```plain
用户: "川味小馆怎么样" / "第二家评价如何" / "这家店几点关门"
        │
        ▼
┌──────────────────────────────────────────┐
│ LLM → function_call:                     │
│ get_shop_detail({shop_id: 5001})         │
└───────────────────┬──────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────┐
│ SELECT s.*, st.name AS type_name         │
│ FROM tb_shop s                           │
│ JOIN tb_shop_type st ON st.id=s.type_id  │
│ WHERE s.id = 5001 AND s.deleted = 0;     │
│                                          │
│ -- 评价维度均值                            │
│ SELECT                                   │
│   AVG(score_taste) AS avg_taste,         │
│   AVG(score_env) AS avg_env,             │
│   AVG(score_service) AS avg_service      │
│ FROM tb_review                           │
│ WHERE shop_id = 5001                     │
│   AND status = 1 AND deleted = 0;        │
│                                          │
│ -- 最新好评摘录(3条)                       │
│ SELECT content, score, create_time       │
│ FROM tb_review                           │
│ WHERE shop_id = 5001                     │
│   AND score >= 4 AND status = 1          │
│ ORDER BY create_time DESC LIMIT 3;       │
└───────────────────┬──────────────────────┘
                    │
                    ▼
            回注 LLM → 流式输出
```

---

### ⑦ 笔记搜索流程（search_blogs）

```plain
用户: "有没有相关的探店笔记" / "看看别人怎么评价的"
        │
        ▼
┌──────────────────────────────────────────┐
│ LLM → function_call:                     │
│ search_blogs({shop_id: 5001})            │
│ 或 search_blogs({keyword: "川菜 望京"})   │
└───────────────────┬──────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────┐
│ -- 按商户搜索                             │
│ SELECT b.id, b.title, b.images,          │
│   b.like_count, b.comment_count,         │
│   u.nick_name, u.avatar                  │
│ FROM tb_blog b                           │
│ JOIN tb_user u ON u.id = b.user_id       │
│ WHERE b.shop_id = #{shopId}              │
│   AND b.status = 1 AND b.deleted = 0     │
│ ORDER BY b.like_count DESC               │
│ LIMIT 5;                                 │
│                                          │
│ -- 或按关键词搜索                          │
│ SELECT b.id, b.title, b.images,          │
│   b.like_count, b.comment_count,         │
│   u.nick_name, u.avatar,                 │
│   s.id AS shop_id, s.name AS shop_name   │
│ FROM tb_blog b                           │
│ JOIN tb_user u ON u.id = b.user_id       │
│ JOIN tb_shop s ON s.id = b.shop_id       │
│ WHERE b.status = 1 AND b.deleted = 0     │
│   AND (b.title LIKE '%#{keyword}%'       │
│        OR b.content LIKE '%#{keyword}%') │
│ ORDER BY b.like_count DESC               │
│ LIMIT 5;                                 │
└───────────────────┬──────────────────────┘
                    │
                    ▼
          回注 LLM → 流式输出 blog_card
```

---

### ⑧ 多轮上下文管理

```plain
═══════════════════════════════════════════════════
  示例对话流程
═══════════════════════════════════════════════════

第1轮: "我想吃人均80的川菜，望京附近"
       → 提取: {cuisine:川菜, budget_max:8000, location:望京}
       → search_shops → 推荐3家商户

第2轮: "第一家有优惠券吗"
       → 上下文识别: "第一家" = 川味小馆(id=5001)
       → query_vouchers → 展示优惠券

第3轮: "有没有更便宜的"
       → 上下文: 保留 cuisine=川菜, location=望京
       → 修改: budget_max ↓ (LLM 判断减少预算)
       → search_shops(budget_max=5000) → 重新推荐

第4轮: "看看第一家的探店笔记"
       → 上下文识别: 新推荐的第一家
       → search_blogs → 展示 blog_card

═══════════════════════════════════════════════════
  Redis 上下文结构
═══════════════════════════════════════════════════

Key: chat:session:{sessionId}
TTL: 24小时

Value (Hash):
{
  userId: 8001,
  longitude: 116.4812,                  ← 创建时定位
  latitude: 39.9961,

  messages: [                           ← 对话历史(发给LLM)
    {role:"system",    content:"你是..."},
    {role:"user",      content:"我想吃人均80的川菜，望京附近"},
    {role:"assistant", function_call:{name:"search_shops",...}},
    {role:"function",  name:"search_shops", content:"[{...}]"},
    {role:"assistant", content:"为你找到3家..."},
    {role:"user",      content:"第一家有优惠券吗"},
    ...
  ],

  entities: {                           ← 累积提取的实体
    cuisine: "川菜",
    budget_max: 8000,
    location: "望京",
    searchLng: 116.4812,                ← 地理编码结果
    searchLat: 40.0020,
    occasion: null,
    last_recommended_shops: [
      {id:5001, name:"川味小馆", score:4.8},
      {id:5015, name:"蜀香园", score:4.6},
      {id:5023, name:"麻辣空间", score:4.5}
    ]
  },

  turnCount: 4,                         ← 对话轮次
  totalTokens: 2580                     ← 累计Token用量
}
```

### 上下文窗口管理策略

```plain
每次发送给 LLM 前，执行上下文压缩:
═══════════════════════════════════════════════════

┌────────────────────────────────────────────────┐
│ 读取 tb_sys_config:                             │
│   context_summary_threshold = 6000 tokens      │
│   max_history_turns = 20                       │
└───────────────────┬────────────────────────────┘
                    │
                    ▼
              totalTokens > 6000 ?
              ┌──────┴──────┐
              No            Yes
              │             │
              ▼             ▼
        正常发送      ┌──────────────────────────┐
                     │ 上下文压缩流程:            │
                     │                          │
                     │ 1. 保留 system prompt     │
                     │    (必须，不压缩)          │
                     │                          │
                     │ 2. 保留最近 3 轮完整对话   │
                     │    (含 function_call 和   │
                     │     function results)     │
                     │                          │
                     │ 3. 早期对话 → 摘要替换     │
                     │    调用 LLM 生成摘要:      │
                     │    "请用1~2句话概括以下     │
                     │     对话的关键信息..."      │
                     │                          │
                     │    摘要示例:               │
                     │    {role: "system",       │
                     │     content:              │
                     │     "[历史摘要] 用户询问了  │
                     │      望京川菜推荐，推荐了   │
                     │      川味小馆/蜀香园/麻辣   │
                     │      空间，查看了川味小馆   │
                     │      的优惠券。"}           │
                     │                          │
                     │ 4. entities 状态始终保留   │
                     │    (不参与压缩)            │
                     │                          │
                     │ 5. function results 精简  │
                     │    只保留关键字段:          │
                     │    {name, score, avgPrice,│
                     │     distance, rate, dish} │
                     └──────────────────────────┘
```

---

### ⑨ 意图识别详细映射

```plain
┌────────────────────────────────────────────────────────┐
│                    意图分类体系                          │
├──────────────────┬─────────────────────────────────────┤
│ 意图              │ 触发示例                             │
├──────────────────┼─────────────────────────────────────┤
│ search_shops     │ "推荐附近的火锅"                      │
│ (商户推荐)        │ "人均50能去哪吃"                      │
│                  │ "约会去哪里好"                        │
│                  │ "望京有什么好吃的"                     │
├──────────────────┼─────────────────────────────────────┤
│ query_vouchers   │ "有优惠券吗"                          │
│ (优惠券查询)      │ "第一家有什么活动"                     │
│                  │ "今天有秒杀吗"                        │
├──────────────────┼─────────────────────────────────────┤
│ get_shop_detail  │ "川味小馆怎么样"                      │
│ (商户详情)        │ "第二家评价如何"                       │
│                  │ "这家店几点关门"                       │
├──────────────────┼─────────────────────────────────────┤
│ search_blogs     │ "有没有相关的探店笔记"                  │
│ (笔记搜索)        │ "看看别人怎么评价的"                   │
│                  │ "这家店的探店视频"                     │
├──────────────────┼─────────────────────────────────────┤
│ search_shops     │ "有没有更便宜的"                      │
│ (条件修正)        │ "换个近一点的"                        │
│                  │ "要有包间的"                          │
│                  │ → 保留已有实体，修改对应字段            │
├──────────────────┼─────────────────────────────────────┤
│ (无function)     │ "你好" / "谢谢推荐"                   │
│ (通用闲聊)        │ "今天天气怎么样"                      │
│                  │ → LLM直接文本回复，引导回探店           │
└──────────────────┴─────────────────────────────────────┘
```

---

### ⑩ SSE 与 Function Calling 完整时序

```plain
═══════════════════════════════════════════════════════════
 单次 Function Call 场景（商户推荐）
═══════════════════════════════════════════════════════════

 时间轴   客户端(SSE)         后端                  LLM API
 ─────────────────────────────────────────────────────────
 T+0s     POST /send ──────▶ 收到请求
                             前置校验链
                             (锁 + 限流 + 敏感词)
 T+0.2s   ◀── thinking      ─────────────────────▶ 发送消息
           "理解需求中..."                          + functions
                                                   定义
 T+1.0s                      ◀── function_call ──  LLM 返回
                              search_shops(...)     (非流式)
 T+1.1s   ◀── thinking      执行 SQL 查询
           "搜索川菜馆..."    地理编码 + 商户检索
                             + 笔记统计 + 排序
 T+2.0s                      结果回注 LLM ────────▶ 接收数据
                              (role: function)
 T+2.5s   ◀── text_chunk    ◀── stream chunk ──   流式生成
           "为你找到"
 T+2.6s   ◀── text_chunk    ◀── stream chunk ──
           "3家"
 T+2.7s   ◀── shop_card     后端穿插推送卡片
           {川味小馆...}      (LLM文本中识别到
 T+2.9s   ◀── shop_card      商户引用后触发)
           {蜀香园...}
 T+3.1s   ◀── shop_card
           {麻辣空间...}
 T+3.3s   ◀── text_chunk    ◀── stream chunk ──
           "要看优惠券吗？"
 T+3.5s   ◀── done          流结束
           {messageId,        异步: 落库 + 更新上下文
            totalTokens}      释放锁

═══════════════════════════════════════════════════════════
 多次 Function Call 场景（推荐 + 查券连续触发）
═══════════════════════════════════════════════════════════

 时间轴   客户端(SSE)         后端                  LLM API
 ─────────────────────────────────────────────────────────
 T+0s     POST /send ──────▶
 T+0.2s   ◀── thinking      ─────────────────────▶ 第1次请求
 T+1.0s                      ◀── function_call:    search_shops
 T+1.1s   ◀── thinking      执行 SQL
 T+2.0s                      回注结果 ────────────▶ 第2次请求
 T+2.8s                      ◀── function_call:    query_vouchers
                              (LLM 判断需要同时      ← 第2次调用
                               展示优惠券)
 T+2.9s   ◀── thinking      执行优惠券查询
           "查询优惠活动..."
 T+3.5s                      回注结果 ────────────▶ 第3次请求
 T+4.0s   ◀── text_chunk    ◀── stream ──         最终流式生成
 T+4.x    ◀── shop_card×3
 T+5.x    ◀── voucher_card×2
 T+6.0s   ◀── done
```

---

### ⑪ LLM 降级策略

```plain
┌─────────────────────────────────────────────────┐
│              LLM 调用降级链                       │
│                                                 │
│ ┌─────────┐    超时/错误    ┌─────────────┐     │
│ │  主模型   │──────────────▶│  备用模型      │     │
│ │DeepSeek  │    3s超时      │ 通义千问/GPT   │     │
│ │ Chat     │    或5xx      │              │     │
│ └─────────┘               └──────┬──────┘     │
│                                   │ 也失败      │
│                                   ▼             │
│                           ┌──────────────┐     │
│                           │  规则引擎兜底   │     │
│                           │ (不走LLM)     │     │
│                           └──────┬───────┘     │
│                                  │              │
│                                  ▼              │
│  规则兜底逻辑:                                    │
│  ┌───────────────────────────────────────────┐  │
│  │ 1. 直接用 ④-E 排序结果                      │  │
│  │ 2. 推荐理由使用模板:                         │  │
│  │    "评分{score}分，{blogCount}篇笔记，       │  │
│  │     好评率{positiveRate}%，                  │  │
│  │     招牌菜: {hotDish}"                      │  │
│  │ 3. 追问语使用固定文案:                       │  │
│  │    "要不要看看优惠券？"                       │  │
│  │ 4. 提示:                                    │  │
│  │    "🤖 AI助手暂时离开，为你自动推荐如下～     │  │
│  │     稍后 AI 恢复后可继续对话"                 │  │
│  └───────────────────────────────────────────┘  │
│                                                 │
│ 监控: 降级事件 → tb_operation_log 记录            │
│       告警: LLM 连续失败 ≥ 3次 → 运营通知         │
└─────────────────────────────────────────────────┘
```

---

### ⑫ 断线恢复机制

```plain
═══════════════════════════════════════════════════
 正常流程
═══════════════════════════════════════════════════

客户端发送消息 → SSE 流式接收 → 收到 done → 标记完成 ✅

═══════════════════════════════════════════════════
 断线场景
═══════════════════════════════════════════════════

客户端发送消息 → SSE 接收中 → 网络断开 ❌ (未收到 done)

客户端处理:
┌──────────────────────────────────────────┐
│ 1. 检测到 SSE 连接断开                     │
│ 2. 已接收的部分内容 → 暂存本地             │
│ 3. 网络恢复后 → 发送恢复请求:              │
│                                          │
│ POST /api/ai/chat/send                   │
│ {                                        │
│   "sessionId": "s_xxx",                  │
│   "message": "(原消息)",       ← 可选     │
│   "retryMessageId": "msg_001"  ← 关键    │
│ }                                        │
└──────────────────────────────────────────┘

后端处理:
┌──────────────────────────────────────────┐
│ 检测到 retryMessageId:                    │
│                                          │
│ 1. 查 Redis 或 MySQL 该消息状态           │
│                                          │
│ ├─ 状态=已完成 (done)                     │
│ │  → 从 tb_ai_message.cards_data 读取    │
│ │  → 重新推送完整 SSE 事件序列            │
│ │  → event: text_chunk (完整文本)         │
│ │  → event: shop_card/voucher_card       │
│ │  → event: done                         │
│ │                                        │
│ ├─ 状态=处理中                            │
│ │  → event: error                        │
│ │    {errorCode: "MESSAGE_PROCESSING"}   │
│ │  → 客户端等待 3s 后重试                 │
│ │                                        │
│ └─ 状态=不存在                            │
│    → 当作新消息处理                       │
└──────────────────────────────────────────┘
```

---

## 接口 4 — 会话历史

### 基本信息

```plain
GET /api/ai/chat/history/{sessionId}
Authorization: Bearer <token>
```

### 请求参数（Query）

| 参数        | 类型    | 必填 | 默认 | 说明                             |
| ----------- | ------- | ---- | ---- | -------------------------------- |
| `lastMsgId` | Long    | 否   | —    | 上一次最后一条消息ID（滚动加载） |
| `size`      | Integer | 否   | 20   | 加载条数                         |

### 响应

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "sessionId": "s_20260321_a8f3b2c1",
    "messages": [
      {
        "id": 1001,
        "role": "assistant",
        "content": "你好！我是AI探店助手 🤖",
        "cards": [],
        "timestamp": "2026-03-21T14:00:00"
      },
      {
        "id": 1002,
        "role": "user",
        "content": "我想吃人均80的川菜，望京附近",
        "cards": [],
        "timestamp": "2026-03-21T14:01:30"
      },
      {
        "id": 1003,
        "role": "assistant",
        "content": "为你找到3家望京附近的川菜推荐：\n\n💡 要不要我帮你看看这几家店的优惠券？",
        "cards": [
          {
            "type": "shop_card",
            "shop": {
              "id": 5001,
              "name": "川味小馆",
              "score": 4.8,
              "distance": 350,
              "distanceText": "350m",
              "avgPrice": 8500,
              "avgPriceText": "人均85元",
              "coverImage": "/imgs/shop/5001/cover.jpg",
              "reason": "326篇笔记中好评率95%，招牌水煮鱼被提及186次",
              "blogCount": 326,
              "positiveRate": 95,
              "hotDish": "水煮鱼",
              "tags": ["川菜", "网红打卡"]
            }
          },
          {
            "type": "shop_card",
            "shop": {
              "id": 5015,
              "name": "蜀香园",
              "score": 4.6,
              "distance": 600,
              "distanceText": "600m",
              "avgPrice": 7500,
              "avgPriceText": "人均75元",
              "coverImage": "/imgs/shop/5015/cover.jpg",
              "reason": "性价比高，回头客多，毛血旺是招牌",
              "blogCount": 189,
              "positiveRate": 91,
              "hotDish": "毛血旺",
              "tags": ["川菜"]
            }
          }
        ],
        "timestamp": "2026-03-21T14:01:35"
      }
    ],
    "hasMore": false
  }
}
```

### 数据查询

```sql
-- 从 MySQL 查询持久化的消息（含完整卡片快照）
SELECT id, session_id, role, content, cards_data, create_time
FROM tb_ai_message
WHERE session_id = #{sessionId}
  AND (#{lastMsgId} IS NULL OR id < #{lastMsgId})
ORDER BY id DESC
LIMIT #{size};

-- cards 字段直接反序列化 cards_data JSON
-- 不依赖重新查询商户/优惠券数据（快照方案）
```

### 存储策略

```plain
┌─────────────────────────────────────────────┐
│            会话数据存储分层                    │
├────────────┬────────────────────────────────┤
│ Redis      │ 活跃会话上下文 (TTL 24h)        │
│ (实时读写)  │ · LLM 对话历史 (最近20轮)       │
│            │ · 提取的实体状态                 │
│            │ · 推荐过的商户列表               │
│            │ · Token 计数                   │
├────────────┼────────────────────────────────┤
│ MySQL      │ 持久化历史记录 (异步落库)         │
│ (持久存储)  │ · tb_ai_session: 会话元信息      │
│            │ · tb_ai_message: 逐条消息        │
│            │   - role, content               │
│            │   - cards_data (JSON快照)        │
│            │ · 用于: 历史回看、偏好分析        │
├────────────┼────────────────────────────────┤
│ 回看逻辑   │ 优先读 Redis (活跃会话)           │
│            │ Redis 过期 → 读 MySQL            │
│            │ 卡片数据 → 直接用 cards_data 快照 │
│            │ (不重新查询，数据可能已变化)        │
└────────────┴────────────────────────────────┘
```

---

## 接口 5 — 删除会话

### 基本信息

```plain
DELETE /api/ai/chat/session/{sessionId}
Authorization: Bearer <token>
```

### 路径参数

| 参数        | 类型   | 必填 | 说明                       |
| ----------- | ------ | ---- | -------------------------- |
| `sessionId` | String | 是   | 会话ID，必须属于当前用户   |

### 响应

```json
{
  "code": 200,
  "msg": "会话已删除"
}
```

### 业务逻辑

```plain
1. 校验 sessionId 属于当前用户
2. Redis: DEL chat:session:{sessionId}
3. MySQL: UPDATE tb_ai_session SET deleted=1 WHERE session_id=#{sessionId}
4. MySQL: (可选) tb_ai_message 保留，不物理删除，用于数据分析
```

---

## 接口 6 — 会话列表（新增）

### 基本信息

```plain
GET /api/ai/chat/sessions
Authorization: Bearer <token>
```

### 请求参数（Query）

| 参数   | 类型    | 必填 | 默认 | 说明         |
| ------ | ------- | ---- | ---- | ------------ |
| `page` | Integer | 否   | 1    | 页码         |
| `size` | Integer | 否   | 10   | 每页条数     |

### 响应

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "records": [
      {
        "sessionId": "s_20260321_a8f3b2c1",
        "title": "望京川菜推荐",
        "messageCount": 6,
        "lastMessage": "好的，已帮你查看优惠券",
        "lastMessageTime": "2026-03-21T14:15:00",
        "createdTime": "2026-03-21T14:00:00"
      },
      {
        "sessionId": "s_20260320_b9e4c3d2",
        "title": "国贸附近日料",
        "messageCount": 4,
        "lastMessage": "推荐了3家日料店，需要查看优惠券吗？",
        "lastMessageTime": "2026-03-20T19:30:00",
        "createdTime": "2026-03-20T19:20:00"
      }
    ],
    "total": 12,
    "page": 1,
    "size": 10,
    "hasMore": true
  }
}
```

### 数据查询

```sql
-- 会话列表
SELECT
  s.session_id,
  s.title,
  s.message_count,
  s.update_time AS last_message_time,
  s.create_time AS created_time
FROM tb_ai_session s
WHERE s.user_id = #{userId}
  AND s.deleted = 0
ORDER BY s.update_time DESC
LIMIT #{offset}, #{size};

-- 总数
SELECT COUNT(*) FROM tb_ai_session
WHERE user_id = #{userId} AND deleted = 0;

-- 最后一条消息（可选，也可冗余到 session 表）
SELECT content FROM tb_ai_message
WHERE session_id = #{sessionId}
ORDER BY id DESC LIMIT 1;
```

---

## RAG 检索增强架构

```plain
┌────────────────────────────────────────────────────────────────┐
│                     AI 探店助手 RAG 架构                         │
│                                                                │
│  用户消息                                                       │
│     │                                                          │
│     ▼                                                          │
│  ┌──────────────┐                                              │
│  │ ⓪ 前置校验     │  · 断线恢复判断                               │
│  │               │  · 会话校验 + 分布式锁                        │
│  │               │  · 频率限制（配置化）                          │
│  │               │  · 敏感词过滤（DFA/AC自动机）                  │
│  └──────┬───────┘                                              │
│         │                                                      │
│         ▼                                                      │
│  ┌──────────────┐                                              │
│  │ ① 意图识别     │  ← LLM Function Calling                    │
│  │    实体提取     │    提取: 菜系/预算/地点/场景                  │
│  └──────┬───────┘                                              │
│         │                                                      │
│         ▼                                                      │
│  ┌──────────────┐    ┌────────────────────────────────────┐    │
│  │ ② 数据检索     │───▶│            MySQL 数据源              │    │
│  │   (Retrieval) │    │                                    │    │
│  │              │    │  tb_shop ──────── 商户基本信息        │    │
│  │  · 地理编码   │    │  tb_shop_type ─── 商户类型           │    │
│  │  · 标签匹配   │    │  tb_tag ───────── 菜系/场景标签       │    │
│  │  · 矩形粗筛   │    │  tb_tag_relation ─ 标签关联          │    │
│  │  · 精确距离   │    │  tb_review ────── 评价数据/多维评分   │    │
│  │  · 多因子排序 │    │  tb_blog ──────── 笔记内容/热度       │    │
│  │              │    │  tb_voucher ───── 优惠券信息          │    │
│  └──────┬───────┘    └────────────────────────────────────┘    │
│         │                                                      │
│         ▼                                                      │
│  ┌──────────────┐                                              │
│  │ ③ 上下文组装   │                                              │
│  │  (Augment)   │  · 结构化数据 → 精简 JSON                     │
│  │              │  · 注入对话历史（窗口管理后）                    │
│  │              │  · 注入 System Prompt                        │
│  │              │  · 实体状态累积传递                             │
│  └──────┬───────┘                                              │
│         │                                                      │
│         ▼                                                      │
│  ┌──────────────┐    ┌─────────────┐                           │
│  │ ④ LLM 生成    │───▶│ 主: DeepSeek │                           │
│  │ (Generation) │    │ 备: 通义/GPT │  ← 降级链                  │
│  │              │◀───│ 兜: 规则引擎  │                           │
│  │  · 推荐理由   │    └─────────────┘                           │
│  │  · 对话回复   │                                              │
│  │  · 追问引导   │                                              │
│  └──────┬───────┘                                              │
│         │                                                      │
│         ▼                                                      │
│  ┌──────────────┐                                              │
│  │ ⑤ 结构化输出   │  · thinking     (状态提示)                    │
│  │    SSE 推送   │  · text_chunk   (流式文本)                    │
│  │              │  · shop_card    (商户卡片)                    │
│  │              │  · voucher_card (优惠券卡片)                   │
│  │              │  · blog_card    (笔记卡片)                    │
│  │              │  · error        (错误)                       │
│  │              │  · done         (完成)                       │
│  └──────┬───────┘                                              │
│         │                                                      │
│         ▼                                                      │
│  ┌──────────────┐                                              │
│  │ ⑥ 异步持久化   │  · Redis 更新上下文 + 实体                    │
│  │              │  · MySQL tb_ai_message (含 cards_data)        │
│  │              │  · MySQL tb_ai_session 更新                   │
│  │              │  · 释放分布式锁                                │
│  └──────────────┘                                              │
└────────────────────────────────────────────────────────────────┘
```

---

## LLM Function Calling 定义

```json
{
  "functions": [
    {
      "name": "search_shops",
      "description": "根据条件搜索推荐商户。当用户提到菜系、预算、地点、场景等需求时调用。当用户说'更便宜''更近'等修正条件时，也调用此函数并调整对应参数。",
      "parameters": {
        "type": "object",
        "properties": {
          "cuisine":      { "type": "string",  "description": "菜系类型(川菜/火锅/日料/西餐/烧烤...)" },
          "budget_min":   { "type": "integer", "description": "最低预算(分)" },
          "budget_max":   { "type": "integer", "description": "最高预算(分)" },
          "location":     { "type": "string",  "description": "地点关键词(望京/国贸/中关村...)，后端负责地理编码" },
          "occasion":     { "type": "string",  "description": "场景(约会/聚餐/家庭/商务/一人食)" },
          "radius":       { "type": "integer", "description": "搜索半径(米),默认3000", "default": 3000 },
          "sort_by":      { "type": "string",  "description": "排序偏好(score/distance/price)", "enum": ["score","distance","price"] },
          "features":     { "type": "array",   "description": "特殊要求(有包间/可停车/有露台/宠物友好等)", "items": {"type":"string"} },
          "exclude_ids":  { "type": "array",   "description": "排除的商户ID(避免重复推荐)", "items": {"type":"integer"} }
        }
      }
    },
    {
      "name": "query_vouchers",
      "description": "查询指定商户的可用优惠券和秒杀活动。当用户问'有优惠吗''有券吗''有活动吗'时调用。",
      "parameters": {
        "type": "object",
        "properties": {
          "shop_id":   { "type": "integer", "description": "商户ID" },
          "shop_name": { "type": "string",  "description": "商户名称(辅助确认)" }
        },
        "required": ["shop_id"]
      }
    },
    {
      "name": "get_shop_detail",
      "description": "获取商户详细信息，包括营业时间、多维评分、最新评价等。当用户问'怎么样''评价如何''几点关门'时调用。",
      "parameters": {
        "type": "object",
        "properties": {
          "shop_id":   { "type": "integer", "description": "商户ID" },
          "shop_name": { "type": "string",  "description": "商户名称(辅助确认)" }
        },
        "required": ["shop_id"]
      }
    },
    {
      "name": "search_blogs",
      "description": "搜索相关探店笔记。当用户想看真实探店体验、评价详情时调用。",
      "parameters": {
        "type": "object",
        "properties": {
          "shop_id": { "type": "integer", "description": "商户ID(按店搜索)" },
          "keyword": { "type": "string",  "description": "搜索关键词(按内容搜索)" },
          "limit":   { "type": "integer", "description": "返回条数,默认5", "default": 5 }
        }
      }
    }
  ]
}
```

### 完整 LLM 请求示例

```json
{
  "model": "deepseek-chat",
  "messages": [
    {
      "role": "system",
      "content": "你是「悦享生活」APP的AI探店助手...（完整System Prompt）"
    },
    {
      "role": "user",
      "content": "我想吃人均80的川菜，望京附近"
    }
  ],
  "functions": [
    { "name": "search_shops", "..." : "..." },
    { "name": "query_vouchers", "..." : "..." },
    { "name": "get_shop_detail", "..." : "..." },
    { "name": "search_blogs", "..." : "..." }
  ],
  "stream": true,
  "temperature": 0.7,
  "max_tokens": 1000
}
```

### Function Calling 多轮交互

```plain
═══════════════════════════════════════════════════
 LLM 调用 Function → 后端执行 → 回注结果
═══════════════════════════════════════════════════

第1步: LLM 返回 function_call
─────────────────────────────────
{
  "function_call": {
    "name": "search_shops",
    "arguments": "{\"cuisine\":\"川菜\",\"budget_max\":8000,\"location\":\"望京\"}"
  }
}

第2步: 后端执行
─────────────────────────────────
· 地理编码: "望京" → (116.4812, 40.0020)
· SQL 查询: 标签匹配 + 矩形粗筛 + 精确距离
· 笔记/评价统计
· 多因子排序 → TOP 3

第3步: 结果回注 LLM
─────────────────────────────────
{
  "role": "function",
  "name": "search_shops",
  "content": "[{\"id\":5001,\"name\":\"川味小馆\",\"score\":4.8,\"avgPrice\":8500,\"avgPriceYuan\":\"人均85元\",\"distance\":350,\"distanceText\":\"350m\",\"blogCount\":326,\"positiveRate\":95,\"hotDish\":\"水煮鱼(186次)\",\"tags\":[\"川菜\",\"网红打卡\"]},{\"id\":5015,\"name\":\"蜀香园\",...},{\"id\":5023,\"name\":\"麻辣空间\",...}]"
}

第4步: LLM 基于真实数据流式生成自然语言推荐
─────────────────────────────────
包含推荐文案、推荐理由、追问引导等
后端解析 LLM 输出，穿插推送 shop_card 事件
```

---

## 数据流全景

```plain
┌─────────┐
│  客户端   │
└────┬────┘
     │
     ├── POST   /session ────────▶ 创建会话 ──────────────┐
     │                                                    │
     ├── GET    /hot-questions ──▶ 热门问题 ───┐           │
     │                                        │           │
     ├── POST   /send (SSE) ────▶ 核心对话 ───┤           │
     │    ↕ 流式响应                           │           │
     │    · thinking                          │           │
     │    · text_chunk                        │           │
     │    · shop_card                         ▼           ▼
     │    · voucher_card               ┌───────────────────────┐
     │    · blog_card                  │        Redis          │
     │    · error                      │                       │
     │    · done                       │ · 会话上下文 (TTL 24h) │
     │                                 │ · 实体状态累积          │
     ├── GET    /history ────────▶     │ · 频率限制计数器        │
     │                                 │ · 会话级分布式锁        │
     ├── DELETE /session ────────▶     │ · 地理编码缓存          │
     │                                 │ · 敏感词缓存            │
     └── GET    /sessions ───────▶     └──────────┬────────────┘
                                                  │
                          ┌───────────────────┬────┴──────────────┐
                          ▼                   ▼                   ▼
                   ┌────────────┐     ┌──────────────┐    ┌────────────┐
                   │   MySQL    │     │   LLM API    │    │  异步任务   │
                   │            │     │              │    │            │
                   │ tb_shop    │◀────│  Function    │    │ 消息落库    │
                   │ tb_tag     │     │  Calling     │    │ 上下文更新  │
                   │ tb_tag_rel │     │              │    │ 操作日志    │
                   │ tb_review  │     │ ┌──────────┐ │    │ 锁释放     │
                   │ tb_blog    │     │ │ DeepSeek │ │    │            │
                   │ tb_voucher │     │ │ 通义/GPT │ │    │            │
                   │ tb_region  │     │ │ 规则兜底  │ │    │            │
                   │            │     │ └──────────┘ │    │            │
                   │ tb_ai_*    │◀────│              │────│            │
                   │ tb_sys_cfg │     │              │    │            │
                   │ tb_sens_wd │     │              │    │            │
                   └────────────┘     └──────────────┘    └────────────┘
```

---

## 关键设计决策

| 决策点         | 方案                                 | 理由                                                       |
| -------------- | ------------------------------------ | ---------------------------------------------------------- |
| 响应方式       | **SSE 流式输出**                     | AI 生成耗时较长；流式逐字体验佳                            |
| 数据可信度     | **RAG 架构**                         | 推荐基于真实数据库数据，不让 LLM 编造商户                  |
| 意图识别       | **LLM Function Calling**             | 比规则引擎更灵活；自然语言理解准确                         |
| 菜系匹配       | **标签体系 (tb_tag + tb_tag_relation)** | 复用已有标签表，灵活扩展，不改 shop 表结构                |
| 地理编码       | **商户聚合 → 地区表 → 外部API 三级降级** | 优先站内数据，减少外部依赖                             |
| 距离计算       | **矩形粗筛 + 球面精算**              | 粗筛利用 B-Tree 索引，精算保证准确性                       |
| 多轮上下文     | **Redis 存储 + 实体累积 + 窗口压缩** | 快速读写；实体跨轮次传递；防 Token 溢出                    |
| 推荐排序       | **多因子加权排序**                    | 评分+好评率+笔记热度+距离+价格综合权衡                     |
| 卡片类型       | **结构化 JSON 事件 (shop/voucher/blog)** | 前端根据类型渲染不同组件                                |
| 卡片持久化     | **cards_data JSON 快照**             | 历史消息直接反序列化，不依赖重新查询                       |
| 会话存储       | **Redis 活跃 + MySQL 归档**          | 活跃会话快速读写；历史对话持久化用于回看和偏好分析          |
| 频率限制       | **分钟 + 日级双层限流（配置化）**    | 防滥用；参数可运营后台调整                                 |
| 敏感词过滤     | **DFA/AC自动机 + 三级处理**          | 拦截/替换/提醒分级处理；启动加载，变更刷新                 |
| 并发控制       | **会话级分布式锁**                   | 防止同一会话并发消息导致上下文混乱                         |
| 断线恢复       | **retryMessageId 幂等重推**          | 移动端网络不稳定场景下保证消息完整性                       |
| LLM 高可用     | **主备降级 + 规则兜底**              | DeepSeek → 通义/GPT → 规则引擎，保证服务可用              |
| 优惠券跳转     | **actionUrl 前端路由**               | 卡片携带跳转路径，前端直接路由到领取/秒杀页面              |