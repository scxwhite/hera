$(function () {
    $('input').iCheck({
        checkboxClass: 'icheckbox_square-blue',
        radioClass: 'iradio_square-blue',
        increaseArea: '20%' // optional
    });

    var loginFormValid = $("#loginForm").validate({
        errorElement : 'span',
        errorClass : 'help-block',
        focusInvalid : true,
        rules : {
            userName : {
                required : true ,
                minlength: 5,
                maxlength: 18
            },
            password : {
                required : true ,
                minlength: 5,
                maxlength: 18
            }
        },
        messages : {
            userName : {
                required :"请输入登录账号."  ,
                minlength:"登录账号不应低于5位",
                maxlength:"登录账号不应超过18位"
            },
            password : {
                required :"请输入登录密码."  ,
                minlength:"登录密码不应低于5位",
                maxlength:"登录密码不应超过18位"
            }
        },
        highlight : function(element) {
            $(element).closest('.form-group').addClass('has-error');
        },
        success : function(label) {
            label.closest('.form-group').removeClass('has-error');
            label.remove();
        },
        errorPlacement : function(error, element) {
            element.parent('div').append(error);
        },
        submitHandler : function() {

            $.post("/toLogin", $("#loginForm").serialize(), function(data) {
                if (data.code == "200") {
                    layer.open({
                        title: '系统提示',
                        content: '登录成功',
                        icon: '1',
                        end: function(layero, index){
                            debugger
                            var url = base_url + "/home";
                            window.location.href = url;
                        }
                    });
                } else {
                    layer.open({
                        title: '系统提示',
                        content: (data.msg || "登录失败"),
                        icon: '2'
                    });
                }
            });
        }
    });
});