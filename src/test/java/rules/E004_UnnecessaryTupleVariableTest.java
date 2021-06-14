package rules;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class E004_UnnecessaryTupleVariableTest {

    private String query;
    private final CCJSqlParserManager pm = new CCJSqlParserManager();
    private final ArrayList<String> results = new ArrayList<>();
    private final E004_UnnecessaryTupleVariable rule = new E004_UnnecessaryTupleVariable();

    @Before
    public void setUp() {
        query = "";
        results.clear();
    }

    @After
    public void tearDown() {}

    @Test
    public void testUnnecessaryTupleVariable() throws JSQLParserException {
        query = "SELECT E.id, E.name FROM employee E, department D WHERE E.dep_number=D.dep_number AND D.dep_number=20;";
        results.add("department");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        results.clear();
        query = "select emp.salary from employee emp inner join sap s on emp.id = s.id where s.id = 111;";
        results.add("sap");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryTupleVariableInSubQuery() throws JSQLParserException {
        query = "SELECT name FROM employee X WHERE EXISTS (SELECT Y.emp_no FROM staff Y WHERE X.emp_no = Y.emp_no AND Y.emp_no <> '123');";
        results.add("staff");
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testNoWarnings() throws JSQLParserException {
        query = "SELECT * FROM employees X, employees Y WHERE X.employee_id=Y.employee_id AND X.salary=1000 AND Y.salary=2000;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select u.firstname, u.lastname, u.rep, u.email, u.password, u.gender, u.level, u.birthday, u.achievements, u.height, u.unit, u.cityid, u.countryid, r.regdate, ci.name as city, co.name as country from users u, registry r, cities ci, countries co where u.id = 1 and r.uid = u.id and u.cityid = ci.id and u.countryid = co.id limit 1;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select case when parent = 1 then child else parent end as leaf from   ownership where  1 in (child, parent) union all select case when o.parent = g.leaf then o.child else o.parent end -- as leaf from   graph g join   ownership o on g.leaf in (o.parent, o.child) and    row(o.parent, o.child) <> all(path) ) select * from   graph;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select id, name, phone from table1 left join table2 on table1.id = table2.id and table2.isdefault = 1 where table1.id = 12;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryTupleVariableNoWarningsWhenTuplesAreNotEquated() throws JSQLParserException {
        query = "select exact.car_id   as e_car_id, exact.color   as e_color, exact.weight   as e_weight, exact.type    as e_type, related.car_id as r_car_id, related.color as r_color, related.weight as r_weight, related.type  as r_type, case when related.color = exact.color      then 1 else 0 end as rank from cars as exact inner join cars as related on ( related.car_id <> exact.car_id and case when related.color = exact.color   then 1 else 0 end ) where exact.car_id = 1 /* black, heavy, limo */ order by rank desc;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
        query = "select e1.* from   event e1 inner join event e2 on e2.clientid = e1.clientid and e2.typeid > e1.typeid and e2.date > e1.date where  e1.typeid = 10;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryTupleVariableTwoDifferentTablesAllColumnsSelected() throws JSQLParserException {
        query = "select * from child c,parent p where c.id=p.parentid and c.parentid !=0;";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryTupleVariableInExpression() throws JSQLParserException {
        query = "select m.actor from movies m where m.movie = 'pulp fiction' and not exists ( select 1 from movies m1 join movies m2 on m1.movie = m2.movie and m1.actor <> m2.actor and m2.movie <> 'pulp fiction' and m2.actor in (select actor from movies where movie = 'pulp fiction') where m.actor = m1.actor );";
        pm.parse(new StringReader(query)).accept(rule);
        assertEquals(results, rule.getResults());
    }

    @Test
    public void testUnnecessaryTupleVariableUsedInFunction() throws JSQLParserException {
        query = "update purchase as p inner join artwork as a on p.purchaseid = a.purchaseid set p.total = sum(a.price) where a.purchaseid = 'd4758';";
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