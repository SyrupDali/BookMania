package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MemberIdValidatorTest {

    private UUID bookshelfId;
    private UUID ownerId;
    private UUID userId;

    @BeforeEach
    public void setUp() {
        bookshelfId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    // Test if the valid user id is handled correctly
    @Test
    public void testHandle_ExistingUserId() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        MemberIdValidator validator = new MemberIdValidator(userRepository);

        // Force the userRepository to return a non-empty optional for userId
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertDoesNotThrow(() -> validator.handle(bookshelfId, ownerId, userId));
        // Ensure that the method does not throw ValidationException for an existing userId
    }

    // Test if the non-existing user id is handled correctly
    @Test
    public void testHandle_NonExistingUserId() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        MemberIdValidator validator = new MemberIdValidator(userRepository);

        // Force the userRepository to return an empty optional for userId
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class, () -> validator.handle(bookshelfId, ownerId, userId));
        assertThat(exception.getMessage()).isEqualTo("User not found");
    }

    // Test if the null user id is handled correctly
    @Test
    public void testHandle_NullUserId() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        MemberIdValidator validator = new MemberIdValidator(userRepository);

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class, () -> validator.handle(bookshelfId, ownerId, null));
        assertThat(exception.getMessage()).isEqualTo("User id cannot be null");
    }

    // Test if the return mutation as true is killed
    @Test
    public void testHandleMutationTrueInCheckNext() throws ValidationException {
        UserRepository userRepository = mock(UserRepository.class);
        MemberIdValidator validator1 = new MemberIdValidator(userRepository);
        BookshelfIdValidator validator2 = mock(BookshelfIdValidator.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(validator2.handle(bookshelfId, ownerId, userId)).thenReturn(false);
        Validator linkedValidator = BaseValidator.link(validator1, validator2);
        assertThat(linkedValidator.handle(bookshelfId, ownerId, userId)).isFalse();
    }
}
