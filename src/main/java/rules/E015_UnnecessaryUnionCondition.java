// ERROR 20
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SetOperationList;

import java.util.*;
import java.util.stream.Collectors;

public class E015_UnnecessaryUnionCondition extends QueryVisitor {

    private final HashMap<Integer, List<String>> selectClauses = new HashMap<>();
    private final HashMap<Integer, List<String>> fromClauses = new HashMap<>();
    private final HashMap<Integer, List<String>> whereClauses = new HashMap<>();
    private final ArrayList<String> unnecessaryUnions = new ArrayList<>();
    private int index = 0;

    public E015_UnnecessaryUnionCondition() {
        outputMessage = "unnecessary union condition";
    }

    private void detectUnnecessaryUnionCondition() {
        if (selectClauses.size() != fromClauses.size() || selectClauses.size() != whereClauses.size())
            return;
        for (int i = 0; i < index - 1; i++) {
            List<String> select = selectClauses.get(i);
            List<String> from = fromClauses.get(i);
            List<String> where = whereClauses.get(i);
            for (int j = i + 1; j < index; j++) {
                List<String> cmpSelect = selectClauses.get(j);
                List<String> cmpFrom = fromClauses.get(j);
                List<String> cmpWhere = whereClauses.get(j);
                if (select.equals(cmpSelect) && from.equals(cmpFrom) && !where.equals(cmpWhere))
                    // unnecessaryUnions.add(String.format("replace with %s OR %s", String.join(" ", where), String.join(" ", cmpWhere)));
                    unnecessaryUnions.add("replace UNION/UNION ALL with WHERE clause connected by OR");
            }
        }
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(unnecessaryUnions);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        index = 0;
        selectClauses.clear();
        fromClauses.clear();
        whereClauses.clear();
        unnecessaryUnions.clear();
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getSelectItems() != null)
            selectClauses.put(index, plainSelect.getSelectItems().stream().map(Object::toString).collect(Collectors.toList()));

        ArrayList<String> fromClause = new ArrayList<>();
        if (plainSelect.getFromItem() != null)
            fromClause.add(plainSelect.getFromItem().toString());
        if (plainSelect.getJoins() != null)
            fromClause.addAll(plainSelect.getJoins().stream().map(Object::toString).collect(Collectors.toList()));
        fromClauses.put(index, fromClause);

        if (plainSelect.getWhere() != null)
            whereClauses.put(index, Collections.singletonList(plainSelect.getWhere().toString()));

        index++;
    }

    @Override
    public void visit(SetOperationList setOperationList) {
        super.visit(setOperationList);
        detectUnnecessaryUnionCondition();
    }
}
