<!DOCTYPE html>
<html>
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/static/plugins/easyPie/style.css">
    <link href="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table.min.css" rel="stylesheet">


    <style>
        .my-easy-pie{
            text-align: center;
            margin-top: 50px;
            font-size: 20px;
            color: #666;
        }
        .btn-default {
            background-color: #fff !important;
            color: #444;
            border-color: #ddd;
        }
        .box-header {
            padding: 12px;
        }
        .table-hover>tbody>tr:hover {
             background-color: #fff;
        }
        #machineList{
            width: auto;
            display: inline;
        }
    </style>
</head>


<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper">

    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "index" />

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->

        <!-- Main content -->
        <section class="content container-fluid">

            <!--------------------------
              | Your Page Content Here |
              -------------------------->
            <div class="row">
                <div class="col-lg-12">
                    <div class="box box-danger">
                        <div class="box-header with-border">
                            <h3 class="box-title">系统概况</h3>
                            <div class="box-tools pull-right">
                                <select id="machineList" class="form-control select2 select2-hidden-accessible"></select>
                                <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                                <button type="button" class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                            </div>
                        </div>
                        <div class="box-body" style="height: 500px">
                            <div class="row">
                                <div class="col-md-2 col-sm-2 col-lg-2 my-easy-pie">
                                    <span id="userPercent" class="chart" data-percent="0">
                                        <span class="percent"></span>
                                    </span>
                                    <p>用户占用</p>
                                </div>
                                <div class="col-md-2 col-sm-2 col-lg-2 my-easy-pie" >
                                    <div id="SysPercent" class="chart"data-percent="0">
                                        <span class="percent"></span>
                                    </div>
                                    <p>系统占用</p>
                                </div>
                                <div class="col-md-4 col-lg-4 col-sm-4">
                                    <div id="ramGauge" style="height: 450px;"></div>
                                </div>
                                <div class="col-md-2 col-sm-2 col-lg-2 my-easy-pie">
                                    <div id="CPUPercent" class="chart" data-percent="0">
                                        <span class="percent"></span>
                                    </div>
                                    <p>CPU空闲</p>
                                </div>
                                <div class="col-md-2 col-sm-2 col-lg-2 my-easy-pie">
                                    <div id="SwapPercent" class="chart" data-percent="0">
                                        <span class="percent"></span>
                                    </div>
                                    <p>Swap空闲</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-8">
                    <div class="box box-info">
                        <div class="box-header with-border">
                            <h3 class="box-title">进程监控</h3>

                            <div class="box-tools pull-right">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i>
                                </button>
                                <button type="button" class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                            </div>
                        </div>
                        <!-- /.box-header -->
                        <div class="box-body table-responsive"">
                            <div id="processMonitor"></div>
                        </div>
                        <!-- /.box-body -->
                    </div>
                </div>
                <div class="col-lg-4">
                    <div class="box box-success">
                        <div class="box-header with-border">
                            <h3 class="box-title">机器信息</h3>
                            <div class="box-tools pull-right">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                                <button type="button" class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                            </div>
                        </div>
                        <div id="machineInfo" class="box-body"></div>
                    </div>
                </div>

            </div>
    </section>
    <!-- /.content -->
</div>
<!-- /.content-wrapper -->

<!-- footer -->
	<@netCommon.commonFooter />

</div>
<!-- ./wrapper -->
<@netCommon.commonScript />
<script src="${request.contextPath}/static/plugins/easyPie/jquery.easypiechart.min.js"></script>
<script src="${request.contextPath}/static/js/machineInfo.js"></script>
<script src="${request.contextPath}/static/plugins/echarts/echarts.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table-zh-CN.min.js"></script>


</body>
</html>
