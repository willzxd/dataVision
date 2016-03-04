/**
 * Created by will on 3/4/16.
 */
var successPreview = function (data) {
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
        $('#previewTableHolder').html(htmls.join(''));
        console.debug("Success:");
        console.debug(data);
};

var errorPreview = function(err) {
    console.debug("Error:");
    console.debug(err);
}

ajaxPreview = {
    success: successPreview,
    error: errorPreview
}
function createPreviewTable(previewTb){
    jsRoutes.controllers.Application.previewTb(previewTb).ajax(ajaxPreview);
}
