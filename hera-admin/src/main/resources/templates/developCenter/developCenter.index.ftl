<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/plugins/ztree/zTreeStyle.css">
</head>

<body class="hold-transition skin-green-light sidebar-mini">
<div class="wrapper">
    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">

        <section class="content" >
            <div class="container-fluid">
                <div class="row-fluid">
                    <div class="col-md-3 panel panel-primary" style="height:10px;padding-bottom:70%">

                        <div >
                            <ul id="documentTree" class="ztree"></ul>
                        </div>

                    </div>

                    <div class="col-md-9 panel panel-primary" style="height:10px;padding-bottom:70%">

                        <form>
                            <button type="submit" class="btn btn-success btn-sm">执行</button>
                            <button type="submit" class="btn btn-success btn-sm">执行选中的代码</button>
                        </form>
                        </br>

                        <div class="row">
                        <div  class="span8" style="height: 600px;overflow-y:auto">
                            <div class="box-body pad" >
                                <form>
                                    <textarea class="textarea" placeholder="编写脚本" style="width: 100%; height: 500px; font-size: 14px; line-height: 18px; border: 1px solid #dddddd; padding: 10px;"></textarea>
                                </form>
                            </div>
                        </div>

                        <div class="span4" >
                            <div class="box-body pad" >
                                <form>
                                    <textarea class="textarea" placeholder="运行日志" style="width: 100%; height: 100px; font-size: 14px; line-height: 18px; border: 1px solid #dddddd; padding: 10px;"></textarea>
                                </form>
                            </div>
                        </div>

                        </div>

                    </div>

                </div>
        </section>
    </div>

<@netCommon.commonScript />
    <script src="${request.contextPath}/plugins/ztree/jquery.ztree.core.js"></script>
    <script src="${request.contextPath}/plugins/ztree/jquery.ztree.exedit.js"></script>
    <script src="${request.contextPath}/plugins/ztree/jquery.ztree.excheck.js"></script>
    <script src="${request.contextPath}/js/developCenter.js"></script>

</body>

</html>


