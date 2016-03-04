/**
 * Created by will on 3/3/16.
 */
$(document).ready(function() {
    $(".clickable").click(changed);
});



var successFn = function(data) {
    var oldTableList = document.getElementById("tableList");
    for(var i in data) {
        for (var key in data[i]) {
            $("#tableList").append("<li class='list-group-item previewable'" + "id='" + data[i][key] +"'>" + data[i][key] + "</li>");
            //newTableList.add(new Option(data[i][key],data[i][key]));
            console.debug("Success:");
            console.debug(data[i][key]);
        }
    }
    console.debug("Success:");
    console.debug(data);
};
var errorFn = function(err) {
    console.debug("Error:");
    console.debug(err);
}

ajax1 = {
    success: successFn,
    error: errorFn
}
function changed(e){
    var databaseName = e.target.id;
    jsRoutes.controllers.Application.getTableList(databaseName).ajax(ajax1);
}