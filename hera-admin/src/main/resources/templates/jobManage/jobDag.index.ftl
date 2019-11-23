<html>
<head>
    <meta charset="UTF-8">
    <title>任务依赖</title>
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
        svg {
            border: 3px solid #eee !important;
        }

    </style>

    <style id="css">

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
                    <h3 class="big-title">任务链路图</h3>
                </div>
                <div class="box-body">

                    <form class="form-inline">

                        <div class="form-group">
                            <label for="itemw">任务ID:</label>
                            <input id="item" class="input-sm" style="width:80px; border: 1px solid #ccc;"/>
                            <input class="btn btn-primary" type="button" value="上游任务链" onclick="keypath(0)"/>
                            <input class="btn btn-primary" type="button" value="下游任务链" onclick="keypath(1)"/>
                        </div>

                        <div class="form-group">
                            <input class="btn btn-primary" type="button" id="expandAll" value="展示全部">
                        </div>
                    </form>

                    </br>
                    <div class="row" style="margin: 0;">
                        <svg style="border: 3px solid dimgrey;height:700" class="col-lg-10">
                            <g/>
                        </svg>
                        <textarea class="label-primary col-lg-2" style="height: 700px" id="jobDetail" readonly>任务信息</textarea>
                    </div>
                </div>
        </section>
    </div>
</div>
</body>

<@netCommon.commonScript />
<script src="${request.contextPath}/static/js/taskGraph.js?v=2"></script>
<script src="${request.contextPath}/static/js/jobDag.js?v=2"></script>
<script src="${request.contextPath}/static/plugins/d3/d3.v3.min.js"></script>
<script src="${request.contextPath}/static/plugins/d3/dagre-d3.js"></script>

</body>
</html>



