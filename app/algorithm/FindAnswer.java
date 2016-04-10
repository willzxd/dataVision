package algorithm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.api.libs.json.Json;

import java.util.*;

/**
 * A brutal force algorithm for find top-k clusters
 * Created by will on 4/6/16.
 */
public class FindAnswer {

    JsonNode root = null;
    JsonNode result = null;
    //list of all tuples
    List<JsonNode> tupleCollection = new ArrayList<JsonNode>();
    //list of all cluster
    List<ClusterNode> clusterCollection = new ArrayList<ClusterNode>();
    //list of all attributes
    List<String> attributeList = new ArrayList<String>();
    //map of attributes and their value list
    Map<String, Set<String>> attributeMap = new HashMap<String, Set<String>>();
    //set of covered tuples
    Set<JsonNode> coveredTuples = new HashSet<JsonNode>();

    public
    private void init(String jsonStr, int k, int coverage, int distance) {

    }
    public static JsonNode findclusters(String jsonStr, int k, int coverage, int distance) {
        if (jsonStr == null || jsonStr.length() == 0) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        //root of all nodes from result
        JsonNode root = null;
        JsonNode result = null;
        //list of all tuples
        List<JsonNode> tupleCollection = new ArrayList<JsonNode>();
        //list of all cluster
        List<ClusterNode> clusterCollection = new ArrayList<ClusterNode>();
        //list of all attributes
        List<String> attributeList = new ArrayList<String>();
        //map of attributes and their value list
        Map<String, Set<String>> attributeMap = new HashMap<String, Set<String>>();
        //set of covered tuples
        Set<JsonNode> coveredTuples = new HashSet<JsonNode>();

        try {
            //construct attributeList
            root = mapper.readTree(jsonStr);
            JsonNode example = root.size() == 0? null: root.get(0);
            if (example != null) {
                Iterator<String> field = example.fieldNames();
                while (field.hasNext()) {
                    String attributeName = field.next();
                    if (!attributeName.equals("val")) {
                        attributeList.add(attributeName);
                        attributeMap.put(attributeName, new HashSet<String>());
                        attributeMap.get(attributeName).add("*");
                    }
                }
            }
            //Test attributeList
//            System.out.println("AttributeList is following.");
//            for (String attribute: attributeList) {
//                System.out.print(attribute + ", ");
//            }
//            System.out.println("");

            //Construct list of all tuples and put values in the attributeMap
            for (JsonNode tuple : root) {
                tupleCollection.add(tuple);
                for (String attribute: attributeList) {
                    attributeMap.get(attribute).add(tuple.get(attribute).asText());
                }
            }
            //Test list of tuples and attributeMap
//            System.out.println("Tuple List: ");
//            for (JsonNode t: tupleCollection) {
//                System.out.println(t.toString());
//            }
//            System.out.println("AttributeMap: ");
//            for (String attribute: attributeMap.keySet()) {
//                System.out.print(attribute + ": ");
//                Set<String> values = attributeMap.get(attribute);
//                for (String value: values) {
//                    System.out.print(value + " ");
//                }
//                System.out.println("");
//            }

            //Construct coverage set
            //System.out.println("The tuples should be covered: ");
            for (int i = 0; i < coverage && i < tupleCollection.size(); i++) {
                //System.out.println(tupleCollection.get(i).toString());
                coveredTuples.add(tupleCollection.get(i));
            }
            //Construct clusterCollection
            ObjectNode clusterContent = mapper.createObjectNode();
            constructClusterList(clusterCollection, clusterContent, attributeList, attributeMap, 0);

            //Test clusterCollection
//            System.out.println("All clusters: ");
//            for (ClusterNode cluster: clusterCollection) {
//                System.out.println(cluster.getContent().toString());
//            }

            //build edges between clusters and tuples
            for (JsonNode t: tupleCollection) {
                for (ClusterNode cluster: clusterCollection) {
                    JsonNode currentClusterContent = cluster.getContent();
                    boolean hasEdge = true;
                    for (String attribute: attributeList) {
                        if (currentClusterContent.get(attribute).asText().equals("*")) {
                            continue;
                        } else if (!t.get(attribute).asText().equals(currentClusterContent.get(attribute).asText())) {
                            hasEdge = false;
                            break;
                        }
                    }
                    if (hasEdge == true) {
                        //System.out.println("Edge between:" + cluster.getContent().toString() +", " + t.toString());
                        cluster.addTuples(t);
                        assert(cluster.getTupleList().contains(t));
                    }
                }
            }

            //build edges between clusters
            for (ClusterNode first: clusterCollection) {
                for (ClusterNode second: clusterCollection) {
                    if (first == second) {
                        continue;
                    }
                    boolean hasDomination = true;
                    for (String attribute: attributeList) {
                        //attribute values of two cluster is the same or one of them is "*", one covers another
                        String firstVal = first.getContent().get(attribute).asText();
                        String secondVal = second.getContent().get(attribute).asText();
                        //System.out.println("firstVal:" + firstVal);
                        //System.out.println("SecondVal:" + secondVal);
                        if (firstVal.equals(secondVal)) {
                            continue;
                        } else if (firstVal.equals("*") || secondVal.equals("*")) {
                            continue;
                        } else {
                            hasDomination = false;
                            break;
                        }
                    }
                    if (hasDomination == true || MaxDistance.getDistance(first, second) < distance) {
                        //build edge
                        //System.out.println("Domination between:" + first.getContent().toString() +", " + second.getContent().toString());
                        first.addCluster(second);
                        assert(first.getClusterList().contains(second));
                        second.addCluster(first);
                        assert(second.getClusterList().contains(first));
                    }
                }
            }
            //BFS for find the answers
            List<List<ClusterNode>> answerList = new ArrayList<List<ClusterNode>>();

            for (int i = 1; i <= k; i++) {
                findLargestScore(clusterCollection, answerList, new ArrayList<ClusterNode>(), coveredTuples,
                         i, 0);
            }

            //Test answerList
            int tmp = 0;
            for (List<ClusterNode> list: answerList) {
                System.out.print("Candidate Answer" + tmp +": " );
                tmp++;
                for (ClusterNode cluster: list) {
                    System.out.print(cluster.getContent() + ": ");
                    for (JsonNode t: cluster.getTupleList()) {
                        System.out.print(t.toString() + " ");
                    }
                    System.out.println("");
                }
            }
            double maxScore = Double.MIN_VALUE;
            List<ClusterNode> answer = null;
            for (List<ClusterNode> list: answerList) {
                double currentScore = 0;
                for (ClusterNode cluster: list) {
                    currentScore += cluster.computeScore();
                }
                if (currentScore > maxScore || (currentScore == maxScore && (answer == null || answer.size() < list.size()))) {
                    maxScore = currentScore;
                    answer = list;
                }
            }

            //Construct answer Json
            ArrayNode finalResult = mapper.createArrayNode();
            if (answer != null) {
                for (int i = 0; i < answer.size(); i++) {
                    ObjectNode current = mapper.createObjectNode();
                    current.set("diversity_" + i, answer.get(i).getContent());
                    ArrayNode covers = mapper.createArrayNode();
                    for (JsonNode tuple : answer.get(i).getTupleList()) {
                        covers.add(tuple);
                    }
                    current.set("coverage_" + i, covers);
                    finalResult.add(current);
                }
            }

            //output result
            String resultStr = mapper.writeValueAsString(finalResult);
            System.out.println(resultStr);
            result = mapper.readTree(resultStr);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * ConstructClusterList based on attributes' List and their values
     * @param clusterCollection
     * @param clusterContent
     * @param attributeList
     * @param attributeMap
     * @param index
     */
    private static void constructClusterList(List<ClusterNode> clusterCollection, ObjectNode clusterContent, List<String> attributeList, Map<String, Set<String>> attributeMap, int index) {
        if (clusterContent.size() == attributeList.size()) {
            //do something
            ObjectNode currentContent = clusterContent.deepCopy();
            clusterCollection.add(new ClusterNode(currentContent));
            return;
        }
        for (int i = index; i < attributeList.size(); i++) {
            String attribute = attributeList.get(i);
            for (String value: attributeMap.get(attribute)) {
                clusterContent.put(attribute, value);
                constructClusterList(clusterCollection, clusterContent, attributeList, attributeMap, i + 1);
                clusterContent.remove(attribute);
            }
        }
    }

    /**
     * Find the set of clusters that has the largest socre and the size is no more than k
     * @param clusterCollection
     * @param answerList
     * @param current
     * @param coveredTuples
     * @param size
     * @param index
     */
    private static void findLargestScore(List<ClusterNode> clusterCollection,
                                         List<List<ClusterNode>> answerList,
                                         List<ClusterNode> current,
                                         Set<JsonNode> coveredTuples,
                                         int size,
                                         int index) {
        if (current.size() == size) {
            //check coverage
            Set<JsonNode> coverList = new HashSet<JsonNode>();
            for (ClusterNode cluster: current) {
                coverList.addAll(cluster.getTupleList());
            }
            for (JsonNode t: coveredTuples) {
                if (!coverList.contains(t)) {
                    //System.out.println("tuple is not be covered:" + t.toString());
                    return;
                }
            }
            //System.out.println("Find an answer!");
            answerList.add(new ArrayList<ClusterNode>(current));
            return;
        }
        for (int i = index; i < clusterCollection.size(); i++) {
            ClusterNode currentCluster = clusterCollection.get(i);
            //System.out.println("CurrentCluster: " + currentCluster.getContent().toString());
            //check whether currentCluster can be add to current list: without edges in the current list
            boolean hasEdge = false;
            for (ClusterNode cluster: current) {
                //cluster.printDomination();
                if (cluster.getClusterList().contains(currentCluster)) {
                    hasEdge = true;
                    break;
                }
            }
            if (hasEdge == false) {
                current.add(currentCluster);
                //System.out.println("A new cluster add to list: " + currentCluster.getContent().toString());
                //update covered tuples list
                findLargestScore(clusterCollection, answerList, current, coveredTuples, size, i + 1);
                current.remove(current.size() - 1);
            }
        }
    }
}
