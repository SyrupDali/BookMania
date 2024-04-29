package nl.tudelft.sem.template.example.database;

import nl.tudelft.sem.template.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query(value = "SELECT * FROM User WHERE userId LIKE ?1", nativeQuery = true)
    Optional<User> findByUserId(UUID userId);

}
