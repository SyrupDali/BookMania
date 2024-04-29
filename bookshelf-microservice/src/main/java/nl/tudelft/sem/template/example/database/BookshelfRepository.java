package nl.tudelft.sem.template.example.database;


import nl.tudelft.sem.template.model.Bookshelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookshelfRepository extends JpaRepository<Bookshelf, UUID> {

    //find all bookshelves of a user
    @Query(value = "SELECT b FROM Bookshelf b WHERE b.owner.userId = :ownerId")
    List<Bookshelf> findByOwnerId(@Param("ownerId") UUID ownerId);
}
