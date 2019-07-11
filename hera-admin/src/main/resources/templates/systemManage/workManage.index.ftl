<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>机器管理</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
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
        <section class="content">
            <div class="box">
                <div class="box-header">
                    <h3 class="big-title">机器管理</h3>
                </div>
                <div class="box-body">

                    <table id="workList" lay-filter="workList"></table>
                </div>
            </div>

        </section>
    </div>

<#--content-wrapper-->
</div>
<@netCommon.commonScript />
<script type="text/html" id="barOption">
    <a class="layui-btn layui-btn-xs layui-btn-normal" lay-event="edit">编辑</a>
    <a class="layui-btn layui-btn-xs layui-btn-danger" lay-event="del">删除</a>
</script>
<script type="text/html" id="toolbar">
    <a class="layui-btn layui-btn-radius layui-btn-normal" lay-event="add">新增</a>
</script>

<script type="text/html" id="addHostGroup">
    <form class="layui-form layui-form-pane" action="" id="addHostGroupForm">

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
            <label class="layui-form-label">ip地址</label>
            <div class="layui-input-block">
                <input type="text" name="host"
                       autocomplete="off"
                       lay-verify="required | ip"
                       class="layui-input">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">机器组</label>
            <div class="layui-input-block">
                <select name="hostGroupId" lay-verify="required">
                    {{# layui.each(d, function(index,item) { }}
                    <option value="{{item.id}}">{{item.name}}</option>
                    {{# }); }}
                </select>
            </div>
        </div>
    </form>
</script>

<script src="${request.contextPath}/static/js/workManage.js"></script>

</body>

</html>


