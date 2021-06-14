package db.repositories;

import db.tables.Answers;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswersRepository extends CrudRepository<Answers, Long> {

    @Query("SELECT * FROM answers WHERE answer_id = :answerId")
    List<Answers> findByAnswerId(@Param("answerId") Long answerId);
}
