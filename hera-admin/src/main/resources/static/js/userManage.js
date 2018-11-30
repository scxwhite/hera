/**
 * 用户管理页面，用户注册之后的权限审核
 *
 * @type {any[]}
 */

var userList = new Array();
var indexList = new Array();

$(function () {
    $('#userManage').addClass('active');
    $('#userManage').parent().addClass('menu-open');
    $('#userManage').parent().parent().addClass('menu-open');
    $('#sysManager').addClass('active');
    $(".add-btn").click(function () {

        var id = $('#id').text();

        var name = $(' #name').val();
        var email = $(' #email').val();
        var phone = $(' #phone').val();
        var description = $('#description').val();

        var user = {
            "id": id,
            "name": name,
            "email": email,
            "phone": phone,
            "description": description
        }

        jQuery.ajax({
            type: "post",
            url: base_url + "/userManage/editUser",
            data: JSON.stringify(user),
            contentType: "application/json",
            dataType: "json",
            success: function (result) {
                successMsg(result);
                $('#editUser').modal('hide');
            }
        })
    });

    $('#confirmModalBtn').on('click', function () {
        var id = $('#hidden_id').text();
        if (id == null || id == undefined || "" == id) return;

        var operateType = $('#operateType').text();

        var parameter = {
            "id": id,
            "operateType": operateType
        }
        jQuery.ajax({
            type: "post",
            url: base_url + "/userManage/operateUser.do",
            data: JSON.stringify(parameter),
            contentType: "application/json",
            dataType: "json",
            success: function (result) {
                successMsg(result);
            }
        })
    });


    var TableInit = function () {
        var oTableInit = new Object();
        var test =[
            {
                "id": 162,
                "email": "chelizi@2dfire.com",
                "gmtCreate": "2016-01-29",
                "gmtModified": "2016-02-25",
                "name": "chelizi",
                "phone": "18658885243",
                "uid": "chelizi",
                "wangwang": "",
                "password": "a2b821d31cb5812d9f75ae6a096f698a",
                "userType": 0,
                "isEffective": -1,
                "description": "afsafasf"
            },
            {
                "id": 163,
                "email": "chelizi@2dfire",
                "gmtCreate": null,
                "gmtModified": null,
                "name": "biadmin",
                "phone": "18658885243",
                "uid": "biadmin",
                "wangwang": null,
                "password": "b80f46414c59ee45086c4188d3824f66",
                "userType": 0,
                "isEffective": 1,
                "description": null
            },
            {
                "id": 164,
                "email": "greemqqran@163.com",
                "gmtCreate": "2016-02-22",
                "gmtModified": "2016-02-25",
                "name": "jetty",
                "phone": "18821273863",
                "uid": "jetty",
                "wangwang": "",
                "password": "fd67b4e1ce7c6d227e090bcfd5d59f58",
                "userType": 0,
                "isEffective": -1,
                "description": "test"
            },
            {
                "id": 165,
                "email": "huoguo@2dfire.com",
                "gmtCreate": "2016-02-22",
                "gmtModified": "2016-12-12",
                "name": "guest",
                "phone": "18821273863",
                "uid": "guest",
                "wangwang": "",
                "password": "e10adc3949ba59abbe56e057f20f883e",
                "userType": 0,
                "isEffective": 1,
                "description": "来吧"
            },
            {
                "id": 166,
                "email": "lingxiao@2dfire.com",
                "gmtCreate": "2016-02-22",
                "gmtModified": "2018-08-20",
                "name": "monkey",
                "phone": "15868424786",
                "uid": "monkey",
                "wangwang": "",
                "password": "80da2ac7f186d89209c1e27015398469",
                "userType": 0,
                "isEffective": 1,
                "description": "hello"
            },
            {
                "id": 167,
                "email": "heimi@2dfire.com;xishu@2dfire.com;wulouzi@2dfire.com;fuling@2dfire.com",
                "gmtCreate": "2016-02-25",
                "gmtModified": "2017-11-15",
                "name": "olap",
                "phone": "15141128007,18768176003,13777473653,18268051492",
                "uid": "olap",
                "wangwang": "",
                "password": "adcc65cf4ed94a27fd357fb092bb1531",
                "userType": 0,
                "isEffective": 1,
                "description": "fuling+heimi+xishu+wulouzi"
            },
            {
                "id": 168,
                "email": " suoluo@2dfire.com",
                "gmtCreate": "2016-02-25",
                "gmtModified": "2016-12-19",
                "name": "dataware",
                "phone": "13362893326",
                "uid": "dataware",
                "wangwang": "",
                "password": "cbff0fb46f464dc0c5e73822fd3b6855",
                "userType": 0,
                "isEffective": 1,
                "description": "suoluo"
            },
            {
                "id": 169,
                "email": "tangbao@2dfire.com",
                "gmtCreate": "2016-02-25",
                "gmtModified": "2018-04-11",
                "name": "import",
                "phone": "15216671875",
                "uid": "import",
                "wangwang": "",
                "password": "a2000161d9d4acbbeeb6f2e22149ed32",
                "userType": 0,
                "isEffective": 1,
                "description": "etl-数据导入任务"
            },
            {
                "id": 170,
                "email": "baisui@2dfire.com",
                "gmtCreate": "2016-02-27",
                "gmtModified": "2016-02-27",
                "name": "baisui",
                "phone": "15868113480",
                "uid": "baisui",
                "wangwang": "",
                "password": "e10adc3949ba59abbe56e057f20f883e",
                "userType": 0,
                "isEffective": -1,
                "description": "我是百岁"
            },
            {
                "id": 171,
                "email": "baisui@2dfire.com",
                "gmtCreate": "2016-02-27",
                "gmtModified": "2016-02-27",
                "name": "admin",
                "phone": "15868113480",
                "uid": "admin",
                "wangwang": "",
                "password": "e10adc3949ba59abbe56e057f20f883e",
                "userType": 0,
                "isEffective": -1,
                "description": "admin"
            },
            {
                "id": 172,
                "email": "baisui@2dfire.com",
                "gmtCreate": "2016-02-27",
                "gmtModified": "2016-02-27",
                "name": "search",
                "phone": "15868113480",
                "uid": "search",
                "wangwang": "",
                "password": "e10adc3949ba59abbe56e057f20f883e",
                "userType": 0,
                "isEffective": 1,
                "description": "search"
            },
            {
                "id": 173,
                "email": "qingtang@2dfire.com",
                "gmtCreate": "2016-03-04",
                "gmtModified": "2016-03-04",
                "name": "database",
                "phone": "221",
                "uid": "database",
                "wangwang": "",
                "password": "e10adc3949ba59abbe56e057f20f883e",
                "userType": 0,
                "isEffective": -2,
                "description": "数据比对"
            },
            {
                "id": 174,
                "email": "qingtang@2dfire.com",
                "gmtCreate": "2016-03-04",
                "gmtModified": "2016-03-04",
                "name": "dba123",
                "phone": "1123",
                "uid": "dba123",
                "wangwang": "",
                "password": "e10adc3949ba59abbe56e057f20f883e",
                "userType": 0,
                "isEffective": 1,
                "description": "hive"
            },
            {
                "id": 175,
                "email": "huaidou@2dfire.com",
                "gmtCreate": "2016-03-10",
                "gmtModified": "2018-01-22",
                "name": "chain",
                "phone": "18638775594",
                "uid": "chain",
                "wangwang": "",
                "password": "66e2573d66f35311278fed9a6d5ed559",
                "userType": 0,
                "isEffective": 1,
                "description": "连锁店营业报表"
            },
            {
                "id": 176,
                "email": "binggun@2dfire.com",
                "gmtCreate": "2016-03-11",
                "gmtModified": "2017-08-26",
                "name": "binggun",
                "phone": "13588418933",
                "uid": "binggun",
                "wangwang": "",
                "password": "0d8040d582e327eb95cdb9e04d5918d3",
                "userType": 0,
                "isEffective": -1,
                "description": "服务端数据分析"
            },
            {
                "id": 177,
                "email": "maodou@2dfire.com",
                "gmtCreate": "2016-04-18",
                "gmtModified": "2016-04-18",
                "name": "maodou",
                "phone": "15057162790",
                "uid": "maodou",
                "wangwang": "",
                "password": "faa7076a752915265784a2c2a4752520",
                "userType": 0,
                "isEffective": 1,
                "description": "支付宝(口碑)"
            },
            {
                "id": 178,
                "email": "heimi@2dfire.com",
                "gmtCreate": "2016-04-29",
                "gmtModified": "2018-04-24",
                "name": "read",
                "phone": "15179865421",
                "uid": "read",
                "wangwang": "",
                "password": "d6e75cef33db9486ecceeb7169afd91b",
                "userType": 0,
                "isEffective": 1,
                "description": "只能读任务，不能写..."
            },
            {
                "id": 179,
                "email": "dabing@2dfire.com",
                "gmtCreate": "2016-05-25",
                "gmtModified": "2016-05-25",
                "name": "dabing",
                "phone": "18072971098",
                "uid": "dabing",
                "wangwang": "",
                "password": "7e642cf92d0666edbd011372f28938e1",
                "userType": 0,
                "isEffective": 1,
                "description": "static"
            },
            {
                "id": 180,
                "email": "heimi@2dfire.com;xishu@2dfire.com",
                "gmtCreate": "2016-06-28",
                "gmtModified": "2017-10-17",
                "name": "olap2",
                "phone": "15141128007,18768176003",
                "uid": "olap2",
                "wangwang": "",
                "password": "4f766ac5bd513863efaf92ae7aa261d6",
                "userType": 0,
                "isEffective": 1,
                "description": "only for heimi !!!"
            },
            {
                "id": 181,
                "email": "bingshi@2dfire.com;xishu@2dfire.com",
                "gmtCreate": "2016-06-29",
                "gmtModified": "2018-04-18",
                "name": "datamine",
                "phone": "15700070582,18768176003",
                "uid": "datamine",
                "wangwang": "",
                "password": "21f50f86e0e88815a8a0b733bb567821",
                "userType": 0,
                "isEffective": 1,
                "description": "1、主要是数据挖掘用\n2、业务量比较大，有可能全表计算\n3、需要开通dw以及ods的权限"
            },
            {
                "id": 182,
                "email": "mujin@2dfire.com;xiaocong@2dfire.com;fuling@2dfire.com",
                "gmtCreate": "2016-06-30",
                "gmtModified": "2018-10-15",
                "name": "olap3",
                "phone": "15267074992,18858272361,17605812211,18268051492",
                "uid": "olap3",
                "wangwang": "",
                "password": "5202e0b98a69b045fb968edb9c3bccff",
                "userType": 0,
                "isEffective": 1,
                "description": "mujin+xiaocong+fuling"
            },
            {
                "id": 183,
                "email": "maodou@2dfire.com",
                "gmtCreate": "2016-10-28",
                "gmtModified": "2016-10-28",
                "name": "open-api",
                "phone": "15057162790",
                "uid": "open-api",
                "wangwang": "",
                "password": "f4fa7892ae23674ee4d0e2f6246f944b",
                "userType": 0,
                "isEffective": 1,
                "description": "开放平台"
            },
            {
                "id": 184,
                "email": "huluobo@2dfire.com",
                "gmtCreate": "2016-10-28",
                "gmtModified": "2018-08-03",
                "name": "supplychain",
                "phone": "13588312942",
                "uid": "supplychain",
                "wangwang": "",
                "password": "74607a4f5eb3a710980de9b752378951",
                "userType": 0,
                "isEffective": 1,
                "description": "外部供应链专用：供应链-畅捷通T+对接"
            },
            {
                "id": 185,
                "email": "huyou@2dfire.com",
                "gmtCreate": "2016-12-05",
                "gmtModified": "2016-12-05",
                "name": "huyou",
                "phone": "15088618857",
                "uid": "huyou",
                "wangwang": "",
                "password": "c60fa35ea6505ac2d766e112767152f7",
                "userType": 0,
                "isEffective": 0,
                "description": "1.火小二app周边店铺搜索中的优惠信息\n2.胡柚"
            },
            {
                "id": 186,
                "email": "dongqing@2dfire.com",
                "gmtCreate": "2017-05-02",
                "gmtModified": "2017-05-02",
                "name": "retail",
                "phone": "13575453253",
                "uid": "retail",
                "wangwang": "",
                "password": "e10adc3949ba59abbe56e057f20f883e",
                "userType": 0,
                "isEffective": 1,
                "description": "零售统一使用账号，目前用于数据分库过程中的数据处理\n负责人：冬青\n需要retail_order库所有表查询权限"
            },
            {
                "id": 187,
                "email": "jingjie@2dfire.com",
                "gmtCreate": "2017-05-17",
                "gmtModified": "2017-05-17",
                "name": "jingjie",
                "phone": "17681821628",
                "uid": "jingjie",
                "wangwang": "",
                "password": "50ed3b5fc541ebc3c46643bf3cd2961f",
                "userType": 0,
                "isEffective": 0,
                "description": "订单冷热数据对比"
            },
            {
                "id": 188,
                "email": "muli@2dfire.com;juemingzi@2dfire.com",
                "gmtCreate": "2017-05-17",
                "gmtModified": "2018-03-20",
                "name": "only_select",
                "phone": "13355786689",
                "uid": "only_select",
                "wangwang": "",
                "password": "c1ba24c04cbfe5bec308dcd31829046b",
                "userType": 0,
                "isEffective": 1,
                "description": "只有查询权限"
            },
            {
                "id": 189,
                "email": "wulouzi@2dfire.com",
                "gmtCreate": "2017-06-07",
                "gmtModified": "2017-06-07",
                "name": "wulouzi",
                "phone": "13777473653",
                "uid": "wulouzi",
                "wangwang": "",
                "password": "c14665e12b629d3c3173c09ce571bd94",
                "userType": 0,
                "isEffective": 1,
                "description": "开发账号"
            },
            {
                "id": 190,
                "email": "muli@2dfire.com",
                "gmtCreate": "2017-06-15",
                "gmtModified": "2018-03-20",
                "name": "joints",
                "phone": "",
                "uid": "joints",
                "wangwang": "",
                "password": "6498ddf6903542943b7007b201696035",
                "userType": 0,
                "isEffective": 1,
                "description": "only for guest"
            },
            {
                "id": 191,
                "email": "banmian@2dfire.com",
                "gmtCreate": "2017-06-19",
                "gmtModified": "2017-06-19",
                "name": "banmian",
                "phone": "15158116099",
                "uid": "banmian",
                "wangwang": "",
                "password": "2846decd7cc09b9deb0f398e46192e20",
                "userType": 0,
                "isEffective": 0,
                "description": "data develop"
            },
            {
                "id": 192,
                "email": "muli@2dfire.com",
                "gmtCreate": "2017-06-27",
                "gmtModified": "2018-03-20",
                "name": "joints-xinhuo",
                "phone": "",
                "uid": "joints-xinhuo",
                "wangwang": "",
                "password": "d28a4ba2dae7cf47a172a53040d08dca",
                "userType": 0,
                "isEffective": 1,
                "description": "for other"
            },
            {
                "id": 193,
                "email": "sijidou@2dfire.com",
                "gmtCreate": "2017-06-30",
                "gmtModified": "2017-06-30",
                "name": "sijidou",
                "phone": "18668080501",
                "uid": "sijidou",
                "wangwang": "",
                "password": "a906449d5769fa7361d7ecc6aa3f6d28",
                "userType": 0,
                "isEffective": 0,
                "description": "数据查询"
            },
            {
                "id": 194,
                "email": "gantang@2dfire.com",
                "gmtCreate": "2017-07-14",
                "gmtModified": "2017-07-14",
                "name": "gantang",
                "phone": "13031710803",
                "uid": "gantang",
                "wangwang": "",
                "password": "2175597ea239aba6bea233c52d6beea7",
                "userType": 0,
                "isEffective": 0,
                "description": "电子发票报表业务使用。\n账号负责人：甘棠。\n数据量不大，业务刚刚开始，是新表。\n每天有5个job要跑。\n要做数据导入工作，导入量半年内在20W以内，需要保留1天。\n库表权限：需要tis.base_shop\n"
            },
            {
                "id": 195,
                "email": "mogu@2dfire.com",
                "gmtCreate": "2017-07-14",
                "gmtModified": "2017-09-08",
                "name": "cashline",
                "phone": "15397222925",
                "uid": "cashline",
                "wangwang": "",
                "password": "6cb34c2fef61b1dd9cae2de00456411f",
                "userType": 0,
                "isEffective": 1,
                "description": "电子发票业务"
            },
            {
                "id": 196,
                "email": "binglang@2dfire.com",
                "gmtCreate": "2017-08-16",
                "gmtModified": "2017-08-16",
                "name": "binglang",
                "phone": "15975555700",
                "uid": "binglang",
                "wangwang": "",
                "password": "4a50c47c503f2c0e433648bdcbc456df",
                "userType": 0,
                "isEffective": 0,
                "description": "槟榔"
            },
            {
                "id": 197,
                "email": "bingobird@163.com",
                "gmtCreate": "2017-08-26",
                "gmtModified": "2017-08-26",
                "name": "bingguna",
                "phone": "13588418933",
                "uid": "bingguna",
                "wangwang": "",
                "password": "d3f22f8a25c14114e4fd0dfa198da928",
                "userType": 0,
                "isEffective": 1,
                "description": "自行分析数据"
            },
            {
                "id": 198,
                "email": "luobogan@2dfire.com",
                "gmtCreate": "2017-09-01",
                "gmtModified": "2017-09-01",
                "name": "luobogan",
                "phone": "18658316588",
                "uid": "luobogan",
                "wangwang": "",
                "password": "0144c7f2535e1d11bd1af309d750783e",
                "userType": 0,
                "isEffective": 0,
                "description": "萝卜干"
            },
            {
                "id": 199,
                "email": "yuguantou@2dfire.com",
                "gmtCreate": "2017-09-01",
                "gmtModified": "2017-09-01",
                "name": "yuguantou",
                "phone": "18601420446",
                "uid": "yuguantou",
                "wangwang": "",
                "password": "79aada8d77f9365df9540aa886721be8",
                "userType": 0,
                "isEffective": 0,
                "description": "查询member库数据"
            },
            {
                "id": 200,
                "email": "spcatman@qq.com",
                "gmtCreate": "2017-09-01",
                "gmtModified": "2017-09-01",
                "name": "banmiann",
                "phone": "15158116099",
                "uid": "banmiann",
                "wangwang": "",
                "password": "2846decd7cc09b9deb0f398e46192e20",
                "userType": 0,
                "isEffective": 1,
                "description": "监控"
            },
            {
                "id": 201,
                "email": "xiaosuda@2dfire.com",
                "gmtCreate": "2017-10-09",
                "gmtModified": "2017-10-09",
                "name": "xiaosuda",
                "phone": "15669910617",
                "uid": "xiaosuda",
                "wangwang": "",
                "password": "c27b7dc14405f234d8c376f11f24157b",
                "userType": 0,
                "isEffective": 0,
                "description": "a"
            },
            {
                "id": 202,
                "email": "xiaomian@2dfire.com",
                "gmtCreate": "2017-10-30",
                "gmtModified": "2017-10-30",
                "name": "finpay",
                "phone": "15867576339",
                "uid": "finpay",
                "wangwang": "",
                "password": "ad69acc1f3b423a496c6aac451a3a9a1",
                "userType": 0,
                "isEffective": 1,
                "description": "金融支付组使用。目前是数据导入，之后会涉及业务开发"
            },
            {
                "id": 203,
                "email": "doufugan@2dfire.com",
                "gmtCreate": "2017-10-31",
                "gmtModified": "2017-10-31",
                "name": "doufugan",
                "phone": "13588195243",
                "uid": "doufugan",
                "wangwang": "",
                "password": "96e79218965eb72c92a549dd5a330112",
                "userType": 0,
                "isEffective": -1,
                "description": "会员营销"
            },
            {
                "id": 204,
                "email": "doufugan@2dfire.com",
                "gmtCreate": "2017-10-31",
                "gmtModified": "2017-10-31",
                "name": "marketing",
                "phone": "13588195243",
                "uid": "marketing",
                "wangwang": "",
                "password": "96e79218965eb72c92a549dd5a330112",
                "userType": 0,
                "isEffective": 1,
                "description": "会员营销"
            },
            {
                "id": 205,
                "email": "fangfeng@2dfire.com",
                "gmtCreate": "2017-11-30",
                "gmtModified": "2017-11-30",
                "name": "item-center",
                "phone": "17816873960",
                "uid": "item-center",
                "wangwang": "",
                "password": "e10adc3949ba59abbe56e057f20f883e",
                "userType": 0,
                "isEffective": 1,
                "description": "项目：商品中心搜索引擎\n负责人：鱼片\n每天hive导入，大概3kw，涉及job数13个\njob完成之后，数据就可以废弃\n数据库：item_center\n表：item、item_assemble、item_cash_config、item_display_prop、item_label、item_sku、property、property_base、purchase_limit_condition、shop_category、value"
            },
            {
                "id": 206,
                "email": "sijidou@2dfire.com",
                "gmtCreate": "2017-12-11",
                "gmtModified": "2017-12-11",
                "name": "trade",
                "phone": "18668080501",
                "uid": "trade",
                "wangwang": "",
                "password": "58a5b122d783dc398fa1568432964015",
                "userType": 0,
                "isEffective": 1,
                "description": "交易中心业务线\n生抽\n交易相关表\n暂时只需要查询权限"
            },
            {
                "id": 207,
                "email": "xiaosuda@2dfire.com",
                "gmtCreate": "2018-01-12",
                "gmtModified": "2018-01-12",
                "name": "realtime",
                "phone": "15669910617",
                "uid": "realtime",
                "wangwang": "",
                "password": "61249dc3083efb3982e2aa44cd0d5221",
                "userType": 0,
                "isEffective": 1,
                "description": "real time"
            },
            {
                "id": 208,
                "email": "zyzzxycj@163.com",
                "gmtCreate": "2018-01-16",
                "gmtModified": "2018-01-16",
                "name": "heguozi",
                "phone": "13588760167",
                "uid": "heguozi",
                "wangwang": "",
                "password": "b168c9619d5bbb373c2a29bf5e069955",
                "userType": 0,
                "isEffective": 0,
                "description": " "
            },
            {
                "id": 209,
                "email": "aihuo@2dfire.com",
                "gmtCreate": "2018-04-24",
                "gmtModified": "2018-04-24",
                "name": "aihuo",
                "phone": "13857123323",
                "uid": "aihuo",
                "wangwang": "",
                "password": "73c67fe06b272a2489ab33eaadd2b267",
                "userType": 0,
                "isEffective": 0,
                "description": "aihou"
            },
            {
                "id": 210,
                "email": "tangbao@2dfire.com",
                "gmtCreate": "2018-05-21",
                "gmtModified": "2018-05-21",
                "name": "flume",
                "phone": "15216671875",
                "uid": "flume",
                "wangwang": "",
                "password": "e337ffebb228287b70273dce6a15becc",
                "userType": 0,
                "isEffective": 1,
                "description": "用于flume数据实时导入"
            },
            {
                "id": 211,
                "email": "hupo@2dfire.com",
                "gmtCreate": "2018-08-22",
                "gmtModified": "2018-08-22",
                "name": "hupo",
                "phone": "18668530007",
                "uid": "hupo",
                "wangwang": "",
                "password": "83c212c93b61fd951a02b0aafdc25899",
                "userType": 0,
                "isEffective": 0,
                "description": "查看预售数据"
            },
            {
                "id": 212,
                "email": "shuixinzi@2dfire.com",
                "gmtCreate": "2018-09-12",
                "gmtModified": "2018-09-12",
                "name": "marketing_center",
                "phone": "15858163054",
                "uid": "marketing_center",
                "wangwang": "",
                "password": "e10adc3949ba59abbe56e057f20f883e",
                "userType": 0,
                "isEffective": 0,
                "description": "营销中心_数据部门"
            },
            {
                "id": 213,
                "email": "lanxinghua@2dfire.com",
                "gmtCreate": "2018-09-17",
                "gmtModified": "2018-09-17",
                "name": "lanxinghua",
                "phone": "18379643981",
                "uid": "lanxinghua",
                "wangwang": "",
                "password": "b0a018005e9bb1f6a0b424ff659b065b",
                "userType": 0,
                "isEffective": 0,
                "description": "现在还没，先了解一下"
            },
            {
                "id": 214,
                "email": "mengguniu@2dfire.com",
                "gmtCreate": "2018-09-18",
                "gmtModified": "2018-09-18",
                "name": "financial",
                "phone": "15857144369",
                "uid": "financial",
                "wangwang": "",
                "password": "e10adc3949ba59abbe56e057f20f883e",
                "userType": 0,
                "isEffective": 1,
                "description": "金融分析使用账户"
            },
            {
                "id": 215,
                "email": "xuanhusuo@2dfire.com",
                "gmtCreate": "2018-11-12",
                "gmtModified": "2018-11-12",
                "name": "xuanhusuo",
                "phone": "15064067913",
                "uid": "xuanhusuo",
                "wangwang": "",
                "password": "47f027bdd6f3325a0c15050517498e13",
                "userType": 0,
                "isEffective": 0,
                "description": "供应链玄胡索测试使用"
            }
        ];
        oTableInit.init = function () {
            var table = $('#table');
            table.bootstrapTable({
                // url: base_url + '/userManage/initUser.do',
                // method: 'post',
                toolbar: '#toolbar',
                pagination: true,
                cache: false,
                clickToSelect: true,
                striped: false,
                showRefresh: true,           //是否显示刷新按钮
                showPaginationSwitch: true,  //是否显示选择分页数按钮
                pageNumber: 1,              //初始化加载第一页，默认第一页
                pageSize: 10,                //每页的记录行数（*）
                sidePagination: "client",
                pageList: [10, 25, 40, 60],
                search: true,
                uniqueId: 'id',
                columns: [
                    {
                        field: '',
                        title: '序号',
                        formatter: function (val, row, index) {
                            return index + 1;
                        }
                    }, {
                        field: 'uid',
                        title: '用户账号'
                    }, {
                        field: 'name',
                        title: '用户姓名'
                    }, {
                        field: 'email',
                        title: '用户邮箱 '
                    }, {
                        field: 'phone',
                        title: '手机号码'
                    }, {
                        field: 'description',
                        title: '描述'
                    }, {
                        field: 'gmtModified',
                        title: '更新时间'
                    }, {
                        field: 'isEffective',
                        title: '是否审核通过',
                        formatter: function (val) {
                            if (val == 0) {
                                return '<label class = "label label-default" >无效</label>';
                            } else if (val == 1) {
                                return '<label class = "label label-success" >有效</label>';
                            }
                            return val;
                        }
                    }, {
                        title: '操作',
                        formatter: function (val, row, index) {
                            userList[index] = row;
                            indexList[row.id] = index;
                            return '<a href="javascript:edit(\'' + index + '\')"><button id ="editBtn" type="button" class="btn btn-primary">编辑</button></a>&nbsp;' +
                                '<a href="javascript:del(\'' + index + '\')"><button type="button" class="btn btn-danger">删除</button></a>&nbsp;' +
                                '<a href="javascript:approve(\'' + index + '\')"><button type="button" class="btn btn-success">审核通过</button></a>&nbsp;' +
                                '<a href="javascript:refuse(\'' + index + '\')"><button type="button" class="btn btn-info">审核拒绝</button></a>'
                        }
                    }

                ],
                data:test
            });
        }
        return oTableInit;
    }

    var oTable = new TableInit();
    oTable.init();
});

