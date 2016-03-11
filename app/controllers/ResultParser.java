package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.api.libs.json.Json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by will on 3/7/16.
 */
public class ResultParser {


    /**
     * Implement swap method for diversity
     * @param resultSet
     * @param k
     * @param tradeOff
     */
    public static JsonNode diversityResult(String resultSet, int k, double tradeOff) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(resultSet);
            //use a List of size k
            //init the set
            List<JsonNode> resultList = new ArrayList<JsonNode>();
            List<JsonNode> candidateList = new ArrayList<>();
            double score = 0;
            int i = 0;
            int size = resultList.size() - 1;
            while (resultList.size() < k) {
                resultList.add(root.get(i));
                i++;
            }
            score = computeScore(resultList, k, tradeOff);
            //traverse the rest tuples
            while (i < root.size()) {
                JsonNode candidate = root.get(i);
                i++;
                for (int j = 0; j < resultList.size(); j++) {
                    JsonNode currentNode = resultList.get(j);
                    resultList.set(j, candidate);
                    double candidateScore = computeScore(resultList, k, tradeOff);
                    if (candidateScore < score) {
                        resultList.set(j, currentNode);
                        //add the candiate to candidateList
                    } else {
                        score = candidateScore;
                    }
                }
            }
            for (JsonNode node: root) {
                if (!resultList.contains(node)) {
                    candidateList.add(node);
                }
            }
            //System.out.println("size of candidateList" + candidateList.size());
            //add coverage
            Map<JsonNode, ArrayNode> finalResultMap = new HashMap<>();
            for (JsonNode node: resultList) {
                finalResultMap.put(node, mapper.createArrayNode());
            }
            //System.out.println("Init Map");
            for (int p = 0; p < candidateList.size(); p++) {
                double distance = Double.MAX_VALUE;
                JsonNode parent = null;
                for (JsonNode node: resultList) {
                    double currentDis = computeDistance(node, candidateList.get(p));
                    if (distance > currentDis) {
                        distance = currentDis;
                        parent = node;
                    }
                }
                //System.out.println(candidateList.get(p).toString());
                finalResultMap.get(parent).add(candidateList.get(p));
            }
            //System.out.println("Map built.");
            ArrayNode finalResult = mapper.createArrayNode();
            for (int p = 0; p < resultList.size(); p++) {
                ObjectNode current = mapper.createObjectNode();
                current.put("diversity_" + p, resultList.get(p));
                current.put("coverage_" + p, finalResultMap.get(resultList.get(p)));
                finalResult.add(current);
            }
            //System.out.println("final build.");
            String resultStr = mapper.writeValueAsString(finalResult);
            System.out.println(resultStr);
            JsonNode result = mapper.readTree(resultStr);
            return result;
        } catch (JsonProcessingException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
        return null;
    }

    private static double computeDistance(JsonNode a, JsonNode b) {
        if (a == null || b == null) {
            return Double.MAX_VALUE;
        }
        double sum = 0.0;
        Iterator<String> it = a.fieldNames();
        while (it.hasNext()) {
            String fieldName = it.next().toString();
            if (!fieldName.equals("val")) {
                sum += Math.abs(a.get(fieldName).asDouble(0) - b.asDouble(0));
            }
        }
        return sum;

    }
    private static double computeScore(List<JsonNode> nodeList, int k, double tradeOff) {
        if (nodeList == null || nodeList.size() == 0) {
            return 0;
        }
        return (k - 1) * (1 - tradeOff) * computeSim(nodeList) + 2 * tradeOff * computeDiv(nodeList);
    }

    private static double computeSim(List<JsonNode> nodeList) {
        if (nodeList == null) {
            return 0;
        }
        double sum = 0.0;
        for (JsonNode sim: nodeList) {
            sum += sim.get("val").asDouble();
        }
        return sum;
    }

    private static double computeDiv(List<JsonNode> nodeList) {
        if (nodeList == null) {
            return 0;
        }
        double sum = 0.0;
        for (int i = 0; i < nodeList.size() - 1; i++) {
            for (int j = i + 1; j < nodeList.size(); j++) {
                Iterator<String> it = nodeList.get(0).fieldNames();
                while (it.hasNext()) {
                    String fieldName = it.next().toString();
                    if (!fieldName.equals("val")) {
                        sum += Math.abs(nodeList.get(i).get(fieldName).asDouble(0) - nodeList.get(j).get(fieldName).asDouble(0));
                    }
                }
            }
        }
        return sum;
    }
}
