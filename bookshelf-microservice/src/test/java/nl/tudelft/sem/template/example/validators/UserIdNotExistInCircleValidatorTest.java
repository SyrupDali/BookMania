package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserIdNotExistInCircleValidatorTest {
    private final BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserIdNotExistInCircleValidator validator =
            new UserIdNotExistInCircleValidator(bookshelfRepository, userRepository);
    private UUID bookshelfId;
    private UUID ownerId;
    private UUID userId;

    @BeforeEach
    public void setUp() {
        bookshelfId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    // Test if the null user id throws the correct exception
    @Test
    public void testHandle_NullUserId() {
        assertThatThrownBy(() -> validator.handle(bookshelfId, ownerId, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User id cannot be null");
    }

    // Test if the non-existing user id throws the correct exception
    @Test
    public void testHandle_NonExistingUserId() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> validator.handle(bookshelfId, ownerId, userId))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User not found");
    }

    // Test if the existing user id in the circle throws the correct exception
    @Test
    public void testHandle_ExistingUserIdInCircle() {
        Bookshelf bookshelf = new Bookshelf();
        User user = new User(userId);
        bookshelf.setMembers(new ArrayList<>(List.of(user)));
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> validator.handle(bookshelfId, ownerId, userId))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User already in circle");
    }

    // Test if the existing user id not in the circle is handled correctly
    @Test
    public void testHandle_ExistingUserIdNotInCircle() throws ValidationException {
        Bookshelf bookshelf = new Bookshelf();
        bookshelf.setMembers(new ArrayList<>());
        User user = new User(userId);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertTrue(validator.handle(bookshelfId, ownerId, userId));
    }

    // Test if bookshelf id is replaced with null mutation is killed
    @Test
    public void testHandle_NullBookshelfIdMutation() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        assertThatThrownBy(() -> validator.handle(null, ownerId, userId))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Bookshelf not found");
    }

    // Test if return replaced with true mutation is killed
    @Test
    public void testHandle_ReturnReplacedWithTrueMutation() throws ValidationException {
        Bookshelf bookshelf = new Bookshelf();
        User user = new User(userId);
        bookshelf.setMembers(new ArrayList<>());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        UserIdExistInPendingListValidator pendingListExistValidator = mock(UserIdExistInPendingListValidator.class);
        when(pendingListExistValidator.handle(bookshelfId, ownerId, userId)).thenReturn(false);
        Validator link = BaseValidator.link(validator, pendingListExistValidator);
        assertThat(link.handle(bookshelfId, ownerId, userId)).isFalse();
    }
}
