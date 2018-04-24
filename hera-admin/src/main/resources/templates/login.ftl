<!DOCTYPE html>
<html>
<head lang="en">
    <title>调度中心</title>
    <base href="${request.contextPath}" id="baseURl">
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/adminlte/plugins/iCheck/square/green.css">
</head>
<body class="hold-transition login-page">
<div class="login-box">
    <div class="login-logo">
        <a href="#"><b>赫拉任务调度系统</b></a>
    </div>
    <!-- /.login-logo -->
    <div class="login-box-body">
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
    <!-- /.login-box-body -->
</div>
<!-- /.login-box -->

<@netCommon.commonScript />
<script src="${request.contextPath}/plugins/jquery/jquery.validate.min.js"></script>
<script src="${request.contextPath}/adminlte/plugins/iCheck/icheck.min.js"></script>
<script src="${request.contextPath}/js/login.js"></script>
</body>
</html>
