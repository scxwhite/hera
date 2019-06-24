<!DOCTYPE html>
<html>
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/static/plugins/easyPie/style.css">
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
            <div class="row" style="margin: 5px;">
                <div class="box box-info" style="overflow: hidden;">
                <div class="box-header with-border">
                        <h3 class="box-title">任务数统计</h3>
                        <div class="box-tools pull-right">
                            <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                            <button type="button" class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                        </div>
                </div>
                    <div class="box-body">
                    <div class="col-lg-3 col-xs-6" style="margin-top: 10px">
                        <!-- small box -->
                        <div class="small-box bg-aqua">
                            <div class="inner">
                                <h3 id="allJobsNum">&nbsp;</h3>

                                <p>今日总任务数</p>
                            </div>
                            <div class="icon">
                                <i class="iconfont icon-home">&#xe668;</i>
                            </div>
                            <a href="${request.contextPath}/jobDetail" class="small-box-footer">More info <i
                                    class="fa fa-arrow-circle-right"></i></a>
                        </div>
                    </div>
                    <!-- ./col -->
                    <div class="col-lg-3 col-xs-6" style="margin-top: 10px">
                        <!-- small box -->
                        <div class="small-box bg-maroon-gradient">
                            <div class="inner">
                                <h3 id="failedNum">&nbsp;</h3>
                                <p>失败任务数</p>
                            </div>
                            <div class="icon">
                                <i class="iconfont icon-home">&#xe6e6;</i>
                            </div>
                            <a href="${request.contextPath}/jobDetail" class="small-box-footer">More info <i
                                    class="fa fa-arrow-circle-right"></i></a>
                        </div>
                    </div>

                    <div class="col-lg-3 col-xs-6" style="margin-top: 10px">
                        <!-- small box -->
                        <div class="small-box bg-yellow">
                            <div class="inner">
                                <h3 id="queueNum">&nbsp;</h3>
                                <p>任务队列详情</p>
                            </div>
                            <div class="icon">
                            <i class="iconfont icon-home">&#xe6a4;</i>
                        </div>
                        <a href="${request.contextPath}/jobDetail" class="small-box-footer">More info <i
                                class="fa fa-arrow-circle-right"></i></a>
                    </div>
                    </div>
                </div>
            </div>
    </div>

    <div class="row">
        <div class="col-lg-4">
            <div class="box box-primary">
                <div class="box-header with-border">
                    <h3 class="box-title">实时任务状态</h3>
                    <div class="box-tools pull-right">
                        <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                        <button type="button" class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                    </div>
                </div>
                <div id="jobStatus"  class="box-body"  style="height: 500px"></div>
            </div>
        </div>
        <div class="col-lg-8">
            <div class="box box-success">
                <div class="box-header with-border">
                    <h3 class="box-title">任务执行状态</h3>
                    <div class="box-tools pull-right">
                        <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                        <button type="button" class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                    </div>
                </div>
                <div id="lineJobStatus"  class="box-body"  style="height: 500px"></div>
            </div>
        </div>

    </div>
    <div class="row">
        <div class="col-lg-12">
            <div class="box box-danger">
                <div class="box-header with-border">
                    <h3 class="box-title">任务时长TOP10</h3>
                    <div class="box-tools pull-right">
                        <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                        <button type="button" class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                    </div>
                </div>
                <div id="jobTop" class="box-body" style="height: 500px"></div>
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
<script src="${request.contextPath}/static/plugins/echarts/echarts.min.js"></script>
<script src="${request.contextPath}/static/plugins/echarts/PercentPie.js"></script>
<script src="${request.contextPath}/static/plugins/echarts/macarons.js"></script>
<script src="${request.contextPath}/static/plugins/echarts/shine.js"></script>

<script src="${request.contextPath}/static/js/home.js"></script>

</body>
</html>
