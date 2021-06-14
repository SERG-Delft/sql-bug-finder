// ERROR 6
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import utils.equations.ColumnEquation;
import utils.equations.ValueEquation;

import java.util.*;

public class E004_UnnecessaryTupleVariable extends QueryVisitor {

    private boolean allTableColumns = false;
    private final HashSet<String> usedTuples = new HashSet<>();
    private final HashMap<String, ArrayList<ColumnEquation>> columnEquations = new HashMap<>();
    private final HashMap<String, ArrayList<ValueEquation>> valueEquations = new HashMap<>();
    private final HashSet<String> tablesInExpressions = new HashSet<>();

    public E004_UnnecessaryTupleVariable() {
        outputMessage = "unnecessary tuple variables";
    }

    private void detectUnnecessaryTupleVariables(BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        Expression rightExpression = expression.getRightExpression();
        if (isColumn(leftExpression) && isColumn(rightExpression))
            addColumnEquation((Column) leftExpression, (Column) rightExpression, expression.toString().split(" ")[1]);
        if (isColumn(leftExpression) && (isStringValue(rightExpression) || isLongValue(rightExpression)))
            addValueEquation((Column) leftExpression, getExpressionValue(rightExpression), expression.toString().split(" ")[1]);
    }

    private void detectUnnecessaryTupleVariables(Between between) {
        Expression leftExpression = between.getLeftExpression();
        Expression betweenStart = between.getBetweenExpressionStart();
        Expression betweenEnd = between.getBetweenExpressionStart();
        if (isColumn(leftExpression) && isColumn(betweenStart))
            addColumnEquation((Column) leftExpression, (Column) betweenStart, ">=");
        if (isColumn(leftExpression) && isColumn(betweenEnd))
            addColumnEquation((Column) leftExpression, (Column) betweenEnd, "<=");
        if (isColumn(leftExpression) && (isStringValue(betweenStart) || isLongValue(betweenStart)))
            addValueEquation((Column) leftExpression, getExpressionValue(betweenStart), ">=");
        if (isColumn(leftExpression) && (isStringValue(betweenEnd) || isLongValue(betweenEnd)))
            addValueEquation((Column) leftExpression, getExpressionValue(betweenEnd), "<=");
    }

    private void addColumnEquation(Column leftColumn, Column rightColumn, String operation) {
        if (!hasTable(leftColumn) || !hasTable(rightColumn))
            return;
        String leftColumnName = leftColumn.getColumnName();
        String rightColumnName = rightColumn.getColumnName();
        String leftColumnTableName = getTableNameFromColumn(leftColumn);
        String rightColumnTableName = getTableNameFromColumn(rightColumn);
        addColumnEquation(leftColumnTableName, rightColumnTableName, rightColumnName, operation);
        addColumnEquation(rightColumnTableName, leftColumnTableName, leftColumnName, operation);
    }

    private void addColumnEquation(String leftColumnTableName, String rightColumnTableName, String rightColumnName, String operation) {
        if (columnEquations.containsKey(leftColumnTableName))
            columnEquations.get(leftColumnTableName).add(new ColumnEquation(rightColumnTableName, rightColumnName, operation));
        else
            columnEquations.put(leftColumnTableName, new ArrayList<>(Collections.singleton(new ColumnEquation(rightColumnTableName, rightColumnName, operation))));
    }

    private void addValueEquation(Column column, String value, String operation) {
        String columnName = column.getColumnName();
        String columnTableName = getTableNameFromColumn(column);
        if (valueEquations.containsKey(columnTableName))
            valueEquations.get(columnTableName).add(new ValueEquation(columnName, value, operation));
        else
            valueEquations.put(columnTableName, new ArrayList<>(Collections.singleton(new ValueEquation(columnName, value, operation))));
    }

