package nl.tudelft.sem.template.example.database;

import nl.tudelft.sem.template.model.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookTagRepository extends JpaRepository<BookTag, UUID> {

    /**
     * Gets the tag by its id.
     *
     * @param uuid Id of the tag
     * @return The tag with the given id
     */
    @Query(value = "SELECT * FROM BookTag WHERE ID LIKE ?1", nativeQuery = true)
    BookTag getTagById(UUID uuid);

    /**
     * Gets the tag by its name. The only constraint is that the name must be unique.
     *
     * @param name Name of the tag
     * @return The tag with the given name
     */
    @Query(value = "SELECT * FROM BookTag WHERE tagName LIKE ?1", nativeQuery = true)
    List<BookTag> getTagByName(String name);

}
