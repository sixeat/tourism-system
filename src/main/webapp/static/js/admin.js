/**
 * ============================================================
 * admin.js — 后台管理控制台逻辑脚本
 *
 * 功能说明：
 * 1. 为 admin.jsp 提供完整的后台管理交互逻辑，包括数据加载、列表渲染、
 *    表单提交、分页、搜索过滤、导航切换等。
 * 2. 覆盖七大管理模块：数据概览、订单管理、用户管理、景点管理、酒店管理、
 *    门票管理、路线管理。
 * 3. 所有数据操作通过 fetch 调用后端 REST API（/api/admin/...），
 *    采用 JSON 格式传输，支持 GET/POST/PUT/DELETE。
 * 4. 内置分页组件（pageSlice + pager + renderPaged），统一处理大数据量列表。
 * 5. 全局搜索框（adminKeyword）可实时过滤所有模块的数据，支持关键词高亮匹配。
 * 6. 路线管理包含可视化点位编排器，支持选择景点→指定天数→指定站次→自动排序。
 *
 * 后端 API 汇总：
 * - GET /api/admin/dashboard/summary
 * - GET /api/admin/order/list
 * - GET /api/admin/user/list
 * - PUT /api/admin/user/update
 * - POST /api/admin/user/status?id=&status=
 * - GET /api/admin/scenic/list
 * - POST /api/admin/scenic/save
 * - PUT /api/admin/scenic/update
 * - DELETE /api/admin/scenic/delete/{id}
 * - GET /api/admin/hotel/list
 * - POST /api/admin/hotel/save
 * - PUT /api/admin/hotel/update
 * - DELETE /api/admin/hotel/delete/{id}
 * - GET /api/admin/ticket/list
 * - POST /api/admin/ticket/save
 * - PUT /api/admin/ticket/update
 * - DELETE /api/admin/ticket/delete/{id}
 * - GET /api/admin/route/list
 * - POST /api/admin/route/save
 * - PUT /api/admin/route/update
 * - DELETE /api/admin/route/delete/{id}
 * - POST /api/admin/order/hotel/status?id=&orderStatus=
 * - POST /api/admin/order/ticket/status?id=&orderStatus=
 * ============================================================
 */

/* 应用上下文路径，由 admin.jsp 通过 window.APP_CTX 注入，用于拼接 API 地址 */
const ctx = window.APP_CTX || '';


/* ==================== 全局数据缓存 ==================== */
/* 各模块数据缓存数组，用于前端筛选、编辑回填和列表渲染 */
let scenicItems = [];
let hotelItems = [];
let ticketItems = [];
let routeItems = [];
let userItems = [];
let orderItems = [];
let routeSpotDraft = [];
/* 分页大小：每页显示 6 条数据，统一应用于所有列表 */
const pageSize = 6;
/* 分页状态记录器：各模块当前页码，切换搜索条件时通过 resetPages() 重置 */
const pageState = {orders: 1, users: 1, scenic: 1, hotels: 1, tickets: 1, routes: 1};


/* ==================== 通用工具函数 ==================== */

/**
 * 根据 id 获取 DOM 元素
 * 简化 document.getElementById 的调用，减少代码冗余。
 */
function el(id) {
    return document.getElementById(id);
}


/**
 * HTML 实体转义函数
 * 防御 XSS 攻击：将特殊字符（& < > " '）转为对应的 HTML 实体，
 * 确保用户输入的内容不会破坏页面结构或执行恶意脚本。
 */
