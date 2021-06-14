package interfaces;

import db.tables.EvoSqlQueries;
import db.tables.Queries;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.io.StringReader;
import java.util.*;

public abstract class QueryVisitor implements StatementVisitor, SelectVisitor, SelectItemVisitor, FromItemVisitor, ItemsListVisitor, ExpressionVisitor, GroupByVisitor, OrderByVisitor {

    public final CCJSqlParserManager pm = new CCJSqlParserManager();
    public String outputMessage;
    public String currentQuery;

    public boolean parsingSelect    = false;
    public boolean parsingFrom      = false;
    public boolean parsingJoin      = false;
    public boolean parsingOn        = false;
    public boolean parsingIn        = false;
    public boolean parsingExists    = false;
    public boolean parsingSubSelect = false;
    public boolean parsingWhere     = false;
    public boolean parsingGroupBy   = false;
    public boolean parsingHaving    = false;
    public boolean parsingOrderBy   = false;
    public boolean parsingFunction  = false;
    public boolean isCrossJoin      = false;

    public final HashSet<String> tableNames = new HashSet<>();
    public final HashMap<String, String> tableAliases = new HashMap<>();
    public final ArrayList<String> allTableNames = new ArrayList<>();
    public final HashMap<Long, ArrayList<String>> results = new HashMap<>();
    public final HashMap<Long, ArrayList<String>> bugs = new HashMap<>();
    public final HashMap<Long, ArrayList<String>> errors = new HashMap<>();

    public abstract ArrayList<String> getResults();
    public abstract void cleanup();

    public void findBugs(ArrayList<Queries> queries) throws JSQLParserException {
        for (Queries query : queries) {
            try {
                currentQuery = query.getQuery();
                Statement statement = pm.parse(new StringReader(query.getQuery()));
                statement.accept(this);
                results.put(query.getQueryId(), getResults());
                interfaceCleanup();
            } catch (Exception e) {
                System.out.printf("[ERROR] rule: %s, query: %s\n", this.getClass().getName(), query.getQuery());
                e.printStackTrace();
            }
        }
        showResults(results);
    }

    public void findEvoSqlBugs(ArrayList<EvoSqlQueries> queries) throws JSQLParserException {
        for (EvoSqlQueries query : queries) {
            try {
                currentQuery = query.getQuery();
                Statement statement = pm.parse(new StringReader(query.getQuery()));
                statement.accept(this);
                results.put(query.getQueryId(), getResults());
                interfaceCleanup();
            } catch (Exception e) {
                System.out.printf("[ERROR] rule: %s, query: %s\n", this.getClass().getName(), query.getQuery());
                e.printStackTrace();
            }
        }
        showResults(results);
    }

    public void interfaceCleanup() {
        tableNames.clear();
        tableAliases.clear();
        allTableNames.clear();
    }

    public void showResults(HashMap<Long, ArrayList<String>> results) {
        String errorCode = this.getClass().getName().split("_")[0].split("\\.")[1];
        for (Map.Entry<Long, ArrayList<String>> entry : results.entrySet()) {
            Long key = entry.getKey();
            for (String bug : entry.getValue()) {
                String message = String.format("%s: %s", outputMessage, bug);
                System.out.printf("[query #%d] %s\n", key, message);
                if (bugs.containsKey(key))
                    bugs.get(key).add(message);
                else
                    bugs.put(key, new ArrayList<>(Collections.singletonList(message)));
            }
            if (!entry.getValue().isEmpty())
                if (errors.containsKey(key))
                    errors.get(key).add(errorCode);
                else
                    errors.put(key, new ArrayList<>(Collections.singletonList(errorCode)));
        }
    }

    public HashMap<Long, ArrayList<String>> getBugs() {
        return bugs;
    }

    public HashMap<Long, ArrayList<String>> getErrors() {
        return errors;
    }

    public boolean isNull(Expression expression) {
        return expression instanceof NullValue;
    }

