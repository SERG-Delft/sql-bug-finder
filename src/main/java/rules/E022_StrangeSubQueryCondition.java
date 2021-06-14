// ERROR 27
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SubSelect;
import utils.equations.SubSelectTuples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class E022_StrangeSubQueryCondition extends QueryVisitor {

    private int subSelectIndex = 1;
    private final HashMap<Integer, SubSelectTuples> subSelects = new HashMap<>();
    private final ArrayList<String> strangeSubQueryConditions = new ArrayList<>();

    public E022_StrangeSubQueryCondition() {
        outputMessage = "strange subquery condition";
        subSelects.put(0, new SubSelectTuples());
    }

    private void detectStrangeSubQueryConditions(ComparisonOperator comparisonOperator) {
        if (subSelectIndex == 1)
            return;
        Expression leftExpression = comparisonOperator.getLeftExpression();
        Expression rightExpression = comparisonOperator.getRightExpression();
        HashSet<String> tuples = subSelects.get(subSelectIndex - 1).getTuples();
        if (isColumn(leftExpression) && (isStringValue(rightExpression) || isLongValue(rightExpression)))
            if (!tuples.contains(getTableNameFromColumn((Column) leftExpression)))
                strangeSubQueryConditions.add(comparisonOperator.toString());
        if (isColumn(leftExpression) && isColumn(rightExpression))
            if (!tuples.contains(getTableNameFromColumn((Column) leftExpression)) && !tuples.contains(getTableNameFromColumn((Column) rightExpression)))
                strangeSubQueryConditions.add(comparisonOperator.toString());
    }

    private void detectStrangeSubQueryConditions(Between between) {
        if (subSelectIndex == 1)
            return;
        Expression leftExpression = between.getLeftExpression();
        Expression betweenStart = between.getBetweenExpressionStart();
        Expression betweenEnd = between.getBetweenExpressionEnd();
        HashSet<String> tuples = subSelects.get(subSelectIndex - 1).getTuples();
        if (isColumn(leftExpression) && isColumn(betweenStart) && isColumn(betweenEnd))
            if(!tuples.contains(getTableNameFromColumn((Column) leftExpression)) &&
               !tuples.contains(getTableNameFromColumn((Column) betweenStart)) &&
               !tuples.contains(getTableNameFromColumn((Column) betweenEnd))) {
                strangeSubQueryConditions.add(between.toString());
            }
        if (isColumn(leftExpression) && !isColumn(betweenStart) && !isColumn(betweenEnd))
            if (!tuples.contains(getTableNameFromColumn((Column) leftExpression)))
                strangeSubQueryConditions.add(between.toString());
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(strangeSubQueryConditions);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        strangeSubQueryConditions.clear();
        subSelects.clear();
        subSelectIndex = 1;
        subSelects.put(0, new SubSelectTuples());
    }

    @Override
    public void visit(Table table) {
        super.visit(table);
        subSelects.get(subSelectIndex - 1).addTuple(table.getFullyQualifiedName());
        if (table.getAlias() != null) {
            subSelects.get(subSelectIndex - 1).addTuple(table.getAlias().getName());
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelects.put(subSelectIndex++, new SubSelectTuples());
        subSelects.get(subSelectIndex - 1).setSubQuery(subSelect.toString());
        super.visit(subSelect);
        subSelectIndex -= 1;
        if (subSelect.getAlias() != null)
            subSelects.get(subSelectIndex - 1).addTuple(subSelect.getAlias().getName());
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        detectStrangeSubQueryConditions(equalsTo);
        visitBinaryExpression(equalsTo);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        detectStrangeSubQueryConditions(notEqualsTo);
        visitBinaryExpression(notEqualsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        detectStrangeSubQueryConditions(greaterThan);
        visitBinaryExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        detectStrangeSubQueryConditions(greaterThanEquals);
        visitBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(MinorThan minorThan) {
        detectStrangeSubQueryConditions(minorThan);
        visitBinaryExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        detectStrangeSubQueryConditions(minorThanEquals);
        visitBinaryExpression(minorThanEquals);
    }

    @Override
    public void visit(Between between) {
        detectStrangeSubQueryConditions(between);
        super.visit(between);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        if (isColumn(isNullExpression.getLeftExpression()) && subSelectIndex > 1) {
            HashSet<String> tuples = subSelects.get(subSelectIndex - 1).getTuples();
            if (!tuples.contains(getTableNameFromColumn((Column) isNullExpression.getLeftExpression())))
                strangeSubQueryConditions.add(isNullExpression.toString());
        }
        super.visit(isNullExpression);
    }
}
