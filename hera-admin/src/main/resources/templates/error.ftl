<!DOCTYPE html>
<html lang="en-US">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>404 - 对不起，您查找的页面不存在！- JS代码站</title>
    <link rel="stylesheet" type="text/css" href="${request.contextPath}/static/css/main.css">
    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
</head>
<body>
<div id="wrapper"><a class="logo" href="/"></a>
    <div id="main">
        <header id="header">
            <h1><span class="icon">!</span>${code}<span class="sub">${msg}</span></h1>
        </header>
        <div id="content">
            <h2>温馨提示</h2>
            <p>${content}</p>
        </div>
        <div id="footer">
            <ul>
                <li><a href='${request.contextPath}/home'>赫拉首页</a></li>
                <li><a href='${request.contextPath}/scheduleCenter' title='调度中心'>调度中心</a></li>
                <li><a href='${request.contextPath}/developCenter' title='开发中心'>开发中心</a></li>
                <li><a href='${request.contextPath}/jobDetail' title='今日任务详情'>今日任务详情</a></li>
                <li><a href='${request.contextPath}/adviceController' title='建议&留言'>建议&留言</a></li>
                <li><a href='${request.contextPath}/help' title='帮助文档'>帮助文档</a></li>
            </ul>
        </div>
    </div>
</div>
</html>
