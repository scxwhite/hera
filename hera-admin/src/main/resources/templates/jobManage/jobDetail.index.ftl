<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>任务历史详情</title>
    <#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />

    <link href="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table.min.css" rel="stylesheet">
  	<link href="${request.contextPath}/static/adminlte/bootstrap/css/bootstrap-datetimepicker.min.css" rel="stylesheet" />




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
                        <div class="input-group form-inline col-lg-4 pull-right" style="margin-left: 100px" >
                            <label class="name input-group-addon">状态</label>
                            <select class="form-control" id="jobStatus" onchange="updateTable()">
                                <option value="all" selected>全部</option>
                                <option value="failed">失败</option>
                                <option value="success">成功</option>
                                <option value="running">运行中</option>
                                <option value="wait">等待</option>
                            </select>
                            <label class="name input-group-addon">日期</label>
                            <input class="form_datetime form-control" id="jobDt" size="12" type="text" readonly placeholder="请选择日期"  onchange="updateTable()">

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
<script src="${request.contextPath}/static/js/jobDetail.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table-zh-CN.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bootstrap/js/bootstrap-datetimepicker.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bootstrap/js/bootstrap-datetimepicker.zh-CN.js"></script>

<script type="text/javascript">
 $(".form_datetime").datetimepicker({
 format: "yyyy-mm-dd",
 autoclose: true,
 todayBtn: true,
 todayHighlight: true,
 language: 'zh-CN',//中文，需要引用zh-CN.js包
 startView: 2,//月视图
 minView: 2,//日期时间选择器所能够提供的最精确的时间选择视图


 });
</script>

<script>
    $(document).ready(function () {
        var time = new Date();
        var day = ("0" + time.getDate()).slice(-2);
        var month = ("0" + (time.getMonth() + 1)).slice(-2);
        var today = time.getFullYear() + "-" + (month) + "-" + (day);
        $(".form_datetime").val(today);
    })
</script>



</html>
