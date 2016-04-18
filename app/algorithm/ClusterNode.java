package algorithm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * This Class is the node in V_t that covers some tuples based on rules
 * e.g. (*, *, c1, d1) will cover (a1, b1, c1, d1)
 * Created by will on 4/6/16.
 */
public class ClusterNode {
    /**
     * The presentation of the content
     */
    private JsonNode content;
    /**
     * List of covered tuples
     */
    private Set<TupleNode> tupleList;

    /**
     * List of clusterNodes that have edges
     */
    private List<ClusterNode> clusterList;
    /**
     * The cluster has dominated edge
     */
    private List<ClusterNode> dominateList;
    /**
     * The cluster has distance edge
     */
    private List<ClusterNode> distanceList;

    private Map<String, Set<String>> attributeMap;

    private double value;
    /**
     * Constructor
     */
    public ClusterNode() {
        this.content = null;
        tupleList = new HashSet<>();
        clusterList = new ArrayList<ClusterNode>();
    }

    public ClusterNode(JsonNode content) {
        this.content = content;
        tupleList = new HashSet<>();
        clusterList = new ArrayList<ClusterNode>();
        this.dominateList = new ArrayList<>();
        this.distanceList = new ArrayList<>();
        this.attributeMap = new HashMap<String, Set<String>>();
    }

    /**
     * Add covered tuple
     * @param tuple
     */
    public void addTuples(TupleNode tuple) {
        if(!tupleList.contains(tuple)) {
            tupleList.add(tuple);
            Iterator<String> it = tuple.getContent().fieldNames();
            while (it.hasNext()) {
                String attribute = it.next();
                if (!attributeMap.containsKey(attribute)) {
                    attributeMap.put(attribute, new HashSet<String>());
                }
                attributeMap.get(attribute).add(tuple.getContent().get(attribute).asText());
            }
        }
    }

    /**
     * Add dominated cluster
     * @param cluster
     */
    public void addCluster(ClusterNode cluster) {
        if (!clusterList.contains(cluster)) {
            clusterList.add(cluster);
        }
    }

    public double computeScore() {
        double res = 0;
        for (TupleNode t: tupleList) {
            res += t.getContent().get("val").asDouble();
        }
        ((ObjectNode)content).remove("val");
        res = tupleList.size() == 0? 0 : res / tupleList.size();
        ((ObjectNode)content).put("val", res);
        this.value = res;
        return res;
    }

    public void printDomination() {
        System.out.print("The cluster " + this.content.toString() + "dominates or dominated by: ");
        for (ClusterNode cluster: this.clusterList) {
            System.out.print(cluster.getContent().toString() + " ");
        }
        System.out.println("");
    }

    public void updateAttributeMap() {

    }

    public boolean equals(ClusterNode o) {
        if (o == null) {
            return false;
        }
        if (this.content.equals(o.getContent())) {
            return true;
        } else {
            return false;
        }

    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }

    public Set<TupleNode> getTupleList() {
        return tupleList;
    }

    public void setTupleList(Set<TupleNode> tupleList) {
        this.tupleList = tupleList;
    }

    public List<ClusterNode> getClusterList() {
        return clusterList;
    }

    public void setClusterList(List<ClusterNode> clusterList) {
        this.clusterList = clusterList;
    }

    public Map<String, Set<String>> getAttributeMap() {
        return this.attributeMap;
    }

    public double getValue() {
        return value;
    }

    public List<ClusterNode> getDominateList() {
        return dominateList;
    }

    public List<ClusterNode> getDistanceList() {
        return distanceList;
    }
}
