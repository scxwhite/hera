layui.use(['table', 'laytpl', 'form'], function () {


    let workManage = $('#workManage');
    workManage.parent().addClass('menu-open');
    workManage.parent().parent().addClass('menu-open');
    workManage.addClass('active');
    $('#sysManager').addClass('active');
    let table = layui.table, laytpl = layui.laytpl, form = layui.form;

    let hostGroupMap = [], hostGroups;

    let tableIns, formOpType = 1;
    $.ajax({
        url: base_url + '/hostGroup/list',
        type: 'get',
        async: false,
        success: function (data) {
            hostGroups = data.data;
            $.each(hostGroups, function (index, group) {
                hostGroupMap[group.id] = group.name;
            })
        }
    });


    tableIns = table.render({
        elem: '#workList'
        , height: "full"
        , url: base_url + '/workManage/list'
        , page: true //开启分页
        , toolbar: '#toolbar'
        , defaultToolbar: ['filter', 'print', 'exports']
        , cols: [[ //表头
            {field: 'id', title: '序号', fixed: 'left', align: 'center'}
            , {field: 'host', title: 'ip地址', align: 'center'}
            , {
                field: 'hostGroupId', title: '机器组', align: 'center', sort: true, templet: function (data) {
                    if (hostGroupMap.hasOwnProperty(data.hostGroupId)) {
                        return hostGroupMap[data.hostGroupId];
                    }
                    return data.hostGroupId;
                }
            }
            , {fixed: 'right', title: '操作', align: 'center', toolbar: '#barOption'}
        ]]
    });
    //TODO  无效 需check
    form.verify({
        ip: [
            /^(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[1-9])(\.(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)){3}$/
            , '密码必须6到12位，且不能出现空格'
        ]
    });

    table.on('tool(workList)', function (obj) {
        switch (obj.event) {
            case 'del':
                layer.confirm(hostGroupMap[obj.data.hostGroupId] + '机器组下的：' + obj.data.host, {
                    icon: 3,
                    title: '确认删除？'
                }, function (index) {
                    $.post(base_url + "/workManage/del", {id: obj.data.id}, function (data) {
                        layer.msg(data.message);
                        tableIns.reload();
                    });
                    layer.close(index);
                });
                break;
            case 'edit':
                formOpType = 2;
                let layHtml = "初始化";
                laytpl($('#addHostGroup')[0].innerHTML).render(hostGroups, function (html) {
                    layHtml = html;
                });
                showForm(layHtml, function (index, layero) {
                    form.render();
                    formDataLoad('addHostGroupForm', obj.data);
                });
                break;
        }
    });

    table.on('toolbar(workList)', function (obj) {
        switch (obj.event) {
            case 'add':
                formOpType = 1;
                let layHtml = "初始化";
                laytpl($('#addHostGroup')[0].innerHTML).render(hostGroups, function (html) {
                    layHtml = html;
                });
                showForm(layHtml, function (index, layero) {
                    form.render();
                });
                break;
        }
    });

    function showForm(layHtml, callback) {
        layer.open({
            type: 1,
            skin: 'layui-layer-rim', //加上边框
            area: ['650px', '500px'], //宽高
            content: layHtml,  //调到新增页面
            btn: ["确定", "取消"],
            success: callback,
            yes: function (index, layero) {

                if (formOpType === 1) {
                    $.ajax({
                        url: base_url + "/workManage/add",
                        data: $('#addHostGroupForm').serialize(),
                        type: "post",
                        success: function (data) {
                            layer.msg(data.message);
                            layer.close(index);
                            tableIns.reload();
                        }
                    })
                } else if (formOpType === 2) {
                    $.ajax({
                        url: base_url + "/workManage/update",
                        data: $('#addHostGroupForm').serialize(),
                        type: "post",
                        success: function (data) {
                            layer.msg(data.message);
                            layer.close(index);
                            tableIns.reload();
                        }
                    })
                }
            },
            btn2: function (index, layero) {
                layer.close(index);
            }
        });
    }

});