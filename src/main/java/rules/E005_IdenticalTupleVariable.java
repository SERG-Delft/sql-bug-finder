// ERROR 7
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.ArrayList;
import java.util.HashSet;

public class E005_IdenticalTupleVariable extends QueryVisitor {

    private boolean parsingParenthesis = false;
    private final ArrayList<String> identicalTupleVariables = new ArrayList<>();
    private final HashSet<String> removeTupleVariables = new HashSet<>();

    public E005_IdenticalTupleVariable() {
        outputMessage = "identical tuple variables";
    }

    private void detectIdenticalTupleVariables(EqualsTo equalsTo) {
        Expression leftExpression = equalsTo.getLeftExpression();
        Expression rightExpression = equalsTo.getRightExpression();
        if (isColumn(leftExpression) && !isColumn(rightExpression)) {
            addToRemoveTuples((Column) equalsTo.getLeftExpression());
            return;
        }
        if (!(isColumn(leftExpression) && isColumn(rightExpression)))
            return;
        Column leftColumn = (Column) equalsTo.getLeftExpression();
        Column rightColumn = (Column) equalsTo.getRightExpression();
        if (!hasTable(leftColumn) || !hasTable(rightColumn))
            return;
        String leftColumnName = leftColumn.getColumnName();
        String rightColumnName = rightColumn.getColumnName();
        String leftColumnTableName = getTableNameFromColumn(leftColumn);
        String rightColumnTableName = getTableNameFromColumn(rightColumn);
        if (detectMissingTable(leftColumnTableName, leftColumn.toString()) || detectMissingTable(rightColumnTableName, rightColumn.toString()))
            return;
        if (leftColumnTableName.equals(rightColumnTableName) && leftColumnName.equals(rightColumnName))
            identicalTupleVariables.add(equalsTo.toString());
    }

    private void detectWrongIdenticalTupleVariables(BinaryExpression expression) {
        if (expression.getLeftExpression() != null && isColumn(expression.getLeftExpression()))
            addToRemoveTuples((Column) expression.getLeftExpression());
        if (expression.getRightExpression() != null && isColumn(expression.getRightExpression()))
            addToRemoveTuples((Column) expression.getRightExpression());
    }

    private void detectWrongIdenticalTupleVariables(Between between) {
        if (isColumn(between.getLeftExpression()))
            addToRemoveTuples((Column) between.getLeftExpression());
        if (isColumn(between.getBetweenExpressionStart()))
            addToRemoveTuples((Column) between.getBetweenExpressionStart());
        if (isColumn(between.getBetweenExpressionEnd()))
            addToRemoveTuples((Column) between.getBetweenExpressionEnd());
    }

    private void addToRemoveTuples(Column column) {
        String columnTableName = getTableNameFromColumn(column);
        String columnTableAlias = (column.getTable() != null) ? column.getTable().getFullyQualifiedName() : "";
        removeTupleVariables.add(columnTableName);
        removeTupleVariables.add(columnTableAlias);
    }

    private boolean detectMissingTable(String columnTableName, String column) {
        boolean missingTable = false;
        if (columnTableName == null) {
            missingTable = true;
            identicalTupleVariables.add(String.format("detected missing table for %s", column));
        }
        return missingTable;
    }

    @Override
    public ArrayList<String> getResults() {
        for (String removeTuple : removeTupleVariables)
            identicalTupleVariables.removeIf(identicalTuple -> identicalTuple.contains(removeTuple));
        ArrayList<String> results = new ArrayList<>(identicalTupleVariables);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        identicalTupleVariables.clear();
        parsingParenthesis = false;
    }

    @Override
    public void visit(Column column) {
        if (parsingParenthesis || parsingFunction)
            addToRemoveTuples(column);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        detectIdenticalTupleVariables(equalsTo);
        super.visit(equalsTo);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        detectWrongIdenticalTupleVariables(notEqualsTo);
        super.visit(notEqualsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        detectWrongIdenticalTupleVariables(greaterThan);
        super.visit(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        detectWrongIdenticalTupleVariables(greaterThanEquals);
        super.visit(greaterThanEquals);
    }

    @Override
    public void visit(MinorThan minorThan) {
        detectWrongIdenticalTupleVariables(minorThan);
        super.visit(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        detectWrongIdenticalTupleVariables(minorThanEquals);
        super.visit(minorThanEquals);
    }

    @Override
    public void visit(Between between) {
        detectWrongIdenticalTupleVariables(between);
        super.visit(between);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        if (isColumn(isNullExpression.getLeftExpression()))
            addToRemoveTuples((Column) isNullExpression.getLeftExpression());
        super.visit(isNullExpression);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        if (parenthesis.getExpression() instanceof BinaryExpression)
            detectWrongIdenticalTupleVariables((BinaryExpression) parenthesis.getExpression());
        parsingParenthesis = true;
        parenthesis.getExpression().accept(this);
        parsingParenthesis = false;
    }

    @Override
    public void visit(SubSelect subSelect) {}
}
