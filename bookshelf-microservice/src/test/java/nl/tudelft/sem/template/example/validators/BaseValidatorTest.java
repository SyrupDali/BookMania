package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BaseValidatorTest {
    private UUID bookshelfId;
    private UUID ownerId;
    private UUID userId;

    @BeforeEach
    public void setUp() {
        bookshelfId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    // Test if the link method links the validators correctly
    @Test
    public void testLinkValidators() {
        // Create mock validators
        BaseValidator validator1 = mock(BaseValidator.class);
        BaseValidator validator2 = mock(BaseValidator.class);
        BaseValidator validator3 = mock(BaseValidator.class);

        // Link validators
        BaseValidator.link(validator1, validator2, validator3);

        // Assert that validators are linked correctly
        verify(validator1).setNext(validator2);
        verify(validator2).setNext(validator3);
    }

    // Test checkNext method
    @Test
    public void testCheckNextDataPassing() throws ValidationException {
        // Create mock validators
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookshelfIdValidator validator1 = new BookshelfIdValidator(bookshelfRepository);


        // Link validators
        BaseValidator linkedValidator = BaseValidator.link(validator1);

        // Mock the behavior of handle method
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));

        // Invoke checkNext
        boolean result = linkedValidator.checkNext(bookshelfId, ownerId, userId);

        // Assert
        assertTrue(result);
        // Ensure that the data passed through the linked validators and returned true
    }

    // Test for checkNext if the final return value is false
    @Test
    public void testNextValidatorReturnFalse() {
        // Create mock validators
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookshelfIdValidator validator1 = new BookshelfIdValidator(bookshelfRepository);
        UserRepository userRepository = mock(UserRepository.class);
        MemberIdValidator validator2 = new MemberIdValidator(userRepository);

        // Link validators
        BaseValidator linkedValidator = BaseValidator.link(validator1, validator2);

        // Mock the behavior of handle method
        when(bookshelfRepository.findById(Mockito.any())).thenReturn(Optional.of(new Bookshelf()));

        // Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> linkedValidator.checkNext(UUID.randomUUID(), UUID.randomUUID(), null));
        assertThat(exception.getMessage()).isEqualTo("User id cannot be null");
    }

    // Test for checkNext if the final return value is true
    @Test
    public void testNextValidatorReturnTrue() {
        // Create mock validators
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookshelfIdValidator validator1 = new BookshelfIdValidator(bookshelfRepository);
        UserRepository userRepository = mock(UserRepository.class);
        MemberIdValidator validator2 = new MemberIdValidator(userRepository);

        // Link validators
        BaseValidator linkedValidator = BaseValidator.link(validator1, validator2);

        // Mock the behavior of handle method
        when(bookshelfRepository.findById(Mockito.any())).thenReturn(Optional.of(new Bookshelf()));
        when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(new User()));

        // Assert
        boolean result = assertDoesNotThrow(
                () -> linkedValidator.checkNext(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        assertTrue(result);
    }

    // Test for checkNext if the second validator's repository is called
    @Test
    public void testSecondValidatorCalled() {
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookshelfIdValidator validator1 = new BookshelfIdValidator(bookshelfRepository);
        UserRepository userRepository = mock(UserRepository.class);
        MemberIdValidator validator2 = new MemberIdValidator(userRepository);

        // Link validators
        BaseValidator linkedValidator = BaseValidator.link(validator1, validator2);

        // Mock the behavior of handle method
        when(bookshelfRepository.findById(Mockito.any())).thenReturn(Optional.of(new Bookshelf()));
        when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(new User()));

        // Assert
        boolean result = assertDoesNotThrow(
                () -> linkedValidator.checkNext(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        assertTrue(result);
        verify(userRepository).findById(Mockito.any());
    }

    // Test for checkNext if return is mutated to true
    @Test
    public void testHandleMutationTrueInCheckNext() {
        UserRepository userRepository = mock(UserRepository.class);
        MemberIdValidator validator2 = new MemberIdValidator(userRepository);
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookshelfIdValidator validator1 = new BookshelfIdValidator(bookshelfRepository);
        OwnerIdValidator validator3 = new OwnerIdValidator(userRepository, bookshelfRepository);

        Bookshelf bookshelf = new Bookshelf();
        User owner = new User();
        owner.setUserId(ownerId);
        bookshelf.setOwner(new User(UUID.randomUUID()));
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Validator linkedValidator = BaseValidator.link(validator1, validator2, validator3);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> linkedValidator.handle(bookshelfId, ownerId, userId));
        assertThat(exception.getMessage()).isEqualTo("User does not match the bookshelf's owner");
        verify(bookshelfRepository, times(2)).findById(bookshelfId);
        verify(userRepository, times(1)).findById(ownerId);
        verify(userRepository, times(1)).findById(userId);
    }

    // Test for handle if mutated return to true and next validator owner is authorized
    @Test
    public void testHandleMutationTrueInCheckNextOwnerIsAuthorized() {
        UserRepository userRepository = mock(UserRepository.class);
        MemberIdValidator validator2 = new MemberIdValidator(userRepository);
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookshelfIdValidator validator1 = new BookshelfIdValidator(bookshelfRepository);
        OwnerIdValidator validator3 = new OwnerIdValidator(userRepository, bookshelfRepository);

        Bookshelf bookshelf = new Bookshelf();
        User owner = new User();
        owner.setUserId(ownerId);
        bookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Validator linkedValidator = BaseValidator.link(validator1, validator2, validator3);
        assertDoesNotThrow(() -> linkedValidator.handle(bookshelfId, ownerId, userId));
        verify(bookshelfRepository, times(2)).findById(bookshelfId);
        verify(userRepository, times(1)).findById(ownerId);
        verify(userRepository, times(1)).findById(userId);
    }

    // Final return statement is mutated to true
    @Test
    public void testHandle_OwnerIdValidatorTrueReturnMutation() throws ValidationException {
        UserRepository userRepository = mock(UserRepository.class);
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        OwnerIdValidator validator1 = new OwnerIdValidator(userRepository, bookshelfRepository);
        BookshelfIdValidator validator2 = mock(BookshelfIdValidator.class);
        Validator linkedValidator = BaseValidator.link(validator1, validator2);

        Bookshelf bookshelf = new Bookshelf();
        User owner = new User(ownerId);
        bookshelf.setOwner(owner);
        when(validator2.handle(bookshelfId, ownerId, userId)).thenReturn(false);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        assertThat(linkedValidator.handle(bookshelfId, ownerId, userId)).isFalse();
    }
}
