<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" isELIgnored="true" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>后台管理 - 智旅旅行</title>
    <link rel="stylesheet" href="<%= ctx %>/static/css/admin.css?v=20260617-page">
</head>
<body>
<div class="admin-shell">
    <aside class="admin-sidebar">
        <div class="sidebar-brand">
            <span class="brand-logo">智</span>
            <div>
                <strong>智旅后台</strong>
                <small>Tourism Admin</small>
            </div>
        </div>
        <nav class="sidebar-nav">
            <button class="nav-link active" data-target="dashboardSection"><span>总</span>数据概览</button>
            <button class="nav-link" data-target="orderSection"><span>订</span>订单管理</button>
            <button class="nav-link" data-target="userSection"><span>用</span>用户管理</button>
            <button class="nav-link" data-target="scenicSection"><span>景</span>景点管理</button>
            <button class="nav-link" data-target="hotelSection"><span>酒</span>酒店管理</button>
            <button class="nav-link" data-target="ticketSection"><span>票</span>门票管理</button>
            <button class="nav-link" data-target="routeSection"><span>线</span>路线管理</button>
        </nav>
        <div class="sidebar-footer">
            <button onclick="location.href=ctx + '/home.jsp'">用户首页</button>
            <button onclick="location.href=ctx + '/index.jsp'">入口页</button>
            <button class="danger-link" onclick="logout()">退出登录</button>
        </div>
    </aside>

    <main class="admin-main">
        <header class="admin-topbar">
            <div>
                <p class="eyebrow">旅游推荐模式酒店预订一体系统</p>
                <h1>后台管理控制台</h1>
            </div>
            <div class="topbar-actions">
                <div class="admin-search">
                    <input id="adminKeyword" placeholder="搜索用户、订单、景点、酒店、门票、路线" oninput="resetPages(); renderAllLists()">
                    <button onclick="renderAllLists()">搜索</button>
                </div>
                <button class="ghost-btn" onclick="loadAll()">刷新数据</button>
            </div>
        </header>

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

        <section class="admin-section active-page" id="dashboardSection">
            <div class="section-head">
                <div>
                    <p class="eyebrow">Dashboard</p>
                    <h2>数据概览</h2>
                </div>
                <button class="ghost-btn" onclick="loadDashboard()">刷新概览</button>
            </div>
            <div id="dashboard" class="metric-grid"></div>
        </section>

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

        <section class="admin-section" id="userSection">
            <div class="section-head">
                <div>
                    <p class="eyebrow">Users</p>
                    <h2>用户管理</h2>
                </div>
                <button class="ghost-btn" onclick="clearUserForm()">清空表单</button>
            </div>
            <div class="manage-layout">
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

        <section class="resource-grid" id="resourcePages">
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

<script>
    window.APP_CTX = '<%= ctx %>';
</script>
<script src="<%= ctx %>/static/js/admin.js?v=20260617-page"></script>
</body>
</html>
