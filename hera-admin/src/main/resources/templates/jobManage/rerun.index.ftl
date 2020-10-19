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

    <div class="layui-inline">
        <div class="layui-input-inline">
            <a class="layui-btn layui-btn-radius layui-btn-normal" lay-event="add">新增</a>
            <a class=" layui-btn layui-btn-radius layui-btn-normal" lay-event="refresh">刷新</a>
        </div>
    </div>

    <div class="layui-inline ">
        <div class="layui-input-inline ">
            <select id="endSelect" lay-verify="required" lay-filter="endSelect">
                <option value="-1">所有状态</option>
                <option value="0">开启</option>
                <option value="1">结束</option>
            </select>
        </div>
    </div>


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
                    <table id="rerunList" class="layui-table" lay-filter="rerunList"></table>
                </div>
            </div>

        </section>
    </div>
</div>
</body>


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

        {{# layui.each(d, function(index,item) { }}
        <div class="layui-form-item">
            <div class="layui-inline">

                <label class="layui-form-label">时间范围</label>

                <div class="layui-input-inline" style="width: 200px;">
                    <input class="layui-input " id="startTime_{{index}}" name="startTime_{{index}}" size="12"
                           type="text" readonly
                           placeholder="请选择起始日期">
                </div>

                <div class="layui-form-mid">-</div>
                <div class="layui-input-inline" style="width: 200px;">
                    <input class="layui-input" id="endTime_{{index}}" name="endTime_{{index}}" size="12" type="text"
                           readonly
                           placeholder="请选择结束日期">

                </div>

                {{# if(index === 0){ }}
                <div class="layui-input-inline" style="width: 10px">
                    <a lay-filter="addDate" id="addDate" href="#">
                        <i class=" layui-icon layui-icon-add-1" style="font-size: 30px; color: #1E9FFF;"></i>
                    </a>
                </div>
                {{# if(d.length > 1){ }}
                <div class="layui-input-inline" style="width: 10px;margin-left: 10px">
                    <a lay-filter="addDate" id="subDate" href="#">
                        <i class=" layui-icon layui-icon-close" style="font-size: 30px; color: #1E9FFF;"></i>
                    </a>
                </div>
                {{#}}}
                {{#}}}


            </div>

        </div>
        {{# }); }}


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


<@netCommon.commonScript />

<script src="${request.contextPath}/static/js/jobRerun.js"></script>


</html>
