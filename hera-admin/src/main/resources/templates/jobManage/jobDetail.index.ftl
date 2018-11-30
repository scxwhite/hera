<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>任务历史运行记录</title>
    <#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <style>
        .table-hover > tbody > tr:hover {
            cursor: pointer;
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
                    <div class="box-header">
                        <h3 class="box-title">机器组管理</h3>
                    </div>
                    <div class="box-body">
                        <div id="toolbar">
                            <button class="btn btn-success" id="addHostGroup">添加</button>
                        </div>

                        <div class="input-group form-inline col-lg-2 pull-right" style="margin-left: 4px" >
                            <label class="name input-group-addon">任务状态</label>
                            <select class="form-control" id="jobStatus" onchange="updateTable()">
                                <option value="failed" selected>失败</option>
                                <option value="success">成功</option>
                                <option value="running">运行中</option>
                            </select>
                        </div>
                        <table id="historyJobTable" class="table-striped" ></table>
                    </div>
                </div>
        </section>
    </div>
</div>

<div class="modal fade" id="jobLog" tabindex="-1" role="dialog" aria-labelledby="jobLog" aria-hidden="true">
    <div class="modal-dialog" style="width: 80%">
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


</html>