package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;

import java.util.ArrayList;
import java.util.HashSet;

public class E000_ComputeQueryComplexity extends QueryVisitor {

    private Long predicates = 0L;
    private Long subqueries = 0L;
    private Long functions = 0L;
    private Long columns = 0L;
    private final HashSet<String> tables = new HashSet<>();

    public E000_ComputeQueryComplexity() {
        outputMessage = "query complexity";
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>();
        results.add("predicates: " + predicates.toString());
        results.add("joins: " + tables.size());
        results.add("subqueries: " + subqueries.toString());
        results.add("functions: " + functions.toString());
        results.add("columns: " + columns.toString());
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        predicates = 0L;
        subqueries = 0L;
        functions = 0L;
        columns = 0L;
        tables.clear();
    }

    @Override
    public void visit(Column column) {
        columns += 1;
    }

    @Override
    public void visit(Table table) {
        tables.add(table.toString());
    }

    @Override
    public void visit(Function function) {
        functions += 1;
    }

    @Override
    public void visit(SubSelect subSelect) {
        subqueries += 1;
    }

    @Override
    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        if (parsingWhere)
            predicates += 2;
        super.visitBinaryExpression(binaryExpression);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getWhere() != null)
            predicates += 1;
        super.visit(plainSelect);
    }

    @Override
    public void visit(Delete delete) {
        if (delete.getWhere() != null)
            predicates += 1;
        super.visit(delete);
    }

    @Override
    public void visit(Update update) {
        if (update.getWhere() != null)
            predicates += 1;
        super.visit(update);
    }
}
