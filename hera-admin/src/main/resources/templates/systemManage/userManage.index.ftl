<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>用户管理中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/css/userManage.css">
</head>

<style type="text/css">

</style>

<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper">
    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">
        <section class="content">
            <div class="box">
                <div class="box-header">
                    <h3 class="big-title">用户管理</h3>
                </div>
                <div class="box-body">
                    <table id="userTable" lay-filter="userTable"/>
                </div>
            </div>


        </section>

    </div>
    <script type="text/html" id="toolbar">
        <a class="layui-btn layui-btn-radius layui-btn-normal" lay-event="refresh">刷新</a>
    </script>
    <script type="text/html" id="barOption">
        <a class="layui-btn layui-btn-xs" lay-event="approve">审核通过</a>
        <a class="layui-btn layui-btn-xs layui-btn-warm" lay-event="refuse">审核拒绝</a>
        <a class="layui-btn layui-btn-xs layui-btn-danger" lay-event="del">删除</a>
    </script>
<@netCommon.commonScript />
    <script src="${request.contextPath}/js/userManage.js"></script>
</body>

</html>


