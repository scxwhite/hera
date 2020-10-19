<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>任务调度中心</title>
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link href="${request.contextPath}/static/plugins/ztree/css/metroStyle/metroStyle.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/lib/codemirror.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/addon/hint/show-hint.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/eclipse.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/lucario.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/3024-day.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/3024-night.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/ambiance.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/base16-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/base16-light.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/bespin.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/blackboard.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/colorforth.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/dracula.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/duotone-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/duotone-light.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/erlang-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/addon/hint/merge.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/gruvbox-dark.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/mbo.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/material.css" rel="stylesheet">
    <link href="${request.contextPath}/static/plugins/codemirror/theme/solarized.css" rel="stylesheet">
    <link href="${request.contextPath}/static/adminlte/bootstrap/css/bootstrap-datetimepicker.min.css"
          rel="stylesheet"/>
    <link href="${request.contextPath}/static/plugins/codemirror/theme/base16-light.css" rel="stylesheet">
    <link href="${request.contextPath}/static/adminlte/plugins/bootstrap-fileinput/fileinput.min.css" rel="stylesheet">
    <link href="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table.min.css"
          rel="stylesheet">
    <link rel="stylesheet" href="${request.contextPath}/static/plugins/bootstrap-select/bootstrap-select.min.css">
    <link rel="stylesheet" href="${request.contextPath}/static/css/scheduleCenter.css">
    <link href="${request.contextPath}/static/adminlte/bootstrap/css/bootstrap-datetimepicker.min.css"
          rel="stylesheet"/>

</head>


