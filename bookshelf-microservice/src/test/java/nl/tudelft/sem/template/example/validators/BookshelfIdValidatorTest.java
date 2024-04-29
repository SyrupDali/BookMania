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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookshelfIdValidatorTest {
    private UUID bookshelfId;
    private UUID ownerId;
    private UUID userId;

    @BeforeEach
    public void setUp() {
        bookshelfId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    // Test if the valid bookshelf id is handled correctly
    @Test
    public void testHandle_ValidBookshelfId() {
        // Arrange
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookshelfIdValidator validator = new BookshelfIdValidator(bookshelfRepository);
        Bookshelf bookshelf = new Bookshelf();
        bookshelf.setBookshelfId(bookshelfId);

        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));

        // Act & Assert
        // Ensure the method doesn't throw an exception for a valid bookshelfId
        assertDoesNotThrow(() -> validator.handle(bookshelfId, ownerId, userId));
    }

    // Test if the null bookshelf id is handled correctly
    @Test
    public void testHandle_NullBookshelfId() {
        // Arrange
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookshelfIdValidator validator = new BookshelfIdValidator(bookshelfRepository);
        // Act & Assert
        // Ensure the method throws a ValidationException for a null bookshelfId
        assertThrows(ValidationException.class, () -> validator.handle(null, ownerId, userId));
    }

    // Test if the invalid bookshelf id is handled correctly
    @Test
    public void testHandle_InvalidBookshelfId() {
        // Arrange
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookshelfIdValidator validator = new BookshelfIdValidator(bookshelfRepository);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.empty());

        // Act & Assert
        // Ensure the method throws a ValidationException for an invalid bookshelfId
        assertThrows(ValidationException.class, () -> validator.handle(bookshelfId, ownerId, userId));
    }

    // Test if return mutation as true is killed
    @Test
    public void testHandleMutationTrueInCheckNext() throws ValidationException {
        UserRepository userRepository = mock(UserRepository.class);
        MemberIdValidator validator1 = new MemberIdValidator(userRepository);
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookshelfIdValidator validator2 = new BookshelfIdValidator(bookshelfRepository);

        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        Validator linkedValidator = BaseValidator.link(validator1, validator2);
        boolean b = linkedValidator.handle(bookshelfId, ownerId, userId);
        assertTrue(b);
        verify(bookshelfRepository, times(1)).findById(bookshelfId);
        verify(userRepository, times(1)).findById(userId);
    }

    // Test if return mutation as true is killed
    @Test
    public void testHandleMutationTrueInCheckNext2() throws ValidationException {
        MemberIdValidator validator1 = mock(MemberIdValidator.class);
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookshelfIdValidator validator2 = new BookshelfIdValidator(bookshelfRepository);

        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));
        when(validator1.handle(bookshelfId, ownerId, userId)).thenReturn(false);
        Validator linkedValidator = BaseValidator.link(validator2, validator1);
        assertThat(linkedValidator.handle(bookshelfId, ownerId, userId)).isFalse();
    }
}
