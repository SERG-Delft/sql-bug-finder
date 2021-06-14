// ERROR 26
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.ArrayList;

public class E021_MismatchInSubQuerySelectCondition extends QueryVisitor {

    private boolean parsingInExpression = false;
    private final ArrayList<String> whereColumns = new ArrayList<>();
    private final ArrayList<String> subSelectColumns = new ArrayList<>();
    private final ArrayList<String> mismatchColumns = new ArrayList<>();

    public E021_MismatchInSubQuerySelectCondition() {
        outputMessage = "mismatch in subquery select condition";
    }

    private void detectMismatchInSubQuerySelectCondition() {
        for (String subSelectColumn : subSelectColumns) {
            boolean match = false;
            for (String whereColumn : whereColumns)
                if (whereColumn.contains(subSelectColumn) || subSelectColumn.contains(whereColumn)) {
                    match = true;
                    break;
                }
            if (!match)
                mismatchColumns.add(subSelectColumn);
        }
        subSelectColumns.clear();
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(mismatchColumns);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        whereColumns.clear();
        subSelectColumns.clear();
        mismatchColumns.clear();
    }

    @Override
    public void visit(Column column) {
        if (parsingInExpression && parsingWhere && !parsingSelect)
            whereColumns.add(column.getColumnName());
        if (parsingInExpression && parsingSelect)
            subSelectColumns.add(column.getColumnName());
        if (parsingInExpression && !parsingWhere && !parsingSelect)
            whereColumns.add(column.getColumnName());
    }

    @Override
    public void visit(InExpression inExpression) {
        parsingInExpression = true;
        super.visit(inExpression);
        parsingInExpression = false;
    }

    @Override
    public void visit(SubSelect subSelect) {
        super.visit(subSelect);
        detectMismatchInSubQuerySelectCondition();
    }
}
