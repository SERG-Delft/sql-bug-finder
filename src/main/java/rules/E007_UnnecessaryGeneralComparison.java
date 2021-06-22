// ERROR 10
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.List;

public class E007_UnnecessaryGeneralComparison extends QueryVisitor {

    private final ArrayList<String> unnecessaryGeneralComparisons = new ArrayList<>();

    public E007_UnnecessaryGeneralComparison() {
        outputMessage = "unnecessary general comparison";
    }

    private void detectUnnecessaryGeneralComparison(ComparisonOperator comparisonOperator, String function) {
        List<Expression> expressions = new ArrayList<>();
        E002_ConstantOutputColumn complexity = new E002_ConstantOutputColumn();

        Expression leftExpression = comparisonOperator.getLeftExpression();
        Expression rightExpression = comparisonOperator.getRightExpression();
        expressions.add(rightExpression);
        complexity.visit(new ExpressionList(expressions));

        String tableName = getTableNameFromColumn((Column)  leftExpression);
        if (shouldCheckSubQuery(tableName, complexity.allTableNames))
            if (isColumn(leftExpression) && detectFunction(function, rightExpression, ((Column) leftExpression).getColumnName()))
                unnecessaryGeneralComparisons.add(comparisonOperator.toString());
    }

    private boolean detectFunction(String function, Expression expression, String columnName) {
        String extractedFunction = "";
        String exp = expression.toString().toLowerCase();
        String regex = "(?i)((min)|(max))\\([a-z]*\\.*" + columnName + "\\)";
        if (exp.contains(function)) {
            int index = exp.indexOf(function);
            while (exp.charAt(index) != ')') {
                extractedFunction += exp.charAt(index++);
            }
            extractedFunction += ')';
        }
        return extractedFunction.matches(regex);
    }

    private boolean shouldCheckSubQuery(String tableName, ArrayList<String> subQueryTables) {
        if (!tableName.equals("null") && subQueryTables.contains(tableName))
            return true;
        if (tableName.equals("null")) {
            for (String table : subQueryTables)
                if (!this.allTableNames.contains(table))
                    return false;
            return true;
        }
        return false;
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