<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper">
    <!-- header -->
    <@netCommon.commonHeader />
    <!-- left -->
    <@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">

        <section class="content">
            <div class="row">
                <div class="col-md-3 col-sm-3 col-lg-3 colStyle" style="border: none" id="treeCon">
                    <div class="height-self left-bar" style="overflow: auto;">
                        <div class="box-header left-bar-head">
                            <ul class="nav nav-tabs" role="tablist">
                                <li role="presentation" class="active" style="background-color: #fff"><a href="#"
                                                                                                         role="tab"
                                                                                                         id="myScheBtn">我的调度任务</a>
                                </li>
                                <li role="presentation" style="background-color: #fff"><a href="#" role="tab"
                                                                                          id="allScheBtn">全部调度任务</a>
                                </li>
                            </ul>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" id="hideTreeBtn"><i
                                            class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body" style="height: 100%;padding-bottom: 10px;">
                            <div>
                                <input type="text" class="form-control" id="keyWords" placeholder="请输入关键词(空格分割,回车搜索)">
                                <p id="searchInfo" style="display: none">查找中，请稍候...</p>
                                <div class="scroll-box">
                                    <ul id="jobTree" class="ztree"></ul>
                                    <ul id="allTree" class="ztree"></ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-8 col-sm-8 col-lg-8 colStyle height-self"
                     style="overflow: auto;background: transparent;border: none;display: none" id="showAllModal">
                    <div class="my-box" style="margin-top: 0">
                        <div class="box box-body text-center">

                            <div id="allTable">

                            </div>
                        </div>
                    </div>
                </div>


                <div class="col-md-8 col-sm-8 col-lg-8 colStyle height-self"
                     style="overflow: auto;" id="infoCon">

                    <div class="my-box" style="margin-top: 0">

                        <div id="groupMessage" class="box-body text-center" style="display: none">
                            <label class="info-title">基本信息</label>
                            <form class="form-horizontal form-group-sm">

                                <div class="row">
                                    <div class="col-sm-12">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-1">组id:</label>
                                            <div class="col-sm-3">
                                                <input class="form-control" type="text" name="id" readonly>
                                            </div>
                                            <label class="control-label input-sm col-sm-1">名称:</label>
                                            <div class="col-sm-3">
                                                <input class="form-control" type="text" name="name" readonly>
                                            </div>
                                            <label class="control-label input-sm col-sm-1">所有人:</label>
                                            <div class="col-sm-3">
                                                <#--<label class="form-control-static" name="owner">类型</label>-->
                                                <input class="form-control" type="text" name="owner" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-1">描述:</label>
                                            <div class="col-sm-3">
                                                <#--<label class="form-control-static" name="description">导数据</label>-->
                                                <input class="form-control" type="text" name="description" readonly>
                                            </div>
                                            <label class="control-label input-sm col-sm-1">关注人员:</label>
                                            <div class="col-sm-3">
                                                <#--<label class="form-control-static" name="focusUser"></label>-->
                                                <input class="form-control" type="text" name="focusUser" readonly>
                                            </div>
                                            <label class="control-label input-sm col-sm-1">管理员:</label>
                                            <div class="col-sm-3">
                                                <#--<label class="form-control-static" name="uidS"></label>-->
                                                <input class="form-control" type="text" name="uidS" readonly>
                                            </div>
                                        </div>

                                    </div>
                                </div>


                            </form>

                        </div>

                        <div id="jobMessage" class="box-body" style="display: none">
                            <!-- <label class="info-title">作业信息</label> -->

                            <form class="form-group-sm form-horizontal">

                                <label class="info-title">基本信息</label>
                                <div class="row">
                                    <!-- 第1列 -->
                                    <div class="col-lg-4 col-md-4 col-sm-4">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">任务id:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="id" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">任务类型:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="runType" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">所有者:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="owner" readonly>
                                            </div>
                                        </div>
                                    </div>
                                    <!-- 第2列 -->
                                    <div class="col-lg-4 col-md-4 col-sm-4">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">名称:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="name" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">优先级:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="runPriorityLevel"
                                                       readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">关注者:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="focusUser" readonly>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 第3列 -->
                                    <div class="col-lg-4 col-md-4 col-sm-4">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">描述:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="description" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">标签:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="bizLabel"
                                                       readonly>
                                            </div>
                                        </div>
                                        <div class="form-group ">
                                            <label class="control-label input-sm col-sm-3">区域:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="area" readonly>
                                            </div>
                                        </div>
                                    </div>
                                </div>


                                <label class="info-title" >调度信息</label>
                                <div class="row">
                                    <!-- 第1列 -->
                                    <div class="col-lg-4 col-md-4 col-sm-4">

                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">自动调度:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="auto" readonly>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">重试次数:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="rollBackTimes" readonly>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">调度周期:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="cronPeriod" readonly>
                                            </div>
                                        </div>

                                        <div class="form-group ">
                                            <label class="control-label input-sm col-sm-3">机器组:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="hostGroupName" readonly>
                                            </div>
                                        </div>

                                    </div>

                                    <div class="col-lg-4 col-md-4 col-sm-4">
                                        <!-- 第2列 -->
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">调度类型:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="scheduleType" readonly>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">重试间隔(分):</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="rollBackWaitTime"
                                                       readonly>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">参数基准间隔:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="cronInterval"
                                                       readonly>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">预计时长:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="mustEndMinute" readonly>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-lg-4 col-md-4 col-sm-4">
                                        <!-- 第3列 -->
                                        <div class="form-group" id="cronExpression">
                                            <label class="control-label input-sm col-sm-3">定时表达式:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="cronExpression" readonly>
                                            </div>
                                        </div>

                                        <div class="form-group" id="dependencies">
                                            <label class="control-label input-sm col-sm-3">依赖任务:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="dependencies" readonly>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">可重复执行:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="repeatRun" readonly>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">报警级别</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="alarmLevel" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">预计结束:</label>
                                            <div class="col-sm-8">
                                                <input class="form-control" type="text" name="estimatedEndHour"
                                                       readonly>
                                            </div>
                                        </div>

                                    </div>
                                </div>


                            </form>

                        </div>

                        <div id="groupMessageEdit" class="box-body" style="display: none;">
                            <form class="form-horizontal form-group-sm" role="form">
                                <form class="form-horizontal">
                                    <div class="row">
                                        <div class="col-lg-8 col-md-8 col-sm-8">
                                            <div class="form-group">
                                                <label class="control-label col-sm-4 col-lg-4 col-md-4">名称:</label>
                                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                    <input class="form-control" type="text" name="name">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="control-label col-sm-4 col-lg-4 col-md-4">描述:</label>
                                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                    <input class="form-control" type="text" name="description">
                                                </div>
                                            </div>

                                        </div>
                                    </div>
                                </form>

                            </form>
                        </div>





                        <div id="jobMessageEdit" class="box-body" style="display: none;">
                            <form class="form-horizontal form-group-sm" role="form" id="jobMsgEditForm">

                                <label class="info-title">基本信息</label>

                                <div class="row">
                                    <!-- 第1列 -->
                                    <div class="col-sm-6 col-md-6 col-lg-6">

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">名称:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="name">

                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">任务类型:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="runType">
                                                    <option value="Shell" selected="selected">Shell</option>
                                                    <option value="Hive">Hive</option>
                                                    <option value="Spark">Spark</option>
                                                </select>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">标签:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="bizLabel">

                                            </div>
                                        </div>
                                    </div>


                                    <!-- 第2列 -->
                                    <div class="col-sm-6 col-md-6 col-lg-6">

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4"><label class="tip">*</label>描述:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="description">

                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">优先级:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="runPriorityLevel">
                                                    <option value="3">high</option>
                                                    <option value="2">medium</option>
                                                    <option value="1" selected="selected">low</option>
                                                </select>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">区域:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select name="areaId" class="selectpicker form-control"
                                                        data-live-search="true" multiple data-done-button="true">

                                                </select>
                                            </div>
                                        </div>

                                    </div>
                                </div>




                                <label class="info-title" >调度信息</label>

                                <div class="row">
                                    <!-- 第1列 -->
                                    <div class="col-sm-6 col-md-6 col-lg-6">

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">调度类型:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="scheduleType">
                                                    <option value="0">定时调度</option>
                                                    <option value="1">依赖调度</option>
                                                </select>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">调度周期:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="cronPeriod">
                                                    <option value="year">年</option>
                                                    <option value="month">月</option>
                                                    <option value="day" selected="selected">天</option>
                                                    <option value="hour">小时</option>
                                                    <option value="minute">分</option>
                                                    <option value="second">秒</option>
                                                    <option value="other">其他</option>
                                                </select>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">重试次数:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="rollBackTimes">
                                                    <option value="0" selected="selected">0</option>
                                                    <option value="1">1</option>
                                                    <option value="2">2</option>
                                                    <option value="3">3</option>
                                                    <option value="4">4</option>
                                                </select>

                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">可重复执行:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select name="repeatRun" class="form-control">
                                                    <option value="1" selected>是</option>
                                                    <option value="0">否</option>
                                                </select>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">报警级别:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="offset">
                                                    <option value="2">电话</option>
                                                    <option value="1">微信</option>
                                                    <option value="0" selected="selected">邮件</option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>


                                    <!-- 第2列 -->
                                    <div class="col-sm-6 col-md-6 col-lg-6">

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">定时表达式:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="cronExpression"
                                                       id="timeChange">
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">依赖任务:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" id="dependJob"
                                                       name="dependencies">

                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">参数基准间隔:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="cronInterval">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">预计结束:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form_datetime form-control" name="estimatedEndHour"
                                                       size="12" type="text" readonly placeholder="请选择日期">

                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">重试间隔(分):</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="rollBackWaitTime">
                                                    <option value="1" selected="selected">1</option>
                                                    <option value="5">5</option>
                                                    <option value="10">10</option>
                                                    <option value="30">30</option>
                                                    <option value="60">60</option>
                                                    <option value="120">120</option>
                                                </select>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">机器组:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="hostGroupId">

                                                </select>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">预计时长(分):</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="mustEndMinute">

                                            </div>
                                        </div>

                                    </div>
                                </div>
                            </form>
                        </div>

                    </div>

                    <div id="config" class="my-box" style="display: none">
                        <div class="box-body">
                            <div class="form-group">
                                <label class="info-title">配置项信息</label>
                                <textarea class="form-control"
                                ></textarea>
                            </div>
                        </div>
                    </div>
                    <div id="script" class="my-box" style="display: none">
                        <div class="box-body">
                            <div class="form-group">
                                <label class="info-title" style="display: inline">脚本</label>
                                <select class="pull-right center-block" onchange="selectTheme()" id="themeSelect">
                                    <option value="default">default</option>
                                    <option value="lucario">lucario</option>
                                    <option value="eclipse">eclipse</option>
                                    <option value="3024-day">3024-day</option>
                                    <option value="ambiance">ambiance</option>
                                    <option value="base16-dark">base16-dark</option>
                                    <option value="base16-light">base16-light</option>
                                    <option value="bespin">bespin</option>
                                    <option value="blackboard">blackboard</option>
                                    <option value="colorforth">colorforth</option>
                                    <option value="dracula">dracula</option>
                                    <option value="duotone-dark">duotone-dark</option>
                                    <option value="duotone-light">duotone-light</option>
                                    <option value="erlang-dark">erlang-dark</option>
                                    <option value="gruvbox-dark">gruvbox-dark</option>
                                    <option value="mbo">mbo</option>
                                    <option value="material">material</option>
                                    <option value="solarized">solarized</option>
                                </select>
                            </div>

                            <div class="form-group">
                                  <textarea id="editor" name="editor"
                                  ></textarea>
                            </div>
                        </div>
                    </div>
                    <div id="inheritConfig" class="my-box" style="display: none">
                        <div class="box-body">
                            <div class="form-group">
                                <label class="info-title">继承的配置项信息</label>
                                <textarea class="form-control" style="resize: none"
                                ></textarea>
                            </div>
                        </div>
                    </div>


                </div>

                <div class="col-md-1 col-lg-1 col-sm-1 colStyle">

                    <div id="groupOperate" style="display: none;" class="btn-con">
                        <div class="box-body">
                            <div>
                                <ul class="list-unstyled">
                                    <li>
                                        <button class="btn btn-xs  btn-primary btn-block" type="button" id="showAllBtn">
                                            任务总览
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-xs  btn-primary btn-block" type="button"
                                                name="showRunning">正在运行
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-xs  btn-primary btn-block" type="button"
                                                name="showFaild">失败记录
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn  btn-xs btn-primary btn-block" type="button" name="addGroup">
                                            添加组
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn  btn-xs btn-primary btn-block" type="button" name="edit">编辑
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn  btn-xs btn-primary btn-block" type="button" name="addJob">
                                            添加任务
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn  btn-xs btn-primary btn-block" type="button" name="delete">
                                            删除
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn  btn-xs btn-primary btn-block" type="button" name="addAdmin">
                                            配置管理员
                                        </button>

                                    </li>
                                    <#--     <br>
                                         <li>
                                             <button class="btn  btn-xs btn-primary btn-block" type="button">关注组下任务</button>
                                         </li>-->
                                </ul>
                            </div>
                        </div>

                    </div>

                    <div id="jobOperate" class="btn-con" style="display: none">
                        <div class="box-body" style="white-space:nowrap;">
                            <ul class="list-unstyled">
                                <li>
                                    <button class="btn btn-xs btn-primary btn-block" type="button" name="runningLog">
                                        运行日志
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn btn-xs btn-primary btn-block" type="button" name="record">
                                        操作记录
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn btn-xs btn-primary btn-block" type="button" name="version">版本生成
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="jobDag"
                                            data-toggle="modal">依赖图
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="edit">编辑
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button id="manual" class="btn  btn-xs btn-primary btn-block" type="button"
                                            data-toggle="modal">
                                        执行任务
                                    </button>
                                </li>

                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="switch">
                                        开启/关闭
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="invalid">
                                        失效
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="delete">删除
                                    </button>

                                </li>
								<br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="copyJob">
                                        复制任务
                                    </button>

                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="addAdmin">
                                        配置管理员
                                    </button>

                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="monitor">
                                        关注该任务
                                    </button>
                                </li>
                            </ul>
                        </div>
                    </div>

                    <div id="editOperator" class="btn-con" style="display: none">
                        <div class="box-body">
                            <ul class="list-unstyled">
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="back">返回
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="upload">
                                        上传资源文件
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn btn-xs  btn-primary btn-block" type="button" name="save">保存
                                    </button>
                                </li>
                                <br>
                            </ul>
                        </div>
                    </div>

                    <div id="overviewOperator" class="btn-con" style="display: none">
                        <div class="box-body">
                            <ul class="list-unstyled">
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="back">返回
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="showRunning">
                                        正在运行
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn btn-xs  btn-primary btn-block" type="button" name="showFaild">
                                        失败记录
                                    </button>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>

