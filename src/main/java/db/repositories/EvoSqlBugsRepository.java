package db.repositories;

import db.tables.EvoSqlBugs;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvoSqlBugsRepository extends CrudRepository<EvoSqlBugs, Long> {

    @Query("SELECT * FROM evo_sql_bugs WHERE query_id = :queryId")
    List<EvoSqlBugs> findByQueryId(@Param("queryId") Long queryId);
}
