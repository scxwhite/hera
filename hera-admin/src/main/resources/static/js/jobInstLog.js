
$(document).ready(function() { 
	id = GetQueryString("id");
	jobId = GetQueryString("jobId"); 
	
    $.ajax({
        url: base_url + "/scheduleCenter/getLog.do",
        type: "get",
        data: {
            id: id,
            jobId: jobId
        },
        success: function (result) {
            var logArea = $('#loginstlogdetail');
            if (result.success === false) {
                layer.msg(result.message);
                logArea[0].innerHTML = "无日志查看权限,请联系管理员进行配置";
                return;
            }
            let data = result.data;
            if (data.status === 'running') {
                window.setTimeout(scheduleLog, 5000);
            }
            logArea[0].innerHTML = data.log;
//            logArea.scrollTop(logArea.prop("scrollHeight"), 200);
//            actionRow.log = data.log;
//            actionRow.status = data.status;
        }
    })

}); 



function GetQueryString(name)
{
     var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
     var r = window.location.search.substr(1).match(reg);
     if(r!=null)return  unescape(r[2]); return null;
}

