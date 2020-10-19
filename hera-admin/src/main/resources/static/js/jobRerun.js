layui.use(['element', 'table', 'laytpl', 'form', 'laydate'], function () {


    $('#rerun').addClass('active');
    $('#rerun').parent().addClass('menu-open');
    $('#rerun').parent().parent().addClass('menu-open');
    $('#jobManage').addClass('active');


    let table = layui.table, laytpl = layui.laytpl, form = layui.form, laydate = layui.laydate, element = layui.element;


    let hostGroups = [{
        id: 0
    }
    ];

    let tableIns, formOpType = 1, cacheData, status = -1;


    function test() {
        console.log(11)
    }

    tableIns = table.render({
        elem: '#rerunList'
        , height: "full"
        , url: base_url + '/rerun/list'
        , where: {
            status: status
        }
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
                field: 'status', fixed: 'right', align: 'center', title: '状态', width: 200, templet: function (data) {
                    if (data.isEnd) {
                        var res = '<input type="checkbox" index="' + data.id + '" lay-skin="switch" lay-text="开启|结束" lay-filter="isEnd" >';
                        if (data.extra['action_failed_num'] !== 0 && data.extra['action_failed_num'] !== undefined) {
                            res += '<button type="button" class="layui-btn layui-btn-xs layui-btn-radius layui-btn-danger"  lay-submit lay-filter="failedBtn"  lay-event="failedBtn" index="' + data.id + '" >失败记录</button>';
                        }
                        return res;
                    }
                    return '<div ><input type="checkbox" index="' + data.id + '" lay-skin="switch" lay-text="开启|结束"  lay-filter="isEnd" checked></div>';
                }
            }
        ]],
        done: function () {

            var q1 = document.getElementById("endSelect");
            for (var i = 0; i < q1.options.length; i++) {
                if (q1.options[i].value === status) {
                    q1.options[i].selected = true;
                }
            }
            form.render();


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
    form.on('submit(failedBtn)', function (data) {
        layer.open({
            skin: 'layui-layer-rim', //加上边框
            area: ['800px', '500px'], //宽高
            btn: ["重跑所有", "取消"],
            content: ' <table id="failedTable"></table>', //这里content是一个普通的String
            yes: function (index, layero) {
                $.ajax({
                    type: 'post',
                    url: base_url + '/rerun/failed',
                    data:{rerunId:$(data.elem).attr("index")},

                    success: function (data) {
                        layer.msg(data.message)
                        tableIns.reload();
                    }
                })
            },
            success: function (layero, index) {
                //第一个实例
                table.render({
                    elem: '#failedTable'
                    , height: 400,
                    where: {
                        rerunId: $(data.elem).attr("index")
                    }
                    , url: base_url + '/rerun/failed'
                    , page: true //开启分页
                    , cols: [[ //表头
                        {field: 'actionId', title: '版本号', width: 180, sort: true, fixed: 'left'}
                        , {field: 'name', title: '任务名称', width: 100}
                        , {field: 'startTime', title: '开始时间', width: 180, sort: true}
                        , {field: 'endTime', title: '结束时间', width: 180}
                        , {
                            field: 'status', title: '状态', width: 80, templet: function (data) {
                                return '<button class="layui-btn layui-btn-xs layui-btn-radius layui-btn-danger">' + data.status + '</button>'
                            }
                        }
                    ]]
                });
            }
        });


        return false; //阻止表单跳转。如果需要表单跳转，去掉这段即可。
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
    form.on('select(endSelect)', function (data) {
        status = data.value;
        tableIns.reload({
            where: {
                status: data.value
            }
        });
    });


    table.on('toolbar(rerunList)', function (obj) {
        switch (obj.event) {
            case 'add':
                formOpType = 1;
                add();
                break;
            case 'refresh':
                tableIns.reload();
                break;
        }
    });

    function add() {
        showForm(buildHtml(), function (layero, index) {
            updateLayerEvent(layero);
        });
    }

    function updateLayerEvent(layero) {
        form.render();

        formDataLoad('addRerunForm', cacheData)

        for (var i = 0; i < hostGroups.length; i++) {
            laydate.render({
                elem: '#startTime_' + i, //指定元素
                type: 'datetime',
                max: 0,
                trigger: 'click',
                done: function (val, date, endDate) {

                }
            });

            laydate.render({
                elem: '#endTime_' + i, //指定元素
                type: 'datetime',
                max: 1,
                trigger: 'click',
                done: function (val, date, endDate) {

                }

            });
        }
        $('#addDate').on('click', function () {
            if (hostGroups.length >= 30) {
                layer.msg("一次最多添加30个重跑日期范围")
                return;
            }

            hostGroups.push({id: 1});

            cacheData = serializeToObject($('#addRerunForm').serializeArray());


            layero.find('form').html(buildHtml());
            updateLayerEvent(layero);
        });

        $('#subDate').on('click', function () {
            hostGroups.pop();
            cacheData = serializeToObject($('#addRerunForm').serializeArray());

            layero.find('form').html(buildHtml());
            updateLayerEvent(layero);

        })


    }


    function serializeToObject(jsonArray) {
        var o = {};
        $.each(jsonArray, function () {
            if (o[this.name] !== undefined) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                o[this.name] = this.value || '';
            }
        });
        return o;
    }


    function buildHtml() {
        let layHtml = "初始化";
        laytpl($('#addRerun')[0].innerHTML).render(hostGroups, function (html) {
            layHtml = html;
        });
        return layHtml;
    }

    function showForm(layHtml, callback) {
        layer.open({
            type: 1,
            skin: 'layui-layer-rim', //加上边框
            area: ['650px', '500px'], //宽高
            content: layHtml,  //调到新增页面
            btn: ["确定", "取消"],
            success: callback,
            shadeClose: true,
            yes: function (index, layero) {
                if (formOpType === 1) {

                    var data = serializeToObject($('#addRerunForm').serializeArray());

                    if (data['jobId'] == null) {
                        layer.msg("jobId不允许为空");
                        return;
                    }
                    $.ajax({
                        url: base_url + "/rerun/add",
                        data: "jobId=" + data['jobId'] + "&rerunJson=" + JSON.stringify(data),
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
