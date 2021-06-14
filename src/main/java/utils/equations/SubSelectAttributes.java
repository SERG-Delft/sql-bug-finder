package utils.equations;

import java.util.ArrayList;

public class SubSelectAttributes {

    private boolean usingAggregationFunctions = false;
    private final ArrayList<String> selectAttributes = new ArrayList<>();
    private final ArrayList<String> groupByAttributes = new ArrayList<>();

    public boolean isUsingAggregationFunctions() {
        return usingAggregationFunctions;
    }

    public ArrayList<String> getSelectAttributes() {
        return selectAttributes;
    }

    public ArrayList<String> getGroupByAttributes() {
        return groupByAttributes;
    }

    public void setUsingAggregationFunctions(boolean usingAggregationFunctions) {
        this.usingAggregationFunctions = usingAggregationFunctions;
    }

    public void addSelectAttribute(String selectAttribute) {
        this.selectAttributes.add(selectAttribute);
    }

    public void addGroupByAttribute(String groupByAttribute) {
        this.groupByAttributes.add(groupByAttribute);
    }
}
