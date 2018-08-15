<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>任务历史运行记录</title>
    <#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />

</head>

<body class="hold-transition skin-blue-light sidebar-mini">
<div class="wrapper">
    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">
        <section class="content">
            <div class="input-group form-inline col-lg-2">
                <label class="name input-group-addon">任务状态</label>
                <select class="form-control" id="jobStatus" onchange="updateTable()">
                    <option value="failed" selected>失败</option>
                    <option value="success">成功</option>
                    <option value="running">运行中</option>
                </select>
            </div>
            <table id="historyJobTable"></table>
        </section>
    </div>

</div>
</body>

<@netCommon.commonScript />
<script type="text/javascript">

    $(function () {
        var oTable = new TableInit();
        oTable.init();
    });

    function updateTable() {
        $('#historyJobTable').bootstrapTable('refresh');
    }

    var TableInit = function () {
        var oTableInit = new Object();
        oTableInit.init = function () {
            var table = $('#historyJobTable');
            table.bootstrapTable({
                url: base_url + '/jobManage/findJobHistoryByStatus',
                method: 'get',
                pagination: true,
                cache: false,
                clickToSelect: true,
                toolTip: "",
                striped: false,
                showRefresh: true,           //是否显示刷新按钮
                showPaginationSwitch: true,  //是否显示选择分页数按钮
                pageNumber: 1,              //初始化加载第一页，默认第一页
                pageSize: 20,                //每页的记录行数（*）
                sidePagination: "client",
                pageList: [40, 60, 80],
                queryParams: params,
                search: true,
                uniqueId: 'id',
                columns: [
                    {
                        field: '',
                        title: '序号',
                        formatter: function (val, row, index) {
                            return index + 1;
                        }
                    }, {
                        field: 'jobId',
                        title: '任务ID'
                    }, {
                        field: 'jobName',
                        title: '任务名称'
                    }, {
                        field: 'description',
                        title: '任务描述'
                    }, {
                        field: 'startTime',
                        title: '开始时间',
                        formatter: function (val) {
                            return getLocalTime(val);
                        }
                    }, {
                        field: 'times',
                        title: '执行次数'
                    }, {
                        field: 'executeHost',
                        title: '执行服务器'
                    }, {
                        field: 'status',
                        title: '执行状态'
                    }, {
                        field: 'operator',
                        title: '执行人'
                    }
                ]
            });
        }
        return oTableInit;
    }

    function params(params) {
        var temp = {
            status: $('#jobStatus').val(),
        };
        return temp;
    }
</script>

<script src="${request.contextPath}/js/common.js"></script>

</html>