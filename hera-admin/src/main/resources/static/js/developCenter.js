$(function(){
    setInterval(log,1000);
    var setting = {
        edit: {
            enable: true,
            showRemoveBtn:true,
            showRenameBtn:true,
            removeTitle : "删除",
            renameTitle : "重命名"
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        callback: {
            beforeDrag: beforeDrag,
            onRemove:onRemove
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

    function beforeDrag(treeId, treeNodes) {
        alert(treeId)
        return false;
    }

    function onRemove(treeId, treeNodes) {
        alert(treeId)
    }

    $(document).ready(function(){
        $.fn.zTree.init($("#documentTree"), setting, zNodes);

    });

    function log() {
        console.log("log.......")
    }

});