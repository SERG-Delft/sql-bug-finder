package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class E009_UnnecessarySelectListInExistsTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E009_UnnecessarySelectListInExists rule = new E009_UnnecessarySelectListInExists();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testUnnecessarySelectListInExistsColumns() throws JSQLParserException {
        query = "SELECT * FROM sales.orders o WHERE EXISTS (SELECT customer_id, name FROM sales.customers c WHERE o.customer_id = c.customer_id AND city = 'San Jose') ORDER BY o.customer_id, order_date;";
        results.add("name, customer_id");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessarySelectListInExistsAllSelector() throws JSQLParserException {
        query = "SELECT * FROM sales.orders o WHERE EXISTS (SELECT *, customer_id FROM sales.customers c WHERE o.customer_id = c.customer_id AND city = 'San Jose') ORDER BY o.customer_id, order_date;";
        results.add("*, customer_id");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessarySelectListInExistsOneSelector() throws JSQLParserException {
        query = "SELECT * FROM sales.orders o WHERE EXISTS (SELECT 1, customer_id FROM sales.customers c WHERE o.customer_id = c.customer_id AND city = 'San Jose') ORDER BY o.customer_id, order_date;";
        results.add("1, customer_id");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessarySelectListInExistsAllSelectors() throws JSQLParserException {
        query = "SELECT * FROM sales.orders o WHERE EXISTS (SELECT *, 1, customer_id FROM sales.customers c WHERE o.customer_id = c.customer_id AND city = 'San Jose') ORDER BY o.customer_id, order_date;";
        results.add("1, *, customer_id");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessarySelectListInExistsAllAndOneSelectors() throws JSQLParserException {
        query = "SELECT * FROM sales.orders o WHERE EXISTS (SELECT *, 1 FROM sales.customers c WHERE o.customer_id = c.customer_id AND city = 'San Jose') ORDER BY o.customer_id, order_date;";
        results.add("*, 1");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings() throws JSQLParserException {
        query = "SELECT * FROM sales.orders o WHERE EXISTS (SELECT customer_id FROM sales.customers c WHERE o.customer_id = c.customer_id AND city = 'San Jose') ORDER BY o.customer_id, order_date;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select distinct vendorid,orders,orders1 from (select vendorid, emp1, sa,emp2,sa1 from #pvt1 ) p;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select r.customerid, r.orderid from  dbo.tblrecords r where not exists ( select personid from dbo.tblpeople p where p.userid= r.customerid and exists ( select 1 from dbo.tblpeople i where i.personid = p.personid and i.userid <> p.userid ) );";
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