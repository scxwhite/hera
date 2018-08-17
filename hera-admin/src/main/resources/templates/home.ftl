<!DOCTYPE html>
<html>
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
</head>


<body class="hold-transition skin-blue-light sidebar-mini">
<div class="wrapper">

    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "index" />

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->

        <!-- Main content -->
        <section class="content container-fluid">

            <!--------------------------
              | Your Page Content Here |
              -------------------------->
            <div class="row">
                <div class="col-lg-3 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-aqua">
                        <div class="inner">
                            <h3 id="allJobsNum">&nbsp;</h3>

                            <p>今日总任务数</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-stats-bars"></i>
                        </div>
                        <a href="${request.contextPath}/jobDetail" class="small-box-footer">More info <i
                                class="fa fa-arrow-circle-right"></i></a>
                    </div>
                </div>
                <!-- ./col -->
                <div class="col-lg-3 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-maroon-gradient">
                        <div class="inner">
                            <h3 id="failedNum">&nbsp;</h3>
                            <p>失败任务数</p>
                        </div>
                        <div class="icon">
                            <i class="ion ion-stats-bars"></i>
                        </div>
                        <a href="${request.contextPath}/jobDetail" class="small-box-footer">More info <i
                                class="fa fa-arrow-circle-right"></i></a>
                    </div>
                </div>

                <div class="col-lg-3 col-xs-6">
                    <!-- small box -->
                    <div class="small-box bg-yellow">
                        <div class="inner">
                            <h3 id="queueNum">&nbsp;</h3>
                            <p>任务队列详情</p>
                        </div>
                        <div class="icon"
                        <i class="ion ion-stats-bars"></i>
                    </div>
                    <a href="${request.contextPath}/jobDetail" class="small-box-footer">More info <i
                            class="fa fa-arrow-circle-right"></i></a>
                </div>
            </div>
    </div>

    <div class="row">
        <div class="col-lg-4">
            <div id="jobStatus" style="height: 500px"></div>
        </div>
        <div class="col-lg-8">
            <div id="lineJobStatus" style="height: 500px"></div>
        </div>

    </div>
    <div class="row">
        <div class="col-lg-12">
            <div id="jobTop" style="height: 500px"></div>
        </div>
    </div>

    </section>
    <!-- /.content -->
</div>
<!-- /.content-wrapper -->

<!-- footer -->
	<@netCommon.commonFooter />

