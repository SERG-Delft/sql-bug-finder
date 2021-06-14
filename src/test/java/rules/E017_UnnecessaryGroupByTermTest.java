package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class E017_UnnecessaryGroupByTermTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E017_UnnecessaryGroupByTerm rule = new E017_UnnecessaryGroupByTerm();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testUnnecessaryGroupByTermTestSimilarTuplesNoWarnings1() throws JSQLParserException {
        query = "select anc.* from node me,node anc where me.tw=22 and anc.nc >= me.tw and anc.tw <= me.tw group by anc.tw;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByTermTestSimilarTuplesNoWarnings2() throws JSQLParserException {
        query = "select des.* from node me,node des where me.tw=17 and des.tw < me.nc and des.tw >= me.tw group by des.tw;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByTermSimpleClause1() throws JSQLParserException {
        query = "select column1 from tablename where column1 = 'bar' group by column1;";
        results.add("column1");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByTermSimpleClause2() throws JSQLParserException {
        query = "SELECT city, state, zip_code FROM sales.customers WHERE state = 'NY' GROUP BY city, state, zip_code;";
        results.add("state");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByTermSameAttributeReportedOnlyOnce() throws JSQLParserException {
        query = "select * from (your_table_name) where flag = 'volunteer' or flag = 'uploaded' group by contactid, flag;";
        results.add("flag");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByTermMultipleQueries() throws JSQLParserException {
        query = "select num, count(1) as count from tbl where num = 1 group by num;";
        results.add("num");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "select count(id) from stats where record_date.year = 2009 group by record_date.year;";
        results.add("year");
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