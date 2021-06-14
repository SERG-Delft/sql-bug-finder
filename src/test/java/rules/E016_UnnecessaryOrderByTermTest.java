package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class E016_UnnecessaryOrderByTermTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E016_UnnecessaryOrderByTerm rule = new E016_UnnecessaryOrderByTerm();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testUnnecessaryOrderByTermTestSimilarTuplesNoWarnings1() throws JSQLParserException {
        query = "select anc.* from node me,node anc where me.tw=22 and anc.nc >= me.tw and anc.tw <= me.tw order by anc.tw;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select   salary from     table_name where    name='inputfromphp' and    (surname='inputfromphp' or country='inputfromphp') order by case surname when 'inputfromphp' then 0 else 1 end, case country when 'inputfromphp' then 0 else 1 end limit    1;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select id,title,release_date from tbl_movies where release_date > '2014-02-20' or release_date='' order by release_date asc;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryOrderByTermTestSimilarTuplesNoWarnings2() throws JSQLParserException {
        query = "select des.* from node me,node des where me.tw=17 and des.tw < me.nc and des.tw >= me.tw order by des.tw;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryOrderByTermSimpleClause1() throws JSQLParserException {
        query = "select column1 from tablename where column1 = 'bar' order by column1;";
        results.add("column1");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryOrderByTermSimpleClause2() throws JSQLParserException {
        query = "SELECT city, state, zip_code FROM sales.customers WHERE state = 'NY' ORDER BY city, state, zip_code;";
        results.add("state");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryOrderByTermSameAttributeReportedOnlyOnce() throws JSQLParserException {
        query = "select * from (your_table_name) where flag = 'volunteer' or flag = 'uploaded' order by contactid, flag asc;";
        results.add("flag");
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