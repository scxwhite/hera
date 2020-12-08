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
                url: base_url + '/job/history',
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

                onClickCell:function(field, value, row, $element){//点击单元格事件
                	if(field === 'groupName'){//点击作业组
                        $('#runningLogDetailTable').bootstrapTable("destroy");
                        var tableObject = new JobLogTable(row.groupId,'group');
                        tableObject.init();
                        $('#jobLog').modal('show');
                	}else if(field === 'jobName'){//点击作业
//                      // console.log(row)
                      $('#runningLogDetailTable').bootstrapTable("destroy");
                      var tableObject = new JobLogTable(row.jobId,'job');
                      tableObject.init();
                      $('#jobLog').modal('show');
                	}
                	else{
                		//do nothing
                	}
                },

//                onClickRow: function (row) {
//                    // console.log(row)
//                    $('#runningLogDetailTable').bootstrapTable("destroy");
//                    var tableObject = new JobLogTable(row.jobId);
//                    tableObject.init();
//                    $('#jobLog').modal('show');
//                },
                columns: [
                    {
                        field: 'groupName',
                        title: '任务组',
                        halign: 'center',
                        align: 'center',
                        sortable: true,
                    }, {
                        field: 'jobName',
                        title: '任务名称',
                        sortable: true,
                        halign: 'center',
                        align: 'center',

                    }, {
                        field: 'description',
                        halign: 'center',
                        align: 'center',
                        title: '任务描述',
                        formatter: function (val) {
                            if (val == null) {
                                return val;
                            }
                            return '<label class="label label-default" style="width: 100%;" data-toggle="tooltip" title="' + val + '" >' + val.slice(0, 20) + '</label>';
                        }
                    }, {
                        field: 'status',
                        title: '状态',
                        halign: 'center',
                        align: 'center',
                        sortable: true,
                        formatter: function (val) {
                            if (val === 'running') {
                                return '<a class="layui-btn layui-btn-xs layui-btn-warm" style="width: 100%;">' + '运行中' + '</a>';
                            }
                            if (val === 'success') {
                                return '<a class="layui-btn layui-btn-xs" style="width: 100%;background-color:#2f8f42" >' + '成功' + '</a>';
                            }
                            if (val === 'wait') {
                                return '<a class="layui-btn layui-btn-xs layui-btn-disabled" style="width: 100%;">' + '等待' + '</a>';
                            }
                            return '<a class="layui-btn layui-btn-xs layui-btn-danger" style="width: 100%;" >' + '失败' + '</a>'
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
                        field: 'endTime',
                        title: '结束时间',
                        halign: 'center',
                        align: 'center',
                        formatter: function (val) {
                            return getLocalTime(val);
                        },
                        sortable: true
                    }, {
                        field: 'times',
                        halign: 'center',
                        align: 'center',
                        title: '次数'
                    }, {
                        field: 'bizLabel',
                        halign: 'center',
                        align: 'center',
                        sortable: true,
                        title: '标签'
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
            begindt: $('#jobDt').val(),
            enddt: $('#jobDt_end').val(),
        };
        return temp;
    }

    var JobLogTable = function (jobId,JobType) {
        var parameter = {jobId: jobId,JobType:JobType};
        var actionRow;
        var oTableInit = new Object();
        var table = $('#runningLogDetailTable');

        function scheduleLog() {

            $.ajax({
                url: base_url + "/scheduleCenter/getLog.do",
                type: "get",
                data: {
                    id: actionRow.id,
                    jobId: actionRow.jobId
                },
                success: function (result) {
                    var logArea = $('#log_' + actionRow.id);
                    if (result.success === false) {
                        layer.msg(result.message);
                        logArea[0].innerHTML = "无日志查看权限,请联系管理员进行配置";
                        return;
                    }
                    let data = result.data;
                    if (data.status === 'running') {
                        window.setTimeout(scheduleLog, 5000);
                    }
                    logArea[0].innerHTML = data.log;
                    logArea.scrollTop(logArea.prop("scrollHeight"), 200);
                    actionRow.log = data.log;
                    actionRow.status = data.status;
                }
            })
        }


        $('#jobLog [name="refreshLog"]').on('click', function () {
            table.bootstrapTable('refresh');
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
                        beginDt: $('#jobDt').val(),
                        endDt: $('#jobDt_end').val(),
                        jobType: JobType,
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
                    //隐藏某个列
                    if(JobType === 'job'){
                    	table.bootstrapTable('hideColumn', 'id');
                    }else{
                    	table.bootstrapTable('hideColumn', 'id');
                    	table.bootstrapTable('hideColumn', 'executeHost');
                    }
                },
                pageList: [10, 25, 50, 100],
                columns: [
                    {
                        field: "id",
                        title: "ID",
                        width: "60px",
                        align: 'center',
                        halign: 'center'
                    }, {
                        field: "groupName",
                        title: "任务组",
                        align: 'center',
                        halign: 'center'
                    }

                    , {
                        field: "jobName",
                        title: "任务名称",
                        align: 'center',
                        halign: 'center',
                        formatter: function (index, row) {
                        	html='<a title="查看任务配置" href="javascript:toJobPage(' + row['jobId'] + ')" > ' + row['jobName']+'['+row['jobId']+']' + '</a>';
                        	return html;
                            //return '<label class="label label-default" style="width: 100%;" data-toggle="tooltip" title="任务ID=' + row['jobId'] + '" >' + row['jobName'] + '</label>';
                        }
                    },{
                        field: "batchId",
                        title: "批次号",
                        halign: 'center',
                        align: 'center',
                        formatter: function (index, row) {
                        	let backInfo = "版本号=" + row['actionId'] ;
                            return '<label class="label label-default" style="width: 100%;" data-toggle="tooltip" title="' + backInfo + '" >' + row['batchId'] + '</label>';

                        }
                    }, {
                        field: "status",
                        title: "状态",
                        width: "60px",
                        halign: 'center',
                        align: 'center',
                        formatter: function (val) {
                        	let v_status='#FF5722';
                        	let vval='失败';
                      	    if (val === 'running') {
                      		  v_status='#FFB800';
                      		  vval='运行中';
                            }else if (val === 'success') {
                          	  v_status='#2f8f42';
                          	  vval='成功';
                            }else if (val === 'wait') {
                          	  v_status='#2F4056';
                          	  vval='等待';
                            }
                      	    let re='<a class="layui-btn layui-btn-xs" style="width: 100%;background-color:'+ v_status +'">' +  vval + '</a>';
                  		    return re;
                        }
                    },
                    {
                        field: "timeCost",
                        title: "时间轴",
                        width: "250px",
                        halign: 'center',
                        align: 'center',
                        formatter: function (index, row) {
                        	let v_status='#FF5722';//fail
                      	    if (row['status'] === 'running') {
                      		  v_status='#FFB800';
                            }else if (row['status'] === 'success') {
                          	  v_status='#2f8f42';
                            }else if (row['status'] === 'wait') {
                          	  v_status='#2F4056';
                            }
                    	  let x1_len=row['begintime240px'];
                    	  let x2_len=parseInt(row['dur240px']);
                    	  if (x2_len == 0){
                    		  x2_len = 1;
                    	  }
                    	  let x3_len=240-x1_len-x2_len;

                    	  let vv='#F0F0F0';
                    	  let xx=x1_len;
                    	  let x1='<td style="width: '+xx+'px;background-color:'+ vv +'">&nbsp</td>';

                    	  //let x1='<td class="layui-bg-gray" style="width: '+x1_len+'px;">&nbsp</td>';
                    	  if (x1_len <= 0){
                    		  x1='';
                    	  }

                    	  vv=v_status;
                    	  xx=x2_len;
                    	  let x2='<td style="width: '+xx+'px;background-color:'+ vv +'">&nbsp</td>';

                    	  //let x2='<td class="layui-bg-'+ v_status  +'" style="width: '+x2_len+'px;">&nbsp</td>';
                    	  //let x3='<td class="layui-bg-gray" style="width: '+x3_len+'px;">&nbsp</td>';

                    	  vv='#F0F0F0';
                    	  xx=x3_len;
                    	  let x3='<td style="width: '+xx+'px;background-color:'+ vv +'">&nbsp</td>';
                    	  if (x3_len <= 0){
                    		  x3='';
                    	  }
                    	  let re=' <table style="border-collapse:collapse;" > <tr style="width: 240px;" class="site-doc-color"> ' + x1+x2+x3 +' </tr></table>';
                  		  return re;
                      }
                    }
                    ,
                    {
                        field: "startTime",
                        title: "开始时间",
                        width: "160px",
                        align: 'center',
                        halign: 'center',
                        formatter: function (val) {
                        	if (val == null) {
                              return val;
                        		}
                            return val.slice(0, 19);
                            }
                    }, {
                        field: "endTime",
                        title: "结束时间",
                        width: "160px",
                        align: 'center',
                        halign: 'center',
                        formatter: function (val) {
                        	if (val == null) {
                                return val;
                          		}
                              return val.slice(0, 19);
                            }
                      }, {
                        field: "durations",
                        title: "时长(分)",
                        width: "60px",
                        halign: 'center',
                        align: 'center',
                        formatter: function (index, row) {
                            let st = new Date(row['startTime']);
                            if (row['endTime'] == null || row['endTime'] == '') {
                                let ed = new Date();
                                return (parseInt(ed - st) / 1000.0 / 60.0).toFixed(1);
                            } else {
                            	if(row['startTime'] == null || row['startTime'] == '') {
                            		return 0;
                            		}
                            	else {
                                    let ed = new Date(row['endTime']);
                                    return (parseInt(ed - st) / 1000.0 / 60.0).toFixed(1);
                            	}
                            }
                        }
                    }, {
                        field: "bizLabel",
                        title: "标签",
                        align: 'center',
                        halign: 'center'
                    } , {
                        field: "operate",
                        title: "操作",
                        width: "160px",
                        halign: 'center',
                        align: 'center',
                        formatter: function (index, row) {

                            let html_cancelJob_click = '<a href="javascript:cancelJob(\'' + row['id'] + '\',\'' + row['jobId'] + '\')">取消</a>';
                            let html_cancelJob_nonclick = '<a href ="javascript:return false;" style="opacity: 0.2" >取消</a>';

                            let html_manualCurr_click = '<a href ="javascript:manualRecoveryJob(\'' + row['actionId'] + '\',\'' + 2 + '\')" >重做当前</a>';
                            let html_manualCurr_nonclick = '<a href ="javascript:return false;" style="opacity: 0.2" >重做当前</a>';

                            let html_manualNexts_click = '<a href ="javascript:manualRecoveryJob(\'' + row['actionId'] + '\',\'' + 3 + '\')" >重做后续</a>';
                            let html_manualNexts_nonclick = '<a href ="javascript:return false;" style="opacity: 0.2" >重做后续</a>';

                            let html_forceOk_click = '<a href ="javascript:manualRecoveryJob(\'' + row['actionId'] + '\',\'' + 2 + '\')  "" >强制成功</a>';
                            let html_forceOk_nonclick = '<a href ="javascript:return false;" style="opacity: 0.2" >强制成功</a>';

                            let js_cancelJob_click = '<a href ="javascript:cancelJobFun(\'' + row['id'] + '\',\'' + row['jobId'] + '\')">取消</a>';
                            let js_cancelJob_nonclick = '<a href ="javascript:return false;" style="opacity: 0.2" >取消</a>';

                            let js_manualJob_click = '<a href ="javascript:manualJobFun(\'' + row['actionId'] + '\')" >重做</a>';
                            let js_manualJob_nonclick = '<a href ="javascript:return false;" style="opacity: 0.2" >重做</a>';

                            let js_manualForce_click = '<a href ="javascript:manualForceFun(\'' + row['id'] + '\')" >强制</a>';
                            let js_manualForce_nonclick = '<a href ="javascript:return false;" style="opacity: 0.2" >强制</a>';

                            if (row['status'] === 'running') {
                            	re =  js_manualJob_nonclick +"|"+js_cancelJob_click +"|"+js_manualForce_nonclick ;
                            }else if (row['status'] === 'success'){
                            	re =  js_manualJob_click +"|"+js_cancelJob_nonclick +"|"+js_manualForce_click ;
                            }else if (row['status'] === 'failed'){
                            	re =  js_manualJob_click +"|"+js_cancelJob_nonclick +"|"+js_manualForce_click ;
                            }else if (row['status'] === 'wait'){
                            	re =  js_manualJob_nonclick +"|"+js_cancelJob_click +"|"+js_manualForce_click ;
                            }else {
                            	re =  js_manualJob_nonclick +"|"+js_cancelJob_nonclick +"|"+js_manualForce_nonclick ;
                            }

                            logdetail='<a target="_blank" href="scheduleCenter/jobInstLog?hisId=' + row['id'] + '&id=' + row['jobId']  +'"> 日志</a>'

                            return re + "|" + logdetail;
                        }
                    }, {
                        field: "description",
                        title: "任务描述",
                        align: 'center',
                        halign: 'center',
                        formatter: function (val) {
                            if (val == null) {
                                return val;
                            }
                            return '<label class="label label-default" style="width: 100%;" data-toggle="tooltip" title="' + val + '" >' + val.slice(0, 20) + '</label>';
                        }
                    }, {
                        field: "triggerType",
                        title: "触发类型",
                        width: "60px",
                        formatter: function (value, row) {
                        	let tmp=value;
                        	let t2='label-default';
                            if (value == 1) {
                            	tmp="自动调度";
                            	t2='label-default';
                            }
                            else if (value == 2) {
                            	tmp="手动触发";
                            	t2='label-primary';
                            }
                            else if (value == 3) {
                            	tmp="手动恢复";
                            	t2='label-info';
                            }
                            let backInfo = row['illustrate'] ;
                            let re='<label class="label ' + t2 + ' style="width: 100%;" data-toggle="tooltip" title="' + backInfo + '" >' + tmp + '</label>';
                            return re;
                        }
                    }
                    , {
                        field: "executeHost",
                        title: "机器|执行人",
                        halign: 'center',
                        align: 'center',
                        formatter: function (index, row) {
                            return row['executeHost'] + ' | ' + row['operator'];
//                        	let backInfo = "执行人=" + row['operator'] ;
//                            return '<label class="label label-default" style="width: 100%;" data-toggle="tooltip" title="' + backInfo + '" >' + row['executeHost'] + '</label>';

                        }
                    }
                ],
//                detailView: true,
//                detailFormatter: function (index, row) {
//                    var html = '<form role="form">' + '<div class="form-group">' + '<div class="form-control"  style="overflow:scroll; word-break: break-all; word-wrap:break-word; height:600px; white-space:pre-line;font-family:Microsoft YaHei" id="log_' + row.id + '">'
//                        + '日志加载中。。' +
//                        '</div>' + '<form role="form">' + '<div class="form-group">';
//                    return html;
//                },
                onExpandRow: function (index, row) {
                    actionRow = row;
                    scheduleLog();
                }
            });
        };
        return oTableInit;
    };

});







