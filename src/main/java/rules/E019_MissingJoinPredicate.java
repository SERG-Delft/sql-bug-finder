// ERROR 24
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;
import utils.equations.SubSelectTuples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class E019_MissingJoinPredicate extends QueryVisitor {

    private int subSelectIndex = 1;
    private final HashMap<Integer, SubSelectTuples> subSelects = new HashMap<>();
    private final HashSet<String> missingPredicates = new HashSet<>();

    public E019_MissingJoinPredicate() {
        outputMessage = "missing join predicate";
        subSelects.put(0, new SubSelectTuples());
    }

    private void detectMissingPredicates() {
        String query = subSelects.get(subSelectIndex - 1).getSubQuery();
        HashSet<String> tuples = subSelects.get(subSelectIndex - 1).getTuples();
        ArrayList<String> tables = subSelects.get(subSelectIndex - 1).getTables();
        tuples.remove("null");
        // no need to detect missing predicates if no JOIN or WHERE clauses are present
        if (!query.toLowerCase().contains("join") && !query.toLowerCase().contains("where"))
            return;
        // corner-case: IN clause inside of WHERE and only two tables being used most certainly does not have missing joins
        if (query.toLowerCase().contains("where") && query.toLowerCase().contains(" in ") && tables.size() == 2)
            return;
        // no need to detect missing predicates if no table joins are present or cross join is used
        if (tables.size() < 2)
            return;
        for (String table : tables)
            if (!tuples.contains(table))
                missingPredicates.add(table);
    }

    private void detectExistingPredicates(BinaryExpression comparison) {
        if (!(parsingOn || parsingWhere))
            return;
        if (isColumn(comparison.getLeftExpression()))
            detectPredicateInColumnExpression((Column) comparison.getLeftExpression());
        else if (comparison.getLeftExpression() instanceof BinaryExpression) {
            if (isColumn(((BinaryExpression) comparison.getLeftExpression()).getLeftExpression()))
                detectPredicateInColumnExpression((Column) ((BinaryExpression) comparison.getLeftExpression()).getLeftExpression());
            if (isColumn(((BinaryExpression) comparison.getLeftExpression()).getRightExpression()))
                detectPredicateInColumnExpression((Column) ((BinaryExpression) comparison.getLeftExpression()).getRightExpression());
        }
        if (isColumn(comparison.getRightExpression()))
            detectPredicateInColumnExpression((Column) comparison.getRightExpression());
        else if (comparison.getRightExpression() instanceof BinaryExpression) {
            if (isColumn(((BinaryExpression) comparison.getRightExpression()).getLeftExpression()))
                detectPredicateInColumnExpression((Column) ((BinaryExpression) comparison.getRightExpression()).getLeftExpression());
            if (isColumn(((BinaryExpression) comparison.getRightExpression()).getRightExpression()))
                detectPredicateInColumnExpression((Column) ((BinaryExpression) comparison.getRightExpression()).getRightExpression());
        }
    }

    private void detectExistingPredicates(Between between) {
        if (!(parsingOn || parsingWhere))
            return;
        if (isColumn(between.getLeftExpression()))
            detectPredicateInColumnExpression((Column) between.getLeftExpression());
        if (isColumn(between.getBetweenExpressionStart()))
            detectPredicateInColumnExpression((Column) between.getBetweenExpressionStart());
        if (isColumn(between.getBetweenExpressionEnd()))
            detectPredicateInColumnExpression((Column) between.getBetweenExpressionEnd());
    }

    private void detectPredicateInColumnExpression(Column column) {
        String columnTableName = getTableNameFromColumn(column);
        if (columnTableName != null)
            subSelects.get(subSelectIndex - 1).addTuple(columnTableName);
    }

    private void setInitialQuery(String query) {
        if (subSelectIndex == 1 && subSelects.get(0).getSubQuery().equals(""))
            subSelects.get(0).setSubQuery(query);
    }

    @Override
    public ArrayList<String> getResults() {
        detectMissingPredicates();
        ArrayList<String> results = new ArrayList<>(missingPredicates);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        missingPredicates.clear();
        subSelects.clear();
        subSelectIndex = 1;
        subSelects.put(0, new SubSelectTuples());
    }

    @Override
    public void visit(Table table) {
        if (isCrossJoin) {
            ArrayList<String> prevSubSelectTables;
            if (subSelectIndex > 1)
                prevSubSelectTables = subSelects.get(subSelectIndex - 2).getTables();
            else
                prevSubSelectTables = allTableNames;
            if (!prevSubSelectTables.isEmpty()) {
                subSelects.get(subSelectIndex - 1).addTuple(table.getFullyQualifiedName());
                subSelects.get(subSelectIndex - 1).addTuple(prevSubSelectTables.get(prevSubSelectTables.size() - 1));
            }
        }
        super.visit(table);
        if (!tableAliases.containsKey(table.getFullyQualifiedName()) && !parsingSelect)
            subSelects.get(subSelectIndex - 1).addTable(table.getFullyQualifiedName());
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        setInitialQuery(plainSelect.toString());
        super.visit(plainSelect);
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelects.put(subSelectIndex++, new SubSelectTuples());
        subSelects.get(subSelectIndex - 1).setSubQuery(subSelect.toString());
        super.visit(subSelect);
        detectMissingPredicates();
        subSelectIndex -= 1;
        if (subSelect.getAlias() != null)
            subSelects.get(subSelectIndex - 1).addTable(subSelect.getAlias().getName());
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        detectExistingPredicates(equalsTo);
        super.visit(equalsTo);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        detectExistingPredicates(notEqualsTo);
        super.visit(notEqualsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        detectExistingPredicates(greaterThan);
        super.visit(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        detectExistingPredicates(greaterThanEquals);
        super.visit(greaterThanEquals);
    }

    @Override
    public void visit(MinorThan minorThan) {
        detectExistingPredicates(minorThan);
        super.visit(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        detectExistingPredicates(minorThanEquals);
        super.visit(minorThanEquals);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        detectExistingPredicates(likeExpression);
        super.visit(likeExpression);
    }

    @Override
    public void visit(Between between) {
        detectExistingPredicates(between);
        super.visit(between);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        if (isColumn(isNullExpression.getLeftExpression()))
            detectPredicateInColumnExpression((Column) isNullExpression.getLeftExpression());
        super.visit(isNullExpression);
    }

    @Override
    public void visit(Delete delete) {
        setInitialQuery(delete.toString());
        super.visit(delete);
    }

    @Override
    public void visit(Update update) {
        setInitialQuery(update.toString());
        super.visit(update);
    }

    @Override
    public void visit(Insert insert) {
        setInitialQuery(insert.toString());
        if (insert.getItemsList() != null)
            insert.getItemsList().accept(this);
        if (insert.getSelect() != null)
            this.visit(insert.getSelect());
    }

    @Override
    public void visit(Replace replace) {
        setInitialQuery(replace.toString());
        super.visit(replace);
    }
}
