var nodes, edges, g, headNode, currIndex = 0, len, inner, initialScale = 0.75, zoom, nodeIndex = {}, graphType;

$(document).ready(function () {
        // keypath();

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


function keypath(type) {
    graphType = type;
    debugger
    var node = $("#item")[0].value;
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