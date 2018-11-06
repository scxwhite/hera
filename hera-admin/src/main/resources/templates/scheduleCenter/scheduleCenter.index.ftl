<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/plugins/ztree/metroStyle/metroStyle.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/codemirror/lib/codemirror.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/codemirror/addon/hint/show-hint.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/codemirror/theme/base16-light.css">
    <link rel="stylesheet" href="${request.contextPath}/plugins/bootstrap-select/bootstrap-select.min.css">
    <link rel="stylesheet" href="${request.contextPath}/css/scheduleCenter.css">
</head>

<style type="text/css">

    .box, .content {
        margin-bottom: 0;
        padding-bottom: 0;
        margin-top: 0;
        padding-top: 0;
    }
    div #rMenu {
        position: absolute;
        visibility: hidden;
        top: 0;
        background-color: #555;
        text-align: left;
        padding: 2px;
    }

    div#rMenu ul {
        margin: 0;
        padding: 0;
        border: 0;
        outline: 0;
        font-weight: inherit;
        font-style: inherit;
        font-size: 100%;
        font-family: inherit;
        vertical-align: baseline;
    }

    div#rMenu ul li {
        margin: 1px 0;
        padding: 0 50px;
        cursor: pointer;
        list-style: none outside none;
        background-color: #DFDFDF;
    }

    .colStyle {
        margin-right: 0px;
        margin-left: 0px;
        margin-top: 0px;
        padding: 0px;
        background-color: #eee;
        border: 1px solid #ddd;
    }
    .form-control-static {
        overflow: hidden;
    }

    body {
        font: 300 14px 'Helvetica Neue', Helvetica;
    }

    .node rect {
        stroke: #333;
        fill: #fff;
    }

    .edgePath path {
        stroke: #333;
        fill: #333;
        stroke-width: 1.5px;
    }
</style>

