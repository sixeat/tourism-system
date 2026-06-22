<%--
    ============================================================
    orders.jsp — 我的订单页面
    
    页面功能：
    1. 展示当前登录用户的所有订单（酒店订单 + 门票订单）。
    2. 顶部统计卡片：全部订单数、酒店订单数、门票订单数、待支付订单数。
    3. 支持按类型/状态筛选：全部、酒店订单、门票订单、待支付、已支付。
    4. 每个订单卡片显示：订单类型、编号、项目名称、金额、使用日期、创建时间、状态。
    5. 对"待支付"（CREATED）状态的订单提供"支付"和"取消"操作按钮。
    
    后端 API：
    - GET /api/auth/me — 鉴权，获取当前用户信息，未登录则跳转 login.jsp。
    - POST /api/auth/logout — 退出登录。
    - GET /api/user/orders — 获取当前用户的全部订单（酒店 + 门票）。
    - POST /api/order/hotel/pay?orderId=xxx — 支付酒店订单。
    - POST /api/order/ticket/pay?orderId=xxx — 支付门票订单。
    - POST /api/order/hotel/cancel?orderId=xxx — 取消酒店订单。
    - POST /api/order/ticket/cancel?orderId=xxx — 取消门票订单。
    
    订单状态流转：
    CREATED（待支付）→ PAID（已支付）→ USED（已使用）/ FINISHED（已完成）
    CREATED → CANCELLED（已取消）
    ============================================================
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" isELIgnored="true" %>
<%-- 获取应用上下文路径，用于构建 API 地址和页面跳转链接 --%>
<% String ctx = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <%-- 视口设置，适配移动端 --%>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>我的订单 - 智旅旅行</title>
    <%-- 引入公共壳样式：包含侧边栏、顶部 header、卡片、按钮等基础样式 --%>
    <link rel="stylesheet" href="<%= ctx %>/static/css/ctrip-shell.css?v=20260616-fixed">
    <style>
        /* 订单筛选标签：横向排列，支持换行 */
        .order-tabs {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        /* 激活状态的筛选按钮：蓝底白字 */
        .order-tabs button.active {
            background: #0b8ff6;
            color: #fff;
        }
        /* 统计摘要区：4 列等宽卡片 */
        .order-summary {
            display: grid;
            grid-template-columns: repeat(4, minmax(160px, 1fr));
            gap: 14px;
            margin-bottom: 18px;
        }
        /* 单个统计卡片：白色背景、圆角、阴影 */
        .summary-card {
            background: #fff;
            border: 1px solid #e5edf6;
            border-radius: 20px;
            padding: 18px;
            box-shadow: 0 10px 26px rgba(20, 40, 80, .05);
        }
        .summary-card strong {
            display: block;
            color: #0b8ff6;
            font-size: 28px;
        }
        /* 订单卡片：左侧蓝色竖条标识，相对定位便于放状态徽章 */
        .order-card {
            border-left: 5px solid #0b8ff6;
            position: relative;
        }
        /* 订单头部：类型编号 + 状态徽章 */
        .order-head {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            align-items: flex-start;
        }
        /* 状态徽章：圆角胶囊，不同状态对应不同配色 */
        .order-badge {
            border-radius: 999px;
            padding: 6px 10px;
            font-size: 13px;
            font-weight: 900;
            background: #eef6ff;
            color: #0b7de3;
        }
        .order-badge.green {
            background: #ecfdf5;
            color: #059669;
        }
        .order-badge.orange {
            background: #fff7ed;
            color: #f97316;
        }
        .order-badge.red {
            background: #fef2f2;
            color: #dc2626;
        }
        /* 订单元信息：金额、日期、创建时间 */
        .order-meta {
            display: grid;
            gap: 6px;
            margin-top: 12px;
        }
        /* 空状态：虚线边框、居中提示 */
        .empty-order {
            min-height: 260px;
            display: flex;
            align-items: center;
            justify-content: center;
            text-align: center;
            border: 1px dashed #cfe4fb;
            border-radius: 22px;
            background: #fff;
            color: #667085;
        }
        .empty-order strong {
            display: block;
            color: #0f172a;
            font-size: 24px;
            margin-bottom: 8px;
        }
        /* 响应式：900px 以下统计卡片变为 2 列 */
        @media (max-width: 900px) {
            .order-summary {
                grid-template-columns: repeat(2, minmax(150px, 1fr));
            }
        }
    </style>
</head>
<body>
<%-- 应用壳：左侧侧边栏 + 右侧主内容 --%>
<div class="app-shell">
    <%-- 左侧侧边栏导航：高亮"我的订单" --%>
    <aside class="side-nav">
        <button class="menu-toggle" title="菜单">&#9776;</button>
        <a class="side-link" href="<%= ctx %>/home.jsp"><span class="side-icon">&#8962;</span>首页</a>
        <a class="side-link" href="<%= ctx %>/routes.jsp"><span class="side-icon">&#9635;</span>路线规划</a>
        <a class="side-link" href="<%= ctx %>/hotels.jsp"><span class="side-icon">&#9635;</span>酒店预订</a>
        <a class="side-link" href="<%= ctx %>/tickets.jsp"><span class="side-icon">&#9636;</span>门票&#183;活动</a>
        <a class="side-link" href="<%= ctx %>/map.jsp"><span class="side-icon">&#8982;</span>旅游地图</a>
        <a class="side-link" href="<%= ctx %>/ai.jsp"><span class="side-icon">&#10022;</span>AI行程助手</a>
        <div class="side-divider"></div>
        <a class="side-link active" href="<%= ctx %>/orders.jsp"><span class="side-icon">&#9776;</span>我的订单</a>
        <a class="side-link" href="<%= ctx %>/profile.jsp"><span class="side-icon">&#9786;</span>个人中心</a>
        <button class="side-link" onclick="logout()"><span class="side-icon">&#8618;</span>退出登录</button>
    </aside>
    <%-- 右侧主内容区 --%>
    <main class="main-area">
        <%-- 顶部 Header：品牌 + 全局搜索 + 用户操作 --%>
        <header class="top-header">
            <a class="brand" href="<%= ctx %>/home.jsp"><span class="brand-mark">&#10022;</span>智旅旅行</a>
            <div class="global-search">
                <input id="topKeyword" placeholder="搜索任何旅游相关" onkeydown="if(event.key==='Enter')topSearch()">
                <button onclick="topSearch()">&#8981;</button>
            </div>
            <div class="top-actions">
                <span class="user-chip" id="userChip">用户中心</span>
                <a href="<%= ctx %>/orders.jsp">我的订单</a>
                <a href="<%= ctx %>/ai.jsp">联系客服</a>
                <button onclick="logout()">退出</button>
            </div>
        </header>
        <%-- 页面内容 --%>
        <section class="page-content">
            <%-- Hero 面板：页面标题 + 说明 --%>
            <div class="hero-panel">
                <h1>我的订单</h1>
                <p>酒店和门票订单都在这里，下单后会立即同步显示。</p>
            </div>
            <%-- 统计摘要区：由 JS 根据订单数据动态渲染 4 张卡片 --%>
            <div id="summary" class="order-summary"></div>
            <%-- 筛选标签栏：全部/酒店/门票/待支付/已支付，点击切换加载对应类型 --%>
            <div class="panel">
                <div class="order-tabs">
                    <button class="secondary active" data-type="ALL">全部订单</button>
                    <button class="secondary" data-type="HOTEL">酒店订单</button>
                    <button class="secondary" data-type="TICKET">门票订单</button>
                    <button class="secondary" data-type="CREATED">待支付</button>
                    <button class="secondary" data-type="PAID">已支付</button>
                </div>
            </div>
            <%-- 订单列表容器：由 JS 动态渲染 order-card --%>
            <div id="orders" class="result-grid"></div>
        </section>
    </main>
</div>

<%-- 页面脚本：订单数据加载、渲染、支付、取消、筛选等逻辑 --%>
<script>
<%-- 注入应用上下文路径，供 JS 中拼接 API URL 使用 --%>
const ctx = '<%= ctx %>';
/* 所有订单数据缓存，用于前端筛选和统计 */
let allOrders = [];
/* 当前筛选类型：ALL / HOTEL / TICKET / CREATED / PAID */
let currentType = 'ALL';

/**
 * 登录状态检查
 * 调用 /api/auth/me 获取当前登录用户，若未登录（code !== 200）则跳转 login.jsp。
 * 同时更新顶部用户昵称显示。
 */
async function ensureLogin() {
    const r = await fetch(ctx + '/api/auth/me');
    const d = await r.json();
    if (d.code !== 200) {
        location.href = ctx + '/login.jsp';
        return null;
    }
    const u = d.data || {};
    const c = document.getElementById('userChip');
    if (c) c.textContent = '尊敬的，' + (u.username || '用户');
    return u;
}

/**
 * 退出登录
 * 调用 /api/auth/logout 销毁 Session，成功后跳转登录页。
 */
async function logout() {
    await fetch(ctx + '/api/auth/logout', {method: 'POST'});
    location.href = ctx + '/login.jsp';
}

/**
 * 顶部全局搜索
 * 读取 topKeyword 输入框内容，跳转到 search.jsp 并携带 q 参数。
 */
function topSearch() {
    const kw = (document.getElementById('topKeyword')?.value || '上海').trim();
    location.href = ctx + '/search.jsp?q=' + encodeURIComponent(kw || '上海');
}

/**
 * HTML 转义函数
 * 防止 XSS 攻击：将 &, <, >, ", ' 转换为对应的 HTML 实体。
 */
function e(t) {
    return String(t == null ? '' : t).replace(/[&<>"']/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
}

/* 辅助函数：确保值转为数组 */
function asArray(value) { return Array.isArray(value) ? value : []; }

/**
 * 订单数据标准化
 * 后端返回的数据格式可能是纯数组、包含 orders 的对象、或分别包含 hotelOrders 和 ticketOrders 的对象。
 * 本函数统一转换为包含 orderType 标记的扁平数组，方便前端统一渲染。
 */
function normalizeOrders(data) {
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.orders)) return data.orders;
    return [
        ...asArray(data && data.hotelOrders).map(x => ({...x, orderType: 'HOTEL'})),
        ...asArray(data && data.ticketOrders).map(x => ({...x, orderType: 'TICKET'}))
    ];
}

/**
 * 日期格式化
 * 后端可能返回 [年,月,日,时,分] 数组或字符串，统一格式化为 YYYY-MM-DD HH:MM 或 YYYY-MM-DD。
 */
function fmtDate(v) {
    if (Array.isArray(v)) {
        const [y, m, d, h, mi] = v;
        return y + '-' + String(m).padStart(2, '0') + '-' + String(d).padStart(2, '0') +
               (h != null ? ' ' + String(h).padStart(2, '0') + ':' + String(mi || 0).padStart(2, '0') : '');
    }
    return e(v || '-');
}

/**
 * 生成订单日期描述文字
 * 酒店订单显示"入住/退房"，门票订单显示"游玩日期"和数量。
 */
function orderDateText(o) {
    if (o.orderType === 'HOTEL') {
        const start = fmtDate(o.checkInDate || o.useDate);
        const end = fmtDate(o.checkOutDate);
        return '入住/退房：' + start + ' 至 ' + end;
    }
    return '游玩日期：' + fmtDate(o.visitDate || o.useDate) + (o.quantity ? ' · 数量 ' + o.quantity + ' 张' : '');
}

/* 订单类型中文标签 */
function typeLabel(t) { return t === 'HOTEL' ? '酒店订单' : '门票订单'; }

/* 订单状态中文映射 */
function statusLabel(s) {
    return ({CREATED:'待支付', PAID:'已支付', USED:'已使用', FINISHED:'已完成', CANCELLED:'已取消'}[s] || s || '-');
}

/* 状态对应的颜色类：成功绿色、取消红色、其他橙色 */
function statusClass(s) {
    if (s === 'PAID' || s === 'USED' || s === 'FINISHED') return 'green';
    if (s === 'CANCELLED') return 'red';
    return 'orange';
}

/* 判断订单是否可操作（仅待支付订单可支付/取消） */
function canOperate(o) { return o.orderStatus === 'CREATED'; }

/**
 * 渲染统计摘要
 * 根据 allOrders 缓存计算总数、酒店数、门票数、待支付数，并生成 4 张 summary-card。
 */
function renderSummary() {
    const total = allOrders.length;
    const hotel = allOrders.filter(o => o.orderType === 'HOTEL').length;
    const ticket = allOrders.filter(o => o.orderType === 'TICKET').length;
    const pending = allOrders.filter(o => o.orderStatus === 'CREATED').length;
    summary.innerHTML = '<div class="summary-card"><strong>' + total + '</strong><span>全部订单</span></div>' +
                        '<div class="summary-card"><strong>' + hotel + '</strong><span>酒店订单</span></div>' +
                        '<div class="summary-card"><strong>' + ticket + '</strong><span>门票订单</span></div>' +
                        '<div class="summary-card"><strong>' + pending + '</strong><span>待支付</span></div>';
}

/**
 * 按当前筛选类型过滤订单
 * 支持按 orderType（HOTEL/TICKET）和 orderStatus（CREATED/PAID）筛选。
 */
function filteredOrders() {
    return allOrders.filter(o => currentType === 'ALL' || o.orderType === currentType || o.orderStatus === currentType);
}

/**
 * 渲染订单列表
 * 1. 高亮当前激活的筛选按钮。
 * 2. 根据 filteredOrders() 结果渲染 order-card 列表。
 * 3. 每个卡片包含：类型编号、项目名称、状态徽章、金额、日期、创建时间、操作按钮。
 * 4. 若无可操作按钮，显示"已处理"。
 * 5. 绑定支付和取消按钮的点击事件。
 * 6. 若列表为空，显示空状态提示。
 */
function renderOrders() {
    document.querySelectorAll('[data-type]').forEach(btn => btn.classList.toggle('active', btn.dataset.type === currentType));
    const list = filteredOrders();
    orders.innerHTML = list.map(o =>
        '<div class="card order-card">' +
            '<div class="order-head">' +
                '<div>' +
                    '<div class="title">' + typeLabel(o.orderType) + ' #' + o.id + '</div>' +
                    '<div class="muted">' + e(o.itemName || '-') + '</div>' +
                '</div>' +
                '<span class="order-badge ' + statusClass(o.orderStatus) + '">' + statusLabel(o.orderStatus) + '</span>' +
            '</div>' +
            '<div class="order-meta">' +
                '<div class="muted">金额：<span class="price">&yen;' + Number(o.totalAmount || 0).toFixed(2) + '</span></div>' +
                '<div class="muted">' + e(orderDateText(o)) + '</div>' +
                '<div class="muted">创建时间：' + fmtDate(o.createTime) + '</div>' +
            '</div>' +
            '<div class="action-row">' +
                (canOperate(o) ?
                    '<button class="primary order-pay" data-order-type="' + e(o.orderType) + '" data-order-id="' + o.id + '">支付订单</button>' +
                    '<button class="secondary order-cancel" data-order-type="' + e(o.orderType) + '" data-order-id="' + o.id + '">取消订单</button>' :
                    '<span class="secondary">已处理</span>') +
            '</div>' +
        '</div>'
    ).join('') || '<div class="empty-order"><div><strong>暂无订单</strong><p>去预订酒店或门票，提交后这里会立即显示。</p></div></div>';
    /* 绑定支付按钮事件：根据 orderType 调用对应支付接口 */
    orders.querySelectorAll('.order-pay').forEach(btn => btn.onclick = () => payOrder(btn.dataset.orderType, Number(btn.dataset.orderId)));
    /* 绑定取消按钮事件 */
    orders.querySelectorAll('.order-cancel').forEach(btn => btn.onclick = () => cancelOrder(btn.dataset.orderType, Number(btn.dataset.orderId)));
}

/**
 * 加载订单数据
 * 1. 显示加载中提示。
 * 2. 调用 /api/user/orders 获取当前用户订单。
 * 3. 标准化数据后更新统计摘要和订单列表。
 */
async function loadOrders(type = 'ALL') {
    currentType = type;
    orders.innerHTML = '<div class="card muted">正在加载订单...</div>';
    const r = await fetch(ctx + '/api/user/orders');
    const d = await r.json();
    if (d.code !== 200) {
        orders.innerHTML = '<div class="card muted">订单加载失败，请重新登录。</div>';
        return;
    }
    allOrders = normalizeOrders(d.data);
    renderSummary();
    renderOrders();
}

/**
 * 支付订单
 * 根据订单类型（HOTEL/TICKET）调用对应的支付接口，支付成功后刷新列表并弹窗提示。
 */
async function payOrder(type, id) {
    const r = await fetch(ctx + '/api/order/' + (type === 'HOTEL' ? 'hotel' : 'ticket') + '/pay?orderId=' + id, {method: 'POST'});
    const d = await r.json();
    alert(d.message || '订单已支付');
    loadOrders(currentType);
}

/**
 * 取消订单
 * 先弹出确认对话框，再根据订单类型调用对应取消接口，成功后刷新列表。
 */
async function cancelOrder(type, id) {
    if (!confirm('确定取消这个订单吗？')) return;
    const r = await fetch(ctx + '/api/order/' + (type === 'HOTEL' ? 'hotel' : 'ticket') + '/cancel?orderId=' + id, {method: 'POST'});
    const d = await r.json();
    alert(d.message || '订单已取消');
    loadOrders(currentType);
}

/* 绑定筛选按钮点击事件：点击时加载对应类型的订单 */
document.querySelectorAll('[data-type]').forEach(btn => btn.onclick = () => loadOrders(btn.dataset.type));

/* 页面初始化：先检查登录状态，再加载全部订单 */
ensureLogin().then(() => loadOrders('ALL'));
</script>
<%-- 加载全局搜索脚本，提供顶部搜索框的自动补全和联想功能 --%>
<script src="<%= ctx %>/static/js/global-search.js?v=20260616-fixed"></script>
</body>
</html>