function edit(index) {
    var user = userList[index];
    tinyInt1isBit = false

    $('#editUser #title').text("编辑用户信息");
    $('#editUser #id').text(user.id);
    $('#editUser #name').val(user.name);
    $('#editUser #email').val(user.email);
    $('#editUser #phone').val(user.phone);
    $('#editUser #description').val(user.description);

    $('#editUser').modal('show');
}

function del(index) {
    var user = userList[index];
    $('#confirmModalLabel').text("删除操作");
    $('#hidden_id').text(user.id);
    $('#operateType').text("1");
    $('#confirmModalBody').html("确认要删除: <span style='color: #ff775a; '> " + user.name + " </span>?");
    $('#confirmModal').modal('show');
}

function approve(index) {
    var user = userList[index];
    $('#confirmModalLabel').text("审核通过");
    $('#hidden_id').text(user.id);
    $('#operateType').text("2");
    $('#confirmModalBody').html("审核通过: <span style='color: #ff775a; '> " + user.name + " </span>?");
    $('#confirmModal').modal('show');
}

function refuse(index) {
    var user = userList[index];
    var id = user.id;
    $('#confirmModalLabel').text("审核拒绝");
    $('#hidden_id').text(user.id);
    $('#operateType').text("3");
    $('#confirmModalBody').html("审核拒绝: <span style='color: #ff775a; '> " + user.name + " </span>?");
    $('#confirmModal').modal('show');
}