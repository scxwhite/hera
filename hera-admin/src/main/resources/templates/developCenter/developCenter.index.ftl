<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/plugins/ztree/zTreeStyle.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/codemirror/lib/codemirror.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/codemirror/addon/hint/show-hint.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/codemirror/theme/paraiso-light.css">
</head>

<style type="text/css">
    div#rMenu {
        position: absolute;
        visibility: hidden;
        top: 0;
        background-color: #3c763d;
        text-align: left;
        padding: 2px;
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
        margin: 1px 0;
        padding: 0 5px;
        cursor: pointer;
        list-style: none outside none;
        background-color: #5d9c0a;
    }


</style>

<body class="hold-transition skin-green sidebar-mini">
<div class="wrapper">
    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">
        <section class="content">

            <div class="row">

                <div class="modal" id="uploadFile" tabindex="-1" role="dialog" aria-labelledby="title">
                    <div class="modal-dialog" style="width: 600px" >
                        <div class="modal-content">

                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal"></button>
                                <div class="modal-title"><h4>上传文件</h4></div>
                            </div>
                            <div class="modal-body">
                                <div id="responseResult" class="modal-title"></div>
                            </div>

                            <div class="modal-footer">
                                <input  multiple  id="fileForm" name="fileForm" type="file" class="file-loading" data-show-preview="false" data-allowed-file-extensions='["py","jar","sql","hive","sh","js"]'>
                                <br>
                                <button class="btn btn-primary" id="closeUploadModal">关闭</button>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-3 panel panel-primary colStyle" style="height:10px;padding-bottom:70%">
                    <div>
                        <ul id="documentTree" class="ztree"></ul>
                    </div>
                    <div id="rMenu">
                        <ul style="font-size: 15px;color: black">
                            <li id="addFolder">增加文件夹</li>
                            <li id="addHiveFile">新建Hive</li>
                            <li id="addShellFile">新建Shell</li>
                            <li id="rename">重命名</li>
                            <li id="openFile">打开</li>
                            <li id="removeFile"> 删除</li>
                            <li id="copyFile">复制文件</li>
                        </ul>
                    </div>
                </div>

                <div class="col-md-9 panel panel-primary colStyle" style="white-space:nowrap;">
                    <div id="config" class="box box-success" style="height:8px">
                        <div class="box-header with-border">
                            <div class="form-group">
                                <button id="execute" type="submit" class="btn btn-success btn-sm">执行</button>
                                <button id="executeSelector" type="submit" class="btn btn-success btn-sm">执行选中的代码</button>
                                <button id="uploadResource" type="submit" class="btn btn-success btn-sm">上传资源</button>
                                <button id="syncingTask" type="submit" class="btn btn-success btn-sm">同步任务</button>
                            </div>
                        </div>
                    </div>
                    </br>
                <#--tab框-->
                    <div id="tabContainer">

                    </div>

                    <div id="scriptEditor" class="box box-danger " >
                        <div class="box-body">
                            <div class="form-group">
                                <textarea id="fileScript" name="editor" rows="70">select 1</textarea>
                            </div>
                        </div>
                    </div>

                    <div class="nav-tabs-custom">
                        <ul class="nav nav-tabs" id="logTab">
                            <li class="active"><a href="#tab_1" data-toggle="tab">编辑</a></li>
                            <li><a href="#tab_2" data-toggle="tab">调试历史</a></li>
                        </ul>

                        <div class="tab-content">
                            <div class="tab-pane active" id="tab_1">
                                <#--<textarea id="logDetail" class="form-control" rows="35" placeholder="运行日志 "></textarea>-->
                            </div>

                            <!-- /.tab-pane -->
                            <div class="tab-pane" id="tab_2">
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
                                                <button type="button" class="btn btn-default" data-dismiss="modal">返回
                                                </button>
                                                <button type="button" class="btn btn-info add-btn" name="refreshLog">
                                                    刷新
                                                </button>
                                            </div>

                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- /.tab-pane -->
                        </div>
                        <!-- /.tab-content -->
                    </div>
                    <!-- nav-tabs-custom -->
                </div>

            </div>
        <#--row-->

        </section>
    </div>
<#--content-wrapper-->
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

</body>

</html>


