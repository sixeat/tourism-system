<%--
    ============================================================
    admin.jsp — 后台管理控制台页面
    
    页面功能：
    1. 为管理员（ADMIN 角色）提供统一的后台管理入口。
    2. 包含数据概览、订单管理、用户管理、景点管理、酒店管理、
       门票管理、路线管理共七大功能模块。
    3. 通过左侧侧边栏导航切换不同模块，右侧主内容区动态渲染。
    4. 所有数据通过 AJAX 请求后端 REST API 获取，前端纯 JS 渲染。
    
    鉴权机制：
    - 页面加载时 admin.js 会调用 /api/auth/me 检查当前登录用户。
    - 若未登录或角色不是 ADMIN，则自动重定向到 login.jsp。
    
    后端 API 调用：
    - GET /api/admin/dashboard/summary — 数据概览
    - GET /api/admin/order/list — 订单列表
    - GET /api/admin/user/list — 用户列表
    - GET /api/admin/scenic/list — 景点列表
    - GET /api/admin/hotel/list — 酒店列表
    - GET /api/admin/ticket/list — 门票列表
    - GET /api/admin/route/list — 路线列表
    - POST/PUT/DELETE 对应资源的增删改接口
    ============================================================
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" isELIgnored="true" %>
<%-- 获取应用上下文路径，用于构建静态资源和 API 的绝对路径 --%>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <%-- 字符编码和视口设置，确保移动端正常缩放 --%>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>后台管理 - 智旅旅行</title>
    <%-- 引入后台专属样式表，带版本号防止缓存 --%>
    <link rel="stylesheet" href="<%= ctx %>/static/css/admin.css?v=20260617-page">
