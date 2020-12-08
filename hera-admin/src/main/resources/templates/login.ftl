<!DOCTYPE html>
<html>
<head lang="en">
    <title>赫拉任务调度系统</title>
    <base href="${request.contextPath}" id="baseURl">
    <#import "/common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/plugins/iCheck/square/green.css">

    <!-- 页面logo设置 start-->
    <link rel="icon" type="image/png" href="${request.contextPath}/static/images/favicon.ico">
    <!-- 页面logo设置 end -->
    <!-- 页面样式设置，使用bootstrap前端框架 start-->
    <link rel="stylesheet" href="${request.contextPath}/static/css/login.css"/>
    <!-- 页面样式设置，使用bootstrap前端框架 end-->
    <!-- 引入JQuery库 start -->
    <style>
        .error {
            color: red;
        }
    </style>
</head>


<body>


<div class="login box box-primary">
    <#--<div class="box png">-->
    <div><h3 align="center" class="title">赫拉任务调度系统</h3></div>
    <div class="input">
        <div class="log">
            <ul class="nav nav-tabs" role="tablist" id="menu-tab">
                <li class="active"><a href="#tab-login" role="tab" data-toggle="tab">登录</a></li>
                <li><a href="#tab-middle" role="tab-register" data-toggle="tab" onclick="loadGroups()"> 注册</a></li>
            </ul>

            <div class="tab-content">

                <div class="tab-pane active" id="tab-login">
                    <form id="loginForm" method="post">
                        <div class="name" style="margin: 20px 10px">
                            <label>用户名</label><input type="text" class="text" name="userName" placeholder="邮箱前缀(sucx@aliyun.com,前缀为sucx)"/>
                        </div>
                        <div class="pwd" style="margin: 20px 10px">
                            <label>密　码</label><input type="password" class="text" name="password" placeholder="密码"/>
                        </div>
                        <button type="submit" class="btn btn-primary btn-block btn-flat" style="border-radius: 4px">
                            登陆
                        </button>
                    </form>
                </div>

                <div class="tab-pane" id="tab-middle">
                    <p class="login-box-msg text-center">提醒：hera账号与hive账号必须相同</p>

                    <form action="" type="post" id="registerForm">
                        <fieldset>
                            <div class="form-group">
                                <label for="name">邮箱</label>
                                <input type="text" class="form-control" name="email" id="email">
                            </div>
                            <div class="form-group">
                                <label for="name">密码</label>
                                <input type="password" class="form-control" name="password"
                                       id="password">
                            </div>
                            <div class="form-group">
                                <label for="name">确认密码</label>
                                <input type="password" class="form-control" name="confirmPassword"
                                       id="confirmPassword">
                            </div>
                            <div class="form-group">
                                <label for="name">手机</label>
                                <input type="text" class="form-control" name="phone" id="phone">
                            </div>
                            <div class="form-group">
                                <label for="name">工号</label>
                                <input type="text" class="form-control" name="jobNumber"
                                       id="jobNumber">
                            </div>

                            <div class="form-group">
                                <label for="name">所在部门</label>
                                <select class="form-control" name="ssoGroup" id="ssoGroup">

                                </select>
                            </div>
                            <input type="reset" class="btn btn-default pull-left" value="重置">
                            <input type="submit" class="btn btn-primary pull-right" value="注册">
                        </fieldset>
                    </form>

                </div>

            </div>

        </div>
    </div>
    <#--</div>-->

</div>

<!-- /.login-box -->

<@netCommon.commonScript />
<script src="${request.contextPath}/static/plugins/jquery/jquery.validate.min.js"></script>
<script src="${request.contextPath}/static/plugins/jquery/jquery.metadata.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/iCheck/icheck.min.js"></script>
<script src="${request.contextPath}/static/plugins/jquery/messages_zh.js"></script>
<script src="${request.contextPath}/static/plugins/jquery/md5.js"></script>
<script src="${request.contextPath}/static/js/login.js"></script>
<script src="${request.contextPath}/static/js/fun.base.js"></script>
</body>
</html>