</div>
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="addConfig"
     aria-hidden="true">
    <div class="modal-dialog" style="height:100px;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title" id="title">选择Job版本</h4>
            </div>
            <div class="modal-body">
                <div class="form-horizontal" role="form">

                    <div class="form-group">
                        <label class="col-sm-2 control-label">选择版本</label>
                        <div class="col-sm-10">
                            <select id="selectJobVersion" class="form-control ">
                            </select>
                        </div>

                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label">触发类型</label>
                        <div class="col-sm-10">
                            <div class="radio">
                                <label>
                                    <input type="radio" name="triggerType" value="2" checked>
                                    手动执行
                                </label>
                                <label>
                                    <input type="radio" name="triggerType" value="3">手动恢复
                                </label>
                                <label>
                                    <input type="radio" name="triggerType" value="6">超级恢复
                                </label>
                            </div>

                        </div>

                    </div>
                </div>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-info add-btn">执行</button>
            </div>
        </div>
    </div>
</div>


<div class="modal fade" id="addJobModal" tabindex="-1" role="dialog" aria-labelledby="addJob"
     aria-hidden="true">
    <div class="modal-dialog" style="height:100px;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title">添加任务</h4>
            </div>
            <div class="modal-body">

                <div class="form-horizontal">
                    <div class="row">
                        <div class="col-sm-8 col-md-8 col-lg-8">
                            <div class="form-group">
                                <label class="control-label col-sm-4 col-lg-4 col-md-4">任务名称</label>
                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                    <input class="form-control" type="text" name="jobName">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-sm-4 col-lg-4 col-md-4">任务类型</label>
                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                    <select class="form-control" name="jobType">
                                        <option value="shell" selected>shell脚本</option>
                                        <option value="hive">hive脚本</option>
                                        <option value="spark">spark脚本</option>
                                        <#--没有权限控制，暂时就不开放了<option value="spark2">spark2脚本</option>-->
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <br>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-info add-btn" name="addBtn">添加</button>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="addGroupModal" tabindex="-1" role="dialog" aria-labelledby="addGroupModal"
     aria-hidden="true">
    <div class="modal-dialog" style="height:100px;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title">添加组</h4>
            </div>
            <div class="modal-body">

                <div class="form-horizontal">
                    <div class="row">
                        <div class="col-sm-8 col-md-8 col-lg-8">
                            <div class="form-group">
                                <label class="control-label col-sm-4 col-lg-4 col-md-4">目录名称</label>
                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                    <input class="form-control" type="text" name="groupName">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-sm-4 col-lg-4 col-md-4">目录类型</label>
                                <div class="col-sm-8 col-lg-8 col-md-8 ">
                                    <select class="form-control" name="groupType">
                                        <option value="0" selected>大目录</option>
                                        <option value="1">小目录</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <br>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-info add-btn" name="addBtn">添加</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="jobLog" tabindex="-1" role="dialog" aria-labelledby="jobLog" aria-hidden="true">
    <div class="modal-dialog" style="width: 90%">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" id="myModalLabel">信息日志</h4>
            </div>

            <div class="modal-body">
                <table class="table " id="runningLogDetailTable"></table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">返回</button>
                <button type="button" class="btn btn-info add-btn" name="refreshLog">刷新</button>
            </div>
        </div>
    </div>
