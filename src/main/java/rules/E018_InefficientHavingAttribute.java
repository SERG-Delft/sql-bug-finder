// ERROR 22
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import utils.equations.ColumnValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class E018_InefficientHavingAttribute extends QueryVisitor {

    private boolean hasGroupByAggregationFunction = false;
    private boolean hasHavingAggregationFunction = false;
    private final ArrayList<ColumnValue> groupByAttributes = new ArrayList<>();
    private final ArrayList<ColumnValue> havingAttributes = new ArrayList<>();
    private final HashSet<String> inefficientHavingAttributes = new HashSet<>();
    private final ArrayList<String> selectAliases = new ArrayList<>();
    private final ArrayList<String> aggregationFunctions = new ArrayList<>(Arrays.asList("avg", "checksum_agg", "count", "count_big", "max", "min", "stdev", "stdevp", "sum", "var", "varp"));

    public E018_InefficientHavingAttribute() {
        outputMessage = "inefficient having";
    }

    private void detectInefficientHavingAttributes() {
        if (hasGroupByAggregationFunction || hasHavingAggregationFunction)
            return;
        if (!groupByAttributes.isEmpty() && !havingAttributes.isEmpty())
            inefficientHavingAttributes.addAll(havingAttributes.stream().map(ColumnValue::toString).collect(Collectors.toList()));
    }

    private boolean detectParsingAggregationFunction(Function function) {
        String expression = function.toString().toLowerCase();
        String func = expression.split("\\(")[0];
        return aggregationFunctions.contains(func);
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(inefficientHavingAttributes);
        inefficientHavingAttributes.clear();
        selectAliases.clear();
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        hasGroupByAggregationFunction = false;
        hasHavingAggregationFunction = false;
        groupByAttributes.clear();
        havingAttributes.clear();
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);
        if (selectExpressionItem.getAlias() != null && !(selectExpressionItem.getExpression() instanceof Column))
            selectAliases.add(selectExpressionItem.getAlias().getName());
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        super.visit(plainSelect);
        detectInefficientHavingAttributes();
        cleanup();
    }

    @Override
    public void visit(Function function) {
        if (parsingGroupBy && !hasGroupByAggregationFunction)
            hasGroupByAggregationFunction = detectParsingAggregationFunction(function);
        if (parsingHaving && !hasHavingAggregationFunction)
            hasHavingAggregationFunction = detectParsingAggregationFunction(function);
        super.visit(function);
    }

    @Override
    public void visit(Column column) {
        if (parsingGroupBy && !selectAliases.contains(column.getColumnName()))
            groupByAttributes.add(new ColumnValue(getTableNameFromColumn(column), column.getColumnName(), getTableAliasFromColumn(column)));
        if (parsingHaving && !selectAliases.contains(column.getColumnName()))
            havingAttributes.add(new ColumnValue(getTableNameFromColumn(column), column.getColumnName(), getTableAliasFromColumn(column)));
    }
}
