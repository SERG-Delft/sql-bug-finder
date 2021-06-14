package utils.equations;

public class ColumnEquation {

    private final String table;
    private final String column;
    private final String operation;

    public ColumnEquation(String table, String column, String operation) {
        this.table = table;
        this.column = column;
        this.operation = operation;
    }

    public String getTable() {
        return table;
    }

    public String getColumn() {
        return column;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return String.format("%s.%s (%s)", table, column, operation);
    }
}
