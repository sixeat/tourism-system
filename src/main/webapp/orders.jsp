<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" isELIgnored="true" %>
<% String ctx=request.getContextPath(); %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>&#25105;&#30340;&#35746;&#21333; - &#26234;&#26053;&#26053;&#34892;</title>
    <link rel="stylesheet" href="<%= ctx %>/static/css/ctrip-shell.css?v=20260616-fixed">
    <style>
        .order-tabs{display:flex;gap:10px;flex-wrap:wrap}.order-tabs button.active{background:#0b8ff6;color:#fff}.order-summary{display:grid;grid-template-columns:repeat(4,minmax(160px,1fr));gap:14px;margin-bottom:18px}.summary-card{background:#fff;border:1px solid #e5edf6;border-radius:20px;padding:18px;box-shadow:0 10px 26px rgba(20,40,80,.05)}.summary-card strong{display:block;color:#0b8ff6;font-size:28px}.order-card{border-left:5px solid #0b8ff6;position:relative}.order-head{display:flex;justify-content:space-between;gap:12px;align-items:flex-start}.order-badge{border-radius:999px;padding:6px 10px;font-size:13px;font-weight:900;background:#eef6ff;color:#0b7de3}.order-badge.green{background:#ecfdf5;color:#059669}.order-badge.orange{background:#fff7ed;color:#f97316}.order-badge.red{background:#fef2f2;color:#dc2626}.order-meta{display:grid;gap:6px;margin-top:12px}.empty-order{min-height:260px;display:flex;align-items:center;justify-content:center;text-align:center;border:1px dashed #cfe4fb;border-radius:22px;background:#fff;color:#667085}.empty-order strong{display:block;color:#0f172a;font-size:24px;margin-bottom:8px}@media(max-width:900px){.order-summary{grid-template-columns:repeat(2,minmax(150px,1fr))}}
    </style>
</head>
<body>
<div class="app-shell">
    <aside class="side-nav">
        <button class="menu-toggle" title="&#33756;&#21333;">&#9776;</button>
        <a class="side-link" href="<%= ctx %>/home.jsp"><span class="side-icon">&#8962;</span>&#39318;&#39029;</a>
        <a class="side-link" href="<%= ctx %>/routes.jsp"><span class="side-icon">&#9635;</span>&#36335;&#32447;&#35268;&#21010;</a>
        <a class="side-link" href="<%= ctx %>/hotels.jsp"><span class="side-icon">&#9635;</span>&#37202;&#24215;&#39044;&#35746;</a>
        <a class="side-link" href="<%= ctx %>/tickets.jsp"><span class="side-icon">&#9636;</span>&#38376;&#31080;&#183;&#27963;&#21160;</a>
        <a class="side-link" href="<%= ctx %>/map.jsp"><span class="side-icon">&#8982;</span>&#26053;&#28216;&#22320;&#22270;</a>
        <a class="side-link" href="<%= ctx %>/ai.jsp"><span class="side-icon">&#10022;</span>AI&#34892;&#31243;&#21161;&#25163;</a>
        <div class="side-divider"></div>
        <a class="side-link active" href="<%= ctx %>/orders.jsp"><span class="side-icon">&#9776;</span>&#25105;&#30340;&#35746;&#21333;</a>
        <a class="side-link" href="<%= ctx %>/profile.jsp"><span class="side-icon">&#9786;</span>&#20010;&#20154;&#20013;&#24515;</a>
        <button class="side-link" onclick="logout()"><span class="side-icon">&#8618;</span>&#36864;&#20986;&#30331;&#24405;</button>
    </aside>
    <main class="main-area">
        <header class="top-header">
            <a class="brand" href="<%= ctx %>/home.jsp"><span class="brand-mark">&#10022;</span>&#26234;&#26053;&#26053;&#34892;</a>
            <div class="global-search"><input id="topKeyword" placeholder="&#25628;&#32034;&#20219;&#20309;&#26053;&#28216;&#30456;&#20851;" onkeydown="if(event.key==='Enter')topSearch()"><button onclick="topSearch()">&#8981;</button></div>
            <div class="top-actions"><span class="user-chip" id="userChip">&#29992;&#25143;&#20013;&#24515;</span><a href="<%= ctx %>/orders.jsp">&#25105;&#30340;&#35746;&#21333;</a><a href="<%= ctx %>/ai.jsp">&#32852;&#31995;&#23458;&#26381;</a><button onclick="logout()">&#36864;&#20986;</button></div>
        </header>
        <section class="page-content">
            <div class="hero-panel"><h1>&#25105;&#30340;&#35746;&#21333;</h1><p>&#37202;&#24215;&#21644;&#38376;&#31080;&#35746;&#21333;&#37117;&#22312;&#36825;&#37324;&#65292;&#19979;&#21333;&#21518;&#20250;&#31435;&#21363;&#21516;&#27493;&#26174;&#31034;&#12290;</p></div>
            <div id="summary" class="order-summary"></div>
            <div class="panel"><div class="order-tabs"><button class="secondary active" data-type="ALL">&#20840;&#37096;&#35746;&#21333;</button><button class="secondary" data-type="HOTEL">&#37202;&#24215;&#35746;&#21333;</button><button class="secondary" data-type="TICKET">&#38376;&#31080;&#35746;&#21333;</button><button class="secondary" data-type="CREATED">&#24453;&#25903;&#20184;</button><button class="secondary" data-type="PAID">&#24050;&#25903;&#20184;</button></div></div>
            <div id="orders" class="result-grid"></div>
        </section>
    </main>
</div>
<script>
const ctx='<%= ctx %>';
let allOrders=[];
let currentType='ALL';
async function ensureLogin(){const r=await fetch(ctx+'/api/auth/me');const d=await r.json();if(d.code!==200){location.href=ctx+'/login.jsp';return null;}const u=d.data||{};const c=document.getElementById('userChip');if(c)c.textContent='\u5c0a\u656c\u7684\uff0c'+(u.username||'\u7528\u6237');return u;}
async function logout(){await fetch(ctx+'/api/auth/logout',{method:'POST'});location.href=ctx+'/login.jsp';}
function topSearch(){const kw=(document.getElementById('topKeyword')?.value||'\u4e0a\u6d77').trim();location.href=ctx+'/search.jsp?q='+encodeURIComponent(kw||'\u4e0a\u6d77');}
function e(t){return String(t==null?'':t).replace(/[&<>"']/g,ch=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));}
function asArray(value){return Array.isArray(value)?value:[];}
function normalizeOrders(data){if(Array.isArray(data))return data;if(data&&Array.isArray(data.orders))return data.orders;return [...asArray(data&&data.hotelOrders).map(x=>({...x,orderType:'HOTEL'})),...asArray(data&&data.ticketOrders).map(x=>({...x,orderType:'TICKET'}))];}
function fmtDate(v){if(Array.isArray(v)){const [y,m,d,h,mi]=v;return y+'-'+String(m).padStart(2,'0')+'-'+String(d).padStart(2,'0')+(h!=null?' '+String(h).padStart(2,'0')+':'+String(mi||0).padStart(2,'0'):'');}return e(v||'-');}
function typeLabel(t){return t==='HOTEL'?'\u9152\u5e97\u8ba2\u5355':'\u95e8\u7968\u8ba2\u5355';}
function statusLabel(s){return ({CREATED:'\u5f85\u652f\u4ed8',PAID:'\u5df2\u652f\u4ed8',USED:'\u5df2\u4f7f\u7528',FINISHED:'\u5df2\u5b8c\u6210',CANCELLED:'\u5df2\u53d6\u6d88'}[s]||s||'-');}
function statusClass(s){if(s==='PAID'||s==='USED'||s==='FINISHED')return 'green';if(s==='CANCELLED')return 'red';return 'orange';}
function canOperate(o){return o.orderStatus==='CREATED';}
function renderSummary(){const total=allOrders.length;const hotel=allOrders.filter(o=>o.orderType==='HOTEL').length;const ticket=allOrders.filter(o=>o.orderType==='TICKET').length;const pending=allOrders.filter(o=>o.orderStatus==='CREATED').length;summary.innerHTML='<div class="summary-card"><strong>'+total+'</strong><span>\u5168\u90e8\u8ba2\u5355</span></div><div class="summary-card"><strong>'+hotel+'</strong><span>\u9152\u5e97\u8ba2\u5355</span></div><div class="summary-card"><strong>'+ticket+'</strong><span>\u95e8\u7968\u8ba2\u5355</span></div><div class="summary-card"><strong>'+pending+'</strong><span>\u5f85\u652f\u4ed8</span></div>';}
function filteredOrders(){return allOrders.filter(o=>currentType==='ALL'||o.orderType===currentType||o.orderStatus===currentType);}
function renderOrders(){document.querySelectorAll('[data-type]').forEach(btn=>btn.classList.toggle('active',btn.dataset.type===currentType));const list=filteredOrders();orders.innerHTML=list.map(o=>'<div class="card order-card"><div class="order-head"><div><div class="title">'+typeLabel(o.orderType)+' #'+o.id+'</div><div class="muted">'+e(o.itemName||'-')+'</div></div><span class="order-badge '+statusClass(o.orderStatus)+'">'+statusLabel(o.orderStatus)+'</span></div><div class="order-meta"><div class="muted">\u91d1\u989d\uff1a<span class="price">&yen;'+Number(o.totalAmount||0).toFixed(2)+'</span></div><div class="muted">\u4f7f\u7528\u65e5\u671f\uff1a'+fmtDate(o.useDate)+'</div><div class="muted">\u521b\u5efa\u65f6\u95f4\uff1a'+fmtDate(o.createTime)+'</div></div><div class="action-row">'+(canOperate(o)?'<button class="primary" onclick="payOrder(\''+o.orderType+'\','+o.id+')">\u652f\u4ed8\u8ba2\u5355</button><button class="secondary" onclick="cancelOrder(\''+o.orderType+'\','+o.id+')">\u53d6\u6d88\u8ba2\u5355</button>':'<span class="secondary">\u5df2\u5904\u7406</span>')+'</div></div>').join('')||'<div class="empty-order"><div><strong>\u6682\u65e0\u8ba2\u5355</strong><p>\u53bb\u9884\u8ba2\u9152\u5e97\u6216\u95e8\u7968\uff0c\u63d0\u4ea4\u540e\u8fd9\u91cc\u4f1a\u7acb\u5373\u663e\u793a\u3002</p></div></div>';}
async function loadOrders(type='ALL'){currentType=type;orders.innerHTML='<div class="card muted">\u6b63\u5728\u52a0\u8f7d\u8ba2\u5355...</div>';const r=await fetch(ctx+'/api/user/orders');const d=await r.json();if(d.code!==200){orders.innerHTML='<div class="card muted">\u8ba2\u5355\u52a0\u8f7d\u5931\u8d25\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55\u3002</div>';return;}allOrders=normalizeOrders(d.data);renderSummary();renderOrders();}
async function payOrder(type,id){const r=await fetch(ctx+'/api/order/'+(type==='HOTEL'?'hotel':'ticket')+'/pay?orderId='+id,{method:'POST'});const d=await r.json();alert(d.message||'\u8ba2\u5355\u5df2\u652f\u4ed8');loadOrders(currentType);}
async function cancelOrder(type,id){if(!confirm('\u786e\u5b9a\u53d6\u6d88\u8fd9\u4e2a\u8ba2\u5355\u5417\uff1f'))return;const r=await fetch(ctx+'/api/order/'+(type==='HOTEL'?'hotel':'ticket')+'/cancel?orderId='+id,{method:'POST'});const d=await r.json();alert(d.message||'\u8ba2\u5355\u5df2\u53d6\u6d88');loadOrders(currentType);}
document.querySelectorAll('[data-type]').forEach(btn=>btn.onclick=()=>loadOrders(btn.dataset.type));
ensureLogin().then(()=>loadOrders('ALL'));
</script>
<script src="<%= ctx %>/static/js/global-search.js?v=20260616-fixed"></script>
</body>
</html>
