package algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 3/17/16.
 */
public class DataPoint {
    private ArrayList<Double> valueList;
    private int id;
    private boolean isSample;

    public DataPoint(int id, ArrayList<Double> valueList) {
        this.id = id;
        this.valueList = valueList;
        isSample = true;
    }
    public DataPoint(int id, ArrayList<Double> valueList, boolean isSample) {
        this.id = id;
        this.valueList = valueList;
        this.isSample = isSample;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("{id: " + id + " value: ");
        for (Double value: valueList) {
            s.append(value + ", ");
        }
        s.append("}");
        return s.toString();
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean isSample() {
        return isSample;
    }

    public void setSample(boolean sample) {
        isSample = sample;
    }

    public ArrayList<Double> getValueList() {
        return valueList;
    }

    public void setValueList(ArrayList<Double> valueList) {
        this.valueList = valueList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
