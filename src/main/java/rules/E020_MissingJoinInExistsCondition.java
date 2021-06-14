// ERROR 25
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Table;

import java.util.ArrayList;
import java.util.Map;

public class E020_MissingJoinInExistsCondition extends QueryVisitor {

    private final ArrayList<String> missingJoins = new ArrayList<>();
    private final ArrayList<String> existExpressions = new ArrayList<>();

    public E020_MissingJoinInExistsCondition() {
        outputMessage = "missing join in exists condition";
    }

    private void detectMissingJoinInExists() {
        for (String existsExpression : existExpressions) {
            boolean hasJoin = false;
            for (String table : allTableNames)
                if (existsExpression.contains(table)) {
                    hasJoin = true;
                    break;
                }
            if (!hasJoin) {
                for (Map.Entry<String, String> entry : tableAliases.entrySet())
                    if (existsExpression.contains(entry.getKey() + ".")) {
                        hasJoin = true;
                        break;
                    }
            }
            if (!hasJoin)
                missingJoins.add(existsExpression);
        }
    }

    @Override
    public ArrayList<String> getResults() {
        detectMissingJoinInExists();
        ArrayList<String> results = new ArrayList<>(missingJoins);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        missingJoins.clear();
        existExpressions.clear();
    }

    @Override
    public void visit(Table table) {
        if (!parsingExists)
            super.visit(table);
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        if (!parsingExists)
            existExpressions.add(existsExpression.toString());
        super.visit(existsExpression);
    }
}
