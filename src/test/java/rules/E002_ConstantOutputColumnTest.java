package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class E002_ConstantOutputColumnTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E002_ConstantOutputColumn rule = new E002_ConstantOutputColumn();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testSingleConstantOutputColumnWithoutAllSelector() throws JSQLParserException {
        query = "SELECT number, name, job FROM employees WHERE job='manager';";
        results.add("job = 'manager'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testMultipleConstantOutputColumnWithoutAllSelector() throws JSQLParserException {
        query = "SELECT number AS C1, name AS C2, job AS C3 FROM employees WHERE C3='manager' AND name='John';";
        results.add("C3 = 'manager'");
        results.add("name = 'John'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testSingleConstantOutputColumnComplexQuery() throws JSQLParserException {
        query = "WITH employee AS (SELECT * FROM employees WHERE city = 'New York') SELECT * FROM employee WHERE ID<20 UNION ALL SELECT * FROM employee WHERE gender='M';";
        results.add("city = 'New York'");
        results.add("gender = 'M'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testSingleConstantOutputColumnTupleVariableWithAllSelector() throws JSQLParserException {
        query = "SELECT * FROM sales.customers C1, sales.customers C2 WHERE C1.customer_id = C2.customer_id AND C1.city='New York';";
        results.add("C1.city = 'New York'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarningsWithoutAllSelector() throws JSQLParserException {
        query = "SELECT number, name FROM employees WHERE job='manager';";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarningsWithAllSelector() throws JSQLParserException {
        query = "SELECT * FROM sales.orders o WHERE EXISTS (SELECT customer_id FROM sales.customers c WHERE o.customer_id = c.customer_id AND city = 'San Jose') ORDER BY o.customer_id, order_date;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testConstantOutputColumnFunctionHandlingNoWarnings() throws JSQLParserException {
        query = "select count(distinct cola) as total_groups, count(case when cola = 'a' then 1 end) as a, count(case when cola = 'b' then 1 end) as b, count(case when cola = 'c' then 1 end) as c, count(case when cola = 'd' then 1 end) as d from test;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testConstantOutputColumnOrExpressionNoWarnings() throws JSQLParserException {
        query = "select * from person where number = 123 or name = 'robert';";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testConstantOutputColumnCaseExpressionSkipped() throws JSQLParserException {
        query = "select * from example order by case when name = 'i want this first' then 0 else 99   end asc;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testConstantOutputColumnJoinNoWarnings() throws JSQLParserException {
        query = "select * from a a left join b b on a.id=b.id and b.id=2;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testConstantOutputColumnInJoin() throws JSQLParserException {
        query = "select * from a a left join b b on a.id=b.id and b.id=2 and a.key='value';";
        results.add("a.key = 'value'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testConstantOutputColumnTableStar() throws JSQLParserException {
        query = "select * from orc_users as  t1 inner join orc_files as t2 on t1.id = t2.userid where t1.email='sdfsdf';";
        results.add("t1.email = 'sdfsdf'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "select t1.* from orc_users as  t1 inner join orc_files as t2 on t1.id = t2.userid where t1.email='sdfsdf';";
        results.add("t1.email = 'sdfsdf'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "select * from orc_users as  t1 inner join orc_files as t2 on t1.id = t2.userid where t2.email='sdfsdf';";
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