function esc(text) {
    const map = {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'};
    return String(text == null ? '' : text).replace(/[&<>"']/g, ch => map[ch]);
}


/**
 * 获取全局搜索关键词
 * 读取 adminKeyword 输入框的值，去除首尾空白并转为小写，用于不区分大小写的匹配。
 */
function keyword() {
    return (el('adminKeyword')?.value || '').trim().toLowerCase();
}


/**
 * 关键词匹配函数
 * 遍历 item 的指定字段（keys），拼接后检查是否包含当前搜索关键词。
 * 若搜索框为空，则返回 true（不过滤）。
 */
function hasKeyword(item, keys) {
    const word = keyword();
    if (!word) return true;
    return keys.map(key => item && item[key]).join(' ').toLowerCase().includes(word);
}



/**
 * 重置所有分页状态
 * 当用户在搜索框输入内容时调用，将所有模块的页码重置为 1，避免搜索后显示空白页。
 */
function resetPages() {
    Object.keys(pageState).forEach(key => pageState[key] = 1);
}


/**
 * 分页切片函数
 * 根据 pageState 中记录的当前页码和 pageSize，计算数据切片范围，
 * 返回总条数、最大页码、当前页和当前页数据行。
 */
function pageSlice(key, list) {
    const total = list.length;
    const maxPage = Math.max(1, Math.ceil(total / pageSize));
    pageState[key] = Math.min(Math.max(1, pageState[key] || 1), maxPage);
    const start = (pageState[key] - 1) * pageSize;
    return {
        total,
        maxPage,
        page: pageState[key],
        rows: list.slice(start, start + pageSize)
    };
}


/**
 * 分页组件 HTML 生成器
 * 根据 pageSlice 返回的分页信息，生成"上一页/下一页"按钮和页码显示条。
 * 当只有一页或数据为空时，不显示分页条。
 */
function pager(key, pageData, renderName) {
    if (pageData.total === 0) return '';
    return `
        <div class="pager-bar">
            <div class="pager-info">\u5171 ${pageData.total} \u6761\uff0c\u6bcf\u9875 ${pageSize} \u6761</div>
            <div class="pager-actions">
                <button onclick="changePage('${key}', -1, ${renderName})" ${pageData.page <= 1 ? 'disabled' : ''}>\u4e0a\u4e00\u9875</button>
                <span class="pager-page">${pageData.page} / ${pageData.maxPage}</span>
                <button onclick="changePage('${key}', 1, ${renderName})" ${pageData.page >= pageData.maxPage ? 'disabled' : ''}>\u4e0b\u4e00\u9875</button>
            </div>
        </div>`;
}


/**
 * 翻页函数
 * 更新 pageState 中指定模块的页码（+1 或 -1），然后调用对应的渲染函数重新加载列表。
 */
function changePage(key, step, renderFn) {
    pageState[key] = (pageState[key] || 1) + step;
    renderFn();
}


/**
 * 分页渲染入口
 * 整合 pageSlice（切片）、renderItem（单行渲染）、pager（分页条）和空状态提示，
 * 统一处理所有模块的列表渲染逻辑，减少重复代码。
 */
function renderPaged(containerId, key, list, renderItem, emptyText, renderName) {
    const pageData = pageSlice(key, list);
    const cards = pageData.rows.map(renderItem).join('') || `<div class="empty-state">${emptyText}</div>`;
    el(containerId).innerHTML = cards + pager(key, pageData, renderName);
}


/* ==================== 格式化工具函数 ==================== */

/**
 * 金额格式化（HTML 版本）
 * 将数值转为人民币符号 + 两位小数，用于卡片内的金额展示（innerHTML）。
 */
function money(value) {
    return '&yen;' + Number(value || 0).toFixed(2);
}


/**
 * 金额格式化（纯文本版本）
 * 与 money 类似，但返回纯文本字符串，适用于需要 textContent 的场景。
 */
function textMoney(value) {
    return '\u00a5' + Number(value || 0).toFixed(2);
}


/**
 * 获取今天日期
 * 返回 ISO 格式的前 10 位（YYYY-MM-DD），用于表单默认值和日期校验。
 */
function today() {
    return new Date().toISOString().slice(0, 10);
}


/**
 * 安全设置表单值
 * 若元素存在，则设置 value；若 value 为 null/undefined，则设为空字符串。
 */
function setValue(id, value) {
    const target = el(id);
    if (target) target.value = value == null ? '' : value;
}


/**
 * 显示操作结果提示
 * 在指定的 result 区域显示成功（绿色）或错误（红色）提示文字。
 * @param id 目标 DOM 元素 id
 * @param text 提示内容
 * @param isError 是否为错误提示（true 时添加 error 类，文字变红）
 */
function message(id, text, isError = false) {
    const target = el(id);
    if (!target) return;
    target.textContent = text || '';
    target.classList.toggle('error', Boolean(isError));
}


/**
 * 平滑滚动并聚焦到指定输入框
 * 编辑/回填后调用，引导用户视线到当前操作区域，提升体验。
 */
function scrollToInput(id) {
    const target = el(id);
    if (!target) return;
    target.scrollIntoView({behavior: 'smooth', block: 'center'});
    target.focus();
}


/* ==================== 后端消息处理 ==================== */

/**
 * 检测乱码/不可读字符
 * 某些后端返回的中文消息可能因编码问题出现乱码（如 0xfffd 替换字符），
 * 本函数检测常见乱码 Unicode 码点，避免将乱码直接展示给用户。
 */
function looksMojibake(text) {
    return Array.from(String(text || '')).some(ch => {
        const code = ch.charCodeAt(0);
        return code === 0xfffd || code === 0x9422 || code === 0x95b0 || code === 0x93c5 || code === 0x95c2 || code === 0x74ba;
    });
}


/**
 * 消息标准化
 * 优先使用后端返回的 message，若 message 为空或包含乱码，则使用本地 fallback 提示。
 */
function normalizeMessage(data, fallback) {
    if (!data || !data.message || looksMojibake(data.message)) {
        return fallback;
    }
    return data.message;
}


/* ==================== 网络请求封装 ==================== */

/**
 * 统一 AJAX 请求封装
 * 1. 默认携带 same-origin 凭证（Cookie/Session），确保鉴权通过。
 * 2. 自动解析 JSON 响应，若格式异常则返回友好提示。
 * 3. 检查 HTTP 状态码和业务 code，非 200 时抛出 Error，由调用方 catch 处理。
 * 4. 使用 normalizeMessage 处理后端消息，避免乱码直接展示。
 */
async function request(url, options = {}) {
    const res = await fetch(url, {
        credentials: 'same-origin',
        ...options,
        headers: {
            ...(options.headers || {})
        }
    });
    const data = await res.json().catch(() => ({code: res.status, message: '\u63a5\u53e3\u8fd4\u56de\u683c\u5f0f\u5f02\u5e38'}));
    if (!res.ok || (data.code && data.code !== 200)) {
        throw new Error(normalizeMessage(data, '\u64cd\u4f5c\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u6570\u636e\u540e\u91cd\u8bd5'));
    }
    return data;
}


/* ==================== 鉴权与登录 ==================== */

/**
 * 管理员身份校验
 * 调用 /api/auth/me 获取当前登录用户，检查 code 和 role 字段。
 * 若未登录或角色不是 ADMIN，则强制跳转回 login.jsp，防止未授权访问后台。
 */
async function ensureAdmin() {
    const res = await fetch(ctx + '/api/auth/me', {credentials: 'same-origin'});
    const data = await res.json().catch(() => ({}));
    if (data.code !== 200 || !data.data || data.data.role !== 'ADMIN') {
        location.href = ctx + '/login.jsp';
        return false;
    }
    return true;
}


/**
 * 退出登录
 * 调用 /api/auth/logout 销毁服务端 Session，然后跳转回登录页。
 */
async function logout() {
    await fetch(ctx + '/api/auth/logout', {method: 'POST', credentials: 'same-origin'});
    location.href = ctx + '/login.jsp';
}


/* ==================== 页面导航切换 ==================== */

/**
 * 绑定侧边栏导航点击事件
 * 为每个 .nav-link 按钮添加 click 事件监听器，点击时调用 showSection 切换对应模块。
 */
function bindNavigation() {
    document.querySelectorAll('.nav-link').forEach(button => {
        button.addEventListener('click', () => {
            showSection(button.dataset.target);
        });
    });
}


/**
 * 切换管理模块显示
 * 1. 高亮当前导航按钮（添加 active 类）。
 * 2. 显示目标 section（添加 active-page 类），隐藏其他 section。
 * 3. 平滑滚动到页面顶部。
 */
function showSection(targetId) {
    document.querySelectorAll('.nav-link').forEach(item => item.classList.toggle('active', item.dataset.target === targetId));
    document.querySelectorAll('.admin-section').forEach(section => section.classList.toggle('active-page', section.id === targetId));
    window.scrollTo({top: 0, behavior: 'smooth'});
}


/* ==================== 数据概览（Dashboard） ==================== */

/**
 * 生成单个指标卡片的 HTML
 * 用于 loadDashboard 中批量生成用户总数、景点数、订单数等统计卡片。
 */
function metric(label, value) {
    return `<div class="metric-card"><strong>${value}</strong><span>${label}</span></div>`;
}


/**
 * 加载数据概览
 * 调用 /api/admin/dashboard/summary 获取全局统计，渲染 9 个指标卡片：
 * 注册用户、景点数量、酒店数量、门票数量、路线数量、订单总数、
 * 酒店订单数、门票订单数、有效收入。
 */
async function loadDashboard() {
    const data = await request(ctx + '/api/admin/dashboard/summary');
    const d = data.data || {};
    el('dashboard').innerHTML = [
        metric('\u6ce8\u518c\u7528\u6237', d.userCount || 0),
        metric('\u666f\u70b9\u6570\u91cf', d.scenicCount || 0),
        metric('\u9152\u5e97\u6570\u91cf', d.hotelCount || 0),
        metric('\u95e8\u7968\u6570\u91cf', d.ticketCount || 0),
        metric('\u8def\u7ebf\u6570\u91cf', d.routeCount || 0),
        metric('\u8ba2\u5355\u603b\u6570', d.orderCount || 0),
        metric('\u9152\u5e97\u8ba2\u5355', d.hotelOrderCount || 0),
        metric('\u95e8\u7968\u8ba2\u5355', d.ticketOrderCount || 0),
        metric('\u6709\u6548\u6536\u5165', money(d.totalRevenue))
    ].join('');
}


/* ==================== 订单管理 ==================== */

/**
 * 订单类型中文映射
 * HOTEL → 酒店订单，其他 → 门票订单。
 */
function orderTypeLabel(type) {
    return type === 'HOTEL' ? '\u9152\u5e97\u8ba2\u5355' : '\u95e8\u7968\u8ba2\u5355';
}


/**
 * 订单状态中文映射
 * 将 CREATED/PAID/USED/CANCELLED/FINISHED 转为对应中文标签，未知状态原样返回。
 */
function statusLabel(status) {
    const labels = {
        CREATED: '\u5f85\u652f\u4ed8',
        PAID: '\u5df2\u652f\u4ed8',
        USED: '\u5df2\u4f7f\u7528',
        CANCELLED: '\u5df2\u53d6\u6d88',
        FINISHED: '\u5df2\u5b8c\u6210'
    };
    return labels[status] || status || '\u672a\u77e5\u72b6\u6001';
}


/**
 * 订单状态颜色类映射
 * 成功状态（PAID/USED/FINISHED）→ green，取消 → red，其他 → orange。
 */
function statusClass(status) {
    if (status === 'PAID' || status === 'USED' || status === 'FINISHED') return 'green';
    if (status === 'CANCELLED') return 'red';
    return 'orange';
}


/**
 * 加载订单列表
 * 调用 /api/admin/order/list 获取所有订单（酒店 + 门票），缓存到 orderItems。
 */
async function loadOrders() {
    const data = await request(ctx + '/api/admin/order/list');
    orderItems = data.data || [];
    renderOrders();
}


/**
 * 渲染订单列表
 * 1. 读取 orderFilter 下拉框的筛选值（ALL/HOTEL/TICKET/CREATED/PAID/USED/CANCELLED）。
 * 2. 根据类型和状态双重过滤，再叠加全局关键词搜索。
 * 3. 渲染分页卡片，每个卡片包含：类型编号、状态徽章、用户信息、项目名称、金额、日期、操作按钮。
 * 4. 酒店订单提供"设为已支付"和"取消订单"；门票订单提供"设为已使用"和"取消订单"。
 */
function renderOrders() {
    const filter = el('orderFilter')?.value || 'ALL';
    const list = orderItems
        .filter(item => filter === 'ALL' || item.orderType === filter || item.orderStatus === filter)
        .filter(item => hasKeyword(item, ['username', 'itemName', 'orderType', 'orderStatus']));
    renderPaged('orders', 'orders', list, item => `
        <div class="data-card">
            <div class="card-title">
                <strong>${orderTypeLabel(item.orderType)} #${item.id}</strong>
                <span class="badge ${statusClass(item.orderStatus)}">${statusLabel(item.orderStatus)}</span>
            </div>
            <div class="muted">\u7528\u6237\uff1a${esc(item.username || '\u6e38\u5ba2')} \uff5c \u9879\u76ee\uff1a${esc(item.itemName || '-')}</div>
            <div class="muted">\u4f7f\u7528\u65e5\u671f\uff1a${esc(item.useDate || '-')} \uff5c \u91d1\u989d\uff1a${money(item.totalAmount)}</div>
            <div class="muted">\u6ce8\u518c\u65f6\u95f4\uff1a${esc(item.createTime || '-')}</div>
            <div class="card-actions">
                ${item.orderType === 'HOTEL'
                    ? `<button class="enable" onclick="updateHotelOrderStatus(${item.id}, 'PAID')">\u8bbe\u4e3a\u5df2\u652f\u4ed8</button><button class="delete" onclick="updateHotelOrderStatus(${item.id}, 'CANCELLED')">\u53d6\u6d88\u8ba2\u5355</button>`
                    : `<button class="enable" onclick="updateTicketOrderStatus(${item.id}, 'USED')">\u8bbe\u4e3a\u5df2\u4f7f\u7528</button><button class="delete" onclick="updateTicketOrderStatus(${item.id}, 'CANCELLED')">\u53d6\u6d88\u8ba2\u5355</button>`}
            </div>
        </div>`, '\u6682\u65e0\u5339\u914d\u8ba2\u5355', 'renderOrders');
}




/* ==================== 用户管理 ==================== */

/**
 * 加载用户列表
 * 调用 /api/admin/user/list 获取所有注册用户，缓存到 userItems。
 */
async function loadUsers() {
    const data = await request(ctx + '/api/admin/user/list');
    userItems = data.data || [];
    renderUsers();
}


/**
 * 渲染用户列表
 * 支持按用户名、手机号、邮箱、角色搜索过滤。
 * 每个用户卡片显示：用户名、启用/禁用状态、角色、手机号、邮箱、注册时间、操作按钮。
 */
function renderUsers() {
    const list = userItems.filter(item => hasKeyword(item, ['username', 'phone', 'email', 'role']));
    renderPaged('userAdmin', 'users', list, item => {
        const enabled = Number(item.status) === 1;
        return `
            <div class="data-card">
                <div class="card-title">
                    <strong>${esc(item.username)}</strong>
                    <span class="badge ${enabled ? 'green' : 'red'}">${enabled ? '\u542f\u7528' : '\u7981\u7528'}</span>
                </div>
                <div class="muted">\u89d2\u8272\uff1a${item.role === 'ADMIN' ? '\u7ba1\u7406\u5458' : '\u666e\u901a\u7528\u6237'}</div>
                <div class="muted">\u624b\u673a\u53f7\uff1a${esc(item.phone || '-')} \uff5c \u90ae\u7bb1\uff1a${esc(item.email || '-')}</div>
                <div class="muted">\u6ce8\u518c\u65f6\u95f4\uff1a${esc(item.createTime || '-')}</div>
                <div class="card-actions">
                    <button class="edit" onclick="editUser(${item.id})">\u7f16\u8f91</button>
                    <button class="${enabled ? 'disable' : 'enable'}" onclick="toggleUserStatus(${item.id}, ${enabled ? 0 : 1})">${enabled ? '\u542f\u7528' : '\u7981\u7528'}</button>
                </div>
            </div>`;
    }, '\u6682\u65e0\u5339\u914d\u7528\u6237', 'renderUsers');
}



/**
 * 组装用户表单数据
 * 从用户编辑表单读取 id、username、phone、email、role、status，用于 updateUser 请求体。
 */
function userPayload() {
    return {
        id: Number(el('userId').value || 0),
        username: el('username').value,
        phone: el('userPhone').value,
        email: el('userEmail').value,
        role: el('userRole').value,
        status: Number(el('userStatus').value || 1)
    };
}


/**
 * 回填用户编辑表单
 * 从 userItems 缓存中查找对应用户，将数据写入表单输入框，方便管理员修改。
 */
function editUser(id) {
    const item = userItems.find(user => user.id === id);
    if (!item) return;
    setValue('userId', item.id);
    setValue('username', item.username);
    setValue('userPhone', item.phone);
    setValue('userEmail', item.email);
    setValue('userRole', item.role || 'USER');
    setValue('userStatus', item.status == null ? '1' : item.status);
    message('userResult', '\u5df2\u56de\u586b\u7528\u6237\u8d44\u6599\uff0c\u53ef\u4ee5\u4fee\u6539\u624b\u673a\u53f7\u3001\u90ae\u7bb1\u3001\u89d2\u8272\u548c\u72b6\u6001\u3002');
    scrollToInput('userPhone');
}


/**
 * 清空用户编辑表单
 * 重置所有输入框为默认值，role 恢复为 USER，status 恢复为 1（启用）。
 */
function clearUserForm() {
    ['userId', 'username', 'userPhone', 'userEmail'].forEach(id => setValue(id, ''));
    setValue('userRole', 'USER');
    setValue('userStatus', '1');
    message('userResult', '\u5df2\u6e05\u7a7a\u7528\u6237\u8868\u5355\u3002');
}


/**
 * 保存用户修改
 * 调用 /api/admin/user/update（PUT），提交 userPayload 数据。
 * 成功后刷新用户列表和数据概览，并显示成功提示。
 */
async function updateUser() {
    if (!el('userId').value) {
        message('userResult', '\u8bf7\u5148\u4ece\u53f3\u4fa7\u7528\u6237\u5217\u8868\u9009\u62e9\u4e00\u4e2a\u7528\u6237\u3002', true);
        return;
    }
    try {
        const data = await request(ctx + '/api/admin/user/update', {
            method: 'PUT',
            headers: {'Content-Type': 'application/json;charset=UTF-8'},
            body: JSON.stringify(userPayload())
        });
        message('userResult', normalizeMessage(data, '\u7528\u6237\u8d44\u6599\u4fee\u6539\u6210\u529f'));
        await Promise.all([loadUsers(), loadDashboard()]);
    } catch (err) {
        message('userResult', err.message, true);
    }
}


/**
 * 切换用户启用/禁用状态
 * 调用 /api/admin/user/status（POST），传入 id 和 target status。
 * 操作前弹出确认对话框，成功后刷新列表和概览。
 */
async function toggleUserStatus(id, status) {
    if (!confirm(`\u786e\u5b9a${status === 1 ? '\u542f\u7528' : '\u7981\u7528'}\u8fd9\u4e2a\u7528\u6237\u5417\uff1f`)) return;
    try {
        const data = await request(ctx + '/api/admin/user/status?id=' + id + '&status=' + status, {method: 'POST'});
        message('userResult', normalizeMessage(data, status === 1 ? '\u7528\u6237\u5df2\u542f\u7528' : '\u7528\u6237\u5df2\u7981\u7528'));
        await Promise.all([loadUsers(), loadDashboard()]);
    } catch (err) {
        message('userResult', err.message, true);
    }
}



/* ==================== 景点管理 ==================== */

/**
 * 加载景点列表
 * 调用 /api/admin/scenic/list 获取所有景点，缓存到 scenicItems。
 * 同时初始化路线编排器中的景点下拉框（initRouteSpotSelect）和点位预览。
 */
async function loadScenic() {
    const data = await request(ctx + '/api/admin/scenic/list');
    scenicItems = data.data || [];
    initRouteSpotSelect();
    renderRouteSpotPreview();
    renderScenic();
}


/**
 * 渲染景点列表
 * 支持按景点名称、城市、分类、标签、介绍搜索过滤。
 * 每个卡片显示：名称、城市、分类、评分、热度、价格、经纬度、操作按钮（加入路线、编辑、删除）。
 */
function renderScenic() {
    const list = scenicItems.filter(item => hasKeyword(item, ['scenicName', 'city', 'category', 'tags', 'description']));
    renderPaged('scenicAdmin', 'scenic', list, item => `
        <div class="data-card">
            <div class="card-title">
                <strong>${esc(item.scenicName)}</strong>
                <span class="badge">${esc(item.city || '-')}</span>
            </div>
            <div class="muted">${esc(item.category || '\u666f\u70b9')} \uff5c \u8bc4\u5206\uff1a${esc(item.score || '-')} \uff5c \u70ed\u5ea6\uff1a${esc(item.popularity || 0)}</div>
            <div class="muted">\u4ef7\u683c\uff1a${money(item.price)} \uff5c \u5750\u6807\uff1a${esc(item.longitude || '-')}, ${esc(item.latitude || '-')}</div>
            <div class="card-actions">
                <button class="route" onclick="quickAddScenicToRoute(${item.id})">\u52a0\u5165\u8def\u7ebf</button>
                <button class="edit" onclick="editScenic(${item.id})">\u7f16\u8f91</button>
                <button class="delete" onclick="deleteScenic(${item.id})">\u5220\u9664</button>
            </div>
        </div>`, '\u6682\u65e0\u5339\u914d\u666f\u70b9', 'renderScenic');
}



/**
 * 组装景点表单数据
 * 从景点编辑表单读取所有字段，withId 为 true 时额外包含 id（用于修改）。
 */
function scenicPayload(withId) {
    const payload = {
        scenicName: el('scenicName').value.trim(),
        city: el('scenicCity').value.trim(),
        category: el('scenicCategory').value.trim(),
        description: el('scenicDesc').value.trim(),
        price: Number(el('scenicPrice').value || 0),
        score: Number(el('scenicScore').value || 0),
        popularity: Number(el('scenicPopularity').value || 0),
        tags: el('scenicTags').value.trim(),
        longitude: Number(el('scenicLongitude').value || 0),
        latitude: Number(el('scenicLatitude').value || 0)
    };
    if (withId) payload.id = Number(el('scenicId').value || 0);
    return payload;
}


/**
 * 回填景点编辑表单
 * 从 scenicItems 缓存中查找对应景点，将所有字段写入表单，方便管理员修改。
 */
function editScenic(id) {
    const item = scenicItems.find(s => s.id === id);
    if (!item) return;
    setValue('scenicId', item.id);
    setValue('scenicName', item.scenicName);
    setValue('scenicCity', item.city);
    setValue('scenicCategory', item.category);
    setValue('scenicPrice', item.price);
    setValue('scenicScore', item.score);
    setValue('scenicPopularity', item.popularity);
    setValue('scenicTags', item.tags);
    setValue('scenicLongitude', item.longitude);
    setValue('scenicLatitude', item.latitude);
    setValue('scenicDesc', item.description);
    message('scenicResult', '\u5df2\u56de\u586b\u666f\u70b9\u4fe1\u606f\uff0c\u4fee\u6539\u540e\u70b9\u51fb\u201c\u4fee\u6539\u666f\u70b9\u201d\u3002');
    scrollToInput('scenicName');
}


/**
 * 清空景点编辑表单
 * 重置所有输入框为空，用于重新录入新景点。
 */
function clearScenicForm() {
    ['scenicId', 'scenicName', 'scenicCity', 'scenicCategory', 'scenicPrice', 'scenicScore', 'scenicPopularity', 'scenicTags', 'scenicLongitude', 'scenicLatitude', 'scenicDesc'].forEach(id => setValue(id, ''));
    message('scenicResult', '\u5df2\u6e05\u7a7a\u666f\u70b9\u8868\u5355\u3002');
}


/**
 * 新增景点
 * 调用 /api/admin/scenic/save（POST），提交 scenicPayload（不含 id）。
 * 成功后刷新景点列表和数据概览。
 */
async function saveScenic() {
    try {
        const data = await request(ctx + '/api/admin/scenic/save', {
            method: 'POST',
            headers: {'Content-Type': 'application/json;charset=UTF-8'},
            body: JSON.stringify(scenicPayload(false))
        });
        message('scenicResult', normalizeMessage(data, '\u666f\u70b9\u4fdd\u5b58\u6210\u529f'));
        await Promise.all([loadScenic(), loadDashboard()]);
    } catch (err) {
        message('scenicResult', err.message, true);
    }
}


/**
 * 修改景点
 * 调用 /api/admin/scenic/update（PUT），提交 scenicPayload（含 id）。
 * 若未填写景点编号则提示错误。
 */
async function updateScenic() {
    if (!el('scenicId').value) {
        message('scenicResult', '\u8bf7\u5148\u9009\u62e9\u6216\u586b\u5199\u666f\u70b9\u7f16\u53f7\u3002', true);
        return;
    }
    try {
        const data = await request(ctx + '/api/admin/scenic/update', {
            method: 'PUT',
            headers: {'Content-Type': 'application/json;charset=UTF-8'},
            body: JSON.stringify(scenicPayload(true))
        });
        message('scenicResult', normalizeMessage(data, '\u666f\u70b9\u4fee\u6539\u6210\u529f'));
        await loadScenic();
    } catch (err) {
        message('scenicResult', err.message, true);
    }
}


/**
 * 删除景点
 * 调用 /api/admin/scenic/delete/{id}（DELETE）。
 * 操作前弹出确认对话框，提示关联门票和路线可能受影响。
 */
async function deleteScenic(id) {
    if (!confirm('\u786e\u5b9a\u5220\u9664\u8fd9\u4e2a\u666f\u70b9\u5417\uff1f\u5173\u8054\u95e8\u7968\u6216\u8def\u7ebf\u53ef\u80fd\u53d7\u5f71\u54cd\u3002')) return;
    try {
        const data = await request(ctx + '/api/admin/scenic/delete/' + id, {method: 'DELETE'});
        message('scenicResult', normalizeMessage(data, '\u666f\u70b9\u5220\u9664\u6210\u529f'));
        await Promise.all([loadScenic(), loadDashboard()]);
    } catch (err) {
        message('scenicResult', err.message, true);
    }
}



/* ==================== 酒店管理 ==================== */

/**
 * 加载酒店列表
 * 调用 /api/admin/hotel/list 获取所有酒店，缓存到 hotelItems。
 */
async function loadHotels() {
    const data = await request(ctx + '/api/admin/hotel/list');
    hotelItems = data.data || [];
    renderHotels();
}


/**
 * 渲染酒店列表
 * 支持按酒店名称、城市、等级、地址、描述搜索过滤。
 * 每个卡片显示：名称、启用/停用状态、城市、等级、地址、操作按钮（编辑、删除）。
 */
function renderHotels() {
    const list = hotelItems.filter(item => hasKeyword(item, ['hotelName', 'city', 'level', 'address', 'description']));
    renderPaged('hotelAdmin', 'hotels', list, item => `
        <div class="data-card">
            <div class="card-title">
                <strong>${esc(item.hotelName)}</strong>
                <span class="badge ${Number(item.status) === 1 ? 'green' : 'red'}">${Number(item.status) === 1 ? '\u542f\u7528' : '\u505c\u7528'}</span>
            </div>
            <div class="muted">${esc(item.city || '-')} \uff5c ${esc(item.level || '\u8212\u9002\u578b')}</div>
            <div class="muted">${esc(item.address || '-')}</div>
            <div class="card-actions">
                <button class="edit" onclick="editHotel(${item.id})">\u7f16\u8f91</button>
                <button class="delete" onclick="deleteHotel(${item.id})">\u5220\u9664</button>
            </div>
        </div>`, '\u6682\u65e0\u5339\u914d\u9152\u5e97', 'renderHotels');
}



/**
 * 组装酒店表单数据
 * 从酒店编辑表单读取名称、城市、等级、状态、地址、描述，withId 为 true 时包含 id。
 */
function hotelPayload(withId) {
    const payload = {
        hotelName: el('hotelName').value.trim(),
        city: el('hotelCity').value.trim(),
        level: el('hotelLevel').value.trim(),
        status: Number(el('hotelStatus').value || 1),
        address: el('hotelAddress').value.trim(),
        description: el('hotelDesc').value.trim()
    };
    if (withId) payload.id = Number(el('hotelId').value || 0);
    return payload;
}


/**
 * 回填酒店编辑表单
 * 从 hotelItems 缓存中查找对应酒店，将所有字段写入表单。
 */
function editHotel(id) {
    const item = hotelItems.find(h => h.id === id);
    if (!item) return;
    setValue('hotelId', item.id);
    setValue('hotelName', item.hotelName);
    setValue('hotelCity', item.city);
    setValue('hotelLevel', item.level);
    setValue('hotelStatus', item.status == null ? 1 : item.status);
    setValue('hotelAddress', item.address);
    setValue('hotelDesc', item.description);
    message('hotelResult', '\u5df2\u56de\u586b\u9152\u5e97\u4fe1\u606f\uff0c\u4fee\u6539\u540e\u70b9\u51fb\u201c\u4fee\u6539\u9152\u5e97\u201d\u3002');
    scrollToInput('hotelName');
}


/**
 * 清空酒店编辑表单
 * 重置输入框并恢复 status 为 1（启用）。
 */
function clearHotelForm() {
    ['hotelId', 'hotelName', 'hotelCity', 'hotelLevel', 'hotelAddress', 'hotelDesc'].forEach(id => setValue(id, ''));
    setValue('hotelStatus', '1');
    message('hotelResult', '\u5df2\u6e05\u7a7a\u9152\u5e97\u8868\u5355\u3002');
}


/**
 * 新增酒店
 * 调用 /api/admin/hotel/save（POST），提交 hotelPayload（不含 id）。
 */
async function saveHotel() {
    try {
        const data = await request(ctx + '/api/admin/hotel/save', {
            method: 'POST',
            headers: {'Content-Type': 'application/json;charset=UTF-8'},
            body: JSON.stringify(hotelPayload(false))
        });
        message('hotelResult', normalizeMessage(data, '\u9152\u5e97\u4fdd\u5b58\u6210\u529f'));
        await Promise.all([loadHotels(), loadDashboard()]);
    } catch (err) {
        message('hotelResult', err.message, true);
    }
}


/**
 * 修改酒店
 * 调用 /api/admin/hotel/update（PUT），提交 hotelPayload（含 id）。
 */
async function updateHotel() {
    if (!el('hotelId').value) {
        message('hotelResult', '\u8bf7\u5148\u9009\u62e9\u6216\u586b\u5199\u9152\u5e97\u7f16\u53f7\u3002', true);
        return;
    }
    try {
        const data = await request(ctx + '/api/admin/hotel/update', {
            method: 'PUT',
            headers: {'Content-Type': 'application/json;charset=UTF-8'},
            body: JSON.stringify(hotelPayload(true))
        });
        message('hotelResult', normalizeMessage(data, '\u9152\u5e97\u4fee\u6539\u6210\u529f'));
        await loadHotels();
    } catch (err) {
        message('hotelResult', err.message, true);
    }
}


/**
 * 删除酒店
 * 调用 /api/admin/hotel/delete/{id}（DELETE）。
 * 操作前提示关联房型和订单展示可能受影响。
 */
async function deleteHotel(id) {
    if (!confirm('\u786e\u5b9a\u5220\u9664\u8fd9\u4e2a\u9152\u5e97\u5417\uff1f\u5173\u8054\u623f\u578b\u548c\u8ba2\u5355\u5c55\u793a\u53ef\u80fd\u53d7\u5f71\u54cd\u3002')) return;
    try {
        const data = await request(ctx + '/api/admin/hotel/delete/' + id, {method: 'DELETE'});
        message('hotelResult', normalizeMessage(data, '\u9152\u5e97\u5220\u9664\u6210\u529f'));
        await Promise.all([loadHotels(), loadDashboard()]);
    } catch (err) {
        message('hotelResult', err.message, true);
    }
}



/* ==================== 门票管理 ==================== */

/**
 * 加载门票列表
 * 调用 /api/admin/ticket/list 获取所有门票，缓存到 ticketItems。
 */
async function loadTickets() {
    const data = await request(ctx + '/api/admin/ticket/list');
    ticketItems = data.data || [];
    renderTickets();
}


/**
 * 根据景点 ID 查询景点名称
 * 从 scenicItems 缓存中查找，用于门票列表中显示所属景点名称。
 * 若找不到，返回"景点 #id"作为占位。
 */
function scenicNameById(id) {
    const scenic = scenicItems.find(item => Number(item.id) === Number(id));
    return scenic ? scenic.scenicName : ('\u666f\u70b9 #' + id);
}


/**
 * 渲染门票列表
 * 支持按门票名称、所属景点名称、可用日期搜索过滤。
 * 每个卡片显示：名称、库存状态（绿色=有库存，红色=无库存）、所属景点、价格、可用日期、操作按钮（编辑、删除）。
 */
function renderTickets() {
    const list = ticketItems.filter(item => {
        const scenicName = scenicNameById(item.scenicId);
        return hasKeyword({...item, scenicName}, ['ticketName', 'scenicName', 'availableDate']);
    });
    renderPaged('ticketAdmin', 'tickets', list, item => `
        <div class="data-card">
            <div class="card-title">
                <strong>${esc(item.ticketName)}</strong>
                <span class="badge ${Number(item.stock) > 0 ? 'green' : 'red'}">\u5e93\u5b58 ${esc(item.stock || 0)}</span>
            </div>
            <div class="muted">\u6240\u5c5e\u666f\u70b9\uff1a${esc(scenicNameById(item.scenicId))}\uff08#${esc(item.scenicId)}\uff09</div>
            <div class="muted">\u4ef7\u683c\uff1a${money(item.price)} \uff5c \u53ef\u7528\u65e5\u671f\uff1a${esc(item.availableDate || '-')}</div>
            <div class="card-actions">
                <button class="edit" onclick="editTicket(${item.id})">\u7f16\u8f91</button>
                <button class="delete" onclick="deleteTicket(${item.id})">\u5220\u9664</button>
            </div>
        </div>`, '\u6682\u65e0\u5339\u914d\u95e8\u7968', 'renderTickets');
}



/**
 * 组装门票表单数据
 * 从门票编辑表单读取 scenicId、名称、价格、库存、可用日期，withId 为 true 时包含 id。
 */
function ticketPayload(withId) {
    const payload = {
        scenicId: Number(el('ticketScenicId').value || 0),
        ticketName: el('ticketName').value.trim(),
        price: Number(el('ticketPrice').value || 0),
        stock: Number(el('ticketStock').value || 0),
        availableDate: el('ticketDate').value || today()
    };
    if (withId) payload.id = Number(el('ticketId').value || 0);
    return payload;
}


/**
 * 回填门票编辑表单
 * 从 ticketItems 缓存中查找对应门票，将所有字段写入表单。
 */
function editTicket(id) {
    const item = ticketItems.find(t => t.id === id);
    if (!item) return;
    setValue('ticketId', item.id);
    setValue('ticketScenicId', item.scenicId);
    setValue('ticketName', item.ticketName);
    setValue('ticketPrice', item.price);
    setValue('ticketStock', item.stock);
    setValue('ticketDate', item.availableDate);
    message('ticketResult', '\u5df2\u56de\u586b\u95e8\u7968\u4fe1\u606f\uff0c\u4fee\u6539\u540e\u70b9\u51fb\u201c\u4fee\u6539\u95e8\u7968\u201d\u3002');
    scrollToInput('ticketName');
}


/**
 * 清空门票编辑表单
 * 重置输入框并恢复可用日期为今天。
 */
function clearTicketForm() {
    ['ticketId', 'ticketScenicId', 'ticketName', 'ticketPrice', 'ticketStock'].forEach(id => setValue(id, ''));
    /* 初始化：设置门票表单默认日期为今天 */
setValue('ticketDate', today());
    message('ticketResult', '\u5df2\u6e05\u7a7a\u95e8\u7968\u8868\u5355\u3002');
}


/**
 * 新增门票
 * 调用 /api/admin/ticket/save（POST），提交 ticketPayload（不含 id）。
 */
async function saveTicket() {
    try {
        const data = await request(ctx + '/api/admin/ticket/save', {
            method: 'POST',
            headers: {'Content-Type': 'application/json;charset=UTF-8'},
            body: JSON.stringify(ticketPayload(false))
        });
        message('ticketResult', normalizeMessage(data, '\u95e8\u7968\u4fdd\u5b58\u6210\u529f'));
        await Promise.all([loadTickets(), loadDashboard()]);
    } catch (err) {
        message('ticketResult', err.message, true);
    }
}


/**
 * 修改门票
 * 调用 /api/admin/ticket/update（PUT），提交 ticketPayload（含 id）。
 */
async function updateTicket() {
    if (!el('ticketId').value) {
        message('ticketResult', '\u8bf7\u5148\u9009\u62e9\u6216\u586b\u5199\u95e8\u7968\u7f16\u53f7\u3002', true);
        return;
    }
    try {
        const data = await request(ctx + '/api/admin/ticket/update', {
            method: 'PUT',
            headers: {'Content-Type': 'application/json;charset=UTF-8'},
            body: JSON.stringify(ticketPayload(true))
        });
        message('ticketResult', normalizeMessage(data, '\u95e8\u7968\u4fee\u6539\u6210\u529f'));
        await loadTickets();
    } catch (err) {
        message('ticketResult', err.message, true);
    }
}


/**
 * 删除门票
 * 调用 /api/admin/ticket/delete/{id}（DELETE）。
 * 操作前提示已有订单可能无法继续展示门票名称。
 */
async function deleteTicket(id) {
    if (!confirm('\u786e\u5b9a\u5220\u9664\u8fd9\u4e2a\u95e8\u7968\u5417\uff1f\u5df2\u6709\u8ba2\u5355\u53ef\u80fd\u65e0\u6cd5\u7ee7\u7eed\u5c55\u793a\u95e8\u7968\u540d\u79f0\u3002')) return;
    try {
        const data = await request(ctx + '/api/admin/ticket/delete/' + id, {method: 'DELETE'});
        message('ticketResult', normalizeMessage(data, '\u95e8\u7968\u5220\u9664\u6210\u529f'));
        await Promise.all([loadTickets(), loadDashboard()]);
    } catch (err) {
        message('ticketResult', err.message, true);
    }
}



/* ==================== 路线管理 ==================== */

/**
 * 加载路线列表
 * 调用 /api/admin/route/list 获取所有路线，缓存到 routeItems。
 */
async function loadRoutes() {
    const data = await request(ctx + '/api/admin/route/list');
    routeItems = data.data || [];
    renderRoutes();
}


/**
 * 渲染路线列表
 * 支持按路线名称、城市、主题、描述搜索过滤。
 * 每个卡片显示：名称、城市、天数、预算、主题、点位列表（第X天第X站）、操作按钮（编辑、删除）。
 */
function renderRoutes() {
    const list = routeItems.filter(item => {
        const route = item.route || {};
        return hasKeyword(route, ['routeName', 'city', 'theme', 'routeDesc']);
    });
    renderPaged('routeAdmin', 'routes', list, item => {
        const route = item.route || {};
        const spots = (item.spots || [])
            .slice()
            .sort((a, b) => a.dayNo - b.dayNo || a.sortNo - b.sortNo)
            .map(s => `${scenicNameById(s.scenicId)}\uff1a\u7b2c${s.dayNo}\u5929\u7b2c${s.sortNo}\u7ad9`)
            .join(' / ');
        return `
            <div class="data-card">
                <div class="card-title">
                    <strong>${esc(route.routeName)}</strong>
                    <span class="badge">${esc(route.city || '-')}</span>
                </div>
                <div class="muted">\u5929\u6570\uff1a${esc(route.days || 0)} \u5929 \uff5c \u9884\u7b97\uff1a${money(route.budget)} \uff5c \u4e3b\u9898\uff1a${esc(route.theme || '-')}</div>
                <div class="muted">\u70b9\u4f4d\uff1a${esc(spots || '\u6682\u672a\u914d\u7f6e\u70b9\u4f4d')}</div>
                <div class="card-actions">
                    <button class="edit" onclick="editRoute(${route.id})">\u7f16\u8f91</button>
                    <button class="delete" onclick="deleteRoute(${route.id})">\u5220\u9664</button>
                </div>
            </div>`;
    }, '\u6682\u65e0\u5339\u914d\u8def\u7ebf', 'renderRoutes');
}



/* ==================== 路线点位编排器 ==================== */

/**
 * 初始化景点下拉框
 * 将 scenicItems 缓存中的所有景点填充到 routeSpotScenicSelect 下拉框中，
 * 格式为"城市-景点名称"，供管理员选择添加到路线。
 */
function initRouteSpotSelect() {
    const select = el('routeSpotScenicSelect');
    if (!select) return;
    select.innerHTML = scenicItems
        .map(item => `<option value="${item.id}">${esc(item.city || '-')}-${esc(item.scenicName)}</option>`)
        .join('');
    fillRouteSpotSuggest();
}


/**
 * 计算下一个点位推荐位置
 * 根据当前路线中的点位草稿（routeSpotDraft），自动推算下一个合理的"第几天"和"第几站"，
 * 用于 fillRouteSpotSuggest 智能补位功能。
 */
function getRouteSpotNextPosition(dayValue) {
    const numericDay = Number(dayValue || 0);
    if (routeSpotDraft.length === 0) {
        return {dayNo: numericDay || 1, sortNo: 1};
    }
    const ordered = [...routeSpotDraft].sort((a, b) => a.dayNo - b.dayNo || a.sortNo - b.sortNo);
    const dayNo = numericDay || ordered[ordered.length - 1].dayNo;
    const sameDay = ordered.filter(item => item.dayNo === dayNo);
    if (sameDay.length === 0) return {dayNo, sortNo: 1};
    return {dayNo, sortNo: Math.max(...sameDay.map(item => item.sortNo)) + 1};
}


/**
 * 智能补位
 * 自动将 routeSpotDayInput 和 routeSpotSortInput 填充为下一个推荐位置，
 * 减少管理员手动输入的工作。
 */
function fillRouteSpotSuggest(dayValue) {
    const next = getRouteSpotNextPosition(dayValue || el('routeSpotDayInput')?.value);
    setValue('routeSpotDayInput', next.dayNo);
    setValue('routeSpotSortInput', next.sortNo);
}


/**
 * 同步点位草稿到隐藏输入框
 * 将 routeSpotDraft 数组转换为"景点ID-第几天-排序"的逗号分隔字符串，
 * 写入 routeSpots 输入框，随表单提交到后端。
 */
function syncRouteSpotInput() {
    el('routeSpots').value = routeSpotDraft
        .map(item => `${item.scenicId}-${item.dayNo}-${item.sortNo}`)
        .join(',');
}


/**
 * 渲染点位预览列表
 * 将 routeSpotDraft 中的点位按天数和站次排序，生成可视化列表，
 * 显示每个点位的景点名称、第几天、第几站，并提供移除按钮。
 */
function renderRouteSpotPreview() {
    const preview = el('routeSpotPreview');
    if (!preview) return;
    if (routeSpotDraft.length === 0) {
        preview.innerHTML = '<div class="empty-state">\u8fd8\u6ca1\u6709\u6dfb\u52a0\u8def\u7ebf\u70b9\u4f4d\u3002\u4f60\u53ef\u4ee5\u9009\u62e9\u666f\u70b9\u3001\u5929\u6570\u548c\u7ad9\u6b21\u6765\u5feb\u901f\u7f16\u6392\u3002</div>';
        syncRouteSpotInput();
        return;
    }
    const ordered = [...routeSpotDraft].sort((a, b) => a.dayNo - b.dayNo || a.sortNo - b.sortNo);
    preview.innerHTML = ordered.map((item, index) => `
        <div class="spot-item">
            <div>
                <strong>${index + 1}. ${esc(scenicNameById(item.scenicId))}</strong>
                <div class="muted">\u7b2c${item.dayNo}\u5929 \uff5c \u7b2c${item.sortNo}\u7ad9</div>
            </div>
            <button class="danger-btn" type="button" onclick="removeRouteSpot(${item.scenicId}, ${item.dayNo}, ${item.sortNo})">\u79fb\u9664</button>
        </div>
    `).join('');
    syncRouteSpotInput();
}


/**
 * 添加路线点位
 * 1. 读取当前选中的景点、天数、站次。
 * 2. 校验必填项和同一天内站次重复。
 * 3. 将点位加入 routeSpotDraft 数组，重新排序并渲染预览。
 * 4. 自动保持当前景点选择并推荐下一个位置。
 */
function addRouteSpot(prefillScenicId) {
    const scenicId = Number(prefillScenicId || el('routeSpotScenicSelect').value || 0);
    const dayNo = Number(el('routeSpotDayInput').value || 0);
    const sortNo = Number(el('routeSpotSortInput').value || 0);
    if (!scenicId || !dayNo || !sortNo) {
        message('routeResult', '\u8bf7\u5148\u9009\u62e9\u666f\u70b9\uff0c\u5e76\u586b\u5199\u7b2c\u51e0\u5929\u548c\u7b2c\u51e0\u7ad9\u3002', true);
        return;
    }
    const exists = routeSpotDraft.some(item => item.dayNo === dayNo && item.sortNo === sortNo);
    if (exists) {
        message('routeResult', '\u540c\u4e00\u5929\u5185\u7684\u7ad9\u6b21\u4e0d\u80fd\u91cd\u590d\uff0c\u8bf7\u8c03\u6574\u6392\u5e8f\u3002', true);
        return;
    }
    routeSpotDraft.push({scenicId, dayNo, sortNo});
    routeSpotDraft.sort((a, b) => a.dayNo - b.dayNo || a.sortNo - b.sortNo);
    renderRouteSpotPreview();
    message('routeResult', '\u5df2\u6dfb\u52a0\u8def\u7ebf\u70b9\u4f4d\uff0c\u53ef\u4ee5\u7ee7\u7eed\u8865\u5145\u666f\u70b9\u3002');
    setValue('routeSpotScenicSelect', scenicId);
    fillRouteSpotSuggest(dayNo);
}


/**
 * 移除路线点位
 * 从 routeSpotDraft 中删除指定景点、天数、站次的点位，重新渲染预览并推荐下一个位置。
 */
function removeRouteSpot(scenicId, dayNo, sortNo) {
    routeSpotDraft = routeSpotDraft.filter(item => !(item.scenicId === scenicId && item.dayNo === dayNo && item.sortNo === sortNo));
    renderRouteSpotPreview();
    message('routeResult', '\u5df2\u79fb\u9664\u8be5\u8def\u7ebf\u70b9\u4f4d\u3002');
    fillRouteSpotSuggest(dayNo);
}


/**
 * 清空所有路线点位
 * 仅清空 routeSpotDraft 和预览，保留路线基本信息表单内容，方便重新编排。
 */
function clearRouteSpotsOnly() {
    routeSpotDraft = [];
    renderRouteSpotPreview();
    fillRouteSpotSuggest(1);
    message('routeResult', '\u5df2\u6e05\u7a7a\u8def\u7ebf\u70b9\u4f4d\uff0c\u53ef\u4ee5\u91cd\u65b0\u7f16\u6392\u3002');
}


/**
 * 快捷将景点加入路线
 * 在景点管理卡片中点击"加入路线"时调用，自动选中景点、添加点位并滚动到路线管理区域。
 */
function quickAddScenicToRoute(scenicId) {
    setValue('routeSpotScenicSelect', scenicId);
    fillRouteSpotSuggest();
    addRouteSpot(scenicId);
    el('routeSection')?.scrollIntoView({behavior: 'smooth', block: 'start'});
}


/**
 * 组装路线表单数据
 * 1. 读取路线基本信息（名称、城市、天数、预算、主题、描述）。
 * 2. 解析 routeSpots 输入框或 routeSpotDraft 数组，转为结构化 spot 对象数组。
 * 3. withId 为 true 时包含路线 id（用于修改）。
 */
function routePayload(withId) {
    const route = {
        routeName: el('routeName').value.trim(),
        city: el('routeCity').value.trim(),
        days: Number(el('routeDays').value || 0),
        budget: Number(el('routeBudget').value || 0),
        theme: el('routeTheme').value.trim(),
        routeDesc: el('routeDesc').value.trim()
    };
    if (withId) route.id = Number(el('routeId').value || 0);
    const rawSpots = routeSpotDraft.length > 0
        ? routeSpotDraft.map(item => `${item.scenicId}-${item.dayNo}-${item.sortNo}`).join(',')
        : (el('routeSpots').value || '');
    const spots = rawSpots
        .split(',')
        .map(s => s.trim())
        .filter(Boolean)
        .map(s => {
            const parts = s.split('-');
            return {
                scenicId: Number(parts[0]),
                dayNo: Number(parts[1]),
                sortNo: Number(parts[2])
            };
        })
        .filter(s => s.scenicId && s.dayNo && s.sortNo);
    return {route, spots};
}


/**
 * 回填路线编辑表单
 * 从 routeItems 缓存中查找对应路线，将基本信息和点位草稿写入表单，渲染预览。
 */
function editRoute(id) {
    const item = routeItems.find(r => r.route && r.route.id === id);
    if (!item) return;
    const route = item.route;
    setValue('routeId', route.id);
    setValue('routeName', route.routeName);
    setValue('routeCity', route.city);
    setValue('routeDays', route.days);
    setValue('routeBudget', route.budget);
    setValue('routeTheme', route.theme);
    setValue('routeDesc', route.routeDesc);
    routeSpotDraft = (item.spots || []).map(s => ({scenicId: s.scenicId, dayNo: s.dayNo, sortNo: s.sortNo}));
    renderRouteSpotPreview();
    fillRouteSpotSuggest(routeSpotDraft.length ? routeSpotDraft[routeSpotDraft.length - 1].dayNo : 1);
    message('routeResult', '\u5df2\u56de\u586b\u8def\u7ebf\u4fe1\u606f\uff0c\u4fee\u6539\u540e\u70b9\u51fb\u201c\u4fee\u6539\u8def\u7ebf\u201d\u3002');
    scrollToInput('routeName');
}


/**
 * 清空路线编辑表单
 * 重置所有基本信息输入框、清空点位草稿和预览，恢复推荐位置为第1天第1站。
 */
function clearRouteForm() {
    ['routeId', 'routeName', 'routeCity', 'routeDays', 'routeBudget', 'routeTheme', 'routeDesc', 'routeSpots'].forEach(id => setValue(id, ''));
    routeSpotDraft = [];
    renderRouteSpotPreview();
    fillRouteSpotSuggest(1);
    message('routeResult', '\u5df2\u6e05\u7a7a\u8def\u7ebf\u8868\u5355\u3002');
}


/**
 * 新增路线
 * 调用 /api/admin/route/save（POST），提交 routePayload（不含 id）。
 * 成功后清空表单并刷新路线列表和数据概览。
 */
async function saveRoute() {
    try {
        const data = await request(ctx + '/api/admin/route/save', {
            method: 'POST',
            headers: {'Content-Type': 'application/json;charset=UTF-8'},
            body: JSON.stringify(routePayload(false))
        });
        message('routeResult', normalizeMessage(data, '\u8def\u7ebf\u4fdd\u5b58\u6210\u529f'));
        clearRouteForm();
        await Promise.all([loadRoutes(), loadDashboard()]);
    } catch (err) {
        message('routeResult', err.message, true);
    }
}


/**
 * 修改路线
 * 调用 /api/admin/route/update（PUT），提交 routePayload（含 id）。
 * 成功后清空表单并刷新路线列表。
 */
async function updateRoute() {
    if (!el('routeId').value) {
        message('routeResult', '\u8bf7\u5148\u9009\u62e9\u6216\u586b\u5199\u8def\u7ebf\u7f16\u53f7\u3002', true);
        return;
    }
    try {
        const data = await request(ctx + '/api/admin/route/update', {
            method: 'PUT',
            headers: {'Content-Type': 'application/json;charset=UTF-8'},
            body: JSON.stringify(routePayload(true))
        });
        message('routeResult', normalizeMessage(data, '\u8def\u7ebf\u4fee\u6539\u6210\u529f'));
        clearRouteForm();
        await loadRoutes();
    } catch (err) {
        message('routeResult', err.message, true);
    }
}


/**
 * 删除路线
 * 调用 /api/admin/route/delete/{id}（DELETE）。
 * 操作前提示路线点位也会一起删除。
 */
async function deleteRoute(id) {
    if (!confirm('\u786e\u5b9a\u5220\u9664\u8fd9\u6761\u8def\u7ebf\u5417\uff1f\u8def\u7ebf\u70b9\u4f4d\u4e5f\u4f1a\u4e00\u8d77\u5220\u9664\u3002')) return;
    try {
        const data = await request(ctx + '/api/admin/route/delete/' + id, {method: 'DELETE'});
        message('routeResult', normalizeMessage(data, '\u8def\u7ebf\u5220\u9664\u6210\u529f'));
        await Promise.all([loadRoutes(), loadDashboard()]);
    } catch (err) {
        message('routeResult', err.message, true);
    }
}



/* ==================== 订单状态操作 ==================== */

/**
 * 更新酒店订单状态
 * 调用 /api/admin/order/hotel/status（POST），传入订单 id 和目标状态。
 * 支持将订单设为 PAID（已支付）或 CANCELLED（已取消）。
 */
async function updateHotelOrderStatus(id, status) {
    if (!confirm('\u786e\u5b9a\u66f4\u65b0\u8ba2\u5355\u72b6\u6001\u4e3a\u201c' + statusLabel(status) + '\u201d\u5417\uff1f')) return;
    try {
        const data = await request(ctx + '/api/admin/order/hotel/status?id=' + id + '&orderStatus=' + status, {method: 'POST'});
        alert(normalizeMessage(data, '\u9152\u5e97\u8ba2\u5355\u72b6\u6001\u5df2\u66f4\u65b0'));
        await Promise.all([loadOrders(), loadDashboard()]);
    } catch (err) {
        alert(err.message);
    }
}


/**
 * 更新门票订单状态
 * 调用 /api/admin/order/ticket/status（POST），传入订单 id 和目标状态。
 * 支持将订单设为 USED（已使用）或 CANCELLED（已取消）。
 */
async function updateTicketOrderStatus(id, status) {
    if (!confirm('\u786e\u5b9a\u66f4\u65b0\u8ba2\u5355\u72b6\u6001\u4e3a\u201c' + statusLabel(status) + '\u201d\u5417\uff1f')) return;
    try {
        const data = await request(ctx + '/api/admin/order/ticket/status?id=' + id + '&orderStatus=' + status, {method: 'POST'});
        alert(normalizeMessage(data, '\u95e8\u7968\u8ba2\u5355\u72b6\u6001\u5df2\u66f4\u65b0'));
        await Promise.all([loadOrders(), loadDashboard()]);
    } catch (err) {
        alert(err.message);
    }
}



/* ==================== 全局刷新与初始化 ==================== */

/**
 * 重新渲染所有列表
 * 在全局搜索关键词变化时调用，一次性刷新订单、用户、景点、酒店、门票、路线六个模块的列表。
 */
function renderAllLists() {
    renderOrders();
    renderUsers();
    renderScenic();
    renderHotels();
    renderTickets();
    renderRoutes();
}


/**
 * 加载所有后台数据
 * 按依赖顺序加载：先概览，再并发加载用户/订单/酒店，然后加载景点、门票、路线。
 * 初始加载时显示"正在加载后台数据..."占位提示。
 */
async function loadAll() {
    el('dashboard').innerHTML = '<div class="empty-state">\u6b63\u5728\u52a0\u8f7d\u540e\u53f0\u6570\u636e...</div>';
    await loadDashboard();
    await Promise.all([loadUsers(), loadOrders(), loadHotels()]);
    await loadScenic();
    await loadTickets();
    await loadRoutes();
}


/* 初始化：绑定侧边栏导航切换事件 */
bindNavigation();
/* 初始化：设置门票表单默认日期为今天 */
setValue('ticketDate', today());
/* 初始化：校验管理员身份，通过后才加载所有数据 */
ensureAdmin().then(ok => {
    if (ok) loadAll();
});
