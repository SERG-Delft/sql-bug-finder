// ERROR 1
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.*;

public class E001_InconsistentTupleVariable extends QueryVisitor {

    private final HashMap<String, HashSet<String>> identicalTuples = new HashMap<>();
    private final HashSet<String> inconsistentTupleVariables = new HashSet<>();

    public E001_InconsistentTupleVariable() {
        outputMessage = "inconsistent tuple variables";
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>();
        for (String result : inconsistentTupleVariables) {
            if (result.contains("null")) {
                if (tableNames.size() == 0)
                    results.add(result.replace("null.", ""));
            }
            else
                results.add(result);
        }
        inconsistentTupleVariables.clear();
        return results;
    }

    @Override
    public void cleanup() {}

    private String getKey(String split) {
        try {
            split = split.replace("(", "");
            split = split.replace(")", "");
            String expression = split.split("=")[0].trim();
            if (!expression.contains("."))
                return "null." + expression;
            String[] tmp = expression.split("\\.")[0].trim().split(" ");
            String table = tmp[tmp.length - 1];
            String column = expression.split("\\.")[1].trim();
            if (tableAliases.containsKey(table))
                return tableAliases.get(table) + "." + column;
            if (tableNames.contains(table))
                return table + "." + column;
            return "null." + column;
        } catch (Exception e) {
            return "";
        }
    }

    private String getValue(String split) {
        split = split.replace("(", "");
        split = split.replace(")", "");
        if (split.contains("'") || split.contains("\"")) {
            String[] tmp;
            if (split.contains("'"))
                tmp = split.split("'");
            else tmp = split.split("\"");
            if (tmp.length <= 1)
                return "";
            else return tmp[1].trim();
        }
        return getKey(split.split("=")[1].trim());
    }

    private boolean hasIdenticalTuple(String key) {
        if (identicalTuples.size() == 1) {
            Map.Entry<String,HashSet<String>> entry = identicalTuples.entrySet().iterator().next();
            if (entry.getValue().size() == 2)
                return false;
        }
        String table = key.split("\\.")[0].trim();
        for (Map.Entry<String, HashSet<String>> entry : identicalTuples.entrySet()) {
            String keyTable = entry.getKey().split("\\.")[0].trim();
            if (keyTable.equals(table) && entry.getValue().contains(entry.getKey()))
                return true;
        }
        return false;
    }

    private boolean detectCheckSplits(ArrayList<String> splits) {
        for (String split : splits)
            if (!split.contains(" = "))
                return false;
        return true;
    }

    @Override
    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        String expression = binaryExpression.toString();
        if (expression.contains("EXISTS")) {
            binaryExpression.getRightExpression().accept(this);
            return;
        }
        if (expression.contains(" AND ") && !expression.contains(" OR ")) {
            ArrayList<String> splits = new ArrayList<>(Arrays.asList(expression.split("AND")));
            if (detectCheckSplits(splits)) {
                for (String split : splits) {
                    if (split.contains(" = ")) {
                        String key = getKey(split);
                        String value = getValue(split);
                        if (identicalTuples.containsKey(key) && !key.equals(""))
                            identicalTuples.get(key).add(value);
                        else
                            identicalTuples.put(key, new HashSet<>(Collections.singleton(value)));
                    }
                }
                for (Map.Entry<String, HashSet<String>> entry : identicalTuples.entrySet())
                    if (entry.getValue().size() > 1 && hasIdenticalTuple(entry.getKey()))
                        inconsistentTupleVariables.add(entry.getKey());
            }
            identicalTuples.clear();
        }
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (!(plainSelect.toString().contains("AND") || plainSelect.toString().contains("and")))
            return;
        super.visit(plainSelect);
    }
}
