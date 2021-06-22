package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class E007_UnnecessaryGeneralComparisonTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E007_UnnecessaryGeneralComparison rule = new E007_UnnecessaryGeneralComparison();

    @Before
    public void setUp()  {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testUnnecessaryGeneralComparisonGreaterThanEqualsMax() throws JSQLParserException {
        query = "SELECT name, salary FROM employee WHERE salary >= (SELECT MAX(salary) FROM employee);";
        results.add("salary >= (SELECT MAX(salary) FROM employee)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGeneralComparisonMinorThanEqualsMin() throws JSQLParserException {
        query = "SELECT name, salary FROM employee WHERE salary <= (SELECT MIN(salary) FROM employee);";
        results.add("salary <= (SELECT MIN(salary) FROM employee)");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarningsGreaterThan() throws JSQLParserException {
        query = "SELECT name, salary FROM employee WHERE salary = (SELECT MIN(salary) FROM employee);";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarningsMinorThan() throws JSQLParserException {
        query = "SELECT name, salary FROM employee WHERE salary = (SELECT MIN(salary) FROM employee);";
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

    @Test
    public void testQueryWithDifferentTableAliases() throws JSQLParserException {
        query = "SELECT name FROM worldus AS w WHERE w.gdp >= (SELECT max(gdp) FROM world WHERE continent = 'Europe');";
        results.clear();
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "SELECT name FROM world AS w WHERE w.gdp >= (SELECT max(gdp) FROM world WHERE continent = 'Europe');";
        results.clear();
        results.add("w.gdp >= (SELECT max(gdp) FROM world WHERE continent = 'Europe')");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "SELECT name FROM world AS w WHERE w.gdp >= (SELECT max(z.gdp) FROM world z WHERE continent = 'Europe');";
        results.clear();
        results.add("w.gdp >= (SELECT max(z.gdp) FROM world z WHERE continent = 'Europe')");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }
}