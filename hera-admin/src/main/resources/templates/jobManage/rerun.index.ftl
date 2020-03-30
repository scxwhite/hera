<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>任务重跑</title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />

    <style>
        .table-hover > tbody > tr:hover {
            cursor: pointer;
        }

        #toolbar {
            margin-bottom: 4px;
        }
    </style>
</head>
<script type="text/html" id="toolbar">
    <a class="layui-btn layui-btn-radius layui-btn-normal" lay-event="add">新增</a>
    <a class="layui-btn layui-btn-radius layui-btn-normal" lay-event="refresh">刷新</a>

</script>



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
                    <h3 class="big-title">任务重跑</h3>
                </div>
                <div class="box-body">
                    <table id="rerunList"  class="layui-table" lay-filter="rerunList"></table>
                </div>
            </div>

        </section>
    </div>
</div>


<script type="text/html" id="addRerun">
    <form class="layui-form layui-form-pane" action="" id="addRerunForm">

        <div class="layui-form-item" style="display: none">
            <label class="layui-form-label">id</label>
            <div class="layui-input-block">
                <input class="layui-input" name="id" size="12" type="text">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">任务Id</label>
            <div class="layui-input-block">
                <input class="layui-input" name="jobId" size="12" type="text">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">名称</label>
            <div class="layui-input-block">
                <input class="layui-input" name="name" size="12" type="text">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">起始日期</label>
            <div class="layui-input-block">
                <input class="layui-input" id="startTime" name="startTime" size="12" type="text" readonly
                       placeholder="请选择日期">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">结束日期</label>
            <div class="layui-input-block">
                <input class="layui-input" id="endTime" name="endTime" size="12" type="text" readonly
                       placeholder="请选择日期">
            </div>
        </div>

        <div class="layui-form-item" pane>
            <label class="layui-form-label">并行度</label>
            <div class="layui-input-block">
                <input type="radio" name="threads" value="1" title="1" checked>
                <input type="radio" name="threads" value="2" title="2">
                <input type="radio" name="threads" value="4" title="4">
                <input type="radio" name="threads" value="8" title="8">
            </div>
        </div>
    </form>
</script>

</body>

<@netCommon.commonScript />

<script src="${request.contextPath}/static/js/jobRerun.js"></script>


</html>
