package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.api.data.ObjectMapping;

import java.io.IOException;
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
            sum += computeNodeRelevance(sim);
        }
        return sum;
    }
    private static double computeNodeRelevance(JsonNode node) {
        if (node == null) {
            return 0;
        }
        //System.out.println("NodeRelevance" + node.toString() + node.get("val").asDouble() );
        return node.get("val").asDouble();
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

    public static JsonNode diversityResultWithGMC (String result, int k, double tradeOff) {
        System.out.println("tradeOff: " + tradeOff);
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(result);
            //key:Si belongs to S, value: key: Sj belongs to S, value: diversity score of Si and Sj
            Map<String, Map<String, Double>> diversityScoreMap = new HashMap<String, Map<String, Double>>();
            //result set R
            List<JsonNode> resultSet = new ArrayList<JsonNode>();
            List<JsonNode> candidateSet = new ArrayList<JsonNode>();

            //init candiateSet
            for (JsonNode node: root) {
                candidateSet.add(node);
            }
            //init diversityScoreMap
            //may use a graph instead to save space
            for(int i = 0; i < candidateSet.size(); i++) {
                diversityScoreMap.put(mapper.writeValueAsString(candidateSet.get(i)), new HashMap<String, Double>());
                for (int j = 0; j < candidateSet.size(); j++) {
                    double score = computeNodesDiversity(candidateSet.get(i), candidateSet.get(j));
                    diversityScoreMap.get(mapper.writeValueAsString(candidateSet.get(i)))
                            .put(mapper.writeValueAsString(candidateSet.get(j)), score);
                }
            }

//            for(String key: diversityScoreMap.keySet()) {
//                for (String key2: diversityScoreMap.get(key).keySet()) {
//                    System.out.println("diversityScoreMap: key: " + key + " key2: " + key2 + " value: " + diversityScoreMap.get(key).get(key2));
//                }
//            }
            //iterate Set S to consist size-k result set R.
            for (int i = 0; i < k; i++) {
                JsonNode candidate = null;
                double candidateScore = Double.MIN_VALUE;
                for (JsonNode node: candidateSet) {
                    //compute mmc(Si)
                    double score = (1 - tradeOff) * computeNodeRelevance(node)
                            + tradeOff / (k - 1) * computeNodeDviersityWithResultSet(node, resultSet)
                            + tradeOff / (k - 1) * computeNodeDiversityWithCandidateSet(k, resultSet.size() + 1,
                            diversityScoreMap, candidateSet, node);
                    System.out.println(node.toString() + " score: " + score);
                    if (score > candidateScore) {
                        candidateScore = score;
                        candidate = node;
                    }
                }
                resultSet.add(candidate);
                candidateSet.remove(candidate);
            }
            for (JsonNode node: resultSet) {
                System.out.println("result:" + node.toString());
            }

            //add coverage
            Map<JsonNode, ArrayNode> finalResultMap = new HashMap<>();
            for (JsonNode node: resultSet) {
                finalResultMap.put(node, mapper.createArrayNode());
            }
            //System.out.println("Init Map");
            for (int p = 0; p < candidateSet.size(); p++) {
                double distance = Double.MAX_VALUE;
                JsonNode parent = null;
                for (JsonNode node: resultSet) {
                    double currentDis = computeDistance(node, candidateSet.get(p));
                    if (distance > currentDis) {
                        distance = currentDis;
                        parent = node;
                    }
                }
                //System.out.println(candidateList.get(p).toString());
                finalResultMap.get(parent).add(candidateSet.get(p));
            }
            //System.out.println("Map built.");
            ArrayNode finalResult = mapper.createArrayNode();
            for (int p = 0; p < resultSet.size(); p++) {
                ObjectNode current = mapper.createObjectNode();
                current.put("diversity_" + p, resultSet.get(p));
                current.put("coverage_" + p, finalResultMap.get(resultSet.get(p)));
                finalResult.add(current);
            }

            //output result
            String resultStr = mapper.writeValueAsString(finalResult);
            System.out.println(resultStr);
            JsonNode finalSet = mapper.readTree(resultStr);
            return finalSet;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Compute diversity between a set of nodes and a node
     * @param node
     * @param resultList
     * @return
     */
    private static double computeNodeDviersityWithResultSet(JsonNode node, List<JsonNode> resultList) {
        if (node == null || resultList == null || resultList.size() == 0) {
            return 0;
        }
        double diversity = 0;
        for (JsonNode otherNode: resultList) {
            diversity += computeNodesDiversity(node, otherNode);
        }
        return diversity;
    }

    /**
     * Compute diversity between two nodes
     * @param node1
     * @param node2
     * @return
     */
    private static double computeNodesDiversity(JsonNode node1, JsonNode node2) {
        if (node1 == null || node2 == null) {
            return 0;
        }
        double diversity = 0;
        Iterator<String> it = node1.fieldNames();
        while (it.hasNext()) {
            String fieldName = it.next().toString();
            if (!fieldName.equals("val")) {
                diversity += Math.abs(node1.get(fieldName).asDouble(0) - node2.get(fieldName).asDouble(0));
            }
        }
        return diversity;
    }

    /**
     * Compute the third part of MMC function for GMC methond
     * @param k the size of result set
     * @param p p - 1 is the size of the partial result
     * @param diversityScoreMap
     * @param candidateSet
     * @param node
     * @return
     */
    private static double computeNodeDiversityWithCandidateSet(int k,
                                                               int p,
                                                               Map<String, Map<String, Double>> diversityScoreMap,
                                                               List<JsonNode> candidateSet,
                                                               JsonNode node) {
        if (diversityScoreMap == null || node == null) {
            return 0;
        }
        PriorityQueue<Double> scoreRank = new PriorityQueue<Double>(11, new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                if (o1 - o2 < 0.0) {
                    return 1;
                } else if (o1 - o2 > 0.0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
//        for(String key: diversityScoreMap.keySet()) {
//            for (String key2: diversityScoreMap.get(key).keySet()) {
//                System.out.println("Incomputer function: diversityScoreMap: key: " + key + " key2: " + key2 + " value: " + diversityScoreMap.get(key).get(key2));
//            }
//        }
        ObjectMapper mapper = new ObjectMapper();
        for (JsonNode candidate: candidateSet) {
            double tmp = 0;
            try {
                tmp = diversityScoreMap.get(mapper.writeValueAsString(node)).get(mapper.writeValueAsString(candidate));
                //System.out.println(tmp);
                scoreRank.offer(tmp);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        double diversity = 0;
        for (int l = 1; l <= k - p; l++) {
            diversity += scoreRank.poll();
        }
        return diversity;
    }
}
