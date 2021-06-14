package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class E019_MissingJoinPredicateTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E019_MissingJoinPredicate rule = new E019_MissingJoinPredicate();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testMissingJoinPredicateForDeleteWithoutErrors() throws JSQLParserException {
        query = "delete k from yourtable1 k inner join yourtable2 t on t.id = k.id;";
        rule.currentQuery = query;
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testMissingJoinPredicateForDeleteWithErrors() throws JSQLParserException {
        query = "delete k from yourtable1 k inner join yourtable2 t;";
        rule.currentQuery = query;
        results.add("yourtable2");
        results.add("yourtable1");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testMissingJoinPredicateCrossJoin() throws JSQLParserException {
        query = "SELECT t1.*, t2.* FROM table_1 t1 CROSS JOIN Table_2 t2;";
        rule.currentQuery = query;
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testMissingJoinPredicateMultipleQueries1() throws JSQLParserException {
        query = "SELECT * FROM dbo.Orders INNER JOIN dbo.OrderDetails AS od ON 1 = 2 WHERE od.OrderId = '00012345';";
        rule.currentQuery = query;
        results.add("dbo.Orders");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "SELECT * FROM HumanResources.Employee AS e, Person.Person AS p, Person.EmailAddress AS ea WHERE p.BusinessEntityID = e.BusinessEntityID;";
        rule.currentQuery = query;
        results.add("Person.EmailAddress");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "SELECT * FROM table1, table2 WHERE 1 = 2;";
        rule.currentQuery = query;
        results.add("table2");
        results.add("table1");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
    }

    @Test
    public void testMissingJoinPredicateMultipleQueries2() throws JSQLParserException {
        query = "select orders.orderid, orders.customername from orders inner join orderdetails;";
        rule.currentQuery = query;
        results.add("orderdetails");
        results.add("orders");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "select u.thumbnail, u.id from users u inner join contestants c where u.thumbnail is not null limit 1;";
        rule.currentQuery = query;
        results.add("contestants");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "select user.thumbnail, user.id from users user inner join contestants cont on cont.id = cont.users_id where cont.thumbnail is not null;";
        rule.currentQuery = query;
        results.add("users");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
    }

    @Test
    public void testMissingJoinPredicateNoWhereOrJoinClause() throws JSQLParserException {
        query = "select * from (select table1.category from dbo.table1 union select table2.category from dbo.table2) as a;";
        rule.currentQuery = query;
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select max(y.num) from (select count(*) as num from table x) y;";
        rule.currentQuery = query;
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "SELECT t1.*, t2.* FROM Table_1 t1, Table_2 t2;";
        rule.currentQuery = query;
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testMissingJoinPredicateNoWarningForInsertTable() throws JSQLParserException {
        query = "insert into test select tc.first_name, tc.last_name, tc.age, tc.dob, o.occupation_id from test_csv tc join occupation o on (tc.occupation_name=o.occupation_name);";
        rule.currentQuery = query;
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "insert into letter_search select w.word_id from words as w join num;";
        rule.currentQuery = query;
        results.add("num");
        results.add("words");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testMissingJoinPredicateNoWarningForInClauseWithTwoTables() throws JSQLParserException {
        query = "select * from names where name not in (select name from excludes);";
        rule.currentQuery = query;
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testMissingJoinPredicateNoWarningBetweenClause() throws JSQLParserException {
        query = "select pers_key, pers_name from visit_info a join valid_dates b where a.visit_date between b.start_date and b.end_date group by pers_key, pers_name;";
        rule.currentQuery = query;
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select ip.ip, ranges.id from ip join ranges on ip.ip between ranges.low and ranges.high;";
        rule.currentQuery = query;
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testMissingJoinPredicateNoWarningIsNullClause() throws JSQLParserException {
        query = "select pers_key, pers_name from visit_info a join valid_dates b where a.visit_date IS NOT NULL AND b.start_date IS NULL group by pers_key, pers_name;";
        rule.currentQuery = query;
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testMissingJoinPredicateMultipleSubSelectsNoWarnings() throws JSQLParserException {
        query = "select (select distinct org_name from table_1 where column1= 1) as terminal_1, (select org_name from table_5 where column5= 1) as cfs_1, (select distinct org_name from table_4 where column7= 136 ) as terminals_2, (select org_name from table_6 where column1=7) as cfs_2;";
        rule.currentQuery = query;
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testMissingJoinPredicateSubSelectAlias() throws JSQLParserException {
        query = "select * from category c join ( select distinct pt.category_id from part pt ) pqparts;";
        rule.currentQuery = query;
        results.add("pqparts");
        results.add("category");
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