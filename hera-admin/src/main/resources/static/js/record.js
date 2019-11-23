layui.use(['table'], function () {

    var curPage = $('#record');
    curPage.addClass('active');
    curPage.parent().addClass('menu-open');
    curPage.parent().parent().addClass('menu-open');
    $('#jobManage').addClass('active');

    let table = layui.table;
    var formSelects = layui.formSelects;


    let tableIns, formOpType = 1;
    tableIns = table.render({
        elem: '#recordTable'
        , height: "full"
        , url: base_url + '/record/list'
        , page: true //开启分页
        , toolbar: '#toolbar'
        , defaultToolbar: ['filter', 'print', 'exports']
        , cols: [[ //表头
            {type: 'numbers'}
            , {
                field: 'logId', title: '操作ID', align: 'center', sort: true, templet: function (data) {
                    if (data.logType !== 'job') {
                        return data.logId;
                    }
                    return '<a href="javascript:toJobPage(' + data.logId + ')" ><u>' + data.logId + '</u></a>';
                }
            }
            , {field: 'type', title: '类型', align: 'center', sort: true}
            , {
                field: 'logType', title: '日志类型', align: 'center', sort: true, templet: function (data) {
                    var defaultClass = "layui-btn-xs layui-btn";
                    if (data.logType === 'job') {
                        defaultClass = "layui-btn-xs layui-btn layui-btn-normal";
                    } else if (data.logType === 'group') {
                        defaultClass = "layui-btn-xs layui-btn layui-btn-warm";
                    } else if (data.logType === 'debug') {
                        defaultClass = "layui-btn-xs layui-btn layui-btn-primary";
                    }
                    return '<a href="#" class="' + defaultClass + '">' + data.logType + '</a>';
                }
            }
            , {field: 'sso', title: '操作人', align: 'center', sort: true}
            , {field: 'gname', title: '所在组', align: 'center', sort: true}
            , {field: 'createTime', title: '操作时间', align: 'center', sort: true}
            , {fixed: 'right', title: '详细', align: 'center', toolbar: '#barOption'}
        ]]
    });

    table.on('tool(recordTable)', function (obj) {
        switch (obj.event) {
            case 'detail':
                showForm($('#content')[0].innerHTML, function (index, layero) {
                    var value = "", mode = "", highlight = true, connect = null, collapse = false;
                    $.ajax({
                        url: base_url + "/record/now",
                        data: {
                            logId: obj.data.logId,
                            logType: obj.data.logType,
                            type: obj.data.type
                        },
                        type: "get",
                        async: false,
                        success: function (res) {
                            if (res.success == false) {
                                layer.msg(res.message);
                            }
                            var data = res.data;
                            if (data.runType === "Shell") {
                                mode = "text/x-sh";
                            } else {
                                mode = "text/x-hive";
                            }
                            value = data.content;
                        }
                    });
                    var target = document.getElementById("view");
                    target.innerHTML = "";
                    CodeMirror.MergeView(target, {
                        value: value,
                        origLeft: obj.data.content,
                        orig: null,
                        theme: 'eclipse',
                        lineNumbers: true,
                        mode: mode,
                        matchBrackets: true,
                        styleActiveLine: true,
                        highlightDifferences: highlight,
                        connect: connect,
                        collapseIdentical: true,
                        revertButtons: true
                    });
                });
                break;
        }
    });


    table.on('toolbar(recordTable)', function (obj) {
        switch (obj.event) {
            case 'refresh':
                tableIns.reload();
                break;
        }
    });


    function showForm(layHtml, callback) {
        layer.open({
            type: 1,
            title: '[历史/最新]对比',
            shadeClose: true,
            offset: 'auto',
            skin: 'layui-layer-rim', //加上边框
            area: ['1400px', '650px'], //宽高
            content: layHtml,  //调到新增页面
            btn: ["关闭"],
            success: callback,
            yes: function (index, layero) {
                layer.close(index);
            }
        });
    }


})
;