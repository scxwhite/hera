<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>任务历史运行记录</title>
    <#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link href="https://cdn.bootcss.com/bootstrap-table/1.11.1/bootstrap-table.min.css" rel="stylesheet">

    <style>
        .table-hover > tbody > tr:hover {
            cursor: pointer;
        }
        #toolbar{
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
                        <div class="input-group form-inline col-lg-2 pull-right" style="margin-left: 4px" >
                        	<label class="name input-group-addon">任务状态</label>
                            <select class="form-control" id="jobDt" onchange="updateTable()">
                                <option value="1" selected>当天</option>
                                <option value="3">3天</option>
                                <option value="7">7天</option>
                                <option value="32">32天</option>
                                <option value="93">93天</option>
                            </select>
                            <label class="name input-group-addon">任务状态</label>
                            <select class="form-control" id="jobStatus" onchange="updateTable()">
                                <option value="all" selected>全部</option>
                                <option value="failed">失败</option>
                                <option value="success">成功</option>
                                <option value="running">运行中</option>
                                <option value="wait">等待</option>
                            </select>
                        </div>
                        <table id="historyJobTable" class="table-striped" ></table>
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
<script src="${request.contextPath}/js/jobDetail.js"></script>
<script src="https://cdn.bootcss.com/bootstrap-table/1.11.1/bootstrap-table.min.js"></script>
<script src="https://cdn.bootcss.com/bootstrap-table/1.11.1/locale/bootstrap-table-zh-CN.min.js"></script>


</html>