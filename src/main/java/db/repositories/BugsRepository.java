package db.repositories;

import db.tables.Bugs;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugsRepository extends CrudRepository<Bugs, Long> {

    @Query("SELECT * FROM bugs WHERE query_id = :queryId")
    List<Bugs> findByQueryId(@Param("queryId") Long queryId);
}
