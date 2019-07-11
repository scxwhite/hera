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

    $('#userManage').addClass('active');
    $('#userManage').parent().addClass('menu-open');
    $('#userManage').parent().parent().addClass('menu-open');
    $('#sysManager').addClass('active');
    userTable = table.render({
        elem: '#userTable'
        , height: "full"
        , url: base_url + '/userManage/initUser'
        , page: false //开启分页
        , toolbar: '#toolbar'
        , defaultToolbar: ['filter', 'print', 'exports']
        , cols: [[ //表头
            {title: '序号', fixed: 'left', align: 'center', type: 'numbers'}
            , {field: 'name', title: '用户', align: 'center', width: 100}
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
        }
    }

    function refreshTable() {
        if (select === 0) {
            userTable.reload({
                page: {
                    curr: 1
                }
            });
        } else if (select === 1) {
            ssoTable.reload({
                page: {
                    curr: 1
                }
            });
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

