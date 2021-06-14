package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class E018_InefficientHavingAttributeTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E018_InefficientHavingAttribute rule = new E018_InefficientHavingAttribute();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testInefficientHavingAttributeSimple() throws JSQLParserException {
        query = "SELECT city, state FROM sales.customers GROUP BY city, state HAVING city = 'Albany' and state = 'NY';";
        results.add("city");
        results.add("state");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "SELECT string_agg(title, ', ') as titles, genre, count(*) FROM films GROUP BY genre, title HAVING genre='SciFi';";
        results.add("genre");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings() throws JSQLParserException {
        query = "select name, count(email) from users group by email having count(email) > 1;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select id, count(id) from table1 group by id having count(id)>1;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select description from table_name group by description having max(len(description)) > 27;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testInefficientHavingAttributeNoWarningForSelectAliasInHaving() throws JSQLParserException {
        query = "select  c.cname, count(distinct o.pid) as `uniques` from o join c group by c.id having `uniques` > 10;";
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