$(function () {
    var focusId = -1;
    var focusItem = null;
    var isGroup;
    var treeObj;
    var selected;
    var triggerType;
    var actionId;
    var setting = {
        view: {
            showLine: false
        },
        data: {
            simpleData: {
                enable: true,
                idKey: "id",
                pIdKey: "parent",
                rootPId: 0
            }
        },
        callback: {
            onRightClick: OnRightClick,
            onClick: leftClick
        }
    };

    /**
     * 把当前选中的节点存入localStorage
     * 页面刷新后，会根据"defaultId"设置当前选中的节点
     * 避免页面刷新丢失
     * @param id    节点ID
     */
    function setCurrentId(id) {
        localStorage.setItem("defaultId", id);

    }

    /**
     * 设置当前默认选中的节点
     * @param id    节点ID
     */
    function setDefaultSelectNode(id) {
        if (id != undefined && id != null) {
            treeObj.selectNode(treeObj.getNodeByParam("id", id));
            leftClick();
        }

    }

    /**
     * 切换任务编辑状态
     * @param status
     */
    function changeEditStyle(status) {
        //默认 展示状态
        var val1 = "block", val2 = "none", val3 = true;
        //编辑状态
        if (status == 0) {
            val1 = "none";
            val2 = "block";
            val3 = false;
        }
        $('#jobMessage').css("display", val1);
        $('#jobMessageEdit').css("display", val2);
        $('#config textarea').attr('disabled', val3);
        $('#script textarea').attr('disabled', val3);
        $('#resource textarea').attr('disabled', val3);
        $('#jobOperate').css("display", val1);
        $('#editOperator').css("display", val2);
        $('#groupMessage').css("display", "none");
        $('#groupMessageEdit').css("display", "none");
    }

    /**
     * 任务编辑事件
     */
    $('#jobOperate [name="edit"]').on('click', function () {
        //回显
        formDataLoad("jobMsgEditForm", focusItem);
        initVal(focusItem.configs, "jobMsgEditForm");
        changeEditStyle(0);
        setJobMessageEdit(focusItem.scheduleType === 0)
    });

    /**
     * 查看任务日志
     */
    $('#jobOperate [name="runningLog"]').on('click', function () {

        $('#runningLogDetailTable').bootstrapTable("destroy");
        var tableObject = new JobLogTable(focusId);
        tableObject.init();

        $('#jobLog').modal('show');

    });

    /**
     * 版本生成
     */
    $('#jobOperate [name="version"]').on('click', function () {

        $.ajax({
            url: base_url + "/scheduleCenter/generateVersion",
            data: {
                jobId: focusId
            },
            type: "post",
            success: function (data) {
                alert(data);
            }
        })
    });
    /**
     * 任务开启关闭按钮
     */
    $('#jobOperate [name="switch"]').on('click', function () {
        //回显
        $.ajax({
            url: base_url + "/scheduleCenter/changeSwitch",
            data: {
                id: focusId
            },
            type: "post",
            success: function (data) {
                if (data.code === 200) {
                    leftClick();
                } else {
                    dealCode(data);
                }
            }
        })
    });
    /**
     * 添加任务按钮的初始化操作
     */
    $('#groupOperate [name="addJob"]').on('click', function () {
        $('#addJobModal .modal-title').text(focusItem.name + "下新建任务");
        $('#addJobModal [name="jobName"]').val("");
        $('#addJobModal [name="jobType"]').val("MapReduce");
        $('#addJobModal').modal('show');
    });
    /**
     * 确认添加任务
     */
    $('#addJobModal [name="addBtn"]').on('click', function () {
        var name = $('#addJobModal [name="jobName"]').val();
        var type = $('#addJobModal [name="jobType"]').val();
        if (name == undefined || name == null || name.trim() == "") {
            alert("任务名不能为空");
            return;
        }
        $.ajax({
            url: base_url + "/scheduleCenter/addJob.do",
            type: "post",
            data: {
                name: name,
                runType: type,
                groupId: focusId
            },
            success: function (data) {

                if (data.code == 200) {
                    location.reload(false);
                } else {
                    dealCode(data);
                }

            }
        })

    });
    /**
     * 选择框事件 动态设置编辑区
     */
    $('#jobMessageEdit [name="scheduleType"]').change(function () {
        var status = $(this).val();
        //定时调度
        if (status == 0) {
            setJobMessageEdit(true);
        } else if (status == 1) {//依赖调度
            setJobMessageEdit(false);
        }
    });

    /**
     * 动态变化任务编辑界面
     * @param val
     */
    function setJobMessageEdit(val) {
        var status1 = "block", status2 = "none";
        if (!val) {
            status1 = "none";
            status2 = "block";
        }
        $("#jobMessageEdit [name='cronExpression']").parent().parent().css("display", status1);
        $("#jobMessageEdit [name='dependencies']").parent().parent().css("display", status2);
        $("#jobMessageEdit [name='heraDependencyCycle']").parent().parent().css("display", status2);
    }

    /**
     * 编辑返回
     */
    $('#editOperator [name="back"]').on('click', function () {
        if (!isGroup) {
            changeEditStyle(1);
        } else {
            changeGroupStyle(1);

        }
    });
    /**
     * 保存按钮
     */
    $('#editOperator [name="save"]').on('click', function () {
        if (!isGroup) {
            $.ajax({
                url: base_url + "/scheduleCenter/updateJobMessage.do",
                data: $('#jobMessageEdit form').serialize() + "&selfConfigs=" + $('#config textarea').val() +
                "&script=" + $('#script textarea').val() + "&resource=" + $('#resource textarea').val() +
                "&id=" + focusId,
                type: "post",
                success: function (data) {
                    if (data.success == true) {
                        leftClick();
                    } else {
                        alert(data.msg)
                    }
                }
            });
        } else {
            $.ajax({
                url: base_url + "/scheduleCenter/updateGroupMessage.do",
                data: $('#groupMessageEdit form').serialize() + "&configs=" + $('#config textarea').val() +
                "&resource=" + $('#resource textarea').val() + "&id=" + focusId,
                type: "post",
                success: function (data) {
                    if (data == true) {
                        leftClick();
                    }
                }
            });
        }


    });
    //组编辑
    $('#groupOperate [name="edit"]').on('click', function () {

        formDataLoad("groupMessageEdit form", focusItem);
        changeGroupStyle(0);
    });
    //删除
    $('[name="delete"]').on('click', function () {
        if (confirm("确认删除 :" + focusItem.name + "?")) {
            $.ajax({
                url: base_url + "/scheduleCenter/deleteJob.do",
                data: {
                    id: focusId,
                    isGroup: isGroup
                },
                type: "post",
                success: function (data) {
                    if (data == true) {
                        treeObj.removeNode(selected);
                        setDefaultSelectNode(focusItem.groupId);
                        leftClick();
                        alert("删除成功");
                    } else {
                        alert("删除失败");
                    }
                }
            });
        }
    });

    function changeGroupStyle(status) {
        var status1 = "none", status2 = "block", status3 = false;
        if (status != 0) {
            status1 = "block";
            status2 = "none";
            status3 = true;
        }
        $('#groupMessage').css("display", status1);
        $('#groupOperate').css("display", status1);
        $('#groupMessageEdit').css("display", status2);
        $('#editOperator').css("display", status2);
        var config = $("#config textarea");
        var resource = $("#resource textarea");
        config.attr("disabled", status3);
        resource.attr("disabled", status3);
        $("#resource").css("display", "block");
        $("#config").css("display", "block");
    }

    function initVal(configs, dom) {
        var val, userConfigs = "";
        //首先过滤内置配置信息 然后拼接用户配置信息
        for (var key in configs) {
            val = configs[key];
            if (key === "roll.back.times") {
                var backTimes = $("#" + dom + " [name='rollBackTimes']");
                if (dom == "jobMessage") {
                    backTimes.text(val);
                } else {
                    backTimes.val(val);
                }
            } else if (key === "roll.back.wait.time") {
                var waitTime = $("#" + dom + " [name='rollBackWaitTime']");
                if (dom == "jobMessage") {
                    waitTime.text(val);
                } else {
                    waitTime.val(val);
                }
            } else if (key === "run.priority.level") {
                var level = $("#" + dom + " [name='runPriorityLevel']");
                if (dom == "jobMessage") {
                    level.text(val == 1 ? "low" : val == 2 ? "medium" : "high");
                } else {
                    level.val(val);
                }
            } else if (key === "zeus.dependency.cycle" || key === "hera.dependency.cycle") {
                var cycle = $("#" + dom + " [name='heraDependencyCycle']");
                if (dom == "jobMessage") {
                    cycle.text(val);
                } else {
                    cycle.val(val);
                }
            } else {
                userConfigs = userConfigs + key + "=" + val + "\n";
            }
        }
        if (focusItem.cronExpression == null || focusItem.cronExpression == undefined || focusItem.cronExpression == "") {
            $('#jobMessageEdit [name="cronExpression"]').val("0 0 3 * * ?");
        }

        return userConfigs;
    }

    function leftClick() {
        selected = zTree.getSelectedNodes()[0];
        var id = selected.id;
        var dir = selected.directory;
        focusId = id;
        setCurrentId(focusId);
        //如果点击的是任务节点
        if (dir == null || dir == undefined) {
            isGroup = false;

            $.ajax({
                url: base_url + "/scheduleCenter/getJobMessage.do",
                type: "get",
                async: false,
                data: {
                    jobId: id
                },
                success: function (data) {
                    focusItem = data;
                    $("#script textarea").val(data.script);
                    var isShow = data.scheduleType === 0;
                    $('#dependencies').css("display", isShow ? "none" : "");
                    $('#heraDependencyCycle').css("display", isShow ? "none" : "");
                    $('#cronExpression').css("display", isShow ? "" : "none");
                    formDataLoad("jobMessage form", data);
                    $("#jobMessage [name='scheduleType']").text(isShow ? "定时调度" : "依赖调度");
                    $('#config textarea:first').val(initVal(data.configs, "jobMessage"));
                    $('#jobMessage [name="auto"]').removeClass("label-success").removeClass("label-default").addClass(data.auto === "开启" ? "label-success" : "label-default");


                }
            });
        } else { //如果点击的是组节点
            isGroup = true;

            $.ajax({
                url: base_url + "/scheduleCenter/getGroupMessage.do",
                type: "get",
                async: false,
                data: {
                    groupId: id
                },
                success: function (data) {
                    focusItem = data;
                    formDataLoad("groupMessage form", data);
                }

            });
            $('#config textarea:first').val(parseJson(focusItem.configs));

        }

        changeEditStyle(1);
        //组管理
        if (dir != undefined && dir != null) {
            //设置操作菜单
            $("#groupOperate").attr("style", "display:block");
            $("#jobOperate").attr("style", "display:none");
            var jobDisabled;
            //设置按钮不可用
            $("#groupOperate [name='addJob']").attr("disabled", jobDisabled = dir == 0);
            $("#groupOperate [name='addGroup']").attr("disabled", !jobDisabled);
            //设置任务相关信息不显示
            $("#script").css("display", "none");
            $("#jobMessage").css("display", "none");
            $("#groupMessage").css("display", "block");
        } else { //任务管理
            $("#groupOperate").css("display", "none");
            $("#groupMessage").css("display", "none");
            $("#jobOperate").css("display", "block");
            $("#script").css("display", "block");
        }
        $('#resource textarea:first').val(focusItem.resource);
        $("#config").css("display", "block");
        $("#resource").css("display", "block");
        $("#inheritConfig").css("display", "block");
    }

    function parseJson(obj) {
        var res = "";
        for (var x in obj) {
            res = res + x + "=" + obj[x] + "\n";
        }
        return res;
    }

    $("#manual").click(function () {
        triggerType = 1;
        setAction();
    });

    $("#manualRecovery").click(function () {
        triggerType = 2;
        setAction();
    });

    $("#myModal .add-btn").click(function () {
        $.ajax({
            url: base_url + "/scheduleCenter/manual.do",
            type: "get",
            async: false,
            data: {
                actionId: $("#selectJobVersion").val(),
                triggerType: triggerType,
                script: $('#script textarea').val()
            },
            success: function (data) {

            }
        });
        $('#myModal').modal('hide');
    });

    function setAction() {
        //获得版本
        jQuery.ajax({
            url: base_url + "/scheduleCenter/getJobVersion.do",
            type: "get",
            data: {
                jobId: focusId
            },
            success: function (data) {
                if (data.success == false) {
                    alert(data.message);
                    return;
                }
                var jobVersion = "";

                data.forEach(function (action, index) {
                    jobVersion += '<option value="' + action.id + '" >' + action.id + '</option>';
                });

                $('#selectJobVersion').empty();
                $('#selectJobVersion').append(jobVersion);
                $('#selectJobVersion').selectpicker('refresh');

                $('#myModal').modal('show');

            }
        });
    }

    function OnRightClick() {

    }


    var zNodes = getDataByPost(base_url + "/scheduleCenter/init.do");

    //修正zTree的图标，让文件节点显示文件夹图标
    function fixIcon() {
        $.fn.zTree.init($("#jobTree"), setting, zNodes);
        treeObj = $.fn.zTree.getZTreeObj("jobTree");
        //过滤出sou属性为true的节点（也可用你自己定义的其他字段来区分，这里通过sou保存的true或false来区分）
        var folderNode = treeObj.getNodesByFilter(function (node) {
            return node.isParent
        });
        for (var j = 0; j < folderNode.length; j++) {//遍历目录节点，设置isParent属性为true;
            folderNode[j].isParent = true;
        }
        treeObj.refresh();//调用api自带的refresh函数。
    }

    var zTree, rMenu;


    $(document).ready(function () {
        $.fn.zTree.init($("#jobTree"), setting, zNodes);
        zTree = $.fn.zTree.getZTreeObj("jobTree");
        rMenu = $("#rMenu");
        fixIcon();//调用修复图标的方法。方法如下：
        setDefaultSelectNode(localStorage.getItem("defaultId"))
    });


});


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
            success: function (data) {
                if (data.status != 'running') {
                    window.clearInterval(timerHandler);
                }
                $('#log_' + actionRow.id).val(data.log);
                actionRow.log=data.log;
                actionRow.status = data.status;
            }
        })
    }

    $('#jobLog').on('hide.bs.modal', function () {
        if (timerHandler != null) {
            window.clearInterval(timerHandler)
        }
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
            pageList: [10, 25, 40, 60],
            columns: [
                {
                    field: "id",
                    title: "id"
                }, {
                    field: "actionId",
                    title: "版本号"
                }, {
                    field: "jobId",
                    title: "jobId"
                }, {
                    field: "executeHost",
                    title: "执行机器ip"
                }, {
                    field: "status",
                    title: "执行状态"
                }, {
                    field: "operator",
                    title: "执行人"
                }, {
                    field: "startTime",
                    title: "开始时间",
                    width: "20%",
                    formatter: function (row) {
                        return getLocalTime(row);
                    }
                }, {
                    field: "endTime",
                    title: "结束时间",
                    width: "20%",
                    formatter: function (row) {
                        return getLocalTime(row);
                    }
                }, {
                    field: "triggerType",
                    title: "触发类型",
                    width: "20%",
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
                    field: "status",
                    title: "操作",
                    width: "20%",
                    formatter: function (index, row) {
                        var html = '<a href="javascript:cancelJob(\'' + row['id'] + '\')">取消任务</a>';
                        var html2 = '<a href="javascript:getLog(\'' + index + ')">查看日志</a>';
                        if (row['status'] == 'running') {
                            return html;
                        } else {
                            return html2;
                        }
                    }
                }
            ],
            detailView: true,
            detailFormatter: function (index, row) {
                var log = row["log"];
                var html = '<form role="form">' + '<div class="form-group">' + '<textarea class="form-control" rows="30" id="log_' + row.id + '">'
                    + log +
                    '</textarea>' + '<form role="form">' + '<div class="form-group">';
                return html;
            },
            onExpandRow: function (index, row) {
                actionRow = row;
                if (index != onExpand) {
                    table.bootstrapTable("collapseRow", onExpand);
                }
                onExpand = index;
                if (row.status == "running") {
                    timerHandler = window.setInterval(scheduleLog, 1000);
                }
            },
            onCollapseRow: function (index, row) {
                window.clearInterval(timerHandler)
            }
        });
    };
    return oTableInit;
};



function cancelJob(historyId) {
    var url = base_url + "/scheduleCenter/cancelJob.do";
    var parameter = {id: historyId};
    getDataByGet(url, parameter)

}