    public boolean isColumn(Expression expression) {
        return expression instanceof Column;
    }

    public boolean isStringValue(Expression expression) {
        return expression instanceof StringValue;
    }

    public boolean isLongValue(Expression expression) {
        return expression instanceof LongValue;
    }

    public String getExpressionValue(Expression expression) {
        if (isStringValue(expression))
            return ((StringValue) expression).getValue();
        if (isLongValue(expression))
            return ((LongValue) expression).getStringValue();
        return "";
    }

    public boolean hasTable(Column column) {
        return column.getTable() != null;
    }

    public String getTableNameFromColumn(Column column) {
        if (column.getTable() == null)
            return "null";
        String tableName = column.getTable().getFullyQualifiedName();
        if (!tableNames.contains(tableName))
            tableName = tableAliases.get(tableName);
        if (tableName == null)
            return "null";
        return tableName;
    }

    public String getTableAliasFromColumn(Column column) {
        if (column.getTable() == null)
            return "null";
        return column.getTable().getName();
    }

    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(Comment comment) {
        if (comment.getTable() != null)
            this.visit(comment.getTable());

        if (comment.getColumn() != null && comment.getColumn().getTable() != null)
            this.visit(comment.getColumn().getTable());
    }

    @Override
    public void visit(Commit commit) {

    }

    @Override
    public void visit(Delete delete) {
        this.visit(delete.getTable());

        if (delete.getJoins() != null)
            for (Join join : delete.getJoins()) {
                join.getRightItem().accept(this);
                if (join.getOnExpression() != null) {
                    parsingOn = true;
                    join.getOnExpression().accept(this);
                    parsingOn = false;
                }
            }

        if (delete.getWhere() != null)
            delete.getWhere().accept(this);
    }

    @Override
    public void visit(Update update) {
        this.visit(update.getTable());

        parsingSelect = true;
        if (update.getSelect() != null)
            update.getSelect().accept(this);
        parsingSelect = false;

        parsingJoin = true;
        if (update.getStartJoins() != null)
            for (Join join : update.getStartJoins()) {
                join.getRightItem().accept(this);
                if (join.getOnExpression() != null) {
                    parsingOn = true;
                    join.getOnExpression().accept(this);
                    parsingOn = false;
                }
            }
        parsingJoin = false;

        if (update.getExpressions() != null)
            for (Expression expression : update.getExpressions())
                expression.accept(this);

        parsingFrom = true;
        if (update.getFromItem() != null)
            update.getFromItem().accept(this);
        parsingFrom = false;

        if (update.getJoins() != null)
            for (Join join : update.getJoins()) {
                join.getRightItem().accept(this);
                if (join.getOnExpression() != null) {
                    parsingOn = true;
                    join.getOnExpression().accept(this);
                    parsingOn = false;
                }
            }

        parsingWhere = true;
        if (update.getWhere() != null)
            update.getWhere().accept(this);
        parsingWhere = false;
    }

    @Override
    public void visit(Insert insert) {
        this.visit(insert.getTable());

        if (insert.getItemsList() != null)
            insert.getItemsList().accept(this);

        if (insert.getSelect() != null)
            this.visit(insert.getSelect());
    }

    @Override
    public void visit(Replace replace) {
        this.visit(replace.getTable());

        if (replace.getExpressions() != null)
            for (Expression expression : replace.getExpressions())
                expression.accept(this);

        if (replace.getItemsList() != null)
            replace.getItemsList().accept(this);
    }

    @Override
    public void visit(Drop drop) {

    }

    @Override
    public void visit(Truncate truncate) {
        this.visit(truncate.getTable());
    }

    @Override
    public void visit(CreateIndex createIndex) {

    }

    @Override
    public void visit(CreateSchema createSchema) {

    }

    @Override
    public void visit(CreateTable createTable) {
        if (createTable.getSelect() != null)
            createTable.getSelect().accept(this);
    }

    @Override
    public void visit(CreateView createView) {

    }

