<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html">
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

        <section class="content" style="width: 100%; height: 100%;">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-md-3">

                        <div>
                            <ul id="documentTree" class="ztree"></ul>
                        </div>

                    </div>

                    <div class="col-md-8">

                        <div class="box box-success">
                            <div class="box-header with-border">
                                <h3 class="box-title">Different Height</h3>
                            </div>
                            <div class="box-body">
                                <input class="form-control input-lg" type="text" placeholder=".input-lg">
                                <br>
                                <input class="form-control" type="text" placeholder="Default input">
                                <br>
                                <input class="form-control input-sm" type="text" placeholder=".input-sm">
                            </div>
                            <!-- /.box-body -->
                        </div>

                        <form>
                            <button type="submit" class="btn btn-success btn-sm">执行</button>
                            <button type="submit" class="btn btn-success btn-sm">执行选中的代码</button>
                        </form>

                        <div height="600">
                            <form>
                                <div class="form-group" >
                                    <input type="code" class="form-control" id="code" placeholder="code">
                                </div>
                            </form>
                        </div>
                        <div height="100">
                            <form>
                                <div class="form-group" >
                                    <input type="log" class="form-control" id="exampleInputEmail1" placeholder="log">
                                </div>
                            </form>
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


