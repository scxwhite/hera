<#macro commonStyle>
<#-- favicon -->
    <link rel="shortcut icon" type="image/ico" href="${request.contextPath}/static/images/favicon.png"/>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <!-- Tell the browser to be responsive to screen width -->
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link href="${request.contextPath}/static/adminlte/bootstrap/css/bootstrap.min.css" rel="stylesheet">

    <link rel="stylesheet"
          href="${request.contextPath}/static/adminlte/plugins/font-awesome-4.5.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/dist/css/AdminLTE.css">
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/dist/css/skins/_all-skins.css">
    <link rel="stylesheet" href="${request.contextPath}/static/plugins/ionicons-2.0.1/css/ionicons.min.css">
    <link rel="stylesheet" href="${request.contextPath}/static/plugins/layui/css/layui.css">
    <link rel="stylesheet" href="${request.contextPath}/static/css/common.css">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>

    <![endif]-->
    <!-- pace -->
</#macro>


<#macro commonScript>
    <script src="${request.contextPath}/static/adminlte/plugins/jQuery/jquery-2.2.3.min.js"></script>
    <script src="${request.contextPath}/static/adminlte/bootstrap/js/bootstrap.min.js"></script>
    <script src="${request.contextPath}/static/plugins/layui/layui.js"></script>
    <script src="${request.contextPath}/static/adminlte/dist/js/respond.min.js"></script>
    <script src="${request.contextPath}/static/adminlte/dist/js/html5shiv.min.js"></script>

    <script src="${request.contextPath}/static/adminlte/dist/js/app.min.js"></script>

    <script src="${request.contextPath}/static/js/common.js"></script>

    <script>var base_url = '${request.contextPath}';</script>

    <script>var screenHeight = document.body.clientHeight</script>
    <script>
        $.get(base_url + '/isAdmin', function (data) {
            if (data.data === true) {
                $('#sysManager').css("display", "block");
            } else {
                $('#sysManager').css("display", "none");
            }
            $('#welcome').text(localStorage.getItem("ssoName"));
        });

    </script>

</#macro>

<#macro commonHeader>
    <header class="main-header">
        <a href="${request.contextPath}/home" class="logo">
            <span class="logo-mini"><b>赫拉</b></span>
            <span class="logo-lg"><b>赫拉任务调度系统</b></span>
        </a>
        <nav class="navbar navbar-static-top" role="navigation">
            <a href="#" class="sidebar-toggle" data-toggle="offcanvas" role="button"><span
                        class="sr-only">切换导航</span></a>
            <div class="navbar-custom-menu">
                <ul class="nav navbar-nav">
                    <li class="dropdown user user-menu">
                        <a class="dropdown-toggle text-center" data-toggle="dropdown" aria-expanded="false">
                            <span class="hidden-xs" id="welcome">欢迎你:</span>
                        </a>
                    </li>
                    <li class="dropdown user user-menu">
                        <a id="logoutBtn" class="dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                            <span class="hidden-xs">注销</span>
                        </a>
                    </li>
                </ul>
            </div>
        </nav>
    </header>
</#macro>

<#macro commonLeft pageName >
    <!-- Left side column. contains the logo and sidebar -->
    <aside class="main-sidebar height-self">
        <!-- sidebar: style can be found in sidebar.less -->
        <section class="sidebar">
            <ul class="sidebar-menu tree">
                <li class="treeview menu-closed" id="home">
                    <a href="${request.contextPath}/home">
                        <i class="fa fa-dashboard"></i> <span>首页</span>
                    </a>
                </li>
                <li class="nav-click" id="machineInfoMenu"><a
                            href="${request.contextPath}/machineInfo"><i class="fa fa-book"></i> <span>机器组监控</span></a>
                </li>


                <li class="treeview menu-closed my-tree" id="sysManager" style="display: none">
                    <a href="#">
                        <i class="fa fa-folder"></i> <span>系统管理</span>
                        <span class="pull-right-container">
                          <i class="fa fa-angle-left pull-right"></i>
                        </span>
                    </a>
                    <ul class="treeview-menu">
                        <li id="userManage"><a href="${request.contextPath}/userManage"><i
                                        class="fa fa-circle-o"></i>
                                用户管理</a></li>
                        <li id="jobDag"><a href="${request.contextPath}/jobMonitor"><i
                                        class="fa fa-circle-o"></i>监控管理</a></li>
                        <li id="hostGroupManage"><a href="${request.contextPath}/hostGroupManage"><i
                                        class="fa fa-circle-o"></i>机器组管理</a>
                        </li>
                        <li id="workManage"><a href="${request.contextPath}/workManage"><i
                                        class="fa fa-circle-o"></i>worker管理</a>
                        </li>
                    </ul>
                </li>

                <li class=" treeview menu-closed my-tree" id="jobManage">
                    <a href="#">
                        <i class="fa fa-folder"></i> <span>任务管理</span>
                        <span class="pull-right-container">
                          <i class="fa fa-angle-left pull-right"></i>
                        </span>
                    </a>
                    <ul class="treeview-menu">
                        <li class="" id="jobDetailMenu"><a href="${request.contextPath}/jobDetail"><i
                                        class="fa fa-circle-o"></i>任务详情</a>
                        </li>
                        <li class="" id="jobDag"><a href="${request.contextPath}/jobDag"><i
                                        class="fa fa-circle-o"></i>任务依赖</a></li>

                        <li class="" id="jobSearch"><a href="${request.contextPath}/jobSearch"><i
                                        class="fa fa-circle-o"></i>任务搜索</a></li>

                        <li id="record"><a href="${request.contextPath}/record"><i
                                        class="fa fa-circle-o"></i>日志记录</a></li>

                        <li id="rerun"><a href="${request.contextPath}/rerun"><i
                                        class="fa fa-circle-o"></i>任务重跑</a></li>
                    </ul>
                </li>


                <li class="nav-click" id="developManage"><a
                            href="${request.contextPath}/developCenter"><i class="fa fa-book"></i> <span>开发中心</span></a>
                </li>
                <li class="nav-click" id="scheduleManage"><a
                            href="${request.contextPath}/scheduleCenter"><i class="fa fa-edit"></i>
                        <span>调度中心</span></a>
                </li>

                <li class="nav-click" id="advice"><a
                            href="${request.contextPath}/adviceController"><i class="fa fa-bug"></i> <span>建议&留言</span></a>
                </li>
                <li class="nav-click" id="advice"><a
                            href="${request.contextPath}/help"><i class="fa fa-paper-plane"></i> <span>帮助文档</span></a>
                </li>

            </ul>
        </section>
        <!-- /.sidebar -->
    </aside>
