<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>任务历史运行记录</title>
    <#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />

</head>

<body class="hold-transition skin-blue-light sidebar-mini">
<div class="wrapper">
    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">
        <section class="content">
            <div class="input-group form-inline col-lg-2">
                <label class="name input-group-addon">任务状态</label>
                <select class="form-control" id="jobStatus" onchange="updateTable()">
                    <option value="failed" selected>失败</option>
                    <option value="success">成功</option>
                    <option value="running">运行中</option>
                </select>
            </div>
            <table id="historyJobTable"></table>
        </section>
    </div>

</div>
</body>

<@netCommon.commonScript />
<script src="${request.contextPath}/js/jobDetail.js"></script>


</html>