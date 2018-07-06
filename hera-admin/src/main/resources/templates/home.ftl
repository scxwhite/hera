<!DOCTYPE html>
<html>
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
</head>


<body class="hold-transition skin-green sidebar-mini">
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
            <p class="text-center" style="font-size: 30px;color: red">二维火赫拉任务调度系统</p>
            <ul style="font-size: 20px;">
                <li>hive脚本调度</li>
                <li>shell脚本调度</li>
                <li>任务之间层级依赖调度</li>
                <li>我是梅西 现在慌得一比！！！</li>
            </ul>




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



</body>
</html>
