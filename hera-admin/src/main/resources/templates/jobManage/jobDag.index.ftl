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
                    <label for="expand">&nbsp;&nbsp;&nbsp;展示个数:</label>
                    <input id="expand" class="input-sm" style="width:80px;" value="1"/>
                    <input class="btn btn-info" type="button" id="nextNode" value="展示">
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
<!--<link rel="stylesheet" href="demo.css">-->


<@netCommon.commonScript />
<script type="text/javascript">
    function timeline_clicked(e) {
        var clicked_item = $(e.target);

        var sel = $('.selected');
        sel.removeClass('selected');
        $('#clicked-item').empty();

        if (sel.length == 0 || sel.data('id') != clicked_item.data('id')) {
            clicked_item.addClass('selected');
            $('#clicked-item').text(clicked_item.data('id'));
        }
    }

    function add_item() {
        var data = timeline.getTimelineData();
        data.push([
            {
                id: 'New One',
                start: -1978,
                end: 1978,
                className: 'styleA'
            }
        ]);
        timeline.setTimelineData(data).refreshTimeline();
    }

    function remove_selected_item() {
        var sel_item_id = $('.selected').data('id');
        var data = timeline.getTimelineData();
        for (var l = 0; l < data.length; l++) {
            for (var i = 0; i < data[l].length; i++) {
                if (data[l][i].id == sel_item_id) {
                    data[l].splice(i, 1);
                    if (data[l].length == 0)
                        data.splice(l, 1);
                    timeline.setTimelineData(data).refreshTimeline();
                    return;
                }
            }
        }
    }


</script>


<script type="text/javascript">
    $(document).ready(function () {
                keypath();

                $('#nextNode').on("click", function () {
                    var expand = $('#expand').val();
                    if (expand == null || expand == undefined || expand == "") {
                        expand = 0;
                    }
                    expandNextNode(expand);

                })
                $('#expandAll').on("click", function () {
                    expandNextNode(len);
                })


            }
    );

    var nodes, edges, g, headNode, currIndex = 0, len, inner, initialScale = 0.75, zoom, nodeIndex = {}, graphType;


    function keypath(type) {
        graphType = type;
        var node = $("#item").val();
        console.log(node)
        if (node == "")
            return;
        var url = base_url + "/scheduleCenter/getJobImpactOrProgress";
        var data = {jobId: node, type: type};

        var success = function (data) {
            // Create a new directed graph
            if (data.success == false) {
                alert("不存在该任务节点");
                return;
            }
            initDate(data);

            // Set up the edges
            svg = d3.select("svg");
            inner = svg.select("g");

            // Set up zoom support
            zoom = d3.behavior.zoom().on("zoom", function () {
                inner.attr("transform", "translate(" + d3.event.translate + ")" +
                        "scale(" + d3.event.scale + ")");
            });
            svg.call(zoom);

            redraw();
            // expandNextNode(1);
            zoom
                    .translate([($('svg').width() - g.graph().width * initialScale) / 2, 20])
                    .scale(initialScale)
                    .event(svg);
            //svg.attr('height', g.graph().height * initialScale + 40);
        }

        jQuery.ajax({
            type: 'POST',
            url: url,
            data: data,
            success: success
            //dataType: 'json'
        });
    }

    function initDate(data) {
        edges = data.data.edges;
        headNode = data.data.headNode;
        len = edges.length;
        currIndex = 0;
        g = new dagreD3.graphlib.Graph().setGraph({});
        g.setNode(headNode.nodeName, {label: headNode.nodeName, style: "fill: #bd16ff" + ";" + headNode.remark})
        var nodeName;
        for (var i = 0; i < len; i++) {
            nodeName = edges[i].nodeA.nodeName;
            if (nodeIndex[nodeName] == null || nodeIndex[nodeName] == undefined || nodeIndex[nodeName] == 0) {
                nodeIndex[nodeName] = i + 1;
            }
        }
    }

    //重新加载界面
    function redraw() {
        var render = new dagreD3.render();
        render(inner, g);

        $('.node').on("mousemove", function () {
            var nodeName = $(this).text();
            var str = g.node(nodeName).style || '';
            $('#jobDetail').text(str.substring(str.indexOf(";") + 1));
        })

        $('.node').on("click", function () {
            var nodeName = $(this).text();
            var currNodeIndex = nodeIndex[nodeName];
            if (currNodeIndex == 0 || currNodeIndex == undefined) {
                $('#jobDetail').text("此任务节点已全部展开^_^");
                return;
            }

            --currNodeIndex;
            while (true) {
                var edge = edges[currNodeIndex];
                addEdgeToGraph(edge);
                if (++currNodeIndex >= len || edge.nodeA.nodeName != edges[currNodeIndex].nodeA.nodeName) {
                    break;
                }
            }
            nodeIndex[nodeName] = 0;
            redraw();
        })
    }

    //根据状态获得颜色
    function getColor(status) {

        if (status.indexOf("success") >= 0)
            return "fill: #37b55a";
        else if (status.indexOf("running") >= 0)
            return "fill: #f0ab4e";
        else
            return "fill: #f77";
    }

    function expandNextNode(nodeNum) {
        while (nodeNum > 0) {
            if (currIndex < len) {
                var edge = edges[currIndex];
                if (addEdgeToGraph(edge)) {
                    nodeNum--;
                }
                currIndex++;
            } else {
                alert("已经全部展示完毕！");
                break;
            }
        }
        redraw();
    }

    function addEdgeToGraph(edge) {
        var targ = edge.nodeA;
        var src = edge.nodeB;

        if (g.node(src.nodeName) == undefined) {
            g.setNode(src.nodeName, {label: src.nodeName, style: getColor(src.remark) + ";" + src.remark});
        }
        if (g.node(targ.nodeName) == undefined) {
            g.setNode(targ.nodeName, {label: targ.nodeName, style: getColor(targ.remark) + ";" + targ.remark});
        }
        if (nodeIndex[targ.nodeName] == 0) {
            return false;
        }
        if (graphType == 0) {
            g.setEdge(src.nodeName, targ.nodeName, {label: ""});
        } else {
            g.setEdge(targ.nodeName, src.nodeName, {label: ""});
        }

        return true;
    }
</script>
</body>
</html>



