<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>监控管理</title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/static/plugins/layui/css/modules/formSelects-v4.css">
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
                <div class="box-body">
                    <table id="monitorTable" lay-filter="monitorTable"></table>
                </div>
            </div>
        </section>
    </div>

    <#--content-wrapper-->
</div>
<@netCommon.commonScript />
<script type="text/html" id="barOption">
    <a class="layui-btn layui-btn-xs layui-btn-normal" lay-event="edit">编辑</a>
</script>
<script type="text/html" id="toolbar">
    <a class="layui-btn layui-btn-radius layui-btn-normal" lay-event="add">新增</a>
    <a class="layui-btn layui-btn-radius layui-btn-normal" lay-event="refresh">刷新</a>
</script>

<script type="text/html" id="addMonitorJob">
    <form class="layui-form layui-form-pane" action="" id="addMonitorJobForm">

        <div class="layui-form-item" >
            <label class="layui-form-label">任务Id</label>
            <div class="layui-input-block">
                <input type="text" name="jobId"
                       autocomplete="off"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">监控人</label>
            <div class="layui-input-block">
                <select name="monitors" xm-select="monitors" xm-select-search xm-select-direction="auto">
                    {{# layui.each(d, function(index,item) { }}
                    <option value="{{item.id}}">{{item.name}}</option>
                    {{# }); }}
                </select>
            </div>
        </div>
    </form>
</script>
<script src="${request.contextPath}/static/js/jobMonitor.js"></script>
<script src="${request.contextPath}/static/plugins/layui/lay/modules/formSelects-v4.min.js"></script>

</body>

</html>


