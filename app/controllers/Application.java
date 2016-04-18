package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;
import views.html.*;

import java.util.ArrayList;
import java.util.List;


public class Application extends Controller {
    /**
     * List of database
     */
    public static List<String> dbList;
    /**
     * current selected database
     */
    public static String currentDatabase;
    public static JsonNode tableList;


    /**
     * This method is used to show the index page
     * @return
     */
    public static Result index() {
        SqlConn sqlWritter = new SqlConn("will");
        dbList = sqlWritter.readDbList();
        sqlWritter.closeCon();
        List<String> tbList = new ArrayList<>();
        return ok(index.render(dbList, null, null));
    }

    /**
     * Used in javascriptRouter to update the list of table
     * @return Result list of table name, format: json
     */
    public static Result getTableList(String db) {
        currentDatabase = db;
        SqlConn sqlWritter = new SqlConn(db);
        tableList = sqlWritter.readTableList();
        sqlWritter.closeCon();
        System.out.println(currentDatabase);
        return ok(tableList);
    }

    /**
     * Given a table name, output the first 5 row of the table as Json
     * Used in javascriptRouter
     * @return Result, format: Json
     */
    public static Result previewTb(String tableName) {
        SqlConn sqlWritter = new SqlConn(currentDatabase);
        //List<String> tbList = sqlWritter.readTableList();
        JsonNode preview = sqlWritter.previewTable(tableName);
        sqlWritter.closeCon();
        return ok(preview);
        //return ok(previewTb.render("DataVision", preview));
    }

    /**
     *
     * @param sql
     * @param topk
     * @param cov
     * @param dist
     * @return
     */
    public static Result getSqlResult(String sql, int topk, int cov, int dist, String algo) {
        //query database
        SqlConn sqlWritter = new SqlConn(currentDatabase);
        JsonNode res = sqlWritter.queryTopK(sql, topk, cov, dist, algo);
        return ok(res);
    }

    // Javascript routing
    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(Routes.javascriptRouter(
                "jsRoutes",
                routes.javascript.Application.getTableList(),
                routes.javascript.Application.previewTb(),
                routes.javascript.Application.getSqlResult()));
    }
}
