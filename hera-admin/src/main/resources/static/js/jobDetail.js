layui.use(['table'], function () {


    $('#jobDetailMenu').addClass('active');
    $('#jobDetailMenu').parent().addClass('menu-open');
    $('#jobDetailMenu').parent().parent().addClass('menu-open');
    $('#jobManage').addClass('active');

    function cancelJob(historyId, jobId) {
        var url = base_url + "/scheduleCenter/cancelJob.do";
        var parameter = {historyId: historyId, jobId: jobId};
        $.get(url, parameter, function (data) {
            layer.msg(data);
            $('#jobLog [name="refreshLog"]').trigger('click');
        });
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
                showPaginationSwitch: false,  //是否显示选择分页数按钮
                pageNumber: 1,              //初始化加载第一页，默认第一页
                pageSize: 20,                //每页的记录行数（*）
                pageList: [40, 60, 80],
                queryParams: params,
                search: true,
                uniqueId: 'id',
                sidePagination: "client",
                searchAlign: 'left',
                buttonsAlign: 'left',
                onClickRow: function (row) {
                    // console.log(row)
                    $('#runningLogDetailTable').bootstrapTable("destroy");
                    var tableObject = new JobLogTable(row.jobId);
                    tableObject.init();
                    $('#jobLog').modal('show');
                },
                columns: [
                    {
                        field: '',
                        title: '序号',
                        halign: 'center',
                        align: 'center',
                        formatter: function (val, row, index) {
                            return index + 1;
                        }
                    }, {
                        field: 'groupName',
                        title: '任务组',
                        halign: 'center',
                        align: 'center',
                        sortable: true
                    }, {
                        field: 'jobName',
                        title: '任务名称',
                        sortable: true,
                        halign: 'center',
                        align: 'center',
                        formatter: function (val, row, index) {
                            let val01 = '<a href = "#">' + val + '[' + row['jobId'] + ']' + '</a>';
                            return val01;
                        }
                    }, {
                        field: 'description',
                        halign: 'center',
                        align: 'center',
                        title: '任务描述'
                    }, {
                        field: 'status',
                        title: '状态',
                        halign: 'center',
                        align: 'center',
                        formatter: function (val) {
                            if (val === 'running') {
                                return '<a class="layui-btn layui-btn-xs layui-btn-warm" style="width: 100%;">' + val + '</a>';
                            }
                            if (val === 'success') {
                                return '<a class="layui-btn layui-btn-xs" style="width: 100%;background-color:#2f8f42" >' + val + '</a>';
                            }
                            if (val === 'wait') {
                                return '<a class="layui-btn layui-btn-xs layui-btn-disabled" style="width: 100%;">' + val + '</a>';
                            }
                            return '<a class="layui-btn layui-btn-xs layui-btn-danger" style="width: 100%;" >' + val + '</a>'
                        }
                    }, {
                        field: 'startTime',
                        title: '开始时间',
                        halign: 'center',
                        align: 'center',
                        formatter: function (val) {
                            return getLocalTime(val);
                        },
                        sortable: true
                    }, {
                        field: 'durations',
                        title: '时长(分)',
                        halign: 'center',
                        align: 'center',
                        sortable: true
                    }, {
                        field: 'times',
                        halign: 'center',
                        align: 'center',
                        title: '次数'
                    }, {
                        field: "executeHost",
                        title: "机器|执行人",
                        halign: 'center',
                        align: 'center',
                        formatter: function (index, row) {
                            let val01 = row['executeHost'] + ' | ' + row['operator'];
                            return val01;
                        }
                    }
                ],
                // data:info.data
            });
        }
        return oTableInit;
    }

    var oTable = new TableInit();
    oTable.init();
    $('#historyJobTable').bootstrapTable('hideLoading');


    function params(params) {
        var temp = {
            status: $('#jobStatus').val(),
            dt: $('#jobDt').val(),
        };
        return temp;
    }

    var JobLogTable = function (jobId) {
        var parameter = {jobId: jobId};
        var actionRow;
        var oTableInit = new Object();
        var onExpand = -1;
        var table = $('#runningLogDetailTable');
        var timerHandler = null;


        function scheduleLog() {

            $.ajax({
                url: base_url + "/scheduleCenter/getLog.do",
                type: "get",
                data: {
                    id: actionRow.id,
                },
                success: function (result) {
                    if (result.success === false) {
                        layer.msg(data.message);
                        return;
                    }
                    let data = result.data;
                    if (data.status != 'running') {
                        window.clearInterval(timerHandler);
                    }
                    var logArea = $('#log_' + actionRow.id);
                    logArea[0].innerHTML = data.log;
                    logArea.scrollTop(logArea.prop("scrollHeight"), 200);
                    actionRow.log = data.log;
                    actionRow.status = data.status;
                }
            })
        }

        $('#jobLog').on('hide.bs.modal', function () {
            if (timerHandler != null) {
                window.clearInterval(timerHandler)
            }
        });

        $('#jobLog [name="refreshLog"]').on('click', function () {
            table.bootstrapTable('refresh');
            table.bootstrapTable('expandRow', onExpand);
        });


        oTableInit.init = function () {
            table.bootstrapTable({
                url: base_url + "/scheduleCenter/getJobHistory.do",
                queryParams: parameter,
                pagination: true,
                showPaginationSwitch: false,
                search: false,
                cache: false,
                pageNumber: 1,
                showRefresh: true,           //是否显示刷新按钮
                showPaginationSwitch: false,  //是否显示选择分页数按钮
                sidePagination: "server",
                queryParamsType: "limit",
                queryParams: function (params) {
                    var tmp = {
                        pageSize: params.limit,
                        offset: params.offset,
                        jobId: jobId
                    };
                    return tmp;
                },
                onLoadSuccess: function (data) {
                    if (data.success === false) {
                        layer.msg("加载日志失败");
                        return;
                    }
                    table.bootstrapTable("load", data.data)
                },
                pageList: [10, 25, 40, 60],
                columns: [
                    {
                        field: "id",
                        title: "id",
                        width: "4%",
                        align: 'center',
                        halign: 'center'
                    }, {
                        field: "actionId",
                        title: "版本号",
                        width: "11%",
                        halign: 'center',
                        align: 'center',
                        formatter: function (val) {
                            let val01 = val.substring(0, 8);
                            let val02 = val.substring(8, 14);
                            let val03 = val.substring(14);
                            let re = '<a class="text-primary" >' + val01 + '</a>' + '<a class="text-warning" >' + val02 + '</a>' + '<a class="text-success" >' + val03 + '</a>';
                            return re;
                        }
                    }, {
                        field: "jobId",
                        title: "任务ID",
                        width: "5%",
                        align: 'center',
                        halign: 'center'
                    }, {
                        field: "status",
                        title: "执行状态",
                        width: "6%",
                        halign: 'center',
                        align: 'center',
                        formatter: function (val) {
                            if (val === 'running') {
                                return '<a class="layui-btn layui-btn-xs layui-btn-warm" style="width: 100%;">' + val + '</a>';
                            }
                            if (val === 'success') {
                                return '<a class="layui-btn layui-btn-xs" style="width: 100%;background-color:#2f8f42" >' + val + '</a>';
                            }
                            if (val === 'wait') {
                                return '<a class="layui-btn layui-btn-xs layui-btn-disabled" style="width: 100%;">' + val + '</a>';
                            }
                            return '<a class="layui-btn layui-btn-xs layui-btn-danger" style="width: 100%;" >' + val + '</a>'
                        }
                    }, {
                        field: "startTime",
                        title: "开始时间",
                        width: "16%",
                        align: 'center',
                        halign: 'center'

                    }, {
                        field: "endTime",
                        title: "结束时间",
                        width: "16%",
                        align: 'center',
                        halign: 'center'

                    }, {
                        field: "durations",
                        title: "时长(分)",
                        width: "5%",
                        halign: 'center',
                        align: 'center',
                        formatter: function (index, row) {
                            let st = new Date(row['startTime']);
                            if (row['endTime'] == null || row['endTime'] == '') {
                                let ed = new Date();
                                return (parseInt(ed - st) / 1000.0 / 60.0).toFixed(1);
                            } else {
                                let ed = new Date(row['endTime']);
                                return (parseInt(ed - st) / 1000.0 / 60.0).toFixed(1);
                            }
                        }
                    }
                    , {
                        field: "illustrate",
                        title: "说明",
                        width: "7%",
                        halign: 'center',
                        align: 'center',
                        formatter: function (val) {
                            if (val == null) {
                                return val;
                            }
                            return '<label class="label label-default" style="width: 100%;" data-toggle="tooltip" title="' + val + '" >' + val.slice(0, 6) + '</label>';
                        }
                    },
                    {
                        field: "triggerType",
                        title: "触发类型",
                        width: "5%",
                        halign: 'center',
                        align: 'center',
                        formatter: function (value, row) {
                            if (row['triggerType'] == 1) {
                                return "自动调度";
                            }
                            if (row['triggerType'] == 2) {
                                return "手动触发";
                            }
                            if (row['triggerType'] == 3) {
                                return "手动恢复";
                            }
                            return value;
                        }
                    },
                    {
                        field: "executeHost",
                        title: "机器|执行人",
                        width: "12%",
                        halign: 'center',
                        align: 'center',
                        formatter: function (index, row) {
                            return row['executeHost'] + ' | ' + row['operator'];
                        }
                    }, {
                        field: "status",
                        title: "操作",
                        width: "5%",
                        halign: 'center',
                        align: 'center',
                        formatter: function (index, row) {
                            let html = '<a href="javascript:cancelJob(\'' + row['id'] + '\',\'' + row['jobId'] + '\')">取消任务</a>';
                            if (row['status'] === 'running') {
                                return html;
                            }
                        }
                    }
                ],
                detailView: true,
                detailFormatter: function (index, row) {
                    var html = '<form role="form">' + '<div class="form-group">' + '<div class="form-control"  style="overflow:scroll; word-break: break-all; word-wrap:break-word; height:600px; white-space:pre-line;font-family:Microsoft YaHei" id="log_' + row.id + '">'
                        + '日志加载中。。' +
                        '</div>' + '<form role="form">' + '<div class="form-group">';
                    return html;
                },
                onExpandRow: function (index, row) {
                    actionRow = row;
                    if (index != onExpand) {
                        table.bootstrapTable("collapseRow", onExpand);
                    }
                    onExpand = index;
                    if (row.status == "running") {
                        scheduleLog();
                        timerHandler = window.setInterval(scheduleLog, 3000);
                    } else {
                        scheduleLog();
                    }
                },
                onCollapseRow: function (index, row) {
                    window.clearInterval(timerHandler)
                }
            });
        };
        return oTableInit;
    };

});

function updateTable() {
    $('#historyJobTable').bootstrapTable('refresh');
}