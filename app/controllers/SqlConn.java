package controllers;

import algorithm.FindAnswer;
import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
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
    public final static int BRUTE_FORCE = 0;
    //public static int TOP_DOWN = 1;
    public final static int  GREEDY = 1;
    public final static int GREEDY_RANDOM = 2;
    public final static int GREEDY_INDIV = 3;
    public final static int GREADY_FIXED_K_NUM = 4;
    public final static int GREEDY_FIXED_K_WEIGHT = 5;
    public final static int GREEDY_FIXED_K_AVERAGE = 6;
    public final static int GREEDY_FIXED_L_NUM = 7;
    public final static int GREEDY_FIXED_L_WEIGHT = 8;
    public final static int GREEDY_FIXED_L_AVERAGE = 9;


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

    public JsonNode queryTopK(String sql, int topK, int coverage, int distance, String algo) {
        Statement statement;
        JsonNode result = null;
        try {
            //execute query
            statement = conn.createStatement();
            String normalSql = null;
            //find topK, coverage, and distance
//            int topKInState = -1;
//            int coverageInState = -1;
//            int distanceInState = -1;
            System.out.println(algo);
            //For experiment purpose, we can use 1 - 9 select an algorithm
            //TODO: after the experiment, we can write logic here to select the best algorithm for specific SQL query
            int algorithm = Integer.parseInt(algo);
            //For experiment purpose, the SQL for outputing clusters should have "limit".
            if(sql.contains("limit")) {
                normalSql = sql.substring(0, sql.indexOf("limit")).trim();
            } else {
                normalSql = sql;
            }

//            int algorithm = algo.equals("BF")? BRUTE_FORCE : TOP_DOWN;
//            if (sql.contains("coverage") && sql.contains("using")) {
//                //find topK, coverage and distance in statement
//                normalSql = sql.substring(0, sql.indexOf("limit"));
//                topKInState = Integer.parseInt(sql.substring(sql.indexOf("limit") + 6, sql.indexOf("coverage")).trim());
//                coverageInState = Integer.parseInt(sql.substring(sql.indexOf("coverage") + 9, sql.indexOf("distance")).trim());
//                if(sql.contains("distance")) {
//                    distanceInState = Integer.parseInt(sql.substring(sql.indexOf("distance") + 9, sql.indexOf("using")).trim());
//                }
//                //If the parameters in SQL is conflict with the parameters in explicit input boxes,
//                //We use the parameters in boxes.
//                if (topKInState != -1 && topK != topKInState) {
//                    topK = topKInState;
//                }
//                if (coverageInState != -1 && coverage != coverageInState) {
//                    coverage = coverageInState;
//                }
//                if (distanceInState != -1 && distance != distanceInState) {
//                    distance = distanceInState;
//                }
//            } else {
//                normalSql = sql;
//            }

            Logger.info("Normal SQL:" + normalSql);
            Logger.info("k=" + topK + " L=" + coverage + " D=" + distance);
            ResultSet resultSet = statement.executeQuery("select array_to_json(array_agg(row_to_json(t))) from (" + normalSql + ") t");
            while (resultSet.next()) {
                //System.out.println(resultSet.getString(1));
                String res = resultSet.getString(1);
                System.out.println("Original result: " + res);
                FindAnswer finder = new FindAnswer();
                switch(algorithm) {
                    case BRUTE_FORCE: result = finder.findclustersBT(res, topK, coverage, distance);
                        break;
                    case GREEDY: result = finder.findclustersGreedy(res, topK, coverage, distance);
                        break;
                    case GREEDY_RANDOM: result = finder.findclustersGreedyRandom(res, topK, coverage, distance);
                        break;
                    case GREEDY_INDIV: result = finder.findclustersGreedyIndiv(res, topK, coverage, distance);
                        break;
                    case GREADY_FIXED_K_NUM: result = finder.findclustersGreedyFiexdKNum(res, topK, coverage, distance);
                        break;
                    case GREEDY_FIXED_K_WEIGHT: result = finder.findclustersGreedyFixedKWeight(res, topK, coverage, distance);
                        break;
                    case GREEDY_FIXED_K_AVERAGE: result = finder.findclustersGreedyFixedKAverage(res, topK, coverage, distance);
                        break;
                    case GREEDY_FIXED_L_NUM: result = finder.findclustersGreedyFixedLNum(res, topK, coverage, distance);
                        break;
                    case GREEDY_FIXED_L_WEIGHT: result = finder.findclustersGreedyFixedLWeight(res, topK, coverage, distance);
                        break;
                    case GREEDY_FIXED_L_AVERAGE: result = finder.findclustersGreedyFixedLAverage(res, topK, coverage, distance);
                        break;
                    default: result = Json.parse(res);
                        break;
                }
//                if (algorithm == BRUTE_FORCE) {
//                    result = finder.findclustersBT(res, topK, coverage, distance);
//                } else if (algorithm == TOP_DOWN) {
//                    result = finder.findclustersGreedy(res, topK, coverage, distance);
//                } else {
//                    result = Json.parse(res);
//                }
                //result = ResultParser.diversityResultWithGMC(res, top, tradeOff);
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