</div>



<div class="modal fade" id="recordModal" tabindex="-1" role="dialog" aria-labelledby="recordModal" aria-hidden="true">
    <div class="modal-dialog" style="width: 90%">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title">操作记录</h4>
            </div>

            <div class="modal-body">
                <table class="table " id="recordTable"></table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">返回</button>
                <button type="button" class="btn btn-info add-btn" name="refreshLog">刷新</button>
            </div>
        </div>
    </div>
</div>
<div class="modal" id="uploadFile" tabindex="-1" role="dialog" aria-labelledby="title">
    <div class="modal-dialog" style="width: 600px">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"></button>
                <div class="modal-title"><h4>上传文件</h4></div>
            </div>
            <div class="modal-body">
                <div id="responseResult" class="modal-title"></div>
            </div>

            <div class="modal-footer">
                <input multiple id="fileForm" name="fileForm" type="file" class="file-loading"
                >
                <br>
                <button class="btn btn-primary" id="closeUploadModal">关闭</button>
            </div>
        </div>
    </div>
</div>


<div class="modal" id="selectDepend" tabindex="-1" role="dialog" aria-labelledby="title">
    <div class="modal-dialog" style="width: 600px">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"></button>
                <div class="modal-title"><h4>选择任务依赖任务</h4></div>
            </div>
            <div class="modal-body">
                <input type="text" class="form-control" id="dependKeyWords" placeholder="请输入关键词">
                <p id="deSearchInfo" style="display: none">查找中，请稍候...</p>
                <ul id="dependTree" class="ztree"></ul>
            </div>

            <div class="modal-footer">
                <button class="btn btn-primary" id="chooseDepend">确定</button>
            </div>
        </div>
    </div>
