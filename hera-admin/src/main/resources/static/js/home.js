var info = {
    "master_10.1.21.67":{
        "machineInfo":[
            {
                "filesystem":"/dev/vda1",
                "type":"ext4",
                "size":"79G",
                "used":"3.4G",
                "avail":"72G",
                "use":5,
                "mountedOn":"/"
            },
            {
                "filesystem":"/dev/vda1",
                "type":"ext4",
                "size":"79G",
                "used":"3.4G",
                "avail":"72G",
                "use":5,
                "mountedOn":"/"
            }
        ],
        "osInfo":{
            "user":0.3,
            "system":0.1,
            "mem":3,
            "cpu":99.6,
            "swap":100
        },
        "processMonitor":[
            {
                "pid":"6250",
                "user":"root",
                "viri":"1788m",
                "res":"46m",
                "cpu":"0.5%",
                "mem":"20%",
                "time":"13:01.92",
                "command":"cmf-agent"
            },
            {
                "pid":"6250",
                "user":"root",
                "viri":"1788m",
                "res":"46m",
                "cpu":"0.5%",
                "mem":"20%",
                "time":"13:01.92",
                "command":"cmf-agent"
            }
        ]
    },
    "work_10.1.28.26":{
        "machineInfo":[
            {
                "filesystem":"/dev/vda1",
                "type":"ext4",
                "size":"79G",
                "used":"3.4G",
                "avail":"72G",
                "use":5,
                "mountedOn":"/"
            },
            {
                "filesystem":"/dev/vda1",
                "type":"ext4",
                "size":"79G",
                "used":"3.4G",
                "avail":"72G",
                "use":5,
                "mountedOn":"/"
            }
        ],
        "osInfo":{
            "user":0.3,
            "system":0.1,
            "mem":3,
            "cpu":99.6,
            "swap":100
        },
        "processMonitor":[
            {
                "pid":"6250",
                "user":"root",
                "viri":"1788m",
                "res":"46m",
                "cpu":"0.5%",
                "mem":"20%",
                "time":"13:01.92",
                "command":"cmf-agent"
            },
            {
                "pid":"6250",
                "user":"root",
                "viri":"1788m",
                "res":"46m",
                "cpu":"0.5%",
                "mem":"20%",
                "time":"13:01.92",
                "command":"cmf-agent"
            }
        ]
    }
}
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
    //内存仪表板
    function initMachine() {
        // $.ajax()
        //机器选择
        for ( var i in info ){
            $('#machineList').append('<option value="'+i+'">'+i+'</option>');
        }
        var machine = $('#machineList').val();
        initInfo(info[machine]);
        // console.log(info[machine])
        function initInfo(machine){
            //系统概况
            ramOption.series[0].data[0].value = parseFloat(machine.osInfo.mem);
            var myChart=echarts.init(document.getElementById('ramGauge'));
            myChart.setOption(ramOption, true);
            var option1 = {
                value:parseFloat(machine.osInfo.user)*100,//百分比,必填
                name:'用户占用',//必填
                backgroundColor:null,
                color:['#38a8da','#d4effa'],
                fontSize:16,
                domEle:document.getElementById("userPercent")//必填
            },percentPie1 = new PercentPie(option1);
            percentPie1.init();
            var option2 = {
                    value:parseFloat(machine.osInfo.system)*100,//百分比,必填
                    name:'系统占用',//必填
                    backgroundColor:null,
                    color:['#38a8da','#d4effa'],
                    fontSize:16,
                    domEle:document.getElementById("SysPercent")//必填
                },
                percentPie2 = new PercentPie(option2);
            percentPie2.init();

            var option3 = {
                    value:parseInt(machine.osInfo.cpu),//百分比,必填
                    name:'CPU空闲',//必填
                    backgroundColor:null,
                    color:['#38a8da','#d4effa'],
                    fontSize:16,
                    domEle:document.getElementById("CPUPercent")//必填
                },
                percentPie3 = new PercentPie(option3);
            percentPie3.init();

            var option4 = {
                    value:parseInt(machine.osInfo.swap),//百分比,必填
                    name:'SWAP空闲',//必填
                    backgroundColor:null,
                    color:['#38a8da','#d4effa'],
                    fontSize:16,
                    domEle:document.getElementById("SwapPercent")//必填
                },
                percentPie4 = new PercentPie(option4);
            percentPie4.init();
            //进程监控
            $('#processMonitor').bootstrapTable({
                columns:[{
                    field: 'pid',
                    title: 'PID',
                    width: 100
                },{
                    field: 'user',
                    title: 'USER',
                    width: 100
                },{
                    field: 'viri',
                    title: 'VIRI',
                    width: 100
                },{
                    field: 'res',
                    title: 'RES',
                    width: 100
                },{
                    field: 'cpu',
                    title: 'CPU',
                    formatter:function(value,row,index){
                        value = parseFloat(value)
                        value*=10;
                        return '<div class="progress progress-xs"><div class="progress-bar progress-bar-primary progress-bar-striped" role="progressbar" aria-valuenow="'+value+'" aria-valuemin="0" aria-valuemax="10" style="width: '+value+'%"><span class="sr-only">40% Complete (success)</span></div></div>'
                    },
                    width: 100

                },{
                    field: 'mem',
                    title: 'MEM',
                    formatter:function(value,row,index){
                        return '<div class="progress progress-xs"><div class="progress-bar progress-bar-warning progress-bar-striped" role="progressbar" aria-valuenow="'+value+'" aria-valuemin="0" aria-valuemax="100" style="width: '+value+'"><span class="sr-only">40% Complete (success)</span></div></div>'
                    },
                    width: 100
                },{
                    field: 'time',
                    title: 'TIME',
                    width: 100
                },{
                    field: 'command',
                    title: 'COMMAND',
                    width: 100
                }],
                data: machine.processMonitor
            })
            //机器信息
            var myChart=echarts.init(document.getElementById('machinePie'));
            var option5 = {
                title: {
                    text: 'Customized Pie',
                    left: 'center',
                    top: 20,
                    textStyle: {
                        color: '#ccc'
                    }
                },

                tooltip : {
                    trigger: 'item',
                    formatter: "{a} <br/>{b} : {c} ({d}%)"
                },

                visualMap: {
                    show: false,
                    min: 80,
                    max: 600,
                    inRange: {
                        colorLightness: [0, 1]
                    }
                },
                series : [
                    {
                        name:'访问来源',
                        type:'pie',
                        radius : '55%',
                        center: ['50%', '50%'],
                        data:[
                            {value:335, name:'直接访问'},
                            {value:310, name:'邮件营销'},
                            {value:274, name:'联盟广告'},
                            {value:235, name:'视频广告'},
                            {value:400, name:'搜索引擎'}
                        ].sort(function (a, b) { return a.value - b.value; }),
                        roseType: 'radius',
                        label: {
                            normal: {
                                textStyle: {
                                    color: 'rgba(255, 255, 255, 0.3)'
                                }
                            }
                        },
                        labelLine: {
                            normal: {
                                lineStyle: {
                                    color: 'rgba(255, 255, 255, 0.3)'
                                },
                                smooth: 0.2,
                                length: 10,
                                length2: 20
                            }
                        },
                        itemStyle: {
                            normal: {
                                color: '#c23531',
                                shadowBlur: 200,
                                shadowColor: 'rgba(0, 0, 0, 0.5)'
                            }
                        },

                        animationType: 'scale',
                        animationEasing: 'elasticOut',
                        animationDelay: function (idx) {
                            return Math.random() * 200;
                        }
                    }
                ]
            };
            myChart.setOption(option5, true);
        }

    }
    initMachine()


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