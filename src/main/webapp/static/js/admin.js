const ctx = window.APP_CTX || '';

let scenicItems = [];
let hotelItems = [];
let ticketItems = [];
let routeItems = [];
let userItems = [];
let orderItems = [];
let routeSpotDraft = [];
const pageSize = 6;
const pageState = {orders: 1, users: 1, scenic: 1, hotels: 1, tickets: 1, routes: 1};

function el(id) {
    return document.getElementById(id);
}

function esc(text) {
    const map = {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'};
    return String(text == null ? '' : text).replace(/[&<>"']/g, ch => map[ch]);
}

function keyword() {
    return (el('adminKeyword')?.value || '').trim().toLowerCase();
}

function hasKeyword(item, keys) {
    const word = keyword();
    if (!word) return true;
    return keys.map(key => item && item[key]).join(' ').toLowerCase().includes(word);
}


function resetPages() {
    Object.keys(pageState).forEach(key => pageState[key] = 1);
}

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

function changePage(key, step, renderFn) {
    pageState[key] = (pageState[key] || 1) + step;
    renderFn();
}

function renderPaged(containerId, key, list, renderItem, emptyText, renderName) {
    const pageData = pageSlice(key, list);
    const cards = pageData.rows.map(renderItem).join('') || `<div class="empty-state">${emptyText}</div>`;
    el(containerId).innerHTML = cards + pager(key, pageData, renderName);
}

function money(value) {
    return '&yen;' + Number(value || 0).toFixed(2);
}

function textMoney(value) {
    return '\u00a5' + Number(value || 0).toFixed(2);
}

function today() {
    return new Date().toISOString().slice(0, 10);
}

function setValue(id, value) {
    const target = el(id);
    if (target) target.value = value == null ? '' : value;
}

function message(id, text, isError = false) {
    const target = el(id);
    if (!target) return;
    target.textContent = text || '';
    target.classList.toggle('error', Boolean(isError));
}

function scrollToInput(id) {
    const target = el(id);
    if (!target) return;
    target.scrollIntoView({behavior: 'smooth', block: 'center'});
    target.focus();
}

function looksMojibake(text) {
    return Array.from(String(text || '')).some(ch => {
        const code = ch.charCodeAt(0);
        return code === 0xfffd || code === 0x9422 || code === 0x95b0 || code === 0x93c5 || code === 0x95c2 || code === 0x74ba;
    });
}

function normalizeMessage(data, fallback) {
    if (!data || !data.message || looksMojibake(data.message)) {
        return fallback;
    }
    return data.message;
}

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

async function ensureAdmin() {
    const res = await fetch(ctx + '/api/auth/me', {credentials: 'same-origin'});
    const data = await res.json().catch(() => ({}));
    if (data.code !== 200 || !data.data || data.data.role !== 'ADMIN') {
        location.href = ctx + '/login.jsp';
        return false;
    }
    return true;
}

async function logout() {
    await fetch(ctx + '/api/auth/logout', {method: 'POST', credentials: 'same-origin'});
    location.href = ctx + '/login.jsp';
}

function bindNavigation() {
    document.querySelectorAll('.nav-link').forEach(button => {
        button.addEventListener('click', () => {
            showSection(button.dataset.target);
        });
    });
}

function showSection(targetId) {
    document.querySelectorAll('.nav-link').forEach(item => item.classList.toggle('active', item.dataset.target === targetId));
    document.querySelectorAll('.admin-section').forEach(section => section.classList.toggle('active-page', section.id === targetId));
    window.scrollTo({top: 0, behavior: 'smooth'});
}

function metric(label, value) {
    return `<div class="metric-card"><strong>${value}</strong><span>${label}</span></div>`;
}

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

function orderTypeLabel(type) {
    return type === 'HOTEL' ? '\u9152\u5e97\u8ba2\u5355' : '\u95e8\u7968\u8ba2\u5355';
}

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

function statusClass(status) {
    if (status === 'PAID' || status === 'USED' || status === 'FINISHED') return 'green';
    if (status === 'CANCELLED') return 'red';
    return 'orange';
}

async function loadOrders() {
    const data = await request(ctx + '/api/admin/order/list');
    orderItems = data.data || [];
    renderOrders();
}

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


async function loadUsers() {
    const data = await request(ctx + '/api/admin/user/list');
    userItems = data.data || [];
    renderUsers();
}

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

function clearUserForm() {
    ['userId', 'username', 'userPhone', 'userEmail'].forEach(id => setValue(id, ''));
    setValue('userRole', 'USER');
    setValue('userStatus', '1');
    message('userResult', '\u5df2\u6e05\u7a7a\u7528\u6237\u8868\u5355\u3002');
}

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

async function loadScenic() {
    const data = await request(ctx + '/api/admin/scenic/list');
    scenicItems = data.data || [];
    initRouteSpotSelect();
    renderRouteSpotPreview();
    renderScenic();
}

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

function clearScenicForm() {
    ['scenicId', 'scenicName', 'scenicCity', 'scenicCategory', 'scenicPrice', 'scenicScore', 'scenicPopularity', 'scenicTags', 'scenicLongitude', 'scenicLatitude', 'scenicDesc'].forEach(id => setValue(id, ''));
    message('scenicResult', '\u5df2\u6e05\u7a7a\u666f\u70b9\u8868\u5355\u3002');
}

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

async function loadHotels() {
    const data = await request(ctx + '/api/admin/hotel/list');
    hotelItems = data.data || [];
    renderHotels();
}

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

function clearHotelForm() {
    ['hotelId', 'hotelName', 'hotelCity', 'hotelLevel', 'hotelAddress', 'hotelDesc'].forEach(id => setValue(id, ''));
    setValue('hotelStatus', '1');
    message('hotelResult', '\u5df2\u6e05\u7a7a\u9152\u5e97\u8868\u5355\u3002');
}

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

async function loadTickets() {
    const data = await request(ctx + '/api/admin/ticket/list');
    ticketItems = data.data || [];
    renderTickets();
}

function scenicNameById(id) {
    const scenic = scenicItems.find(item => Number(item.id) === Number(id));
    return scenic ? scenic.scenicName : ('\u666f\u70b9 #' + id);
}

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

function clearTicketForm() {
    ['ticketId', 'ticketScenicId', 'ticketName', 'ticketPrice', 'ticketStock'].forEach(id => setValue(id, ''));
    setValue('ticketDate', today());
    message('ticketResult', '\u5df2\u6e05\u7a7a\u95e8\u7968\u8868\u5355\u3002');
}

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

async function loadRoutes() {
    const data = await request(ctx + '/api/admin/route/list');
    routeItems = data.data || [];
    renderRoutes();
}

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


function initRouteSpotSelect() {
    const select = el('routeSpotScenicSelect');
    if (!select) return;
    select.innerHTML = scenicItems
        .map(item => `<option value="${item.id}">${esc(item.city || '-')}-${esc(item.scenicName)}</option>`)
        .join('');
    fillRouteSpotSuggest();
}

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

function fillRouteSpotSuggest(dayValue) {
    const next = getRouteSpotNextPosition(dayValue || el('routeSpotDayInput')?.value);
    setValue('routeSpotDayInput', next.dayNo);
    setValue('routeSpotSortInput', next.sortNo);
}

function syncRouteSpotInput() {
    el('routeSpots').value = routeSpotDraft
        .map(item => `${item.scenicId}-${item.dayNo}-${item.sortNo}`)
        .join(',');
}

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

function removeRouteSpot(scenicId, dayNo, sortNo) {
    routeSpotDraft = routeSpotDraft.filter(item => !(item.scenicId === scenicId && item.dayNo === dayNo && item.sortNo === sortNo));
    renderRouteSpotPreview();
    message('routeResult', '\u5df2\u79fb\u9664\u8be5\u8def\u7ebf\u70b9\u4f4d\u3002');
    fillRouteSpotSuggest(dayNo);
}

function clearRouteSpotsOnly() {
    routeSpotDraft = [];
    renderRouteSpotPreview();
    fillRouteSpotSuggest(1);
    message('routeResult', '\u5df2\u6e05\u7a7a\u8def\u7ebf\u70b9\u4f4d\uff0c\u53ef\u4ee5\u91cd\u65b0\u7f16\u6392\u3002');
}

function quickAddScenicToRoute(scenicId) {
    setValue('routeSpotScenicSelect', scenicId);
    fillRouteSpotSuggest();
    addRouteSpot(scenicId);
    el('routeSection')?.scrollIntoView({behavior: 'smooth', block: 'start'});
}

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

function clearRouteForm() {
    ['routeId', 'routeName', 'routeCity', 'routeDays', 'routeBudget', 'routeTheme', 'routeDesc', 'routeSpots'].forEach(id => setValue(id, ''));
    routeSpotDraft = [];
    renderRouteSpotPreview();
    fillRouteSpotSuggest(1);
    message('routeResult', '\u5df2\u6e05\u7a7a\u8def\u7ebf\u8868\u5355\u3002');
}

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

function renderAllLists() {
    renderOrders();
    renderUsers();
    renderScenic();
    renderHotels();
    renderTickets();
    renderRoutes();
}

async function loadAll() {
    el('dashboard').innerHTML = '<div class="empty-state">\u6b63\u5728\u52a0\u8f7d\u540e\u53f0\u6570\u636e...</div>';
    await loadDashboard();
    await Promise.all([loadUsers(), loadOrders(), loadHotels()]);
    await loadScenic();
    await loadTickets();
    await loadRoutes();
}

bindNavigation();
setValue('ticketDate', today());
ensureAdmin().then(ok => {
    if (ok) loadAll();
});
