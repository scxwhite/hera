$(function () {
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


    function leftClick() {

        var selected = zTree.getSelectedNodes()[0];
        var id = selected['id'];
        var parent = selected['parent'];
        var name = selected['name'];


        var parameter = "jobId=" + id ;
        var result = null;

        $.ajax({
            url:"/scheduleCenter/getJobMessage.do",
            type: "get",
            async:false,
            data: parameter,
            success : function(data) {
                result = data;
            }
        });
        var script = result['script'];


        $("#jobMessage").attr("style", "display:block");
        $("#config").attr("style", "display:block");
        $("#script").attr("style", "display:block");
        $("#resource").attr("style", "display:block");
        $("#inheritConfig").attr("style", "display:block");
        $("#jobOperate").attr("style", "display:block");

        $("#jobScript").val(script);
        $("#jobId").html(id);


        jQuery.ajax({
            url:"/scheduleCenter/getJobVersion.do",
            type:"get",
            async:false,
            data:parameter,
            success:function (data) {
                if (data.success == false) {
                    alert(data.message);
                    return ;
                }
                var jobVersion = "";

                debugger

                data.forEach(function (action, index) {
                    jobVersion += '<option value="'+action.id+'" >'+ action.id+'</option>';
                })
                $('#selectJobVersion').empty();
                $('#selectJobVersion').append(jobVersion);
                $('#selectJobVersion').selectpicker('refresh');
            }
        })

    }


    $("#manual").click(function() {

        $('#myModal').modal('show');
    });


    $(".add-btn").click(function () {
        var actionId = $("#selectJobVersion").val();
        var parameter = "actionId=" + actionId ;

        $.ajax({
            url:"/scheduleCenter/manual.do",
            type: "get",
            async:false,
            data: parameter,
            success : function(data) {
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
            success : function(data) {
                dataStore = data;
            }
        });
        return dataStore;
    }

    //修正zTree的图标，让文件节点显示文件夹图标
    function fixIcon(){
        $.fn.zTree.init($("#jobTree"), setting, zNodes);
        var treeObj = $.fn.zTree.getZTreeObj("jobTree");
        //过滤出sou属性为true的节点（也可用你自己定义的其他字段来区分，这里通过sou保存的true或false来区分）
        var folderNode = treeObj.getNodesByFilter(function (node) { return node.isParent});
        for(var j = 0 ; j<folderNode.length; j++){//遍历目录节点，设置isParent属性为true;
            folderNode[j].isParent = true;
        }
        treeObj.refresh();//调用api自带的refresh函数。
    }


    var zTree, rMenu;
    $(document).ready(function(){
        $.fn.zTree.init($("#jobTree"), setting, zNodes);
        zTree = $.fn.zTree.getZTreeObj("jobTree");
        rMenu = $("#rMenu");
        fixIcon();//调用修复图标的方法。方法如下：

    });


});