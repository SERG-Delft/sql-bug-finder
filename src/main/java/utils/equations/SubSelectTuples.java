package utils.equations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class SubSelectTuples {

    private HashSet<String> tuples = new HashSet<>(Collections.singleton("null"));
    private ArrayList<String> tables = new ArrayList<>();
    private String subQuery = "";

    public void addTuple(String tuple) {
        tuples.add(tuple);
    }

    public void addTable(String table) {
        tables.add(table);
    }

    public HashSet<String> getTuples() {
        return tuples;
    }

    public ArrayList<String> getTables() { return tables; }

    public void setSubQuery(String subQuery) {
        this.subQuery = subQuery;
    }

    public String getSubQuery() {
        return subQuery;
    }
}
