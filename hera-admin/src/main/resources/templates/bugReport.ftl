<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
<head>
    <title>任务调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />

    <style>
        * {
            margin: 0px;
            padding: 0px;
        }

        .item {
            width: 266px;
            max-height: 300px;
            line-height: 30px;
        }

        #container {
            width: 100%;
            height: 100%;
            OVERFLOW-Y: auto;
            OVERFLOW-X: hidden;
        }

        #input {
            border: 0;
            border-radius: 5px;
            display: block;
            height: 135px;
            padding: 0 1em;
            line-height: 30px;
            width: 100%;
            font-size: 20px;
            max-height: 135px;
            resize: none;
        }

        .item h2 {
            font-family: 'Eater', cursive;
            font-size: 14px;
            padding-bottom: 10px;
            padding-top: 5px;
            padding-left: 10px;
        }

        .item p {
            font-size: large;
            padding-left: 10px;
            padding-right: 10px;
            max-width: 100%;
            word-break: break-all;
            overflow: hidden;
            line-height: 1.2;
            display: block;
        }

        .item h3 {
            font-family: 'Eater', cursive;
            padding-bottom: 10px;
            padding-top: 10px;
            padding-left: 10px;
            color: rgba(33, 33, 33, 0.7);
            width: 200px;
        }
    </style>
</head>


<body class="hold-transition skin-black sidebar-mini">
<div class="wrapper" style="height: 100%;">
    <!-- header -->
	<@netCommon.commonHeader />
    <!-- left -->
	<@netCommon.commonLeft "adviceController" />

    <div class="content-wrapper">
        <!-- 留言板 -->
        <div class="content">
            <textarea id="input" type="text" placeholder="随便说说吧...按回车发布"></textarea>
            <div id="container">
                <#list allMsg as msg>
                    <div class="item" style="background: ${msg.color} " onmouseout="addMouseOut(this)"
                         onmouseover="addMouseOver(this)">
                        <h2> ${msg.createTime} </h2>
                        <p style="width:90%;height:90%"> ${msg.msg} </p>
                        <h3> ${msg.address} </h3>
                    </div>
                </#list>

            </div>
        </div>
    </div>
</div>

<@netCommon.commonScript />
<script src="http://pv.sohu.com/cityjson?ie=utf-8"></script>
<script type="text/javascript">
    function addMouseOver(div) {
        $(div).css({
            "border": "2px solid #FF00FF"
        });

    }

    function addMouseOut(div) {
        $(div).css({
            "border": "none"
        });

    }

    (function ($) {
        var address = returnCitySN.cip;
        var color;
        var createTime;
        var container;

        // 可选颜色
        var colors = ['	#FFF68F', '#FFBBFF', '#98FB98', '#9AFF9A', '#EE9572', '#00E5EE', '#BFEFFF'];

        //创建许愿页
        var createItem = function (text) {
            var color = colors[parseInt(Math.random() * 5, 10)]
            $('<div class="item" onmouseout="addMouseOut(this)" onmouseover="addMouseOut(this)"> <h2>' + createTime + '</h2> <p>' + text + '</p> <h3>来自：' + address + '</h3></div>').css({'background': color}).appendTo(container).drag();
        };

        // 定义拖拽函数
        $.fn.drag = function () {

            var $this = $(this);
            var parent = $this.parent();

            var pw = parent.width();
            var ph = parent.height();
            var thisWidth = $this.width() + parseInt($this.css('padding-left'), 10) + parseInt($this.css('padding-right'), 10);
            var thisHeight = $this.height() + parseInt($this.css('padding-top'), 10) + parseInt($this.css('padding-bottom'), 10);

            var x, y, positionX, positionY;
            var isDown = false;

            var randY = parseInt(Math.random() * (ph - thisHeight), 10);
            var randX = parseInt(Math.random() * (pw - thisWidth), 10);

            console.log(thisWidth + "-" + thisHeight)

            parent.css({
                "position": "relative",
                "overflow": "hidden"
            });

            $this.css({
                "cursor": "move",
                "position": "absolute"
            }).css({
                top: randY,
                left: randX
            }).mousedown(function (e) {
                parent.children().css({
                    "zIndex": "0"
                });
                $this.css({
                    "zIndex": "1"
                });
                isDown = true;
                x = e.pageX;
                y = e.pageY;
                positionX = $this.position().left;
                positionY = $this.position().top;

                console.log(positionX + " " + positionY)
                console.log(x + " " + y)
                return false;
            });


            $(document).mouseup(function (e) {
                isDown = false;
            }).mousemove(function (e) {
                var xPage = e.pageX;
                var moveX = positionX + xPage - x;

                var yPage = e.pageY;
                var moveY = positionY + yPage - y;

                if (isDown == true) {
                    $this.css({
                        "left": moveX,
                        "top": moveY
                    });
                } else {
                    return;
                }
                if (moveX < 0) {
                    $this.css({
                        "left": "0"
                    });
                }
                if (moveX > (pw - thisWidth)) {
                    $this.css({
                        "left": pw - thisWidth
                    });
                }
                if (moveY < 0) {
                    $this.css({
                        "top": "0"
                    });
                }
                if (moveY > (ph - thisHeight)) {
                    $this.css({
                        "top": ph - thisHeight
                    });
                }
            });
        };

        // 初始化
        var init = function () {

            container = $('#container');

            // 绑定关闭事件
            container.on('click', 'a', function () {
                $(this).parent().remove();
            }).height($(window).height() - 204);


            // 绑定输入框
            $('#input').keydown(function (e) {
                var $this = $(this);
                if (e.keyCode == '13') {
                    var value = $this.val();
                    value = value.replace(/< (.*?)>/, '');
                    value = value.replace(/<\/>/, '');
                    if (value.length > 130) {
                        if (confirm("你输入的有点太多了哦，会被截取的，再改改吗？^_^")) {
                            return;
                        }
                        value = value.substring(0, 129) + "...";
                    }
                    if (value.length == 0) {
                        alert("你确定不留下点什么吗？");
                        return;
                    }
                    color = colors[parseInt(Math.random() * colors.length, 10)]
                    if (value) {
                        var time = new Date();
                        var month = (time.getMonth() + 1) > 9 ? (time.getMonth() + 1) : "0" + (time.getMonth() + 1);
                        var day = time.getDate() > 9 ? time.getDate() : "0" + time.getDate();
                        var hours = time.getHours() > 9 ? time.getHours() : "0" + time.getHours();
                        var minutes = time.getMinutes() > 9 ? time.getMinutes() : "0" + time.getMinutes();
                        var seconds = time.getSeconds() > 9 ? time.getSeconds() : "0" + time.getSeconds();
                        createTime = time.getFullYear() + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;

                        $.ajax({
                            url: "adviceController/add",
                            type: "post",
                            data: {
                                "msg": value,
                                "address": address,
                                "color": color,
                                "createTime": createTime
                            },
                            success: function (data) {
                                createItem(value);
                                $this.val('');
                                layer.msg(data.msg)
                            }
                        });
                    }
                }
            });

        };

        $(function () {
            init();
            $('.item').each(function () {
                $(this).drag();
            });
        });

    })(jQuery);

</script>

</body>

</html>


