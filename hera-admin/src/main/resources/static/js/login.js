$(function () {
    $('input').iCheck({
        checkboxClass: 'icheckbox_square-blue',
        radioClass: 'iradio_square-blue',
        increaseArea: '20%' // optional
    });

    jQuery.validator.addMethod("phone", function (value, element) {
        var length = value.length;
        var mobile = /^1[3|5|8]{1}[0-9]{9}$/;
        return this.optional(element) || (length == 11 && mobile.test(value));
    }, "请正确填写您的手机号码");
    var registerForm = $('#registerForm');
    registerForm.validate({
        rules: {
            name: {
                required: true,
                minlength: 5,
                maxlength: 18
            },
            password: {
                required: true,
                minlength: 5,
                maxlength: 18
            },
            confirmPassword: {
                required: true,
                equalTo: '#password'
            },
            email: {
                required: true,
                email: true
            },
            phone: {
                required: true,
                phone: true
            },
            description: {
                required: true,
                minlength: 20,
                maxlength: 200
            }
        },
        messages: {
            confirmPassword: {
                equalTo: "两次输入的密码必须一致"
            }
        },
        submitHandler: function () {
            $.post($("#baseURl").attr("href") + "/register", {
                name: $('#name').val(),
                password: hex_md5($('#password').val()),
                phone: $('#phone').val(),
                email: $('#email').val(),
                description: $('#description').val()
            }, function (data) {
                if (data.success === true) {
                    layer.open({
                        title: '系统提示',
                        content: data.msg,
                        icon: '1'
                    });
                    registerForm[0].reset();
                } else {
                    layer.open({
                        title: '系统提示',
                        content: (data.msg || "注册失败"),
                        icon: '2'
                    });
                }
            });
        }
    });

    $("#loginForm").validate({
        errorElement: 'span',
        errorClass: 'help-block',
        focusInvalid: true,
        rules: {
            userName: {
                required: true,
                minlength: 5,
                maxlength: 18
            },
            password: {
                required: true,
                minlength: 5,
                maxlength: 18
            }
        },
        messages: {
            userName: {
                required: "请输入登录账号.",
                minlength: "登录账号不应低于5位",
                maxlength: "登录账号不应超过18位"
            },
            password: {
                required: "请输入登录密码.",
                minlength: "登录密码不应低于5位",
                maxlength: "登录密码不应超过18位"
            }
        },
        highlight: function (element) {
            $(element).closest('.form-group').addClass('has-error');
        },
        success: function (label) {
            label.closest('.form-group').removeClass('has-error');
            label.remove();
        },
        errorPlacement: function (error, element) {
            element.parent('div').append(error);
        },
        submitHandler: function () {
            $.post($("#baseURl").attr("href") + "/loginCheck", $("#loginForm").serialize(), function (data) {
                if (data.code == "200") {
                    layer.open({
                        title: '系统提示',
                        content: '登录成功',
                        icon: '1',
                        end: function (layero, index) {
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