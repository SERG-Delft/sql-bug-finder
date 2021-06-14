package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class E012_UnnecessaryCountArgumentTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E012_UnnecessaryCountArgument rule = new E012_UnnecessaryCountArgument();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testUnnecessaryCountArgumentInSimpleQuery1() throws JSQLParserException {
        query = "select id, count(id) from table1 group by id having count(id)>1;";
        results.add("count(id)");
        results.add("count(id)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryCountArgumentInSimpleQuery2() throws JSQLParserException {
        query = "select count(student_id) as 'studentcount' from coursesemone where student_id=3 having count(student_id) < 6 and count(student_id) > 0;";
        results.add("count(student_id)");
        results.add("count(student_id)");
        results.add("count(student_id)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryCountArgumentInComplexQuery() throws JSQLParserException {
        query = "select yr, count(title) from actor join casting on actor.id=actorid join movie on movie.id=movieid where name = 'john travolta' group by yr having count(title)=(select top 1 count(title) from casting join movie on movieid=movie.id join actor on actor.id=actorid where name='john travolta' group by yr order by count(title) desc);";
        results.add("count(title)");
        results.add("count(title)");
        results.add("count(title)");
        results.add("count(title)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryCountArgumentMultipleQueries() throws JSQLParserException {
        query = "SELECT COUNT(*) FROM sales.customers;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "SELECT COUNT(DISTINCT first_name) FROM sales.customers;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "SELECT COUNT(first_name) FROM sales.customers;";
        results.add("count(first_name)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryCountArgumentInCountBigFunction() throws JSQLParserException {
        query = "SELECT COUNT_BIG(supplier), COUNT(*) FROM orders WHERE order_num = '10101234' HAVING COUNT(DISTINCT supplier) > 100;";
        results.add("count_big(supplier)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarningsInCountBigFunction() throws JSQLParserException {
        query = "SELECT COUNT_BIG(DISTINCT supplier), COUNT(*) FROM orders WHERE order_num = '10101234' HAVING COUNT(DISTINCT supplier) > 100;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings() throws JSQLParserException {
        query = "SELECT COUNT(DISTINCT supplier), COUNT(*) FROM orders WHERE order_num = '10101234';";
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