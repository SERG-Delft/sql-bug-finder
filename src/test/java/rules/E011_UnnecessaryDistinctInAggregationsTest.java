package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class E011_UnnecessaryDistinctInAggregationsTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E011_UnnecessaryDistinctInAggregations rule = new E011_UnnecessaryDistinctInAggregations();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testUnnecessaryDistinctInAggregationsCountFunction() throws JSQLParserException {
        query = "SELECT AVG(DISTINCT supplier), COUNT(*) FROM orders WHERE order_num = '10101234';";
        results.add("AVG(DISTINCT supplier)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryDistinctInAggregationsMultipleFunctions1() throws JSQLParserException {
        query = "SELECT MIN(DISTINCT supplier), COUNT(*) FROM orders WHERE order_num = '10101234' HAVING COUNT(DISTINCT supplier) > 100;";
        results.add("MIN(DISTINCT supplier)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryDistinctInAggregationsMultipleFunctions2() throws JSQLParserException {
        query = "SELECT AVG(DISTINCT list_price), SUM(DISTINCT list_price) FROM production.product;";
        results.add("AVG(DISTINCT list_price)");
        results.add("SUM(DISTINCT list_price)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryDistinctInAggregationsDuplicateFunctions1() throws JSQLParserException {
        query = "SELECT STDEVP(bonus), SUM(DISTINCT bonus) FROM sales.sales_person;";
        results.add("SUM(DISTINCT bonus)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings() throws JSQLParserException {
        query = "SELECT COUNT(supplier), COUNT(*) FROM orders WHERE order_num = '10101234';";
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