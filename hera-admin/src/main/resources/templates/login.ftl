<!DOCTYPE html>
<html>
<head lang="en">
    <title>调度中心</title>
    <base href="${request.contextPath}" id="baseURl">
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/adminlte/plugins/iCheck/square/green.css">
    <style>
        .error{
            color: red;
        }
    </style>
</head>
<body class="hold-transition login-page">
<div class="login-box">
    <div class="login-logo">
        <a href="#"><b>赫拉任务调度系统</b></a>
    </div>
    <!-- /.login-logo -->
    <div class="login-box-body">
        <ul class="nav nav-tabs" role="tablist" id="menu-tab">
            <li class="active"><a href="#tab-login" role="tab" data-toggle="tab">登录</a></li>
            <li ><a href="#tab-middle" role="tab-register" data-toggle="tab">注册</a></li>
        </ul>
        <div class="tab-content">
            <div class="tab-pane active" id="tab-login">
                <div class="row">
                    <div class="col-lg-12">
                        <p class="login-box-msg">输入登陆账号密码</p>

                        <form id="loginForm" method="post">
                            <div class="form-group has-feedback">
                                <input type="text" name="userName"class="form-control" placeholder="请输入登录账号">
                                <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
                            </div>
                            <div class="form-group has-feedback">
                                <input type="password" name="password"class="form-control" placeholder="请输入登录密码">
                                <span class="glyphicon glyphicon-lock form-control-feedback"></span>
                            </div>

                            <div class="row">
                                <div class="col-xs-8">
                                    <div class="checkbox icheck">
                                        <label>
                                            <input type="checkbox">记住我
                                        </label>
                                    </div>
                                </div>
                                <!-- /.col -->
                                <div class="col-xs-4">
                                    <button type="submit" class="btn btn-primary btn-block btn-flat">登陆</button>
                                </div>

                                <!-- /.col -->
                            </div>
                        </form>

                    </div>
                </div>
            </div>
            <div class="tab-pane" id="tab-middle">
                <div class="row">
                    <div class="col-lg-12">
                        <p class="login-box-msg text-center" style="color: #3dff0e;">提醒：hera账号与hive账号必须相同</p>

                        <form action="" type="post" id="registerForm">
                            <fieldset>
                                <div class="form-group">
                                    <label for="name">账号</label>
                                    <input type="text" class="form-control" name="name" id="name" >
                                </div>
                                <div class="form-group">
                                    <label for="name">密码</label>
                                    <input type="password" class="form-control" name="password" id="password" >
                                </div>
                                <div class="form-group">
                                    <label for="name">确认密码</label>
                                    <input type="password" class="form-control" name="confirmPassword" id="confirmPassword">
                                </div>
                                <div class="form-group">
                                    <label for="name">邮箱</label>
                                    <input type="text" class="form-control" name="email" id="email" >
                                </div>
                                <div class="form-group">
                                    <label for="name">手机</label>
                                    <input type="text" class="form-control" name="phone" id="phone" >
                                </div>
                                <div class="form-group">
                                    <label for="name">账号描述</label>
                                    <input type="text" class="form-control" name="description" id="description" >
                                </div>
                                <input type="reset" class="btn btn-default pull-left" value="重置">
                                <input type="submit" class="btn btn-primary pull-right" value="注册">
                            </fieldset>
                        </form>

                    </div>
                </div>
            </div>
        </div>


    </div>
    <!-- /.login-box-body -->
</div>
<!-- /.login-box -->

<@netCommon.commonScript />
<script src="${request.contextPath}/plugins/jquery/jquery.validate.min.js"></script>
<script src="${request.contextPath}/plugins/jquery/jquery.metadata.js"></script>
<script src="${request.contextPath}/adminlte/plugins/iCheck/icheck.min.js"></script>
<script src="${request.contextPath}/plugins/jquery/messages_zh.js"></script>
<script src="${request.contextPath}/plugins/jquery/md5.js"></script>
<script src="${request.contextPath}/js/login.js"></script>
</body>
</html>
