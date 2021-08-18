let nodes, edges, g, headNode, currIndex = 0, len, inner, initialScale = 1.25, zoom, nodeIndex = {}, graphType,
    codeMirror, themeSelect = $('#themeSelect');

layui.use(['table'], function () {
    let table = layui.table;
    $('#scheduleManage').addClass('active');
    let focusItem = null;
    let isGroup;
    let focusTree;
    let dependTreeObj;
    let selected;
    let allArea = [];
    let groupTaskTable, groupTaskType, jobDt = '', focusId = -1;
    let inheritConfigCM, selfConfigCM;
    let editor = $('#editor');
    let setting = {
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
        edit: {
            drag: {
                isCopy: false,
                isMove: true,
                prev: true,
                next: true
            },
            enable: true

        },
        callback: {
            beforeDrag: beforeDrag,
            onClick: leftClick,
            beforeDrop: beforeDrop
        }
    };


    function refreshCm() {
        selfConfigCM.refresh();
        codeMirror.refresh();
        inheritConfigCM.refresh();
    }


    /**
     * 设置当前默认选中的节点
     * @param id    节点ID
     */
    function setDefaultSelectNode(id) {

        if (localStorage.getItem("taskGroup") === 'all') {
            allJobTree();
        } else {
            myJobTree();
        }
        if (id === null || id === undefined) {
            id = localStorage.getItem("defaultId");
        }
        if (id !== undefined && id !== null) {
            if (id.indexOf('group') !== -1) {
                let node = focusTree.getNodeByParam("parent", id);
                expandParent(node, focusTree);
                focusTree.selectNode(node);
            } else {
                let node = focusTree.getNodeByParam("id", id);
                expandParent(node, focusTree);
                focusTree.selectNode(node);
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
        let val1 = "block", val2 = "none";
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
        let areaId = $('#jobMessageEdit [name="areaId"]');

        let areas = new Array();
        focusItem.areaId.split(",").forEach(function (val) {
            areas.push(val);
        });
        areaId.selectpicker('val', areas);
        areaId.selectpicker('refresh');
    });

    /**
     * 查看任务日志
     */
    $('#jobOperate [name="runningLog"]').on('click', function () {

        $('#runningLogDetailTable').bootstrapTable("destroy");
        let tableObject = new JobLogTable(focusId);
        tableObject.init();

        $('#jobLog').modal('show');

    });

    /**
     * 查看操作记录
     */
    $('#jobOperate [name="record"]').on('click', function () {
        $('#recordTable').bootstrapTable("destroy");
        new recordTable(focusId).init();
        $('#recordModal').modal('show');

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
                type: isGroup ? "GROUP" : "JOB"
            },
            success: function (data) {
                if (data.success) {
                    let html = '';
                    data.data['allUser'].forEach(function (val) {
                        html = html + '<option value= "' + val.name + '">' + val.name + '</option>';
                    });
                    $('#userList').empty();
                    $('#userList').append(html);
                    let admins = new Array();
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
        let uids = $('#userList').val();
        $.ajax({
            url: base_url + "/scheduleCenter/updatePermission",
            type: "post",
            data: {
                uIdS: JSON.stringify(uids),
                id: focusId,
                type: isGroup ? "GROUP" : "JOB"
            },
            success: function (data) {
                layer.msg(data.message);
                window.setTimeout(leftClick, 100);
            }
        })
    });


    $('#jobOperate [name="jobDag"]').on('click', function () {
        $('#jobDagModal').modal('show');
        $('#item').val($('#jobMessage [name="id"]').val());
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
                    setCurrentId(data.message);
                    location.reload(false);
                } else {
                    alert(data.message);
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
                layer.msg(res.message);
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
        changeSwitch(focusId, focusItem.auto === "开启" ? 0 : 1);
    });


    /**
     * 任务失效按钮
     */
    $('#jobOperate [name="invalid"]').on('click', function () {
        //回显
        changeSwitch(focusId, 2);
    });

    function changeSwitch(id, status) {
        $.ajax({
            url: base_url + "/scheduleCenter/updateSwitch",
            data: {
                id: id,
                status: status
            },
            type: "post",
            success: function (data) {
                if (data.success === false) {
                    layer.msg(data.message);
                } else {
                    layer.msg(data.message);
                    leftClick();
                }
            }
        })
    }

    /**
     * 任务监控按钮
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
                        alert(data.message);
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
                        alert(data.message);

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
        let name = $('#addJobModal [name="jobName"]').val();
        let type = $('#addJobModal [name="jobType"]').val();
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
                    setCurrentId(data.message)
                    location.reload(false);
                } else {
                    alert(data.message);
                }

            }
        })
    });
    /**
     * 选择框事件 动态设置编辑区
     */
    $('#jobMessageEdit [name="scheduleType"]').change(function () {
        let status = $(this).val();
        //定时调度
        if (status == 0) {
            setJobMessageEdit(true);
        } else if (status == 1) {//依赖调度
            setJobMessageEdit(false);
        }
    });

    $('#keyWords').on('keydown', function (e) {
        if (e.keyCode == '13') {
            searchNodeLazy($.trim($(this).val()), focusTree, "keyWords", false);
        }
    });

    $('#dependKeyWords').on('keydown', function (e) {
        if (e.keyCode == '13') {
            searchNodeLazy($.trim($(this).val()), dependTreeObj, "dependKeyWords", false);
        }

    });
    let timeoutId;

    function searchNodeLazy(key, tree, keyId, first) {
        let searchInfo = $('#searchInfo');
        let deSearchInfo = $('#deSearchInfo');
        let isDepen = tree === $.fn.zTree.getZTreeObj("jobTree") || tree === $.fn.zTree.getZTreeObj('allTree');
        if (key == null || key === "" || key === undefined) {
            return;
        }
        if (isDepen) {
            searchInfo.show();
            searchInfo.text('查找中，请稍候...');
        } else {
            deSearchInfo.show();
            deSearchInfo.text('查找中，请稍候...');
        }

        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = setTimeout(function () {
            search(key); //lazy load ztreeFilter function
            $('#' + keyId).focus();
        }, 50);

        function search(key) {
            let keys, length;
            if (key !== null && key !== "" && key !== undefined) {
                keys = key.split(" ");
                length = keys.length;
                for (let i = 0; i < length; i++) {
                    if (isNaN(keys[i])) {
                        keys[i] = keys[i].toLowerCase();
                    }
                }
                let nodeShow = tree.getNodesByFilter(filterNodes);
                if (nodeShow && nodeShow.length > 0) {
                    nodeShow.forEach(function (node) {
                        expandParent(node, tree);
                    });
                    if (isDepen) {
                        searchInfo.hide();
                    } else {
                        deSearchInfo.hide();
                    }
                    tree.refresh();
                } else {
                    if (isDepen) {
                        searchInfo.text('未找到该节点');
                    } else {
                        deSearchInfo.text('未找到该节点');
                    }
                    layer.msg("如果是新加节点，请刷新网页后再搜索一次哟");
                }
            }

            function filterNodes(node) {
                for (let i = 0; i < length; i++) {
                    //id搜索
                    if (!isNaN(keys[i])) {
                        if (node.jobId == keys[i]) {
                            if (isDepen) {
                                tree.checkNode(node, true, true, false);
                                node.isParent ? node.highlight = 1 : node.highlight = 2;
                                tree.showNode(node);
                                if (i === 0) {
                                    tree.selectNode(node);
                                }
                            } else {
                                if (!node.isParent) {
                                    node.highlight = 2;
                                    tree.showNode(node);
                                    if (node.name === '0-1. tmp_tab(1218)' && node.checked === false) {
                                        console.log('start node unchecked');
                                    }
                                    if (first) tree.checkNode(node, true, true, false);
                                }
                            }
                            return true;
                        }
                    } else {//name搜索
                        if (node.jobName.toLowerCase().indexOf(keys[i]) != -1) {
                            if (isDepen) {
                                node.isParent ? node.highlight = 1 : node.highlight = 2;
                                tree.showNode(node);
                                tree.checkNode(node, true, true, false);
                                if (i === 0) {
                                    tree.selectNode(node);
                                }
                            } else {
                                if (!node.isParent) {
                                    node.highlight = 2;
                                    tree.showNode(node);
                                    if (first) tree.checkNode(node, true, true, false);
                                }
                            }
                            return true;
                        }
                    }
                }

                if (node.checked && !node.isParent) {
                    tree.checkNode(node, false, true, false);
                }
                if (!first) {
                    tree.hideNode(node);
                }
                node.highlight = 0;
                return false;
            }
        }
    }


    function expandParent(node, obj) {
        if (node) {
            let path = node.getPath();
            if (path && path.length > 0) {
                for (let i = 0; i < path.length - 1; i++) {
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
        let status1 = "block", status2 = "none";
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
                        layer.msg(data.message)
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
                        layer.msg(data.message);
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
                    type: isGroup ? "GROUP" : "JOB"
                },
                type: "post",
                success: function (data) {
                    layer.msg(data.message);
                    if (data.success == true) {
                        let parent = selected.getParentNode();
                        focusTree.removeNode(selected);
                        expandParent(parent, focusTree);
                        focusTree.selectNode(parent);
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
        let status1 = "none", status2 = "block";
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
        let val, userConfigs = "";
        //首先过滤内置配置信息 然后拼接用户配置信息
        for (let key in configs) {
            val = configs[key];
            if (key === "roll.back.times") {
                let backTimes = $("#" + dom + " [name='rollBackTimes']");
                if (dom == "jobMessage") {
                    backTimes.val(val);
                } else {
                    backTimes.val(val);
                }
            } else if (key === "roll.back.wait.time") {
                let waitTime = $("#" + dom + " [name='rollBackWaitTime']");
                if (dom == "jobMessage") {
                    waitTime.val(val);
                } else {
                    waitTime.val(val);
                }
            } else if (key === "run.priority.level") {
                let level = $("#" + dom + " [name='runPriorityLevel']");
                if (dom == "jobMessage") {
                    level.val(val == 1 ? "low" : val == 2 ? "medium" : "high");
                } else {
                    level.val(val);
                }
            } else if (key === "zeus.dependency.cycle" || key === "hera.dependency.cycle") {
                let cycle = $("#" + dom + " [name='heraDependencyCycle']");
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

        if (treeNode.highlight === 1) {
            return {
                color: "#37a64d",
                "font-weight": "bold"
            };
        } else if (treeNode.highlight === 2) {
            return {
                color: "#A60000",
                "font-weight": "bold"
            };
        } else {
            return {
                color: "rgba(0, 0, 0, 0.65)",
                "font-weight": "normal"
            };
        }
    }

    function beforeDrop(treeId, treeNodes, targetNode, moveType) {
        let node = treeNodes[0];

        //inner
        if (moveType === 'inner') {
            if (targetNode.directory === node.directory && node.directory === null) {
                layer.msg("任务无法放到任务节点内");
                return false;
            }

            if (targetNode.directory === null || (targetNode.directory === 1 && node.directory === 0)) {
                layer.msg("大节点无法放在小节点内");
                return false;
            }

            return moveNode(node, targetNode.id);
        } else {
            if (targetNode.directory !== node.directory) {
                layer.msg("两个节点的级别不同，无法移动");
                return false;
            }
            return moveNode(node, targetNode.parent);
        }
    }

    function moveNode(node, parent) {
        let res = false;
        $.ajax({
            url: base_url + '/scheduleCenter/moveNode',
            data: {
                id: node.id,
                parent: parent,
                lastParent: node.parent
            },
            async: false,
            success: function (data) {
                res = data.success;
            }
        });
        if (res) {
            layer.msg("移动节点[" + node.name + "]成功");
        } else {
            layer.msg("移动节点[" + node.id + "]失败");
        }
        return res;
    }

    function beforeDrag(treeId, treeNodes) {
        if (treeNodes.length > 1) {
            layer.msg("不允许同时拖动多个任务");
            return false;
        }
        let check = false;
        $.ajax({
            url: base_url + '/scheduleCenter/check',
            data: {
                id: treeNodes[0].id
            },
            async: false,
            success: function (data) {
                check = data.data;
            }
        });
        if (!check) {
            layer.msg("抱歉，无权限移动该任务");
        }

        return check;
    }

    function leftClick() {
        selected = focusTree.getSelectedNodes()[0];
        changeOverview(true);
        if (selected) {
            let id = selected.id;
            let dir = selected.directory;
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
                    success: function (result) {
                        if (result.success === false) {
                            layer.msg(result.message);
                            return;
                        }
                        let data = result.data;
                        focusItem = data;
                        if (data.runType === "Shell") {
                            codeMirror.setOption("mode", "text/x-sh");
                        } else {
                            codeMirror.setOption("mode", "text/x-hive");
                        }
                        if (data.script != null) {
                            codeMirror.setValue(data.script);
                        } else {
                            codeMirror.setValue('');
                        }
                        let isShow = data.scheduleType === 0;
                        $('#dependencies').css("display", isShow ? "none" : "");
                        $('#heraDependencyCycle').css("display", isShow ? "none" : "");
                        $('#cronExpression').css("display", isShow ? "" : "none");
                        formDataLoad("jobMessage form", data);
                        $("#jobMessage [name='scheduleType']").val(isShow ? "定时调度" : "依赖调度");
                        selfConfigCM.setValue(initVal(data.configs, "jobMessage"));
                        $('#jobMessage [name="auto"]').removeClass("label-primary")
                            .removeClass("label-default").removeClass("label-info")
                            .addClass(data.auto === "开启" ? "label-primary" : data.auto === "失效" ? "label-info" : "label-default");
                        $('#jobMessage [name="repeatRun"]').removeClass("label-primary")
                            .removeClass("label-default").addClass(data.repeatRun === 1 ? "label-primary" : "label-default").val(data.repeatRun === 1 ? "是" : "否");

                        $('#jobOperate [name="monitor"]').text(data.focus ? "取消关注" : "关注该任务");

                        let areas = '';
                        $.each(data.areaId.split(","), function (index, id) {
                            if (index === 0) {
                                areas = allArea[id];
                            } else {
                                areas = areas + "," + allArea[id];
                            }
                        });

                        $('#jobMessage [name="area"]').val(areas);
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
                    success: function (result) {
                        if (result.success === false) {
                            layer.msg(result.message);
                        }
                        var data = result.data;
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
                let jobDisabled;
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

    }

    function parseJson(obj) {
        let res = "";
        for (let x in obj) {
            res = res + x + "=" + obj[x] + "\n";
        }
        return res;
    }

    $("#manual").click(function () {
        setAction();
    });



    $("#myModal .add-btn").click(function () {

        $.ajax({
            url: base_url + "/scheduleCenter/manual.do",
            type: "get",
            data: {
                actionId: $("#selectJobVersion").val(),
                triggerType: $('#myModal [name="triggerType"]:radio:checked').val()
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
        $('#myModal').modal('hide');
    });


    $('#jobOperate [name="copyJob"]').on('click', function () {
        $('#copyJobModal').modal('show');
    });

    $("#copyJobModal .add-btn").click(function () {
        $.ajax({
            url: base_url + "/scheduleCenter/copyJob",
            type: "post",
            data: {
                jobId: focusId
            },
            success: function (res) {
                if (res.success === true) {
                    setCurrentId(res.message)
                    window.location.reload();
                    layer.msg(res.message);
                } else {
                    layer.msg(res.message)
                }

            },
            error: function (err) {
                layer.msg(err);
            }
        });
        $('#copyJobModal').modal('hide');
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
                if (data.success === false) {
                    alert(data.message);
                    return;
                }
                let jobVersion = "";
                data.data.forEach(function (action, index) {
                    jobVersion += '<option value="' + action.id + '" >' + action.id + '</option>';
                });
                let jobVer = $('#selectJobVersion');
                jobVer.empty();
                jobVer.append(jobVersion);
                jobVer.selectpicker('refresh');
                $('#myModal').modal('show');
            }
        });
    }


    let zNodes;
    let firstAllTreeInit = true;
    let firstMyTreeInit = true;

    $(document).ready(function () {

            $.ajax({
                url: base_url + "/scheduleCenter/getAllArea",
                async: false,
                type: "get",
                success: function (data) {
                    let areaOption = '';
                    $.each(data.data, function (index, area) {
                        allArea[area.id] = area.name;
                        areaOption = areaOption + '<option value="' + area.id + '">' + area.name + '</option>';
                    });
                    let areas = $('#jobMessageEdit [name="areaId"]');
                    areas.empty();
                    areas.append(areaOption);
                    areas.selectpicker('refresh');
                }
            });
            zNodes = getDataByPost(base_url + "/scheduleCenter/init.do");
            $('#allScheBtn').click(function (e) {
                e.stopPropagation();
                allJobTree();
                localStorage.setItem("taskGroup", 'all');
            });

            $('#myScheBtn').click(function (e) {
                e.stopPropagation();
                myJobTree();
                localStorage.setItem("taskGroup", 'mySelf');
            });
            $.each($(".content .row .height-self"), function (i, n) {
                $(n).css("height", (screenHeight - 50) + "px");
            });

            let theme = localStorage.getItem("theme");
            if (theme == null) {
                theme = 'default';
            }
            themeSelect.val(theme);
            codeMirror = CodeMirror.fromTextArea(editor[0], {
                mode: "text/x-sh",
                lineNumbers: true,
                theme: theme,
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
            setDefaultSelectNode();
            $.ajax({
                url: base_url + "/scheduleCenter/getHostGroupIds",
                type: "get",
                success: function (result) {
                    if (result.success === false) {
                        layer.msg(data.message);
                        return;
                    }
                    let data = result.data;
                    let hostGroup = $('#jobMessageEdit [name="hostGroupId"]');
                    let option = '';
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
                let para = $.trim($('#timeChange').val());
                let arr = para.split(' ');
                let min = arr[1];
                let hour = arr[2];
                let day = arr[3];
                let month = arr[4];
                let week = arr[5];
                $('#inputMin').val(min);
                $('#inputHour').val(hour);
                $('#inputDay').val(day);
                $('#inputMonth').val(month);
                $('#inputWeek').val(week);
            });
            $('#saveTimeBtn').click(function (e) {
                e.stopPropagation();
                let min = $.trim($('#inputMin').val());
                let hour = $.trim($('#inputHour').val());
                let day = $.trim($('#inputDay').val());
                let month = $.trim($('#inputMonth').val());
                let week = $.trim($('#inputWeek').val());
                let para = '0 ' + min + ' ' + hour + ' ' + day + ' ' + month + ' ' + week;
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

            $('#nextNode').on("click", function () {
                let expand = $('#expand').val();
                if (expand == null || expand == undefined || expand == "") {
                    expand = 0;
                }
                expandNextNode(expand);

            });
            $('#expandAll').on("click", function () {
                expandNextNode(len);
            });

        }
    )
    ;
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
        let setting = {
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
            }
        };
        $.fn.zTree.init($("#dependTree"), setting, zNodes.allJob);
        dependTreeObj = $.fn.zTree.getZTreeObj("dependTree");
        searchNodeLazy($(this).val().split(',').join(' '), dependTreeObj, "dependKeyWords", true);
        $("#chooseDepend").bind('click', function () {
            let nodes = dependTreeObj.getCheckedNodes(true);
            let ids = new Array();
            for (let i = 0; i < nodes.length; i++) {
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


    function allJobTree() {
        $('#jobTree').hide();
        $('#allTree').show();
        $('#allScheBtn').parent().addClass('active');
        $('#myScheBtn').parent().removeClass('active');

        if (firstAllTreeInit) {
            firstAllTreeInit = false;
            $.fn.zTree.init($("#allTree"), setting, zNodes.allJob);
        }
        focusTree = $.fn.zTree.getZTreeObj("allTree");


    }

    function myJobTree() {
        $('#jobTree').show();
        $('#allTree').hide();
        $('#myScheBtn').parent().addClass('active');
        $('#allScheBtn').parent().removeClass('active');
        if (firstMyTreeInit) {
            firstMyTreeInit = false;
            $.fn.zTree.init($("#jobTree"), setting, zNodes.myJob);
        }
        focusTree = $.fn.zTree.getZTreeObj("jobTree");
    }

    function changeOverview(type) {
        let overview = "", notShow = "none";
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
        groupTaskType = 'all';
        jobDt = $('#jobDt').val();
        reloadGroupTaskTable();

    });

    $('#overviewOperator [name="showAll"]').on('click', function () {
        groupTaskType = 'all';
        jobDt = $('#jobDt').val();
        reloadGroupTaskTable();
    });


    $('#overviewOperator [name="showRunning"]').on('click', function () {
        groupTaskType = 'running';
        jobDt = $('#jobDt').val();
        reloadGroupTaskTable();
    });

    $('#overviewOperator [name="showFaild"]').on('click', function () {
        groupTaskType = 'failed';
        jobDt = $('#jobDt').val();
        reloadGroupTaskTable();
    });

    $('#overviewOperator [name="showSucc"]').on('click', function () {
        groupTaskType = 'success';
        jobDt = $('#jobDt').val();
        reloadGroupTaskTable();
    });


    $('#groupOperate [name="showFaild"]').on('click', function () {
        groupTaskType = 'failed';
        jobDt = $('#jobDt').val();
        reloadGroupTaskTable();
    });

    $('#groupOperate [name="showRunning"]').on('click', function () {
        groupTaskType = 'running';
        jobDt = $('#jobDt').val();
        reloadGroupTaskTable();
    });

    $('#groupOperate [name="showAll"]').on('click', function () {
        groupTaskType = 'all';
        jobDt = $('#jobDt').val();
        reloadGroupTaskTable();
    });

    $('#groupOperate [name="showSucc"]').on('click', function () {
        groupTaskType = 'success';
        jobDt = $('#jobDt').val();
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
                    status: groupTaskType,
                    dt: jobDt
                }
                , method: 'get'
                , page: true
                , limits: [10, 30, 50]
            });
        } else {
            groupTaskTable.reload({
                where: {
                    groupId: focusId,
                    status: groupTaskType,
                    dt: jobDt
                },
                page: {
                    curr: 1 //重新从第 1 页开始
                }
            });
        }

    }

    let recordTable = function (jobId) {
        let oTableInit = new Object();
        let table = $('#recordTable');
        oTableInit.init = function () {
            table.bootstrapTable({
                url: base_url + "/record/find",
                pagination: true,
                showPaginationSwitch: false,
                search: false,
                cache: false,
                pageNumber: 1,
                sidePagination: "server",
                queryParamsType: "limit",
                queryParams: function (params) {
                    return {
                        pageSize: params.limit,
                        offset: params.offset,
                        jobId: jobId
                    };
                },
                onLoadSuccess: function (data) {
                    if (data.success === false) {
                        layer.msg("加载操作日志失败");
                        return;
                    }
                    console.log(data.data)
                    table.bootstrapTable("load", data.data)
                },
                pageList: [10, 25, 40, 60],
                columns: [
                    {
                        field: "type",
                        title: "操作类型",
                        align: 'center',
                        halign: 'center'
                    }, {
                        field: "logType",
                        title: "日志类型",
                        halign: 'center',
                        align: 'center'
                    }, {
                        field: "sso",
                        title: "操作人",
                        align: 'center',
                        halign: 'center'
                    }, {
                        field: "gname",
                        title: "所在组",
                        align: 'center',
                        halign: 'center'
                    }, {
                        field: "createTime",
                        title: "操作时间",
                        align: 'center',
                        halign: 'center'
                    }
                ],
                detailView: true,
                detailFormatter: function (index, row) {
                    return "<div id='compare'> </div>";
                },
                onExpandRow: function (index, row) {
                    var value = "", mode = "", highlight = true, connect = null, collapse = false;
                    $.ajax({
                        url: base_url + "/record/now",
                        data: {
                            logId: row.logId,
                            logType: row.logType,
                            type: row.type
                        },
                        type: "get",
                        async: false,
                        success: function (res) {
                            if (res.success == false) {
                                layer.msg(res.message);
                            }
                            var data = res.data;
                            if (data.runType === "Shell") {
                                mode = "text/x-sh";
                            } else {
                                mode = "text/x-hive";
                            }
                            value = data.content;
                        }
                    });
                    var target = document.getElementById("compare");
                    target.innerHTML = "";
                    CodeMirror.MergeView(target, {
                        value: value,
                        origLeft: row.content,
                        orig: null,
                        theme: 'eclipse',
                        lineNumbers: true,
                        mode: mode,
                        matchBrackets: true,
                        styleActiveLine: true,
                        highlightDifferences: highlight,
                        connect: connect,
                        collapseIdentical: true,
                        revertButtons: true
                    });
                }
            });
        };
        return oTableInit;
    }


    var JobLogTable = function (jobId) {
        let parameter = {jobId: jobId};
        let actionRow;
        let oTableInit = new Object();
        let table = $('#runningLogDetailTable');
        let closeLog = false;

        function scheduleLog() {

            $.ajax({
                url: base_url + "/scheduleCenter/getLog.do",
                type: "get",
                data: {
                    id: actionRow.id,
                    jobId: actionRow.jobId
                },
                success: function (result) {
                    let logArea = $('#log_' + actionRow.id);
                    if (result.success === false) {
                        layer.msg(result.message);
                        logArea[0].innerHTML = "无日志查看权限,请联系管理员进行配置";
                        return;
                    }
                    let data = result.data;
                    if (data.status === 'running' && !closeLog) {
                        window.setTimeout(scheduleLog, 5000);
                    }
                    logArea[0].innerHTML = data.log;
                    logArea.scrollTop(logArea.prop("scrollHeight"), 200);

                    actionRow.log = data.log;
                    actionRow.status = data.status;
                }
            })
        }

        $('#jobLog').on('hide.bs.modal', function () {
            closeLog = true;
        });

        $('#jobLog [name="refreshLog"]').on('click', function () {
            table.bootstrapTable('refresh');
            closeLog = true;
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
                showPaginationSwitch: false,  //是否显示选择分页数按钮
                sidePagination: "server",
                queryParamsType: "limit",
                queryParams: function (params) {
                    let tmp = {
                        pageSize: params.limit,
                        offset: params.offset,
                        jobId: jobId,
                        beginDt: "1900-01-01",
                        endDt: "2999-12-31",
                        jobType: "job"
                    };
                    return tmp;
                }, onLoadSuccess: function (data) {
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
                        width: "14%",
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
                        width: "11%",
                        align: 'center',
                        halign: 'center'

                    }, {
                        field: "endTime",
                        title: "结束时间",
                        width: "11%",
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
                        align: 'center'
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
                        width: "10%",
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
                    let html = '<form role="form">' + '<div class="form-group" style="background: #2c4762;min-height:600px; overflow:scroll;  ">' + '<div class="form-control"  style="border:none; height:600px; word-break: break-all; word-wrap:break-word; white-space:pre-line;font-family:Microsoft YaHei" id="log_' + row.id + '">'
                        + '日志加载中。。' +
                        '</div>' + '<form role="form">' + '<div class="form-group">';
                    return html;
                },
                onExpandRow: function (index, row) {
                    actionRow = row;
                    closeLog = false;
                    scheduleLog();
                },
                onCollapseRow: function (index, row) {
                    closeLog = true;
                }
            });
        };
        return oTableInit;
    };

    }
);

function keypath(type) {
    $('#expandAll').removeClass('active').addClass('disabled');
    graphType = type;
    let node = $("#item")[0].value;
    if (node == "")
        return;
    let url = base_url + "/scheduleCenter/getJobImpactOrProgress";
    let data = {jobId: node, type: type};

    let success = function (data) {
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
    labelname = headNode.descName +"["+ headNode.nodeName +"]";
    g.setNode(headNode.nodeName, {label: labelname, style: "fill: #bd16ff" + ";" + headNode.remark})
    let nodeName;
    for (let i = 0; i < len; i++) {
        nodeName = edges[i].nodeA.nodeName;
        if (nodeIndex[nodeName] == null || nodeIndex[nodeName] == undefined || nodeIndex[nodeName] == 0) {
            nodeIndex[nodeName] = i + 1;
        }
    }
}


function expandNextNode(nodeNum) {
    while (nodeNum > 0) {
        if (currIndex < len) {
            let edge = edges[currIndex];
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


function selectTheme() {
    let theme = themeSelect.val();
    codeMirror.setOption("theme", theme);
    localStorage.setItem("theme", theme);
}

function cancelJob(historyId, jobId) {
    let url = base_url + "/scheduleCenter/cancelJob.do";
    let parameter = {historyId: historyId, jobId: jobId};
    $.get(url, parameter, function (data) {
        layer.msg(data.message);
        $('#jobLog [name="refreshLog"]').trigger('click');
    });
}
