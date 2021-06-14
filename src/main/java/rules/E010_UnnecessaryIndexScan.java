// ERROR SA0007
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;

import java.util.ArrayList;

public class E010_UnnecessaryIndexScan extends QueryVisitor {

    private final ArrayList<String> unnecessaryIndexScans = new ArrayList<>();

    public E010_UnnecessaryIndexScan() {
        outputMessage = "unnecessary index scan";
    }

    private void detectUnnecessaryIndexScan(LikeExpression likeExpression) {
        Expression leftExpression = likeExpression.getLeftExpression();
        Expression rightExpression = likeExpression.getRightExpression();
        if (isColumn(leftExpression) && isStringValue(rightExpression)) {
            String value = getExpressionValue(rightExpression);
            if (value.startsWith("%"))
                unnecessaryIndexScans.add(value);
        }
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(unnecessaryIndexScans);
        unnecessaryIndexScans.clear();
        return results;
    }

    @Override
    public void cleanup() {}

    @Override
    public void visit(LikeExpression likeExpression) {
        detectUnnecessaryIndexScan(likeExpression);
        visitBinaryExpression(likeExpression);
    }
}
