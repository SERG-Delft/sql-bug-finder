// ERROR 29
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.ArrayList;

public class E023_StrangeHavingClause extends QueryVisitor {

    private final ArrayList<String> groupByAttributes = new ArrayList<>();
    private final ArrayList<String> havingAttributes = new ArrayList<>();
    private final ArrayList<String> strangeHavingClause = new ArrayList<>();

    public E023_StrangeHavingClause() {
        outputMessage = "strange having clause";
    }

    private void detectStrangeHavingClause() {
        if (groupByAttributes.size() == 0 && havingAttributes.size() != 0)
            strangeHavingClause.add("query using having clause without group by clause");
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(strangeHavingClause);
        strangeHavingClause.clear();
        return results;
    }

    @Override
    public void cleanup() {
        groupByAttributes.clear();
        havingAttributes.clear();
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        super.visit(plainSelect);
        detectStrangeHavingClause();
        cleanup();
    }

    @Override
    public void visit(Column column) {
        if (parsingGroupBy)
            groupByAttributes.add(column.getColumnName());
        if (parsingHaving)
            havingAttributes.add(column.getColumnName());
    }
}
