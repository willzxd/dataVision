package algorithm;

import java.util.*;

/**
 * Created by will on 3/17/16.
 */
public class KMedoids {
    private int k;
    private int iterationNum;
    private List<DataPoint> dataSet = new ArrayList<DataPoint>();
    private AbstractDistance distanceFuction;

    public KMedoids(int k, int num, List<DataPoint> dataSet) {
        this.k = k;
        this.iterationNum = num;
        this.dataSet = dataSet;
        check();
    }

    private void check() {
        if (k == 0)
            throw new IllegalArgumentException("k must be the number > 0");

        if (dataSet == null)
            throw new IllegalArgumentException("program can't get real data");
    }

    /**
     * Randomly select K center points
     * @return
     */
    public Set<DataPoint> chooseCenter() {
        Set<DataPoint> center = new HashSet<DataPoint>();
        Random ran = new Random();
        int roll = 0;
        while (center.size() < k) {
            roll = ran.nextInt(dataSet.size());
            center.add(dataSet.get(roll));
        }
        return center;
    }
}
