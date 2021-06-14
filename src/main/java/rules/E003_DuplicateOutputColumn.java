// ERROR 4
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;


import java.util.ArrayList;
import java.util.HashSet;

public class E003_DuplicateOutputColumn extends QueryVisitor {

    private boolean allTableColumns = false;
    private final ArrayList<String> columns = new ArrayList<>();
    private final ArrayList<String> expressions = new ArrayList<>();
    private final ArrayList<String> aliases = new ArrayList<>();
    private final HashSet<String> duplicateColumns = new HashSet<>();
    private final HashSet<String> duplicateExpressions = new HashSet<>();

    public E003_DuplicateOutputColumn() {
        outputMessage = "duplicate output column";
    }

    private boolean detectDuplicateExpressions(SelectExpressionItem selectExpressionItem) {
        Expression exp = selectExpressionItem.getExpression();
        if (exp instanceof CaseExpression || exp instanceof CastExpression || exp instanceof FullTextSearch ||
            exp instanceof AnalyticExpression || exp instanceof Parenthesis || exp instanceof Function ||
            exp instanceof Addition || exp instanceof Subtraction || exp instanceof Multiplication || exp instanceof Division) {
            String expression = selectExpressionItem.getExpression().toString();
            if (expressions.contains(expression))
                duplicateExpressions.add(expression);
            else
                expressions.add(expression);
            return true;
        }
        return false;
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(duplicateColumns);
        results.addAll(duplicateExpressions);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        columns.clear();
        aliases.clear();
        expressions.clear();
        duplicateColumns.clear();
        duplicateExpressions.clear();
        allTableColumns = false;
    }

    @Override
    public void visit(AllColumns allColumns) {
        this.allTableColumns = true;
    }

    @Override
    public void visit(SetOperationList setOperationList) {
        if (setOperationList.getSelects() != null) {
            if (setOperationList.getOperations() != null)
                for (SetOperation operation : setOperationList.getOperations())
                    if (operation.toString().toLowerCase().contains("union"))
                        return;
            for (SelectBody selectBody : setOperationList.getSelects())
                selectBody.accept(this);
        }
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        super.visit(selectExpressionItem);
        if (!parsingSelect || parsingSubSelect || parsingFunction || parsingIn || parsingExists || parsingWhere)
            return;
        if (detectDuplicateExpressions(selectExpressionItem))
            return;
        else {
            String expression = selectExpressionItem.getExpression().toString();
            if (columns.contains(expression) || aliases.contains(expression) || allTableColumns)
                duplicateColumns.add(expression);
            else
                columns.add(expression);
        }
        if (selectExpressionItem.getAlias() != null)
            aliases.add(selectExpressionItem.getAlias().getName());
    }

    @Override
    public void visit(Delete delete) {}

    @Override
    public void visit(Update update) {}

    @Override
    public void visit(Insert insert) {}

    @Override
    public void visit(Replace replace) {}
}
