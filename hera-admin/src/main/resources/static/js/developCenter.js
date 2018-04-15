$(function(){
    setInterval(log,1000);
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
        }
    };
    debugger

    var zNodes = getDataStore("/developCenter/init.do");
    console.log(zNodes);

    function getDataStore(url) {
        var dataStore;
        $.ajax({
            type: "post",
            url: url,
            async: false,
            success : function(data) {
                dataStore = data;
            }
        });
        return dataStore;
    }



    function OnRightClick(event, treeId, treeNode) {
        if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
            zTree.cancelSelectedNode();
            showRMenu("root", event.clientX, event.clientY);
        } else if (treeNode && !treeNode.noR) {
            zTree.selectNode(treeNode);
            showRMenu("node", event.clientX, event.clientY);
        }
    }
    function showRMenu(type, x, y) {
        $("#rMenu ul").show();
        if (type=="root") {
            $("#removeFile").hide();
        } else {
            $("#addFolder").show();
            $("#addHiveFile").show();
            $("#addShellFile").show();
            $("#rename").show();
            $("#openFile").show();
            $("#removeFile").show();
            $("#copyFile").show();
            $("#resetTree").show();
        }

        y += document.body.scrollTop;
        x += document.body.scrollLeft;

        rMenu.css({"top":y+"px", "left":x+"px", "visibility":"visible",position: "absolute"});

        $("body").bind("mousedown", onBodyMouseDown);
    }

    function hideRMenu() {
        // if (rMenu) rMenu.css({"visibility": "hidden"});
        $("body").unbind("mousedown", onBodyMouseDown);
    }
    function onBodyMouseDown(event){
        if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
            rMenu.css({"visibility" : "hidden"});
        }
    }
    var addCount = 1;

    $("#addFolder").click(function() {
        hideRMenu();
        var selected = zTree.getSelectedNodes()[0];
        var id = selected['id'];
        var parent = selected['parent'];
        var name = "文件夹" +  addCount;

        var newNode = { name: "文件夹" +  addCount, isParent:true};
        addCount ++;

        var parameter = "parent=" + parent + "&type=" + "1" + "&name=" + name ;

        $.ajax({
            url:"/developCenter/addFile.do",
            type: "get",
            async:false,
            data: parameter,
            success : function(data) {
                alert(data);
            }
        });

        if (zTree.getSelectedNodes()[0]) {
            newNode.checked = zTree.getSelectedNodes()[0].checked;
            zTree.addNodes(zTree.getSelectedNodes()[0].getParentNode(), newNode);
        }

        fixIcon();//调用修复图标的方法。方法如下：

    });


    $("#addHiveFile").click(function() {
        hideRMenu();
        var selected = zTree.getSelectedNodes()[0];
        var id = selected['id'];
        var parent = selected['parent'];
        var name = selected['name'];

        var newNode = { name: +  addCount + name, isParent:true};
        addCount ++;

        var parameter = "parent=" + parent + "&type=" + "2" + "&name=" + "copy_" +  name ;

        $.ajax({
            url:"/developCenter/addFile.do",
            type: "get",
            async:false,
            data: parameter,
            success : function(data) {
                alert(data);
            }
        });

        if (zTree.getSelectedNodes()[0]) {
            newNode.checked = zTree.getSelectedNodes()[0].checked;
            zTree.addNodes(zTree.getSelectedNodes()[0].getParentNode(), newNode);
        }

        fixIcon();//调用修复图标的方法。方法如下：

    });

    $("#addShellFile").click(function() {
        hideRMenu();
        var selected = zTree.getSelectedNodes()[0];
        var id = selected['id'];
        var parent = selected['parent'];
        var name = selected['name'];

        var newNode = { name: +  addCount + name, isParent:true};
        addCount ++;

        var parameter = "parent=" + parent + "&type=" + "2" + "&name=" + "copy_" + name ;

        $.ajax({
            url:"/developCenter/addFile.do",
            type: "get",
            async:false,
            data: parameter,
            success : function(data) {
                alert(data);
            }
        });

        if (zTree.getSelectedNodes()[0]) {
            newNode.checked = zTree.getSelectedNodes()[0].checked;
            zTree.addNodes(zTree.getSelectedNodes()[0].getParentNode(), newNode);
        }

        fixIcon();//调用修复图标的方法。方法如下：

    });

    $("#rename").click(function() {
        hideRMenu();
        var selected = zTree.getSelectedNodes()[0];
        var id = selected['id'];
        var parent = selected['parent'];

    });


    $("#openFile").click(function() {
        hideRMenu();
        var selected = zTree.getSelectedNodes()[0];
        var id = selected['id'];
        var parent = selected['parent'];

    });

    $("#removeFile").click(function() {
        // hideRMenu();
        var selected = zTree.getSelectedNodes()[0];
        var id = selected['id'];

        var parameter = "id=" + id ;

        $.ajax({
            url:"/developCenter/delete.do",
            type: "get",
            async:false,
            data: parameter,
            success : function(data) {
                alert(data);
            }
        });

        if (zTree.getSelectedNodes()[0]) {
            newNode.checked = zTree.getSelectedNodes()[0].checked;
            zTree.addNodes(zTree.getSelectedNodes()[0].getParentNode(), newNode);
        }

        fixIcon();//调用修复图标的方法。方法如下：

    });

    $("#copyFile").click(function() {
        hideRMenu();
        var selected = zTree.getSelectedNodes()[0];
        var id = selected['id'];
        var parent = selected['parent'];

    });



    function resetTree() {
        hideRMenu();
        $.fn.zTree.init($("#treeDemo"), setting, zNodes);
    }


    var zTree, rMenu;
    $(document).ready(function(){
        $.fn.zTree.init($("#documentTree"), setting, zNodes);
        zTree = $.fn.zTree.getZTreeObj("documentTree");
        rMenu = $("#rMenu");
        fixIcon();//调用修复图标的方法。方法如下：

    });





//修正zTree的图标，让文件节点显示文件夹图标
    function fixIcon(){
        $.fn.zTree.init($("#documentTree"), setting, zNodes);
        var treeObj = $.fn.zTree.getZTreeObj("documentTree");
        //过滤出sou属性为true的节点（也可用你自己定义的其他字段来区分，这里通过sou保存的true或false来区分）
        var folderNode = treeObj.getNodesByFilter(function (node) { return node.isParent});
        for(var j = 0 ; j<folderNode.length; j++){//遍历目录节点，设置isParent属性为true;
            folderNode[j].isParent = true;
        }
        treeObj.refresh();//调用api自带的refresh函数。
    }

    function log() {
        console.log("log.......")
    }

});

