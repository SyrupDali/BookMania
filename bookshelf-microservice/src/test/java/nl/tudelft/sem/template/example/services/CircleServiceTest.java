package nl.tudelft.sem.template.example.services;

import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.example.validators.BaseValidator;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import nl.tudelft.sem.template.model.BookshelfBookshelfIdCircleDelete200Response;
import nl.tudelft.sem.template.model.BookshelfBookshelfIdCirclePut200Response;
import nl.tudelft.sem.template.model.User;
import org.junit.jupiter.api.BeforeEach;
import nl.tudelft.sem.template.model.Bookshelf;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CircleServiceTest {
    private final BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final BaseValidator validationHandler = mock(BaseValidator.class);
    private final BookshelfService bookshelfService = mock(BookshelfService.class);
    private final CircleService circleService = new CircleService(bookshelfRepository, userRepository, bookshelfService);

    private UUID bookshelfId;
    private UUID ownerId;
    private UUID userId;

    @BeforeEach
    public void setUp() {
        // Initialize UUIDs before each test
        bookshelfId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    // Test the addMemberToCircle method - no exceptions
    @Test
    public void testAddMemberToCircleSuccess() throws Exception {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenReturn(true);

        User member = new User(); // Create a user object for the member
        member.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(member));

        User owner = new User(); // Create a user object for the owner
        owner.setUserId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        existingBookshelf.setMembers(new ArrayList<>());
        existingBookshelf.setPendingMembers(new ArrayList<>());
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));

        circleService.setAddMemberValidationHandler(validationHandler);
        // Perform the method call
        BookshelfBookshelfIdCirclePut200Response response = circleService.addMemberToCircle(bookshelfId, ownerId, userId);

        // Assertions to verify the response or changes made
        assertNotNull(response);
        assertEquals(bookshelfId, response.getBookshelfId());
        assertTrue(response.getUserIds().contains(userId));
        // Verify if save was called on the repository
        verify(bookshelfRepository, times(1)).save(existingBookshelf);
        verify(bookshelfService).addBookWrapperMultiple(userId, existingBookshelf.getBooks());
    }

    // Test the addMemberToCircle method with null bookshelf id - validation exception
    @Test
    public void testAddMemberToCircleNullBookshelfId() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(null, ownerId, userId)).thenThrow(new ValidationException("Bookshelf id cannot be null"));

        circleService.setAddMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.addMemberToCircle(null, ownerId, userId));

        // Assert the exception message
        assertEquals("Bookshelf id cannot be null", exception.getMessage());
    }

    // Test the addMemberToCircle method with null owner id - validation exception
    @Test
    public void testAddMemberToCircleNullOwnerId() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, null, userId)).thenThrow(new ValidationException("Owner id cannot be null"));

        circleService.setAddMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.addMemberToCircle(bookshelfId, null, userId));

        // Assert the exception message
        assertEquals("Owner id cannot be null", exception.getMessage());
    }

    // Test the addMemberToCircle method with null member id - validation exception
    @Test
    public void testAddMemberToCircleNullMemberId() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, null)).thenThrow(new ValidationException("User id cannot be null"));

        circleService.setAddMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.addMemberToCircle(bookshelfId, ownerId, null));

        // Assert the exception message
        assertEquals("User id cannot be null", exception.getMessage());
    }

    // Test the addMemberToCircle method with bookshelf id not found - validation exception
    @Test
    public void testAddMemberToCircleBookshelfIdNotFound() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("Bookshelf not found"));

        circleService.setAddMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.addMemberToCircle(bookshelfId, ownerId, userId));

        // Assert the exception message
        assertEquals("Bookshelf not found", exception.getMessage());
    }

    // Test the addMemberToCircle method with owner id not found - validation exception
    @Test
    public void testAddMemberToCircleOwnerNotFound() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("Owner not found"));

        circleService.setAddMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.addMemberToCircle(bookshelfId, ownerId, userId));

        // Assert the exception message
        assertEquals("Owner not found", exception.getMessage());
    }

    // Test the addMemberToCircle method with member id not found - validation exception
    @Test
    public void testAddMemberToCircleMemberNotFound() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("User not found"));

        circleService.setAddMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.addMemberToCircle(bookshelfId, ownerId, userId));

        // Assert the exception message
        assertEquals("User not found", exception.getMessage());
    }

    // Test the addMemberToCircle method with owner mismatch - validation exception
    @Test
    public void testAddMemberToCircleOwnerMismatch() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId))
                .thenThrow(new ValidationException("User does not match the bookshelf's owner"));

        circleService.setAddMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.addMemberToCircle(bookshelfId, ownerId, userId));

        // Assert the exception message
        assertEquals("User does not match the bookshelf's owner", exception.getMessage());
    }

    // Test the addMemberToCircle method with member not in circle - no exceptions
    @Test
    public void testAddMemberToCircleWithMemberNotInCircle() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenReturn(true);

        User member = new User(); // Create a user object for the member
        member.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(member));

        User owner = new User(); // Create a user object for the owner
        owner.setUserId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        members.add(user1);
        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        members.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(new ArrayList<>());
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));

        circleService.setAddMemberValidationHandler(validationHandler);
        // Perform the method call
        BookshelfBookshelfIdCirclePut200Response response = circleService.addMemberToCircle(bookshelfId, ownerId, userId);

        // Assertions to verify the response or changes made
        assertNotNull(response);
        assertEquals(bookshelfId, response.getBookshelfId());
        assertTrue(response.getUserIds().contains(userId));
        // Verify if save was called on the repository
        verify(bookshelfRepository, times(1)).save(existingBookshelf);
    }

    // Test the addMemberToCircle method with member already in circle - user already in collection exception
    @Test
    public void testAddMemberToCircleWithMemberInCircle() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId))
                .thenThrow(new ValidationException("User already in circle"));

        User member = new User(); // Create a user object for the member
        member.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(member));

        User owner = new User(); // Create a user object for the owner
        owner.setUserId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        members.add(user1);
        User user2 = new User();
        user2.setUserId(userId);
        members.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));

        circleService.setAddMemberValidationHandler(validationHandler);
        // Perform the method call
        assertThrows(ValidationException.class,
                () -> circleService.addMemberToCircle(bookshelfId, ownerId, userId));
    }

    // Test the addMemberToCircle method with null bookshelf id mutation - validation exception
    @Test
    public void testAddMemberToCircleNullBookshelfIdMutation() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(null, ownerId, userId)).thenReturn(true);

        circleService.setAddMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.addMemberToCircle(null, ownerId, userId));
        assertThat(exception.getMessage()).isEqualTo("Bookshelf not found");
    }

    // Test the addMemberToCircle method with null owner id mutation - validation exception
    @Test
    public void testAddMemberToCircleNullMemberIdMutation() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, null)).thenReturn(true);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));

        circleService.setAddMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.addMemberToCircle(bookshelfId, ownerId, null));
        assertThat(exception.getMessage()).isEqualTo("User not found");
    }

    // Test the removeMemberFromCircle method - no exceptions
    @Test
    public void testRemoveMemberFromCircleSuccess() throws Exception {
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenReturn(true);

        User member = new User(); // Create a user object for the member
        member.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(member));

        User owner = new User(); // Create a user object for the owner
        owner.setUserId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        List<User> members = new ArrayList<>();
        members.add(member);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(new ArrayList<>());
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));

        circleService.setRemoveMemberValidationHandler(validationHandler);
        // Perform the method call
        BookshelfBookshelfIdCircleDelete200Response response = circleService.removeMemberFromCircle(bookshelfId, ownerId, userId);

        // Assertions to verify the response or changes made
        assertNotNull(response);
        assertEquals(bookshelfId, response.getBookshelfId());
        assertFalse(response.getUserIds().contains(userId));
        // Verify if save was called on the repository
        verify(bookshelfRepository, times(1)).save(existingBookshelf);
        verify(bookshelfService).deleteBookWrapperMultiple(userId, existingBookshelf.getBooks());
    }

    // Test the removeMemberFromCircle method with null bookshelf id - validation exception
    @Test
    public void testRemoveMemberFromCircleNullBookshelfId() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(null, ownerId, userId)).thenThrow(new ValidationException("Bookshelf id cannot be null"));

        circleService.setRemoveMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.removeMemberFromCircle(null, ownerId, userId));

        // Assert the exception message
        assertEquals("Bookshelf id cannot be null", exception.getMessage());
    }

    // Test the removeMemberFromCircle method with null owner id - validation exception
    @Test
    public void testRemoveMemberFromCircleNullOwnerId() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, null, userId)).thenThrow(new ValidationException("Owner id cannot be null"));

        circleService.setRemoveMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.removeMemberFromCircle(bookshelfId, null, userId));

        // Assert the exception message
        assertEquals("Owner id cannot be null", exception.getMessage());
    }

    // Test the removeMemberFromCircle method with null member id - validation exception
    @Test
    public void testRemoveMemberFromCircleNullMemberId() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, null)).thenThrow(new ValidationException("User id cannot be null"));

        circleService.setRemoveMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.removeMemberFromCircle(bookshelfId, ownerId, null));

        // Assert the exception message
        assertEquals("User id cannot be null", exception.getMessage());
    }

    // Test the removeMemberFromCircle method with bookshelf id not found - validation exception
    @Test
    public void testRemoveMemberFromCircleBookshelfIdNotFound() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("Bookshelf not found"));

        circleService.setRemoveMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.removeMemberFromCircle(bookshelfId, ownerId, userId));

        // Assert the exception message
        assertEquals("Bookshelf not found", exception.getMessage());
    }

    // Test the removeMemberFromCircle method with owner id not found - validation exception
    @Test
    public void testRemoveMemberFromCircleOwnerNotFound() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("Owner not found"));

        circleService.setRemoveMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception

        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.removeMemberFromCircle(bookshelfId, ownerId, userId));

        // Assert the exception message
        assertEquals("Owner not found", exception.getMessage());
    }

    // Test the removeMemberFromCircle method with member id not found - validation exception
    @Test
    public void testRemoveMemberFromCircleMemberNotFound() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("User not found"));

        circleService.setRemoveMemberValidationHandler(validationHandler);

        // Perform the method call and assert the thrown exception

        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.removeMemberFromCircle(bookshelfId, ownerId, userId));

        // Assert the exception message
        assertEquals("User not found", exception.getMessage());
    }

    // Test the removeMemberFromCircle method with owner mismatch - validation exception
    @Test
    public void testRemoveMemberFromCircleOwnerMismatch() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("User does not match the bookshelf's owner"));

        circleService.setRemoveMemberValidationHandler(validationHandler);

        // Perform the method call

        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.removeMemberFromCircle(bookshelfId, ownerId, userId));

        // Assert the exception message
        assertEquals("User does not match the bookshelf's owner", exception.getMessage());
    }

    // Test the removeMemberFromCircle method with member not in circle - user not in collection exception
    @Test
    public void testRemoveMemberFromCircleWithMemberNotInCircle() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId))
                .thenThrow(new ValidationException("User not in circle"));

        User member = new User(); // Create a user object for the member
        member.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(member));

        User owner = new User(); // Create a user object for the owner
        owner.setUserId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        members.add(user1);
        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        members.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));

        circleService.setRemoveMemberValidationHandler(validationHandler);
        // Perform the method call
        assertThrows(ValidationException.class,
                () -> circleService.removeMemberFromCircle(bookshelfId, ownerId, userId));
    }

    // Test the removeMemberFromCircle method with member already in circle - no exceptions
    @Test
    public void testRemoveMemberFromCircleWithMemberInCircle() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenReturn(true);

        User member = new User(); // Create a user object for the member
        member.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(member));

        User owner = new User(); // Create a user object for the owner
        owner.setUserId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        members.add(user1);
        User user2 = new User();
        user2.setUserId(userId);
        members.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(new ArrayList<>());
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));

        circleService.setRemoveMemberValidationHandler(validationHandler);
        // Perform the method call
        BookshelfBookshelfIdCircleDelete200Response response = circleService.removeMemberFromCircle(bookshelfId, ownerId, userId);

        // Assertions to verify the response or changes made
        assertNotNull(response);
        assertEquals(bookshelfId, response.getBookshelfId());
        assertTrue(response.getUserIds().contains(user1.getUserId()));
        assertFalse(response.getUserIds().contains(userId));
        // Verify if save was called on the repository
        verify(bookshelfRepository, times(1)).save(existingBookshelf);
    }

    // Test the removeMemberFromCircle method with null bookshelf id mutation - validation exception
    @Test
    public void testRemoveMemberFromCircleNullBookshelfIdMutation() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(null, ownerId, userId)).thenReturn(true);

        circleService.setRemoveMemberValidationHandler(validationHandler);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.removeMemberFromCircle(null, ownerId, userId));
        assertThat(exception.getMessage()).isEqualTo("Bookshelf not found");
    }

    // Test the removeMemberFromCircle method with null member id mutation - validation exception
    @Test
    public void testRemoveMemberFromCircleNullMemberIdMutation() throws ValidationException {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, null)).thenReturn(true);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));

        circleService.setRemoveMemberValidationHandler(validationHandler);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.removeMemberFromCircle(bookshelfId, ownerId, null));
        assertThat(exception.getMessage()).isEqualTo("User not found");
    }

    // Test the getMember method - no exceptions
    @Test
    public void testGetMemberSuccess() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(userId);
        members.add(user1);
        User user2 = new User();
        UUID user2Id = UUID.randomUUID();
        user2.setUserId(user2Id);
        members.add(user2);
        existingBookshelf.setMembers(members);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));

        List<UUID> actual = circleService.getMembers(bookshelfId);
        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertThat(actual).containsExactlyInAnyOrder(userId, user2Id);
    }

    // Test the getMember method with null bookshelf id - validation exception
    @Test
    public void testGetMemberNullBookshelfId() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.getMembers(null));
        assertEquals("Bookshelf not found", exception.getMessage());
    }

    // Test the getMember method with bookshelf id not found - validation exception
    @Test
    public void testGetMemberBookshelfNotFound() {
        when(bookshelfRepository.findById(bookshelfId)).thenThrow(new ValidationException("Bookshelf not found"));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.getMembers(bookshelfId));
        assertEquals("Bookshelf not found", exception.getMessage());
    }

    // Test the requestToJoinCircle method - no exceptions
    @Test
    public void testRequestToJoinCircleSuccess() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User member = new User();
        member.setUserId(userId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        members.add(user1);
        List<User> pendingMembers = new ArrayList<>();
        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        pendingMembers.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(pendingMembers);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(userRepository.findById(userId)).thenReturn(Optional.of(member));
        when(validationHandler.handle(bookshelfId, null, userId)).thenReturn(true);
        circleService.setRequestToJoinValidationHandler(validationHandler);
        circleService.requestToJoinCircle(bookshelfId, userId);
        verify(bookshelfRepository, times(1)).save(existingBookshelf);
        assertThat(existingBookshelf.getPendingMembers()).containsExactlyInAnyOrder(user2, member);
        assertThat(existingBookshelf.getMembers()).containsExactlyInAnyOrder(user1);
    }

    // Test the requestToJoinCircle method with null bookshelf id - validation exception
    @Test
    public void testRequestToJoinCircleNullBookshelfId() throws ValidationException {
        when(validationHandler.handle(null, null, userId))
                .thenThrow(new ValidationException("Bookshelf id cannot be null"));
        circleService.setRequestToJoinValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.requestToJoinCircle(null, userId));
        assertEquals("Bookshelf id cannot be null", exception.getMessage());
    }

    // Test the requestToJoinCircle method with null member id - validation exception
    @Test
    public void testRequestToJoinCircleNullMemberId() throws ValidationException {
        when(validationHandler.handle(bookshelfId, null, null))
                .thenThrow(new ValidationException("User id cannot be null"));
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        circleService.setRequestToJoinValidationHandler(validationHandler);
        ValidationException e = assertThrows(ValidationException.class,
                () -> circleService.requestToJoinCircle(bookshelfId, null));
        assertEquals("User id cannot be null", e.getMessage());
    }

    // Test the requestToJoinCircle method with bookshelf id not found - validation exception
    @Test
    public void testRequestToJoinCircleBookshelfNotFound() throws ValidationException {
        when(validationHandler.handle(bookshelfId, null, userId))
                .thenThrow(new ValidationException("Bookshelf not found"));
        when(bookshelfRepository.findById(bookshelfId)).thenThrow(new ValidationException("Bookshelf not found"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        circleService.setRequestToJoinValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.requestToJoinCircle(bookshelfId, userId));
        assertEquals("Bookshelf not found", exception.getMessage());
    }

    // Test the requestToJoinCircle method with member id not found - validation exception
    @Test
    public void testRequestToJoinCircleMemberNotFound() throws ValidationException {
        when(validationHandler.handle(bookshelfId, null, userId))
                .thenThrow(new ValidationException("User not found"));
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));
        when(userRepository.findById(userId)).thenThrow(new ValidationException("User not found"));
        circleService.setRequestToJoinValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.requestToJoinCircle(bookshelfId, userId));
        assertEquals("User not found", exception.getMessage());
    }

    // Test the requestToJoinCircle method with member already in pending list - user already in collection exception
    @Test
    public void testRequestToJoinCircleMemberAlreadyInPendingList() throws ValidationException {
        when(validationHandler.handle(bookshelfId, null, userId))
                .thenThrow(new ValidationException("User already requested to join circle"));
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        circleService.setRequestToJoinValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.requestToJoinCircle(bookshelfId, userId));
        assertEquals("User already requested to join circle", exception.getMessage());
    }

    // Test the requestToJoinCircle method with member already in member list not in pending list
    // - user already in collection exception
    @Test
    public void testRequestToJoinCircleMemberAlreadyInMemberListButNotInPendingList() throws ValidationException {
        when(validationHandler.handle(bookshelfId, null, userId))
                .thenThrow(new ValidationException("User already in circle"));
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        circleService.setRequestToJoinValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.requestToJoinCircle(bookshelfId, userId));
        assertEquals("User already in circle", exception.getMessage());
    }

    // Test the requestToJoinCircle method with null bookshelf id mutation - validation exception
    @Test
    public void testRequestToJoinCircleNullBookshelfIdMutation() throws ValidationException {
        when(validationHandler.handle(null, null, userId))
                .thenReturn(true);
        circleService.setRequestToJoinValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.requestToJoinCircle(bookshelfId, userId));
        assertEquals("Bookshelf not found", exception.getMessage());
    }

    // Test the requestToJoinCircle method with null user id mutation - validation exception
    @Test
    public void testRequestToJoinCircleNullUserIdMutation() throws ValidationException {
        when(validationHandler.handle(bookshelfId, null, null))
                .thenReturn(true);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));
        circleService.setRequestToJoinValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.requestToJoinCircle(bookshelfId, null));
        assertEquals("User not found", exception.getMessage());
    }

    // Test the getPendingMembers method - no exceptions
    @Test
    public void testGetPendingMembersSuccess() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(ownerId);
        List<User> pendingMembers = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        pendingMembers.add(user1);
        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        pendingMembers.add(user2);
        existingBookshelf.setPendingMembers(pendingMembers);
        existingBookshelf.setOwner(owner);
        when(validationHandler.handle(bookshelfId, ownerId, null)).thenReturn(true);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        circleService.setGetPendingMembersValidationHandler(validationHandler);
        List<UUID> actual = circleService.getPendingMembers(bookshelfId, ownerId);
        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertThat(actual).containsExactlyInAnyOrder(user1.getUserId(), user2.getUserId());
    }

    // Test the getPendingMembers method with null bookshelf id - validation exception
    @Test
    public void testGetPendingMembersNullBookshelfId() throws ValidationException {
        when(validationHandler.handle(null, ownerId, null))
                .thenThrow(new ValidationException("Bookshelf id cannot be null"));
        circleService.setGetPendingMembersValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.getPendingMembers(null, ownerId));
        assertEquals("Bookshelf id cannot be null", exception.getMessage());
    }

    // Test the getPendingMembers method with null owner id - validation exception
    @Test
    public void testGetPendingMembersNullOwnerId() throws ValidationException {
        when(validationHandler.handle(bookshelfId, null, null))
                .thenThrow(new ValidationException("Owner id cannot be null"));
        circleService.setGetPendingMembersValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.getPendingMembers(bookshelfId, null));
        assertEquals("Owner id cannot be null", exception.getMessage());
    }

    // Test the getPendingMembers method with bookshelf id not found - validation exception
    @Test
    public void testGetPendingMembersBookshelfNotFound() throws ValidationException {
        when(validationHandler.handle(bookshelfId, ownerId, null))
                .thenThrow(new ValidationException("Bookshelf not found"));
        when(bookshelfRepository.findById(bookshelfId)).thenThrow(new ValidationException("Bookshelf not found"));
        circleService.setGetPendingMembersValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.getPendingMembers(bookshelfId, ownerId));
        assertEquals("Bookshelf not found", exception.getMessage());
    }

    // Test the getPendingMembers method with owner id not found - validation exception
    @Test
    public void testGetPendingMembersOwnerNotFound() throws ValidationException {
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));
        when(validationHandler.handle(bookshelfId, ownerId, null))
                .thenThrow(new ValidationException("Owner not found"));
        circleService.setGetPendingMembersValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.getPendingMembers(bookshelfId, ownerId));
        assertEquals("Owner not found", exception.getMessage());
    }

    // Test the getPendingMembers method with owner mismatch - validation exception
    @Test
    public void testGetPendingMembersOwnerMismatch() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(UUID.randomUUID());
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(validationHandler.handle(bookshelfId, ownerId, null))
                .thenThrow(new ValidationException("User does not match the bookshelf's owner"));
        circleService.setGetPendingMembersValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.getPendingMembers(bookshelfId, ownerId));
        assertEquals("User does not match the bookshelf's owner", exception.getMessage());
    }

    // Test the getPendingMembers method with bookshelf id mutation
    @Test
    public void testGetPendingMembersNullBookshelfIdMutation() throws ValidationException {
        when(validationHandler.handle(null, ownerId, null))
                .thenReturn(true);
        circleService.setGetPendingMembersValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.getPendingMembers(null, ownerId));
        assertEquals("Bookshelf not found", exception.getMessage());
    }

    // Test the acceptPendingMember method - no exceptions
    @Test
    public void testAcceptPendingMemberSuccess() throws Exception {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(ownerId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        members.add(user1);
        List<User> pendingMembers = new ArrayList<>();
        User user2 = new User();
        user2.setUserId(userId);
        pendingMembers.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(pendingMembers);
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user2));
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenReturn(true);
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        List<UUID> actual = circleService.acceptPendingMember(bookshelfId, ownerId, userId);
        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertThat(actual).containsExactlyInAnyOrder(user1.getUserId(), userId);
        assertThat(existingBookshelf.getMembers()).containsExactlyInAnyOrder(user1, user2);
        assertThat(existingBookshelf.getPendingMembers()).isEmpty();
        verify(bookshelfRepository, times(1)).save(existingBookshelf);
        verify(bookshelfService).addBookWrapperMultiple(userId, existingBookshelf.getBooks());
    }

    // Test the acceptPendingMember method with null bookshelf id - validation exception
    @Test
    public void testAcceptPendingMemberNullBookshelfId() throws ValidationException {
        when(validationHandler.handle(null, ownerId, userId)).thenThrow(new ValidationException("Bookshelf id cannot be null"));
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(null, ownerId, userId));
        assertEquals("Bookshelf id cannot be null", exception.getMessage());
    }

    // Test the acceptPendingMember method with null owner id - validation exception
    @Test
    public void testAcceptPendingMemberNullOwnerId() throws ValidationException {
        when(validationHandler.handle(bookshelfId, null, userId)).thenThrow(new ValidationException("Owner id cannot be null"));
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(bookshelfId, null, userId));
        assertEquals("Owner id cannot be null", exception.getMessage());
    }

    // Test the acceptPendingMember method with null member id - validation exception
    @Test
    public void testAcceptPendingMemberNullMemberId() throws ValidationException {
        when(validationHandler.handle(bookshelfId, ownerId, null)).thenThrow(new ValidationException("User id cannot be null"));
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(bookshelfId, ownerId, null));
        assertEquals("User id cannot be null", exception.getMessage());
    }

    // Test the acceptPendingMember method with bookshelf id not found - validation exception
    @Test
    public void testAcceptPendingMemberBookshelfNotFound() throws ValidationException {
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("Bookshelf not found"));
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(bookshelfId, ownerId, userId));
        assertEquals("Bookshelf not found", exception.getMessage());
    }

    // Test the acceptPendingMember method with owner id not found - validation exception
    @Test
    public void testAcceptPendingMemberOwnerNotFound() throws ValidationException {

        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("Owner not found"));
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(bookshelfId, ownerId, userId));
        assertEquals("Owner not found", exception.getMessage());
    }

    // Test the acceptPendingMember method with member id not found - validation exception
    @Test
    public void testAcceptPendingMemberMemberNotFound() throws ValidationException {
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("User not found"));
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(bookshelfId, ownerId, userId));
        assertEquals("User not found", exception.getMessage());
    }

    // Test the acceptPendingMember method with owner mismatch - validation exception
    @Test
    public void testAcceptPendingMemberOwnerMismatch() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(UUID.randomUUID());
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("User does not match the bookshelf's owner"));
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(bookshelfId, ownerId, userId));
        assertEquals("User does not match the bookshelf's owner", exception.getMessage());
    }

    // Test the acceptPendingMember method with member not in pending list
    // - user not in collection exception
    @Test
    public void testAcceptPendingMemberMemberNotInPendingList() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(ownerId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        members.add(user1);
        List<User> pendingMembers = new ArrayList<>();
        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        pendingMembers.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(pendingMembers);
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(validationHandler.handle(bookshelfId, ownerId, userId))
                .thenThrow(new ValidationException("User is not a pending member"));
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(bookshelfId, ownerId, userId));
        assertEquals("User is not a pending member", exception.getMessage());
    }

    // Test the acceptPendingMember method with member already in member list
    // - user already in collection exception
    @Test
    public void testAcceptPendingMemberMemberAlreadyInMemberList() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(ownerId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(userId);
        members.add(user1);
        List<User> pendingMembers = new ArrayList<>();
        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        pendingMembers.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(pendingMembers);
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(validationHandler.handle(bookshelfId, ownerId, userId))
                .thenThrow(new ValidationException("User already in circle"));
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(bookshelfId, ownerId, userId));
        assertEquals("User already in circle", exception.getMessage());
    }

    // Test the acceptPendingMember method with member already in both lists
    // - user already in collection exception
    @Test
    public void testAcceptPendingMemberMemberAlreadyInBothList() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(ownerId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(userId);
        members.add(user1);
        List<User> pendingMembers = new ArrayList<>();
        User user2 = new User();
        user2.setUserId(userId);
        pendingMembers.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(pendingMembers);
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(validationHandler.handle(bookshelfId, ownerId, userId))
                .thenThrow(new ValidationException("User already in circle"));
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(bookshelfId, ownerId, userId));
        assertEquals("User already in circle", exception.getMessage());
    }

    // Test the acceptPendingMember method null bookshelf id mutation - validation exception
    @Test
    public void testAcceptPendingMemberNullBookshelfIdMutation() throws ValidationException {
        when(validationHandler.handle(null, ownerId, userId)).thenReturn(true);
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(null, ownerId, userId));
        assertEquals("Bookshelf not found", exception.getMessage());
    }

    // Test the acceptPendingMember method null pending member id mutation - validation exception
    @Test
    public void testAcceptPendingMemberNullPendingMemberIdMutation() throws ValidationException {
        when(validationHandler.handle(bookshelfId, ownerId, null)).thenReturn(true);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));
        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.acceptPendingMember(bookshelfId, ownerId, null));
        assertEquals("User not found", exception.getMessage());
    }

    // Test the rejectPendingMember method - no exceptions
    @Test
    public void testRejectPendingMemberSuccess() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(ownerId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        members.add(user1);
        List<User> pendingMembers = new ArrayList<>();
        User user2 = new User();
        user2.setUserId(userId);
        pendingMembers.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(pendingMembers);
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user2));
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenReturn(true);
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        circleService.rejectPendingMember(bookshelfId, ownerId, userId);
        assertThat(existingBookshelf.getMembers()).containsExactlyInAnyOrder(user1);
        assertThat(existingBookshelf.getPendingMembers()).isEmpty();
        verify(bookshelfRepository, times(1)).save(existingBookshelf);
    }

    // Test the rejectPendingMember method with null bookshelf id - validation exception
    @Test
    public void testRejectPendingMemberNullBookshelfId() throws ValidationException {
        when(validationHandler.handle(null, ownerId, userId)).thenThrow(new ValidationException("Bookshelf id cannot be null"));
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.rejectPendingMember(null, ownerId, userId));
        assertEquals("Bookshelf id cannot be null", exception.getMessage());
    }

    // Test the rejectPendingMember method with null owner id - validation exception
    @Test
    public void testRejectPendingMemberNullOwnerId() throws ValidationException {
        when(validationHandler.handle(bookshelfId, null, userId)).thenThrow(new ValidationException("Owner id cannot be null"));
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.rejectPendingMember(bookshelfId, null, userId));
        assertEquals("Owner id cannot be null", exception.getMessage());
    }

    // Test the rejectPendingMember method with null member id - validation exception
    @Test
    public void testRejectPendingMemberNullMemberId() throws ValidationException {
        when(validationHandler.handle(bookshelfId, ownerId, null)).thenThrow(new ValidationException("User id cannot be null"));
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.rejectPendingMember(bookshelfId, ownerId, null));
        assertEquals("User id cannot be null", exception.getMessage());
    }

    // Test the rejectPendingMember method with bookshelf id not found - validation exception
    @Test
    public void testRejectPendingMemberBookshelfNotFound() throws ValidationException {
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("Bookshelf not found"));
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> circleService.rejectPendingMember(bookshelfId, ownerId, userId));
        assertEquals("Bookshelf not found", exception.getMessage());
    }

    // Test the rejectPendingMember method with owner id not found - validation exception
    @Test
    public void testRejectPendingMemberOwnerNotFound() throws ValidationException {
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("Owner not found"));
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        ValidationException e = assertThrows(ValidationException.class,
                () -> circleService.rejectPendingMember(bookshelfId, ownerId, userId));
        assertEquals("Owner not found", e.getMessage());
    }

    // Test the rejectPendingMember method with member id not found - validation exception
    @Test
    public void testRejectPendingMemberMemberNotFound() throws ValidationException {
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("User not found"));
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        ValidationException e = assertThrows(ValidationException.class,
                () -> circleService.rejectPendingMember(bookshelfId, ownerId, userId));
        assertEquals("User not found", e.getMessage());
    }

    // Test the rejectPendingMember method with owner mismatch - validation exception
    @Test
    public void testRejectPendingMemberOwnerMismatch() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(UUID.randomUUID());
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenThrow(new ValidationException("User does not match the bookshelf's owner"));
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        ValidationException e = assertThrows(ValidationException.class,
                () -> circleService.rejectPendingMember(bookshelfId, ownerId, userId));
        assertEquals("User does not match the bookshelf's owner", e.getMessage());
    }

    // Test the rejectPendingMember method with member not in pending list
    @Test
    public void testRejectPendingMemberMemberNotInPendingList() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(ownerId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        members.add(user1);
        List<User> pendingMembers = new ArrayList<>();
        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        pendingMembers.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(pendingMembers);
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(validationHandler.handle(bookshelfId, ownerId, userId))
                .thenThrow(new ValidationException("User is not a pending member"));
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        ValidationException e = assertThrows(ValidationException.class,
                () -> circleService.rejectPendingMember(bookshelfId, ownerId, userId));
        assertEquals("User is not a pending member", e.getMessage());
    }

    // Test the rejectPendingMember method with member already in member list
    @Test
    public void testRejectPendingMemberMemberAlreadyInBothList() throws ValidationException {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(ownerId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(userId);
        members.add(user1);
        List<User> pendingMembers = new ArrayList<>();
        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        pendingMembers.add(user2);
        pendingMembers.add(user1);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(pendingMembers);
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(validationHandler.handle(bookshelfId, ownerId, userId))
                .thenReturn(true);
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        circleService.rejectPendingMember(bookshelfId, ownerId, userId);
        assertThat(existingBookshelf.getMembers()).containsExactlyInAnyOrder(user1);
        assertThat(existingBookshelf.getPendingMembers()).containsExactlyInAnyOrder(user2);
        verify(bookshelfRepository, times(1)).save(existingBookshelf);
    }

    // Test the rejectPendingMember method null bookshelf id mutation - validation exception
    @Test
    public void testRejectPendingMemberNullBookshelfIdMutation() throws ValidationException {
        when(validationHandler.handle(null, ownerId, userId)).thenReturn(true);
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        ValidationException e = assertThrows(ValidationException.class,
                () -> circleService.rejectPendingMember(null, ownerId, userId));
        assertEquals("Bookshelf not found", e.getMessage());
    }

    // Test the rejectPendingMember method null pending member id mutation - validation exception
    @Test
    public void testRejectPendingMemberNullPendingMemberIdMutation() throws ValidationException {
        when(validationHandler.handle(bookshelfId, ownerId, null)).thenReturn(true);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(new Bookshelf()));
        circleService.setRejectPendingMemberValidationHandler(validationHandler);
        ValidationException e = assertThrows(ValidationException.class,
                () -> circleService.rejectPendingMember(bookshelfId, ownerId, null));
        assertEquals("User not found", e.getMessage());
    }

    @Test
    public void testAddMemberToCircleFailBookWrapper() throws Exception {
        // Mock the validation handler
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenReturn(true);

        User member = new User(); // Create a user object for the member
        member.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(member));

        User owner = new User(); // Create a user object for the owner
        owner.setUserId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        existingBookshelf.setMembers(new ArrayList<>());
        existingBookshelf.setPendingMembers(new ArrayList<>());
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        doThrow(new Exception("something wrong in creating wrappers"))
                .when(bookshelfService).addBookWrapperMultiple(userId, existingBookshelf.getBooks());

        circleService.setAddMemberValidationHandler(validationHandler);
        // Perform the method call
        assertThatThrownBy(() -> circleService.addMemberToCircle(bookshelfId, ownerId, userId))
                .isInstanceOf(Exception.class);
    }

    @Test
    public void testRemoveMemberFromCircleFailBookWrapper() throws Exception {
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenReturn(true);

        User member = new User(); // Create a user object for the member
        member.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(member));

        User owner = new User(); // Create a user object for the owner
        owner.setUserId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        List<User> members = new ArrayList<>();
        members.add(member);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(new ArrayList<>());
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        doThrow(new Exception("something wrong in creating wrappers"))
                .when(bookshelfService).deleteBookWrapperMultiple(userId, existingBookshelf.getBooks());

        circleService.setRemoveMemberValidationHandler(validationHandler);
        // Perform the method call
        assertThatThrownBy(() -> circleService.removeMemberFromCircle(bookshelfId, ownerId, userId))
                .isInstanceOf(Exception.class);
    }

    @Test
    public void testAcceptPendingMemberFailBookWrapper() throws Exception {
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        User owner = new User();
        owner.setUserId(ownerId);
        List<User> members = new ArrayList<>();
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        members.add(user1);
        List<User> pendingMembers = new ArrayList<>();
        User user2 = new User();
        user2.setUserId(userId);
        pendingMembers.add(user2);
        existingBookshelf.setMembers(members);
        existingBookshelf.setPendingMembers(pendingMembers);
        existingBookshelf.setOwner(owner);
        when(bookshelfRepository.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user2));
        when(validationHandler.handle(bookshelfId, ownerId, userId)).thenReturn(true);
        doThrow(new Exception("something wrong in creating wrappers"))
                .when(bookshelfService).addBookWrapperMultiple(userId, existingBookshelf.getBooks());

        circleService.setAcceptPendingMemberValidationHandler(validationHandler);
        assertThatThrownBy(() -> circleService.acceptPendingMember(bookshelfId, ownerId, userId))
                .isInstanceOf(Exception.class);
    }
}