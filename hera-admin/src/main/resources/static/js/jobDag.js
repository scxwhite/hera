var nodes, edges, g, headNode, currIndex = 0, len, inner, initialScale = 0.75, zoom, nodeIndex = {}, graphType;

$(document).ready(function () {
        // keypath();
    $('#jobDag').addClass('active');
    $('#jobDag').parent().addClass('menu-open');
    $('#jobDag').parent().parent().addClass('menu-open');
    $('#jobManage').addClass('active');

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


