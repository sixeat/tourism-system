/**
 * ============================================================
 * global-search.js — 全局搜索联想组件
 *
 * 功能说明：
 * 1. 为顶部搜索框（id="topKeyword"）提供实时联想搜索和下拉提示面板。
 * 2. 数据源包括：
 *    - 硬编码的 10 条 fallback 数据（热门城市、景点、酒店、交通枢纽）。
 *    - 动态从后端加载：/api/map/points（地图点位）和 /api/scenic/list（景点列表）。
 * 3. 支持拼音、首字母、中文、英文混合搜索，按相关度排序（标题匹配 > 城市匹配 > 类型匹配 > 全文匹配）。
 * 4. 支持键盘导航：上下箭头切换、Enter 跳转、Esc 关闭。
 * 5. 根据结果类型自动跳转不同页面：
 *    - 酒店 → hotels.jsp?city=xxx
 *    - 景点/美食 → map.jsp?city=xxx&q=xxx
 *    - 机场/火车站 → map.jsp?city=xxx&q=xxx
 *    - 城市/其他 → routes.jsp?city=xxx
 *
 * 使用方式：
 * 在需要使用全局搜索的页面底部引入本脚本，它会自动检测页面中的 topKeyword 输入框并初始化。
 * 通过单例模式（window.__TOURISM_GLOBAL_SEARCH__）防止重复加载。
 * ============================================================
 */
