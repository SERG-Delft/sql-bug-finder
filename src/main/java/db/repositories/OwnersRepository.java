package db.repositories;

import db.tables.Owners;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnersRepository extends CrudRepository<Owners, Long> {

    @Query("SELECT * FROM owners WHERE user_id = :userId")
    List<Owners> findByOwnerId(@Param("userId") Long userId);
}
