package db.repositories;

import db.tables.EvoSqlQueries;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvoSqlQueriesRepository extends CrudRepository<EvoSqlQueries, Long> {

    @Query("SELECT * FROM evo_sql_queries WHERE query_id = :queryId")
    List<EvoSqlQueries> findByQueryId(@Param("queryId") Long queryId);

    @Query("SELECT * FROM evo_sql_queries WHERE is_valid = ''")
    List<EvoSqlQueries> findUnknownQueries();

    @Query("SELECT * FROM evo_sql_queries WHERE is_valid = '1'")
    List<EvoSqlQueries> findValidQueries();
}