<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper">
    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">

        <section class="content">
            <div class="row">
                <div class="col-md-3 col-sm-3 col-lg-3 colStyle">
                    <div class="box box-primary height-self" style="overflow: auto;">
                        <div class="box-body">
                            <div>
                                <input type="text" class="form-control" id="keyWords" placeholder="请输入关键词(空格分割)">
                                <ul id="jobTree" class="ztree"></ul>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-8 col-sm-8 col-lg-8 colStyle height-self" style="overflow: auto;">

                    <div class="box box-primary">

                        <div id="groupMessage" class="box box-body text-center" style="display: none">
                            <label>基本信息</label>
                            <form class="form-horizontal form-group-sm">

                                <div class="row">
                                    <div class="col-sm-4">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-4">组id:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="id">1</label>-->
                                                <input class="form-control" type="text" name="id" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-4">名称:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="name">activity_import</label>-->
                                                <input class="form-control" type="text" name="name" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-4">所有人:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="owner">类型</label>-->
                                                <input class="form-control" type="text" name="owner" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-4">描述:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="description">导数据</label>-->
                                                <input class="form-control" type="text" name="description" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-4">关注人员:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="focusUser"></label>-->
                                                <input class="form-control" type="text" name="focusUser" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-4">管理员:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="uidS"></label>-->
                                                <input class="form-control" type="text" name="uidS" readonly>
                                            </div>
                                        </div>

                                    </div>
                                </div>


                            </form>

                        </div>

                        <div id="jobMessage" class="box-body text-center" style="display: none">
                            <label>基本信息</label>

                            <form class="form-group-sm form-horizontal">

                                <div class="row">
                                    <div class="col-lg-4 col-md-4 col-sm-4">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">任务id:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="id"></label>-->
                                                    <input class="form-control" type="text" name="id" readonly>

                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">名称:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="name">哈哈</label>-->
                                                <input class="form-control" type="text" name="name" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">任务类型:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="runType"></label>-->
                                                <input class="form-control" type="text" name="runType" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3"> 自动调度:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="label" name="auto"></label>-->
                                                    <input class="form-control" type="text" name="auto" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">任务优先级:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="runPriorityLevel"></label>-->
                                                <input class="form-control" type="text" name="runPriorityLevel" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">描述:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static"></label>-->
                                                    <input class="form-control" type="text" name="runPriorityLevel" readonly>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-lg-4 col-md-4 col-sm-4">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">调度类型:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="scheduleType"></label>-->
                                                <input class="form-control" type="text" name="scheduleType" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group" id="dependencies">
                                            <label class="control-label input-sm col-sm-3">依赖任务:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="dependencies"></label>-->
                                                <input class="form-control" type="text" name="dependencies" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group" id="heraDependencyCycle">
                                            <label class="control-label input-sm col-sm-3">依赖周期:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="heraDependencyCycle"></label>-->
                                                <input class="form-control" type="text" name="heraDependencyCycle" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group" id="cronExpression">
                                            <label class="control-label input-sm col-sm-3">定时表达式:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="cronExpression"></label>-->
                                                    <input class="form-control" type="text" name="cronExpression" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">重试次数:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="rollBackTimes"></label>-->
                                                <input class="form-control" type="text" name="rollBackTimes" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">重试间隔:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="rollBackWaitTime"></label>-->
                                                <input class="form-control" type="text" name="rollBackWaitTime" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">预计时长:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static"></label>-->
                                                <input class="form-control" type="text" name="" readonly>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-lg-4 col-md-4 col-sm-4">
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">所有人:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="owner"></label>-->
                                                <input class="form-control" type="text" name="owner" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group ">
                                            <label class="control-label input-sm col-sm-3">重要联系人:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static"></label>-->
                                                <input class="form-control" type="text" name="" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label input-sm col-sm-3">关注人员:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="focusUser"></label>-->
                                                <input class="form-control" type="text" name="focusUser" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group ">
                                            <label class="control-label input-sm col-sm-3">管理员:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="uidS"></label>-->
                                                <input class="form-control" type="text" name="uidS" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group ">
                                            <label class="control-label input-sm col-sm-3">host组id:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static" name="hostGroupName"></label>-->
                                                <input class="form-control" type="text" name="hostGroupName" readonly>
                                            </div>
                                        </div>
                                        <div class="form-group ">
                                            <label class="control-label input-sm col-sm-3">host组名:</label>
                                            <div class="col-sm-8">
                                                <#--<label class="form-control-static"></label>-->
                                                <input class="form-control" type="text" name="" readonly>
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

                                <div class="row">
                                    <div class="col-sm-6 col-md-6 col-lg-6">

                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">名称:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="name">

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
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">重试间隔(分):</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="rollBackWaitTime">
                                                    <option value="1" selected="selected">1</option>
                                                    <option value="10">10</option>
                                                    <option value="30">30</option>
                                                    <option value="60">60</option>
                                                    <option value="120">120</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">任务优先级:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="runPriorityLevel">
                                                    <option value="3">high</option>
                                                    <option value="2">medium</option>
                                                    <option value="1" selected="selected">low</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">描述:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="jobName">

                                            </div>
                                        </div>
                                    </div>
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
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">定时表达式:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="cronExpression" id="timeChange">

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
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">依赖周期:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="heraDependencyCycle">

                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">host组id:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control" name="hostGroupId">

                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">脚本是否可见:</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <select class="form-control">
                                                    <option value="不可见">不可见</option>
                                                    <option value="可见">可见</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="control-label col-sm-4 col-lg-4 col-md-4">预计时长(分):</label>
                                            <div class="col-sm-8 col-lg-8 col-md-8 ">
                                                <input class="form-control" type="text" name="jobName">

                                            </div>
                                        </div>
                                    </div>
                                </div>


                            </form>


                        </div>

                    </div>

                    <div id="config" class="box box-primary" style="display: none">
                        <div class="box-body">
                            <div class="form-group">
                                <label>配置项信息</label>
                                <textarea class="form-control"
                                ></textarea>
                            </div>
                        </div>
                    </div>
                    <div id="script" class="box box-primary " style="display: none">
                        <div class="box-body">
                            <div class="form-group">
                                <label>脚本</label>
                                <textarea id="editor" name="editor"
                                ></textarea>
                            </div>
                        </div>
                    </div>
                    <div id="inheritConfig" class="box box-primary" style="display: none">
                        <div class="box-body">
                            <div class="form-group">
                                <label>继承的配置项信息</label>
                                <textarea class="form-control" style="resize: none"
                                ></textarea>
                            </div>
                        </div>
                    </div>


                </div>

                <div class="col-md-1 col-lg-1 col-sm-1 colStyle">
                    <div id="groupOperate" class="box box-primary" style="display: none">
                        <div class="box-body">
                            <div>
                                <ul class="list-unstyled">
                                    <li>
                                        <button class="btn btn-xs  btn-primary btn-block" type="button">任务总览</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-xs  btn-primary btn-block" type="button">自动任务</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn  btn-xs btn-primary btn-block" type="button">手动任务</button>
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
                                        <button class="btn  btn-xs btn-primary btn-block" type="button" name="addAdmin">配置管理员</button>

                                    </li>
                                <#--     <br>
                                     <li>
                                         <button class="btn  btn-xs btn-primary btn-block" type="button">关注组下任务</button>
                                     </li>-->
                                </ul>
                            </div>
                        </div>

                    </div>

                    <div id="jobOperate" class="box box-primary" style="display: none">
                        <div class="box-body" style="white-space:nowrap;">
                            <ul class="list-unstyled">
                                <li>
                                    <button class="btn btn-xs btn-primary btn-block" type="button" name="runningLog">
                                        运行日志
                                    </button>
                                </li>
                                <br>

                                <li>
                                    <button class="btn btn-xs btn-primary btn-block" type="button" name="version">版本生成
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="jobDag">依赖图</button>
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
                                        手动执行
                                    </button>
                                </li>
                                <br>
                                <li>
                                    <button id="manualRecovery" class="btn  btn-xs btn-primary btn-block" type="button">
                                        手动恢复
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
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="delete">删除
                                    </button>

                                </li>
                                <br>
                                <li>
                                    <button class="btn  btn-xs btn-primary btn-block" type="button" name="addAdmin">配置管理员</button>

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

                    <div id="editOperator" class="box box-primary" style="display: none">
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
                <div class="input-group form-inline">
                    <label class="input-group-addon control-label form-inline" for="jobVersion">选择Job版本</label>
                    <select id="selectJobVersion" class="form-control">
                    </select>
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
                                        <option value="spark2">spark2脚本</option>
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
    <div class="modal-dialog" style="width: 80%">
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
                <ul id="dependTree" class="ztree"></ul>
            </div>

            <div class="modal-footer">
                <button class="btn btn-primary" id="chooseDepend">确定</button>
            </div>
        </div>
    </div>
