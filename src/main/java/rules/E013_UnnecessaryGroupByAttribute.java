// ERROR 18
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import utils.equations.ColumnValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class E013_UnnecessaryGroupByAttribute extends QueryVisitor {

    private final ArrayList<ColumnValue> selectAttributes = new ArrayList<>();
    private final ArrayList<ColumnValue> havingAttributes = new ArrayList<>();
    private final ArrayList<ColumnValue> groupByAttributes = new ArrayList<>();
    private final ArrayList<String> allTableColumns = new ArrayList<>();
    private boolean allColumns = false;
    private boolean parsingAggregationFunction = false;
    private final ArrayList<String> aggregationFunctions = new ArrayList<>(Arrays.asList("avg", "checksum_agg", "count", "count_big", "max", "min", "stdev", "stdevp", "sum", "var", "varp"));
    private final ArrayList<String> unnecessaryGroupByAttributes = new ArrayList<>();

    public E013_UnnecessaryGroupByAttribute() {
        outputMessage = "unnecessary group by attribute";
    }

    private void detectUnnecessaryGroupByAttributes() {
        if (allColumns)
            return;
        for (ColumnValue groupByAttribute : groupByAttributes)
            if (!selectAttributes.contains(groupByAttribute) && !havingAttributes.contains(groupByAttribute))
                if (groupByAttribute.getTable() != null && !allTableColumns.contains(groupByAttribute.getTable()))
                    unnecessaryGroupByAttributes.add(groupByAttribute.toString());
    }

    private void addMissingTableNames() {
        for (ColumnValue columnValue : selectAttributes)
            addMissingTableName(columnValue);
        for (ColumnValue columnValue : havingAttributes)
            addMissingTableName(columnValue);
        for (ColumnValue columnValue : groupByAttributes)
            addMissingTableName(columnValue);
        for (Map.Entry<String, String> tableAlias : tableAliases.entrySet())
            if (allTableColumns.contains(tableAlias.getKey())) {
                allTableColumns.remove(tableAlias.getKey());
                allTableColumns.add(tableAlias.getValue());
            }
    }

    private void addMissingTableName(ColumnValue columnValue) {
        if ((columnValue.getTable() == null || columnValue.getTable().equals("null")) && (columnValue.getAlias() != null && !columnValue.getAlias().equals("null")))
            if (tableAliases.containsKey(columnValue.getAlias()))
                columnValue.setTable(tableAliases.get(columnValue.getAlias()));
            else
                columnValue.setTable(columnValue.getAlias());
    }

    private void detectParsingAggregationFunction(Function function) {
        String expression = function.toString().toLowerCase();
        String func = expression.split("\\(")[0];
        if (aggregationFunctions.contains(func))
            parsingAggregationFunction = true;
    }

    @Override
    public ArrayList<String> getResults() {
        addMissingTableNames();
        detectUnnecessaryGroupByAttributes();
        ArrayList<String> results = new ArrayList<>(unnecessaryGroupByAttributes);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        allColumns = false;
        selectAttributes.clear();
        havingAttributes.clear();
        groupByAttributes.clear();
        allTableColumns.clear();
        unnecessaryGroupByAttributes.clear();
    }

    @Override
    public void visit(Function function) {
        parsingFunction = true;
        detectParsingAggregationFunction(function);
        if (function.getParameters() != null)
            visit(function.getParameters());
        parsingFunction = false;
        parsingAggregationFunction = false;
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        if (selectExpressionItem.getAlias() != null)
            selectAttributes.add(new ColumnValue("null", selectExpressionItem.getAlias().getName(), "null"));
        super.visit(selectExpressionItem);
    }

    @Override
    public void visit(AllColumns allColumns) {
        this.allColumns = true;
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        if (allTableColumns.getTable() != null) {
            visit(allTableColumns.getTable());
            this.allTableColumns.add(allTableColumns.getTable().getFullyQualifiedName());
        }
    }

    @Override
    public void visit(Column column) {
        if (parsingSelect && !parsingAggregationFunction) {
            selectAttributes.add(new ColumnValue(getTableNameFromColumn(column), column.getColumnName(), getTableAliasFromColumn(column)));
            selectAttributes.get(selectAttributes.size() - 1).setCurrentQuery(currentQuery);
        }
        if (parsingHaving && !parsingAggregationFunction) {
            havingAttributes.add(new ColumnValue(getTableNameFromColumn(column), column.getColumnName(), getTableAliasFromColumn(column)));
            havingAttributes.get(havingAttributes.size() - 1).setCurrentQuery(currentQuery);
        }
        if (parsingGroupBy && !parsingFunction) {
            groupByAttributes.add(new ColumnValue(getTableNameFromColumn(column), column.getColumnName(), getTableAliasFromColumn(column)));
            groupByAttributes.get(groupByAttributes.size() - 1).setCurrentQuery(currentQuery);
        }
    }
}
