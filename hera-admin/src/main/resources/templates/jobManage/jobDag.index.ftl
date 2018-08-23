<html>
<head>
    <meta charset="UTF-8">
    <title>任务历史运行记录</title>
    <#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />

    <style>
        #timeline {
            position: relative;
            margin-top: 10px;
            max-width: 100%;
            overflow-x: auto;
            overflow-y: hidden;
            border: 1px solid dimgray;
            box-shadow: 3px 3px 10px 0px rgba(0, 0, 0, 0.75);
        }

        #timeline .selected {
            font-weight: bold;
            box-shadow: 0px 0px 3px 1px gray;
        }

        #timeline-collapse {
            top: 100px
        }

        .styleA {
            color: darkgreen;
            background-color: lightgreen;
        }

        .styleB {
            color: darkred;
            background-color: mistyrose;
        }

        .styleC {
            color: darkblue;
            background-color: lightblue;
        }

        .timeline-unused-phase {
            background: repeating-linear-gradient(
                    -45deg,
                    rgba(255, 255, 255, 0.85),
                    rgba(255, 255, 255, 0.85) 10px,
                    rgba(235, 235, 235, 0.85) 10px,
                    rgba(235, 235, 235, 0.85) 20px
            );
        }


    </style>

    <style id="css">
        body {
            font: 300 14px 'Helvetica Neue', Helvetica;
        }

        .node rect {
            stroke: #333;
            fill: #fff;
        }

        .edgePath path {
            stroke: #333;
            fill: #333;
            stroke-width: 1.5px;
        }
    </style>

</head>

<body class="hold-transition skin-blue-light sidebar-mini">
<div class="wrapper">
    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "developCenter" />

    <div class="content-wrapper">
        <section class="content">

            <form class="form-inline">

                <div class="form-group">
                    <label for="itemw">任务ID:</label>
                    <input id="item" class="input-sm" style="width:80px;"/>
                    <input class="btn btn-info" type="button" value="上游任务链" onclick="keypath(0)"/>
                    <input class="btn btn-info" type="button" value="下游任务链" onclick="keypath(1)"/>
                </div>

                <div class="form-group">
                    <#--<label for="expand">&nbsp;&nbsp;&nbsp;展示个数:</label>-->
                    <#--<input id="expand" class="input-sm" style="width:80px;" value="1"/>-->
                    <#--<input class="btn btn-info" type="button" id="nextNode" value="展示">-->
                    <input class="btn btn-info" type="button" id="expandAll" value="展示全部">
                </div>
            </form>

            </br>
            <div class="row">
                <svg style="border: 3px solid dimgrey;height:700" class="col-lg-10">
                    <g/>
                </svg>
                <textarea class="label-info col-lg-2" style="height: 300px" id="jobDetail" readonly>任务信息</textarea>
            </div>

        </section>
    </div>
</div>
</body>

<@netCommon.commonScript />
<script src="${request.contextPath}/js/jobDag.js"></script>

</body>
</html>



