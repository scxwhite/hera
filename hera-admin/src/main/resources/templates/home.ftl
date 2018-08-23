<!DOCTYPE html>
<html>
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
</head>


<body class="hold-transition skin-blue-light sidebar-mini">
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
                <div class="col-lg-3 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-aqua">
                        <div class="inner">
                            <h3 id="allJobsNum">&nbsp;</h3>

                            <p>今日总任务数</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-stats-bars"></i>
                        </div>
                        <a href="${request.contextPath}/jobDetail" class="small-box-footer">More info <i
                                class="fa fa-arrow-circle-right"></i></a>
                    </div>
                </div>
                <!-- ./col -->
                <div class="col-lg-3 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-maroon-gradient">
                        <div class="inner">
                            <h3 id="failedNum">&nbsp;</h3>
                            <p>失败任务数</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-stats-bars"></i>
                        </div>
                        <a href="${request.contextPath}/jobDetail" class="small-box-footer">More info <i
                                class="fa fa-arrow-circle-right"></i></a>
                    </div>
                </div>

                <div class="col-lg-3 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-yellow">
                        <div class="inner">
                            <h3 id="queueNum">&nbsp;</h3>
                            <p>任务队列详情</p>
                        </div>
                        <div class="icon"
                        <i class="ion ion-stats-bars"></i>
                    </div>
                    <a href="${request.contextPath}/jobDetail" class="small-box-footer">More info <i
                            class="fa fa-arrow-circle-right"></i></a>
                </div>
            </div>
    </div>

    <div class="row">
        <div class="col-lg-4">
            <div id="jobStatus" style="height: 500px"></div>
        </div>
        <div class="col-lg-8">
            <div id="lineJobStatus" style="height: 500px"></div>
        </div>

    </div>
    <div class="row">
        <div class="col-lg-12">
            <div id="jobTop" style="height: 500px"></div>
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
<script src="${request.contextPath}/js/home.js"></script>

</body>
</html>
