// ERROR 9
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;

import java.util.ArrayList;

public class E006_ComparisonWithNull extends QueryVisitor {

    private ArrayList<String> comparisonWithNulls = new ArrayList<>();

    public E006_ComparisonWithNull() {
        outputMessage = "comparison with NULL";
    }

    private void detectComparisonWithNull(ComparisonOperator comparisonOperator) {
        Expression leftExpression = comparisonOperator.getLeftExpression();
        Expression rightExpression = comparisonOperator.getRightExpression();
        if (isColumn(leftExpression) && isNull(rightExpression))
            comparisonWithNulls.add(comparisonOperator.toString());
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(comparisonWithNulls);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        comparisonWithNulls.clear();
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        detectComparisonWithNull(equalsTo);
        visitBinaryExpression(equalsTo);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        detectComparisonWithNull(notEqualsTo);
        visitBinaryExpression(notEqualsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        detectComparisonWithNull(greaterThan);
        visitBinaryExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        detectComparisonWithNull(greaterThanEquals);
        visitBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(MinorThan minorThan) {
        detectComparisonWithNull(minorThan);
        visitBinaryExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        detectComparisonWithNull(minorThanEquals);
        visitBinaryExpression(minorThanEquals);
    }
}
