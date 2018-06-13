$(function () {
    var focusId = -1;
    var focusItem = null;
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

    //切换任务编辑状态
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
        $('#jobEditOper').css("display", val2);
    }

    //任务编辑
    $('#jobOperate [name="edit"]').on('click', function () {
        //回显
        formDataLoad("jobMsgEditForm", focusItem);
        initVal(focusItem.configs, "jobMsgEditForm");
        changeEditStyle(0);
    });
    //组编辑
    $('#jobEditOper [name="back"]').on('click', function () {
        changeEditStyle(1);
    });
    $('#jobEditOper [name="save"]').on('click', function () {
        $.ajax({
            url: "scheduleCenter/updateJobMessage.do",
            data: $('#jobMessageEdit form').serialize() + "&selfConfigs=" + $('#config textarea').val() +
                "&script=" + $('#script textarea').val() + "&rescources=" + $('#resource textarea').val(),
            type: "post",
            success: function (data) {

            }
        });

    });

    $('#groupOperate [name="edit"]').on('click', function () {
        console.log("focusid: " + focusId)
    });

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
        return userConfigs;
    }

    function leftClick() {
        var selected = zTree.getSelectedNodes()[0];
        var id = selected.id;
        var dir = selected.directory;
        focusId = id;
        var parameter = "jobId=" + id;
        //如果点击的是任务节点
        if (!selected.isParent) {
            $.ajax({
                url: "/scheduleCenter/getJobMessage.do",
                type: "get",
                async: false,
                data: parameter,
                success: function (data) {
                    focusItem = data;
                    $("#script textarea").val(data.script);
                    var isShow = data.scheduleType === "0";
                    $('#dependencies').css("display", isShow ? "none" : "");
                    $('#heraDependencyCycle').css("display", isShow ? "none" : "");
                    $('#cronExpression').css("display", isShow ? "" : "none");
                    formDataLoad("jobMessage", data);
                    $("#jobMessage [name='scheduleType']").text(isShow ? "定时调度" : "依赖调度");
                    $('#config textarea:first').val(initVal(data.configs, "jobMessage"));
                }
            });
            //获得版本
            jQuery.ajax({
                url: "/scheduleCenter/getJobVersion.do",
                type: "get",
                data: parameter,
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
                }
            });
        } else { //如果点击的是组节点
            $.ajax({
                url: "scheduleCenter/getGroupMessage.do",
                type: "get",
                async: false,
                data: {
                    groupId : id
                },
                success: function (data) {
                    formDataLoad("groupMessage", data);
                }

            })
        }
        changeEditStyle(1);
        //组管理
        if (dir != undefined && dir != null) {
            //设置操作菜单
            $("#groupOperate").attr("style", "display:block");
            $("#jobOperate").attr("style", "display:none");
            var jobDisabled;
            //设置按钮不可用
            $("#addJob").attr("disabled", jobDisabled = dir == 0);
            $("#addGroup").attr("disabled", !jobDisabled);
            //设置任务相关信息不显示
            $("#config").css("display", "none");
            $("#script").css("display", "none");
            $("#resource").css("display", "none");
            $("#inheritConfig").css("display", "none");
            $("#jobMessage").css("display", "none");
            $("#groupMessage").css("display", "block");
        } else { //任务管理
            $("#groupOperate").css("display", "none");
            $("#groupMessage").css("display", "none");

            $("#jobOperate").css("display", "block");
            $("#config").css("display", "block");
            $("#script").css("display", "block");
            $("#resource").css("display", "block");
            $("#inheritConfig").css("display", "block");
        }
    }


    $("#manual").click(function () {
        $('#myModal').modal('show');
    });


    $(".add-btn").click(function () {
        var actionId = $("#selectJobVersion").val();
        var parameter = "actionId=" + actionId;
        $.ajax({
            url: "/scheduleCenter/manual.do",
            type: "get",
            async: false,
            data: parameter,
            success: function (data) {
            }
        });
    });

    function OnRightClick() {

    }

    var zNodes = getDataStore("/scheduleCenter/init.do");

    function getDataStore(url) {
        var dataStore;
        $.ajax({
            type: "post",
            url: url,
            async: false,
            success: function (data) {
                dataStore = data;
            }
        });
        return dataStore;
    }

    //修正zTree的图标，让文件节点显示文件夹图标
    function fixIcon() {
        $.fn.zTree.init($("#jobTree"), setting, zNodes);
        var treeObj = $.fn.zTree.getZTreeObj("jobTree");
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

    });


});