layui.use(['element', 'layer'], function () {

    var page = $('#jobSearch');
    var table = $('#searchTable');
    page.addClass('active');
    page.parent().addClass('menu-open');
    page.parent().parent().addClass('menu-open');
    $('#jobManage').addClass('active');


    $(document).on('click', '#searchBtn', function (data) {
        $.ajax({
            url: base_url + "/job/search",
            data: $('#searchForm').serialize(),
            type: "get",
            success: function (data) {
                if (data.success === false) {
                    layer.msg(data.message);
                    return;
                }
                table.bootstrapTable('load', data.data);
            }
        })
    });


    var TableInit = function () {
        var oTableInit = new Object();
        oTableInit.init = function () {
            table.bootstrapTable({
                pagination: true,
                cache: false,
                clickToSelect: true,
                striped: false,
                showPaginationSwitch: false,  //是否显示选择分页数按钮
                pageNumber: 1,              //初始化加载第一页，默认第一页
                pageSize: 20,                //每页的记录行数（*）
                pageList: [40, 60, 80],
                sidePagination: "client",
                columns: [
                    {
                        field: 'id',
                        title: '任务ID',
                        halign: 'center',
                        align: 'center',
                        sortable: true,
                        formatter: function (id) {
                            return '<a href="javascript:toJobPage(' + id + ')">' + id + '</a>';
                        }
                    }, {
                        field: 'name',
                        title: '任务名称',
                        sortable: true,
                        halign: 'center',
                        align: 'center'
                    }, {
                        field: 'description',
                        halign: 'center',
                        align: 'center',
                        title: '任务描述'
                    }, {
                        field: 'config',
                        halign: 'center',
                        title: '任务配置',
                        formatter:function (val) {
                            return '<div class="form-group" style="max-height:200px;border:none; overflow-y:auto;">' + val +'</div>';
                        }
                    }, {
                        field: 'runType',
                        halign: 'center',
                        align: 'center',
                        title: '任务类型'
                    }, {
                        field: 'script',
                        halign: 'center',
                        title: '脚本内容',
                        formatter:function (val) {
                            return '<div class="form-group" style="max-height:200px;border:none; overflow-y:auto;">' + val +'</div>';
                        }
                    }
                ]
            });
        }
        return oTableInit;
    };
    var oTable = new TableInit();
    oTable.init();

});

