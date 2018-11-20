$(function () {
    $('#home').addClass('active');

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

    var ramOption = {
        tooltip : {
            formatter: "{a} <br/>{b} : {c}%"
        },
        series: [
            {
                name: '业务指标',
                type: 'gauge',
                detail: {formatter:'{value}%'},
                data: [{value: 50, name: '内存使用率'}]
            }
        ]
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
    var info = {};
    //内存仪表板
    function initMachine() {
        $.ajax({
            url: base_url + '/homePage/getAllWorkInfo',
            type: 'get',
            success: function (data) {
                //机器选择
                for ( var i in data ){
                    $('#machineList').append('<option value="'+i+'">'+i+'</option>');
                }
                info=data;
                var machine = $('#machineList').val();
                initInfo(data[machine]);
            }
        })
    }
    function initInfo(machine){
        //系统概况
        ramOption.series[0].data[0].value = parseFloat(machine.osInfo.mem);
        var myChart=echarts.init(document.getElementById('ramGauge'));
        myChart.setOption(ramOption, true);

        $('#userPercent').easyPieChart({
            animate: 1000,
            barColor: '#011935',
            onStep: function(from, to, percent) {
                $(this.el).find('.percent').text(Math.round(percent));
            },
            size:200,
            lineWidth:10,
            scaleColor: '#666'
        });
        $('#userPercent').data('easyPieChart').update(0);
        $('#userPercent').data('easyPieChart').update(parseFloat(machine.osInfo.user));

        $('#SysPercent').easyPieChart({
            animate: 1000,
            barColor: '#009FAB',
            onStep: function(from, to, percent) {
                $(this.el).find('.percent').text(Math.round(percent));
            },
            size:200,
            lineWidth:10,
            scaleColor: '#666'
        });
        $('#SysPercent').data('easyPieChart').update(0);
        $('#SysPercent').data('easyPieChart').update(parseFloat(machine.osInfo.system));

        $('#CPUPercent').easyPieChart({
            animate: 1000,
            barColor: '#1DB0B8',
            onStep: function(from, to, percent) {
                $(this.el).find('.percent').text(Math.round(percent));
            },
            size:200,
            lineWidth:10,
            scaleColor: '#666'
        });
        console.log($('#CPUPercent'))
        $('#CPUPercent').data('easyPieChart').update(0);
        $('#CPUPercent').data('easyPieChart').update(parseInt(machine.osInfo.cpu));

        $('#SwapPercent').easyPieChart({
            animate: 1000,
            barColor: '#008891',
            onStep: function(from, to, percent) {
                $(this.el).find('.percent').text(Math.round(percent));
            },
            size:200,
            lineWidth:10,
            scaleColor: '#666'
        });
        $('#SwapPercent').data('easyPieChart').update(0);
        $('#SwapPercent').data('easyPieChart').update(parseInt(machine.osInfo.swap));

        //进程监控
        $('#processMonitor').bootstrapTable({
            columns:[{
                field: 'pid',
                title: 'PID',
                width: 150
            },{
                field: 'user',
                title: 'USER',
                width: 150
            },{
                field: 'viri',
                title: 'VIRI',
                width: 150
            },{
                field: 'res',
                title: 'RES',
                width: 150
            },{
                field: 'cpu',
                title: 'CPU',
                formatter:function(value,row,index){
                    value = parseFloat(value)
                    value*=10;
                    return '<div class="progress progress-xs"><div class="progress-bar progress-bar-primary progress-bar-striped" role="progressbar" aria-valuenow="'+value+'" aria-valuemin="0" aria-valuemax="10" style="width: '+value+'%"><span class="sr-only">40% Complete (success)</span></div></div>'
                },
                width: 150

            },{
                field: 'mem',
                title: 'MEM',
                formatter:function(value,row,index){
                    return '<div class="progress progress-xs"><div class="progress-bar progress-bar-warning progress-bar-striped" role="progressbar" aria-valuenow="'+value+'" aria-valuemin="0" aria-valuemax="100" style="width: '+value+'"><span class="sr-only">40% Complete (success)</span></div></div>'
                },
                width: 150
            },{
                field: 'time',
                title: 'TIME',
                width: 150
            },{
                field: 'command',
                title: 'COMMAND',
                width: 150
            }],
            data: machine.processMonitor
        })
        $('#processMonitor').bootstrapTable('hideLoading');
        //机器信息
        var machineInfo =machine.machineInfo;
        $('#machineInfo').empty();
        for(var i =0;i<machineInfo.length;i++){
            $('#machineInfo').append('<div class="machine"><p class="filesystem">'+machineInfo[i].filesystem+'(已用'+machineInfo[i].used+'/可用'+machineInfo[i].avail+')</p><div class="progress progress-xs"><div class="progress-bar progress-bar-warning progress-bar-striped" role="progressbar" aria-valuenow="'+machineInfo[i].use+'" aria-valuemin="0" aria-valuemax="100" style="width: '+machineInfo[i].use+'%"><span class="sr-only"></span></div></div></div>')
        }
    }
    initMachine()
    $('#machineList').change(function (e) {
        e.stopPropagation();
        var machine = $(this).val();
        machine = info[machine];
        console.log(machine)
        initInfo(machine);
    })

    function initLineJobStatus(data) {
        initOption();
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
                if (job.curDate == xAxis) {
                    find = true;
                    runFailed[index] = job.num;
                }
            })
            if (find == false) runFailed[index] = 0;
            else find = false;
            data['runSuccess'].forEach(function (job, jobIndex) {
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

        var myChart = echarts.init(document.getElementById('lineJobStatus'));
        myChart.setOption(option)
    }

    function initPieJobStatus(status) {
        initOption();
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
            // color: ['#F38181','#FCE38A','#95E1D3','#EAFFD0']
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

        var myChart = echarts.init(document.getElementById('jobStatus'));
        myChart.setOption(option)

        myChart.on('click', function (param) {
            if (param.data.name == "failed" || param.data.name == "success" || param.data.name == "running") {
                location.href = base_url + "/jobDetail";
            }
        })
    }

    function initJobTopTen(jobs) {
        initOption();
        var myChart = echarts.init(document.getElementById('jobTop'));
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
    if (name === '今日'){
        this.itemStyle = {
            normal: {
                label: {
                    show: false, position: 'insideRight'
                },
                // color:'#F38181'
            }
        };
    } else {
        this.itemStyle = {
            normal: {
                label: {
                    show: false, position: 'insideRight'
                },
                // color:'#95E1D3'
            }
        };
    }
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
    // switch (name) {
    //     case '运行成功次数':
    //         this.itemStyle={
    //         normal:{color:'#F38181'}
    //         }
    //         break;
    //     case '运行失败次数':
    //         this.itemStyle={
    //             normal:{color:'#FCE38A'}
    //         }
    //         break;
    //     case '运行总次数':
    //         this.itemStyle={
    //             normal:{color:'#95E1D3'}
    //         }
    //         break;
    //     case '成功任务数':
    //         this.itemStyle={
    //             normal:{color:'#00cae0'}
    //         }
    //         break;
    // }

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