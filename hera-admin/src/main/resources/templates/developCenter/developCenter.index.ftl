<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/plugins/ztree/metroStyle/metroStyle.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/codemirror/lib/codemirror.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/codemirror/addon/hint/show-hint.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/codemirror/theme/lucario.css">
    <link rel="stylesheet" href="${request.contextPath}/css/iconfont.css">
    <link rel="stylesheet" href="${request.contextPath}/css/developCenter.css">
<#--<link rel="stylesheet" href="${request.contextPath}/adminlte/dist/css/AdminLTE.css">-->
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
    div#rMenu ul li:hover{
        background-color: rgba(0,0,0,0.1);
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
                <div class="col-md-2 panel panel-primary">
                    <div style="overflow: auto;" class="height-self">
                        <ul id="documentTree" class="ztree"></ul>
                    </div>
                    <div id="rMenu" class="box box-primary">
                        <ul>
                            <li id="addFolder">增加文件夹</li>
                            <li id="addHiveFile">新建Hive</li>
                            <li id="addShellFile">新建Shell</li>
                            <li id="rename">重命名</li>
                            <li id="removeFile">删除</li>
                        </ul>
                    </div>
                </div>

                <div class="col-md-10 panel panel-primary left-panel">
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
                        <div id="scriptEditor" class="box box-primary " class="devStyle">
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

<div id="alertSuccess" z-index="1001" class="alert alert-success text-center fade in" style="position: fixed; right: 0px;top: 0px;display: none; height: 50px;" >
    <strong id="successText"></strong>
</div>
<div id="alertFailure" z-index="1001" class="alert alert-danger text-center fade in" style="position: fixed; right: 0px;top: 0px;display: none;height: 50px;" >
    <strong id="failureText" ></strong>
</div>

<div class="response box box-success" id="responseCon">
    <p id="response"></p>
</div>

<@netCommon.commonScript />
<script src="${request.contextPath}/plugins/codemirror/lib/codemirror.js"></script>
<script src="${request.contextPath}/plugins/codemirror/mode/shell/shell.js"></script>
<script src="${request.contextPath}/plugins/codemirror/addon/hint/anyword-hint.js"></script>
<script src="${request.contextPath}/plugins/codemirror/addon/hint/show-hint.js"></script>
<script src="${request.contextPath}/plugins/codemirror/addon/hint/sql-hint.js"></script>
<script src="${request.contextPath}/plugins/codemirror/mode/python/python.js"></script>
<script src="${request.contextPath}/plugins/codemirror/mode/sql/sql.js"></script>
<script src="${request.contextPath}/plugins/ztree/jquery.ztree.core.js"></script>
<script src="${request.contextPath}/plugins/ztree/jquery.ztree.exedit.js"></script>
<script src="${request.contextPath}/plugins/ztree/jquery.ztree.excheck.js"></script>
<script src="${request.contextPath}/js/common.js"></script>
<script src="${request.contextPath}/js/developCenter.js"></script>
<script src="https://cdn.bootcss.com/bootstrap-fileinput/4.4.2/js/locales/zh.js"></script>

</body>

</html>


