package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.api.libs.json.Json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by will on 3/7/16.
 */
public class ResultParser {

    public double relevanceFunction() {

        return 0;
    }

    public double diversityFunction() {
        return 0;
    }

    public double coverageFunction() {
        return 0;
    }

    /**
     * Implement swap method
     * @param resultSet
     * @param k
     * @param tradeOff
     */
    public static JsonNode diversityResult(String resultSet, int k, double tradeOff) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(resultSet);
            //use a List of size k
            List<JsonNode> resultList = new ArrayList<JsonNode>();
            List<Integer> resultIndex = new ArrayList<Integer>();
            double score = 0;
            int i = 0;
            int size = resultList.size() - 1;
            while (resultList.size() < k) {
                resultList.add(root.get(i));
                resultIndex.add(size - i);
                i++;
            }
            score = computeScore(resultList, resultIndex, k, tradeOff);
            while (i < root.size()) {
                JsonNode candidate = root.get(i);
                i++;
                for (int j = 0; j < resultList.size(); j++) {
                    JsonNode waitingList = resultList.get(j);
                    int waitingIndex = resultIndex.get(j);
                    resultList.set(j, candidate);
                    resultIndex.set(j, size - i);
                    double candidateScore = computeScore(resultList, resultIndex, k, tradeOff);
                    if (candidateScore < score) {
                        resultList.set(j, waitingList);
                        resultIndex.set(j, waitingIndex);
                    } else {
                        score = candidateScore;
                    }
                }
            }
            String resultStr = mapper.writeValueAsString(resultList);
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

    private static double computeScore(List<JsonNode> nodeList, List<Integer> nodeIndex, int k, double tradeOff) {
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
