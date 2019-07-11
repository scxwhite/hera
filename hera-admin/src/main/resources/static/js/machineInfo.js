$(function () {
    $('#machineInfoMenu').addClass('active');
    var info = {};
    var ramOption = {
        tooltip: {
            formatter: "{a} <br/>{b} : {c}%"
        },
        series: [
            {
                name: '业务指标',
                type: 'gauge',
                detail: {formatter: '{value}%'},
                data: [{value: 50, name: '内存使用率'}]
            }
        ]
    };

//内存仪表板
    function initMachine() {
        $.ajax({
            url: base_url + '/homePage/getAllWorkInfo',
            type: 'get',
            success: function (result) {
                if (result.success === false) {
                    alert(result.message);
                    return;
                }
                let data = result.data;
                //机器选择
                for (var i in data) {
                    $('#machineList').append('<option value="' + i + '">' + i + '</option>');
                }
                info = data;
                var machine = $('#machineList').val();
                initInfo(data[machine]);
            }
        })
    }

    function initInfo(machine) {
        console.log(machine)
        //系统概况
        if (machine.osInfo.mem != null) {
            ramOption.series[0].data[0].value = parseFloat(machine.osInfo.mem.toFixed(2));
        }
        var myChart = echarts.init(document.getElementById('ramGauge'));
        myChart.setOption(ramOption, true);

        $('#userPercent').easyPieChart({
            animate: 1000,
            barColor: '#011935',
            onStep: function (from, to, percent) {
                $(this.el).find('.percent').text(Math.round(percent));
            },
            size: 200,
            lineWidth: 10,
            scaleColor: '#666'
        });
        $('#userPercent').data('easyPieChart').update(0);
        $('#userPercent').data('easyPieChart').update(parseFloat(machine.osInfo.user));

        $('#SysPercent').easyPieChart({
            animate: 1000,
            barColor: '#009FAB',
            onStep: function (from, to, percent) {
                $(this.el).find('.percent').text(Math.round(percent));
            },
            size: 200,
            lineWidth: 10,
            scaleColor: '#666'
        });
        $('#SysPercent').data('easyPieChart').update(0);
        $('#SysPercent').data('easyPieChart').update(parseFloat(machine.osInfo.system));

        $('#CPUPercent').easyPieChart({
            animate: 1000,
            barColor: '#1DB0B8',
            onStep: function (from, to, percent) {
                $(this.el).find('.percent').text(Math.round(percent));
            },
            size: 200,
            lineWidth: 10,
            scaleColor: '#666'
        });
        $('#CPUPercent').data('easyPieChart').update(0);
        $('#CPUPercent').data('easyPieChart').update(parseInt(machine.osInfo.cpu));

        $('#SwapPercent').easyPieChart({
            animate: 1000,
            barColor: '#008891',
            onStep: function (from, to, percent) {
                $(this.el).find('.percent').text(Math.round(percent));
            },
            size: 200,
            lineWidth: 10,
            scaleColor: '#666'
        });
        $('#SwapPercent').data('easyPieChart').update(0);
        $('#SwapPercent').data('easyPieChart').update(parseInt(machine.osInfo.swap));

        //进程监控
        $("#processMonitor").bootstrapTable('destroy');
        if (machine.processMonitor !== null) {
            $('#processMonitor').bootstrapTable({
                columns: [{
                    field: 'pid',
                    title: 'PID',
                    width: 150
                }, {
                    field: 'user',
                    title: 'USER',
                    width: 150
                }, {
                    field: 'viri',
                    title: 'VIRI',
                    width: 150
                }, {
                    field: 'cpu',
                    title: 'CPU',
                    formatter: function (value, row, index) {
                        value = parseFloat(value)
                        if (value <= 60) {
                            return '<div class="progress progress-xs"><div class="progress-bar progress-bar-success progress-bar-striped" role="progressbar" aria-valuenow="' + value + '" aria-valuemin="0" aria-valuemax="100" style="width: ' + value + '%"><span class="sr-only">40% Complete (success)</span></div></div>'
                        } else if (value <= 80) {
                            return '<div class="progress progress-xs"><div class="progress-bar progress-bar-warning progress-bar-striped" role="progressbar" aria-valuenow="' + value + '" aria-valuemin="0" aria-valuemax="100" style="width: ' + value + '%"><span class="sr-only">40% Complete (success)</span></div></div>'
                        } else {
                            return '<div class="progress progress-xs"><div class="progress-bar progress-bar-danger progress-bar-striped" role="progressbar" aria-valuenow="' + value + '" aria-valuemin="0" aria-valuemax="100" style="width: ' + value + '%"><span class="sr-only">40% Complete (success)</span></div></div>'
                        }
                    },
                    width: 150

                }, {
                    field: 'mem',
                    title: 'MEM',
                    formatter: function (value, row, index) {
                        if (value <= 60) {
                            return '<div class="progress progress-xs"><div class="progress-bar progress-bar-success progress-bar-striped" role="progressbar" aria-valuenow="' + value + '" aria-valuemin="0" aria-valuemax="100" style="width: ' + value + '%"><span class="sr-only">40% Complete (success)</span></div></div>'
                        } else if (value <= 80) {
                            return '<div class="progress progress-xs"><div class="progress-bar progress-bar-warning progress-bar-striped" role="progressbar" aria-valuenow="' + value + '" aria-valuemin="0" aria-valuemax="100" style="width: ' + value + '%"><span class="sr-only">40% Complete (success)</span></div></div>'
                        } else {
                            return '<div class="progress progress-xs"><div class="progress-bar progress-bar-danger progress-bar-striped" role="progressbar" aria-valuenow="' + value + '" aria-valuemin="0" aria-valuemax="100" style="width: ' + value + '%"><span class="sr-only">40% Complete (success)</span></div></div>'

                        }
                    },
                    width: 150
                }, {
                    field: 'time',
                    title: 'TIME',
                    width: 150
                }, {
                    field: 'command',
                    title: 'COMMAND',
                    width: 300
                }],
                data: machine.processMonitor
            });
        }
        $('#processMonitor').bootstrapTable('hideLoading');
        //机器信息
        var machineInfo = machine.machineInfo;
        $('#machineInfo').empty();

        for (var i = 0; machineInfo != null && i < machineInfo.length; i++) {
            $('#machineInfo').append('<div class="machine"><p class="filesystem">' + machineInfo[i].filesystem + '(已用' + machineInfo[i].used + '/可用' + machineInfo[i].avail + ')</p><div class="progress progress-xs"><div class="progress-bar progress-bar-warning progress-bar-striped" role="progressbar" aria-valuenow="' + machineInfo[i].use + '" aria-valuemin="0" aria-valuemax="100" style="width: ' + machineInfo[i].use + '%"><span class="sr-only"></span></div></div></div>')
        }
    }

    initMachine();
    $('#machineList').change(function (e) {
        e.stopPropagation();
        var machine = $(this).val();
        machine = info[machine];
        initInfo(machine);
    })
})
