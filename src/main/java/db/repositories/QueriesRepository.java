package db.repositories;

import db.tables.Queries;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueriesRepository extends CrudRepository<Queries, Long> {

    @Query("SELECT * FROM queries WHERE query_id = :queryId")
    List<Queries> findByQueryId(@Param("queryId") Long queryId);

    @Query("SELECT * FROM queries WHERE is_valid = ''")
    List<Queries> findUnknownQueries();

    @Query("SELECT * FROM queries WHERE is_valid = '1'")
    List<Queries> findValidQueries();
}
