package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class E003_DuplicateOutputColumnTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E003_DuplicateOutputColumn rule = new E003_DuplicateOutputColumn();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testDuplicateOutputColumnInSelectClause() throws JSQLParserException {
        query = "SELECT number, name, job, number FROM employees WHERE job='manager';";
        results.add("number");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumnAsTupleVariable() throws JSQLParserException {
        query = "SELECT number AS C1, name, job, C1 FROM employees WHERE job='manager';";
        results.add("C1");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumnWhenUsingAllSelector() throws JSQLParserException {
        query = "SELECT *, name FROM movies WHERE name='avengers';";
        results.add("name");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumnVisitColumnCondition() throws JSQLParserException {
        query = "select * from a inner join b on a.a = b.b;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select * from a left outer join b on a.a = b.b;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select emailaddress, customername from customers as a inner join customers as b on a.customername <> b.customername and a.emailaddress = b.emailaddress;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumnAliasSelectExpression() throws JSQLParserException {
        query = "select name, genre, title, minutes, minutes/60 as hours from movies;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumnInExpression() throws JSQLParserException {
        query = "select * from sometable where tableid in(select listvalue from dbo.fn_listtotable(',',@ids) s);";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumnSubSelectExpression() throws JSQLParserException {
        query = "select * from ( (select city, length(city) as maxlen from station order by maxlen desc limit 1) union (select city, length(city) as minlen from station order by minlen,city limit 1))a;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumnSkipUnionQuery() throws JSQLParserException {
        query = "select id, surname as name from users where surname is not null union all select id, firstname as name from users where surname is null;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumnWithAllSelectorAndFunctions() throws JSQLParserException {
        query = "select *, count(messages.id) as nums from dialogs join messages on dialogs.id=messages.dialog_id where messages.unread = 1 group by dialogs.id having nums <> 0;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select *, count(messages.id) as nums, count(messages.id) as nums1 from dialogs join messages on dialogs.id=messages.dialog_id where messages.unread = 1 group by dialogs.id having nums <> 0;";
        results.add("count(messages.id)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumnSkipCase() throws JSQLParserException {
        query = "select *, case when password = 'asdf' then 1 else 0 end as ispasswordmatch, case when expiration >= now() then 1 else 0 end as isactiveaccount from users where username = 'user';";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumnSkipAnalyticExpression() throws JSQLParserException {
        query = "select top 1000000 *, row_number() over (order by (select null)) from tblejtransactions;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumn() throws JSQLParserException {
        query = "select top 1000000 *, row_number() over (order by (select null)) from tblejtransactions;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings() throws JSQLParserException {
        query = "SELECT * FROM movies WHERE name='avengers';";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarningsWithFunctions() throws JSQLParserException {
        query = "SELECT AVG(DISTINCT list_price), SUM(DISTINCT list_price) FROM production.product;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testDuplicateOutputColumnAsFunction() throws JSQLParserException {
        query = "SELECT AVG(DISTINCT list_price), AVG(DISTINCT list_price) FROM production.product;";
        results.add("AVG(DISTINCT list_price)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testEmptyQueryNoWarnings() throws JSQLParserException {
        query = "SELECT *;";
        results.clear();
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testCorrectComplexQueriesNoWarnings() throws JSQLParserException {
        query = "select `account` from `tabParty Account` where `parenttype` = 'Customer' and `company` = '_Test Company' and `parent` = '_Test Customer USD' order by modified desc;";
        results.clear();
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select `variant_of` from `tabItem` where `name` = 'Test Item for Merging 1' order by modified desc;";
        results.clear();
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select `name` from `tabDesktop Icon` where `owner` = 'test@example.com' and `module_name` = 'Fees' and `standard` = 0 order by modified desc;";
        results.clear();
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select `variant_of` from `tabItem` where `name` = 'Test Item for Merging 1' order by modified desc;";
        results.clear();
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }
}
