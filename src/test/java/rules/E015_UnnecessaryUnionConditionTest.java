package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class E015_UnnecessaryUnionConditionTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E015_UnnecessaryUnionCondition rule = new E015_UnnecessaryUnionCondition();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testUnnecessaryUnionConditionTwoSimpleClauses() throws JSQLParserException {
        query = "select * from citizen c where c.name = 'smith' union select * from citizen c where p.area = 'moon';";
        results.add("replace UNION/UNION ALL with WHERE clause connected by OR");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryUnionConditionMultipleQueriesWithWarnings() throws JSQLParserException {
        query = "select * from mytable where a=x union all select * from mytable where b=y and a!=x;";
        results.add("replace UNION/UNION ALL with WHERE clause connected by OR");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "select top 10 * from table where section=1 union select top 10 * from table where section=2 union select top 10 * from table where section=3;";
        results.add("replace UNION/UNION ALL with WHERE clause connected by OR");
        results.add("replace UNION/UNION ALL with WHERE clause connected by OR");
        results.add("replace UNION/UNION ALL with WHERE clause connected by OR");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "select * from emps where id < 20 union all select * from emps where sex = 'f';";
        results.add("replace UNION/UNION ALL with WHERE clause connected by OR");
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