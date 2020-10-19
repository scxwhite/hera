<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>用户管理中心</title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/static/css/userManage.css">
</head>

<style type="text/css">

</style>

<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper">
    <!-- header -->
    <@netCommon.commonHeader />
    <!-- left -->
    <@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">
        <ul class="nav nav-tabs" id="user-tab">
            <li class="active"><a href="#tab-admin" data-toggle="tab" id="adminTab">用户组</a></li>
            <li><a href="#tab-sso" data-toggle="tab" id="ssoTab">用户</a></li>
        </ul>

        <div class="tab-content">
            <div class="tab-pane active" id="tab-admin">
                <div class="box-body">
                    <table id="userTable" lay-filter="userTable"></table>
                </div>
            </div>

            <div class="tab-pane" id="tab-sso">
                <div class="box-body">
                    <table id="ssoTable" lay-filter="ssoTable"></table>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/html" id="toolbar">
    <a class="layui-btn layui-btn-radius layui-btn-normal" lay-event="refresh">刷新</a>
</script>
<script type="text/html" id="barOption">
    <a class="layui-btn layui-btn-xs" lay-event="approve">审核通过</a>
    <a class="layui-btn layui-btn-xs layui-btn-warm" lay-event="refuse">审核拒绝</a>
    <a class="layui-btn layui-btn-xs layui-btn-danger" lay-event="del">删除</a>
    <a class="layui-btn layui-btn-xs layui-btn-primary" lay-event="edit">编辑</a>
</script>
<script type="text/html" id="editUser">
    <form class="layui-form layui-form-pane" id="editUserForm">
        <div class="layui-form-item" style="display: none">
            <label class="layui-form-label"></label>
            <div class="layui-input-block">
                <input type="text" name="id"
                       autocomplete="off"
                       class="layui-input">
            </div>
        </div>
        <div class="layui-form-item" style="display: none">
            <label class="layui-form-label"></label>
            <div class="layui-input-block">
                <input type="text" name="isEffective"
                       autocomplete="off"
                       class="layui-input">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">用户</label>
            <div class="layui-input-block">
                <input type="text" name="name"
                       autocomplete="off"
                       class="layui-input">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">邮箱</label>
            <div class="layui-input-block">
                <input type="text" name="email"
                       autocomplete="off"
                       class="layui-input">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">电话</label>
            <div class="layui-input-block">
                <input type="text" name="phone"
                       autocomplete="off"
                       class="layui-input">
            </div>
        </div>
    </form>
</script>

<script type="text/html" id="editSso">
    <form class="layui-form layui-form-pane" action="" id="editSsoForm">

        <div class="layui-form-item"style="display: none">
            <label class="layui-form-label"></label>
            <div class="layui-input-block">
                <input type="text" name="id"
                       autocomplete="off"
                       lay-verify="required | ip"
                       class="layui-input">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">用户</label>
            <div class="layui-input-block">
                <input type="text" name="name"
                       autocomplete="off"
                       class="layui-input" readonly>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">工号</label>
            <div class="layui-input-block">
                <input type="text" name="jobNumber"
                       autocomplete="off"
                       class="layui-input">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">邮箱</label>
            <div class="layui-input-block">
                <input type="text" name="email"
                       autocomplete="off"
                       class="layui-input">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">电话</label>
            <div class="layui-input-block">
                <input type="text" name="phone"
                       autocomplete="off"
                       class="layui-input">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">所在组</label>
            <div class="layui-input-block">
                <select name="gid">
                    {{# layui.each(d, function(index,item) { }}
                    <option value="{{item.id}}">{{item.name}}</option>
                    {{# }); }}
                </select>
            </div>
        </div>
    </form>
</script>
<@netCommon.commonScript />
<script src="${request.contextPath}/static/js/userManage.js"></script>
</body>

</html>


