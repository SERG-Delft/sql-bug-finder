package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class E001_InconsistentTupleVariableTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E001_InconsistentTupleVariable rule = new E001_InconsistentTupleVariable();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testInconsistentTupleVariableInWhereClauseStrings() throws JSQLParserException {
        query = "SELECT * FROM employees X, employees Y WHERE X.employee_id = Y.employee_id AND X.job = 'manager' AND Y.job = 'president';";
        results.add("employees.job");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testInconsistentTupleVariableInWhereClauseLongValues() throws JSQLParserException {
        query = "SELECT * FROM employees X, employees Y WHERE X.employee_id = Y.employee_id AND X.salary = 1000 AND Y.salary = 2000;";
        results.add("employees.salary");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testInconsistentTupleVariableInSubQuerySameType() throws JSQLParserException {
        query = "SELECT * FROM sales.orders o WHERE o.id IN (SELECT id FROM sales.orders c WHERE o.id = c.id AND o.id = 123 AND c.id = 456) ORDER BY o.customer_id, order_date;";
        results.add("sales.orders.id");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testInconsistentTupleVariableInExistsSubQuerySameType() throws JSQLParserException {
        query = "SELECT * FROM sales.orders o WHERE EXISTS (SELECT id FROM sales.orders c WHERE o.id = c.id AND o.id = 123 AND c.id = 456) ORDER BY o.customer_id, order_date;";
        results.add("sales.orders.id");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testInconsistentTupleVariableInExistsSubQueryDifferentType() throws JSQLParserException {
        query = "SELECT * FROM sales.orders o WHERE EXISTS (SELECT id FROM sales.orders c WHERE o.id = c.id AND o.id = 'Mango' AND c.id = 123) ORDER BY o.customer_id, order_date;";
        results.add("sales.orders.id");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testInconsistentTupleVariableNoWarningsWhenSameTupleDetectedInDifferentSubQueries() throws JSQLParserException {
        query = "select * from accounts_details where account_id=1001 union select * from accounts_details where account_id=1002;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testInconsistentTupleVariableNoWarningsWhenAndIsNotDetectedInQuery() throws JSQLParserException {
        query = "select username from users where company = 'bbc' or company = 'itv';";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select sum(case when position = 'manager' then 1 else 0 end) as managercount, sum(case when position = 'ceo' then 1 else 0 end) as ceocount from sometable;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select count(*) as cnt, `regions_id` from regionables where `regionable_id` = '115' or `regionable_id` = '714' group by `regions_id` having cnt > 1;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings() throws JSQLParserException {
        query = "SELECT * FROM sales.orders o WHERE EXISTS (SELECT id FROM sales.customers c WHERE o.id = c.id AND o.city = 'San Jose' AND c.city = 'New York') ORDER BY o.customer_id, order_date;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select t.email , t.storeid from transactiondata t where exists (select 1 from subscribers where t.email = email) and not exists (select 1 from transactiondata where t.email = email and storeid <> 50001) group by t.email , t.storeid;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "delete from table a where rowid<(select max(rowid) from table b where a.id = b.id and a.id = 122);";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select * from tmp t0 where exists ( select * from tmp t1 join tmp t2 on t2.clientid = t1.clientid and t2.serverid = t1.serverid and t2.logtime > t1.logtime where t1. status = 'aborted' and t2. status = 'failed' and t1.clientid = t0.clientid and t1.serverid = t0.serverid and t1.logtime = t0.logtime or t2.logtime = t0.logtime and not exists ( select * from tmp x where x.clientid = t1.clientid and x.serverid = t1.serverid and x.logtime > t1.logtime and x.logtime < t2.logtime ) );";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select * from cats outside where not exists(select * from cats cat where exists( select dog.foo,dog.bar from dogs dog where cat.foo = dog.foo and cat.bar = dog.bar) and outside.foo = cat.foo and outside.bar=cat.bar );";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testInconsistentTupleVariableWrongQuery() throws JSQLParserException {
        query = "select * from sys.types where (system_type_id <> user_type_id) and is_user_defined = 0.;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testInconsistentTupleVariableNoTableKeyExactMatch() throws JSQLParserException {
        query = "select p1.domain_id, p2.domain_id, count(p1.domain_id) as d1, count(p2.domain_id) as d2 from pdb as p1, interacting_pdbs as i1, pdb as p2, interacting_pdbs as i2 where p1.id = i1.pdb_first_id and p2.id = i2.pdb_second_id and i1.id = i2.id group by p1.domain_id, p2.domain_id having d1 > 100 and d2 > 100 order by d1, d2;";
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