</head>
<body>
<%-- 后台整体布局：左侧固定侧边栏 + 右侧自适应主内容 --%>
<div class="admin-shell">
    <%-- 左侧侧边栏：品牌区、导航菜单、底部快捷操作 --%>
    <aside class="admin-sidebar">
        <%-- 品牌区：Logo + 系统名称 + 英文副标题 --%>
        <div class="sidebar-brand">
            <span class="brand-logo">智</span>
            <div>
                <strong>智旅后台</strong>
                <small>Tourism Admin</small>
            </div>
        </div>
        <%-- 导航菜单：每个按钮通过 data-target 关联对应 section 的 id，
             由 admin.js 绑定点击事件实现无刷新页面切换。 --%>
        <nav class="sidebar-nav">
            <button class="nav-link active" data-target="dashboardSection"><span>总</span>数据概览</button>
            <button class="nav-link" data-target="orderSection"><span>订</span>订单管理</button>
            <button class="nav-link" data-target="userSection"><span>用</span>用户管理</button>
            <button class="nav-link" data-target="scenicSection"><span>景</span>景点管理</button>
            <button class="nav-link" data-target="hotelSection"><span>酒</span>酒店管理</button>
            <button class="nav-link" data-target="ticketSection"><span>票</span>门票管理</button>
            <button class="nav-link" data-target="routeSection"><span>线</span>路线管理</button>
        </nav>
        <%-- 侧边栏底部：跳转用户首页、入口页、退出登录 --%>
        <div class="sidebar-footer">
            <button onclick="location.href=ctx + '/home.jsp'">用户首页</button>
            <button onclick="location.href=ctx + '/index.jsp'">入口页</button>
            <button class="danger-link" onclick="logout()">退出登录</button>
        </div>
    </aside>

    <%-- 右侧主内容区：顶部工具栏 + 各功能模块 section --%>
    <main class="admin-main">
        <%-- 顶部工具栏：系统标题 + 全局搜索 + 刷新按钮 --%>
        <header class="admin-topbar">
            <div>
                <p class="eyebrow">旅游推荐模式酒店预订一体系统</p>
                <h1>后台管理控制台</h1>
            </div>
            <div class="topbar-actions">
                <%-- 全局搜索框：输入时触发 resetPages() 和 renderAllLists()，
                     实现对用户、订单、景点、酒店、门票、路线的统一过滤。 --%>
                <div class="admin-search">
                    <input id="adminKeyword" placeholder="搜索用户、订单、景点、酒店、门票、路线" oninput="resetPages(); renderAllLists()">
                    <button onclick="renderAllLists()">搜索</button>
                </div>
                <button class="ghost-btn" onclick="loadAll()">刷新数据</button>
            </div>
        </header>

        <%-- Hero 欢迎卡片：展示后台业务流程闭环（推荐算法 → 路线规划 → 预订订单） --%>
        <section class="hero-card">
            <div>
                <span class="hero-tag">管理中心</span>
                <h2>统一维护路线、酒店、门票与订单库存链路</h2>
                <p>演示时可以先看概览，再从景点、路线、酒店、门票和订单闭环讲业务流程。</p>
            </div>
            <div class="hero-steps">
                <span>推荐算法</span>
                <i></i>
                <span>路线规划</span>
                <i></i>
                <span>预订订单</span>
            </div>
        </section>

        <%-- 模块一：数据概览（Dashboard）
             默认激活（active-page），由 admin.js 调用 /api/admin/dashboard/summary 渲染指标卡片。 --%>
        <section class="admin-section active-page" id="dashboardSection">
            <div class="section-head">
                <div>
                    <p class="eyebrow">Dashboard</p>
                    <h2>数据概览</h2>
                </div>
                <button class="ghost-btn" onclick="loadDashboard()">刷新概览</button>
            </div>
            <%-- 指标网格容器，由 JS 动态填充 metric-card --%>
            <div id="dashboard" class="metric-grid"></div>
        </section>

        <%-- 模块二：订单管理
             支持下拉筛选（全部/酒店/门票/待支付/已支付/已使用/已取消），
             由 JS 调用 /api/admin/order/list 获取数据并渲染。 --%>
        <section class="admin-section" id="orderSection">
            <div class="section-head">
                <div>
                    <p class="eyebrow">Orders</p>
                    <h2>订单管理</h2>
                </div>
                <div class="filter-row">
                    <select id="orderFilter" onchange="pageState.orders = 1; renderOrders()">
                        <option value="ALL">全部订单</option>
                        <option value="HOTEL">酒店订单</option>
                        <option value="TICKET">门票订单</option>
                        <option value="CREATED">待支付</option>
                        <option value="PAID">已支付</option>
                        <option value="USED">已使用</option>
                        <option value="CANCELLED">已取消</option>
                    </select>
                </div>
            </div>
            <div id="orders" class="list-grid"></div>
        </section>

        <%-- 模块三：用户管理
             左侧表单（编辑手机号、邮箱、角色、状态），右侧用户列表。
             调用 /api/admin/user/list、/api/admin/user/update、/api/admin/user/status。 --%>
        <section class="admin-section" id="userSection">
            <div class="section-head">
                <div>
                    <p class="eyebrow">Users</p>
                    <h2>用户管理</h2>
                </div>
                <button class="ghost-btn" onclick="clearUserForm()">清空表单</button>
            </div>
            <div class="manage-layout">
                <%-- 用户编辑表单：id 和 username 只读（从列表选择后回填），
                     其他字段可修改。提交时调用 updateUser()。 --%>
                <form class="form-card" onsubmit="event.preventDefault(); updateUser();">
                    <input id="userId" placeholder="用户编号（从列表选择）" readonly>
                    <input id="username" placeholder="用户名" readonly>
                    <input id="userPhone" placeholder="手机号">
                    <input id="userEmail" placeholder="邮箱">
                    <select id="userRole">
                        <option value="USER">普通用户</option>
                        <option value="ADMIN">管理员</option>
                    </select>
                    <select id="userStatus">
                        <option value="1">启用</option>
                        <option value="0">禁用</option>
                    </select>
                    <div class="form-actions">
                        <button class="primary-btn" type="submit">保存用户</button>
                    </div>
                    <div id="userResult" class="result"></div>
                </form>
                <div id="userAdmin" class="list-grid compact"></div>
            </div>
        </section>

        <%-- 模块四至六：景点、酒店、门票管理（三列资源网格）
             每个模块均包含表单（新增/修改）和列表，通过 REST API 与后端交互。 --%>
        <section class="resource-grid" id="resourcePages">
            <%-- 景点管理：包含名称、城市、分类、价格、评分、热度、标签、经纬度、介绍等字段。 --%>
            <article class="admin-section" id="scenicSection">
                <div class="section-head">
                    <div>
                        <p class="eyebrow">Scenic</p>
                        <h2>景点管理</h2>
                    </div>
                    <button class="ghost-btn" onclick="clearScenicForm()">清空</button>
                </div>
                <form class="form-card" onsubmit="event.preventDefault(); saveScenic();">
                    <input id="scenicId" placeholder="编号（修改时填写）">
                    <input id="scenicName" placeholder="景点名称">
                    <input id="scenicCity" placeholder="城市">
                    <input id="scenicCategory" placeholder="分类">
                    <input id="scenicPrice" placeholder="价格">
                    <input id="scenicScore" placeholder="评分">
                    <input id="scenicPopularity" placeholder="热度">
                    <input id="scenicTags" placeholder="标签">
                    <input id="scenicLongitude" placeholder="经度">
                    <input id="scenicLatitude" placeholder="纬度">
                    <textarea id="scenicDesc" placeholder="景点介绍"></textarea>
                    <div class="form-actions">
                        <button class="primary-btn" type="button" onclick="saveScenic()">新增景点</button>
                        <button class="secondary-btn" type="button" onclick="updateScenic()">修改景点</button>
                    </div>
                    <div id="scenicResult" class="result"></div>
                </form>
                <div id="scenicAdmin" class="list-grid compact"></div>
            </article>

            <%-- 酒店管理：包含名称、城市、等级、状态、地址、描述等字段。 --%>
            <article class="admin-section" id="hotelSection">
                <div class="section-head">
                    <div>
                        <p class="eyebrow">Hotel</p>
                        <h2>酒店管理</h2>
                    </div>
                    <button class="ghost-btn" onclick="clearHotelForm()">清空</button>
                </div>
                <form class="form-card" onsubmit="event.preventDefault(); saveHotel();">
                    <input id="hotelId" placeholder="编号（修改时填写）">
                    <input id="hotelName" placeholder="酒店名称">
                    <input id="hotelCity" placeholder="城市">
                    <input id="hotelLevel" placeholder="等级">
                    <input id="hotelStatus" placeholder="状态：1启用，0停用">
                    <input id="hotelAddress" placeholder="地址">
                    <textarea id="hotelDesc" placeholder="酒店描述"></textarea>
                    <div class="form-actions">
                        <button class="primary-btn" type="button" onclick="saveHotel()">新增酒店</button>
                        <button class="secondary-btn" type="button" onclick="updateHotel()">修改酒店</button>
                    </div>
                    <div id="hotelResult" class="result"></div>
                </form>
                <div id="hotelAdmin" class="list-grid compact"></div>
            </article>

            <%-- 门票管理：关联景点（scenicId），包含名称、价格、库存、可用日期。 --%>
            <article class="admin-section" id="ticketSection">
                <div class="section-head">
                    <div>
                        <p class="eyebrow">Ticket</p>
                        <h2>门票管理</h2>
                    </div>
                    <button class="ghost-btn" onclick="clearTicketForm()">清空</button>
                </div>
                <form class="form-card" onsubmit="event.preventDefault(); saveTicket();">
                    <input id="ticketId" placeholder="编号（修改时填写）">
                    <input id="ticketScenicId" placeholder="景点编号">
                    <input id="ticketName" placeholder="门票名称">
                    <input id="ticketPrice" placeholder="价格">
                    <input id="ticketStock" placeholder="库存">
                    <input id="ticketDate" placeholder="日期，例如 2026-06-18">
                    <div class="form-actions">
                        <button class="primary-btn" type="button" onclick="saveTicket()">新增门票</button>
                        <button class="secondary-btn" type="button" onclick="updateTicket()">修改门票</button>
                    </div>
                    <div id="ticketResult" class="result"></div>
                </form>
                <div id="ticketAdmin" class="list-grid compact"></div>
            </article>
        </section>

        <%-- 模块七：路线管理
             包含路线基本信息表单和点位编排器。
             点位编排器支持选择景点、指定第几天和第几站，自动拼接为 routeSpots 格式（景点ID-第几天-排序）。 --%>
        <section class="admin-section" id="routeSection">
            <div class="section-head">
                <div>
                    <p class="eyebrow">Routes</p>
                    <h2>路线管理</h2>
                </div>
                <button class="ghost-btn" onclick="clearRouteForm()">清空表单</button>
            </div>
            <div class="route-layout">
                <form class="form-card route-form" onsubmit="event.preventDefault(); saveRoute();">
                    <input id="routeId" placeholder="编号（修改时填写）">
                    <input id="routeName" placeholder="路线名称">
                    <input id="routeCity" placeholder="城市">
                    <input id="routeDays" placeholder="天数">
                    <input id="routeBudget" placeholder="预算">
                    <input id="routeTheme" placeholder="主题">
                    <textarea id="routeDesc" placeholder="路线描述"></textarea>
                    <input id="routeSpots" placeholder="路线点位：景点ID-第几天-排序，例如 1-1-1,2-2-1">
                    <%-- 路线点位快捷编排器：可视化添加景点到路线中 --%>
                    <div class="route-builder">
                        <div class="builder-head">
                            <strong>路线点位快捷编排</strong>
                            <span>选择景点后自动写入路线点位</span>
                        </div>
                        <div class="builder-grid">
                            <select id="routeSpotScenicSelect"></select>
                            <input id="routeSpotDayInput" type="number" min="1" placeholder="第几天">
                            <input id="routeSpotSortInput" type="number" min="1" placeholder="第几站">
                            <button class="secondary-btn" type="button" onclick="addRouteSpot()">添加点位</button>
                        </div>
                        <div class="form-actions">
                            <button class="ghost-btn" type="button" onclick="fillRouteSpotSuggest()">智能补位</button>
                            <button class="danger-btn" type="button" onclick="clearRouteSpotsOnly()">清空点位</button>
                        </div>
                        <div id="routeSpotPreview" class="spot-preview"></div>
                    </div>
                    <div class="form-actions">
                        <button class="primary-btn" type="button" onclick="saveRoute()">新增路线</button>
                        <button class="secondary-btn" type="button" onclick="updateRoute()">修改路线</button>
                    </div>
                    <div id="routeResult" class="result"></div>
                </form>
                <div id="routeAdmin" class="list-grid"></div>
            </div>
        </section>
    </main>
</div>

<%-- 将 JSP 上下文路径注入到 JS 全局变量，供 admin.js 中的 API 请求使用 --%>
<script>
    window.APP_CTX = '<%= ctx %>';
</script>
<%-- 加载后台管理逻辑脚本，包含所有 AJAX 请求、列表渲染、表单提交等逻辑 --%>
<script src="<%= ctx %>/static/js/admin.js?v=20260617-page"></script>
</body>
</html>
