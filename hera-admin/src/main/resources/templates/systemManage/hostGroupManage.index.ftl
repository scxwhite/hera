<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>机器组管理</title>
    <base href="${request.contextPath}" id="contextPath">
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
</head>


<body class="hold-transition skin-black sidebar-mini">

<div class="wrapper">
    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "hostgroup"/>
    <div class="content-wrapper">
        <div class="content">
            <div class="box">
                <div class="box-body">
                    <table id="hostGroupTable" lay-filter="hostGroupTable"/>
                </div>
            </div>

        </div>
    </div>
</div>
<script type="text/html" id="toolbar">
    <a class="layui-btn layui-btn-radius layui-btn-normal" lay-event="add">新增</a>
</script>
<script type="text/html" id="barOption">
    <a class="layui-btn layui-btn-xs" lay-event="edit">编辑</a>
    <a class="layui-btn layui-btn-xs layui-btn-danger" lay-event="del">删除</a>
</script>

<script type="text/html" id="addHostGroup">
    <form class="layui-form layui-form-pane" action="" id="addHostGroupForm">

        <div class="layui-form-item" style="display: none">
            <label class="layui-form-label">id</label>
            <div class="layui-input-block">
                <input type="text" name="id"
                       autocomplete="off"
                       lay-verify="required | ip"
                       class="layui-input">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">名称</label>
            <div class="layui-input-block">
                <input type="text" name="name"
                       autocomplete="off"
                       lay-verify="required | ip"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">状态</label>
            <div class="layui-input-block">
                <select name="effective" lay-verify="required">
                    {{# layui.each(d, function(index,item) { }}
                    <option value="{{item.id}}">{{item.name}}</option>
                    {{# }); }}
                </select>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">描述</label>
            <div class="layui-input-block">
                <input type="text" name="description"
                       autocomplete="off"
                       lay-verify="required | ip"
                       class="layui-input">
            </div>
        </div>
    </form>
</script>

<@netCommon.commonScript />
<script>

    layui.use(['table', 'laytpl', 'form'], function () {
        init();

        let table = layui.table, laytpl = layui.laytpl, form = layui.form;
        let hostGroupTable, formOpType;

        let option = [{
            id: 1,
            name: "有效"
        }, {
            id: 0,
            name: "无效"
        }
        ];

        hostGroupTable = table.render({
            elem: '#hostGroupTable'
            , height: "full"
            , url: base_url + '/hostGroup/list'
            , page: false //开启分页
            , toolbar: '#toolbar'
            , defaultToolbar: ['filter', 'print', 'exports']
            , cols: [[ //表头
                {title: '序号', fixed: 'left', align: 'center', type: 'numbers'}
                , {field: 'name', title: '名称', align: 'center'}
                , {
                    field: 'effective', title: '状态', align: 'center', templet: function (data) {
                        if (data.effective === 0) {
                            return '<button class="layui-btn layui-btn-xs layui-btn-primary">无效</button>'

                        } else if (data.effective === 1) {
                            return '<button class="layui-btn layui-btn-xs layui-btn-normal">有效</button>'

                        } else {
                            return data.effective;
                        }
                    }
                }
                , {field: 'description', title: '描述', align: 'center'}
                , {fixed: 'right', title: '操作', align: 'center', toolbar: '#barOption'}
            ]]
        });

        table.on('toolbar(hostGroupTable)', function (obj) {
            switch (obj.event) {
                case 'add':
                    formOpType = 1;
                    let layHtml = "初始化";
                    laytpl($('#addHostGroup')[0].innerHTML).render(option, function (html) {
                        layHtml = html;
                    });
                    showForm(layHtml, function (index, layero) {
                        form.render();
                    });
                    break;
            }
        });
        table.on('tool(hostGroupTable)', function (obj) {
            switch (obj.event) {
                case 'del':
                    layer.confirm('确认删除机器组：' + obj.data.name + '?', {
                        icon: 3,
                        title: '确认删除？'
                    }, function (index) {
                        $.post(base_url + "/hostGroup/del", {id: obj.data.id}, function (data) {
                            layer.msg(data.message);
                            refreshTable();
                        });
                        layer.close(index);
                    });
                    break;
                case 'edit' :
                    formOpType = 2;
                    let layHtml = "初始化";
                    laytpl($('#addHostGroup')[0].innerHTML).render(option, function (html) {
                        layHtml = html;
                    });
                    showForm(layHtml, function (index, layero) {
                        form.render();
                        formDataLoad('addHostGroupForm', obj.data);
                    });
                    break;

            }

        });

        function showForm(layHtml, callback) {
            layer.open({
                type: 1,
                skin: 'layui-layer-rim', //加上边框
                area: ['450px', '300px'], //宽高
                content: layHtml,  //调到新增页面
                btn: ["确定", "取消"],
                success: callback,
                yes: function (index, layero) {

                    if (formOpType === 1) {
                        $.ajax({
                            url: base_url + "/hostGroup/add",
                            data: $('#addHostGroupForm').serialize(),
                            type: "post",
                            success: function (data) {
                                layer.msg(data.message);
                                layer.close(index);
                                refreshTable();

                            }
                        })
                    } else if (formOpType === 2) {
                        $.ajax({
                            url: base_url + "/hostGroup/update",
                            data: $('#addHostGroupForm').serialize(),
                            type: "post",
                            success: function (data) {
                                layer.msg(data.message);
                                layer.close(index);
                                refreshTable();
                            }
                        })
                    }
                },
                btn2: function (index, layero) {
                    layer.close(index);
                }
            });
        }

        function refreshTable() {
            hostGroupTable.reload({
                page: {
                    curr: 1
                }
            });
        }

        function init() {
            let groupManager = $('#hostGroupManage');
            groupManager.addClass('active');
            groupManager.parent().addClass('menu-open');
            groupManager.parent().parent().addClass('menu-open');
            $('#sysManager').addClass('active');
        }

    });


    function edit(id) {
        $('#myModalLabel').text("编辑机器组");
        form[0].reset();
        formDataLoad("hostGroupTable", groupCache[id]);
        $('#myModal').modal('show');
    }

    function del(id) {
        if (confirm("确认删除 " + groupCache[id].name + "?")) {
            $.ajax({
                url: contextPath + '/hostGroup/del',
                type: "post",
                data: {
                    id: id
                },
                success: function (data) {
                    alert(data.message);
                    $('#selectTable').bootstrapTable('refresh');
                }
            })
        }
    }

    function initEvent() {
        form.validate();
        $('#addHostGroup').on('click', function () {
            $('#myModalLabel').text("添加机器组");
            form[0].reset();
            $('#myModal').modal('show')
        });

        $('#submitFormat').on('click', function () {
            $.ajax({
                url: contextPath + '/hostGroup/saveOrUpdate',
                type: "post",
                data: form.serialize(),
                success: function (data) {
                    alert(data.msg);
                    $('#myModal').modal('hide')
                    $('#selectTable').bootstrapTable('refresh');

                }
            })
        });
    }

</script>
</body>

</html>