</div>
<div class="modal" id="addAdminModal" tabindex="-1" role="dialog" aria-labelledby="title">
    <div class="modal-dialog" style="width: 600px">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"></button>
                <div class="modal-title"><h4>配置管理员</h4></div>
            </div>
            <div class="modal-body">
                <select id="userList" class="selectpicker form-control" multiple data-done-button="true">

                </select>
            </div>

            <div class="modal-footer">
                <button class="btn btn-primary" name="submit" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div>
<#--定时表达式模态框-->

<div class="modal fade" tabindex="-1" role="dialog" id="timeModal">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title">构造定时表达式</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal">
                    <div class="form-group">
                        <label for="inputMin" class="col-sm-2 control-label">分</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="inputMin" placeholder="分">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputHour" class="col-sm-2 control-label">时</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="inputHour" placeholder="时">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputDay" class="col-sm-2 control-label">天</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="inputDay" placeholder="天">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputMonth" class="col-sm-2 control-label">月</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="inputMonth" placeholder="月">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputWeek" class="col-sm-2 control-label">周</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="inputWeek" placeholder="周">
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" id="saveTimeBtn">确认</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="response response-sch box box-success" id="responseCon">
    <p id="response"></p>
</div>

<div class="modal fade" tabindex="-1" role="dialog" id="jobDagModal">
    <div class="modal-dialog modal-lg" role="document" id="jobDagModalCon">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="box-title">任务链路图</h3>
                <div id="biggerBtn">
                    <i class="fa fa-plus"></i>
                </div>
            </div>
            <div class="modal-body">

                <form class="form-inline">

                    <div class="form-group">
                        <label for="itemw">任务ID:</label>
                        <input id="item" class="input-sm" style="width:80px; border: 1px solid #ccc;"/>
                        <input class="btn btn-primary" type="button" value="上游任务链" onclick="keypath(0)"/>
                        <input class="btn btn-primary" type="button" value="下游任务链" onclick="keypath(1)"/>
                    </div>
                    <div class="form-group">
                        <input class="btn btn-primary disabled" type="button" id="expandAll" value="展示全部">
                    </div>
                </form>

                </br>
                <div class="row" style="margin: 0;">
                    <svg style="border: 3px solid dimgrey;height:700" class="col-lg-10">
                        <g/>
                    </svg>
                    <textarea class="label-primary col-lg-2 col-sm-2 col-md-2" style="height: 400px;" id="jobDetail"
                              readonly>任务信息</textarea>
                </div>
            </div>
        </div>
    </div>
