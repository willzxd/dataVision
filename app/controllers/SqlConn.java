package controllers;

import algorithm.FindAnswer;
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

    /**
     * (Old version) Only consider the diversity and relevance
     * @param sql
     * @param tradeOff
     * @return
     */
    public JsonNode query(String sql, double tradeOff) {
        Statement statement;
        JsonNode result = null;
        try {
            //execute query
            statement = conn.createStatement();
            String normalSql = null;
            int top = 5;
            if (sql.contains("top")) {
                 normalSql = sql.substring(0, sql.indexOf("top") + 1);
                 top = Integer.parseInt(sql.substring(sql.indexOf("top") + 3).trim());
            } else {
                normalSql = sql;
            }
            System.out.println("top: " + top);
            System.out.println("sql: " + normalSql);
            ResultSet resultSet = statement.executeQuery("select array_to_json(array_agg(row_to_json(t))) from (" + normalSql + ") t");
            while (resultSet.next()) {
                //System.out.println(resultSet.getString(1));
                String res = resultSet.getString(1);
                result = ResultParser.diversityResultWithGMC(res, top, tradeOff);
                //result = Json.parse(resultSet.getString(1));

            }
            return result;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return result;
        }
    }

    public JsonNode queryTopK(String sql) {
        Statement statement;
        JsonNode result = null;
        try {
            //execute query
            statement = conn.createStatement();
            String normalSql = null;
            //default topK, coverage, and distance
            int topK = 8;
            int coverage = 40;
            int distance = 3;
            if (sql.contains("top")) {
                normalSql = sql.substring(0, sql.indexOf("top") + 1);
                topK = Integer.parseInt(sql.substring(sql.indexOf("top") + 3).trim());
            } else {
                normalSql = sql;
            }
            //System.out.println("top: " + top);
            //System.out.println("sql: " + normalSql);
            ResultSet resultSet = statement.executeQuery("select array_to_json(array_agg(row_to_json(t))) from (" + normalSql + ") t");
            while (resultSet.next()) {
                //System.out.println(resultSet.getString(1));
                String res = resultSet.getString(1);
                System.out.println("Original result: " + res);
                FindAnswer finder = new FindAnswer();
                result = finder.findclustersBT(res, topK, coverage, distance);
                //result = finder.findclustersGreedy(res, topK, coverage, distance);
                //result = ResultParser.diversityResultWithGMC(res, top, tradeOff);
                //result = Json.parse(resultSet.getString(1));
                //k=10,coverage=20,distance=4, greedy25.89022907761008, bt34.59936431569112(not to bad)
            }
            return result;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return result;
        }
    }
}
