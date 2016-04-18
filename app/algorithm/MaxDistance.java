package algorithm;

import java.util.Iterator;

/**
 * Created by will on 4/8/16.
 */
public class MaxDistance {
    /**
     * Compute the max distance of two clusters based on the distance of possible tuples covered by them
     * @param a
     * @param b
     * @return
     */
    public static int getDistance(ClusterNode a, ClusterNode b) {
        //Assume they have same size of attribute
        if (a == null || b == null) {
            System.out.println("Compute Distance: One Cluster is Null!");
            return Integer.MAX_VALUE;
        }
        int distance = 0;
        Iterator<String> fieldsName = a.getContent().fieldNames();
        while (fieldsName.hasNext()) {
            String attribute = fieldsName.next();
            if (attribute.equals("val")) {
                continue;
            }
            //if attribute value of two cluster are not equal or one of them is "*", distance++
            if (!a.getContent().get(attribute).asText().equals(b.getContent().get(attribute).asText())) {
                distance++;
            } else if (a.getContent().get(attribute).asText().equals("*") ||
                    b.getContent().get(attribute).asText().equals("*")) {
                distance++;
            }
        }
        //System.out.println("The distance between " + a.getContent().toString() + " and " + b.getContent().toString() + ": " + distance);
        return distance;
    }
}