</#macro>

<#macro commonControl >
    <!-- Control Sidebar -->
    <aside class="control-sidebar control-sidebar-dark">
        <!-- Create the tabs -->
        <ul class="nav nav-tabs nav-justified control-sidebar-tabs">
            <li class="active"><a href="#control-sidebar-home-tab" data-toggle="tab"><i class="fa fa-home"></i></a></li>
            <li><a href="#control-sidebar-settings-tab" data-toggle="tab"><i class="fa fa-gears"></i></a></li>
        </ul>
        <!-- Tab panes -->
        <div class="tab-content">
            <!-- Home tab content -->
            <div class="tab-pane active" id="control-sidebar-home-tab">
                <h3 class="control-sidebar-heading">近期活动</h3>
                <ul class="control-sidebar-menu">
                    <li>
                        <a href="javascript::;">
                            <i class="menu-icon fa fa-birthday-cake bg-red"></i>
                            <div class="menu-info">
                                <h4 class="control-sidebar-subheading">张三今天过生日</h4>
                                <p>2015-09-10</p>
                            </div>
                        </a>
                    </li>
                    <li>
                        <a href="javascript::;">
                            <i class="menu-icon fa fa-user bg-yellow"></i>
                            <div class="menu-info">
                                <h4 class="control-sidebar-subheading">Frodo 更新了资料</h4>
                                <p>更新手机号码 +1(800)555-1234</p>
                            </div>
                        </a>
                    </li>
                    <li>
                        <a href="javascript::;">
                            <i class="menu-icon fa fa-envelope-o bg-light-blue"></i>
                            <div class="menu-info">
                                <h4 class="control-sidebar-subheading">Nora 加入邮件列表</h4>
                                <p>nora@example.com</p>
                            </div>
                        </a>
                    </li>
                    <li>
                        <a href="javascript::;">
                            <i class="menu-icon fa fa-file-code-o bg-green"></i>
                            <div class="menu-info">
                                <h4 class="control-sidebar-subheading">001号定时作业调度</h4>
                                <p>5秒前执行</p>
                            </div>
                        </a>
                    </li>
                </ul>
                <!-- /.control-sidebar-menu -->
            </div>
            <!-- /.tab-pane -->

            <!-- Settings tab content -->
            <div class="tab-pane" id="control-sidebar-settings-tab">
                <form method="post">
                    <h3 class="control-sidebar-heading">个人设置</h3>
                    <div class="form-group">
                        <label class="control-sidebar-subheading"> 左侧菜单自适应
                            <input type="checkbox" class="pull-right" checked>
                        </label>
                        <p>左侧菜单栏样式自适应</p>
                    </div>
                    <!-- /.form-group -->

                </form>
            </div>
            <!-- /.tab-pane -->
        </div>
    </aside>
    <!-- /.control-sidebar -->
    <!-- Add the sidebar's background. This div must be placed immediately after the control sidebar -->
    <div class="control-sidebar-bg"></div>
</#macro>

<#macro commonFooter >
    <footer class="main-footer">
        Powered by <b>hera</b> 2.4.4
        <div class="pull-right hidden-xs">
            <strong>Copyright &copy; 2018-${.now?string('yyyy')} &nbsp;
                <a href="https://github.com/scxwhite/hera" target="_blank">github/scx_white</a>
                &nbsp;
                <a href="" target="_blank"></a>
            </strong><!-- All rights reserved. -->
        </div>
    </footer>
</#macro>

