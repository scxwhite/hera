/**
 * 用户管理页面，用户注册之后的权限审核
 *
 * @type {any[]}
 */

var userList = new Array();
var indexList = new Array();

$(function () {
    $('#userManage').addClass('active');
    $('#userManage').parent().addClass('menu-open');
    $('#userManage').parent().parent().addClass('menu-open');
    $('#sysManager').addClass('active');
    $(".add-btn").click(function () {

        var id = $('#id').text();

        var name = $(' #name').val();
        var email = $(' #email').val();
        var phone = $(' #phone').val();
        var description = $('#description').val();

        var user = {
            "id": id,
            "name": name,
            "email": email,
            "phone": phone,
            "description": description
        }

        jQuery.ajax({
            type: "post",
            url: base_url + "/userManage/editUser",
            data: JSON.stringify(user),
            contentType: "application/json",
            dataType: "json",
            success: function (result) {
                successMsg(result);
                $('#editUser').modal('hide');
            }
        })
    });

    $('#confirmModalBtn').on('click', function () {
        var id = $('#hidden_id').text();
        if (id == null || id == undefined || "" == id) return;

        var operateType = $('#operateType').text();

        var parameter = {
            "id": id,
            "operateType": operateType
        }
        jQuery.ajax({
            type: "post",
            url: base_url + "/userManage/operateUser.do",
            data: JSON.stringify(parameter),
            contentType: "application/json",
            dataType: "json",
            success: function (result) {
                successMsg(result);
            }
        })
    });


    var TableInit = function () {
        var oTableInit = new Object();
        oTableInit.init = function () {
            var table = $('#table');
            table.bootstrapTable({
                url: base_url + '/userManage/initUser.do',
                method: 'post',
                toolbar: '#toolbar',
                pagination: true,
                cache: false,
                clickToSelect: true,
                striped: false,
                showRefresh: true,           //是否显示刷新按钮
                showPaginationSwitch: true,  //是否显示选择分页数按钮
                pageNumber: 1,              //初始化加载第一页，默认第一页
                pageSize: 10,                //每页的记录行数（*）
                sidePagination: "client",
                pageList: [10, 25, 40, 60],
                search: true,
                uniqueId: 'id',
                columns: [
                    {
                        field: '',
                        title: '序号',
                        formatter: function (val, row, index) {
                            return index + 1;
                        }
                    }, {
                        field: 'uid',
                        title: '用户账号'
                    }, {
                        field: 'name',
                        title: '用户姓名'
                    }, {
                        field: 'email',
                        title: '用户邮箱 '
                    }, {
                        field: 'phone',
                        title: '手机号码'
                    }, {
                        field: 'description',
                        title: '描述'
                    }, {
                        field: 'gmtModified',
                        title: '更新时间'
                    }, {
                        field: 'isEffective',
                        title: '是否审核通过',
                        formatter: function (val) {
                            if (val == 0) {
                                return '<label class = "label label-default" >无效</label>';
                            } else if (val == 1) {
                                return '<label class = "label label-success" >有效</label>';
                            }
                            return val;
                        }
                    }, {
                        title: '操作',
                        formatter: function (val, row, index) {
                            userList[index] = row;
                            indexList[row.id] = index;
                            return '<a href="javascript:edit(\'' + index + '\')"><button id ="editBtn" type="button" class="btn btn-primary">编辑</button></a>&nbsp;' +
                                '<a href="javascript:del(\'' + index + '\')"><button type="button" class="btn btn-danger">删除</button></a>&nbsp;' +
                                '<a href="javascript:approve(\'' + index + '\')"><button type="button" class="btn btn-success">审核通过</button></a>&nbsp;' +
                                '<a href="javascript:refuse(\'' + index + '\')"><button type="button" class="btn btn-info">审核拒绝</button></a>'
                        }
                    }

                ]
            });
        }
        return oTableInit;
    }

    var oTable = new TableInit();
    oTable.init();
});

function edit(index) {
    var user = userList[index];
    tinyInt1isBit = false

    $('#editUser #title').text("编辑用户信息");
    $('#editUser #id').text(user.id);
    $('#editUser #name').val(user.name);
    $('#editUser #email').val(user.email);
    $('#editUser #phone').val(user.phone);
    $('#editUser #description').val(user.description);

    $('#editUser').modal('show');
}

function del(index) {
    var user = userList[index];
    $('#confirmModalLabel').text("删除操作");
    $('#hidden_id').text(user.id);
    $('#operateType').text("1");
    $('#confirmModalBody').html("确认要删除: <span style='color: #ff775a; '> " + user.name + " </span>?");
    $('#confirmModal').modal('show');
}

function approve(index) {
    var user = userList[index];
    $('#confirmModalLabel').text("审核通过");
    $('#hidden_id').text(user.id);
    $('#operateType').text("2");
    $('#confirmModalBody').html("审核通过: <span style='color: #ff775a; '> " + user.name + " </span>?");
    $('#confirmModal').modal('show');
}

function refuse(index) {
    var user = userList[index];
    var id = user.id;
    $('#confirmModalLabel').text("审核拒绝");
    $('#hidden_id').text(user.id);
    $('#operateType').text("3");
    $('#confirmModalBody').html("审核拒绝: <span style='color: #ff775a; '> " + user.name + " </span>?");
    $('#confirmModal').modal('show');
}