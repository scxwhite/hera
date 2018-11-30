<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>用户管理中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/css/userManage.css">
</head>

<style type="text/css">

</style>

<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper">
    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">
        <section class="content">
            <div class="box">
                <div class="box-header">
                    <h3 class="big-title">用户管理</h3>
                </div>
                <div class="box-body">
                    <div class = "jobPreview"  class="div-row" style="margin-bottom:10px">
                        <table id="table" class="table table-striped"/></table>
                    </div>
                </div>
            </div>

            <div class="modal fade" id="editUser" tabindex="-1" role="dialog" aria-labelledby="addConfig" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                            <h4 class="modal-title" id="title"></h4>
                        </div>
                        <div class="modal-body">
                            <label id="id" hidden></label>
                            <label id="operateType" hidden></label>
                            <label id="isValid" hidden></label>
                            <br>
                            <div class="input-group" >
                                <span class="input-group-addon" >用户姓名&nbsp;&nbsp;&nbsp;</span>
                                <input type="text" class="form-control" name="name" id="name">
                            </div>
                            <br>
                            <div class="input-group " >
                                <span class="input-group-addon" >邮箱地址&nbsp;&nbsp;&nbsp;</span>
                                <input type="text" class="form-control" name="email" id="email" >
                            </div>
                            <br>

                            <div class="input-group " >
                                <span class="input-group-addon">手机号码&nbsp;&nbsp;&nbsp;</span>
                                <input type="text" class="form-control" name="phone" id="phone" placeholder="0~1">
                            </div>
                            <br>

                            <div class="input-group " >
                                <span class="input-group-addon">描述&nbsp;&nbsp;&nbsp;</span>
                                <input type="text" class="form-control" name="description" id="description" >
                            </div>
                            <br>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                            <button type="button" class="btn btn-info add-btn">保存</button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal fade" id="confirmModal" tabindex="-1" role="dialog" aria-labelledby="confirmModal" aria-hidden="false" data-keyboard="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                            <h4 class="modal-title" id="confirmModalLabel" ></h4>
                            <label id="hidden_id" hidden></label>
                        </div>
                        <div class="modal-body" id="confirmModalBody"></div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal" id="cancelModalBtn">关闭</button>
                            <button type="button" class="btn btn-info" data-dismiss="modal" id="confirmModalBtn">确定</button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal fade" id="confirmModal" tabindex="-1" role="dialog" aria-labelledby="confirmModal" aria-hidden="false" data-keyboard="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                            <h4 class="modal-title" id="confirmModalLabel" ></h4>
                            <label id="hidden_id" hidden></label>
                        </div>
                        <div class="modal-body" id="confirmModalBody"></div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal" id="cancelModalBtn">关闭</button>
                            <button type="button" class="btn btn-info" data-dismiss="modal" id="confirmModalBtn">确定</button>
                        </div>
                    </div>
                </div>
            </div>

            <div id="alertSuccess" z-index="1001" class="alert alert-success text-center fade in" style="position: fixed; right: 0px;top: 0px;display: none; height: 50px;" >
                <strong id="successText"></strong>
            </div>
            <div id="alertFailure" z-index="1001" class="alert alert-danger text-center fade in" style="position: fixed; right: 0px;top: 0px;display: none;height: 50px;" >
                <strong id="failureText" ></strong>
            </div>
        </section>

    </div>
<#--row-->

<#--content-wrapper-->

<@netCommon.commonScript />
    <script src="${request.contextPath}/plugins/bootstrap-fixed-columns/bootstrap-table-fixed-columns.js"></script>
<script src="${request.contextPath}/js/userManage.js"></script>

</body>

</html>


