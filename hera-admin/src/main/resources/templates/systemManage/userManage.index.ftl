<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>用户管理中心</title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/static/css/userManage.css">
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
        <ul class="nav nav-tabs" id="user-tab">
            <li class="active"><a href="#tab-admin" data-toggle="tab" id="adminTab">用户组</a></li>
            <li><a href="#tab-sso" data-toggle="tab" id="ssoTab">用户</a></li>
        </ul>

        <div class="tab-content">
            <div class="tab-pane active" id="tab-admin">
                <div class="box-body">
                    <table id="userTable" lay-filter="userTable"></table>
                </div>
            </div>

            <div class="tab-pane" id="tab-sso">
                <div class="box-body">
                    <table id="ssoTable" lay-filter="ssoTable"></table>
                </div>
            </div>
        </div>
    </div>
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
<script src="${request.contextPath}/static/js/userManage.js"></script>
</body>

</html>


