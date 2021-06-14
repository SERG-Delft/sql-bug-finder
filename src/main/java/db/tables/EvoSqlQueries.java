package db.tables;

import org.springframework.data.annotation.Id;

public class EvoSqlQueries {

    @Id
    private Long queryId;
    private Long answerId;
    private Long questionId;
    private String query;
    private String isValid;
    private Long predicates;
    private Long joins;
    private Long subqueries;
    private Long functions;
    private Long columns;

    public EvoSqlQueries(Long queryId, Long answerId, Long questionId, String query, String isValid) {
        this.queryId = queryId;
        this.answerId = answerId;
        this.questionId = questionId;
        this.query = query;
        this.isValid = isValid;
    }

    @Override
    public String toString() {
        return String.format("query_id: %s, answer_id: %s, question_id: %s, query: %s, is_valid: %s", queryId, answerId, questionId, query, isValid);
    }

    public String getQuery() {
        return query;
    }

    public Long getQueryId() {
        return queryId;
    }

    public void setIsValid(String isValid) {
        this.isValid = isValid;
    }

    public void setPredicates(Long predicates) {
        this.predicates = predicates;
    }

    public void setJoins(Long joins) {
        this.joins = joins;
    }

    public void setSubqueries(Long subqueries) {
        this.subqueries = subqueries;
    }

    public void setFunctions(Long functions) {
        this.functions = functions;
    }

    public void setColumns(Long columns) {
        this.columns = columns;
    }
}
