package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class E014_UnnecessaryGroupByClauseTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E014_UnnecessaryGroupByClause rule = new E014_UnnecessaryGroupByClause();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testUnnecessaryGroupByClauseSimpleCondition() throws JSQLParserException {
        query = "SELECT city, state FROM sales.customers GROUP BY city, state;";
        results.add("select city, state ... group by city, state");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByClauseWithFunctionInSelect() throws JSQLParserException {
        query = "SELECT city, MAX(state) FROM sales.customers GROUP BY city, state HAVING city = 'Albany' and state = 'NY';";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByClauseWithFunctionInWhere() throws JSQLParserException {
        query = "SELECT city, state FROM sales.customers WHERE MAX(state)>0 GROUP BY city, state";
        pm.parse(new StringReader(query)).accept(rule);
        results.add("select city, state ... group by city, state");
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByClauseInOuterClause() throws JSQLParserException {
        query = "select animal from pets group by animal having animal not in ( select animal from pets where name not in ('homer','bart','marge','lisa','maggie') group by animal );";
        pm.parse(new StringReader(query)).accept(rule);
        results.add("select animal ... group by animal");
        results.add("select animal ... group by animal");
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByClauseInSubSelectClause() throws JSQLParserException {
        query = "select * from table_name where primarykey in ( select primarykey from table_name group by primarykey having primarykey > 1 ) order by primarykey;";
        pm.parse(new StringReader(query)).accept(rule);
        results.add("select primarykey ... group by primarykey");
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByClauseCaseWithColumnAlias() throws JSQLParserException {
        query = "select ins1 as insurance from insauth2 where ins1 is not null group by ins1 union select ins2 as insurance from insauth2 where ins2 is not null group by ins2 union select ins3 as insurance from insauth2 where ins3 is not null;";
        pm.parse(new StringReader(query)).accept(rule);
        results.add("select ins1 ... group by ins1");
        results.add("select ins2 ... group by ins2");
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings() throws JSQLParserException {
        query = "SELECT city FROM sales.customers WHERE state = 'NY' GROUP BY city, state;";
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