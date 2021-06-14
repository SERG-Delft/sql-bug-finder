// ERROR 3
package rules;

import interfaces.QueryVisitor;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class E002_ConstantOutputColumn extends QueryVisitor {

    private boolean allTableColumns = false;
    private boolean parsingSubSelect = false;
    private final HashSet<String> columns = new HashSet<>();
    private final HashSet<String> aliases = new HashSet<>();
    public final HashSet<String> fromTableNames = new HashSet<>();
    private final ArrayList<String> constantColumns = new ArrayList<>();
    private final HashSet<String> allTableColumnsStars = new HashSet<>();

    public E002_ConstantOutputColumn() {
        outputMessage = "constant output column";
    }

    private String getAliasForTable(String table) {
        for (Map.Entry<String, String> entry : tableAliases.entrySet())
            if (entry.getValue().equals(table))
                return entry.getKey();
        return "";
    }

    private void detectConstantOutputColumn(EqualsTo equalsTo) {
        Expression leftExpression = equalsTo.getLeftExpression();
        Expression rightExpression = equalsTo.getRightExpression();
        if (parsingSubSelect || parsingFunction || (isColumn(leftExpression) && isColumn(rightExpression)))
            return;
        if (isColumn(leftExpression) && (isStringValue(rightExpression) || isLongValue(rightExpression))) {
            String column = ((Column) equalsTo.getLeftExpression()).getColumnName();
            String table = getTableNameFromColumn((Column) equalsTo.getLeftExpression());
            if (!table.equals("null") && !fromTableNames.contains(table))
                return;
            if (columns.contains(column) || aliases.contains(column) || allTableColumns)
                constantColumns.add(equalsTo.toString());
            else if (allTableColumnsStars.contains(table) || allTableColumnsStars.contains(getAliasForTable(table)))
                constantColumns.add(equalsTo.toString());
        }
    }

    @Override
    public ArrayList<String> getResults() {
        ArrayList<String> results = new ArrayList<>(constantColumns);
        cleanup();
        return results;
    }

    @Override
    public void cleanup() {
        columns.clear();
        aliases.clear();
        fromTableNames.clear();
        constantColumns.clear();
        allTableColumnsStars.clear();
        allTableColumns = false;
        parsingSubSelect = false;
    }

    @Override
    public void visit(CaseExpression caseExpression) {}

    @Override
    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        if (binaryExpression.toString().contains(" OR "))
            return;
        super.visitBinaryExpression(binaryExpression);
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        if (allTableColumns.getTable() != null) {
            allTableColumnsStars.add(allTableColumns.getTable().getFullyQualifiedName());
            visit(allTableColumns.getTable());
        }
    }

    @Override
    public void visit(Table table) {
        if (parsingFrom)
            fromTableNames.add(table.getFullyQualifiedName());
        super.visit(table);
    }

    @Override
    public void visit(AllColumns allColumns) {
        allTableColumns = true;
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);
        if (selectExpressionItem.getAlias() != null && parsingSelect && !parsingSubSelect)
            aliases.add(selectExpressionItem.getAlias().getName());
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        detectConstantOutputColumn(equalsTo);
        visitBinaryExpression(equalsTo);
    }

    @Override
    public void visit(Column column) {
        if (parsingSelect && !parsingSubSelect && !parsingFunction)
            columns.add(column.getColumnName());
    }

    @Override
    public void visit(SubSelect subSelect) {
        parsingSubSelect = true;
        super.visit(subSelect);
        parsingSubSelect = false;
    }
}
