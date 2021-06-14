package utils.equations;

public class ValueEquation {

    private final String column;
    private final String value;
    private final String operation;

    public ValueEquation(String column, String value, String operation) {
        this.column = column;
        this.value = value;
        this.operation = operation;
    }

    public String getColumn() {
        return column;
    }

    public String getValue() {
        return value;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return String.format("%s%s%s", column, operation, value);
    }
}
