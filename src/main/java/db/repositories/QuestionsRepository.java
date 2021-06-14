package db.repositories;

import db.tables.Questions;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionsRepository extends CrudRepository<Questions, Long> {

    @Query("SELECT * FROM questions WHERE question_id = :questionId")
    List<Questions> findByQuestionId(@Param("questionId") Long questionId);
}
