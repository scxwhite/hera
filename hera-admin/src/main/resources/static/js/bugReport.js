var wallColor = "#dd1818";


function addMouseOut(div) {
    $(div).css({
        "border": "none"
    });
}
function addMouseOver(div) {
    $(div).css({
        "border": "2px solid #FF00FF"
    });
}

(function ($) {
    var address = returnCitySN.cip;
    var createTime;
    var container;


    //创建许愿页
    var createItem = function (text) {
        $('<div class="item" onmouseout="addMouseOut(this)" onmouseover="addMouseOver(this)"> <h2>' + createTime + '</h2> <p>' + text + '</p> <h2 class="pull-right">来自：' + address + '</h2></div>').css({'background': wallColor}).appendTo(container).drag();
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


        console.log(randX + "-" + randY)


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
                            "color": wallColor,
                            "createTime": createTime
                        },
                        success: function (data) {
                            if (data.success == true) {
                                createItem(value);
                                $this.val('');
                            } else {
                                layer.msg(data.message)
                            }

                        }
                    });
                }
            }
        });

    };

    layui.use(['colorpicker'], function () {
        init();
        $('#advice').addClass('active');

        var items = $('.item');
        items.each(function () {
            $(this).drag();
        });

        items.on('onmouseout', function () {
            addMouseOut();
        }).on('onmouseover', function () {
            addMouseOver();
        });
        layui.colorpicker.render({
            elem: '#colorPicker',
            color: "FFBBFF",
            size: "sm"
            , done: function (newColor) {
                console.log(newColor)
                wallColor = newColor;
            }
        });
    });

})(jQuery);

