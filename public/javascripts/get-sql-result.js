/**
 * Created by will on 3/6/16.
 */
$(document).ready(function() {
    $("#submitSql").click(getSqlResult);
});
var successGetSqlResult = function (data) {
    if (data == null) {
        return errorGetSqlResult("data is null");
    }
    var htmls = ['<table class="table table-striped">'];
    htmls.push('<thead><tr>');
    for (var k in data[0]) {
        htmls.push('<th>' + k + '</th>');
    }
    htmls.push("</tr></thead>");
    for (var i = 0, L = data.length; i < L; i++) {
        htmls.push("<tr>");
        for (var k in data[i]) htmls.push('<td>' + data[i][k] + '</td>');
        htmls.push('</tr>');
    }
    htmls.push('</table>');
    $('#sqlResultHolder').html(htmls.join(''));
    console.debug("Success:");
    console.debug(data);
};

var errorGetSqlResult = function(err) {
    console.debug("Error:");
    console.debug(err);
}

ajaxGetSqlResult = {
    success: successGetSqlResult,
    error: errorGetSqlResult
}
function getSqlResult(){
    var sql = $("#sqlInput").val();
    var diversity = $("#slider").val();
    console.debug(sql);
    jsRoutes.controllers.Application.getSqlResult(sql, diversity).ajax(ajaxGetSqlResult);
}
