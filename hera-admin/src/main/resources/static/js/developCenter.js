$(function(){
    setInterval(log,1000);
    var setting = {
        edit: {
            enable: true,
            showRemoveBtn: true,
            showRenameBtn: true,
            removeTitle: "删除",
            renameTitle: "重命名"
        },
        view: {
            showLine: false
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        callback: {
            onRightClick: OnRightClick,
        }
    };

    var zNodes =[
        { name:"文档中心", open:true,
            children: [
                { name:"个人文档",
                    children: [
                        { name:"文档111"},
                        { name:"叶子节点112"},
                        { name:"叶子节点113"},
                        { name:"叶子节点114"}
                    ]},
                { name:"共享文档",
                    children: [
                        { name:"叶子节点121"},
                        { name:"叶子节点122"},
                        { name:"叶子节点123"},
                        { name:"叶子节点124"}
                    ]}
            ]}
    ];

    // debugger
    // var zNodes = getDataStore("/developCenter/init.do");
    // zNodes = JSON.stringify(zNodes);

    function getDataStore(url) {
        var dataStore;
        $.ajax({
            dataType : 'json',
            type : 'get',
            url : url,
            async : false,
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
    debugger
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

        rMenu.css({"top":y+"px", "left":x+"px", "visibility":"visible"});

        $("body").bind("mousedown", onBodyMouseDown);
    }

    function hideRMenu() {
        if (rMenu) rMenu.css({"visibility": "hidden"});
        $("body").unbind("mousedown", onBodyMouseDown);
    }
    function onBodyMouseDown(event){
        if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
            rMenu.css({"visibility" : "hidden"});
        }
    }
    var addCount = 1;

    function addFolder() {
        hideRMenu();
        var newNode = { name:"增加文件夹" + (addCount++)};
        if (zTree.getSelectedNodes()[0]) {
            newNode.checked = zTree.getSelectedNodes()[0].checked;
            zTree.addNodes(zTree.getSelectedNodes()[0], newNode);
        } else {
            zTree.addNodes(null, newNode);
        }
    }


    function addHiveFile() {
        hideRMenu();
        var newNode = { name:"新建Hive脚本" + (addCount++)};
        if (zTree.getSelectedNodes()[0]) {
            newNode.checked = zTree.getSelectedNodes()[0].checked;
            zTree.addNodes(zTree.getSelectedNodes()[0], newNode);
        } else {
            zTree.addNodes(null, newNode);
        }
    }

    function addShellFile() {
        hideRMenu();
        var newNode = { name:"新建Shell脚本" + (addCount++)};
        if (zTree.getSelectedNodes()[0]) {
            newNode.checked = zTree.getSelectedNodes()[0].checked;
            zTree.addNodes(zTree.getSelectedNodes()[0], newNode);
        } else {
            zTree.addNodes(null, newNode);
        }
    }

    function rename() {
        hideRMenu();
        var newNode = { name:"重命名" + (addCount++)};
        if (zTree.getSelectedNodes()[0]) {
            newNode.checked = zTree.getSelectedNodes()[0].checked;
            zTree.addNodes(zTree.getSelectedNodes()[0], newNode);
        } else {
            zTree.addNodes(null, newNode);
        }
    }

    function openFile() {
        hideRMenu();
        var newNode = { name:"打开" + (addCount++)};
        if (zTree.getSelectedNodes()[0]) {
            newNode.checked = zTree.getSelectedNodes()[0].checked;
            zTree.addNodes(zTree.getSelectedNodes()[0], newNode);
        } else {
            zTree.addNodes(null, newNode);
        }
    }

    function removeFile() {
        hideRMenu();
        var nodes = zTree.getSelectedNodes();
        if (nodes && nodes.length>0) {
            if (nodes[0].children && nodes[0].children.length > 0) {
                var msg = "要删除的节点是父节点，如果删除将连同子节点一起删掉。\n\n请确认！";
                if (confirm(msg)==true){
                    zTree.removeNode(nodes[0]);
                }
            } else {
                zTree.removeNode(nodes[0]);
            }
        }
    }

    function copyFile(checked) {
        hideRMenu();
        var newNode = { name:"重命名" + (addCount++)};
        if (zTree.getSelectedNodes()[0]) {
            newNode.checked = zTree.getSelectedNodes()[0].checked;
            zTree.addNodes(zTree.getSelectedNodes()[0], newNode);
        } else {
            zTree.addNodes(null, newNode);
        }
    }

    function resetTree() {
        hideRMenu();
        $.fn.zTree.init($("#treeDemo"), setting, zNodes);
    }


    var zTree, rMenu;
    $(document).ready(function(){
        $.fn.zTree.init($("#documentTree"), setting, zNodes);
        zTree = $.fn.zTree.getZTreeObj("documentTree");
        rMenu = $("#rMenu");

    });

    function log() {
        console.log("log.......")
    }

});

