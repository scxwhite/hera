var msgHeight;

function successMsg(data) {
    if (window.screen.width <= 767) {
        msgHeight = 100;
    } else {
        msgHeight = 50;
    }
    if (data.success == true) {
        success(data.message);
        $("#table").bootstrapTable("refresh");
    } else {
        failure(data.message);
    }

}

function success(msg) {
    $('#alertSuccess').css({
        "width": 700,
        "right": ($(window).width() - 700) / 2,
        "display": "block"
    });
    $('#alertSuccess #successText').text(msg);
    $('#alertSuccess').animate({
        top: msgHeight
    }, 2000);
    $('#alertSuccess').animate({
        top: 0
    }, 2000, "linear", function () {
        $('#alertSuccess').css("display", "none");
    });
}

function failure(msg) {

    $('#alertFailure').css({
        "width": 500,
        "right": ($(window).width() - 500) / 2,
        "display": "block"
    });
    $('#alertFailure #failureText').text(msg);
    $('#alertFailure').animate({
        top: msgHeight
    }, 2000);
    $('#alertFailure').animate({
        top: 0
    }, 2000, "linear", function () {
        $('#alertFailure').css("display", "none");
    });
}

function dealCode(data) {
    if (data.code == 401) {
        location.href = "/";
    } else {
        alert("错误代码：" + data.code);
    }
}


function formDataLoad(domId, obj) {
    $("#" + domId)[0].reset();
    for (var property in obj) {
        if (obj.hasOwnProperty(property) == true) {
            if ($("#" + domId + " [name='" + property + "']").size() > 0) {
                $("#" + domId + " [name='" + property + "']").each(function () {
                    var dom = this;
                    if ($(dom).attr("type") == "radio") {
                        $(dom).filter("[value='" + obj[property] + "']").attr("checked", true);
                    }
                    else if ($(dom).attr("type") == "checkbox") {
                        obj[property] == true ? $(dom).attr("checked", "checked") : $(dom).attr("checked", "checked").removeAttr("checked");
                    }
                    else if ($(dom).attr("type") == "text" || $(dom).prop("tagName") == "SELECT" || $(dom).attr("type") == "hidden" || $(dom).attr("type") == "textarea") {
                        if (obj[property] == null || obj[property] == undefined) {
                            $("dom option:first").attr("selected", true)
                        } else {
                            $(dom).val(obj[property]);
                        }
                    }
                    else if ($(dom).prop("tagName") == "TEXTAREA") {
                        $(dom).val(obj[property]);
                    } else {
                        $(dom).val(obj[property]);
                        $(dom).text(obj[property]);
                    }
                });
            }
        }
    }
}

function httpRequest(url, type, success) {
    httpRequest(url, type, null, false, success);
}

function httpRequest(url, type, params, async, success) {
    if (isNull(url)) {
        return ;
    }
    type = type || "post";
    async = async || false;
    success = success || function (data) {
        alert(data);
    };

    $.ajax({
        url: url,
        type: type,
        data: params,
        async: async,
        success: success
    })
}

function isNull(val) {
    if (val == null || val == undefined || val == "") {
        return true;
    }
    return false;
}