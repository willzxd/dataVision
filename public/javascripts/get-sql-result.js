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
    var htmls = ['<table class="table table-expandable">'];
    htmls.push('<thead><tr>');
    for (var i in data[0]) {
        if (i.indexOf("diversity") >= 0) {
            for (var j in data[0][i]) {
                htmls.push('<th>' + j + '</th>');
                console.log("schema: " + i);
            }
        }
    }
    htmls.push("<th width='16px'>Expand</th>");
    htmls.push("</tr></thead>");
    for (var i = 0, L = data.length; i < L; i++) {
        console.log(data[i]);
        for (var j in data[i]) {
            //
            console.log(j);
            if (j.indexOf("diversity") >= 0) {
                htmls.push("<tr class='diversity expand' id='cluster_"+ i +"'>");
                for (var k in data[i][j]) {
                    //
                    console.log(k);
                    htmls.push('<td>' + data[i][j][k] + '</td>');
                    console.log(data[i][j][k]);
                }
                htmls.push("<td><div class='arrow'></div></td>");
                htmls.push("</tr>");
            } else {
                for (var k = 0; k < data[i][j].length; k++) {
                    //
                    console.log(data[i][j][k]);
                    htmls.push("<tr class='coverage child_cluster_" + i + "'>");
                    for (var l in data[i][j][k])
                        htmls.push('<td>' + data[i][j][k][l] + '</td>');
                    htmls.push("<td width='16px'><div></div></td>");
                    htmls.push("</tr>");
                }
            }
        }
    }
    htmls.push('</table>');
    $('#sqlResultHolder').html(htmls.join(''));
    $('.coverage').hide();
    $("tr.expand").on("click", function(event){
        console.log("blind expand" + this);
        $(this).find(".arrow").toggleClass("up");
        $(this).siblings('.child_' + this.id).toggle();
    });

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
