// ERROR 11
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;

import java.util.ArrayList;
import java.util.Arrays;

public class E008_LikeWithoutWildcards extends QueryVisitor {

    private final ArrayList<String> likeWithoutWildcards = new ArrayList<>();
    private final ArrayList<String> wildcards = new ArrayList<>(Arrays.asList("*", "?", "#", "%", "_", "[", "]"));

    public E008_LikeWithoutWildcards() {
        outputMessage = "using like without wildcards";
    }

    private void detectLikeWithoutWildcards(LikeExpression likeExpression) {
        Expression leftExpression = likeExpression.getLeftExpression();
        Expression rightExpression = likeExpression.getRightExpression();
        if (!(isColumn(leftExpression) && isStringValue(rightExpression)))
            return;
        String likeValue = getExpressionValue(rightExpression);
        String escape = likeExpression.getEscape();
        likeValue = removeEscapedChars(likeValue, escape);
        boolean foundWildcard = false;
        for (String wildcard : wildcards)
            if (likeValue.contains(wildcard)) {
                foundWildcard = true;
                break;
            }
        if (!foundWildcard)
            likeWithoutWildcards.add(likeExpression.toString());
    }

    private String removeEscapedChars(String value, String escape) {
        if (escape == null)
            return value;
        while (value.contains(escape)) {
            int index = value.indexOf(escape);
            value = value.substring(0, index) + value.substring(index + 2);
        }
        return value;
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(likeWithoutWildcards);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        likeWithoutWildcards.clear();
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        detectLikeWithoutWildcards(likeExpression);
        visitBinaryExpression(likeExpression);
    }
}
