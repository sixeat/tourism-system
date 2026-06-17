(function () {
    if (window.__TOURISM_GLOBAL_SEARCH__) return;
    window.__TOURISM_GLOBAL_SEARCH__ = true;

    var input = document.getElementById('topKeyword');
    if (!input) return;
    var ctx = window.ctx || (function () {
        var path = location.pathname || '';
        var parts = path.split('/').filter(Boolean);
        return parts.length ? '/' + parts[0] : '';
    })();
    var source = [];
    var loaded = false;
    var timer = null;
    var activeIndex = -1;
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

    function esc(text) {
        var map = {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'};
        return String(text == null ? '' : text).replace(/[&<>"']/g, function (ch) { return map[ch]; });
    }

    function icon(type) {
        if (type === '酒店') return '酒';
        if (type === '美食') return '食';
        if (type === '机场') return '机';
        if (type === '火车站') return '站';
        if (type === 'CITY') return '城';
        return '景';
    }

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

    function urlFor(item) {
        if (item.type === '酒店') return ctx + '/hotels.jsp?city=' + encodeURIComponent(item.city || item.title || '上海');
        if (item.type === '景点' || item.type === '美食') return ctx + '/map.jsp?city=' + encodeURIComponent(item.city || '上海') + '&q=' + encodeURIComponent(item.title || '');
        if (item.type === '机场' || item.type === '火车站') return ctx + '/map.jsp?city=' + encodeURIComponent(item.city || '上海') + '&q=' + encodeURIComponent(item.title || '');
        return ctx + '/routes.jsp?city=' + encodeURIComponent(item.city || item.title || '上海');
    }

    function unique(list) {
        var seen = {};
        return list.filter(function (item) {
            var key = item.type + '|' + item.city + '|' + item.title;
            if (seen[key]) return false;
            seen[key] = true;
            return true;
        });
    }

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
            var cities = Array.from(new Set(points.map(function (x) { return x.city; }).filter(Boolean))).map(function (city) {
                return {type: 'CITY', title: city, city: city, desc: '热门目的地', tags: '城市 目的地'};
            });
            source = unique(fallbackSource
                .concat(cities)
                .concat(points.map(function (x) { return {type: x.pointType || '景点', title: x.pointName, city: x.city, address: x.address, desc: x.description, tags: x.tags}; }))
                .concat(scenics.map(function (x) { return {type: '景点', title: x.scenicName, city: x.city, address: x.category, desc: x.description, tags: x.tags}; })));
        } catch (e) {
            source = unique(fallbackSource);
        }
        return source;
    }

    function panel() {
        var existing = document.getElementById('globalSuggestPanel');
        if (existing) return existing;
        var div = document.createElement('div');
        div.id = 'globalSuggestPanel';
        div.className = 'global-suggest';
        document.body.appendChild(div);
        return div;
    }

    function positionPanel() {
        var box = input.closest('.global-search') || input;
        var rect = box.getBoundingClientRect();
        var div = panel();
        div.style.left = Math.round(rect.left) + 'px';
        div.style.top = Math.round(rect.bottom + 8) + 'px';
        div.style.width = Math.max(280, Math.round(rect.width - 68)) + 'px';
    }

    function render(list) {
        var box = panel();
        activeIndex = -1;
        if (!list.length) {
            box.classList.remove('open');
            box.innerHTML = '';
            return;
        }
        positionPanel();
        box.innerHTML = list.map(function (item, index) {
            return '<div class="global-suggest-item" data-index="' + index + '" data-url="' + esc(urlFor(item)) + '"><div class="global-suggest-icon">' + icon(item.type) + '</div><div><div class="global-suggest-title">' + esc(item.title) + '</div><div class="global-suggest-meta">' + esc(item.city || '') + ' · ' + esc(item.type) + ' · ' + esc(item.address || item.tags || item.desc || '') + '</div></div><div class="global-suggest-action">打开</div></div>';
        }).join('') + '<div class="global-suggest-item" data-search="1"><div class="global-suggest-icon">搜</div><div><div class="global-suggest-title">查看全部搜索结果</div><div class="global-suggest-meta">按当前关键词进入全站搜索页</div></div><div class="global-suggest-action">Enter</div></div>';
        box.classList.add('open');
    }

    async function update() {
        var keyword = (input.value || '').trim();
        if (!keyword) {
            panel().classList.remove('open');
            return;
        }
        var data = await loadSource();
        render(data.map(function (item) {
            var next = Object.assign({}, item);
            next.score = score(item, keyword);
            return next;
        }).filter(function (item) { return item.score > 0; }).sort(function (a, b) { return b.score - a.score; }).slice(0, 8));
    }

    function goSearch() {
        var keyword = (input.value || '').trim() || '上海';
        location.href = ctx + '/search.jsp?q=' + encodeURIComponent(keyword);
    }

    input.addEventListener('input', function () {
        clearTimeout(timer);
        timer = setTimeout(update, 80);
    });
    input.addEventListener('focus', update);
    input.addEventListener('keydown', function (event) {
        var items = Array.from(panel().querySelectorAll('.global-suggest-item'));
        if (event.key === 'ArrowDown' && items.length) {
            event.preventDefault();
            activeIndex = (activeIndex + 1) % items.length;
            items.forEach(function (item) { item.classList.remove('active'); });
            items[activeIndex].classList.add('active');
        }
        if (event.key === 'ArrowUp' && items.length) {
            event.preventDefault();
            activeIndex = (activeIndex - 1 + items.length) % items.length;
            items.forEach(function (item) { item.classList.remove('active'); });
            items[activeIndex].classList.add('active');
        }
        if (event.key === 'Enter' && panel().classList.contains('open') && activeIndex >= 0) {
            event.preventDefault();
            items[activeIndex].dispatchEvent(new MouseEvent('mousedown', {bubbles: true}));
        }
    });
    panel().addEventListener('mousedown', function (event) {
        var item = event.target.closest('.global-suggest-item');
        if (!item) return;
        event.preventDefault();
        if (item.dataset.search) return goSearch();
        if (item.dataset.url) location.href = item.dataset.url;
    });
    window.addEventListener('resize', function () { if (panel().classList.contains('open')) positionPanel(); });
    window.addEventListener('scroll', function () { if (panel().classList.contains('open')) positionPanel(); }, true);
    document.addEventListener('click', function (event) {
        if (!event.target.closest('.global-search') && !event.target.closest('#globalSuggestPanel')) panel().classList.remove('open');
    });
})();

