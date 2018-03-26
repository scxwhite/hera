<!DOCTYPE html>
<html>
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
</head>


<body class="hold-transition skin-green-light sidebar-mini">
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

        </section>
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->

    <!-- footer -->
	<@netCommon.commonFooter />

</div>
<!-- ./wrapper -->
<@netCommon.commonScript />
<script src="${request.contextPath}/adminlte/plugins/daterangepicker/moment.min.js"></script>
<script src="${request.contextPath}/adminlte/plugins/daterangepicker/daterangepicker.js"></script>
<script src="${request.contextPath}/plugins/echarts/echarts.common.min.js"></script>
<script src="${request.contextPath}/js/index.js"></script>



</body>
</html>