</div>
<!-- ./wrapper -->
<@netCommon.commonScript />
<script src="${request.contextPath}/adminlte/plugins/daterangepicker/moment.min.js"></script>
<script src="${request.contextPath}/adminlte/plugins/daterangepicker/daterangepicker.js"></script>
<script src="${request.contextPath}/plugins/echarts/echarts.common.min.js"></script>
<script src="${request.contextPath}/plugins/echarts/echarts.js"></script>
<script type="text/javascript">
    $(function () {
        var option = {
            title: {
                show: true
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            legend: {
                data: [],
                selected: {}
            },
            toolbox: {
                show: false,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar', 'stack', 'tiled']},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            calculable: true,
            xAxis: [],
            yAxis: [],
            series: []
        };


        //job top ten
        jQuery.ajax({
            url: base_url + "/homePage/findJobRunTimeTop10",
            type: "get",
            success: function (data) {
                if (data.success == false) {
                    return;
                }
                initJobTopTen(data.data);
            }
        })

        jQuery.ajax({
            url: base_url + "/homePage/findAllJobStatus",
            type: "get",
            success: function (data) {
                if (data.success == false) {
                    return;
                }
                initPieJobStatus(data.data)
            }
        })


        jQuery.ajax({
            url: base_url + "/homePage/findAllJobStatusDetail",
            type: "get",
            success: function (data) {
                if (data.success == false) {
                    alert(data.message);
                    return;
                }
                initLineJobStatus(data.data);
            }
        })

        function initLineJobStatus(data) {
            initOption();
            option.title.text = "任务执行状态";
            option.title.subtext = data['xAxis'][0] + "~" + data['xAxis'][data['xAxis'].length - 1];
            option.xAxis.boundaryGap = false;
            option.xAxis.type = "category";
            option.xAxis.data = data['xAxis'];
            option.legend.data = ['成功任务数', '失败任务数', '总任务数', '运行成功次数', '运行失败次数', '运行总次数'];
            option.legend.selected = {
                '成功任务数': false,
                '失败任务数': false,
                '总任务数': false
            }
            option.yAxis = {
                type: 'value',
                axisLabel: {
                    formatter: '{value}'
                }
            };
            var failedJobs = new Array();   //成功任务数
            var successJob = new Array();   //失败任务数
            var allJob = new Array();       //总任务数
            var runSuccess = new Array();   //运行成功次数
            var runFailed = new Array();   //运行失败次数
            var allRun = new Array();       //运行总次数
            var find;
            data['xAxis'].forEach(function (xAxis, index) {
                find = false
                data['runFailed'].forEach(function (job, jobIndex) {
                    debugger
                    if (job.curDate == xAxis) {
                        find = true;
                        runFailed[index] = job.num;
                    }
                })
                if (find == false) runFailed[index] = 0;
                else find = false;
                data['runSuccess'].forEach(function (job, jobIndex) {
                    debugger
                    if (job.curDate == xAxis) {
                        find = true;
                        runSuccess[index] = job.num;
                    }
                })
                if (find == false) runSuccess[index] = 0;
                else find = false;
                allRun[index] = runSuccess[index] + runFailed[index];

                if (data.hasOwnProperty(xAxis)) {
                    data[xAxis].forEach(function (val) {
                        if (val.status == 'success') {
                            successJob[index] = val.num;
                        } else if (val.status == 'failed') {
                            failedJobs[index] = val.num;
                        }
                    })
                }
                if (successJob[index] == undefined || successJob[index] == null) {
                    successJob[index] = 0;
                }
                if (failedJobs[index] == undefined || failedJobs[index] == null) {
                    failedJobs[index] = 0;
                }
                allJob[index] = failedJobs[index] + successJob[index];
            })
            option.series[0] = new lineRow('运行成功次数', runSuccess);
            option.series[1] = new lineRow('运行失败次数', runFailed);
            option.series[2] = new lineRow('运行总次数', allRun);
            option.series[3] = new lineRow('成功任务数', successJob);
            option.series[4] = new lineRow('失败任务数', failedJobs);
            option.series[5] = new lineRow('总任务数', allJob);

            var myChart = echarts.init(document.getElementById('lineJobStatus'), 'macarons');
            myChart.setOption(option)
        }

        function initPieJobStatus(status) {
            initOption();
            option.title.text = "实时任务状态";
            option.title.subtext = getNowFormatDate(0);
            option.title.x = "center";
            option.xAxis = [];
            option.yAxis = [];
            option.tooltip.trigger = "item";
            option.tooltip.formatter = "{a} <br/>{b} : {c} ({d}%)";
            option.series[0] = {
                name: '运行状态',
                type: 'pie',
                radius: '65%',
                startAngle: 60,
                minAngle: 3,
                center: ['50%', '50%'],
                data: []
            };
            var legend = new Array();
            var allJobsNum = 0;
            var filedNum = 0;
            status.forEach(function (sta, index) {
                legend[index] = sta.status;
                option.series[0].data[index] = new pieRow(sta.status, sta.num)
                if (sta.status == "failed") {
                    filedNum = sta.num;
                }
                allJobsNum += sta.num;
            });
            $('#failedNum').text(filedNum);
            $('#allJobsNum').text(allJobsNum);
            option.legend.data = legend;
            option.legend.orient = "vertical";
            option.legend.x = "left";

            var myChart = echarts.init(document.getElementById('jobStatus'), 'macarons');
            myChart.setOption(option)

            myChart.on('click', function (param) {
                if (param.data.name == "failed" || param.data.name == "success" || param.data.name == "running") {
                    location.href = base_url + "/jobDetail";
                }
            })
        }

        function initJobTopTen(jobs) {
            initOption();
            var myChart = echarts.init(document.getElementById('jobTop'), 'macarons');
            option.title.text = '任务时长TOP10';
            option.title.subtext = getNowFormatDate(0);
            option.title.x = 'center';
            option.legend.data = ['昨日', '今日'];
            option.legend.orient = 'vertical';
            option.legend.x = 'right';
            var yesterday = new Array();
            var today = new Array();
            var xAxis = new Array();
            jobs.forEach(function (job, index) {
                today[index] = job.jobTime;
                yesterday[index] = job.yesterdayTime;
                xAxis[index] = job.jobId;
            })
            option.xAxis = new yAxis(xAxis);
            option.xAxis.name = "任务ID";
            option.yAxis = {
                type: 'value',
                name: '分钟'
            };
            option.series[0] = new row("昨日", yesterday, 'bar', null);
            option.series[1] = new row("今日", today, 'bar', null);
            myChart.setOption(option);

            myChart.on('click', function (param) {
                if (param.seriesName == "今日" || param.seriesName == "昨日") {
                    // window.open(env + "/#App:schedule/jobmanager:sharedjob/jobdisplay:job-"+param.name) ;
                }
            })

        }

        function initOption() {
            option.series = [];
            option.title = {};
            option.legend = {};
            option.xAxis = {};
            option.tooltip = {
                trigger: 'axis',
                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            };
        }

    })

    function row(name, data, type, stack) {
        this.name = name;
        this.data = data;
        this.type = type;
        this.stack = stack;
        this.itemStyle = {normal: {label: {show: false, position: 'insideRight'}}};
    }

    function pieRow(name, value) {
        this.value = value;
        this.name = name;
    }

    function yAxis(data) {
        this.data = data;
        this.type = 'category';
    }

    function lineRow(name, data) {
        this.name = name;
        this.type = 'line';
        this.data = data;
        this.markPoint = {
            data: [
                {type: 'max', name: '最大值'},
                {type: 'min', name: '最小值'}
            ]
        };
    }

    function getNowFormatDate(subDays) {
        var date = new Date();
        date.setDate(date.getDate() + subDays);
        var seperator1 = "-";
        var year = date.getFullYear();
        var month = date.getMonth() + 1;
        var strDate = date.getDate();
        if (month >= 1 && month <= 9) {
            month = "0" + month;
        }
        if (strDate >= 0 && strDate <= 9) {
            strDate = "0" + strDate;
        }
        var currentdate = year + seperator1 + month + seperator1 + strDate;
        return currentdate;
    }
</script>


</body>
</html>