    @Override
    public void visit(AlterView alterView) {

    }

    @Override
    public void visit(Alter alter) {

    }

    @Override
    public void visit(Statements statements) {

    }

    @Override
    public void visit(Execute execute) {

    }

    @Override
    public void visit(SetStatement setStatement) {

    }

    @Override
    public void visit(ShowColumnsStatement showColumnsStatement) {

    }

    @Override
    public void visit(Merge merge) {
        this.visit(merge.getTable());
        if (merge.getUsingTable() != null)
            merge.getUsingTable().accept(this);
        else if (merge.getUsingSelect() != null)
            merge.getUsingSelect().accept((ExpressionVisitor) this);
    }

    @Override
    public void visit(Select select) {
        if (select.getWithItemsList() != null)
            for (WithItem withItem : select.getWithItemsList())
                withItem.accept(this);
        select.getSelectBody().accept(this);
    }

    @Override
    public void visit(Upsert upsert) {
        this.visit(upsert.getTable());

        if (upsert.getItemsList() != null)
            upsert.getItemsList().accept(this);

        if (upsert.getSelect() != null)
            this.visit(upsert.getSelect());
    }

    @Override
    public void visit(UseStatement useStatement) {

    }

    @Override
    public void visit(Block block) {
        if (block.getStatements() != null)
            this.visit(block.getStatements());
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        parsingSelect = true;
        if (plainSelect.getSelectItems() != null)
            for (SelectItem item : plainSelect.getSelectItems())
                item.accept(this);
        parsingSelect = false;

        parsingFrom = true;
        if (plainSelect.getFromItem() != null)
            plainSelect.getFromItem().accept(this);
        parsingFrom = false;

        parsingJoin = true;
        if (plainSelect.getJoins() != null)
            for (Join join : plainSelect.getJoins()) {
                isCrossJoin = join.isCross();
                join.getRightItem().accept(this);
                if (join.getOnExpression() != null) {
                    parsingOn = true;
                    join.getOnExpression().accept(this);
                    parsingOn = false;
                }
            }
        parsingJoin = false;

        parsingWhere = true;
        if (plainSelect.getWhere() != null)
            plainSelect.getWhere().accept(this);
        parsingWhere = false;

        parsingGroupBy = true;
        if (plainSelect.getGroupBy() != null)
            plainSelect.getGroupBy().accept(this);
        parsingGroupBy = false;

        parsingHaving = true;
        if (plainSelect.getHaving() != null)
            plainSelect.getHaving().accept(this);
        parsingHaving = false;

        parsingOrderBy = true;
        if (plainSelect.getOrderByElements() != null)
            for (OrderByElement orderByElement : plainSelect.getOrderByElements())
                orderByElement.accept(this);
        parsingOrderBy = false;

        if (plainSelect.getOracleHierarchical() != null)
            plainSelect.getOracleHierarchical().accept(this);
    }

    @Override
    public void visit(SetOperationList setOperationList) {
        if (setOperationList.getSelects() != null)
            for (SelectBody selectBody : setOperationList.getSelects())
                selectBody.accept(this);
    }

    @Override
    public void visit(WithItem withItem) {
        withItem.getSelectBody().accept(this);
    }

    @Override
    public void visit(ValuesStatement valuesStatement) {
        if (valuesStatement.getExpressions() != null)
            for (Expression expression : valuesStatement.getExpressions())
                expression.accept(this);
    }

    @Override
    public void visit(DescribeStatement describeStatement) {
        describeStatement.getTable().accept(this);
    }

    @Override
    public void visit(ExplainStatement explainStatement) {
        explainStatement.getStatement().accept(this);
    }

    @Override
    public void visit(ShowStatement showStatement) {

    }

    @Override
    public void visit(DeclareStatement declareStatement) {

    }

    @Override
    public void visit(Grant grant) {

    }

    @Override
    public void visit(CreateSequence createSequence) {

    }

