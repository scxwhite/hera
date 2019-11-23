/**
 * 用户管理页面，用户注册之后的权限审核
 *
 * @type {any[]}
 */
layui.use(['table', 'laytpl', 'form', 'element'], function () {

    var userTable, ssoTable;
    var select = 0;
    var hasLoad = false;
    let table = layui.table, laytpl = layui.laytpl, form = layui.form;

    let hostGroups;

    $('#userManage').addClass('active');
    $('#userManage').parent().addClass('menu-open');
    $('#userManage').parent().parent().addClass('menu-open');
    $('#sysManager').addClass('active');
    userTable = table.render({
        elem: '#userTable'
        , height: "full"
        , url: base_url + '/userManage/initUser.do'
        , page: false //开启分页
        , toolbar: '#toolbar'
        , defaultToolbar: ['filter', 'print', 'exports']
        , cols: [[ //表头
            {title: '序号', fixed: 'left', align: 'center', type: 'numbers'}
            , {field: 'name', title: '组名称', align: 'center', width: 100}
            , {field: 'email', title: '邮箱', align: 'center', width: 180}
            , {field: 'phone', title: '手机号', align: 'center', width: 130}
            , {
                field: 'isEffective', title: '审核状态', align: 'center', width: 90, templet: function (data) {
                    if (data.isEffective === 1) {
                        return "<button class=\"layui-btn layui-btn-xs\"> 通过 </button>";
                    }
                    return "<button class=\"layui-btn  layui-btn-warm layui-btn-xs\"> 未通过 </button>";
                }
            }
            , {field: 'createTime', title: '申请时间', align: 'center', width: 170}
            , {field: 'opTime', title: '更新时间', align: 'center', width: 170}
            , {fixed: 'right', title: '操作', align: 'center', toolbar: '#barOption'}
        ]]
    });

    $(document).on('click', '#adminTab', function (data) {
        select = 0;
    });

    $(document).on('click', '#ssoTab', function (data) {
        select = 1;
        if (!hasLoad) {
            hasLoad = true;
            ssoTable = table.render({
                elem: '#ssoTable'
                , height: "full"
                , url: base_url + '/userManage/initSso.do'
                , page: false //开启分页
                , toolbar: '#toolbar'
                , defaultToolbar: ['filter', 'print', 'exports']
                , cols: [[ //表头
                    {title: '序号', fixed: 'left', align: 'center', type: 'numbers'}
                    , {field: 'name', title: '用户', align: 'center', width: 100}
                    , {field: 'gname', title: '所在组', align: 'center', width: 100}
                    , {field: 'jobNumber', title: '工号', align: 'center', width: 115}
                    , {field: 'email', title: '邮箱', align: 'center', width: 180}
                    , {field: 'phone', title: '手机号', align: 'center', width: 130}
                    , {
                        field: 'isValid', title: '审核状态', align: 'center', width: 90, templet: function (data) {
                            if (data.isValid === 1) {
                                return "<button class=\"layui-btn layui-btn-xs\"> 通过 </button>";
                            }
                            return "<button class=\"layui-btn  layui-btn-warm layui-btn-xs\"> 未通过 </button>";
                        }
                    }
                    , {fixed: 'right', title: '操作', align: 'center', toolbar: '#barOption'}
                ]]
            });
        }
    });


    table.on('tool(userTable)', function (obj) {
        doTools(obj)
    });
    table.on('tool(ssoTable)', function (obj) {
        doTools(obj)
    });

    function doTools(obj) {
        switch (obj.event) {
            case 'del':
                layer.confirm('确认删除用户：' + obj.data.name + '?', {
                    icon: 3,
                    title: '确认删除？'
                }, function (index) {
                    $.post(base_url + "/userManage/operateUser", {
                        id: obj.data.id,
                        operateType: 1,
                        userType: select
                    }, function (data) {
                        layer.msg(data.message);
                        refreshTable();
                    });
                    layer.close(index);
                });
                break;
            case 'approve' :
                $.post(base_url + "/userManage/operateUser", {
                    id: obj.data.id,
                    operateType: 2,
                    userType: select
                }, function (data) {
                    layer.msg(data.message);
                    refreshTable();
                });
                break;

            case 'refuse':
                $.post(base_url + "/userManage/operateUser", {
                    id: obj.data.id,
                    operateType: 3,
                    userType: select
                }, function (data) {
                    layer.msg(data.message);
                    refreshTable();
                });
                break;
            case 'edit':
                if (select === 1) {
                    let layHtml = "初始化";
                    if (hostGroups == null) {
                        $.ajax({
                            url: base_url + "/userManage/groups",
                            type: "get",
                            async: false,
                            success: function (data) {
                                if (data.success === false) {
                                    layer.msg("查询用户组失败");
                                    return;
                                }
                                hostGroups = data.data;
                            }

                        });
                    }
                    laytpl($('#editSso')[0].innerHTML).render(hostGroups, function (html) {
                        layHtml = html;
                    });
                    showForm(layHtml, function (index, layero) {
                        form.render();
                        formDataLoad('editSsoForm', obj.data);
                    });
                } else {
                    showForm($('#editUser')[0].innerHTML, function (index, layero) {
                        form.render();
                        formDataLoad('editUserForm', obj.data);
                    });
                }

                break;
        }
    }

    function showForm(layHtml, callback) {
        layer.open({
            type: 1,
            skin: 'layui-layer-rim', //加上边框
            area: ['650px', '500px'], //宽高
            content: layHtml,  //调到新增页面
            btn: ["确定", "取消"],
            success: callback,
            yes: function (index, layero) {
                if (select === 1) {
                    $.ajax({
                        url: base_url + "/userManage/sso/update",
                        data: $('#editSsoForm').serialize(),
                        type: "post",
                        success: function (data) {
                            layer.msg(data.message);
                            layer.close(index);
                            refreshTable();
                        }
                    })
                } else {
                    $.ajax({
                        url: base_url + "/userManage/user/update",
                        data: $('#editUserForm').serialize(),
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
        if (select === 0) {
            userTable.reload();
        } else if (select === 1) {
            ssoTable.reload();
        }
    }

    table.on('toolbar(userTable)', function (obj) {
        switch (obj.event) {
            case 'refresh':
                refreshTable();
                break;
        }
    });

    table.on('toolbar(ssoTable)', function (obj) {
        switch (obj.event) {
            case 'refresh':
                refreshTable();
                break;
        }
    });

});

