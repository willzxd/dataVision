package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;
import views.html.*;

import controllers.SqlConn;

import java.util.ArrayList;
import java.util.List;


public class Application extends Controller {
    public static List<String> dbList;
    public static String dbName;
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
        return ok(index.render(dbList, null, null, null, null));
    }

    /**
     * Used in javascriptRouter to update the list of table
     * @return Result list of table name, format: json
     */
    public static Result getTableList(String db) {
        dbName = db;
        SqlConn sqlWritter = new SqlConn(db);
        tableList = sqlWritter.readTableList();
        sqlWritter.closeCon();
        System.out.println("im");
        return ok(tableList);
    }

    public static Result previewTb() {
        DynamicForm requestData = Form.form().bindFromRequest();
        dbName = requestData.get("dbname");
        System.out.println(dbName);
        String tbname = requestData.get("tbname");
        System.out.println(tbname);
        SqlConn sqlWritter = new SqlConn(dbName);
        //List<String> tbList = sqlWritter.readTableList();
        List<List<String>> preview = sqlWritter.previewTable(tbname);
        sqlWritter.closeCon();
        return ok(index.render(dbList, dbName, null, preview, null));
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
        SqlConn sqlWritter = new SqlConn(dbName);
        List<List<String>> res = sqlWritter.query(select, from, where, groupBy, orderBy, limit);
        return ok(index.render(dbList, dbName, null, null, res));
    }

    // Javascript routing
    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(Routes.javascriptRouter(
                "jsRoutes",
                routes.javascript.Application.getTableList()));
    }
}