    @Override
    public void visit(AlterSequence alterSequence) {

    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {

    }

    @Override
    public void visit(Table table) {
        tableNames.add(table.getFullyQualifiedName());
        allTableNames.add(table.getFullyQualifiedName());
        if (table.getAlias() != null) {
            tableNames.remove(table.getAlias().getName());
            tableAliases.put(table.getAlias().getName(), table.getFullyQualifiedName());
        }
    }

    @Override
    public void visit(BitwiseRightShift bitwiseRightShift) {
        visitBinaryExpression(bitwiseRightShift);
    }

    @Override
    public void visit(BitwiseLeftShift bitwiseLeftShift) {
        visitBinaryExpression(bitwiseLeftShift);
    }

    @Override
    public void visit(NullValue nullValue) {

    }

    @Override
    public void visit(Function function) {
        parsingFunction = true;
        if (function.getParameters() != null)
            visit(function.getParameters());
        parsingFunction = false;
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        signedExpression.getExpression().accept(this);
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {

    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {

    }

    @Override
    public void visit(DoubleValue doubleValue) {

    }

    @Override
    public void visit(LongValue longValue) {

    }

    @Override
    public void visit(HexValue hexValue) {

    }

    @Override
    public void visit(DateValue dateValue) {

    }

    @Override
    public void visit(TimeValue timeValue) {

    }

    @Override
    public void visit(TimestampValue timestampValue) {

    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue stringValue) {

    }

    @Override
    public void visit(Addition addition) {
        visitBinaryExpression(addition);
    }

    @Override
    public void visit(Division division) {
        visitBinaryExpression(division);
    }

    @Override
    public void visit(IntegerDivision integerDivision) {
        visitBinaryExpression(integerDivision);
    }

    @Override
    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication);
    }

    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction);
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression);
    }

    @Override
    public void visit(Between between) {
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        visitBinaryExpression(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        visitBinaryExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        visitBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression) {
        parsingIn = true;
        if (inExpression.getLeftExpression() != null)
            inExpression.getLeftExpression().accept(this);
        else if (inExpression.getLeftItemsList() != null)
            inExpression.getLeftItemsList().accept(this);
        if (inExpression.getRightItemsList() != null)
            inExpression.getRightItemsList().accept(this);
        parsingIn = false;
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        isNullExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {

    }

    @Override
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression);
    }

    @Override
    public void visit(MinorThan minorThan) {
        visitBinaryExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        visitBinaryExpression(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
    }

    @Override
    public void visit(Column column) {

    }

    @Override
    public void visit(SubSelect subSelect) {
        parsingSubSelect = true;
        if (subSelect.getAlias() != null) {
            tableNames.add(subSelect.getAlias().getName());
            allTableNames.add(subSelect.getAlias().getName());
        }
        if (subSelect.getWithItemsList() != null)
            for (WithItem withItem : subSelect.getWithItemsList())
                withItem.accept(this);
        subSelect.getSelectBody().accept(this);
        parsingSubSelect = false;
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        if (caseExpression.getSwitchExpression() != null)
            caseExpression.getSwitchExpression().accept(this);

        if (caseExpression.getWhenClauses() != null)
            for (WhenClause whenClause : caseExpression.getWhenClauses())
                whenClause.accept(this);

        if (caseExpression.getElseExpression() != null)
            caseExpression.getElseExpression().accept(this);
    }

    @Override
    public void visit(WhenClause whenClause) {
        if (whenClause.getWhenExpression() != null)
            whenClause.getWhenExpression().accept(this);
        if (whenClause.getThenExpression() != null)
            whenClause.getThenExpression().accept(this);
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        parsingExists = true;
        existsExpression.getRightExpression().accept(this);
        parsingExists = false;
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        allComparisonExpression.getSubSelect().getSelectBody().accept(this);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
    }

    @Override
    public void visit(Concat concat) {
        visitBinaryExpression(concat);
    }

    @Override
    public void visit(Matches matches) {
        visitBinaryExpression(matches);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        visitBinaryExpression(bitwiseAnd);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        visitBinaryExpression(bitwiseOr);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        visitBinaryExpression(bitwiseXor);
    }

    @Override
    public void visit(CastExpression castExpression) {
        castExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo);
    }

    @Override
    public void visit(AnalyticExpression analyticExpression) {

    }

    @Override
    public void visit(ExtractExpression extractExpression) {

    }

    @Override
    public void visit(IntervalExpression intervalExpression) {

    }

    @Override
    public void visit(OracleHierarchicalExpression oracleHierarchicalExpression) {
        if (oracleHierarchicalExpression.getStartExpression() != null)
            oracleHierarchicalExpression.getStartExpression().accept(this);
        if (oracleHierarchicalExpression.getConnectExpression() != null)
            oracleHierarchicalExpression.getConnectExpression().accept(this);
    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {
        visitBinaryExpression(regExpMatchOperator);
    }

    @Override
    public void visit(JsonExpression jsonExpression) {

    }

    @Override
    public void visit(JsonOperator jsonOperator) {

    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        visitBinaryExpression(regExpMySQLOperator);
    }

    @Override
    public void visit(UserVariable userVariable) {

    }

    @Override
    public void visit(NumericBind numericBind) {

    }

    @Override
    public void visit(KeepExpression keepExpression) {

    }

    @Override
    public void visit(MySQLGroupConcat mySQLGroupConcat) {

    }

    @Override
    public void visit(ValueListExpression valueListExpression) {
        valueListExpression.getExpressionList().accept(this);
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        if (rowConstructor.getExprList() != null)
            for (Expression expression : rowConstructor.getExprList().getExpressions())
                expression.accept(this);
    }

    @Override
    public void visit(OracleHint oracleHint) {

    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {

    }

    @Override
    public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {

    }

    @Override
    public void visit(NotExpression notExpression) {
        notExpression.getExpression().accept(this);
    }

    @Override
    public void visit(NextValExpression nextValExpression) {

    }

    @Override
    public void visit(CollateExpression collateExpression) {
        collateExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(SimilarToExpression similarToExpression) {
        visitBinaryExpression(similarToExpression);
    }

    @Override
    public void visit(ArrayExpression arrayExpression) {
        arrayExpression.getObjExpression().accept(this);
        arrayExpression.getIndexExpression().accept(this);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        if (expressionList.getExpressions() != null)
            for (Expression expression : expressionList.getExpressions())
                expression.accept(this);
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        if (namedExpressionList.getExpressions() != null)
            for (Expression expression : namedExpressionList.getExpressions())
                expression.accept(this);
    }

    @Override
    public void visit(MultiExpressionList multiExpressionList) {
        if (multiExpressionList.getExprList() != null)
            for (ExpressionList expressionList : multiExpressionList.getExprList())
                expressionList.accept(this);
    }

    @Override
    public void visit(SubJoin subJoin) {
        subJoin.getLeft().accept(this);
        if (subJoin.getJoinList() != null)
            for (Join join : subJoin.getJoinList())
                join.getRightItem().accept(this);
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        lateralSubSelect.getSubSelect().getSelectBody().accept(this);
    }

    @Override
    public void visit(ValuesList valuesList) {

    }

    @Override
    public void visit(TableFunction tableFunction) {

    }

    @Override
    public void visit(ParenthesisFromItem parenthesisFromItem) {
        parenthesisFromItem.getFromItem().accept(this);
    }

    @Override
    public void visit(AllColumns allColumns) {

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        if (allTableColumns.getTable() != null)
            visit(allTableColumns.getTable());
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);
    }

    @Override
    public void visit(GroupByElement groupByElement) {
        if (groupByElement.getGroupByExpressions() != null)
            for (Expression expression : groupByElement.getGroupByExpressions())
                expression.accept(this);
    }

    @Override
    public void visit(OrderByElement orderByElement) {
        orderByElement.getExpression().accept(this);
    }
}