</div>
<div class="modal" id="addAdminModal" tabindex="-1" role="dialog" aria-labelledby="title">
    <div class="modal-dialog" style="width: 600px" >
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
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
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

<@netCommon.commonScript />

<script src="${request.contextPath}/plugins/ztree/jquery.ztree.core.js"></script>
<script src="${request.contextPath}/plugins/codemirror/lib/codemirror.js"></script>
<script src="${request.contextPath}/plugins/codemirror/mode/shell/shell.js"></script>
<script src="${request.contextPath}/plugins/codemirror/addon/hint/anyword-hint.js"></script>
<script src="${request.contextPath}/plugins/codemirror/addon/hint/show-hint.js"></script>
<script src="${request.contextPath}/plugins/codemirror/addon/hint/sql-hint.js"></script>
<script src="${request.contextPath}/plugins/codemirror/addon/hint/active-line.js"></script>
<script src="${request.contextPath}/plugins/codemirror/mode/python/python.js"></script>
<script src="${request.contextPath}/plugins/codemirror/mode/sql/sql.js"></script>
<script src="${request.contextPath}/plugins/ztree/jquery.ztree.exedit.js"></script>
<script src="${request.contextPath}/plugins/ztree/jquery.ztree.excheck.js"></script>
<script src="${request.contextPath}/plugins/ztree/jquery.ztree.exhide.min.js"></script>



<script src="${request.contextPath}/plugins/bootstrap-select/bootstrap-select.min.js"></script>
<script src="${request.contextPath}/js/scheduleCenter.js"></script>
<script src="${request.contextPath}/js/common.js"></script>
<script src="https://cdn.bootcss.com/bootstrap-fileinput/4.4.2/js/locales/zh.js"></script>


</body>

</html>


