// ERROR 37
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class E025_DivisionByZero extends QueryVisitor {

    private final ArrayList<String> selectColumns = new ArrayList<>();
    private final HashMap<String, Boolean> divisions = new HashMap<>();

    public E025_DivisionByZero() {
        outputMessage = "possible division by zero";
    }

    private void detectDivisionByZero(Division division) {
        Expression leftExpression = division.getLeftExpression();
        Expression rightExpression = division.getRightExpression();
        if (isColumn(leftExpression) && isColumn(rightExpression))
            divisions.put(division.toString(), parsingSelect);
        if (isColumn(leftExpression) && (isStringValue(rightExpression) || isLongValue(rightExpression))) {
            String value = getExpressionValue(rightExpression);
            if (value.equals("0"))
                divisions.put(division.toString(), parsingSelect);
        }
    }

    private void detectCorrectDivision(NotEqualsTo notEqualsTo) {
        Expression leftExpression = notEqualsTo.getLeftExpression();
        Expression rightExpression = notEqualsTo.getRightExpression();
        if (isColumn(leftExpression) && (isStringValue(rightExpression) || isLongValue(rightExpression)))
        {
            String value = getExpressionValue(rightExpression);
            if (value.equals("0"))
                for (Map.Entry<String, Boolean> entry : divisions.entrySet())
                    if (entry.getValue() && entry.getKey().contains(leftExpression.toString()))
                        divisions.remove(entry.getKey());
        }
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(divisions.keySet());
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        divisions.clear();
        selectColumns.clear();
    }

    @Override
    public void visit(Column column) {
        if (parsingSelect)
            selectColumns.add(column.getColumnName());
    }

    @Override
    public void visit(Division division) {
        detectDivisionByZero(division);
        super.visit(division);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        detectCorrectDivision(notEqualsTo);
        super.visit(notEqualsTo);
    }
}
