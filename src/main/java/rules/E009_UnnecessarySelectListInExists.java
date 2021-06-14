// ERROR 12
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SubSelect;
import utils.equations.SubSelectValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class E009_UnnecessarySelectListInExists extends QueryVisitor {

    private int subSelectIndex = 0;
    private boolean parsingSubSelect = false;
    private final HashMap<Integer, SubSelectValues> subSelects = new HashMap<>();
    private final HashSet<String> unnecessarySelects = new HashSet<>();

    public E009_UnnecessarySelectListInExists() {
        outputMessage = "unnecessary select list in exists subquery";
    }

    private void detectUnnecessarySelectListInExists() {
        SubSelectValues subSelectValues = subSelects.get(subSelectIndex - 1);
        if (subSelectValues.getColumns().size() != 0) {
            if (subSelectValues.getColumns().size() == 1 && !subSelectValues.isAllTableColumns() && !subSelectValues.isSelectOne())
                return;
            if (subSelectValues.isSelectOne())
                subSelectValues.addColumn("1");
            if (subSelectValues.isAllTableColumns())
                subSelectValues.addColumn("*");
            unnecessarySelects.add(String.join(", ", subSelectValues.getColumns()));

        }
        else if (subSelectValues.isAllTableColumns() && subSelectValues.isSelectOne())
            unnecessarySelects.add("*, 1");
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(unnecessarySelects);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        subSelects.clear();
        unnecessarySelects.clear();
        subSelectIndex = 0;
    }

    @Override
    public void visit(AllColumns allColumns) {
        if (parsingSelect && parsingSubSelect)
            subSelects.get(subSelectIndex - 1).setAllTableColumns(true);
    }

    @Override
    public void visit(Column column) {
        if (parsingSelect && parsingSubSelect)
            subSelects.get(subSelectIndex - 1).addColumn(column.getColumnName());
    }

    @Override
    public void visit(LongValue longValue) {
        if (parsingSelect && parsingSubSelect && longValue.getStringValue().equals("1"))
            subSelects.get(subSelectIndex - 1).setSelectOne(true);
    }

    @Override
    public void visit(SubSelect subSelect) {
        parsingSubSelect = true;
        subSelects.put(subSelectIndex++, new SubSelectValues());
        super.visit(subSelect);
        if (parsingExists)
            detectUnnecessarySelectListInExists();
        subSelects.remove(subSelectIndex - 1);
        subSelectIndex -= 1;
        parsingSubSelect = false;
    }
}
