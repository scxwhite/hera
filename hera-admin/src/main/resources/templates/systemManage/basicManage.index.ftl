<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>基础运维</title>
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
        <div class="layui-container">
            <div class="layui-row">
                <h1 class="text-center">管理运维界面</h1>
            </div>
            <div class="layui-row" style="border: 1px solid rgba(18,18,18,0.98);">
                <div class="layui-col-md2">
                    <button type="button" class="layui-btn " id="generateVersion">版本生成</button>
                </div>
                <div class="layui-col-md2">
                    <button type="button" class="layui-btn " id="updateWork">刷新work</button>
                </div>
            </div>
        </div>

    </div>
</div>


<@netCommon.commonScript />
<script src="${request.contextPath}/static/js/basicManage.js"></script>

</body>

</html>


