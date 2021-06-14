// ERROR 31
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class E024_StrangeWildcardsWithoutLike extends QueryVisitor {

    private final ArrayList<String> wildcards = new ArrayList<>(Arrays.asList("*", "?", "#", "%", "[", "]"));
    private final HashSet<String> strangeWildcards = new HashSet<>();

    public E024_StrangeWildcardsWithoutLike() {
        outputMessage = "wildcards used without like";
    }

    private void detectWildcards(BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        Expression rightExpression = expression.getRightExpression();
        if (!(isColumn(leftExpression) && isStringValue(rightExpression)))
            return;
        String value = getExpressionValue(rightExpression);
        for (String wildcard : wildcards)
            if (value.contains(wildcard))
                strangeWildcards.add(expression.toString());
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(strangeWildcards);
        strangeWildcards.clear();
        return results;
    }

    @Override
    public void cleanup() {}

    @Override
    public void visit(EqualsTo equalsTo) {
        detectWildcards(equalsTo);
        super.visit(equalsTo);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        detectWildcards(notEqualsTo);
        super.visit(notEqualsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        detectWildcards(greaterThan);
        super.visit(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        detectWildcards(greaterThanEquals);
        super.visit(greaterThanEquals);
    }

    @Override
    public void visit(MinorThan minorThan) {
        detectWildcards(minorThan);
        super.visit(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        detectWildcards(minorThanEquals);
        super.visit(minorThanEquals);
    }
}
