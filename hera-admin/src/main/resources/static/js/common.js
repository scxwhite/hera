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

/**
 * 把当前选中的节点存入localStorage
 * 页面刷新后，会根据"defaultId"设置当前选中的节点
 * 避免页面刷新丢失
 * @param id    节点ID
 */
function setCurrentId(id) {

    localStorage.setItem("defaultId", id);
}

function toJobPage(id) {
    setCurrentId(id);
    window.open(base_url + "/scheduleCenter");
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


/**
 * 判断value是否已经存在arr数据中
 *
 * @param arr
 * @param value
 * @returns {boolean}
 */
function isInArray(arr, value) {
    var b = false;
    if (arr == null) {
        return b;
    }
    for (var i = 0; i < arr.length; i++) {
        if (arr[i]['id'] == value['id']) {
            b = true;
            break;
        }
    }
    return b;
}

/**
 * 以post方式请求数据
 *
 * @param url
 * @returns {*}
 */

function getDataByPost(url) {
    var dataStore = null;
    $.ajax({
        type: "post",
        url: url,
        async: false,
        success: function (data) {
            if (data.success === true) {
                dataStore = data.data;
            } else {
                alert(data.message);
            }

        }
    });
    return dataStore;
}

/**
 * 以get方式获取数据
 * @param url
 * @param parameter
 * @returns {*}
 */
function getDataByGet(url, parameter) {
    var result = '';
    $.ajax({
        url: url,
        type: "get",
        async: false,
        data: parameter,
        success: function (data) {
            if (data.success === true) {
                result = data.data;
            } else {
                alert(data.message);
            }
        }
    });
    return result;
}

/**
 * 格式化日期
 * @param timestamp
 * @returns {string}
 */
function getLocalTime(timestamp) {
    if(timestamp == null) {
        return "";
    }
    var date = new Date(timestamp);
    var newDate = date.toLocaleDateString().replace(/\//g, "-") + " " + date.toTimeString().substr(0, 8);
    return newDate;
}

function uploadFile() {
    $("#uploadFile").modal('show');

    $("#fileForm").fileinput({
        uploadUrl: base_url + "/uploadResource/upload.do",
        maxFileCount: 5,
        enctype: 'multipart/form-data',
        language: 'zh',
        allowedFileExtensions: ['py','jar','sql','hive','sh','js','txt','png','jpg','gif'],
        msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！"
    }).on("fileuploaded", function (event, data) {
        var response = data.response;
        var message = response.message;
        if (response.success === false) {
            $("#responseResult").html('<span style="color: red; ">" + message + "</span>');
            alert(message);
        }
        if (response.success === true) {
            $("#responseResult").html("<b>" +"hadoop文件使用路径: "+ message + "</b>");
        }
    }).on('filepredelete', function () {
            $("#responseResult").html("");
        }

    );
}


$("#logoutBtn").click(function () {
    var url = base_url;
clearAllCookie();
    window.location.href = url;
});

function clearAllCookie() {
    var keys = document.cookie.match(/[^ =;]+(?=\=)/g);
    if(keys) {
        for(var i=0; i < keys.length; i++) {
            document.cookie = keys[i] + '=0;expires=' + new Date(0).toUTCString();
        }
    }
}
$('.my-tree').click(function (e) {
    $(this).addClass('menu-open');
})