(function () {
    /* 单例模式：防止同一页面多次加载本脚本导致重复绑定事件 */
    if (window.__TOURISM_GLOBAL_SEARCH__) return;
    window.__TOURISM_GLOBAL_SEARCH__ = true;

    /* 获取顶部搜索输入框，若不存在则退出（某些页面可能没有） */
    var input = document.getElementById('topKeyword');
    if (!input) return;

    /**
     * 获取应用上下文路径
     * 优先使用 window.ctx（由页面注入），否则从 location.pathname 推断。
     * 例如：/tourism-system/home.jsp → /tourism-system
     */
    var ctx = window.ctx || (function () {
        var path = location.pathname || '';
        var parts = path.split('/').filter(Boolean);
        return parts.length ? '/' + parts[0] : '';
    })();

    /* 搜索数据源缓存：包含 fallback 和后端动态加载的数据 */
    var source = [];
    /* 是否已加载过后端数据 */
    var loaded = false;
    /* 输入防抖定时器，避免频繁触发搜索 */
    var timer = null;
    /* 当前键盘选中的下拉项索引 */
    var activeIndex = -1;

    /**
     * fallback 默认数据源：10 条热门旅游相关数据
     * 包含城市、景点、酒店、火车站、机场等类型，用于后端接口不可用时保底展示。
     */
    var fallbackSource = [
        {type: 'CITY', title: '北京', city: '北京', desc: '故宫 · 长城 · 环球度假区', tags: 'beijing bj bei 北京 北方 城市 历史 亲子'},
        {type: 'CITY', title: '上海', city: '上海', desc: '外滩 · 迪士尼 · 城市漫游', tags: 'shanghai sh 上海 城市 夜景 亲子'},
        {type: 'CITY', title: '杭州', city: '杭州', desc: '西湖 · 灵隐寺 · 宋城', tags: 'hangzhou hz 杭州 江南 湖景 美食'},
        {type: 'CITY', title: '厦门', city: '厦门', desc: '鼓浪屿 · 环岛路 · 海鲜', tags: 'xiamen xm 厦门 海岛 海边 美食'},
        {type: 'CITY', title: '桂林', city: '桂林', desc: '漓江 · 阳朔 · 山水', tags: 'guilin gl 桂林 山水 亲子 摄影'},
        {type: '景点', title: '北京故宫', city: '北京', desc: '明清皇家宫殿建筑群', tags: 'beijing gugong 故宫 历史 古建筑 门票'},
        {type: '景点', title: '八达岭长城', city: '北京', desc: '北京经典长城路线', tags: 'beijing changcheng 长城 徒步 摄影'},
        {type: '酒店', title: '北京王府井精选酒店', city: '北京', desc: '靠近故宫与王府井商圈', tags: 'beijing hotel 酒店 住宿 市中心'},
        {type: '火车站', title: '北京南站', city: '北京', desc: '高铁出发与到达枢纽', tags: 'beijing railway train 交通 高铁 火车站'},
        {type: '机场', title: '北京首都国际机场', city: '北京', desc: '北京主要航空枢纽', tags: 'beijing airport 交通 机场 航班'}
    ];

    /**
     * HTML 实体转义
     * 防止搜索结果中的特殊字符破坏 DOM 结构，防御 XSS。
     */
    function esc(text) {
        var map = {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'};
        return String(text == null ? '' : text).replace(/[&<>"']/g, function (ch) { return map[ch]; });
    }

    /**
     * 根据数据类型返回对应图标文字
     * 用于下拉面板左侧的视觉标识，增强可读性。
     */
    function icon(type) {
        if (type === '酒店') return '酒';
        if (type === '美食') return '食';
        if (type === '机场') return '机';
        if (type === '火车站') return '站';
        if (type === 'CITY') return '城';
        return '景';
    }

    /**
     * 搜索相关度评分算法
     * 将关键词按空格拆分为多个词，分别在标题、城市、类型、全文等字段中匹配，赋予不同权重：
     * - 标题匹配：+8 分（最高优先级）
     * - 城市匹配：+5 分
     * - 类型匹配：+3 分
     * - 全文匹配：+1 分
     * 总分数越高，排序越靠前。
     */
    function score(item, keyword) {
        var parts = String(keyword || '').toLowerCase().split(/\s+/).filter(Boolean);
        var text = [item.title, item.city, item.type, item.address, item.desc, item.tags].join(' ').toLowerCase();
        var total = 0;
        parts.forEach(function (part) {
            if (String(item.title || '').toLowerCase().indexOf(part) >= 0) total += 8;
            if (String(item.city || '').toLowerCase().indexOf(part) >= 0) total += 5;
            if (String(item.type || '').toLowerCase().indexOf(part) >= 0) total += 3;
            if (text.indexOf(part) >= 0) total += 1;
        });
        return total;
    }

    /**
     * 根据结果类型生成跳转 URL
     * 酒店 → hotels.jsp，景点/美食/机场/火车站 → map.jsp，其他（城市）→ routes.jsp
     */
    function urlFor(item) {
        if (item.type === '酒店') return ctx + '/hotels.jsp?city=' + encodeURIComponent(item.city || item.title || '上海');
        if (item.type === '景点' || item.type === '美食') return ctx + '/map.jsp?city=' + encodeURIComponent(item.city || '上海') + '&q=' + encodeURIComponent(item.title || '');
        if (item.type === '机场' || item.type === '火车站') return ctx + '/map.jsp?city=' + encodeURIComponent(item.city || '上海') + '&q=' + encodeURIComponent(item.title || '');
        return ctx + '/routes.jsp?city=' + encodeURIComponent(item.city || item.title || '上海');
    }

    /**
     * 去重函数
     * 根据 "类型|城市|标题" 组合键去重，避免地图点位和景点列表中出现重复项。
     */
    function unique(list) {
        var seen = {};
        return list.filter(function (item) {
            var key = item.type + '|' + item.city + '|' + item.title;
            if (seen[key]) return false;
            seen[key] = true;
            return true;
        });
    }

    /**
     * 加载搜索数据源
     * 1. 首次调用时加载 fallback 数据作为保底。
     * 2. 并发请求 /api/map/points（地图点位）和 /api/scenic/list（景点列表）。
     * 3. 将地图点位中的 city 提取为城市项，景点转为搜索项，与 fallback 合并后去重。
     * 4. 若后端请求失败，仍使用 fallback 数据，保证搜索功能可用。
     */
    async function loadSource() {
        if (loaded) return source;
        loaded = true;
        source = fallbackSource.slice();
        try {
            var results = await Promise.all([
                fetch(ctx + '/api/map/points').then(function (r) { return r.json(); }),
                fetch(ctx + '/api/scenic/list').then(function (r) { return r.json(); })
            ]);
            var points = results[0].data || [];
            var scenics = results[1].data || [];
            /* 从地图点位中提取所有不重复的城市，生成城市搜索项 */
            var cities = Array.from(new Set(points.map(function (x) { return x.city; }).filter(Boolean))).map(function (city) {
                return {type: 'CITY', title: city, city: city, desc: '热门目的地', tags: '城市 目的地'};
            });
            /* 合并 fallback、城市、点位、景点四类数据，并去重 */
            source = unique(fallbackSource
                .concat(cities)
                .concat(points.map(function (x) { return {type: x.pointType || '景点', title: x.pointName, city: x.city, address: x.address, desc: x.description, tags: x.tags}; }))
                .concat(scenics.map(function (x) { return {type: '景点', title: x.scenicName, city: x.city, address: x.category, desc: x.description, tags: x.tags}; })));
        } catch (e) {
            /* 后端异常时降级使用 fallback 数据 */
            source = unique(fallbackSource);
        }
        return source;
    }

    /**
     * 获取或创建下拉提示面板
     * 若面板已存在则直接返回，否则动态创建并插入到 body 中。
     * 面板绝对定位，跟随搜索框位置。
     */
    function panel() {
        var existing = document.getElementById('globalSuggestPanel');
        if (existing) return existing;
        var div = document.createElement('div');
        div.id = 'globalSuggestPanel';
        div.style.cssText = 'position:absolute;z-index:9999;background:#fff;border:1px solid #e5edf6;border-radius:14px;box-shadow:0 20px 60px rgba(15,40,80,.18);max-width:520px;min-width:320px;overflow:hidden;display:none;';
        document.body.appendChild(div);
        return div;
    }

    /**
     * 定位面板到搜索框下方
     * 根据输入框的 getBoundingClientRect 计算绝对位置，确保面板在可视区域内。
     */
    function positionPanel() {
        var p = panel();
        var rect = input.getBoundingClientRect();
        p.style.top = (window.scrollY + rect.bottom + 6) + 'px';
        p.style.left = (window.scrollX + rect.left) + 'px';
        p.style.width = rect.width + 'px';
    }

    /**
     * 显示下拉面板
     */
    function showPanel() { panel().style.display = 'block'; }

    /**
     * 隐藏下拉面板，并重置选中索引
     */
    function hidePanel() { panel().style.display = 'none'; activeIndex = -1; }

    /**
     * 渲染下拉面板内容
     * 1. 根据 keyword 对 source 评分排序，取前 8 条。
     * 2. 生成每条结果的 HTML：图标 + 标题 + 描述 + 城市。
     * 3. 每条结果可点击跳转，支持鼠标悬停高亮。
     */
    function render(keyword) {
        var p = panel();
        if (!keyword) { hidePanel(); return; }
        var list = source.slice().map(function (item) {
            return {item: item, score: score(item, keyword)};
        }).filter(function (x) { return x.score > 0; }).sort(function (a, b) { return b.score - a.score; }).slice(0, 8);
        if (list.length === 0) { hidePanel(); return; }
        p.innerHTML = list.map(function (x, i) {
            var item = x.item;
            return '<div class="global-suggest-item" data-index="' + i + '" data-url="' + esc(urlFor(item)) + '" style="padding:10px 14px;cursor:pointer;border-bottom:1px solid #f1f5f9;display:flex;align-items:center;gap:10px;">' +
                '<span style="width:24px;height:24px;border-radius:8px;background:#eef6ff;color:#0b7de3;display:inline-flex;align-items:center;justify-content:center;font-size:12px;font-weight:900;">' + icon(item.type) + '</span>' +
                '<div style="flex:1;min-width:0;">' +
                    '<div style="font-weight:800;font-size:14px;color:#0f172a;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">' + esc(item.title) + '</div>' +
                    '<div style="font-size:12px;color:#64748b;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">' + esc(item.city) + ' · ' + esc(item.desc) + '</div>' +
                '</div>' +
                '<span style="font-size:11px;color:#94a3b8;white-space:nowrap;">' + esc(item.type) + '</span>' +
            '</div>';
        }).join('');
        positionPanel();
        showPanel();
        bindItems();
    }

    /**
     * 绑定下拉项的鼠标事件
     * 点击跳转对应页面，鼠标悬停时高亮当前项。
     */
    function bindItems() {
        var items = panel().querySelectorAll('.global-suggest-item');
        items.forEach(function (item) {
            item.addEventListener('click', function () {
                location.href = item.dataset.url;
            });
            item.addEventListener('mouseenter', function () {
                activeIndex = Number(item.dataset.index);
                updateActive(items);
            });
        });
    }

    /**
     * 更新键盘导航的高亮状态
     * 根据 activeIndex 为对应项添加蓝色背景，其他项移除高亮。
     */
    function updateActive(items) {
        items.forEach(function (item, i) {
            item.style.background = (i === activeIndex) ? '#f0f7ff' : '#fff';
        });
    }

    /**
     * 输入框 input 事件处理
     * 使用 150ms 防抖，减少不必要的搜索计算和 DOM 操作。
     */
    input.addEventListener('input', function () {
        clearTimeout(timer);
        timer = setTimeout(function () {
            loadSource().then(function () { render(input.value.trim()); });
        }, 150);
    });

    /**
     * 键盘事件处理：上下箭头、Enter、Esc
     * - 上/下箭头：在结果项之间移动高亮。
     * - Enter：若面板打开且有选中项，则跳转；否则直接执行搜索（topSearch）。
     * - Esc：关闭面板。
     */
    input.addEventListener('keydown', function (e) {
        var items = panel().querySelectorAll('.global-suggest-item');
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            activeIndex = Math.min(activeIndex + 1, items.length - 1);
            updateActive(items);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            activeIndex = Math.max(activeIndex - 1, -1);
            updateActive(items);
        } else if (e.key === 'Enter') {
            if (activeIndex >= 0 && items[activeIndex]) {
                e.preventDefault();
                location.href = items[activeIndex].dataset.url;
            } else {
                hidePanel();
                if (window.topSearch) window.topSearch();
            }
        } else if (e.key === 'Escape') {
            hidePanel();
        }
    });

    /**
     * 点击页面其他区域时关闭下拉面板
     * 提升用户体验，避免面板遮挡其他内容。
     */
    document.addEventListener('click', function (e) {
        if (!input.contains(e.target) && !panel().contains(e.target)) {
            hidePanel();
        }
    });

    /* 页面加载时预加载数据源，提升首次搜索响应速度 */
    loadSource();
})();
