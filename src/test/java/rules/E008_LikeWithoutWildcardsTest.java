package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class E008_LikeWithoutWildcardsTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E008_LikeWithoutWildcards rule = new E008_LikeWithoutWildcards();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testLikeWithoutWildcardsChar() throws JSQLParserException {
        query = "SELECT customer_id, first_name, last_name FROM sales.customers WHERE last_name LIKE 'z' ORDER BY first_name;";
        results.add("last_name LIKE 'z'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testLikeWithoutWildcardsString1() throws JSQLParserException {
        query = "SELECT customer_id, first_name, last_name FROM sales.customers WHERE last_name LIKE 'YZ' ORDER BY last_name;";
        results.add("last_name LIKE 'YZ'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testLikeWithoutWildcardsString2() throws JSQLParserException {
        query = "SELECT LocationID FROM Locations WHERE Specialities LIKE 'App';";
        results.add("Specialities LIKE 'App'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testLikeWithoutWildcardsEscapeWildcard() throws JSQLParserException {
        query = "SELECT feedback_id, comment FROM sales.feedbacks WHERE comment LIKE '30!%' ESCAPE '!';";
        results.add("comment LIKE '30!%' ESCAPE '!'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testLikeWithoutWildcardsEscapeNoWildcards() throws JSQLParserException {
        query = "SELECT feedback_id, comment FROM sales.feedbacks WHERE comment LIKE '30!' ESCAPE '?';";
        results.add("comment LIKE '30!' ESCAPE '?'");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings1() throws JSQLParserException {
        query = "SELECT customer_id, first_name, last_name FROM sales.customers WHERE last_name LIKE 'z%' ORDER BY first_name;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings2() throws JSQLParserException {
        query = "SELECT customer_id, first_name, last_name FROM sales.customers WHERE last_name LIKE '%er' ORDER BY first_name;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings3() throws JSQLParserException {
        query = "SELECT customer_id, first_name, last_name FROM sales.customers WHERE last_name LIKE 't%s' ORDER BY first_name;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings4() throws JSQLParserException {
        query = "SELECT customer_id, first_name, last_name FROM sales.customers WHERE last_name LIKE '_u%' ORDER BY first_name;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings5() throws JSQLParserException {
        query = "SELECT customer_id, first_name, last_name FROM sales.customers WHERE last_name LIKE '[YZ]%' ORDER BY last_name;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings6() throws JSQLParserException {
        query = "SELECT customer_id, first_name, last_name FROM sales.customers WHERE last_name LIKE '[A-C]%' ORDER BY first_name;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings7() throws JSQLParserException {
        query = "SELECT customer_id, first_name, last_name FROM sales.customers WHERE last_name LIKE '[^A-X]%' ORDER BY last_name;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings8() throws JSQLParserException {
        query = "SELECT customer_id, first_name, last_name FROM sales.customers WHERE first_name NOT LIKE 'A%' ORDER BY first_name;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings9() throws JSQLParserException {
        query = "SELECT feedback_id, comment FROM sales.feedbacks WHERE comment LIKE '%30!%%' ESCAPE '!';";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings10() throws JSQLParserException {
        query = "SELECT feedback_id, comment FROM sales.feedbacks WHERE comment LIKE '30!%' ESCAPE '?';";
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