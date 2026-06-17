<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" isELIgnored="true" %>
<% String ctx = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>欢迎 - 智旅旅行</title>
    <style>
        *{box-sizing:border-box}
        body{margin:0;min-height:100vh;font-family:"Microsoft YaHei","Segoe UI",sans-serif;background:#eef5fc;color:#0f172a}
        .hero{min-height:100vh;padding:36px 7vw;background:linear-gradient(110deg,rgba(8,51,92,.92),rgba(17,118,214,.72) 48%,rgba(255,255,255,.82)),url('https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=2200&q=85') center/cover;display:grid;grid-template-columns:1.1fr .9fr;gap:54px;align-items:center}
        .brand{display:inline-flex;align-items:center;gap:12px;color:#fff;font-size:30px;font-weight:900;margin-bottom:38px}
        .brand-mark{width:44px;height:44px;border-radius:14px;background:#fff;color:#0b8ff6;display:grid;place-items:center;box-shadow:0 18px 36px rgba(0,0,0,.18)}
        .left h1{margin:0 0 24px;color:#fff;font-size:62px;line-height:1.08;letter-spacing:-2px;text-shadow:0 8px 28px rgba(0,0,0,.22)}
        .left p{max-width:760px;margin:0 0 28px;color:rgba(255,255,255,.92);font-size:22px;line-height:1.8}
        .chips{display:flex;gap:14px;flex-wrap:wrap}.chip{border:0;border-radius:999px;background:#fff;color:#0b8ff6;padding:12px 20px;font-weight:900;font-size:16px;box-shadow:0 12px 30px rgba(0,0,0,.12)}
        .login-card{background:rgba(255,255,255,.96);border:1px solid rgba(255,255,255,.72);border-radius:28px;padding:34px;box-shadow:0 28px 80px rgba(8,51,92,.22);backdrop-filter:blur(12px)}
        .login-card h2{margin:0 0 12px;font-size:30px}.login-card p{margin:0 0 24px;color:#64748b;line-height:1.8}
        .login-row{display:grid;grid-template-columns:1fr 132px;gap:12px;margin-bottom:18px}.login-row input{height:58px;border:1px solid #dbe7f3;border-radius:14px;padding:0 18px;font-size:17px;outline:none}.login-row input:focus{border-color:#0b8ff6;box-shadow:0 0 0 4px rgba(11,143,246,.12)}
        button.primary{height:58px;border:0;border-radius:14px;background:#0b8ff6;color:#fff;font-size:18px;font-weight:900;cursor:pointer;box-shadow:0 16px 30px rgba(11,143,246,.25)}
        .features{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin-top:22px}.feature{border:1px solid #e5edf6;background:#f8fbff;border-radius:16px;padding:18px}.feature strong{display:block;font-size:18px;margin-bottom:10px}.feature span{color:#64748b;font-size:15px;line-height:1.6}
        .demo{display:inline-flex;margin-top:6px;border-radius:14px;background:#eef6ff;color:#0b7de3;font-weight:900;padding:13px 18px}.msg{min-height:24px;color:#ef4444;font-weight:800;margin-top:14px}
        @media(max-width:1100px){.hero{grid-template-columns:1fr}.left h1{font-size:46px}.login-card{max-width:760px}.features{grid-template-columns:1fr}}
    </style>
</head>
<body>
<div class="hero">
    <section class="left">
        <div class="brand"><span class="brand-mark">✦</span>智旅旅行</div>
        <h1>把路线、酒店和门票放进同一个旅程里</h1>
        <p>输入预算、天数和兴趣点，系统会推荐旅游路线，并联动酒店房态、景点门票、地图点位和订单支付。适合期末答辩完整演示。</p>
        <div class="chips">
            <span class="chip">AI 规划</span>
            <span class="chip">地图联动</span>
            <span class="chip">订单闭环</span>
        </div>
    </section>
    <section class="login-card">
        <h2>用户登录</h2>
        <p>登录后系统会根据账号权限自动进入对应页面。普通用户进入旅游首页，管理员进入后台管理。</p>
        <div class="login-row"><input id="username" placeholder="用户名" value="demo"><button class="primary" onclick="login()">登录</button></div>
        <div class="login-row"><input id="password" type="password" placeholder="密码" value="demo123"><button class="primary" onclick="goRegister()">注册</button></div>
        <div class="features">
            <div class="feature"><strong>路线规划</strong><span>按价格、兴趣点和天数推荐行程</span></div>
            <div class="feature"><strong>酒店预订</strong><span>查看房型、库存和互斥提醒</span></div>
            <div class="feature"><strong>AI 问答</strong><span>RAG 检索 + 大模型接口预留</span></div>
        </div>
        <div class="demo">演示账号：demo / demo123　管理员：admin / admin123</div>
        <div id="msg" class="msg"></div>
    </section>
</div>
<script>
    const ctx = '<%= ctx %>';
    async function login(){
        msg.textContent = '正在登录...';
        try {
            const response = await fetch(ctx + '/api/auth/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username: username.value.trim(), password: password.value})
            });
            if(!response.ok){
                msg.textContent = '后端接口异常，请检查 Tomcat 和数据库连接';
                return;
            }
            const data = await response.json();
            if(data.code !== 200){
                msg.textContent = data.message || '登录失败，请检查账号密码';
                return;
            }
            const user = data.data || {};
            location.href = user.role === 'ADMIN' ? ctx + '/admin.jsp' : ctx + '/home.jsp';
        } catch (error) {
            msg.textContent = '登录请求失败，请确认后端已启动';
        }
    }
    function goRegister(){
        location.href = ctx + '/login.jsp';
    }
    document.addEventListener('keydown', function(event){
        if(event.key === 'Enter') login();
    });
</script>
</body>
</html>
