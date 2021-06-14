package db.repositories;

import db.tables.Pages;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagesRepository extends CrudRepository<Pages, Long> {

    @Query("SELECT * FROM pages WHERE page_id = :pageId")
    List<Pages> findByPageId(@Param("pageId") Long pageId);
}
