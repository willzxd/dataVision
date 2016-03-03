/**
 * Created by will on 3/2/16.
 */
$(function() {

    ajaxCall();

});

var ajaxCall = function() {
    var ajaxCallBack = {
        success : onSuccess,
        error : onError
    }

    jsRoutes.controllers.Application.getTableList.ajax(ajaxCallBack);
};

var  onSuccess = function(data) {
    alert(data);
}

var onError = function(error) {
    alert(error);
}
