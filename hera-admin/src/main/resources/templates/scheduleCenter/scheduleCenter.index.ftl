<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/plugins/ztree/zTreeStyle.css">
</head>

<style type="text/css">
    div#rMenu {
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


</style>

<body class="hold-transition skin-green-light sidebar-mini">
<div class="wrapper">
    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "developCenter" />


    <style type="text/css">
        .modal.fade.in{
            top:190px;
        }
    </style>

    <div class="content-wrapper">

        <section class="content">
            <div class="container-fluid">

                <div class="row-fluid col-wrap">
                    <div class="col-md-3">


                        <div class="box box-success">
                            <div class="box-body">

                                <div>
                                    <ul id="jobTree" class="ztree"></ul>
                                </div>
                            </div>
                        </div>



                        <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="addConfig" aria-hidden="true">
                            <div class="modal-dialog" style="height:100px;">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                                        <h4 class="modal-title" id="title">选择Job版本</h4>
                                    </div>
                                    <div class="modal-body">
                                        <div class="input-group form-inline">
                                            <label class="input-group-addon control-label form-inline" for="jobVersion">选择Job版本&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>
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

                    </div>

                    <div class="col-md-8 " style="white-space:nowrap;">

                        <div class="box box-primary " style="display: none">

                            <form id="groupMessage" class="form-inline form-group-sm " style="white-space:nowrap;">
                                <label>基本信息</label>
                                <div class="form-group">
                                    <label class=" control-label  input-sm" for="">id</label>
                                    <p id="groupId" class="form-control-static">123456</p>
                                </div>
                                </br>
                                <div class="form-group input-sm">
                                    <label class=" control-label">名称</label>
                                    <p class="form-control-static">activity_import</p>
                                </div>
                                </br>
                                <div class="form-group input-sm">
                                    <label class="control-label">所有人</label>
                                    <p class="form-control-static">类型</p>
                                </div>
                                </br>
                                <div class="form-group input-sm">
                                    <label class=" control-label">描述</label>
                                    <p class="form-control-static">导数据</p>
                                </div>
                                </br>
                                <div class="form-group input-sm">
                                    <label class="control-label">关注人员</label>
                                    <p class="form-control-static">凌霄</p>
                                </div>
                                </br>
                                <div class="form-group input-sm">
                                    <label class="control-label">管理员</label>
                                    <p class="form-control-static">凌霄</p>
                                </div>

                            </form>

                        </div>

                        <div class="box box-primary">
                            <div id="jobMessage" class="box-body" style="display: none">
                                <label>基本信息</label>

                                <form class="form-inline form-group-sm">
                                    <div class="  form-group">
                                        <label class="control-label input-sm" for="">id</label>
                                        <p id="jobId" class="form-control-static input-sm text-left">123456</p>


                                        <label class="control-label input-sm">任务类型</label>
                                        <p class="form-control-static input-sm text-right">shell脚本</p>

                                    </div>

                                    </br>
                                    <div class="form-group ">
                                        <label class="control-label input-sm">名称</label>
                                        <p class="form-control-static">END_4820_to_mysql</p>
                                        <label class="control-label input-sm">调度类型</label>
                                        <p class="form-control-static">依赖调度</p>

                                    </div>
                                    </br>

                                    <div class="form-group">
                                        <label class="control-label input-sm">所有人:</label>
                                        <p class="form-control-static">datamine</p>
                                        <label class="control-label input-sm"> 自动调度:</label>
                                        <p class="form-control-static">开启</p>

                                    </div>
                                    </br>

                                    <div class="form-group ">
                                        <label class="control-label input-sm"> 描述:</label>
                                        <p class="form-control-static">会员定向营销发券</p>
                                        <label class="control-label input-sm">依赖任务:</label>
                                        <p class="form-control-static">[4820]</p>
                                    </div>
                                    </br>

                                    <div class="form-group ">
                                        <label class="control-label input-sm"> 重要联系人:</label>
                                        <p class="form-control-static">[]</p>
                                        <label class="control-label input-sm"> 依赖周期:</label>
                                        <p class="form-control-static">同一天</p>
                                    </div>
                                    </br>

                                    <div class="form-group">
                                        <label class="control-label input-sm">关注人员:</label>
                                        <p class="form-control-static">[]</p>
                                        <label class="control-label input-sm">任务优先级:</label>
                                        <p class="form-control-static">high</p>

                                    </div>

                                    </br>
                                    <div class="form-group ">
                                        <label class="control-label input-sm"> 管理员:</label>
                                        <p class="form-control-static">[]</p>
                                        <label class="control-label input-sm">失败重试次数:</label>
                                        <p class="form-control-static">2</p>

                                    </div>
                                    </br>

                                    <div class="form-group ">
                                        <label class="control-label input-sm"> host组id:</label>
                                        <p class="form-control-static">1</p>
                                        <label class="control-label input-sm">重试时间间隔:</label>
                                        <p class="form-control-static">1分钟</p>

                                    </div>
                                    </br>

                                    <div class="form-group ">
                                        <label class="control-label input-sm"> host组名:</label>
                                        <p class="form-control-static">default</p>
                                        <label class="control-label input-sm">预计时长:</label>
                                        <p class="form-control-static">180分钟</p>
                                    </div>
                                    </br>
                                </form>

                            </div>

                        </div>

                        <div id="config" class="box box-success" style="display: none">
                            <div class="box-body">
                                <div class="form-group">
                                    <label>配置项信息</label>
                                    <textarea class="form-control" rows="5" placeholder="Enter ..." disabled></textarea>
                                </div>
                            </div>
                        </div>
                        <div id="script" class="box box-danger " style="display: none">
                            <div class="box-body">
                                <div class="form-group">
                                    <label>脚本</label>
                                    <textarea id="jobScript" class="form-control" rows="10"
                                              placeholder="Enter ..."></textarea>
                                </div>
                            </div>
                        </div>
                        <div id="resource" class="box box-info" style="display: none">
                            <div class="box-body">
                                <div class="form-group">
                                    <label>资源信息</label>
                                    <textarea class="form-control" rows="5" placeholder="Enter ..." disabled></textarea>
                                </div>
                            </div>
                        </div>
                        <div id="inheritConfig" class="box box-success" style="display: none">
                            <div class="box-body">
                                <div class="form-group">
                                    <label>继承的配置项信息</label>
                                    <textarea class="form-control" rows="5" placeholder="Enter ..." disabled></textarea>
                                </div>
                            </div>
                        </div>

                    </div>

                    <div class="col-md-1">

                        <div id="groupOperate" class="box" style="display: none">
                            <div class="box-body">
                                <ul class="list-unstyled">
                                    <li>
                                        <button class="btn btn-primary" type="button">任务总览</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">自动任务</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">手动任务</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">添加组</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">编辑</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">添加任务</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">删除</button>

                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">配置管理员</button>

                                    </li>
                                </ul>
                            </div>

                        </div>

                        <div id="jobOperate" class="box" style="display: none">
                            <div class="box-body">
                                <ul class="list-unstyled">
                                    <li>
                                        <button class="btn btn-primary" type="button">运行日志</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">依赖图</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">编辑</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button id="manual" class="btn btn-primary" type="button" data-toggle="modal" >手动执行</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button id="manualRecovery" class="btn btn-primary" type="button">手动恢复
                                        </button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">开启/关闭</button>
                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">删除</button>

                                    </li>
                                    <br>
                                    <li>
                                        <button class="btn btn-primary" type="button">配置管理员</button>

                                    </li>
                                </ul>
                            </div>
                        </div>

        </section>

    </div>
</div>


</div>

<@netCommon.commonScript />
<script src="${request.contextPath}/plugins/ztree/jquery.ztree.core.js"></script>
<script src="${request.contextPath}/plugins/ztree/jquery.ztree.exedit.js"></script>
<script src="${request.contextPath}/plugins/ztree/jquery.ztree.excheck.js"></script>
<script src="${request.contextPath}/js/scheduleCenter.js"></script>

</body>

</html>


