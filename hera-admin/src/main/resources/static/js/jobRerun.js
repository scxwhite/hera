layui.use(['element', 'table', 'laytpl', 'form', 'laydate'], function () {


    $('#rerun').addClass('active');
    $('#rerun').parent().addClass('menu-open');
    $('#rerun').parent().parent().addClass('menu-open');
    $('#jobManage').addClass('active');


    let table = layui.table, laytpl = layui.laytpl, form = layui.form, laydate = layui.laydate, element = layui.element;


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

    function test() {
        console.log(11)
    }

    tableIns = table.render({
        elem: '#rerunList'
        , height: "full"
        , url: base_url + '/rerun/list'
        , page: true //开启分页
        , toolbar: '#toolbar'
        , defaultToolbar: ['filter', 'print', 'exports']
        , cols: [[ //表头
            {type: 'numbers', title: '序号', fixed: 'left', align: 'center', width: 60}
            , {field: 'name', title: '重跑名称', align: 'center', width: 120}
            , {field: 'jobId', title: '任务ID', align: 'center', width: 80}
            , {field: 'startTime', title: '起始日期', align: 'center', width: 180}
            , {field: 'endTime', title: '结束日期', align: 'center', width: 180}
            , {field: 'gmtCreate', title: '创建时间', align: 'center', width: 180}
            , {field: 'ssoName', title: '创建人', align: 'center', width: 80}
            , {field: 'actionNow', title: '当前执行版本', align: 'center', width: 180}
            , {
                title: '并行度', align: 'center', width: 80, templet: function (data) {
                    return data.threads;
                }
            }
            , {
                title: '进度', align: 'center', width: 280, templet: function (data) {
                    var success = data.extra['action_process_num'], total = data.extra['action_all_num'];
                    if (success === undefined || total === undefined) {
                        return '<div class="layui-progress layui-progress-big" lay-showPercent="yes" >\n' +
                            '  <div class="layui-progress-bar" lay-percent="0%"></div>\n' +
                            '</div>';
                    }
                    return '<div class="layui-progress layui-progress-big" lay-showPercent="yes" >\n' +
                        '  <div class="layui-progress-bar" lay-percent="' + success + '/' + total + '"></div>\n' +
                        '</div>';
                }
            }
            , {
                field: 'status', fixed: 'right', align: 'center', title: '状态', width: 100, templet: function (data) {
                    if (data.isEnd) {
                        return '<div><input type="checkbox" index="' + data.id + '" lay-skin="switch" lay-text="开启|结束" lay-filter="isEnd" ></div>';
                    }
                    return '<div ><input type="checkbox" index="' + data.id + '" lay-skin="switch" lay-text="开启|结束"  lay-filter="isEnd" checked></div>';
                }
            }
        ]],
        done: function () {
            $('.layui-progress').parent().removeClass('layui-table-cell');
            element.render();
        },
        parseData: function (res) {
            res.data.forEach(function (row) {
                row.threads = row.extra['rerun_thread']
            });
            return res

        }
    });
    form.on('switch(isEnd)', function (data) {
        var id = $(data.elem).attr("index");
        var end = data.elem.checked ? 0 : 1;
        $.ajax({
            url: base_url + "/rerun/status",
            data: {
                "id": id,
                "isEnd": end
            },
            type: "put",
            success: function (data) {
                layer.msg(data.message);
                if (!data.success) {
                    tableIns.reload();
                }
            }
        })


    });


    table.on('toolbar(rerunList)', function (obj) {
        switch (obj.event) {
            case 'add':
                formOpType = 1;
                let layHtml = "初始化";
                laytpl($('#addRerun')[0].innerHTML).render(hostGroups, function (html) {
                    layHtml = html;
                });
                showForm(layHtml, function (index, layero) {
                    form.render();
                    laydate.render({
                        elem: '#startTime', //指定元素
                        type: 'datetime',
                        max: 0
                    });

                    laydate.render({
                        elem: '#endTime', //指定元素
                        type: 'datetime',
                        max: 1

                    });
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
                        url: base_url + "/rerun/add",
                        data: $('#addRerunForm').serialize(),
                        type: "post",
                        success: function (data) {
                            layer.msg(data.message);
                            if (data.success === true) {
                                layer.close(index);
                                tableIns.reload();
                            }

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
