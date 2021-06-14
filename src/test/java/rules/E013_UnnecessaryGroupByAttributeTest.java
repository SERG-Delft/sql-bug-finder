package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class E013_UnnecessaryGroupByAttributeTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E013_UnnecessaryGroupByAttribute rule = new E013_UnnecessaryGroupByAttribute();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testUnnecessaryGroupByAttributeSelectWithAliasNoWarnings() throws JSQLParserException {
        query = "select distinct(concat(a,b)) as cc from my_table group by (cc);";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByAttributeUnusedSimpleTerm() throws JSQLParserException {
        query = "select employeeid, sum(amount) from sales group by employee having sum(amount) > 20000;";
        results.add("employee");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByAttributeMultipleQueries() throws JSQLParserException {
        query = "select productname, sum(saleamount) as totalsales from sales where dbo.gettotalsalesbyproduct(productname)  > 1000 group by product;";
        results.add("product");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "select client_id, name, portfolio_id, cash + stocks from client join portfolio using (client_id) join (select client_id, max(cash + stocks) as maxtotal from portfolio group by client_id) as maxima where cash + stocks = maxtotal group by client_id, cash + stocks;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "select accountid from account a left join balance openingbal left join balance closingbal left join balancetoken openingbalanceamounts left join balancetoken closingbalanceamounts group by accountid, accountbalancedate;";
        results.add("accountbalancedate");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByAttributeAllTableColumnsSelectorNoWarnings() throws JSQLParserException {
        query = "select p.*, q.id from posts p, questions q group by p.category_id, q.id;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByAttributeAllTableColumnsSelectorWarnings() throws JSQLParserException {
        query = "select p.group_id, q.id from posts p, questions q group by p.category_id, q.id;";
        results.add("posts.category_id");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByAttributeWithFunctionNoWarnings() throws JSQLParserException {
        query = "select iif(isdate(processed_timestamp) = 0, 'null', 'non null'), count(*) from mytable group by isdate(processed_timestamp);";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select date_format(`date`, '%y-%m') as date, if(`productname` = 'santa', sum(`quantity`), 'none') as santa, if(`productname` = 'toy', sum(`quantity`), 'none') as toy, if(`productname` = 'tree', sum(`quantity`), 'none') as tree from `products` group by date_format(`date`, '%y-%m'),`productname`;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryGroupByAttributeSelectAllColumnsNoWarnings() throws JSQLParserException {
        query = "select * from posts group by category_id;";
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