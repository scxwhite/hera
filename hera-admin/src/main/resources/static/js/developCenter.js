var codeMirror;
$(function () {
    $('#developManage').addClass('active');
    /**
     * 开发中心zTree初始化配置
     *
     */
    var setting = {
        view: {
            // addHoverDom:addHoverDom,
            // removeHoverDom: removeHoverDom,
            selectedMulti: false
        },
        data: {
            simpleData: {
                enable: true,
                idKey: "id",
                pIdKey: "parent",
                rootPId: 0
            },
            keep:{
                parent:true
            }
        },
        callback: {
            onRightClick: OnRightClick,
            onClick: leftClick,
            onRename:renameFile,
            onRemove:removeFile
        },
        edit:{
            enable:true,
            editNameSelectAll: true,
        }
    };

    function add(e) {
        var zTree = $.fn.zTree.getZTreeObj("documentTree"),
            isParent = e.data.isParent,
            nodes = zTree.getSelectedNodes(),
            treeNode = nodes[0];
            hideRMenu();
            var selected = zTree.getSelectedNodes()[0];
            var id = selected['id'];
            addCount++;
            if(e.data.type===1){
                //new folder
                var name = "文件夹" + addCount;
                var parameter = "parent=" + id + "&type=" + "1" + "&name=" + name;

                $.ajax({
                    url: base_url + "/developCenter/addFile.do",
                    type: "get",
                    async: false,
                    data: parameter,
                    success: function (data) {
                        if (treeNode) {
                            treeNode = zTree.addNodes(treeNode, {id:data, pId:treeNode.id, isParent:isParent, name:name});
                        } else {
                            treeNode = zTree.addNodes(null, {id:data, pId:0, isParent:isParent, name: name});
                        }
                        if (treeNode) {
                            zTree.editName(treeNode[0]);
                        } else {
                            layer.msg("叶子节点被锁定，无法增加子节点");
                        }
                    }
                });
            } else if(e.data.type===2){
                var name = addCount + ".hive";

                var parameter = "parent=" + id + "&type=" + "2" + "&name=" + name;
                console.log(zTree)
                $.ajax({
                    url: base_url + "/developCenter/addFile.do",
                    type: "get",
                    async: false,
                    data: parameter,
                    success: function (data) {
                        console.log(data)
                        if (treeNode) {
                            treeNode = zTree.addNodes(treeNode, {id:data, pId:treeNode.id, isParent:isParent, name: name});
                        } else {
                            treeNode = zTree.addNodes(null, {id:data, pId:0, isParent:isParent, name: name});
                        }
                        if (treeNode) {
                            zTree.editName(treeNode[0]);
                        } else {
                            layer.msg("叶子节点被锁定，无法增加子节点");
                        }
                    }
                });

            }else if(e.data.type===3){
                //new .sh file
                var name = addCount + ".sh";
                var parameter = "parent=" + id + "&type=" + "2" + "&name=" + name;

                $.ajax({
                    url: base_url + "/developCenter/addFile.do",
                    type: "get",
                    async: false,
                    data: parameter,
                    success: function (data) {
                        if (treeNode) {
                            treeNode = zTree.addNodes(treeNode, {id:data, pId:treeNode.id, isParent:isParent, name: name});
                        } else {
                            treeNode = zTree.addNodes(null, {id:data, pId:0, isParent:isParent, name: name});
                        }
                        if (treeNode) {
                            zTree.editName(treeNode[0]);
                        } else {
                            layer.msg("叶子节点被锁定，无法增加子节点");
                        }
                    }
                });
            }

    };
    //修改文件名后回调
    function renameFile(event, treeId, treeNode, isCancel) {
        $.ajax({
            url: base_url + "/developCenter/rename.do",
            type:'get',
            data:{
                id:treeNode.id,
                name:treeNode.name
            },
            success:function (res) {
                layer.msg(res);
                //同步修改codemirror上的tab名
                var id = treeNode.id;
                var name = treeNode.name;
                var tabDetail = {id: id, text: name, closeable: true, url: 'hera', select: 0};
                tabData = JSON.parse(localStorage.getItem('tabData'));
                tabObj = $("#tabContainer").tabs({
                    data: tabDetail,
                    showIndex: 0,
                    loadAll: true
                });
                if(isInArray(tabData,tabDetail)){
                    tabObj = $("#tabContainer").data("tabs").changeText(id,name);
                    //更改localStorage内的值
                    console.log(tabData);
                    for(var i =0;i<tabData.length;i++){
                        if(tabData[i].id===id){
                            tabData.splice(i,1);
                            break;
                        }
                    }
                }else{
                    tabObj = $('#tabContainer').data('tabs').addTab(tabDetail);
                }
                if(!treeNode.isParent){
                    tabData.push(tabDetail);
                }
                localStorage.setItem("tabData", JSON.stringify(tabData));
            },
            error:function (err) {
                layer.msg(err);
            }
        })
    }

    //删除文件后回调
    function removeFile(event, treeId, treeNode) {
        $.ajax({
            url: base_url + "/developCenter/delete.do",
            type:'get',
            data:{
                id:treeNode.id
            },
            success:function (res) {
                layer.msg(res);
                //从localStorage中删除
                tabData = JSON.parse(localStorage.getItem('tabData'));
                for(var i =0;i<tabData.length;i++){
                    if(tabData[i].id===treeNode.id){
                        tabData.splice(i,1);
                        break;
                    }
                }
                //localStorage delete
                localStorage.setItem('tabData',JSON.stringify(tabData));
                //移除tab
                tabObj = $("#tabContainer").data("tabs").remove(treeNode.id);
            },
            error:function (err) {
                layer.msg(err);
            }
        })

    }
    /**
     * zTree 右键菜单初始化数据
     */
    var zTree, rMenu;

    /**
     * tab项数据
     *
     * @type {{}}
     */
    var tabObj = {};

    /**
     * 存储在localStorage中的数据
     *
     * @type {Array}
     */

    var tabData = new Array();
    var logData = new Array();

    /**
     * 添加的叶子节点个数统计，为重命名统计
     *
     */
    var addCount = 1;

    var zNodes = getDataByPost(base_url + "/developCenter/init.do");
    var editor = $("#fileScript");


    /**
     * 点击脚本的事件
     */
    var tabContainer = $('#tabContainer');
    var logTabContainer = $('#rightLogCon');
    var rightNowLogCon = $('#rightNowLogCon');
    var rightTimer = null;
    var debugId;

    function leftClick() {
        var selected = zTree.getSelectedNodes()[0];
        var id = selected['id'];
        localStorage.setItem("id", id);
        var parent = selected['parent'];
        var name = selected['name'];
        var isParent = selected['isParent'];//true false
        if (isParent == true) {
            return;
        }

        var parameter = "id=" + id;
        var url = base_url + "/developCenter/find.do";
        var result = getDataByGet(url, parameter)
        var script = result['content'];
        if (script == null || script == '') {
            script = '';
        }

        setScript(id);

        var tabDetail = {id: id, text: name, closeable: true, url: 'hera', select: 0};
        localStorage.setItem("id", id);//记录活动选项卡id
        tabData = JSON.parse(localStorage.getItem('tabData'));
        var b = isInArray(tabData, tabDetail);
        if (b == false) {
            tabData.push(tabDetail);
            tabObj = $("#tabContainer").tabs({
                data: tabDetail,
                showIndex: 0,
                loadAll: true
            });

            $("#tabContainer").data("tabs").addTab(tabDetail);
            var lis = tabContainer.children('ul').children();
            tabContainer.tabsLength += lis[lis.length - 1].clientWidth;
            showPrevNext(tabContainer);
        } else {
            tabObj = $("#tabContainer").tabs({
                data: tabDetail,
                showIndex: 0,
                loadAll: true
            });
            tabObj = $("#tabContainer").data("tabs").showTab(id);
            console.log(id)
        }
        localStorage.setItem("tabData", JSON.stringify(tabData));

    }
    tabContainer.on('deleteTab',function (e,length) {
        e.stopPropagation();
        tabContainer.tabsLength -= length;
    })

    /**
     * 查看脚本运行日志
     */

    $("#logButton").click(function () {
        var targetId = $("#tabContainer").data("tabs").getCurrentTabId();
        $('#debugLogDetailTable').bootstrapTable("destroy");
        var tableObject = new TableInit(targetId);
        tableObject.init();
        $("#debugLogDetail").modal('show');
    });

    $('#openLogs').click(function (e) {
        e.stopPropagation();
        $(this).tab('show');
        showLogs();
    })

    //显示日志
    function showLogs(){
        var targetId = $("#tabContainer").data("tabs").getCurrentTabId();
        var parameter = {fileId: targetId};
        var actionRow = new Object();
        var timerHandler = null;
        actionRow.id = targetId;
        $.ajax({
            url: base_url + "/developCenter/getLog.do",
            data: {
                fileId:actionRow.id
            },
            success: function (data) {
                if (data.status != 'running') {
                    window.clearInterval(timerHandler);
                }
                var logArea = $('#outputContainer');
                logArea.innerHTML = data.log;
                logArea.scrollTop(logArea.prop("scrollHeight"), 200);
                actionRow.log = data.log;
                actionRow.status = data.status;
            }
        })
    }

    /**
     *
     */
    $("#saveScript").click(function () {
        var fileId = $("#tabContainer").data("tabs").getCurrentTabId();
        var fileScript = codeMirror.getValue();
        var parameter = {
            id: fileId,
            content: fileScript
        };
        var url = base_url + "/developCenter/saveScript.do";

        $.ajax({
            url: url,
            type: "post",
            data: JSON.stringify(parameter),
            contentType: "application/json",
            dataType: "json",
            success: function (data) {
                layer.msg(data.msg);
            },
            error:function (err) {
                layer.msg(err);
            }
        });
     });

    /**
     * 树形菜单右击事件
     * @param event
     * @param treeId
     * @param treeNode
     * @constructor
     */
    var rightClickNode;
    function OnRightClick(event, treeId, treeNode) {
        zTree.selectNode(treeNode);
        rightClickNode=treeNode;
        var selected = zTree.getSelectedNodes()[0];
        var isParent = selected['isParent'];//true false
        if (isParent == true) {
            showRMenu("root", event.clientX, event.clientY);
        } else if (isParent == false) {
            showRMenu("node", event.clientX, event.clientY);
        }
    }

    /**
     * 修改右击后菜单显示样式
     * @param type
     * @param x
     * @param y
     */
    function showRMenu(type, x, y) {
        $("#rMenu ul").show();
        if (type == "root") {
            $("#addFolder").show();
            $("#addHiveFile").show();
            $("#addShellFile").show();
            $("#rename").show();
            $("#removeFile").show();
        } else if (type == "node") {
            $("#addFolder").hide();
            $("#addHiveFile").hide();
            $("#addShellFile").hide();
        }

        y += document.body.scrollTop;
        x += document.body.scrollLeft;

        rMenu.css({"top": y / 2 + "px", "left": x / 2 + "px", "visibility": "visible", position: "absolute"});

        $("body").bind("mousedown", onBodyMouseDown);
    }

    /**
     * 隐藏菜单
     */
    function hideRMenu() {
        if (rMenu) rMenu.css({"visibility": "hidden"});
        $("body").unbind("mousedown", onBodyMouseDown);
    }

    /**
     * 鼠标移开后的菜单隐藏事件
     * @param event
     */
    function onBodyMouseDown(event) {
        if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length > 0)) {
            rMenu.css({"visibility": "hidden"});
        }
    }

    $("#addFolder").bind('click',{isParent:true,type:1},add);

    $("#addHiveFile").bind('click',{isParent:false,type:2},add);


    $("#addShellFile").bind('click',{isParent:false,type:3},add);
    //重命名
    $("#rename").bind('click',{node:rightClickNode},function () {
        hideRMenu();
        var selected = zTree.getSelectedNodes()[0];
        console.log(selected,rightClickNode);
        var treeObj = $.fn.zTree.getZTreeObj("documentTree");
        treeObj.editName(selected);
    });


    $("#removeFile").click(function () {
        var selected = zTree.getSelectedNodes()[0];
        var id = selected['id'];
        var parameter = "id=" + id;

        var tabData = JSON.parse(localStorage.getItem('tabData'));
        tabData = tabData.filter(function (item) {
            return item['id'] != id;
        });
        localStorage.setItem("tabData", JSON.stringify(tabData));
        $.ajax({
            url: base_url + "/developCenter/delete.do",
            type: "get",
            async: false,
            data: parameter,
            success: function (data) {
                layer.msg(data)
                var zTree = $.fn.zTree.getZTreeObj("documentTree"),
                    nodes = zTree.getSelectedNodes(),
                    treeNode = nodes[0];
                if (nodes.length == 0) {
                    alert("请先选择一个节点");
                    return;
                }
                zTree.removeNode(treeNode);
            }
        });
    });


    /**
     * 修正zTree的图标，让文件节点显示文件夹图标
     */
    function fixIcon() {
        $.fn.zTree.init($("#documentTree"), setting, zNodes);
        var treeObj = $.fn.zTree.getZTreeObj("documentTree");
        //过滤出属性为true的节点（也可用你自己定义的其他字段来区分，这里通过保存的true或false来区分）
        var folderNode = treeObj.getNodesByFilter(function (node) {
            return node.isParent
        });
        for (var j = 0; j < folderNode.length; j++) {//遍历目录节点，设置isParent属性为true;
            folderNode[j].isParent = true;
            folderNode[j].directory = 0;
        }
        treeObj.refresh();//调用api自带的refresh函数。
    }


    $("#execute").click(function () {

        var fileId = $("#tabContainer").data("tabs").getCurrentTabId();
        var fileScript = codeMirror.getValue();
        var parameter = {
            id: fileId,
            content: fileScript
        };
        var result = null;
        var url = base_url + "/developCenter/debug.do";

        $.ajax({
            url: url,
            type: "post",
            data: JSON.stringify(parameter),
            contentType: "application/json",
            dataType: "json",
            success: function (data) {
                result = data;
                debugId=data.debugId;
                showRightNowLog(data.fileId,data.debugId);
            }
        });


    });
    //获得当前log
    function getRightNowLog(){
        $.ajax({
            url: base_url + "/developCenter/getLog.do",
            type: "get",
            data: {
                id: debugId,
            },
            success: function (data) {
                console.log("data.status " + data.status)
                if (data.status !== 'running') {
                    clearInterval(rightTimer);
                    set('log'+debugId,data.log);
                }
                var logArea = $('#log' + debugId);
                logArea.html(data.log);

            }
        })
    }
    //检查localStorage 有则显示日志
    function showStorageLog() {
        var last;
        for(var i =0;i<localStorage.length;i++){
            if(localStorage.key(i).indexOf('log')!==-1){
                last = localStorage.key(i);
                var ul = logTabContainer.children('ul');
                ul.children().removeClass('active-log');
                ul.prepend("<li class=\"logs-id active-log\">DebugId : "+localStorage.key(i).slice(3)+"<span class=\"iconfont close-btn\">&#xe64d;</span></li>");
                ul.children('li:first').attr('his-id',localStorage.key(i).slice(3));
                showPrevNext(logTabContainer);
            }
        }
        rightNowLogCon.children().first().remove();
        rightNowLogCon.prepend('<div class=\"right-now-log\" id=\"'+last+'\"></div>');
        var logArea = $('#' + last);
        logArea.html(localStorage.getItem(last));
    }
    showStorageLog();
    //显示当前日志
    function showRightNowLog(id,debugId){
        //tab
        var ul = logTabContainer.children('ul');
        ul.children().removeClass('active-log');
        ul.prepend("<li class=\"logs-id active-log\">DebugId : "+debugId+"<span class=\"iconfont close-btn\">&#xe64d;</span></li>");
        var lis=ul.children();
        logTabContainer.tabsLength += lis[lis.length - 1].clientWidth;
        showPrevNext(logTabContainer);
        ul.children('li:first').attr('his-id',debugId);
        //日志
        rightNowLogCon.children().first().remove();
        rightNowLogCon.prepend('<div class=\"right-now-log\" id=\"log'+debugId+'\"></div>');
        if(rightTimer){
            clearInterval(rightTimer);
        }
        rightTimer=setInterval(getRightNowLog,2000);
    }
    //单击当前日志tab
    logTabContainer.on('click','li',function (e) {
        e.stopPropagation();
        logTabContainer.children('ul').children().removeClass('active-log');
        $(this).addClass('active-log');
        debugId=$(this).attr('his-id');
        rightNowLogCon.children().first().remove();
        rightNowLogCon.prepend('<div class=\"right-now-log\" id=\"log'+debugId+'\"></div>');
        var storageLog=get('log'+debugId,1000*60*60*24);
        if(storageLog){
            rightNowLogCon.children('#log'+debugId).html(storageLog);
        }else {
            if(rightTimer){
                clearInterval(rightTimer);
            }
            rightTimer=setInterval(getRightNowLog,2000);
        }
    })
    //关闭日志
    logTabContainer.on('click','span',function(e){
        e.stopPropagation();
        $('#cancelSrueModal').modal('show');
        var _this = $(this);
        $('#sureCancelBtn').click(function (e) {
            e.stopPropagation();
            $('#cancelSrueModal').modal('hide');
            var li = _this.parent();
            var width = li.width();
            logTabContainer.tabsLength -=width;
            var debugId=li.attr('his-id');
            localStorage.removeItem('log'+debugId);
            li.remove();
            clearInterval(rightTimer);
            var url = base_url + "/developCenter/cancelJob.do";
            var parameter = {id: debugId};
            $.get(url, parameter, function (data) {
                layer.msg(data);
            });

        })
    })

    /**
     * 点击执行选中代码执行逻辑
     */
    $("#executeSelector").click(function () {
        var fileId = $("#tabContainer").data("tabs").getCurrentTabId();
        var fileScript = codeMirror.getSelection();
        var parameter = {
            id: fileId,
            content: fileScript
        };
        var result = null;
        var url = base_url + "/developCenter/debugSelectCode.do";

        $.ajax({
            url: url,
            type: "post",
            data: JSON.stringify(parameter),
            contentType: "application/json",
            dataType: "json",
            success: function (data) {
                result = data;
                debugId=data.debugId;
                showRightNowLog(data.fileId,data.debugId);
            }
        });

    });
    //封装过期控制代码
    function set(key,value){
        var curTime = new Date().getTime();
        localStorage.setItem(key,JSON.stringify({data:value,time:curTime}));
    }
    function get(key,exp){
        var data = localStorage.getItem(key);
        var dataObj = JSON.parse(data);
        if(dataObj){
            if (new Date().getTime() - dataObj.time>exp) {
                console.log('信息已过期');
            }else{
                var dataObjDatatoJson = dataObj.data;
                return dataObjDatatoJson;
            }
        }
    }

    /**
     * 弹出日志div
     */

    /**
     * 文件上传
     */

    $("#uploadResource").click(function () {
        uploadFile();
    });


    $("#closeUploadModal").click(function () {
        $("#uploadFile").modal('hide');
    });


    /**
     * 初始化开发中心页面
     *
     */
    $(document).ready(function () {

        $.fn.zTree.init($("#documentTree"), setting, zNodes);
        zTree = $.fn.zTree.getZTreeObj("documentTree");
        rMenu = $("#rMenu");
        fixIcon();
        var currentId;

        codeMirror = CodeMirror.fromTextArea(editor[0], {
            mode: "sql",
            lineNumbers: true,
            autofocus: true,
            theme: "lucario",
            readOnly: false
        });
        codeMirror.display.wrapper.style.height = 500 + "px";
        codeMirror.on('keypress', function () {
            if (!codeMirror.getOption('readOnly')) {
                codeMirror.showHint();
            }
        });
        var saveTimer;
        //监听codemirror change事件 实时保存
        codeMirror.on('change',function () {
            if(saveTimer){
                clearTimeout(saveTimer);
            }
            saveTimer=setTimeout(function () {
                var fileId = $("#tabContainer").data("tabs").getCurrentTabId();
                var fileScript = codeMirror.getValue();
                var parameter = {
                    id: fileId,
                    content: fileScript
                };
                var url = base_url + "/developCenter/saveScript.do";

                $.ajax({
                    url: url,
                    type: "post",
                    data: JSON.stringify(parameter),
                    contentType: "application/json",
                    dataType: "json",
                    success: function (data) {
                        console.log(data.msg);
                    },
                    error:function (err) {
                        layer.msg(err);
                    }
                });
            },1000);
        });

        var storeData = JSON.parse(localStorage.getItem('tabData'));
        if (storeData != null) {
            for (var i = 0; i < storeData.length; i++) {
                $("#tabContainer").tabs({
                    data: storeData[i],
                    showIndex: 0,
                    loadAll: true
                });
                $("#tabContainer").data("tabs").addTab(storeData[i]);
                currentId = storeData[i]['id'];
                setScript(storeData[i]['id']);
            }
        } else {
            var tmp = new Array();
            localStorage.setItem("tabData", JSON.stringify(tmp));
        }

        $.each($(".height-self"), function (i, n) {
            $(n).css("height", (screenHeight - 50) + "px");
        });
        tabInitLength(tabContainer);
        showPrevNext(tabContainer);
        tabInitLength(logTabContainer);
        showPrevNext(logTabContainer);


    });
    //初始化log
    function logInit(){

    }

    //初始化tabs的长度
    function tabInitLength(tabContainer){
        tabContainer.tabsLength=0;
        var ul = tabContainer.children('ul');
        if(tabContainer.children('ul').length > 0) {
            var lis = tabContainer.children('ul').children();
            //初始tabs width
            for (var i = 0; i < lis.length; i++) {
                tabContainer.tabsLength += lis[i].clientWidth;
            }
            console.log('tab init',tabContainer.tabsLength);
        }
    }
    //计算tabs长度是否超过container
    function showPrevNext(tabContainer){
        tabContainer.tabContainerWidth =  tabContainer.width();
        if(tabContainer.tabsLength > tabContainer.tabContainerWidth){
            tabContainer.siblings('.prev-next-con').children('.prev-tab').addClass('show');
            tabContainer.siblings('.prev-next-con').children('.next-tab').addClass('show');
            tabContainer.children('ul').css({"padding-left": "30px"});
        }
    }

    //tab超出范围出现左右调整框
    //单击左右按钮
    $('#prevNextCon').on('click','.prev-tab',tabContainer,function (e) {
        e.stopPropagation();
        var ul =tabContainer.children('ul');
        var positionLeft = ul.position().left;
        if(positionLeft<0){
            if(-positionLeft+tabContainer.tabContainerWidth>tabContainer.tabsLength+100){
                ul.stop();
            }else{
                ul.animate({
                    left:'-=60px'
                },100)
            }
        }else {
            ul.animate({
                left:'-=60px'
            },100)
        }
    });
    $('#prevNextCon').on('click','.next-tab',tabContainer,function (e) {
        e.stopPropagation();
        var ul =tabContainer.children('ul');
        var positionLeft = ul.position().left;
        if(positionLeft<=0){
            ul.animate({
                left:'+=50px'
            },100)
        }else{
            ul.stop();
        }
    });
    $('#logContainer').on('click','.prev-tab',logTabContainer,function (e) {
        e.stopPropagation();
        var tabContainer = e.data;
        var ul =tabContainer.children('ul');
        var positionLeft = ul.position().left;
        if(positionLeft<0){
            if(-positionLeft+tabContainer.tabContainerWidth>tabContainer.tabsLength+100){
                ul.stop();
            }else{
                ul.animate({
                    left:'-=50px'
                },100)
            }
        }else {
            ul.animate({
                left:'-=50px'
            },100)
        }
    });
    $('#logContainer').on('click','.next-tab',logTabContainer,function (e) {
        e.stopPropagation();
        var tabContainer = e.data;
        var ul =tabContainer.children('ul');
        var positionLeft = ul.position().left;
        if(positionLeft<=0){
            ul.animate({
                left:'+=50px'
            },100)
        }else{
            ul.stop();
        }
    });
    var rightNowLogs = $('#outputContainer');
    rightNowLogs.hide();
    //点击查看日志 显示日志
    $('#showLog').click(function (e) {
        e.stopPropagation();
        rightNowLogs.toggle();
    })
});