    private boolean isValueEquationInColumnEquations(ValueEquation valueEquation, String key) {
        boolean foundColumnEquation = false;
        for (Map.Entry<String, ArrayList<ColumnEquation>> entry : columnEquations.entrySet()) {
            for (ColumnEquation columnEquation : entry.getValue())
                if (columnEquation.getTable().equals(key) && columnEquation.getColumn().equals(valueEquation.getColumn())) {
                    foundColumnEquation = true;
                    break;
                }
            if (foundColumnEquation)
                break;
        }
        if (!foundColumnEquation)
            return false;
        if (columnEquations.get(key) != null) {
            for (ColumnEquation columnEquation : columnEquations.get(key)) {
                if (columnEquation.getColumn().equals(valueEquation.getColumn()) && columnEquation.getOperation().contains("="))
                    return true;
            }
        }
        return false;
    }

    private HashSet<String> getSelectedTables() {
        HashSet<String> selectedTables = new HashSet<>();
        for (String selectedTuple : usedTuples) {
            if (!tableNames.contains(selectedTuple)) {
                if (tableAliases.containsKey(selectedTuple))
                    selectedTables.add(tableAliases.get(selectedTuple));
            }
            else
                selectedTables.add(selectedTuple);
        }
        return selectedTables;
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>();
        if (allTableColumns && tableNames.size() > 1) {
            cleanup();
            return results;
        }
        HashSet<String> selectedTables = getSelectedTables();
        for (String key : valueEquations.keySet()) {
            if (tablesInExpressions.contains(key) || selectedTables.contains(key))
                continue;
            boolean unnecessaryTuple = true;
            for (ValueEquation valueEquation : valueEquations.get(key)) {
                if (!isValueEquationInColumnEquations(valueEquation, key)) {
                    unnecessaryTuple = false;
                    break;
                }
            }
            if (unnecessaryTuple)
                results.add(key);
        }
        if (results.contains("null") && tableNames.size() != 0)
            results.remove("null");
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        usedTuples.clear();
        columnEquations.clear();
        valueEquations.clear();
        tablesInExpressions.clear();
        allTableColumns = false;
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        if (allTableColumns.getTable() != null) {
            usedTuples.add(allTableColumns.getTable().getFullyQualifiedName());
            visit(allTableColumns.getTable());
        }
    }

    @Override
    public void visit(Table table) {
        if (parsingFrom && usedTuples.isEmpty())
            usedTuples.add(table.getFullyQualifiedName());
        super.visit(table);
    }

    @Override
    public void visit(Column column) {
        if (parsingSelect && !parsingSubSelect && !parsingFunction)
            if (column.getTable() != null)
                usedTuples.add(column.getTable().getFullyQualifiedName());
        if (!parsingSelect && parsingFunction)
            if (column.getTable() != null)
                usedTuples.add(column.getTable().getFullyQualifiedName());
    }

    @Override
    public void visit(InExpression inExpression) {
        super.visit(inExpression);
        if (inExpression.getLeftExpression() instanceof Column)
            tablesInExpressions.add(getTableNameFromColumn((Column) inExpression.getLeftExpression()));
    }

    @Override
    public void visit(AllColumns allColumns) {
        allTableColumns = true;
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        detectUnnecessaryTupleVariables(equalsTo);
        super.visit(equalsTo);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        detectUnnecessaryTupleVariables(notEqualsTo);
        super.visit(notEqualsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        detectUnnecessaryTupleVariables(greaterThan);
        super.visit(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        detectUnnecessaryTupleVariables(greaterThanEquals);
        super.visit(greaterThanEquals);
    }

    @Override
    public void visit(MinorThan minorThan) {
        detectUnnecessaryTupleVariables(minorThan);
        super.visit(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        detectUnnecessaryTupleVariables(minorThanEquals);
        super.visit(minorThanEquals);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        detectUnnecessaryTupleVariables(likeExpression);
        super.visit(likeExpression);
    }

    @Override
    public void visit(Between between) {
        detectUnnecessaryTupleVariables(between);
        super.visit(between);
    }
}
