package utils.equations;

import java.util.HashSet;

public class SubSelectValues {

    private boolean allTableColumns = false; //SELECT *
    private boolean selectOne = false;       //SELECT 1
    private HashSet<String> columns = new HashSet<>();

    public boolean isAllTableColumns() {
        return allTableColumns;
    }

    public boolean isSelectOne() {
        return selectOne;
    }

    public HashSet<String> getColumns() {
        return columns;
    }

    public void setAllTableColumns(boolean allTableColumns) {
        this.allTableColumns = allTableColumns;
    }

    public void setSelectOne(boolean selectOne) {
        this.selectOne = selectOne;
    }

    public void addColumn(String column) {
        this.columns.add(column);
    }
}
