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
//        DynamicForm requestData = Form.form().bindFromRequest();
//        currentDatabase = requestData.get("dbname");
//        System.out.println(currentDatabase);
//        String tbname = requestData.get("tbname");
//        System.out.println(tbname);
        SqlConn sqlWritter = new SqlConn(currentDatabase);
        //List<String> tbList = sqlWritter.readTableList();
        JsonNode preview = sqlWritter.previewTable(tableName);
        sqlWritter.closeCon();
        return ok(preview);
        //return ok(previewTb.render("DataVision", preview));
    }

    public static Result query() {
        //process form
        DynamicForm requestData = Form.form().bindFromRequest();
        String select = requestData.get("select");
        String from = requestData.get("from");
        String where = requestData.get("where");
        String groupBy = requestData.get("group");
        String orderBy = requestData.get("order");
        String limit = requestData.get("limit");
        //query database
        SqlConn sqlWritter = new SqlConn(currentDatabase);
        List<List<String>> res = sqlWritter.query(select, from, where, groupBy, orderBy, limit);
        return ok(index.render(dbList, currentDatabase, res));
    }

    // Javascript routing
    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(Routes.javascriptRouter(
                "jsRoutes",
                routes.javascript.Application.getTableList(),
                routes.javascript.Application.previewTb()));
    }
}
