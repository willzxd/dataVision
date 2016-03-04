package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 2/7/16.
 */
public class SqlConn {
    public Connection conn = null;
    //private String database;
    public static String USER = "will";
    public static String PASSWORD = "will";

    public SqlConn(String database) {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost/" + database + "?user=" + USER + "&password=" + PASSWORD;
            conn = DriverManager.getConnection(url);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean closeCon() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public List<String> readDbList() {
        Statement statement;
        List<String> list = new ArrayList<String>();
        try {
            statement = conn.createStatement();
            ResultSet res = statement.executeQuery("select datname from pg_database");
            while (res.next()) {
                list.add(res.getString("datname"));
            }
            return list;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return list;
        }
    }

    /**
     * output list of tables in a given name of database
     * @return a JsonNode object. It contains an array of table. The key is tablename, value is names of table
     */
    public JsonNode readTableList() {
        Statement statement;
        String jsonResult = null;
        JsonNode result = null;
        try {
            statement = conn.createStatement();
            //get the json string directly from database
            ResultSet res = statement.executeQuery("select array_to_json(array_agg(row_to_json(t))) from (select tablename from pg_tables " +
                    "where schemaname != 'pg_catalog' and schemaname != 'information_schema') t");
            while (res.next()) {
                jsonResult = res.getString(1);

            }
            result = Json.parse(jsonResult);
            return result;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return result;
        }
    }

    public JsonNode previewTable(String tbName) {
        Statement statement;
        JsonNode preview = null;
        try {
            //execute query
            statement = conn.createStatement();
//            ResultSet res = statement.executeQuery("select column_name from information_schema.columns where table_name = '" +
//                    tbName + "'");
//            List<String> columnList = new ArrayList<>();
//            //find columns name
//            while (res.next()) {
//                columnList.add(res.getString("column_name"));
//            }
//            preview.add(columnList);
            ResultSet res = statement.executeQuery("select array_to_json(array_agg(row_to_json(t))) from ("
                    +"select * from " + tbName + " limit 5) t");
            //find res of query
            while (res.next()) {
                preview = Json.parse(res.getString(1));
            }
            return preview;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return preview;
        }
    }
    public List<List<String>> query(String select, String from, String where, String group, String order, String limit) {
        Statement statement;
        List<List<String>> res = new ArrayList<List<String>>();
        try {
            //execute query
            statement = conn.createStatement();
            StringBuilder query = new StringBuilder();
            query.append("select " + select + " from " + from);
            if (where != null && where.trim().length() != 0) {
                query.append(" where " + where.trim());
            }
            if (group != null && group.trim().length() != 0) {
                query.append(" group by " + group.trim());
            }
            if (order != null && order.trim().length() != 0) {
                query.append(" order by " + order.trim());
            }
            if (limit != null && limit.trim().length() != 0) {
                query.append(" limit " + limit.trim());
            }
            System.out.println(query.toString());
            ResultSet ans = statement.executeQuery(query.toString());

            //store column name
//            int numCol = ans.getMetaData().getColumnCount();
//            List<String> colList = new ArrayList<>();
//            for (int i = 1; i <= numCol; i++) {
//                String colName = ans.getMetaData().getColumnName(i);
//                colList.add(colName);
//            }
//            res.add(colList);
//            //store every tuples
//            while (ans.next()) {
//                List<String> row = new ArrayList<String>();
//                for (String str: colList) {
//                    row.add(ans.getString(str));
//                }
//                res.add(row);
//            }

            //every list: column name + data
            int numCol = ans.getMetaData().getColumnCount();
            for (int i = 1; i <= numCol; i++) {
                List<String> column = new ArrayList<>();
                String colName = ans.getMetaData().getColumnName(i);
                column.add(colName);
                res.add(column);
            }
            while (ans.next()) {
                int i = 0;
                while (i < res.size()) {
                    res.get(i).add(ans.getString(res.get(i).get(0)));
                    i++;
                }
            }
            for(List<String> list: res) {
                list.remove(0);
            }
            for (List<String> list: res) {
                for (String str: list) {
                    System.out.println(str);
                }
            }
            return res;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return res;
        }
    }
}
