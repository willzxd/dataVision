package algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for KMedoids algorithm
 * Created by will on 3/17/16.
 *
 */
public class Cluster {
    private int id;
    private DataPoint center;
    private List<DataPoint> members;

    public Cluster(int id, DataPoint center) {
        this.id = id;
        this.center = center;
        this.members = new ArrayList<DataPoint>();
    }

    public Cluster(int id, DataPoint center, List<DataPoint> members) {
        this.id = id;
        this.center = center;
        this.members = members;
    }

    public void addPoint(DataPoint newPoint) {
        if (!members.contains(newPoint))
            members.add(newPoint);
        else {
            throw new IllegalStateException("试图处理同一个样本数据!");
        }
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "id =" + id +
                ", center = " + center.toString() +
                ", sizeOfMembers=" + members.size() +
                "}";
    }

    public List<DataPoint> getMembers() {
        return members;
    }

    public void setMembers(List<DataPoint> members) {
        this.members = members;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DataPoint getCenter() {
        return center;
    }

    public void setCenter(DataPoint center) {
        this.center = center;
    }
}
