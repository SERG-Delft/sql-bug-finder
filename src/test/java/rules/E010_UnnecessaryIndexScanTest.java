package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class E010_UnnecessaryIndexScanTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E010_UnnecessaryIndexScan rule = new E010_UnnecessaryIndexScan();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testUnnecessaryIndexUsingLike() throws JSQLParserException {
        query = "SELECT LocationID FROM Locations WHERE Specialities LIKE '%pples';";
        results.add("%pples");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "SELECT customer_id, first_name, last_name FROM sales.customers WHERE last_name LIKE '%er' ORDER BY first_name;";
        results.add("%er");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "SELECT sales.staff.id FROM sales.staff WHERE sales.staff.last_name LIKE '%123' ORDER BY sales.staff.first_name;";
        results.add("%123");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryIndexScanUsingNotLike() throws JSQLParserException {
        query = "select column from table where column not like '%apple%';";
        results.add("%apple%");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings() throws JSQLParserException {
        query = "SELECT LocationID FROM Locations WHERE Specialities LIKE 'a%ples';";
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