</div>




<div class="modal fade" id="copyJobModal" tabindex="-1" role="dialog" aria-labelledby="copyJobConfig"
     aria-hidden="true">
    <div class="modal-dialog" style="height:100px;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title" id="title">是否复制一个新的任务!</h4>
            </div>
            <div class="modal-body">
                <div class="input-group form-inline">
                    <label class="control-label form-inline" for="jobVersion">新任务名称=原名_copy,状态=失效</label>
                </div>
                <br>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-info add-btn">执行</button>
            </div>
        </div>
    </div>
</div>


<@netCommon.commonScript />

<script src="${request.contextPath}/static/plugins/ztree/js/jquery.ztree.core.min.js"></script>
<script src="${request.contextPath}/static/plugins/ztree/js/jquery.ztree.exedit.min.js"></script>
<script src="${request.contextPath}/static/plugins/ztree/js/jquery.ztree.excheck.min.js"></script>
<script src="${request.contextPath}/static/plugins/ztree/js/jquery.ztree.exhide.min.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/lib/codemirror.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/mode/shell/shell.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/anyword-hint.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/show-hint.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/sql-hint.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/active-line.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/mode/python/python.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/mode/sql/sql.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-fileinput/fileinput.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-fileinput/zh.min.js"></script>

<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/bootstrap-table/bootstrap-table-zh-CN.min.js"></script>
<script src="${request.contextPath}/static/plugins/d3/dagre-d3.js"></script>
<script src="${request.contextPath}/static/plugins/d3/d3.v3.min.js"></script>
<script src="${request.contextPath}/static/plugins/bootstrap-select/bootstrap-select.min.js"></script>
<script src="${request.contextPath}/static/js/taskGraph.js?v=2"></script>
<script src="${request.contextPath}/static/js/scheduleCenter.js"></script>
<script src="${request.contextPath}/static/js/common.js"></script>
<script src="${request.contextPath}/static/adminlte/bootstrap/js/bootstrap-datetimepicker.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bootstrap/js/bootstrap-datetimepicker.zh-CN.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/merge.js"></script>
<script src="${request.contextPath}/static/plugins/codemirror/addon/hint/diff_match_patch.js"></script>

<script type="text/javascript">
    $(".form_datetime").datetimepicker({
        format: "hh:ii",
        autoclose: true,
        todayBtn: true,
        todayHighlight: true,
        language: 'zh-CN',//中文，需要引用zh-CN.js包
        startView: 1,
        minView: 0,
        maxView: 1,
        forceParse: false
    });
</script>
<script type="text/html" id="content">
    <div id="view"></div>
</script>


</body>

</html>


