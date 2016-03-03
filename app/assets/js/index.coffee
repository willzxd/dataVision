$(document).ready ->
  $.get "/readTables", (tables) ->
    $.innerHTML(tables)
    $.each tables, (table) ->
      $('#tbname').append($("<option></option>").attr("value",key).text(value))