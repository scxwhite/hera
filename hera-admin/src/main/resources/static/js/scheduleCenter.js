var nodes, edges, g, headNode, currIndex = 0, len, inner, initialScale = 0.75, zoom, nodeIndex = {}, graphType;

layui.use(['table'], function () {
    var table = layui.table;
    $('#scheduleCenter').addClass('active');
    var focusItem = null;
    var isGroup;
    var treeObj, allTreeObj;
    var dependTreeObj;
    var selected;
    var triggerType;
    var groupTaskTable, groupTaskType, focusId = -1;
    var codeMirror, inheritConfigCM, selfConfigCM;
    var editor = $('#editor');
    var setting = {
        view: {
            fontCss: getFontCss
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
            onClick: leftClick
        }
    };

    function refreshCm() {
        selfConfigCM.refresh();
        codeMirror.refresh();
        inheritConfigCM.refresh();
    }

    /**
     * 把当前选中的节点存入localStorage
     * 页面刷新后，会根据"defaultId"设置当前选中的节点
     * 避免页面刷新丢失
     * @param id    节点ID
     */
    function setCurrentId(id) {
        if ($('#jobTree').css('display') === 'block') {
            localStorage.setItem("defaultId", id);
        } else {
            localStorage.setItem("allDefaultId", id);

        }
    }

    /**
     * 设置当前默认选中的节点
     * @param id    节点ID
     */
    function setDefaultSelectNode(id) {
        if (id !== undefined && id !== null) {
            if (id.indexOf('group') !== -1) {
                var node = treeObj.getNodeByParam("parent", id);
                expandParent(node, treeObj);
                treeObj.selectNode(node);
            } else {
                var node = treeObj.getNodeByParam("id", id);
                expandParent(node, treeObj);
                treeObj.selectNode(node);
            }
            leftClick();


        }
    }

    /**
     * 切换任务编辑状态
     * @param status
     */
    function changeEditStyle(status) {
        //默认 展示状态
        var val1 = "block", val2 = "none";
        //编辑状态
        if (status == 0) {
            val1 = "none";
            val2 = "block";
        }
        codeMirror.setOption("readOnly", status != 0);
        selfConfigCM.setOption("readOnly", status != 0);
        $('#jobMessage').css("display", val1);
        $('#jobMessageEdit').css("display", val2);
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

    $('#jobOperate [name="addAdmin"]').on('click', function () {
        addAdmin();
    });


    $('#groupOperate [name="addAdmin"]').on('click', function () {
        addAdmin();
    });

    function addAdmin() {
        $.ajax({
            url: base_url + "/scheduleCenter/getJobOperator",
            type: "get",
            data: {
                jobId: focusId,
                type: isGroup
            },
            success: function (data) {
                if (data.success) {
                    var html = '';
                    data.data['allUser'].forEach(function (val) {
                        html = html + '<option value= "' + val.name + '">' + val.name + '</option>';
                    });
                    $('#userList').empty();
                    $('#userList').append(html);
                    var admins = new Array();
                    data.data['admin'].forEach(function (val) {
                        admins.push(val.uid);

                    });
                    $('#userList').selectpicker('val', admins);

                    $('#userList').selectpicker('refresh');
                    $('#addAdminModal').modal('show');
                } else {
                    alert(data.message);
                }
            }
        });
    }

    $('#addAdminModal [name="submit"]').on('click', function () {
        var uids = $('#userList').val();
        $.ajax({
            url: base_url + "/scheduleCenter/updatePermission",
            type: "post",
            data: {
                uIdS: JSON.stringify(uids),
                id: focusId,
                type: isGroup
            },
            success: function (data) {
                alert(data.message);
                window.setTimeout(leftClick, 100);
            }
        })
    });


    $('#jobOperate [name="jobDag"]').on('click', function () {
        $('#jobDagModal').modal('show');
        $('#item').val($('#jobMessage [name="id"]').val());
        keypath(0);
    });

    $("#groupOperate [name='addGroup']").on('click', function () {
        $('#addGroupModal [name="groupName"]').val("");
        $('#addGroupModal [name="groupType"]').val("0");
        $('#addGroupModal').modal('show');

    });

    $('#addGroupModal [name="addBtn"]').on('click', function () {
        $.ajax({
            url: base_url + "/scheduleCenter/addGroup.do",
            type: "post",
            data: {
                name: $('#addGroupModal [name="groupName"]').val(),
                directory: $('#addGroupModal [name="groupType"]').val(),
                parentId: focusId
            },
            success: function (data) {
                $('#addGroupModal').modal('hide');
                if (data.success == true) {
                    localStorage.setItem("defaultId", data.msg);
                    location.reload(false);
                } else {
                    alert(data.msg);
                }
            }
        })
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
            success: function (res) {
                layer.msg(res);
            },
            error: function (err) {
                layer.msg(err);
            }
        })
    });
    /**
     * 任务开启关闭按钮
     */
    $('#jobOperate [name="switch"]').on('click', function () {
        //回显
        $.ajax({
            url: base_url + "/scheduleCenter/updateSwitch",
            data: {
                id: focusId
            },
            type: "post",
            success: function (data) {
                if (data.success === false) {
                    layer.msg(data.msg);
                } else {
                    layer.msg(data.msg);
                    leftClick();
                }
            }
        })
    });

    /**
     * 任务开启关闭按钮
     */
    $('#jobOperate [name="monitor"]').on('click', function () {
        if (focusItem.focus) {
            $.ajax({
                url: base_url + "/scheduleCenter/delMonitor",
                data: {
                    id: focusId
                },
                type: "post",
                success: function (data) {
                    leftClick();
                    if (data.success == false) {
                        alert(data.msg);
                    }
                }
            })
        } else {
            $.ajax({
                url: base_url + "/scheduleCenter/addMonitor",
                data: {
                    id: focusId
                },
                type: "post",
                success: function (data) {
                    leftClick();
                    if (data.success == false) {
                        alert(data.msg);

                    }
                }
            })
        }

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
                parentId: focusId
            },
            success: function (data) {
                if (data.success == true) {
                    localStorage.setItem("defaultId", data.msg)
                    location.reload(false);
                } else {
                    alert(data.msg);
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
            var setting = {
                view: {
                    fontCss: getFontCss
                },
                check: {
                    enable: true
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
                    onClick: zTreeOnClick
                }
            };
            var dependNodes = getDataByPost(base_url + "/scheduleCenter/init.do");
            $.fn.zTree.init($("#dependTree"), setting, dependNodes.myJob);

            // var testNode ={"allJob": [
            //         {
            //             "id": "1218",
            //             "parent": "group_3848",
            //             "name": "0-1. tmp_tab(1218)",
            //             "directory": null,
            //             "isParent": false
            //         },
            //         {
            //             "id": "1219",
            //             "parent": "group_3848",
            //             "name": "0-2. 到店(1219)",
            //             "directory": null,
            //             "isParent": false
            //         }]}
            // $.fn.zTree.init($("#dependTree"), setting, testNode.allJob);


            dependTreeObj = $.fn.zTree.getZTreeObj("dependTree");

            $("#dependJob").bind('click', function () {
                $("#selectDepend").modal('show');
            });
            $("#chooseDepend").bind('click', function () {
                var nodes = dependTreeObj.getCheckedNodes(true);
                var ids = new Array();
                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i]['isParent'] == false) {
                        ids.push(nodes[i]['id']);
                    }
                }
                $("#dependJob").val(ids.join(","));
                $("#selectDepend").modal('hide');

            });
            setJobMessageEdit(false);
        }
    });

    $('#keyWords').on('keydown', function (e) {
        if (e.keyCode == '13') {
            searchNodeLazy($.trim($(this).val()), treeObj, "keyWords", false);
        }
    });

    $('#dependKeyWords').on('keydown', function () {
        if (e.keyCode == '13') {
            searchNodeLazy( $.trim($(this).val()), dependTreeObj, "dependKeyWords", false);
        }

    });
    var timeoutId;

    function searchNodeLazy(key, tree, keyId, first) {
        if (tree === $.fn.zTree.getZTreeObj("jobTree") || tree === $.fn.zTree.getZTreeObj('allTree')) {
            $('#searchInfo').show();
            $('#searchInfo').text('查找中，请稍候...');
        } else {
            $('#deSearchInfo').show();
            $('#deSearchInfo').text('查找中，请稍候...');
        }
        if (key == null || key == "" || key == undefined) {
            tree.getNodesByFilter(function (node) {
                node.highlight = false;
                node.checked = false;
                tree.showNode(node);
            });
            if (tree === $.fn.zTree.getZTreeObj("jobTree") || tree === $.fn.zTree.getZTreeObj('allTree')) {
                setDefaultSelectNode(localStorage.getItem("defaultId"));
                $('#searchInfo').hide();
            } else {
                $('#deSearchInfo').hide();
            }
            tree.refresh();
        }
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = setTimeout(function () {
            search(key); //lazy load ztreeFilter function
            $('#' + keyId).focus();
        }, 50);

        function search(key) {
            var keys, length;
            if (key !== null && key !== "" && key !== undefined) {
                keys = key.split(" ");
                length = keys.length;
                var nodeShow = tree.getNodesByFilter(filterNodes);
                if (nodeShow && nodeShow.length > 0) {
                    nodeShow.forEach(function (node) {
                        expandParent(node, tree);
                    });
                    if (tree === $.fn.zTree.getZTreeObj("jobTree") || tree === $.fn.zTree.getZTreeObj('allTree')) {
                        $('#searchInfo').hide();
                    } else {
                        $('#deSearchInfo').hide();
                    }
                    tree.refresh();
                } else {
                    if (tree === $.fn.zTree.getZTreeObj("jobTree") || tree === $.fn.zTree.getZTreeObj('allTree')) {
                        $('#searchInfo').text('未找到该节点');
                    } else {
                        $('#deSearchInfo').text('未找到该节点');
                    }
                }
            }

            function filterNodes(node) {
                for (var i = 0; i < length; i++) {
                    if (node.name) {
                        //id搜索
                        if (!isNaN(keys[i])) {
                            var start = node.name.lastIndexOf('(');
                            var end = node.name.lastIndexOf(')');
                            var id = node.name.substring(++start, end);
                            if (id === keys[i]) {
                                if (tree === $.fn.zTree.getZTreeObj("jobTree") || tree === $.fn.zTree.getZTreeObj('allTree')) {
                                    tree.checkNode(node, true, true, false);
                                    node.highlight = true;
                                    tree.showNode(node);
                                    if (i === 0) {
                                        tree.selectNode(node);
                                        $('.curSelectedNode').trigger('click');
                                    }
                                } else {
                                    if (!node.isParent) {
                                        node.highlight = true;
                                        tree.showNode(node);
                                        if (node.name === '0-1. tmp_tab(1218)' && node.checked === false) {
                                            console.log(node)
                                            console.log('start node unchecked');
                                        }
                                        if (first) tree.checkNode(node, true, true, false);
                                    }
                                }
                                return true;
                            }
                        } else {//name搜索
                            var end = node.name.lastIndexOf('(');
                            var name = node.name.substring(0, end);
                            if (name.indexOf(keys[i]) !== -1) {
                                if (tree === $.fn.zTree.getZTreeObj("jobTree") || tree === $.fn.zTree.getZTreeObj("allTree")) {
                                    node.highlight = true;
                                    tree.showNode(node);
                                    tree.checkNode(node, true, true, false);
                                    if (i === 0) {
                                        tree.selectNode(node);
                                        $('.curSelectedNode').trigger('click');
                                    }
                                } else {
                                    if (!node.isParent) {
                                        node.highlight = true;
                                        tree.showNode(node);
                                        if (first) tree.checkNode(node, true, true, false);
                                    }
                                }
                                return true;
                            }
                        }
                    }
                }
                if (node.checked && !node.isParent) {
                    tree.checkNode(node, false, true, false);
                }
                if (!first) {
                    tree.hideNode(node);
                }
                node.highlight = false;
                return false;
            }
        }
    }


    function expandParent(node, obj) {
        if (node) {
            var path = node.getPath();
            if (path && path.length > 0) {
                for (var i = 0; i < path.length - 1; i++) {
                    obj.showNode(path[i]);
                    obj.expandNode(path[i], true);
                }
            }
        }
    }

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
        leftClick();
    });
    /**
     * 上传文件
     */
    $('#editOperator [name="upload"]').on('click', function () {
        uploadFile();
    });
    /**
     * 保存按钮
     */
    $('#editOperator [name="save"]').on('click', function () {
        if (!isGroup) {
            $.ajax({
                url: base_url + "/scheduleCenter/updateJobMessage.do",
                data: $('#jobMessageEdit form').serialize() + "&selfConfigs=" + encodeURIComponent(selfConfigCM.getValue()) +
                "&script=" + encodeURIComponent(codeMirror.getValue()) +
                "&id=" + focusId,
                type: "post",
                success: function (data) {
                    if (data.success == false) {
                        layer.msg(data.msg)
                    } else {
                        leftClick();
                    }
                }
            });
        } else {
            $.ajax({
                url: base_url + "/scheduleCenter/updateGroupMessage.do",
                data: $('#groupMessageEdit form').serialize() + "&selfConfigs=" + encodeURIComponent(selfConfigCM.getValue()) +
                "&resource=" + "&groupId=" + focusId,
                type: "post",
                success: function (data) {
                    if (data.success == false) {
                        layer.msg(data.msg);
                    } else {
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

        layer.confirm("确认删除 :" + focusItem.name + "?", {
            icon: 0,
            skin: 'msg-class',
            btn: ['确定', '取消'],
            anim: 0
        }, function (index, layero) {
            $.ajax({
                url: base_url + "/scheduleCenter/deleteJob.do",
                data: {
                    id: focusId,
                    isGroup: isGroup
                },
                type: "post",
                success: function (data) {
                    layer.msg(data.msg);
                    if (data.success == true) {
                        var parent = selected.getParentNode();
                        treeObj.removeNode(selected);
                        expandParent(parent, treeObj);
                        treeObj.selectNode(parent);
                        leftClick();
                    }
                }
            });
            layer.close(index)
        }, function (index) {
            layer.close(index)
        });


    });

    function changeGroupStyle(status) {
        var status1 = "none", status2 = "block";
        if (status != 0) {
            status1 = "block";
            status2 = "none";
        }
        selfConfigCM.setOption("readOnly", status != 0);
        $('#groupMessage').css("display", status1);
        $('#groupOperate').css("display", status1);
        $('#groupMessageEdit').css("display", status2);
        $('#editOperator').css("display", status2);
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
                    backTimes.val(val);
                } else {
                    backTimes.val(val);
                }
            } else if (key === "roll.back.wait.time") {
                var waitTime = $("#" + dom + " [name='rollBackWaitTime']");
                if (dom == "jobMessage") {
                    waitTime.val(val);
                } else {
                    waitTime.val(val);
                }
            } else if (key === "run.priority.level") {
                var level = $("#" + dom + " [name='runPriorityLevel']");
                if (dom == "jobMessage") {
                    level.val(val == 1 ? "low" : val == 2 ? "medium" : "high");
                } else {
                    level.val(val);
                }
            } else if (key === "zeus.dependency.cycle" || key === "hera.dependency.cycle") {
                var cycle = $("#" + dom + " [name='heraDependencyCycle']");
                if (dom == "jobMessage") {
                    cycle.val(val);
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

    //搜索结果节点颜色改变
    function getFontCss(treeId, treeNode) {
        return (!!treeNode.highlight) ? {color: "#A60000", "font-weight": "bold"} : {
            color: "rgba(0, 0, 0, 0.65)",
            "font-weight": "normal"
        };
    }

    function leftClick() {
        if ($('#jobTree').css('display') === 'block') {
            selected = zTree.getSelectedNodes()[0];
        } else {
            selected = zAllTree.getSelectedNodes()[0];
        }
        changeOverview(true);
        if (selected) {
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
                        if (data.runType == "Shell") {
                            codeMirror.setOption("mode", "text/x-sh");
                        } else {
                            codeMirror.setOption("mode", "text/x-hive");
                        }
                        if (data.script != null) {
                            codeMirror.setValue(data.script);
                        } else {
                            codeMirror.setValue('');
                        }
                        var isShow = data.scheduleType === 0;
                        $('#dependencies').css("display", isShow ? "none" : "");
                        $('#heraDependencyCycle').css("display", isShow ? "none" : "");
                        $('#cronExpression').css("display", isShow ? "" : "none");
                        formDataLoad("jobMessage form", data);
                        $("#jobMessage [name='scheduleType']").val(isShow ? "定时调度" : "依赖调度");
                        selfConfigCM.setValue(initVal(data.configs, "jobMessage"));
                        $('#jobMessage [name="auto"]').removeClass("label-primary").removeClass("label-default").addClass(data.auto === "开启" ? "label-primary" : "label-default");
                        $('#jobOperate [name="monitor"]').text(data.focus ? "取消关注" : "关注该任务");
                        inheritConfigCM.setValue(parseJson(data.inheritConfig));


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
                        inheritConfigCM.setValue(parseJson(data.inheritConfig));

                        selfConfigCM.setValue(parseJson(data.configs));
                    }

                });


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
            $("#config").css("display", "block");
            $("#inheritConfig").css("display", "block");
            refreshCm();
        }

        // $.each($("textarea"), function (i, n) {
        //     $(n).css("height", n.scrollHeight + "px");
        // })
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
            data: {
                actionId: $("#selectJobVersion").val(),
                triggerType: triggerType
            },
            success: function (res) {
                if (res.success === true) {
                    layer.msg('执行成功');
                } else {
                    layer.msg(res.msg)
                }
            },
            error: function (err) {
                layer.msg(err);
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


    var zNodes = getDataByPost(base_url + "/scheduleCenter/init.do");

    //修正zTree的图标，让文件节点显示文件夹图标
    function fixIcon() {
        $.fn.zTree.init($("#jobTree"), setting, zNodes.myJob);
        treeObj = $.fn.zTree.getZTreeObj("jobTree");
        treeObj.refresh();//调用api自带的refresh函数。
    }

    var zTree, rMenu, zAllTree;


    $(document).ready(function () {
        $('#allScheBtn').click(function (e) {
            e.stopPropagation();
            $('#jobTree').hide();
            $('#allTree').show();
            $(this).parent().addClass('active');
            $('#myScheBtn').parent().removeClass('active');
            $.fn.zTree.init($("#allTree"), setting, zNodes.allJob);
            zAllTree = $.fn.zTree.getZTreeObj("allTree");
            treeObj = $.fn.zTree.getZTreeObj("allTree");
            setDefaultSelectNode(localStorage.getItem("allDefaultId"));
            var key = $('#keyWords').val();
            searchNodeLazy(key, treeObj, "keyWords", false);
        });
        $.fn.zTree.init($("#jobTree"), setting, zNodes.myJob);
        zTree = $.fn.zTree.getZTreeObj("jobTree");
        treeObj = $.fn.zTree.getZTreeObj("jobTree");

        $('#myScheBtn').click(function (e) {
            e.stopPropagation();
            $('#jobTree').show();
            $('#allTree').hide();
            $(this).parent().addClass('active');
            $('#allScheBtn').parent().removeClass('active');
            treeObj = $.fn.zTree.getZTreeObj("jobTree");
            var key = $('#keyWords').val();
            searchNodeLazy(key, treeObj, "keyWords", false);
        });
        rMenu = $("#rMenu");
        $.each($(".content .row .height-self"), function (i, n) {
            $(n).css("height", (screenHeight - 50) + "px");
        });
        // fixIcon();//调用修复图标的方法。方法如下：
        codeMirror = CodeMirror.fromTextArea(editor[0], {
            mode: "text/x-sh",
            lineNumbers: true,
            theme: "lucario",
            readOnly: true,
            matchBrackets: true,
            smartIndent: true,
            styleActiveLine: true,
            styleSelectedText: true,
            nonEmpty: true
        });

        codeMirror.on('keypress', function () {
            if (!codeMirror.getOption('readOnly')) {
                codeMirror.showHint({
                    completeSingle: false
                });
            }
        });

        selfConfigCM = CodeMirror.fromTextArea($('#config textarea')[0], {
            mode: "text/x-sh",
            theme: "base16-light",
            readOnly: true,
            matchBrackets: true,
            smartIndent: true,
            nonEmpty: true
        });
        inheritConfigCM = CodeMirror.fromTextArea($('#inheritConfig textarea')[0], {
            mode: "text/x-sh",
            theme: "base16-light",
            readOnly: true,
            matchBrackets: true,
            smartIndent: true,
            nonEmpty: true
        });

        codeMirror.setSize('auto', 'auto');
        inheritConfigCM.setSize('auto', 'auto');
        selfConfigCM.setSize('auto', 'auto');
        setDefaultSelectNode(localStorage.getItem("defaultId"));
        $.ajax({
            url: base_url + "/scheduleCenter/getHostGroupIds",
            type: "get",
            success: function (data) {
                var hostGroup = $('#jobMessageEdit [name="hostGroupId"]');
                var option = '';
                data.forEach(function (val) {
                    option = option + '"<option value="' + val.id + '">' + val.name + '</option>';
                });
                hostGroup.empty();
                hostGroup.append(option);
            }

        })
        $('#timeChange').focus(function (e) {
            e.stopPropagation();
            $('#timeModal').modal('toggle');
            var para = $.trim($('#timeChange').val());
            var arr = para.split(' ');
            var min = arr[1];
            var hour = arr[2];
            var day = arr[3];
            var month = arr[4];
            var week = arr[5];
            $('#inputMin').val(min);
            $('#inputHour').val(hour);
            $('#inputDay').val(day);
            $('#inputMonth').val(month);
            $('#inputWeek').val(week);
        });
        $('#saveTimeBtn').click(function (e) {
            e.stopPropagation();
            var min = $.trim($('#inputMin').val());
            var hour = $.trim($('#inputHour').val());
            var day = $.trim($('#inputDay').val());
            var month = $.trim($('#inputMonth').val());
            var week = $.trim($('#inputWeek').val());
            var para = '0 ' + min + ' ' + hour + ' ' + day + ' ' + month + ' ' + week;
            $('#timeChange').val(para);
            $('#timeModal').modal('toggle');
        });

        //隐藏
        $('.hideBtn').click(function (e) {
            e.stopPropagation();
            $(this).parent().hide();
        })
        //隐藏树
        $('#hideTreeBtn').click(function (e) {
            e.stopPropagation();
            if ($(this).children().hasClass('fa-minus')) {
                $('#treeCon').removeClass('col-md-3 col-sm-3 col-lg-3').addClass('col-md-1 col-sm-1 col-lg-1');
                $(this).children().removeClass('fa-minus').addClass('fa-plus');
                $('#infoCon').removeClass('col-md-8 col-sm-8 col-lg-8').addClass('col-md-10 col-sm-10 col-lg-10');
                $('#showAllModal').removeClass('col-md-8 col-sm-8 col-lg-8').addClass('col-md-10 col-sm-10 col-lg-10');
            } else {
                $('#treeCon').removeClass('col-md-1 col-sm-1 col-lg-1').addClass('col-md-3 col-sm-3 col-lg-3');
                $(this).children().removeClass('fa-plus').addClass('fa-minus');
                $('#infoCon').removeClass('col-md-10 col-sm-10 col-lg-10').addClass('col-md-8 col-sm-8 col-lg-8');
                $('#showAllModal').removeClass('col-md-10 col-sm-10 col-lg-10').addClass('col-md-8 col-sm-8 col-lg-8');

            }
        })
        // keypath();

        $('#nextNode').on("click", function () {
            var expand = $('#expand').val();
            if (expand == null || expand == undefined || expand == "") {
                expand = 0;
            }
            expandNextNode(expand);

        });
        $('#expandAll').on("click", function () {
            expandNextNode(len);
        });

    });
    $('#biggerBtn').click(function (e) {
        e.stopPropagation();
        if ($(this).children().hasClass('fa-plus')) {
            $('#jobDagModalCon').addClass('bigger');
            $(this).children().removeClass('fa-plus').addClass('fa-minus');
        } else {
            $('#jobDagModalCon').removeClass('bigger');
            $(this).children().removeClass('fa-minus').addClass('fa-plus');
        }
    })
    $("#dependJob").bind('click', function () {
        $("#selectDepend").modal('show');
        $('#dependKeyWords').val($(this).val().split(',').join(' '));
        //定时调度
        var setting = {
            view: {
                fontCss: getFontCss
            },
            check: {
                enable: true
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
                onClick: zTreeOnClick
            }
        };

        var dependNodes = getDataByPost(base_url + "/scheduleCenter/init.do");
        $.fn.zTree.init($("#dependTree"), setting, dependNodes.allJob);
        dependTreeObj = $.fn.zTree.getZTreeObj("dependTree");
        searchNodeLazy($(this).val().split(',').join(' '), dependTreeObj, "dependKeyWords", true);
        $("#chooseDepend").bind('click', function () {
            var nodes = dependTreeObj.getCheckedNodes(true);
            var ids = new Array();
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i]['isParent'] == false) {
                    ids.push(nodes[i]['id']);
                }
            }
            $("#dependJob").val(ids.join(","));
            $("#selectDepend").modal('hide');

        });
        setJobMessageEdit(false);
    });
    $('#showAllModal').modal('hide');


    function changeOverview(type) {
        var overview = "", notShow = "none";
        if (type) {
            overview = "none";
            notShow = "";
        }
        $('#showAllModal').css("display", overview);
        $('#infoCon').css("display", notShow);
        $('#overviewOperator').css("display", overview);
        $('#groupOperate').css("display", notShow);
    }

    $('#overviewOperator [name="back"]').click(function () {
        changeOverview(true);

    });
    $('#showAllBtn').click(function () {
        // 表格渲染
        groupTaskType = 0;
        reloadGroupTaskTable();

    });


    $('#groupOperate [name="showRunning"]').on('click',function () {
        groupTaskType = 1;
        reloadGroupTaskTable();
    });

    $('#overviewOperator [name="showRunning"]').on('click',function () {
        groupTaskType = 1;
        reloadGroupTaskTable();
    });

    $('#overviewOperator [name="showFaild"]').on('click',function () {
        groupTaskType = 2;
        reloadGroupTaskTable();
    });

    $('#groupOperate [name="showFaild"]').on('click',function () {
        groupTaskType = 2;
        reloadGroupTaskTable();
    });
    $('#closeAll').click(function (e) {
        $("#showAllModal").modal('hide');
    });

    function reloadGroupTaskTable() {
        changeOverview(false);
        if (!groupTaskTable) {
            groupTaskTable = table.render({
                elem: '#allTable'                  //指定原始表格元素选择器（推荐id选择器）
                , height: "full-100"
                , cols: [[                  //标题栏
                    {field: 'actionId', title: 'ActionId', width: 151}
                    , {field: 'jobId', title: 'JobId', width: 70}
                    , {field: 'name', title: '任务名称', width: 125}
                    , {field: 'status', title: '执行状态', width: 105}
                    , {field: 'readyStatus', title: '依赖状态', width: 340}
                    , {field: 'lastResult', title: '上次执行结果', width: 125}
                ]]
                , id: 'dataCheck'
                , url: base_url + '/scheduleCenter/getGroupTask'
                , where: {
                    groupId: focusId,
                    type: groupTaskType
                }
                , method: 'get'
                , page: true
                , limits: [10, 30, 50]
            });
        } else {
            groupTaskTable.reload({
                where: {
                    groupId: focusId,
                    type: groupTaskType
                }
            });
        }

    }

});

