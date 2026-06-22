<%--
    ============================================================
    index.jsp — 系统入口页（登录页 / 欢迎页）
    
    页面功能：
    1. 作为旅游系统的入口，展示系统核心卖点（AI 规划、地图联动、订单闭环）。
    2. 提供用户登录入口，支持账号密码登录，登录成功后根据角色自动跳转：
       - ADMIN 角色 → admin.jsp（后台管理）
       - USER 角色 → home.jsp（旅游首页）
    3. 提供注册按钮，点击跳转到 login.jsp（注册/登录统一页）。
    4. 包含演示账号提示：demo / demo123（普通用户）、admin / admin123（管理员）。
    
    后端 API：
    - POST /api/auth/login — 用户登录，传入 username 和 password，
      返回用户信息（含 role 字段），前端据此决定跳转目标。
    
    鉴权：无（未登录用户可访问），但登录成功后会建立 Session。
    ============================================================
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" isELIgnored="true" %>
<%-- 获取应用上下文路径，用于构建 JS 中的 API 地址和页面跳转地址 --%>
<% String ctx = request.getContextPath(); %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>欢迎 - 智旅旅行</title>
    <%-- 内联样式：本页为独立入口页，不依赖外部 CSS，减少首次加载资源 --%>
    <style>
        /* 全局盒模型重置 */
        * { box-sizing: border-box }
        /* 页面基础：浅蓝灰背景，微软雅黑字体 */
        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Microsoft YaHei", "Segoe UI", sans-serif;
            background: #eef5fc;
            color: #0f172a;
        }
        /* Hero 区域：全屏高度，双列网格（左文案 + 右登录卡），
           背景为旅游风景图叠加蓝色渐变蒙版，提升视觉冲击力。 */
        .hero {
            min-height: 100vh;
            padding: 36px 7vw;
            background: linear-gradient(110deg, rgba(8,51,92,.92), rgba(17,118,214,.72) 48%, rgba(255,255,255,.82)),
                        url('https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=2200&q=85') center/cover;
            display: grid;
            grid-template-columns: 1.1fr .9fr;
            gap: 54px;
            align-items: center;
        }
        /* 品牌区：Logo 图标 + 系统名称 */
        .brand {
            display: inline-flex;
            align-items: center;
            gap: 12px;
            color: #fff;
            font-size: 30px;
            font-weight: 900;
            margin-bottom: 38px;
        }
        /* Logo 图标：白色圆角方块，蓝色文字 */
        .brand-mark {
            width: 44px;
            height: 44px;
            border-radius: 14px;
            background: #fff;
            color: #0b8ff6;
            display: grid;
            place-items: center;
            box-shadow: 0 18px 36px rgba(0,0,0,.18);
        }
        /* 左侧大标题：白色、大字号、紧凑行高，带阴影增强可读性 */
        .left h1 {
            margin: 0 0 24px;
            color: #fff;
            font-size: 62px;
            line-height: 1.08;
            letter-spacing: -2px;
            text-shadow: 0 8px 28px rgba(0,0,0,.22);
        }
        /* 左侧描述文字：半透明白色，大字号，宽行高 */
        .left p {
            max-width: 760px;
            margin: 0 0 28px;
            color: rgba(255,255,255,.92);
            font-size: 22px;
            line-height: 1.8;
        }
        /* 特性标签组：AI 规划、地图联动、订单闭环 */
        .chips {
            display: flex;
            gap: 14px;
            flex-wrap: wrap;
        }
        .chip {
            border: 0;
            border-radius: 999px;
            background: #fff;
            color: #0b8ff6;
            padding: 12px 20px;
            font-weight: 900;
            font-size: 16px;
            box-shadow: 0 12px 30px rgba(0,0,0,.12);
        }
        /* 右侧登录卡片：毛玻璃效果（半透明 + 模糊），白色圆角 */
        .login-card {
            background: rgba(255,255,255,.96);
            border: 1px solid rgba(255,255,255,.72);
            border-radius: 28px;
            padding: 34px;
            box-shadow: 0 28px 80px rgba(8,51,92,.22);
            backdrop-filter: blur(12px);
        }
        .login-card h2 {
            margin: 0 0 12px;
            font-size: 30px;
        }
        .login-card p {
            margin: 0 0 24px;
            color: #64748b;
            line-height: 1.8;
        }
        /* 登录输入行：输入框 + 按钮，双列网格 */
        .login-row {
            display: grid;
            grid-template-columns: 1fr 132px;
            gap: 12px;
            margin-bottom: 18px;
        }
        .login-row input {
            height: 58px;
            border: 1px solid #dbe7f3;
            border-radius: 14px;
            padding: 0 18px;
            font-size: 17px;
            outline: none;
        }
        /* 输入框聚焦：蓝色边框 + 光晕 */
        .login-row input:focus {
            border-color: #0b8ff6;
            box-shadow: 0 0 0 4px rgba(11,143,246,.12);
        }
        /* 主按钮：蓝色、圆角、大字号、阴影 */
        button.primary {
            height: 58px;
            border: 0;
            border-radius: 14px;
            background: #0b8ff6;
            color: #fff;
            font-size: 18px;
            font-weight: 900;
            cursor: pointer;
            box-shadow: 0 16px 30px rgba(11,143,246,.25);
        }
        /* 特性介绍区：路线规划、酒店预订、AI 问答三列卡片 */
        .features {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 14px;
            margin-top: 22px;
        }
        .feature {
            border: 1px solid #e5edf6;
            background: #f8fbff;
            border-radius: 16px;
            padding: 18px;
        }
        .feature strong {
            display: block;
            font-size: 18px;
            margin-bottom: 10px;
        }
        .feature span {
            color: #64748b;
            font-size: 15px;
            line-height: 1.6;
        }
        /* 演示账号提示 */
        .demo {
            display: inline-flex;
            margin-top: 6px;
            border-radius: 14px;
            background: #eef6ff;
            color: #0b7de3;
            font-weight: 900;
            padding: 13px 18px;
        }
        /* 错误提示文字 */
        .msg {
            min-height: 24px;
            color: #ef4444;
            font-weight: 800;
            margin-top: 14px;
        }
        /* 响应式：1100px 以下改为单列布局，适配平板和小屏桌面 */
        @media(max-width:1100px) {
            .hero { grid-template-columns: 1fr; }
            .left h1 { font-size: 46px; }
            .login-card { max-width: 760px; }
            .features { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<div class="hero">
    <%-- 左侧：品牌介绍 + 核心卖点文案 + 特性标签 --%>
    <section class="left">
        <div class="brand"><span class="brand-mark">&#10022;</span>智旅旅行</div>
        <h1>把路线、酒店和门票放进同一个旅程里</h1>
        <p>输入预算、天数和兴趣点，系统会推荐旅游路线，并联动酒店房态、景点门票、地图点位和订单支付。适合期末答辩完整演示。</p>
        <div class="chips">
            <span class="chip">AI 规划</span>
            <span class="chip">地图联动</span>
            <span class="chip">订单闭环</span>
        </div>
    </section>
    <%-- 右侧：登录表单区 --%>
    <section class="login-card">
        <h2>用户登录</h2>
        <p>登录后系统会根据账号权限自动进入对应页面。普通用户进入旅游首页，管理员进入后台管理。</p>
        <%-- 第一行：用户名输入 + 登录按钮 --%>
        <div class="login-row">
            <input id="username" placeholder="用户名" value="demo">
            <button class="primary" onclick="login()">登录</button>
        </div>
        <%-- 第二行：密码输入 + 注册按钮（跳转到注册页） --%>
        <div class="login-row">
            <input id="password" type="password" placeholder="密码" value="demo123">
            <button class="primary" onclick="goRegister()">注册</button>
        </div>
        <%-- 三大功能特性介绍 --%>
        <div class="features">
            <div class="feature"><strong>路线规划</strong><span>按价格、兴趣点和天数推荐行程</span></div>
            <div class="feature"><strong>酒店预订</strong><span>查看房型、库存和互斥提醒</span></div>
            <div class="feature"><strong>AI 问答</strong><span>RAG 检索 + 大模型接口预留</span></div>
        </div>
        <%-- 演示账号提示 --%>
        <div class="demo">演示账号：demo / demo123　管理员：admin / admin123</div>
        <%-- 登录结果提示（成功/失败/异常） --%>
        <div id="msg" class="msg"></div>
    </section>
</div>

<%-- 页面脚本：登录逻辑、页面跳转 --%>
<script>
    /* 应用上下文路径，用于拼接 API 地址 */
    const ctx = '<%= ctx %>';
    
    /**
     * 用户登录函数
     * 1. 显示"正在登录..."提示，提升用户等待体验。
     * 2. 通过 fetch 向 /api/auth/login 发送 POST 请求，携带 username 和 password。
     * 3. 根据后端响应 code 判断结果：
     *    - code === 200：登录成功，根据 user.role 跳转（ADMIN → admin.jsp，其他 → home.jsp）。
     *    - 其他：显示错误提示（账号密码错误或接口异常）。
     * 4. 捕获网络异常，提示检查后端启动状态。
     */
    async function login() {
        msg.textContent = '正在登录...';
        try {
            const response = await fetch(ctx + '/api/auth/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username: username.value.trim(), password: password.value})
            });
            if (!response.ok) {
                msg.textContent = '后端接口异常，请检查 Tomcat 和数据库连接';
                return;
            }
            const data = await response.json();
            if (data.code !== 200) {
                msg.textContent = data.message || '登录失败，请检查账号密码';
                return;
            }
            const user = data.data || {};
            /* 根据角色跳转不同页面：ADMIN 进后台，普通用户进首页 */
            location.href = user.role === 'ADMIN' ? ctx + '/admin.jsp' : ctx + '/home.jsp';
        } catch (error) {
            msg.textContent = '登录请求失败，请确认后端已启动';
        }
    }
    
    /**
     * 跳转到注册页面
     * 实际系统中 login.jsp 可能同时承担登录和注册功能。
     */
    function goRegister() {
        location.href = ctx + '/login.jsp';
    }
    
    /* 监听回车键：在输入框中按 Enter 自动触发登录，提升操作效率 */
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Enter') login();
    });
</script>
</body>
</html>
