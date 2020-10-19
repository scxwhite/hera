layui.use(['table', 'laytpl', 'form'], function () {


    let workManage = $('#jobMonitor');
    workManage.parent().addClass('menu-open');
    workManage.parent().parent().addClass('menu-open');
    workManage.addClass('active');
    $('#sysManager').addClass('active');
    let table = layui.table, laytpl = layui.laytpl, form = layui.form;
    var formSelects = layui.formSelects;

    var ssoList = null;

    let tableIns, formOpType = 1;
    tableIns = table.render({
        elem: '#monitorTable'
        , height: "full"
        , url: base_url + '/jobMonitor/list'
        , page: false //开启分页
        , toolbar: '#toolbar'
        , defaultToolbar: ['filter', 'print', 'exports']
        , cols: [[ //表头
            {field: 'id', title: '任务Id', fixed: 'left', align: 'center', sort: true}
            , {field: 'jobName', title: '任务名称', align: 'center', sort: true}
            , {field: 'description', title: '任务描述', align: 'center', sort: true}
            , {
                field: 'monitors', title: '监控人', align: 'center', sort: true, templet: function (data) {
                    var monitors = '';
                    data.monitors.forEach(function (val) {
                        if (monitors === '') {
                            monitors = val.email;
                        } else {
                            monitors = monitors + ';' + val.email;
                        }
                    });
                    return monitors;
                }
            }
            , {fixed: 'right', title: '操作', align: 'center', toolbar: '#barOption'}
        ]]
    });

    table.on('tool(monitorTable)', function (obj) {
        switch (obj.event) {
            case 'edit':
                formOpType = 2;
                initSso();
                let layHtml = "初始化";
                laytpl($('#addMonitorJob')[0].innerHTML).render(ssoList, function (html) {
                    layHtml = html;
                });
                showForm(layHtml, function (index, layero) {
                    form.render();
                    formSelects.render('monitors');
                    $('#addMonitorJobForm [name=jobId]').val(obj.data.id).attr("readonly", true);

                    formSelects.value('monitors', obj.data.userIds.split(","));
                });
                break;
        }
    });

    function initSso() {
        if (ssoList === null) {
            $.ajax({
                url: base_url + "/userManage/initSso",
                async: false,
                type: 'get',
                success: function (data) {
                    if (data.code !== 0) {
                        layer.msg("查询用户失败，请联系管理员");
                        return;
                    }
                    ssoList = data.data;
                }
            })
        }
    }

    table.on('toolbar(monitorTable)', function (obj) {
        switch (obj.event) {
            case 'add':
                formOpType = 1;
                initSso();
                let layHtml = "初始化";
                laytpl($('#addMonitorJob')[0].innerHTML).render(ssoList, function (html) {
                    layHtml = html;
                });
                showForm(layHtml, function (index, layero) {
                    form.render();
                    formSelects.render('monitors');
                    $('#addMonitorJobForm [name=jobId]').attr("readonly", false);
                    formSelects.value('monitors', [], true);
                });
                break;
            case 'refresh':
                tableIns.reload();
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
                        url: base_url + "/jobMonitor/add",
                        data: $('#addMonitorJobForm').serialize(),
                        type: "post",
                        success: function (data) {
                            layer.msg(data.message);
                            layer.close(index);
                            tableIns.reload();
                        }
                    })
                } else if (formOpType === 2) {
                    $.ajax({
                        url: base_url + "/jobMonitor/update",
                        data: $('#addMonitorJobForm').serialize(),
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