<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>开发中心</title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link href="${request.contextPath}/static/plugins/ztree/css/metroStyle/metroStyle.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/lib/codemirror.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/addon/hint/show-hint.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/eclipse.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/lucario.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/3024-day.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/3024-night.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/ambiance.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/base16-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/base16-light.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/bespin.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/blackboard.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/colorforth.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/dracula.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/duotone-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/duotone-light.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/erlang-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/gruvbox-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/mbo.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/material.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/solarized.css" rel="stylesheet">
    <link href="${request.contextPath}/static/adminlte/bootstrap/css/bootstrap-tab.css" rel="stylesheet">
    <link href="${request.contextPath}/static/css/iconfont.css" rel="stylesheet">
    <link href="${request.contextPath}/static/css/developCenter.css" rel="stylesheet">
    <link href="${request.contextPath}/static/adminlte/plugins/bootstrap-fileinput/fileinput.min.css" rel="stylesheet">
    <link href="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table.min.css" rel="stylesheet">

</head>

<style type="text/css">

    div#rMenu {
        position: absolute;
        visibility: hidden;
        top: 0;
        text-align: left;
        width: 100px;
        z-index: 9999;
    }

    div#rMenu ul {
        margin: 0;
        padding: 0;
        border: 0;
        outline: 0;
        font-weight: inherit;
        font-style: inherit;
        font-size: 100%;
        font-family: inherit;
        vertical-align: baseline;
    }

    div#rMenu ul li {
        padding: 1px 5px;
        cursor: pointer;
        list-style: none outside none;
    }

    div#rMenu ul li:hover {
        background-color: rgba(0, 0, 0, 0.1);
    }


</style>

<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper" style="height: 100%;">
    <!-- header -->
    <@netCommon.commonHeader />
    <!-- left -->
    <@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">
        <section class="content">
            <div class="row myPanel">
                <div class="col-md-2 panel panel-primary left-bar">
                    <div style="overflow: auto;" class="height-self">
                        <ul id="documentTree" class="ztree"></ul>
                    </div>
                    <div id="rMenu" class="box box-primary">
                        <ul>
                            <li id="addFolder">增加文件夹</li>
                            <li id="addHiveFile">新建Hive</li>
                            <li id="addShellFile">新建Shell</li>
                            <li id="addSparkFile">新建Spark</li>
                            <li id="rename">重命名</li>
                            <li id="removeFile">删除</li>
                        </ul>
                    </div>
                </div>

                <div class="col-md-10 panel panel-primary left-panel" id="devCenter">
                    <div id="config" class="devStyle">
                        <button id="execute" type="submit" class="btn btn-primary btn-sm">执行</button>
                        <button id="executeSelector" type="submit" class="btn btn-primary btn-sm">执行选中的代码</button>
                        <button id="uploadResource" type="submit" class="btn btn-primary btn-sm">上传资源</button>
                        <button id="syncingTask" type="submit" class="btn btn-primary btn-sm">同步任务</button>
                        <button id="saveScript" class="btn btn-primary btn-sm">保存脚本</button>
                    </div>
                    <#--tab框-->
                    <div class="prev-next-con" id="prevNextCon">
                        <div class="prev-tab iconfont icon-prev">&#xe62d;</div>
                        <div class="next-tab iconfont icon-next">&#xe62e;</div>
                    </div>
                    <div id="tabContainer" class="devStyle"></div>
                    <div class="code-log-con">
                        <div id="scriptEditor" class="devStyle">
                            <textarea id="fileScript" name="editor"></textarea>
                        </div>
                        <div id="logContainer" class="log-container">
                            <div class="prev-next-con">
                                <div class="prev-tab iconfont icon-prev">&#xe62d;</div>
                                <div class="next-tab iconfont icon-next">&#xe62e;</div>
                            </div>
                            <div class="right-now-logs-id" id="rightLogCon">
                                <ul class="right-now-ul"></ul>
                            </div>
                            <div class="right-now-log-con" id="rightNowLogCon">
                                <div class="right-now-log">
                                </div>
                            </div>
                            <div class="bottom-tabs">
                                <span id="showLog" class="right-log-tab log-tab">查看日志</span>
                                <span id="logButton" class="history-log-tab log-tab">历史日志</span>
                            </div>
                        </div>
                    </div>
                    <div class="modal fade" id="debugLogDetail" tabindex="-1" role="dialog"
                         aria-labelledby="title">
                        <div id="debugLog" class="modal-dialog" style="width: 1300px">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <button type="button" class="close" data-dismiss="modal"></button>
                                    <div class="modal-title">详细日志</div>
                                </div>
                                <div class="modal-body">
                                    <table class="table " id="debugLogDetailTable"></table>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-default" data-dismiss="modal">返回</button>
                                    <button type="button" class="btn btn-info add-btn" name="refreshLog">刷新</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>
</div>

<div class="modal" id="uploadFile" tabindex="-1" role="dialog" aria-labelledby="title">
    <div class="modal-dialog" style="width: 600px">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"></button>
                <div class="modal-title"><h4>上传文件</h4></div>
            </div>
            <div class="modal-body">
                <div id="responseResult" class="modal-title"></div>
            </div>

            <div class="modal-footer">
                <input multiple id="fileForm" name="fileForm" type="file" class="file-loading">
                <br>
                <button class="btn btn-primary" id="closeUploadModal">关闭</button>
            </div>
        </div>
    </div>
</div>

<div id="alertSuccess" z-index="1001" class="alert alert-success text-center fade in"
     style="position: fixed; right: 0px;top: 0px;display: none; height: 50px;">
    <strong id="successText"></strong>
</div>
<div id="alertFailure" z-index="1001" class="alert alert-danger text-center fade in"
     style="position: fixed; right: 0px;top: 0px;display: none;height: 50px;">
    <strong id="failureText"></strong>
</div>

<div class="response box box-success" id="responseCon">
    <p id="response"></p>
</div>

<div class="modal fade" tabindex="-1" role="dialog" id="cancelSrueModal">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <p>确认取消running中任务吗?</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">不了</button>
                <button type="button" class="btn btn-primary" id="sureCancelBtn">确认</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<script type="text/html" id="addHostGroup">
    <form class="layui-form layui-form-pane" action="" id="addHostGroupForm">
        <div class="layui-form-item">
            <label class="layui-form-label">机器组</label>
            <div class="layui-input-block">
                <select id="hostGroupId" lay-verify="required">
                    {{# layui.each(d, function(index,item) { }}
                    <option value="{{item.id}}">{{item.name}}</option>
                    {{# }); }}
                </select>
            </div>
        </div>
    </form>
</script>
<@netCommon.commonScript />
<script src="${request.contextPath}/static/plugins/codemirror/lib/codemirror.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/mode/shell/shell.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/anyword-hint.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/show-hint.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/sql-hint.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/active-line.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/mode/python/python.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/mode/sql/sql.js"></script>
<script src="${request.contextPath}/static/plugins/ztree/js/jquery.ztree.core.min.js"></script>
<script src="${request.contextPath}/static/plugins/ztree/js/jquery.ztree.exedit.min.js"></script>
<script src="${request.contextPath}/static/plugins/ztree/js/jquery.ztree.excheck.min.js"></script>
<script src="${request.contextPath}/static/plugins/ztree/js/jquery.ztree.exhide.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-fileinput/fileinput.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-fileinput/zh.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table-zh-CN.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bootstrap/js/bootstrap-tab.js"></script>
<script src="${request.contextPath}/static/js/common.js"></script>
<script src="${request.contextPath}/static/js/developCenter.js?v=1"></script>
</body>

</html>


