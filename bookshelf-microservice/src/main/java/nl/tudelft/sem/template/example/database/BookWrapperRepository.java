package nl.tudelft.sem.template.example.database;


import nl.tudelft.sem.template.example.entities.BookWrapperId;
import nl.tudelft.sem.template.model.BookWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookWrapperRepository extends JpaRepository<BookWrapper, BookWrapperId> {

    @Query(value = "SELECT b FROM BookWrapper b WHERE b.userId = :userId")
    List<BookWrapper> findByUserId(@Param("userId") UUID userId);

}
