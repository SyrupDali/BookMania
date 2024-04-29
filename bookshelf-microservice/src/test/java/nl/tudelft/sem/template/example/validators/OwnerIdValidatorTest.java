package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class OwnerIdValidatorTest {
    private UUID bookshelfId;
    private UUID ownerId;
    private UUID userId;

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
    private final OwnerIdValidator validator = new OwnerIdValidator(userRepository, bookshelfRepository);

    @BeforeEach
    public void setUp() {
        bookshelfId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    // Test if the valid owner id is handled correctly
    @Test
    public void testHandle_NullOwnerId() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validator.handle(bookshelfId, null, userId));
        assertThat(exception.getMessage()).isEqualTo("Owner id cannot be null");
    }

    // Test if the non-existing owner id is handled correctly
    @Test
    public void testHandle_NonExistingOwnerId() {
        // Force the userRepository to return an empty optional for ownerId
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validator.handle(bookshelfId, ownerId, userId));
        assertThat(exception.getMessage()).isEqualTo("Owner not found");
    }

    // Test if the owner id mismatching is handled correctly
    @Test
    public void testHandle_OwnerIdMismatch() {
        // Arrange
        Bookshelf bookshelf = new Bookshelf();
        User bookshelfOwner = new User();
        bookshelfOwner.setUserId(UUID.randomUUID()); // Different owner ID
        bookshelf.setOwner(bookshelfOwner);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(new User(ownerId)));
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validator.handle(bookshelfId, ownerId, userId));
        assertThat(exception.getMessage()).isEqualTo("User does not match the bookshelf's owner");
    }

    // Test if the null bookshelf id is handled correctly
    @Test
    public void testHandle_NullBookshelfId() {
        // Arrange
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(new User(ownerId)));
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validator.handle(null, ownerId, userId));
        assertThat(exception.getMessage()).isEqualTo("Bookshelf not found");
    }

    // Test if the in check next return mutation as true is killed
    @Test
    public void testHandleMutationTrueInCheckNext() throws ValidationException {
        BookshelfIdValidator validator2 = new BookshelfIdValidator(bookshelfRepository);

        Bookshelf bookshelf = new Bookshelf();
        User bookshelfOwner = new User();
        bookshelfOwner.setUserId(ownerId);
        bookshelf.setOwner(bookshelfOwner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(bookshelfOwner));

        Validator linkedValidator = BaseValidator.link(validator, validator2);
        boolean b = linkedValidator.handle(bookshelfId, ownerId, userId);
        assertTrue(b);
        verify(bookshelfRepository, times(2)).findById(bookshelfId);
        verify(userRepository, times(1)).findById(ownerId);
    }


}
