package algorithm;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 4/12/16.
 */
public class TupleNode {
    private JsonNode content;
    private List<ClusterNode> ClusterList;

    public TupleNode(JsonNode content) {
        this.content = content;
        this.ClusterList = new ArrayList<>();
    }

    public boolean equals(TupleNode o) {
        if (o == null) {
            return false;
        }
        return this.content.equals(o.getContent());
    }
    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }

    public List<ClusterNode> getClusterList() {
        return ClusterList;
    }

    public void setClusterList(List<ClusterNode> clusterList) {
        ClusterList = clusterList;
    }
}
