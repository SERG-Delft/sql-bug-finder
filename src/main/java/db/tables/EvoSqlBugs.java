package db.tables;

import org.springframework.data.annotation.Id;

public class EvoSqlBugs {

    @Id
    private Long bugId;
    private Long queryId;
    private String bugs;
    private String errors;

    public EvoSqlBugs(Long queryId, String bugs, String errors) {
        this.queryId = queryId;
        this.bugs = bugs;
        this.errors = errors;
    }

    @Override
    public String toString() {
        return String.format("query_id: %s, bugs: %s, errors: %s", queryId, bugs, errors);
    }
}
