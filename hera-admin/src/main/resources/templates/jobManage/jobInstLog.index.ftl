<html>
<head>
    <meta charset="UTF-8">
    <title>任务执行日志</title>
    <#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />


</head>

<body class="hold-transition skin-black sidebar-mini">

   <div class="form-group">
        <div style="font-family:Microsoft YaHei" id="loginstlogdetail">
        </div>
   </div>


</body>

<@netCommon.commonScript />
<script src="${request.contextPath}/static/js/jobInstLog.js"></script>
<script src="${request.contextPath}/static/plugins/d3/d3.v3.min.js"></script>
<script src="${request.contextPath}/static/plugins/d3/dagre-d3.js"></script>

</body>
</html>



