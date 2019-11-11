<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>任务搜索</title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />

    <link href="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table.min.css"
          rel="stylesheet">
    <link href="${request.contextPath}/static/adminlte/bootstrap/css/bootstrap-datetimepicker.min.css"
          rel="stylesheet"/>


    <style>
        .table-hover > tbody > tr:hover {
            cursor: pointer;
        }

        #toolbar {
            margin-bottom: 4px;
        }
    </style>
</head>

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
                    <form class="form-inline" role="form" id="searchForm">
                        <div class="form-group">
                            <input type="text" class="form-control" name="script" placeholder="脚本内容">
                        </div>

                        <div class="form-group">
                            <input type="text" class="form-control" name="name" placeholder="任务名称">
                        </div>

                        <div class="form-group">
                            <input type="text" class="form-control" name="description" placeholder="描述内容">
                        </div>

                        <div class="form-group">
                            <input type="text" class="form-control" name="config" placeholder="变量内容">
                        </div>

                        <div class="form-group">
                            <select class="form-control" name="auto">
                                <option style="display: none;" disabled selected>任务状态</option>
                                <option value="0">关闭</option>
                                <option value="1">开启</option>
                                <option value="2">失效</option>
                                <option value="">所有</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <select class="form-control" name="runType">
                                <option style="display: none;" disabled selected>任务类型</option>
                                <option value="shell">shell</option>
                                <option value="spark">spark</option>
                                <option value="hive">hive</option>
                                <option value="">all</option>
                            </select>
                        </div>
                        <input type="button" value="搜索" id="searchBtn" class="btn btn-primary pull-right">
                    </form>

                    <br>
                    <table id="searchTable"></table>
                </div>
            </div>
        </section>
    </div>
</div>

<div class="modal fade" id="jobLog" tabindex="-1" role="dialog" aria-labelledby="jobLog" aria-hidden="true">
    <div class="modal-dialog" style="width: 90%">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" id="myModalLabel">信息日志</h4>
            </div>

            <div class="modal-body">
                <table class="table " id="runningLogDetailTable"></table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">返回</button>
                <button type="button" class="btn btn-info add-btn" name="refreshLog">刷新</button>
            </div>
        </div>
    </div>
</div>

</body>

<@netCommon.commonScript />
<script src="${request.contextPath}/static/js/jobSearch.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table-zh-CN.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bootstrap/js/bootstrap-datetimepicker.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bootstrap/js/bootstrap-datetimepicker.zh-CN.js"></script>


</html>