<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>日志记录</title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link href="${request.contextPath}/static/plugins/codemirror/lib/codemirror.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/addon/hint/merge.css" rel="stylesheet">
    <link rel="stylesheet" href="${request.contextPath}/static/plugins/layui/css/modules/formSelects-v4.css">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/eclipse.css" rel="stylesheet">


</head>

<style type="text/css">
    .CodeMirror-merge, .CodeMirror-merge .CodeMirror {
        height: 500px;
    }
    .layui-layer-title{
        text-align: center;
    }

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
                <div class="box-body">
                    <table id="recordTable" lay-filter="recordTable"></table>
                </div>
            </div>
        </section>
    </div>

    <#--content-wrapper-->
</div>
<@netCommon.commonScript />
<script type="text/html" id="barOption">
    <a class="layui-btn layui-btn-xs layui-btn-normal" lay-event="detail">查看</a>
</script>
<script type="text/html" id="toolbar">
    <a class="layui-btn layui-btn-radius layui-btn-normal" lay-event="refresh">刷新</a>
</script>
<script type="text/html" id="content">
    <div id="view"></div>
</script>
<script src="${request.contextPath}/static/js/record.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/lib/codemirror.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/merge.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/diff_match_patch.js"></script>
<script src="${request.contextPath}/static/plugins/layui/lay/modules/formSelects-v4.min.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/mode/shell/shell.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/mode/sql/sql.js"></script>

</body>

</html>


