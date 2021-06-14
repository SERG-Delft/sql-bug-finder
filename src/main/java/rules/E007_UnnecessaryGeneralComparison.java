// ERROR 10
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;

public class E007_UnnecessaryGeneralComparison extends QueryVisitor {

    private final ArrayList<String> unnecessaryGeneralComparisons = new ArrayList<>();

    public E007_UnnecessaryGeneralComparison() {
        outputMessage = "unnecessary general comparison";
    }

    private void detectUnnecessaryGeneralComparison(ComparisonOperator comparisonOperator, String function) {
        Expression leftExpression = comparisonOperator.getLeftExpression();
        Expression rightExpression = comparisonOperator.getRightExpression();
        if (isColumn(leftExpression) && detectFunction(function, rightExpression, ((Column) leftExpression).getColumnName()))
            unnecessaryGeneralComparisons.add(comparisonOperator.toString());
    }

    private boolean detectFunction(String function, Expression expression, String columnName) {
        String exp = expression.toString().toLowerCase();
        String lookup = function + "(" + columnName + ")";
        return exp.contains(lookup);
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(unnecessaryGeneralComparisons);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        unnecessaryGeneralComparisons.clear();
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        detectUnnecessaryGeneralComparison(greaterThanEquals, "max");
        visitBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        detectUnnecessaryGeneralComparison(minorThanEquals, "min");
        visitBinaryExpression(minorThanEquals);
    }
}