function keypath(type) {
    $('#expandAll').removeClass('active').addClass('disabled');
    graphType = type;
    var node = $("#item")[0].value;
    if (node == "")
        return;
    var url = base_url + "/scheduleCenter/getJobImpactOrProgress";
    var data = {jobId: node, type: type};

    var success = function (data) {
        // Create a new directed com.dfire.graph
        if (data.success == false) {
            alert("不存在该任务节点");
            return;
        }
        initDate(data);

        // Set up the edges
        svg = d3.select("svg");
        inner = svg.select("g");

        // Set up zoom support
        zoom = d3.behavior.zoom().on("zoom", function () {
            inner.attr("transform", "translate(" + d3.event.translate + ")" +
                "scale(" + d3.event.scale + ")");
        });
        svg.call(zoom);

        redraw();
        // expandNextNode(1);
        zoom
            .translate([($('svg').width() - g.graph().width * initialScale) / 2, 20])
            .scale(initialScale)
            .event(svg);
        //svg.attr('height', g.com.dfire.graph().height * initialScale + 40);

        $('#expandAll').removeClass('disabled').addClass('active');
    }
    jQuery.ajax({
        type: 'POST',
        url: url,
        data: data,
        success: success
        //dataType: 'json'
    });
}

function initDate(data) {
    edges = data.data.edges;
    headNode = data.data.headNode;
    len = edges.length;
    currIndex = 0;
    g = new dagreD3.graphlib.Graph().setGraph({});
    g.setNode(headNode.nodeName, {label: headNode.nodeName, style: "fill: #bd16ff" + ";" + headNode.remark})
    var nodeName;
    for (var i = 0; i < len; i++) {
        nodeName = edges[i].nodeA.nodeName;
        if (nodeIndex[nodeName] == null || nodeIndex[nodeName] == undefined || nodeIndex[nodeName] == 0) {
            nodeIndex[nodeName] = i + 1;
        }
    }
}


