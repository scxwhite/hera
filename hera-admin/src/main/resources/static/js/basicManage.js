/**
 * 用户管理页面，用户注册之后的权限审核
 *
 * @type {any[]}
 */
layui.use(['table', 'laytpl', 'form', 'element'], function () {


    let table = layui.table, laytpl = layui.laytpl, form = layui.form;


    $('#basicManage').addClass('active');
    $('#basicManage').parent().addClass('menu-open');
    $('#basicManage').parent().parent().addClass('menu-open');
    $('#sysManager').addClass('active');

    $('#generateVersion').on('click', function () {
        $.ajax({
            url: base_url + "admin/generateAllVersion",
            async: false,
            type: 'put',
            success: function (data) {
                layer.msg(data.message);
            }
        })
    })

    $('#updateWork').on('click', function () {
        $.ajax({
            url: base_url + "admin/updateWork",
            async: false,
            type: 'put',
            success: function (data) {
                layer.msg(data.message);
            }
        })
    })

});

