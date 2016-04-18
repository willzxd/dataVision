package algorithm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.api.data.ObjectMapping;
import play.api.libs.ws.ssl.SystemConfiguration;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Algorithms for find top-k clusters
 * Created by will on 4/6/16.
 */
public class FindAnswer {

    ObjectMapper mapper;
    //list of all tuples
    //List<JsonNode> tupleCollection;
    //list of all cluster
    List<ClusterNode> clusterCollection;
    //list of all attributes
    List<String> attributeList;
    //map of attributes and their value list
    Map<String, Set<String>> attributeMap;
    //set of covered tuples
    Set<TupleNode> coveredTuples;
    Comparator<ClusterNode> comparator;
    List<ClusterNode> answer;

    public FindAnswer() {
        this.mapper = new ObjectMapper();
        //this.tupleCollection = new ArrayList<JsonNode>();
        this.clusterCollection = new ArrayList<ClusterNode>();
        this.attributeList = new ArrayList<String>();
        this.attributeMap = new HashMap<String, Set<String>>();
        this.coveredTuples = new HashSet<>();
        this.answer = null;
        this.comparator = new Comparator<ClusterNode>() {
            @Override
            public int compare(ClusterNode o1, ClusterNode o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null || o1.getContent().get("val").asDouble() - o2.getContent().get("val").asDouble() < 0) {
                    return 1;
                } else if (o2 == null || o1.getContent().get("val").asDouble() - o2.getContent().get("val").asDouble() > 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
    }

    /**
     * Init fields based on k, coverage, distance and candidate tuples
     * TODO: If the jsonStr does not change, we just need to partially update the fields.
     * @param jsonStr
     * @param k
     * @param coverage
     * @param distance
     */
    private void init(String jsonStr, int k, int coverage, int distance) {
        if (jsonStr == null || jsonStr.length() == 0) {
            Logger.info("No input tuples!");
            return;
        }
        Logger.info(new Date() + " Begin initialization." );
        long startT = System.currentTimeMillis();
        //root of all nodes from result
        JsonNode root = null;
        //message for log
        StringBuilder message = new StringBuilder();

        try {
            //construct attributeList
            root = mapper.readTree(jsonStr);
            JsonNode example = root.size() == 0 ? null : root.get(0);
            if (example != null) {
                Iterator<String> field = example.fieldNames();
                while (field.hasNext()) {
                    String attributeName = field.next();
                    if (!attributeName.equals("val")) {
                        attributeList.add(attributeName);
                        attributeMap.put(attributeName, new HashSet<String>());
                        //attributeMap.get(attributeName).add("*");
                    }
                }
            }
            if (attributeList.size() < distance) {
                Logger.info("Decreasing distance D will get better result!");
                distance = attributeList.size();
            }

            //Test attributeList
//            message.append("Attribute List: ");
//            for (String attribute: attributeList) {
//               message.append("[" + attribute + "] ");
//            }
//            Logger.info(message.toString());
//            message.setLength(0);

            //Construct coverage set
            Logger.info(new Date() + " Construct the set of covered tuples.");
            //System.out.println("The tuples should be covered: ");
            for (int i = 0; (i < coverage || i < k) && i < root.size(); i++) {
                //System.out.println(tupleCollection.get(i).toString());
                TupleNode tuple = new TupleNode(root.get(i));
                coveredTuples.add(tuple);
                for (String attribute : attributeList) {
                    attributeMap.get(attribute).add(tuple.getContent().get(attribute).asText());
                }
            }

            for (Set<String> values: attributeMap.values()) {
                if (values.size() > 1) {
                    values.add("*");
                }
            }
            //Test list of tuples and attributeMap
//            System.out.println("Tuple List: ");
//            for (TupleNode t: coveredTuples) {
//                System.out.println(t.getContent().toString());
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

            //Construct clusterCollection
            Logger.info("Start to construct clusterCollection...");
            ObjectNode clusterContent = mapper.createObjectNode();
            constructClusterList(clusterCollection, clusterContent, attributeList, attributeMap, 0);

            Logger.info("Start to build edges between clusters and tuples...");
            //build edges between clusters and tuples
            for (TupleNode t : coveredTuples) {
                for (ClusterNode cluster : clusterCollection) {
                    JsonNode currentClusterContent = cluster.getContent();
                    boolean hasEdge = true;
                    for (String attribute : attributeList) {
                        if (currentClusterContent.get(attribute).asText().equals("*")) {
                            continue;
                        } else if (!t.getContent().get(attribute).asText().equals(currentClusterContent.get(attribute).asText())) {
                            hasEdge = false;
                            break;
                        }
                    }
                    if (hasEdge == true) {
                        //System.out.println("Edge between:" + cluster.getContent().toString() +", " + t.toString());
                        cluster.addTuples(t);
                        t.getClusterList().add(cluster);
                        assert (cluster.getTupleList().contains(t));
                    }
                }
            }
            //delete trivial clusters
            Iterator<ClusterNode> iterator = clusterCollection.iterator();
            while (iterator.hasNext()) {
                ClusterNode cluster = iterator.next();
                if (cluster.getTupleList().size() == 0) {
                    iterator.remove();
                    continue;
                }
                //check if some attribute of content is "*" but all the tuples has the same value of this attribute
                for (String attribute: attributeList) {
                    //System.out.println(attribute + " " + cluster.getContent().toString());
                    if (cluster.getContent().get(attribute).asText().equals("*") &&
                            cluster.getAttributeMap().get(attribute).size() == 1) {
                        iterator.remove();
                        break;
                    }
                }
            }
            //Test clusterCollection
//            System.out.println("All clusters: ");
//            for (ClusterNode cluster: clusterCollection) {
//                System.out.print(cluster.getContent().toString() + ": ");
//                for (TupleNode tuple: cluster.getTupleList()) {
//                    System.out.print(tuple.getContent().toString() + " ");
//                }
//                System.out.println("");
//            }

            Logger.info("Start to build edges between clusters...");
            //build edges between clusters
            for (ClusterNode first : clusterCollection) {
                for (ClusterNode second : clusterCollection) {
                    if (first == second) {
                        continue;
                    }
                    boolean hasDomination = true;
                    for (String attribute : attributeList) {
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
                    if (hasDomination == true) {
                        //build edge
                        //System.out.println("Domination between:" + first.getContent().toString() +", " + second.getContent().toString());
                        first.addCluster(second);
                        assert (first.getClusterList().contains(second));
                        second.addCluster(first);
                        assert (second.getClusterList().contains(first));
                        first.getDominateList().add(second);
                        second.getDominateList().add(first);
                    } else if (MaxDistance.getDistance(first, second) < distance) {
                        first.addCluster(second);
                        second.addCluster(first);
                    }
                }
            }

            //buid score of cluster
            for (ClusterNode cluster: clusterCollection) {
                cluster.computeScore();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.info(new Date() + "Finish initialization.");
        long endT = System.currentTimeMillis();
        Logger.info("Initialization Time Cost: " + ((endT - startT) / 1000.0) + "s");
    }

    /**
     * Brutal Force Algorithm
     * @param jsonStr
     * @param k
     * @param coverage
     * @param distance
     * @return
     */
    public JsonNode findclustersBT(String jsonStr, int k, int coverage, int distance) {
        init(jsonStr, k, coverage, distance);
        long startT = System.currentTimeMillis();
        JsonNode result = null;
        //BFS for find the answers
        Set<Set<ClusterNode>> answerList = new HashSet<Set<ClusterNode>>();

        for (int i = 1; i <= k; i++) {
            findLargestScore(clusterCollection, answerList, new ArrayList<ClusterNode>(), coveredTuples,
                     i, 0);
        }

        //Test answerList
//        int tmp = 0;
//        for (Set<ClusterNode> list: answerList) {
//            System.out.print("Candidate Answer" + tmp +": " );
//            tmp++;
//            for (ClusterNode cluster: list) {
//                System.out.print(cluster.getContent() + ": ");
//                for (JsonNode t: cluster.getTupleList()) {
//                    System.out.print(t.toString() + " ");
//                }
//                System.out.println("");
//            }
//        }
        double maxScore = Double.MIN_VALUE;
        List<ClusterNode> answer = null;
        for (Set<ClusterNode> list: answerList) {
            double currentScore = 0;
            for (ClusterNode cluster: list) {
                currentScore += cluster.computeScore();
            }
            if (currentScore > maxScore || (currentScore == maxScore && (answer == null || answer.size() < list.size()))) {
                maxScore = currentScore;
                answer = new ArrayList<ClusterNode>(list);
            }
        }
        long endT = System.currentTimeMillis();
        Collections.sort(answer, comparator);
        result = ConstructAnswerJsonStr(answer);
        Logger.info("The overall score is: " + maxScore);
        Logger.info("The running Time is: " + ((endT - startT) / 1000.0));
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
    private void constructClusterList(List<ClusterNode> clusterCollection,
                                      ObjectNode clusterContent,
                                      List<String> attributeList,
                                      Map<String, Set<String>> attributeMap,
                                      int index) {
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
     * Find the set of clusters that has the largest socre and the size is no more than k.
     * Used in Brtual Force Algorithm
     * @param clusterCollection
     * @param answerList
     * @param current
     * @param coveredTuples
     * @param size
     * @param index
     */
    private void findLargestScore(List<ClusterNode> clusterCollection,
                                  Set<Set<ClusterNode>> answerList,
                                  List<ClusterNode> current,
                                  Set<TupleNode> coveredTuples,
                                  int size,
                                  int index) {
        if (current.size() == size) {
            //check coverage
            Set<TupleNode> coverList = new HashSet<>();
            for (ClusterNode cluster: current) {
                coverList.addAll(cluster.getTupleList());
            }
            for (TupleNode t: coveredTuples) {
                if (!coverList.contains(t)) {
                    //System.out.println("tuple is not be covered:" + t.toString());
                    return;
                }
            }
            //System.out.println("Find an answer!");
            answerList.add(new HashSet<ClusterNode>(current));
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

    /**
     * This method is only used for construct the final result that return to Browsers.
     * @param answer
     * @return
     */
    private JsonNode ConstructAnswerJsonStr(List<ClusterNode> answer) {
        //Construct answer Json
        JsonNode result = null;
        ArrayNode finalResult = mapper.createArrayNode();
        if (answer != null) {
            for (int i = 0; i < answer.size(); i++) {
                ObjectNode current = mapper.createObjectNode();
                current.set("diversity_" + i, answer.get(i).getContent());
                ArrayNode covers = mapper.createArrayNode();
                for (TupleNode tuple : answer.get(i).getTupleList()) {
                    covers.add(tuple.getContent());
                }
                current.set("coverage_" + i, covers);
                finalResult.add(current);
            }
        }
        //output result
        String resultStr = null;
        try {
            resultStr = mapper.writeValueAsString(finalResult);
            System.out.println(resultStr);
            result = mapper.readTree(resultStr);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Split Tree Greedy Algorithm
     * @param jsonStr
     * @param k
     * @param coverage
     * @param distance
     * @return
     */
    public JsonNode findclustersGreedy(String jsonStr, int k, int coverage, int distance) {
        init(jsonStr, k, coverage, distance);
        long startT = System.currentTimeMillis();
        JsonNode result = null;

        //find the cluster that cover all tuples, begin from this root
        Queue<ClusterNode> currentLayer = new LinkedList<ClusterNode>();
        for (ClusterNode cluster: clusterCollection) {
            //Logger.info("cluster is " + cluster.getContent().toString());
            if (cluster.getTupleList().size() == coveredTuples.size()) {
                currentLayer.add(cluster);
                break;
            }
        }
        Logger.info("The cluster we will start from is " + currentLayer.peek().getContent().toString());
        //start a new list presented next layer
        //every time we split a cluster as two new clusters
            //check whether the new clusters, dominated by others in the current layer
            //check whether the new clusters, conflict with distance by the next layer
            //store them in the next layer
            //if meet the k, stop,
            //  or finish split the previous layer, loop
        for (ClusterNode cluster: currentLayer) {
            Logger.info("cluster in currentLayer" + cluster.getContent().toString());
        }
        Set<ClusterNode> visited = new HashSet<ClusterNode>();
        while (!currentLayer.isEmpty()) {

            Logger.info("A new split stage begin");
//            for (ClusterNode cluster: currentLayer) {
//                Logger.info("The cluster in the stage" + cluster.getContent().toString());
//            }

            int size = currentLayer.size();
            int count = size;
            Queue<ClusterNode> nextLayer = new LinkedList<ClusterNode>();
            while (count > 0) {
                ClusterNode clusterNeedSplit = currentLayer.poll();
                Logger.info("The current cluster needs to split: " + clusterNeedSplit.getContent().toString());
                //if it is a single cluster, do nothing
                if (clusterNeedSplit.getTupleList().size() == 1) {
                    nextLayer.add(clusterNeedSplit);
                    count--;
                    continue;
                }
                //A List that store the splited children
                List<ClusterNode> children = new ArrayList<ClusterNode>();
                //The tuple set that we need to cover
                Set<TupleNode> tuples = new HashSet<>(clusterNeedSplit.getTupleList());
                //The cluster set of all candiates
                Set<ClusterNode> dominatedList = new HashSet<>(clusterNeedSplit.getDominateList());
                //if covered everything or do not have enough space to cover everything, stop
                while (tuples.size() != 0 && k > children.size() + nextLayer.size() + currentLayer.size()) {
                    Logger.info("The number of tuples need to be covered is " + tuples.size());
//                    for (TupleNode tuple: tuples) {
//                        Logger.info(tuple.getContent().toString());
//                    }

                    //A heap help find valid candidate which covers as many tuples as possible
                    PriorityQueue<ClusterNode> childrenHeap = new PriorityQueue<>(11, new Comparator<ClusterNode>() {
                        @Override
                        public int compare(ClusterNode o1, ClusterNode o2) {
                            if (o1 == null && o2 == null || (o1.getTupleList().size() == o2.getTupleList().size())) {
                                return 0;
                            } else if (o1 == null || o1.getTupleList().size() < o2.getTupleList().size()) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });

                    //Iterate every candidate cluster
                    for (ClusterNode child: dominatedList) {
                        //flag
                        boolean isValid = true;
                        //if it is same as the cluster needs to split or
                        //it is same as some cluster that already visited
                        //it is same as cluster that already in the next layer, just continue
                        if (child == clusterNeedSplit || nextLayer.contains(child) || visited.contains(child)) {
                            continue;
                        }
                        //check domination/distance between other clusters
                        for (ClusterNode cluster : currentLayer) {
                            if (child.getClusterList().contains(cluster)) {
                                isValid = false;
                                break;
                            }
                        }
                        for (ClusterNode cluster : children) {
                            if (child.getClusterList().contains(cluster)) {
                                isValid = false;
                                break;
                            }
                        }
                        for (ClusterNode cluster : nextLayer) {
                            if (child.getClusterList().contains(cluster)) {
                                isValid = false;
                                break;
                            }
                        }
                        if (isValid) {
                            childrenHeap.add(child);
                        }
                    }
                    if (childrenHeap.size() != 0 && k > children.size() + nextLayer.size() + currentLayer.size()) {
                        ClusterNode child = childrenHeap.poll();
                        //Logger.info("child is null? " + (child == null));
                        children.add(child);
                        Logger.info("A child add to children list: " + child.getContent().toString());
                        for (TupleNode tuple : child.getTupleList()) {
                            //Logger.info(tuple.getContent().toString());
                            tuples.remove(tuple);
                            //here we update the dominatedList to make sure there are no overlap between child
                            dominatedList.removeAll(tuple.getClusterList());
                        }
                        Logger.info("The rest number of tuples need to be covered is " + tuples.size());
                        Logger.info("The children size is " + children.size() + "\nThe nextLayer size is "
                                + nextLayer.size());
                    } else {
                        break;
                    }
                }
                //If tuples is empty, means the children can be added to the next step
                if (tuples.size() == 0) {
                    //add children to next layer
                    nextLayer.addAll(children);
                    visited.add(clusterNeedSplit);
                    Logger.info("Cluster \n" + clusterNeedSplit.getContent().toString() + "\nhas been added to visited list.");
                } else {
                    nextLayer.add(clusterNeedSplit);
                    Logger.info("Do not have enough space to split " + clusterNeedSplit.getContent().toString());
                }
                count--;
            }
            if (size == nextLayer.size()) {
                answer = new ArrayList<>(nextLayer);
                break;
            } else {
                currentLayer = nextLayer;
                Logger.info("The nextLayer is ready!");
//                for (ClusterNode cluster: nextLayer) {
//                    System.out.println(cluster.getContent().toString());
//                }
            }
        }
//        for (ClusterNode cluster: answer) {
//            System.out.println("answer cluster:" + cluster.getContent().toString());
//        }
//        Collections.sort(answer, comparator);
//        for (ClusterNode cluster: answer) {
//            System.out.println("After sorted answer cluster:" + cluster.getContent().toString());
//        }
        long endT = System.currentTimeMillis();
        if (answer.size() < (k * 0.8)) {
            Logger.info("Increasing k may get better result");
        }
        if (distance < 2) {
            Logger.info("Increasing D may get better result.");
        }
        result = ConstructAnswerJsonStr(answer);
        double score = 0;
        for (ClusterNode cluster: answer) {
            score+= cluster.getValue();
        }
        Logger.info("The overall score is:" + score);
        Logger.info("The Running Time is:" + ((endT - startT) / 1000.0) + "s");
        return result;
    }

    private void splitHelper(List<ClusterNode> children, Set<TupleNode> tuples, ClusterNode splitCluster, int restSpace) {


    }
}