var TableInit = function (targetId) {
    var parameter = {fileId: targetId};
    var actionRow;
    var onExpand = -1;
    var table = $('#debugLogDetailTable');
    var timerHandler = null;
    var oTableInit = new Object();


    function debugLog() {
        $.ajax({
            url: base_url + "/developCenter/getLog.do",
            type: "get",
            data: {
                id: actionRow.id,
            },
            success: function (data) {
                console.log("data.status " + data.status)
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

    $('#debugLog').on('hide.bs.modal', function () {
        if (timerHandler != null) {
            window.clearInterval(timerHandler)
        }
    });

    $('#debugLog [name="refreshLog"]').on('click', function () {
        table.bootstrapTable('refresh');
        table.bootstrapTable('expandRow', onExpand);
    });

    oTableInit.init = function () {
        table.bootstrapTable({
            url: base_url + "/developCenter/findDebugHistory.do",
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
                    field: "fileId",
                    title: "文件id"
                },
                {
                    field: "executeHost",
                    title: "执行机器ip"
                }, {
                    field: "status",
                    title: "状态"
                }, {
                    field: "startTime",
                    title: "开始时间",
                    formatter: function (row) {
                        return getLocalTime(row);
                    }
                }, {
                    field: "endTime",
                    title: "结束时间",
                    formatter: function (row) {
                        return getLocalTime(row);
                    }
                },
                {
                    field: "status",
                    title: "操作",
                    width: "20%",
                    formatter: function (index, row) {
                        var html = '<a href="javascript:cancelJob(\'' + row['id'] + '\')">取消任务</a>';
                        if (row['status'] == 'running') {
                            return html;
                        }
                    }
                }
            ],
            detailView: true,
            detailFormatter: function (index, row) {
                var log = row["log"];
                var html = '<form role="form">' + '<div class="form-group">' + '<div class="form-control"  style="overflow:scroll;  word-break: break-all; word-wrap:break-word;white-space: pre-line; height:600px;font-family:Microsoft YaHei" id="log_' + row.id + '">'
                    + log +
                    '</div>' + '<form role="form">' + '<div class="form-group">';
                return html;
            },
            onExpandRow: function (index, row) {
                actionRow = row;
                if (index != onExpand) {
                    table.bootstrapTable("collapseRow", onExpand);
                }
                onExpand = index;
                console.log("row.status" + row.status)
                if (row.status == "running") {
                    timerHandler = window.setInterval(debugLog, 2000);
                }
            },
            onCollapseRow: function (index, row) {
                window.clearInterval(timerHandler);
            }

        });
    }
    return oTableInit;

}


function cancelJob(historyId) {
    var url = base_url + "/developCenter/cancelJob.do";
    var parameter = {id: historyId};
    $.get(url, parameter, function (data) {
        layer.msg(data);
        $('#debugLog [name="refreshLog"]').trigger('click');
    });
}

/**
 * 根据id设置代码区值
 * @param id
 */


function setScript(id) {

    var parameter = "id=" + id;
    var url = base_url + "/developCenter/find.do";
    var result = getDataByGet(url, parameter)


    var type = result['type'];
    if (type == '2') {
        codeMirror.setOption("mode", "text/x-hive");
    } else if (type == '1') {
        codeMirror.setOption("mode", "text/x-sh");
    }
    var script = result['content'];
    if (script == null || script == '') {
        script = '';
    }
    codeMirror.setValue(script);
}

