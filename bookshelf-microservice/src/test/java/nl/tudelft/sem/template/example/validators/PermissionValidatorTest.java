package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionValidatorTest {

    private BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);

    private UserRepository userRepository = mock(UserRepository.class);

    @Test
    public void userIsNull() {
        PermissionValidator validator = new PermissionValidator(bookshelfRepository, userRepository);
        assertThatThrownBy(() -> {
            validator.handle(UUID.randomUUID(), UUID.randomUUID(), null);
        }).isInstanceOf(ValidationException.class)
                .hasMessage("User id cannot be null");
    }

    @Test
    public void bookshelfNull() {
        PermissionValidator validator = new PermissionValidator(bookshelfRepository, userRepository);
        assertThatThrownBy(() -> {
            validator.handle(null, UUID.randomUUID(), UUID.randomUUID());
        }).isInstanceOf(ValidationException.class)
                .hasMessage("Bookshelf not found");
    }

    @Test
    public void bookshelfNotFound() {
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.empty());
        PermissionValidator validator = new PermissionValidator(bookshelfRepository, userRepository);
        assertThatThrownBy(() -> {
            validator.handle(bookshelfId, UUID.randomUUID(), UUID.randomUUID());
        }).isInstanceOf(ValidationException.class)
                .hasMessage("Bookshelf not found");
    }

    @Test
    public void userIsOwner() {
        UUID bookshelfId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Bookshelf bookshelf = new Bookshelf().owner(new User(ownerId));
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        PermissionValidator validator = new PermissionValidator(bookshelfRepository, userRepository);
        assertThat(
            validator.handle(bookshelfId, ownerId, ownerId)
        ).isTrue();
    }

    @Test
    public void userPartOfCircle() {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User(userId);
        UUID ownerId = UUID.randomUUID();
        User owner = new User(ownerId);
        List<User> circle = List.of(new User(UUID.randomUUID()), user);
        Bookshelf bookshelf = new Bookshelf().owner(owner).members(circle);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        PermissionValidator validator = new PermissionValidator(bookshelfRepository, userRepository);
        assertThat(
                validator.handle(bookshelfId, ownerId, userId)
        ).isTrue();
    }

    @Test
    public void userNotPartOfCircle() {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        User owner = new User(ownerId);
        List<User> circle = List.of(new User(UUID.randomUUID()), new User(UUID.randomUUID()));
        Bookshelf bookshelf = new Bookshelf().owner(owner).members(circle);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        PermissionValidator validator = new PermissionValidator(bookshelfRepository, userRepository);
        assertThatThrownBy(() -> {
            validator.handle(bookshelfId, ownerId, userId);

        }).isInstanceOf(ValidationException.class)
                .hasMessage("User does not have permission to modify the bookshelf");
    }

    /**
     * This tests kills the mutation that is created for the first if block.
     * I mock the output of the super.checkNext, and expect that value again, therefore the mutation is killed.
     */
    @Test
    public void mutationReturnFalseOnSuperHandleFirstIf() {
        UUID bookshelfId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        User owner = new User(ownerId);
        List<User> circle = List.of(new User(UUID.randomUUID()), new User(UUID.randomUUID()));
        Bookshelf bookshelf = new Bookshelf().owner(owner).members(circle);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        PermissionValidator permissionValidator = new PermissionValidator(bookshelfRepository, userRepository);
        MemberIdValidator memberIdValidator = mock(MemberIdValidator.class);
        BaseValidator validator = BaseValidator.link(permissionValidator, memberIdValidator);
        when(memberIdValidator.handle(bookshelfId, ownerId, ownerId)).thenReturn(false);
        assertThat(validator.handle(bookshelfId, ownerId, ownerId)).isFalse();

    }

    /**
     * This tests kills the mutation that is created for the second if block.
     * I mock the output of the super.checkNext, and expect that value again, therefore the mutation is killed.
     */
    @Test
    public void mutationReturnFalseOnSuperHandleSecondIf() {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User(userId);
        UUID ownerId = UUID.randomUUID();
        User owner = new User(ownerId);
        List<User> circle = List.of(new User(UUID.randomUUID()), user);
        Bookshelf bookshelf = new Bookshelf().owner(owner).members(circle);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(bookshelf));
        PermissionValidator permissionValidator = new PermissionValidator(bookshelfRepository, userRepository);
        MemberIdValidator memberIdValidator = mock(MemberIdValidator.class);
        BaseValidator validator = BaseValidator.link(permissionValidator, memberIdValidator);
        when(memberIdValidator.handle(bookshelfId, ownerId, userId)).thenReturn(false);
        assertThat(validator.handle(bookshelfId, ownerId, userId)).isFalse();

    }

}