function expandNextNode(nodeNum) {
    while (nodeNum > 0) {
        if (currIndex < len) {
            var edge = edges[currIndex];
            if (addEdgeToGraph(edge)) {
                nodeNum--;
            }
            currIndex++;
        } else {
            layer.msg("已经全部展示完毕！");
            break;
        }
    }
    redraw();
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
            success: function (data) {
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
            showPaginationSwitch: true,  //是否显示选择分页数按钮
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
            pageList: [10, 25, 40, 60],
            columns: [
                {
                    field: "id",
                    title: "id",
                    width: "5%",
                }, {
                    field: "actionId",
                    title: "版本号",
                    width: "15%",
                }, {
                    field: "jobId",
                    title: "任务ID",
                    width: "5%",
                }, {
                    field: "executeHost",
                    title: "执行机器ip",
                    width: "8%",

                }, {
                    field: "status",
                    title: "执行状态",
                    width: "8%",
                }, {
                    field: "operator",
                    title: "执行人",
                    width: "8%",

                }, {
                    field: "startTime",
                    title: "开始时间",
                    width: "12%",
                    formatter: function (row) {
                        return getLocalTime(row);
                    }
                }, {
                    field: "endTime",
                    title: "结束时间",
                    width: "12%",
                    formatter: function (row) {
                        return getLocalTime(row);
                    }
                }, {
                    field: "illustrate",
                    title: "说明",
                    width: "8%",
                    formatter: function (val) {
                        if (val == null) {
                            return val;
                        }
                        return "<span class='label label-info' data-toggle='tooltip' title='" + val + "' >" + val.slice(0, 6) + "</span>";
                    }
                },
                {
                    field: "triggerType",
                    title: "触发类型",
                    width: "8%",
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
                    width: "10%",
                    formatter: function (index, row) {
                        var html = '<a href="javascript:cancelJob(\'' + row['id'] + '\',\'' + row['jobId'] + '\')">取消任务</a>';
                        if (row['status'] == 'running') {
                            return html;
                        }
                    }
                }
            ],
            detailView: true,
            detailFormatter: function (index, row) {
                var html = '<form role="form">' + '<div class="form-group" style="background: #2c4762;min-height:600px; overflow:scroll;  ">' + '<div class="form-control"  style="border:none; height:600px; word-break: break-all; word-wrap:break-word; white-space:pre-line;font-family:Microsoft YaHei" id="log_' + row.id + '">'
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

function cancelJob(historyId, jobId) {
    var url = base_url + "/scheduleCenter/cancelJob.do";
    var parameter = {historyId: historyId, jobId: jobId};
    $.get(url, parameter, function (data) {
        layer.msg(data);
        $('#jobLog [name="refreshLog"]').trigger('click');
    });
}


function zTreeOnClick() {

}