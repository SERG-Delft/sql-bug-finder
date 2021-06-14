package interfaces;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rules.E001_InconsistentTupleVariable;

import static org.junit.Assert.*;

public class QueryVisitorTest {

    private final QueryVisitor qv = new E001_InconsistentTupleVariable();

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    @Test
    public void testIsNull() {
        Expression nullValue = new NullValue();
        assertTrue(qv.isNull(nullValue));
    }

    @Test
    public void testIsColumn() {
        Expression column = new Column();
        assertTrue(qv.isColumn(column));
    }

    @Test
    public void testIsStringValue() {
        Expression stringValue = new StringValue("value");
        assertTrue(qv.isStringValue(stringValue));
    }

    @Test
    public void testIsNotStringValue() {
        Expression stringValue = new LongValue("123");
        assertFalse(qv.isStringValue(stringValue));
    }

    @Test
    public void testIsLongValue() {
        Expression longValue = new LongValue("123");
        assertTrue(qv.isLongValue(longValue));
    }

    @Test
    public void testIsNotLongValue() {
        Expression longValue = new StringValue("123");
        assertFalse(qv.isLongValue(longValue));
    }

    @Test
    public void testGetExpressionValue() {
        Expression longValue = new LongValue("123");
        assertEquals("123", qv.getExpressionValue(longValue));
        Expression stringValue = new StringValue("value");
        assertEquals("value", qv.getExpressionValue(stringValue));
        Expression nullValue = new NullValue();
        assertEquals("", qv.getExpressionValue(nullValue));
    }

    @Test
    public void testHasTable() {
        Table table = new Table("table");
        Column column = new Column(table, "column");
        assertTrue(qv.hasTable(column));
    }

    @Test
    public void testGetTableNameFromColumn() {
        qv.tableNames.add("table");
        Table table = new Table("table");
        Column column = new Column(table, "column");
        assertEquals("table", qv.getTableNameFromColumn(column));
    }

    @Test
    public void testGetTableNameFromColumnNonExistentTable() {
        Table table = new Table("table");
        Column column = new Column(table, "column");
        assertEquals("null", qv.getTableNameFromColumn(column));
    }

    @Test
    public void testGetTableNameFromColumnNullTable() {
        Column column = new Column("column");
        assertEquals("null", qv.getTableNameFromColumn(column));
    }

    @Test
    public void testGetTableAliasFromColumn() {
        qv.tableNames.add("table");
        Table table = new Table("table");
        Column column = new Column(table, "column");
        assertEquals(table.getName(), qv.getTableAliasFromColumn(column));
    }
}