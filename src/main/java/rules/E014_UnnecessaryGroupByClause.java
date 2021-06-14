// ERROR 19
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import utils.equations.SubSelectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class E014_UnnecessaryGroupByClause extends QueryVisitor {

    private int subSelectIndex = 0;
    private final ArrayList<String> unnecessaryGroupBy = new ArrayList<>();
    private final ArrayList<String> aggregationFunctions = new ArrayList<>(Arrays.asList("avg", "checksum_agg", "count", "count_big", "max", "min", "stdev", "stdevp", "sum", "var", "varp"));
    private final HashMap<Integer, SubSelectAttributes> subSelects = new HashMap<>();

    public E014_UnnecessaryGroupByClause() {
        outputMessage = "unnecessary group by clause";
    }

    private void detectUnnecessaryGroupByClause() {
        if (subSelectIndex == 0)
            return;
        if (subSelects.get(subSelectIndex - 1).isUsingAggregationFunctions())
            return;
        String msg = "";
        ArrayList<String> selectAttributes = subSelects.get(subSelectIndex - 1).getSelectAttributes();
        ArrayList<String> groupByAttributes = subSelects.get(subSelectIndex - 1).getGroupByAttributes();
        if (selectAttributes.equals(groupByAttributes) && selectAttributes.size() > 0) {
            msg = String.format("select %s ... group by %s", String.join(", ", selectAttributes), String.join(", ", groupByAttributes));
            unnecessaryGroupBy.add(msg);
        }
    }

    private void detectParsingAggregationFunction(Function function) {
        if (subSelects.get(subSelectIndex - 1).isUsingAggregationFunctions())
            return;
        String expression = function.toString().toLowerCase();
        String func = expression.split("\\(")[0];
        if (aggregationFunctions.contains(func))
            subSelects.get(subSelectIndex - 1).setUsingAggregationFunctions(true);
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(unnecessaryGroupBy);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        subSelectIndex = 0;
        subSelects.clear();
        unnecessaryGroupBy.clear();
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (!parsingSubSelect)
            subSelects.put(subSelectIndex++, new SubSelectAttributes());
        super.visit(plainSelect);
        detectUnnecessaryGroupByClause();
        subSelects.remove(subSelectIndex - 1);
        subSelectIndex -= 1;
        if (subSelectIndex == 0 && subSelects.isEmpty())
            subSelects.put(subSelectIndex++, new SubSelectAttributes());
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelects.put(subSelectIndex++, new SubSelectAttributes());
        super.visit(subSelect);
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);
        if (selectExpressionItem.getAlias() != null && !(selectExpressionItem.getExpression() instanceof Column) && parsingSelect)
            subSelects.get(subSelectIndex - 1).addSelectAttribute(selectExpressionItem.getAlias().getName());
        if (selectExpressionItem.getAlias() != null && !(selectExpressionItem.getExpression() instanceof Column) && parsingGroupBy)
            subSelects.get(subSelectIndex - 1).addGroupByAttribute(selectExpressionItem.getAlias().getName());
    }

    @Override
    public void visit(Column column) {
        if (parsingSelect && !parsingFunction)
            subSelects.get(subSelectIndex - 1).addSelectAttribute(column.getColumnName());
        if (parsingGroupBy && !parsingFunction)
            subSelects.get(subSelectIndex - 1).addGroupByAttribute(column.getColumnName());
    }

    @Override
    public void visit(Function function) {
        if (parsingSelect || parsingGroupBy || parsingHaving)
            detectParsingAggregationFunction(function);
        if (parsingSelect)
            subSelects.get(subSelectIndex - 1).addSelectAttribute(function.toString());
        if (parsingGroupBy)
            subSelects.get(subSelectIndex - 1).addGroupByAttribute(function.toString());
        super.visit(function);
    }
}