function updateTable() {
    $('#historyJobTable').bootstrapTable('refresh');
}


//function cancelJob(historyId, jobId) {
//    var url = base_url + "/scheduleCenter/cancelJob.do";
//    var parameter = {historyId: historyId, jobId: jobId};
//    $.get(url, parameter, function (data) {
//        layer.msg(data);
//        $('#jobLog [name="refreshLog"]').trigger('click');
//    });
//}





//重做作业
function manualJobFun(actionId) {
	manualJobFunActionId=actionId
	$('#myManualJob').modal('show');
}


//重做任务弹出页
$("#myManualJob .add-btn").click(function () {
    $.ajax({
        url: base_url + "/scheduleCenter/manual.do",
        type: "get",
        data: {
            actionId: manualJobFunActionId,
            triggerType: $("#myManualJobType").val()
        },
        success: function (res) {
            if (res.success === true) {
                layer.msg('执行成功！'+res.message);
            } else {
                layer.msg(res.message)
            }
        },
        error: function (err) {
            layer.msg(err);
        }
    });
    $('#myManualJob').modal('hide');
});




//强制作业
function manualForceFun(jobHisId) {
	manualJobHisId=jobHisId
	$('#myManualForce').modal('show');
}


//强制作业弹出页
$("#myManualForce .add-btn").click(function () {
    $.ajax({
        url: base_url + "/scheduleCenter/forceJobHisStatus.do",
        type: "get",
        data: {
        	jobHisId: manualJobHisId,
            status: $("#myManualForceType").val()
        },
        success: function (res) {
            if (res.success === true) {
                layer.msg('执行成功');
            } else {
                layer.msg(res.message)
            }
        },
        error: function (err) {
            layer.msg(err);
        }
    });
    $('#myManualForce').modal('hide');
});



//取消任务
function cancelJobFun(historyId, jobId) {
	cancelJobFunhistoryId=historyId
	cancelJobFunjobId=jobId
	$('#mycancelJobFun').modal('show');
}


//取消任务弹出页
$("#mycancelJobFun .add-btn").click(function () {
  $.ajax({
      url: base_url + "/scheduleCenter/cancelJob.do",
      type: "get",
      data: {
      	   historyId: cancelJobFunhistoryId,
    	   jobId: cancelJobFunjobId
      },
      success: function (res) {
          if (res.success === true) {
        	  layer.msg('执行成功！'+res.message);
          } else {
              layer.msg(res.message)
          }
      },
      error: function (err) {
          layer.msg(err);
      }
  });
  $('#mycancelJobFun').modal('hide');
});

