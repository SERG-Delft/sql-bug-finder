package utils.equations;

public class ColumnValue {

    private String table;
    private String column;
    private String alias;
    private String currentQuery;

    public ColumnValue(String table, String column, String alias) {
        this.table = table;
        this.column = column;
        this.alias = alias;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setCurrentQuery(String query) {
        this.currentQuery = query;
    }

    public String getTable() {
        return table;
    }

    public String getColumn() {
        return column;
    }

    public String getAlias() {
        return alias;
    }

    public boolean exactMatch(ColumnValue columnValue) {
        return this.table.equals(columnValue.table) && this.column.equals(columnValue.column) && this.alias.equals(columnValue.alias);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof ColumnValue)) return false;
        ColumnValue columnValue = (ColumnValue) object;
        return columnValue.getTable().equals(this.table) && columnValue.getColumn().equals(this.column);
    }

    @Override
    public String toString() {
        if (table == null || table.equals("null"))
            return column;
        return String.format("%s.%s", table, column);
    }
}
