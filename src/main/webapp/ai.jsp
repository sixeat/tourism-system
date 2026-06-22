<%--
    ============================================================
    ai.jsp — AI 行程助手页面
    
    页面功能：
    1. 提供 AI 驱动的旅游行程问答助手，用户可输入问题、出发地、目的地、预算、天数、兴趣点。
    2. 调用后端 /api/ai/ask 接口，支持两种模式：本地 RAG 检索 和 大模型 API 增强。
    3. 展示 AI 回答、参考来源（RAG 命中的本地知识库条目）和后续追问建议。
    4. 提供快捷提问模板（杭州三天两晚、亲子迪士尼、低预算路线等），一键填入问题。
    5. 支持将当前参数（城市、预算、天数）直接跳转到路线规划页面（routes.jsp）。
    
    后端 API：
    - GET /api/auth/me — 鉴权，未登录跳转 login.jsp。
    - POST /api/auth/logout — 退出登录。
    - POST /api/ai/ask — AI 问答主接口，接收 question/originCity/destinationCity/budget/days/interests。
    
    页面结构：
    - 左侧：参数输入表单（问题、出发地、目的地、预算、天数、兴趣点）+ 快捷提问 + 操作按钮。
    - 右侧：AI 回答展示区（answerBox）+ 模式标签（modeChip）+ 参考来源列表 + 追问建议。
    ============================================================
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" isELIgnored="true" %><% String ctx=request.getContextPath(); %><!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><title>AI&#34892;&#31243;&#21161;&#25163; - &#26234;&#26053;&#26053;&#34892;</title><link rel="stylesheet" href="<%= ctx %>/static/css/ctrip-shell.css?v=20260616-ai-workbench"><%-- AI 页面专属样式：双列网格、回答卡片、模式标签、快捷提问按钮等 --%>
<style>.ai-grid{display:grid;grid-template-columns:minmax(390px,.9fr) minmax(560px,1.1fr);gap:22px;align-items:stretch}.ai-grid>.panel{height:100%}.answer-shell{min-height:560px;display:flex;flex-direction:column}.assistant-head{display:flex;align-items:flex-start;justify-content:space-between;gap:14px;margin-bottom:14px}.assistant-head h2{margin:0}.mode-chip{display:inline-flex;border-radius:999px;padding:5px 10px;background:#f3f6fb;color:#667085;border:1px solid #e5edf6;font-size:12px;font-weight:800;white-space:nowrap}.mode-chip.ready{background:#eef8ff;color:#0b7de3;border-color:#cfe8ff}.answer-box{flex:1;min-height:320px;white-space:pre-wrap;line-height:1.9;background:linear-gradient(180deg,#fff,#f8fbff);border:1px solid #e5edf6;border-radius:18px;padding:18px}.assist-empty{display:grid;gap:12px;color:#526071}.prompt-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px}.prompt-chip{border:1px solid #d9e8f7;background:#fff;border-radius:14px;padding:12px;text-align:left;color:#0f172a;font-weight:800;cursor:pointer}.prompt-chip:hover{border-color:#0b8ff6;color:#0b7de3;background:#f3f9ff}.result-dock{margin-top:14px;border-top:1px solid #edf2f7;padding-top:14px;display:grid;gap:12px}.dock-title{display:flex;justify-content:space-between;align-items:center;color:#667085;font-size:13px;font-weight:900}.rag-list{display:flex;gap:8px;flex-wrap:wrap}.rag-item{border:1px solid #e5edf6;background:#f8fbff;border-radius:999px;padding:7px 11px;line-height:1.4;font-size:13px;color:#475467;max-width:100%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.source-empty{display:none}.suggestion-row{display:flex;gap:10px;flex-wrap:wrap}.suggestion-row .secondary{padding:8px 12px;font-size:13px}.ai-hint{margin-top:14px;border:1px solid #e5edf6;background:#f8fbff;border-radius:16px;padding:14px;color:#526071;line-height:1.7}.ai-hint strong{color:#0f172a}.loading{opacity:.68}@media(max-width:1050px){.ai-grid{grid-template-columns:1fr}.answer-shell{min-height:auto}.prompt-grid{grid-template-columns:1fr}}</style></head><body><div class="app-shell"><aside class="side-nav"><button class="menu-toggle" title="&#33756;&#21333;">&#9776;</button><a class="side-link " href="<%= ctx %>/home.jsp"><span class="side-icon">&#8962;</span>&#39318;&#39029;</a><a class="side-link " href="<%= ctx %>/routes.jsp"><span class="side-icon">&#9635;</span>&#36335;&#32447;&#35268;&#21010;</a><a class="side-link " href="<%= ctx %>/hotels.jsp"><span class="side-icon">&#9635;</span>&#37202;&#24215;&#39044;&#35746;</a><a class="side-link " href="<%= ctx %>/tickets.jsp"><span class="side-icon">&#9636;</span>&#38376;&#31080;&#183;&#27963;&#21160;</a><a class="side-link " href="<%= ctx %>/map.jsp"><span class="side-icon">&#8982;</span>&#26053;&#28216;&#22320;&#22270;</a><a class="side-link active" href="<%= ctx %>/ai.jsp"><span class="side-icon">&#10022;</span>AI&#34892;&#31243;&#21161;&#25163;</a><div class="side-divider"></div><a class="side-link " href="<%= ctx %>/orders.jsp"><span class="side-icon">&#9776;</span>&#25105;&#30340;&#35746;&#21333;</a><a class="side-link " href="<%= ctx %>/profile.jsp"><span class="side-icon">&#9786;</span>&#20010;&#20154;&#20013;&#24515;</a><button class="side-link" onclick="logout()"><span class="side-icon">&#8618;</span>&#36864;&#20986;&#30331;&#24405;</button></aside><main class="main-area"><header class="top-header"><a class="brand" href="<%= ctx %>/home.jsp"><span class="brand-mark">&#10022;</span>&#26234;&#26053;&#26053;&#34892;</a><div class="global-search"><input id="topKeyword" placeholder="&#25628;&#32034;&#20219;&#20309;&#26053;&#28216;&#30456;&#20851;" onkeydown="if(event.key==='Enter')topSearch()"><button onclick="topSearch()">&#8981;</button></div><div class="top-actions"><span class="user-chip" id="userChip">&#29992;&#25143;&#20013;&#24515;</span><a href="<%= ctx %>/orders.jsp">&#25105;&#30340;&#35746;&#21333;</a><a href="<%= ctx %>/ai.jsp">&#32852;&#31995;&#23458;&#26381;</a><button onclick="logout()">&#36864;&#20986;</button></div></header><section class="page-content"><div class="hero-panel"><h1>AI&#34892;&#31243;&#21161;&#25163;</h1><p>&#36755;&#20837;&#39044;&#31639;&#12289;&#20852;&#36259;&#21644;&#30446;&#30340;&#22320;&#65292;&#31995;&#32479;&#20808;&#20570;RAG&#26816;&#32034;&#65292;&#20877;&#29983;&#25104;&#36335;&#32447;&#12289;&#37202;&#24215;&#12289;&#38376;&#31080;&#21644;&#22320;&#22270;&#32852;&#21160;&#24314;&#35758;&#12290;</p></div><div class="ai-grid"><div class="panel"><h2>&#34892;&#31243;&#20559;&#22909;</h2><div class="form-grid"><label>&#20986;&#21457;&#22320;<input id="origin" value="&#21414;&#38376;"></label><label>&#30446;&#30340;&#22320;<input id="city" value="&#26477;&#24030;"></label><label>&#39044;&#31639;<input id="budget" type="number" value="1888"></label><label>&#22825;&#25968;<input id="days" type="number" value="5"></label></div><label>&#20852;&#36259;&#20851;&#38190;&#35789;<textarea id="interests">&#32654;&#39135;&#12289;&#22812;&#26223;&#12289;&#25991;&#21270;&#20307;&#39564;</textarea></label><label>&#21521;AI&#25552;&#38382;<textarea id="question">&#24110;&#25105;&#35268;&#21010;&#19968;&#20010;&#19977;&#22825;&#20004;&#26202;&#34892;&#31243;&#65292;&#21253;&#21547;&#20132;&#36890;&#12289;&#37202;&#24215;&#21644;&#38376;&#31080;&#24314;&#35758;&#12290;</textarea></label><div class="action-row"><button class="primary" onclick="askAi()">&#29983;&#25104;AI&#24314;&#35758;</button><button class="secondary" onclick="goRoute()">&#36827;&#20837;&#36335;&#32447;&#35268;&#21010;</button><a class="secondary" href="<%= ctx %>/map.jsp">&#25171;&#24320;&#26053;&#28216;&#22320;&#22270;</a></div><div class="ai-hint"><strong>&#24037;&#20316;&#27969;&#65306;</strong>&#31995;&#32479;&#20250;&#25353;&#30446;&#30340;&#22320;&#12289;&#39044;&#31639;&#12289;&#22825;&#25968;&#21644;&#20852;&#36259;&#26816;&#32034;&#26412;&#22320;&#26223;&#28857;&#12289;&#37202;&#24215;&#12289;&#38376;&#31080;&#12289;&#36335;&#32447;&#19982;&#22320;&#22270;&#28857;&#20301;&#65292;&#20877;&#32452;&#32455;&#25104;&#21487;&#28436;&#31034;&#30340;&#19968;&#20307;&#21270;&#26053;&#34892;&#26041;&#26696;&#12290;</div></div><div class="panel answer-shell"><div class="assistant-head"><div><h2>&#26234;&#33021;&#31572;&#22797;</h2><p class="muted">&#25226;&#38656;&#27714;&#20889;&#28165;&#26970;&#65292;&#25105;&#20250;&#30452;&#25509;&#32473;&#20986;&#21487;&#33853;&#22320;&#30340;&#26053;&#34892;&#26041;&#26696;&#12290;</p></div><span id="modeChip" class="mode-chip">&#26410;&#29983;&#25104;</span></div><div id="answerBox" class="answer-box"><div class="assist-empty"><strong>&#21487;&#20197;&#36825;&#26679;&#38382;&#65306;</strong><div class="prompt-grid"><button class="prompt-chip" onclick="quickAsk('\u676d\u5dde\u4e09\u5929\u4e24\u665a\uff0c\u9884\u7b971800\uff0c\u60f3\u5403\u7f8e\u98df\u548c\u770b\u591c\u666f\uff0c\u600e\u4e48\u5b89\u6392\uff1f')">&#26477;&#24030;&#19977;&#22825;&#20004;&#26202; / &#32654;&#39135;&#22812;&#26223;</button><button class="prompt-chip" onclick="quickAsk('\u4ece\u53a6\u95e8\u51fa\u53d1\u53bb\u4e0a\u6d77\uff0c\u4e09\u5929\u4e24\u665a\uff0c\u5e2e\u6211\u5b89\u6392\u4ea4\u901a\u3001\u9152\u5e97\u548c\u95e8\u7968\u3002')">&#21414;&#38376;&#20986;&#21457;&#21435;&#19978;&#28023; / &#21547;&#20132;&#36890;</button><button class="prompt-chip" onclick="quickAsk('\u4eb2\u5b50\u6e38\u53bb\u8fea\u58eb\u5c3c\u9644\u8fd1\uff0c\u9884\u7b972500\uff0c\u9152\u5e97\u548c\u95e8\u7968\u600e\u4e48\u9009\uff1f')">&#20146;&#23376;&#36842;&#22763;&#23612; / &#37202;&#24215;&#38376;&#31080;</button><button class="prompt-chip" onclick="quickAsk('\u5e2e\u6211\u505a\u4e00\u4e2a\u4f4e\u9884\u7b97\u8def\u7ebf\uff0c\u4f18\u5148\u514d\u8d39\u666f\u70b9\u548c\u6027\u4ef7\u6bd4\u9152\u5e97\u3002')">&#20302;&#39044;&#31639;&#36335;&#32447; / &#24615;&#20215;&#27604;</button></div></div></div><div class="result-dock"><div><div class="dock-title"><span>RAG&#26816;&#32034;&#26469;&#28304;</span><span id="sourceCount">&#29983;&#25104;&#21518;&#23637;&#31034;</span></div><div id="referenceList" class="rag-list source-empty"></div></div><div><div class="dock-title"><span>&#25512;&#33616;&#36861;&#38382;</span><span>&#21487;&#19968;&#38190;&#32487;&#32493;&#32454;&#21270;</span></div><div id="suggestionList" class="suggestion-row"><button class="secondary" onclick="quickAsk('\u628a\u4e0a\u9762\u7684\u65b9\u6848\u6309\u4e0a\u5348\u3001\u4e0b\u5348\u3001\u665a\u4e0a\u7ec6\u5316\u3002')">&#32454;&#21270;&#21040;&#27599;&#22825;&#26102;&#27573;</button><button class="secondary" onclick="quickAsk('\u5e2e\u6211\u628a\u9884\u7b97\u538b\u4f4e\u4e00\u70b9\uff0c\u4fdd\u7559\u6838\u5fc3\u666f\u70b9\u3002')">&#21387;&#20302;&#39044;&#31639;</button><button class="secondary" onclick="quickAsk('\u63a8\u8350\u66f4\u9002\u5408\u62cd\u7167\u548c\u7f8e\u98df\u7684\u7248\u672c\u3002')">&#20559;&#25293;&#29031;&#32654;&#39135;</button></div></div></div></div></div></section></main></div><script>
(function(){
    const ctx='<%= ctx %>';
    const $=id=>document.getElementById(id);

    function valueOf(id,fallback=''){
        const el=$(id);
        return el ? el.value : fallback;
    }

    function setText(id,text){
        const el=$(id);
        if(el)el.textContent=text;
    }

    async function ensureLogin(){
        const r=await fetch(ctx+'/api/auth/me');
        const d=await r.json();
        if(d.code!==200){
            location.href=ctx+'/login.jsp';
            return null;
        }
        const u=d.data||{};
        setText('userChip','尊敬的，'+(u.username||'用户'));
        return u;
    }

    async function logout(){
        await fetch(ctx+'/api/auth/logout',{method:'POST'});
        location.href=ctx+'/login.jsp';
    }

    function topSearch(){
        const kw=valueOf('topKeyword','上海').trim();
        location.href=ctx+'/search.jsp?q='+encodeURIComponent(kw||'上海');
    }

    function e(t){
        return String(t==null?'':t).replace(/[&<>"']/g,ch=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
    }

    function renderList(id,list){
        const box=$(id);
        const count=$('sourceCount');
        const arr=list||[];
        if(!box)return;
        box.classList.toggle('source-empty',arr.length===0);
        if(count)count.textContent=arr.length?('命中 '+arr.length+' 条'):'暂无来源';
        box.innerHTML=arr.map(x=>'<div class="rag-item" title="'+e(x)+'">'+e(x)+'</div>').join('');
    }

    function renderSuggestions(list){
        const box=$('suggestionList');
        if(!box)return;
        const fallback=[
            '把方案按上午、下午、晚上细化。',
            '帮我把预算压低一点，保留核心景点。',
            '推荐更适合拍照和美食的版本。'
        ];
        const arr=(list&&list.length?list:fallback);
        box.innerHTML='';
        arr.forEach(text=>{
            const button=document.createElement('button');
            button.type='button';
            button.className='secondary';
            button.textContent=text;
            button.addEventListener('click',()=>quickAsk(text));
            box.appendChild(button);
        });
    }

    function quickAsk(text){
        const question=$('question');
        if(question)question.value=text;
        askAi();
    }

    async function askAi(){
        const answerBox=$('answerBox');
        const modeChip=$('modeChip');
        if(!answerBox)return;
        answerBox.textContent='正在检索本地旅游知识库，并生成行程建议...';
        answerBox.classList.add('loading');
        if(modeChip){
            modeChip.textContent='检索中';
            modeChip.classList.add('ready');
        }
        const body={
            question:valueOf('question'),
            originCity:valueOf('origin'),
            destinationCity:valueOf('city'),
            city:valueOf('city'),
            budget:valueOf('budget'),
            days:valueOf('days'),
            interests:valueOf('interests')
        };
        try{
            const r=await fetch(ctx+'/api/ai/ask',{
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body:JSON.stringify(body)
            });
            if(!r.ok){
                answerBox.textContent='AI 接口返回异常，请检查 Tomcat、数据库或后端日志。';
                if(modeChip)modeChip.textContent='接口异常';
                renderList('referenceList',[]);
                renderSuggestions([]);
                return;
            }
            const d=await r.json();
            const data=d.data||{};
            answerBox.textContent=data.answer||d.message||'没有拿到回答';
            if(modeChip)modeChip.textContent=data.mode==='API_RAG'?'大模型增强':'本地检索';
            renderList('referenceList',data.references);
            renderSuggestions(data.suggestions);
        }catch(err){
            console.error('AI ask failed',err);
            answerBox.textContent='AI 服务调用失败，请检查后端接口或 API Key。';
            if(modeChip)modeChip.textContent='调用失败';
            renderList('referenceList',[]);
            renderSuggestions([]);
        }finally{
            answerBox.classList.remove('loading');
        }
    }

    function goRoute(){
        location.href=ctx+'/routes.jsp?city='+encodeURIComponent(valueOf('city'))+'&budget='+encodeURIComponent(valueOf('budget'))+'&days='+encodeURIComponent(valueOf('days'));
    }

    function bindPageEvents(){
        const generateBtn=document.querySelector('.action-row .primary');
        const routeBtn=[...document.querySelectorAll('.action-row .secondary')].find(btn=>btn.textContent.includes('进入路线规划'));
        if(generateBtn)generateBtn.addEventListener('click',askAi);
        if(routeBtn)routeBtn.addEventListener('click',goRoute);

        const promptMap={
            '杭州三天两晚 / 美食夜景':'杭州三天两晚，预算1800，想吃美食和看夜景，怎么安排？',
            '厦门出发去上海 / 含交通':'从厦门出发去上海，三天两晚，帮我安排交通、酒店和门票。',
            '亲子迪士尼 / 酒店门票':'亲子游去迪士尼附近，预算2500，酒店和门票怎么选？',
            '低预算路线 / 性价比':'帮我做一个低预算路线，优先免费景点和性价比酒店。'
        };
        document.querySelectorAll('.prompt-chip').forEach(button=>{
            button.addEventListener('click',()=>quickAsk(promptMap[button.textContent.trim()]||button.textContent.trim()));
        });
    }

    function initAiPage(){
        const params=new URLSearchParams(location.search);
        const city=$('city');
        if(city)city.value=params.get('city')||city.value;
        bindPageEvents();
        renderSuggestions();
        ensureLogin();
    }

    window.logout=logout;
    window.topSearch=topSearch;
    window.quickAsk=quickAsk;
    window.askAi=askAi;
    window.goRoute=goRoute;

    initAiPage();
})();
</script>
<%-- 加载全局搜索组件，提供顶部搜索框的联想补全功能 --%>
<script src="<%= ctx %>/static/js/global-search.js?v=20260616-ai-workbench"></script></body></html>