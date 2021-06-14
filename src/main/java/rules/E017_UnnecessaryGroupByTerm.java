// ERROR 21_1
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import utils.equations.ColumnValue;

import java.util.ArrayList;
import java.util.HashSet;

public class E017_UnnecessaryGroupByTerm extends QueryVisitor {

    private final ArrayList<ColumnValue> whereAttributes = new ArrayList<>(); // only those which have a single value (equalsTo)
    private final ArrayList<ColumnValue> groupByAttributes = new ArrayList<>(); // all attributes under the group by clause
    private final ArrayList<ColumnValue> multiValueAttributes = new ArrayList<>();
    private final HashSet<String> unnecessaryGroupByTerms = new HashSet<>();

    public E017_UnnecessaryGroupByTerm() {
        outputMessage = "unnecessary group by term";
    }

    private void detectUnnecessaryGroupByTerms() {
        for (ColumnValue whereAttribute : whereAttributes)
            for (ColumnValue groupByAttribute : groupByAttributes)
                if (groupByAttribute.exactMatch(whereAttribute) && !multiValueAttributes.contains(whereAttribute))
                    unnecessaryGroupByTerms.add(whereAttribute.toString());
    }

    private void detectWhereAttribute(BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        Expression rightExpression = expression.getRightExpression();
        if ((expression instanceof EqualsTo) && isColumn(leftExpression) && (isStringValue(rightExpression) || isLongValue(rightExpression)) && parsingWhere)
            addColumnValueToList((Column) leftExpression, "whereAttributes");
        if (!(expression instanceof EqualsTo)) {
            if (isColumn(leftExpression))
                addColumnValueToList((Column) leftExpression, "multiValueAttributes");
            if (isColumn(rightExpression))
                addColumnValueToList((Column) rightExpression, "multiValueAttributes");
        }
    }

    private void addColumnValueToList(Column column, String list) {
        if (list.equals("whereAttributes"))
            whereAttributes.add(new ColumnValue(getTableNameFromColumn(column), column.getColumnName(), getTableAliasFromColumn(column)));
        if (list.equals("multiValueAttributes"))
            multiValueAttributes.add(new ColumnValue(getTableNameFromColumn(column), column.getColumnName(), getTableAliasFromColumn(column)));
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(unnecessaryGroupByTerms);
        unnecessaryGroupByTerms.clear();
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        whereAttributes.clear();
        groupByAttributes.clear();
        multiValueAttributes.clear();
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        super.visit(plainSelect);
        detectUnnecessaryGroupByTerms();
        cleanup();
    }

    @Override
    public void visit(Column column) {
        if (parsingGroupBy)
            groupByAttributes.add(new ColumnValue(getTableNameFromColumn(column), column.getColumnName(), getTableAliasFromColumn(column)));
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        detectWhereAttribute(equalsTo);
        visitBinaryExpression(equalsTo);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        detectWhereAttribute(notEqualsTo);
        visitBinaryExpression(notEqualsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        detectWhereAttribute(greaterThan);
        visitBinaryExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        detectWhereAttribute(greaterThanEquals);
        visitBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(MinorThan minorThan) {
        detectWhereAttribute(minorThan);
        visitBinaryExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        detectWhereAttribute(minorThanEquals);
        visitBinaryExpression(minorThanEquals);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        detectWhereAttribute(likeExpression);
        visitBinaryExpression(likeExpression);
    }
}
