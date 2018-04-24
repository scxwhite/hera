function formDataLoad(domId, obj) {
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
                        $(dom).val(obj[property]);
                    }
                    else if ($(dom).prop("tagName") == "TEXTAREA") {
                        $(dom).val(obj[property]);
                    } else {
                        $(dom).val(obj[property]);
                    }
                });
            }
        }
    }
}