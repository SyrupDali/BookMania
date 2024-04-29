package nl.tudelft.sem.template.example.services;

import javassist.NotFoundException;
import nl.tudelft.sem.template.example.database.*;
import nl.tudelft.sem.template.example.entities.BookWrapperId;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.*;
import nl.tudelft.sem.template.example.validators.BaseValidator;
import nl.tudelft.sem.template.example.validators.Validator;
import nl.tudelft.sem.template.model.Book;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookshelfServiceTest {

    private BookshelfService bookshelfService;
    private TestBookshelfRepository testBookshelfRepo;
    private BookshelfRepository bookshelfRepository;
    private TestBookWrapperRepository bookWrapperRepo;
    private UtilityService util;

    private final UserService userServiceMock = mock(UserService.class);
    private final BookshelfRepository bookshelfRepositoryMock = mock(BookshelfRepository.class);
    private final BookWrapperRepository bookWrapperRepositoryMock = mock(BookWrapperRepository.class);
    private final BookRepository bookRepositoryMock = mock(BookRepository.class);

    private User owner;
    private Bookshelf existingBookshelf;
    private Book testBook;
    private Book testBook2;
    private UUID randomUserId;
    private UUID randomBookshelfId;

    private UUID randomBookId;

    @BeforeEach
    public void setUp() {
        TestBookRepository bookRepo = new TestBookRepository();
        testBookshelfRepo = new TestBookshelfRepository();
        TestUserRepository userRepo = new TestUserRepository();
        TestCategoryRepository categoryRepo = new TestCategoryRepository();

        CategoryService categoryService = new CategoryService(categoryRepo, userRepo, testBookshelfRepo);

        bookWrapperRepo = new TestBookWrapperRepository();

        util = mock(UtilityService.class);
        when(util.validId(any())).thenReturn(true);

        UserService userService = new UserService(userRepo, testBookshelfRepo, bookRepo, bookWrapperRepo, categoryService, util);
        bookshelfService = new BookshelfService(testBookshelfRepo, bookRepo, userService, bookWrapperRepo);


        owner = new User(UUID.randomUUID());

        // Create a bookshelf and add it to the repository
        UUID existingBookshelfId = UUID.randomUUID();
        existingBookshelf = new Bookshelf()
                .bookshelfId(existingBookshelfId)
                .owner(owner)
                .title("Test Bookshelf")
                .description("A test bookshelf")
                .books(new ArrayList<>())
                .privacy(Bookshelf.PrivacyEnum.PUBLIC)
                .members(new ArrayList<>())
                .pendingMembers(new ArrayList<>());

        testBookshelfRepo.save(existingBookshelf);

        // Save the existing user to the repository
        userRepo.save(owner);

        randomUserId = UUID.randomUUID();
        User randomUser = new User(randomUserId);
        userRepo.save(randomUser);

        // Create books and add them to the repository
        UUID existingBookId = UUID.randomUUID();
        testBook = new Book()
                .bookId(existingBookId)
                .title("Existing Book");
        UUID testBook2Id = UUID.randomUUID();
        testBook2 = new Book()
                .bookId(testBook2Id)
                .title("Second Existing Book");

        // Save the existing books to the repository
        bookRepo.save(testBook);
        bookRepo.save(testBook2);

        randomBookshelfId = UUID.randomUUID();
        bookshelfRepository = mock(BookshelfRepository.class);

        randomBookId = UUID.randomUUID();
    }

    /**
     * Creates a bookshelfPostRequest for the creation of bookshelf
     *
     * @param title       the title the user wants to name the new bookshelf
     * @param description the description the user wants to have for the new bookshelf
     * @param privacy     the privacy the user wants to set for the new bookshelf
     * @return a BookshelfPostRequest with those details
     */
    public BookshelfPostRequest getPostBookshelfRequest(String title, String description, String privacy) {
        BookshelfPostRequest request = new BookshelfPostRequest();
        request.setTitle(title);
        request.setDescription(description);
        if (privacy == null) {
            request.setPrivacy(null);
        } else if (privacy.equals("PUBLIC")) {
            request.setPrivacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);
        } else {
            request.setPrivacy(BookshelfPostRequest.PrivacyEnum.PRIVATE);
        }
        return request;
    }

    public BookshelfBookshelfIdPutRequest getPutBookshelfRequest(String title, String description, String privacy) {
        BookshelfBookshelfIdPutRequest request = new BookshelfBookshelfIdPutRequest();
        request.setTitle(title);
        request.setDescription(description);
        if (privacy == null) {
            request.setPrivacy(null);
        } else if (privacy.equals("PUBLIC")) {
            request.setPrivacy(BookshelfBookshelfIdPutRequest.PrivacyEnum.PUBLIC);
        } else {
            request.setPrivacy(BookshelfBookshelfIdPutRequest.PrivacyEnum.PRIVATE);
        }
        return request;
    }

    @Test
    public void getBookshelfException() {
        UUID nonExistingBookshelfId = UUID.fromString("b123e456-7890-1234-5678-9abcdef01234");

        Exception exception = assertThrows(NotFoundException.class, () ->
                bookshelfService.getBookshelfById(nonExistingBookshelfId));

        assertEquals("Bookshelf not found with ID: " + nonExistingBookshelfId, exception.getMessage());
    }

    @Test
    public void getBookshelfCorrect() throws NotFoundException {
        // Call the method and check if it returns the correct bookshelf
        Bookshelf retrievedBookshelf = bookshelfService.getBookshelfById(existingBookshelf.getBookshelfId());

        // Assert that the retrieved bookshelf is not null and has the correct attributes
        assertNotNull(retrievedBookshelf);
        assertEquals(existingBookshelf.getBookshelfId(), retrievedBookshelf.getBookshelfId());
        assertEquals(owner, retrievedBookshelf.getOwner());
        assertEquals("Test Bookshelf", retrievedBookshelf.getTitle());
        assertEquals("A test bookshelf", retrievedBookshelf.getDescription());
        assertEquals(Bookshelf.PrivacyEnum.PUBLIC, retrievedBookshelf.getPrivacy());
    }

    @Test
    public void getAllPublicBookshelves() {
        List<Bookshelf> expectedResult = new ArrayList<>();
        expectedResult.add(existingBookshelf);
        List<Bookshelf> result = bookshelfService.getAllPublicBookshelves();

        assertEquals(result.size(), expectedResult.size());
        assertEquals(result, expectedResult);
    }

    @Test
    public void getAllPublicBookshelvesMix() {
        //Create a private bookshelf
        Bookshelf bookshelf2 = new Bookshelf()
                .bookshelfId(randomBookshelfId)
                .owner(owner)
                .title("New Bookshelf")
                .description("A new bookshelf")
                .books(new ArrayList<>())
                .privacy(Bookshelf.PrivacyEnum.PRIVATE)
                .members(new ArrayList<>())
                .pendingMembers(new ArrayList<>());
        testBookshelfRepo.save(bookshelf2);

        List<Bookshelf> expectedResult = new ArrayList<>();
        expectedResult.add(existingBookshelf);
        List<Bookshelf> result = bookshelfService.getAllPublicBookshelves();

        assertEquals(result.size(), expectedResult.size());
        assertEquals(result, expectedResult);
    }


    @Test
    public void addBookshelfPublicSuccessfully() throws Exception {
        BookshelfPostRequest request = getPostBookshelfRequest("New Test Bookshelf", "A new test bookshelf", "PUBLIC");
        Bookshelf createdBookshelf = bookshelfService.addBookshelf(request, owner.getUserId());
        UUID createdBookshelfID = createdBookshelf.getBookshelfId();
        Bookshelf repoBookshelf = testBookshelfRepo.findById(createdBookshelfID).get();

        assertEquals("New Test Bookshelf", createdBookshelf.getTitle());
        assertEquals("A new test bookshelf", createdBookshelf.getDescription());
        assertEquals("PUBLIC", createdBookshelf.getPrivacy().getValue());

        assertNotNull(repoBookshelf);
        assertEquals("New Test Bookshelf", repoBookshelf.getTitle());
        assertEquals("A new test bookshelf", repoBookshelf.getDescription());
        assertEquals("PUBLIC", repoBookshelf.getPrivacy().getValue());

        //Checks that the bookshelf has been added to the repository
        assertEquals(2, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void addBookshelfPrivateSuccessfully() throws Exception {
        BookshelfPostRequest request = getPostBookshelfRequest("New Test Bookshelf", "A new test bookshelf", "PRIVATE");
        Bookshelf createdBookshelf = bookshelfService.addBookshelf(request, owner.getUserId());
        UUID createdBookshelfID = createdBookshelf.getBookshelfId();
        Bookshelf repoBookshelf = testBookshelfRepo.findById(createdBookshelfID).get();

        assertEquals("New Test Bookshelf", createdBookshelf.getTitle());
        assertEquals("A new test bookshelf", createdBookshelf.getDescription());
        assertEquals("PRIVATE", createdBookshelf.getPrivacy().getValue());

        assertNotNull(repoBookshelf);
        assertEquals("New Test Bookshelf", repoBookshelf.getTitle());
        assertEquals("A new test bookshelf", repoBookshelf.getDescription());
        assertEquals("PRIVATE", repoBookshelf.getPrivacy().getValue());
        assertEquals(repoBookshelf.getOwner().getUserId(), owner.getUserId());

        //Checks that the bookshelf has been added to the repository
        assertEquals(2, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void addBookshelfUserNonexistent() {
        UUID nonExistentUser = UUID.randomUUID();
        BookshelfPostRequest request = getPostBookshelfRequest("New Test Bookshelf", "A new test bookshelf", "PUBLIC");
        assertThatThrownBy(() -> bookshelfService.addBookshelf(request, nonExistentUser))
                .isInstanceOf(IllegalArgumentException.class);

        //Checks that the bookshelf has not been added to the repository
        assertEquals(1, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void addBookshelfBadRequestNullTitle() {
        BookshelfPostRequest request = getPostBookshelfRequest(null, "A new test bookshelf", "PUBLIC");
        assertThatThrownBy(() -> bookshelfService.addBookshelf(request, owner.getUserId()))
                .isInstanceOf(IllegalArgumentException.class);

        //Checks that the bookshelf has not been added to the repository
        assertEquals(1, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void addBookshelfBadRequestEmptyTitle() {
        BookshelfPostRequest request = getPostBookshelfRequest("", "A new test bookshelf", "PUBLIC");
        assertThatThrownBy(() -> bookshelfService.addBookshelf(request, owner.getUserId()))
                .isInstanceOf(IllegalArgumentException.class);

        //Checks that the bookshelf has not been added to the repository
        assertEquals(1, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void addBookshelfBadRequestNullDescription() {
        BookshelfPostRequest request = getPostBookshelfRequest("New Test Bookshelf", null, "PUBLIC");
        assertThatThrownBy(() -> bookshelfService.addBookshelf(request, owner.getUserId()))
                .isInstanceOf(IllegalArgumentException.class);

        //Checks that the bookshelf has not been added to the repository
        assertEquals(1, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void addBookshelfBadRequestEmptyDescription() {
        BookshelfPostRequest request = getPostBookshelfRequest("New Test Bookshelf", "", "PUBLIC");
        assertThatThrownBy(() -> bookshelfService.addBookshelf(request, owner.getUserId()))
                .isInstanceOf(IllegalArgumentException.class);

        //Checks that the bookshelf has not been added to the repository
        assertEquals(1, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void addBookshelfBadRequestNullPrivacy() {
        BookshelfPostRequest request = getPostBookshelfRequest("New Test Bookshelf", "A new test bookshelf", null);
        assertThatThrownBy(() -> bookshelfService.addBookshelf(request, owner.getUserId()))
                .isInstanceOf(IllegalArgumentException.class);

        //Checks that the bookshelf has not been added to the repository
        assertEquals(1, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void deleteBookshelfSuccessfully() throws Exception {
        Bookshelf deletedBookshelf = bookshelfService.deleteBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId());

        assertNotNull(deletedBookshelf);
        assertEquals(deletedBookshelf, existingBookshelf);

        //Checks that the bookshelf has been deleted from the repository
        assertEquals(0, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void deleteBookshelfWithBookSuccessfully() throws Exception{
        existingBookshelf.addBooksItem(testBook);
        BookWrapper bookWrapper = new BookWrapper();
        bookWrapper.setBookId(testBook.getBookId());
        bookWrapper.setUserId(owner.getUserId());
        bookWrapperRepo.save(bookWrapper);

        Bookshelf deletedBookshelf = bookshelfService.deleteBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId());

        assertEquals(deletedBookshelf, existingBookshelf);

        //Checks that the bookshelf has been deleted from the repository
        assertEquals(0, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void deleteBookshelfBadRequestBookshelfIdNull() {
        assertThatThrownBy(() -> bookshelfService.deleteBookshelf(null, owner.getUserId()))
                .isInstanceOf(IllegalArgumentException.class);

        //Checks that the bookshelf has not been deleted from the repository
        assertEquals(1, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void deleteBookshelfBadRequestUserIdNull() {
        assertThatThrownBy(() -> bookshelfService.deleteBookshelf(existingBookshelf.getBookshelfId(), null))
                .isInstanceOf(IllegalArgumentException.class);

        //Checks that the bookshelf has not been deleted
        assertEquals(1, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void deleteBookshelfBadRequestUserNotOwner() {
        assertThatThrownBy(() -> bookshelfService.deleteBookshelf(existingBookshelf.getBookshelfId(), randomUserId))
                .isInstanceOf(IllegalArgumentException.class);

        //Checks that the bookshelf has not been deleted
        assertEquals(1, bookshelfService.getAllBookshelves().size());
    }

    @Test
    public void editBookshelfSuccessfully() {
        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test edit", "A tested edited description", "PUBLIC");
        Bookshelf b = bookshelfService.editBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), request);

        assertNotNull(b);
        //Check that there is still only 1 bookshelf in the repository, the updated one
        assertEquals(1, bookshelfService.getAllBookshelves().size());

        assertEquals("Test edit", existingBookshelf.getTitle());
        assertEquals("A tested edited description", existingBookshelf.getDescription());
        assertEquals("PUBLIC", existingBookshelf.getPrivacy().getValue());
    }

    @Test
    public void editBookshelfSuccessfully2() {
        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test edit", "A tested edited description", "PRIVATE");
        Bookshelf b = bookshelfService.editBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), request);

        assertNotNull(b);
        //Check that there is still only 1 bookshelf in the repository, the updated one
        assertEquals(1, bookshelfService.getAllBookshelves().size());

        assertEquals("Test edit", existingBookshelf.getTitle());
        assertEquals("A tested edited description", existingBookshelf.getDescription());
        assertEquals("PRIVATE", existingBookshelf.getPrivacy().getValue());
    }

    @Test
    public void editBookshelfSuccessfully3() {
        existingBookshelf.setPrivacy(Bookshelf.PrivacyEnum.PRIVATE);
        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test edit", "A tested edited description", "PRIVATE");
        Bookshelf b = bookshelfService.editBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), request);

        assertNotNull(b);
        //Check that there is still only 1 bookshelf in the repository, the updated one
        assertEquals(1, bookshelfService.getAllBookshelves().size());

        assertEquals("Test edit", existingBookshelf.getTitle());
        assertEquals("A tested edited description", existingBookshelf.getDescription());
        assertEquals("PRIVATE", existingBookshelf.getPrivacy().getValue());
    }

    @Test
    public void editBookshelfSuccessfully4() {
        existingBookshelf.setPrivacy(Bookshelf.PrivacyEnum.PRIVATE);
        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test edit", "A tested edited description", "PUBLIC");
        Bookshelf b = bookshelfService.editBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), request);

        assertNotNull(b);
        //Check that there is still only 1 bookshelf in the repository, the updated one
        assertEquals(1, bookshelfService.getAllBookshelves().size());

        assertEquals("Test edit", existingBookshelf.getTitle());
        assertEquals("A tested edited description", existingBookshelf.getDescription());
        assertEquals("PUBLIC", existingBookshelf.getPrivacy().getValue());
    }

    @Test
    public void editBookshelfNotExists() {
        when(bookshelfRepository.existsById(randomBookshelfId)).thenReturn(false);

        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test edit", "A tested edited description", "PUBLIC");
        assertThatThrownBy(() -> bookshelfService.editBookshelf(randomBookshelfId, owner.getUserId(), request))
                .isInstanceOf(ValidationException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfUserNotExists() {
        UUID nonExistentUser = UUID.randomUUID();
        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test edit", "A tested edited description", "PUBLIC");
        assertThatThrownBy(() -> bookshelfService.editBookshelf(existingBookshelf.getBookshelfId(), nonExistentUser, request))
                .isInstanceOf(ValidationException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfBadRequestNullTitle() {
        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest(null, "A tested edited description", "PUBLIC");
        assertThatThrownBy(() -> bookshelfService.editBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfBadRequestEmptyTitle() {
        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("", "A tested edited description", "PUBLIC");
        assertThatThrownBy(() -> bookshelfService.editBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfBadRequestNullDescription() {
        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test edit", null, "PUBLIC");
        assertThatThrownBy(() -> bookshelfService.editBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfBadRequestEmptyDescription() {
        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test edit", "", "PUBLIC");
        assertThatThrownBy(() -> bookshelfService.editBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfBadRequestNullPrivacy() {
        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test edit", "A tested edited bookshelf", null);
        assertThatThrownBy(() -> bookshelfService.editBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfTitleSuccessfully() {
        Bookshelf b = bookshelfService.editTitleBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), "Edited title");

        assertNotNull(b);
        //Check that there is still only 1 bookshelf in the repository, the updated one
        assertEquals(1, bookshelfService.getAllBookshelves().size());

        //Check that only the title has changed
        assertEquals("Edited title", existingBookshelf.getTitle());
        assertEquals("A test bookshelf", existingBookshelf.getDescription());
        assertEquals("PUBLIC", existingBookshelf.getPrivacy().getValue());
    }

    @Test
    public void editBookshelfTitleBookshelfNotFound() {
        when(bookshelfRepository.existsById(randomBookshelfId)).thenReturn(false);

        assertThatThrownBy(() -> bookshelfService
                .editTitleBookshelf(randomBookshelfId, owner.getUserId(), "Edited title"))
                .isInstanceOf(ValidationException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfTitleUserNotFound() {
        UUID nonExistentUser = UUID.randomUUID();
        assertThatThrownBy(() -> bookshelfService
                .editTitleBookshelf(existingBookshelf.getBookshelfId(), nonExistentUser, "Edited title"))
                .isInstanceOf(ValidationException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfTitleNull() {
        assertThatThrownBy(() -> bookshelfService
                .editTitleBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), null))
                .isInstanceOf(IllegalArgumentException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfTitleEmpty() {
        assertThatThrownBy(() -> bookshelfService
                .editTitleBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), ""))
                .isInstanceOf(IllegalArgumentException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfDescriptionSuccessfully() {
        Bookshelf b = bookshelfService.editDescriptionBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), "Edited description");

        assertNotNull(b);
        //Check that there is still only 1 bookshelf in the repository, the updated one
        assertEquals(1, bookshelfService.getAllBookshelves().size());

        //Check that only the description has changed
        assertEquals("Test Bookshelf", existingBookshelf.getTitle());
        assertEquals("Edited description", existingBookshelf.getDescription());
        assertEquals("PUBLIC", existingBookshelf.getPrivacy().getValue());
    }

    @Test
    public void editBookshelfDescriptionBookshelfNotFound() {
        when(bookshelfRepository.existsById(randomBookshelfId)).thenReturn(false);

        assertThatThrownBy(() -> bookshelfService
                .editDescriptionBookshelf(randomBookshelfId, owner.getUserId(), "Edited description"))
                .isInstanceOf(ValidationException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfDescriptionUserNotFound() {
        UUID nonExistentUser = UUID.randomUUID();
        assertThatThrownBy(() -> bookshelfService
                .editDescriptionBookshelf(existingBookshelf.getBookshelfId(), nonExistentUser, "Edited description"))
                .isInstanceOf(ValidationException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfDescriptionNull() {
        assertThatThrownBy(() -> bookshelfService
                .editDescriptionBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), null))
                .isInstanceOf(IllegalArgumentException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfDescriptionEmpty() {
        assertThatThrownBy(() -> bookshelfService
                .editDescriptionBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), ""))
                .isInstanceOf(IllegalArgumentException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfPrivacySuccessfully() {
        Bookshelf b = bookshelfService.editPrivacyBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), "PRIVATE");

        assertNotNull(b);
        //Check that there is still only 1 bookshelf in the repository, the updated one
        assertEquals(1, bookshelfService.getAllBookshelves().size());

        //Check that only the description has changed
        assertEquals("Test Bookshelf", existingBookshelf.getTitle());
        assertEquals("A test bookshelf", existingBookshelf.getDescription());
        assertEquals("PRIVATE", existingBookshelf.getPrivacy().getValue());
    }

    @Test
    public void editBookshelfPrivacySuccessfully2() {
        existingBookshelf.setPrivacy(Bookshelf.PrivacyEnum.PRIVATE);
        bookshelfService.editPrivacyBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), "PUBLIC");

        //Check that there is still only 1 bookshelf in the repository, the updated one
        assertEquals(1, bookshelfService.getAllBookshelves().size());

        //Check that only the description has changed
        assertEquals("Test Bookshelf", existingBookshelf.getTitle());
        assertEquals("A test bookshelf", existingBookshelf.getDescription());
        assertEquals("PUBLIC", existingBookshelf.getPrivacy().getValue());
    }

    @Test
    public void editBookshelfPrivacyBookshelfNotFound() {
        when(bookshelfRepository.existsById(randomBookshelfId)).thenReturn(false);

        assertThatThrownBy(() -> bookshelfService
                .editPrivacyBookshelf(randomBookshelfId, owner.getUserId(), "PRIVATE"))
                .isInstanceOf(ValidationException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfPrivacyUserNotFound() {
        UUID nonExistentUser = UUID.randomUUID();
        assertThatThrownBy(() -> bookshelfService
                .editPrivacyBookshelf(existingBookshelf.getBookshelfId(), nonExistentUser, "PRIVATE"))
                .isInstanceOf(ValidationException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfPrivacyNull() {

        assertThatThrownBy(() -> bookshelfService
                .editPrivacyBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), null))
                .isInstanceOf(IllegalArgumentException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void editBookshelfPrivacyEmpty() {

        assertThatThrownBy(() -> bookshelfService
                .editPrivacyBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), ""))
                .isInstanceOf(IllegalArgumentException.class);

        //Check that the bookshelf hasn't been updated
        verify(bookshelfRepository, never()).save(existingBookshelf);
    }

    @Test
    public void addBookToBookshelfSuccessful() throws Exception {
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        Validator validator = mock(BaseValidator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(existingBookshelf.getBookshelfId(), owner.getUserId(), owner.getUserId()))
                .thenReturn(true);
        when(bookshelfRepositoryMock.findById(existingBookshelf.getBookshelfId()))
                .thenReturn(Optional.of(existingBookshelf));
        when(bookRepositoryMock.findById(testBook.getBookId()))
                .thenReturn(Optional.of(testBook));
        User user = new User(UUID.randomUUID());
        existingBookshelf.getMembers().add(user);
        when(bookshelfRepositoryMock.findAll()).thenReturn(new ArrayList<>(List.of(existingBookshelf)));
        when(bookshelfRepositoryMock.existsById(existingBookshelf.getBookshelfId())).thenReturn(true);
        when(bookRepositoryMock.findById(testBook.getBookId())).thenReturn(Optional.of(testBook));
        when(userServiceMock.findById(owner.getUserId())).thenReturn(owner);
        when(userServiceMock.findById(user.getUserId())).thenReturn(user);
        assertEquals(0, existingBookshelf.getBooks().size());   // Check that the bookshelf is empty
        Bookshelf result = bookshelfService.addBookToBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), testBook.getBookId());
        assertEquals(1, result.getBooks().size());   // Check that the bookshelf now has 1 book
        assertThat(result.getBooks()).containsExactlyInAnyOrder(testBook);   // Check that the bookshelf contains the correct book
        verify(bookWrapperRepositoryMock, times(2)).save(Mockito.any());
    }

    @Test
    public void addBookToBookshelfBookExistsInBookshelf() {
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock,
                userServiceMock, bookWrapperRepositoryMock);
        Validator validator = mock(BaseValidator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(existingBookshelf.getBookshelfId(), owner.getUserId(), owner.getUserId()))
                .thenReturn(true);
        when(bookshelfRepositoryMock.findById(existingBookshelf.getBookshelfId()))
                .thenReturn(Optional.of(existingBookshelf));
        when(bookRepositoryMock.findById(testBook.getBookId()))
                .thenReturn(Optional.of(testBook));
        existingBookshelf.getBooks().add(testBook);
        ValidationException e = assertThrows(ValidationException.class,
                () -> bookshelfService.addBookToBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(),
                        testBook.getBookId()));
        assertEquals("Book already exists in the bookshelf", e.getMessage());
    }

    @Test
    public void addBookToBookshelfNullBookshelfMutation() {
        // Call the addBookToBookshelf method
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        Bookshelf newBookshelf = new Bookshelf();
        newBookshelf.setBookshelfId(UUID.randomUUID());
        when(bookshelfRepositoryMock.existsById(newBookshelf.getBookshelfId())).thenReturn(true);
        when(bookRepositoryMock.existsById(testBook.getBookId())).thenReturn(true);
        Validator validator = mock(BaseValidator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(existingBookshelf.getBookshelfId(), owner.getUserId(), owner.getUserId()))
                .thenReturn(true);
        when(bookshelfRepositoryMock.existsById(newBookshelf.getBookshelfId())).thenReturn(true);
        when(bookRepositoryMock.existsById(testBook.getBookId())).thenReturn(true);
        when(userServiceMock.existsById(owner.getUserId())).thenReturn(true);
        NotFoundException e = assertThrows(NotFoundException.class, () ->
                bookshelfService.addBookToBookshelf(newBookshelf.getBookshelfId(),
                        owner.getUserId(), testBook.getBookId()));
        assertEquals("Bookshelf not found", e.getMessage());
    }

    @Test
    public void addBookToBookshelfNullBookMutation() {
        // Call the addBookToBookshelf method
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookWrapperRepository bookWrapperRepository = mock(BookWrapperRepository.class);
        BookRepository bookRepository = mock(BookRepository.class);
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepository, bookRepository, userServiceMock, bookWrapperRepository);
        Book newBook = new Book();
        newBook.setBookId(UUID.randomUUID());
        Validator validator = mock(BaseValidator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(existingBookshelf.getBookshelfId(), owner.getUserId(), newBook.getBookId()))
                .thenReturn(true);

        when(bookshelfRepository.existsById(existingBookshelf.getBookshelfId())).thenReturn(true);
        when(bookRepository.existsById(newBook.getBookId())).thenReturn(true);
        when(userServiceMock.existsById(owner.getUserId())).thenReturn(true);

        when(bookshelfRepository.findById(existingBookshelf.getBookshelfId()))
                .thenReturn(Optional.of(existingBookshelf));

        NotFoundException e = assertThrows(NotFoundException.class, () ->
                bookshelfService.addBookToBookshelf(existingBookshelf.getBookshelfId(),
                        owner.getUserId(), newBook.getBookId()));
        assertEquals("Book not found", e.getMessage());
    }

    @Test
    public void removeBookToBookshelfBookWrapperRepoCapture() throws Exception {
        // Call the removeBookFromBookshelf method, capture the bookWrapperRepo.findById call
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock,
                userServiceMock, bookWrapperRepositoryMock);
        // Mock repository responses
        Bookshelf existingBookshelf = new Bookshelf();
        existingBookshelf.setBookshelfId(bookshelfId);
        existingBookshelf.setOwner(owner);
        existingBookshelf.setMembers(new ArrayList<>(List.of(new User(userId))));

        Book existingBook = new Book();
        existingBook.setBookId(bookId);
        List<Book> books = new ArrayList<>();
        books.add(existingBook);
        existingBookshelf.setBooks(books);

        BookWrapper existingBookWrapper = new BookWrapper();
        existingBookWrapper.setBookId(bookId);
        existingBookWrapper.setUserId(userId);

        when(bookshelfRepositoryMock.findById(bookshelfId)).thenReturn(Optional.of(existingBookshelf));
        when(bookRepositoryMock.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookWrapperRepositoryMock.findById(any(BookWrapperId.class))).thenReturn(Optional.of(existingBookWrapper));

        ArgumentCaptor<BookWrapperId> bookWrapperIdCaptor = ArgumentCaptor.forClass(BookWrapperId.class);
        // Mock circleValidator to not throw an exception
        Validator validator = mock(BaseValidator.class);
        when(validator.handle(bookshelfId, userId, userId)).thenReturn(true);
        bookshelfService.setCircleValidator(validator);
        assertEquals(1, existingBookshelf.getBooks().size());
        // Execute the method
        Bookshelf result = bookshelfService.removeBookFromBookshelf(bookshelfId, userId, bookId);
        assertEquals(0, result.getBooks().size());
        // Verify interactions and assertions
        verify(bookshelfRepositoryMock, times(1)).save(any(Bookshelf.class));
        verify(bookWrapperRepositoryMock, times(2)).delete(existingBookWrapper);
        verify(bookWrapperRepositoryMock, times(1)).findById(bookWrapperIdCaptor.capture());
        assertEquals(bookId, bookWrapperIdCaptor.getValue().getBookId());
        assertEquals(userId, bookWrapperIdCaptor.getValue().getUserId());
    }

    @Test
    public void removeBookFromBookshelfReplaceBookWrapperNullMutation() {
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock,
                userServiceMock, bookWrapperRepositoryMock);
        Validator validator = mock(BaseValidator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(existingBookshelf.getBookshelfId(), owner.getUserId(), owner.getUserId()))
                .thenReturn(true);
        when(bookshelfRepositoryMock.findById(existingBookshelf.getBookshelfId()))
                .thenReturn(Optional.of(existingBookshelf));
        when(bookRepositoryMock.findById(testBook.getBookId()))
                .thenReturn(Optional.of(testBook));
        when(bookWrapperRepositoryMock.findById(any(BookWrapperId.class))).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookshelfService.removeBookFromBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), testBook.getBookId()));
        assertEquals("Book wrapper not found", exception.getMessage());
    }

    @Test
    public void removeBookToBookshelfNullBookshelfMutation() {
        // Call the addBookToBookshelf method
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookRepository bookRepository = mock(BookRepository.class);
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepository, bookRepository, userServiceMock, bookWrapperRepo);
        Bookshelf newBookshelf = new Bookshelf();
        newBookshelf.setBookshelfId(UUID.randomUUID());
        Validator validator = mock(BaseValidator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(existingBookshelf.getBookshelfId(), owner.getUserId(), owner.getUserId()))
                .thenReturn(true);
        when(bookshelfRepository.existsById(newBookshelf.getBookshelfId())).thenReturn(true);
        when(bookRepository.existsById(testBook.getBookId())).thenReturn(true);
        when(userServiceMock.existsById(owner.getUserId())).thenReturn(true);
        NotFoundException e = assertThrows(NotFoundException.class, () ->
                bookshelfService.removeBookFromBookshelf(newBookshelf.getBookshelfId(),
                        owner.getUserId(), testBook.getBookId()));
        assertEquals("Bookshelf not found", e.getMessage());
    }

    @Test
    public void removeBookToBookshelfNullBookMutation() {
        // Call the addBookToBookshelf method
        BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
        BookWrapperRepository bookWrapperRepository = mock(BookWrapperRepository.class);
        BookRepository bookRepository = mock(BookRepository.class);
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepository, bookRepository, userServiceMock, bookWrapperRepository);
        Book newBook = new Book();
        newBook.setBookId(UUID.randomUUID());
        Validator validator = mock(BaseValidator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(existingBookshelf.getBookshelfId(), owner.getUserId(), newBook.getBookId()))
                .thenReturn(true);

        when(bookshelfRepository.existsById(existingBookshelf.getBookshelfId())).thenReturn(true);
        when(bookRepository.existsById(newBook.getBookId())).thenReturn(true);
        when(userServiceMock.existsById(owner.getUserId())).thenReturn(true);


        when(bookshelfRepository.findById(existingBookshelf.getBookshelfId()))
                .thenReturn(Optional.of(existingBookshelf));

        NotFoundException e = assertThrows(NotFoundException.class, () ->
                bookshelfService.removeBookFromBookshelf(existingBookshelf.getBookshelfId(),
                        owner.getUserId(), newBook.getBookId()));
        assertEquals("Book not found", e.getMessage());
    }

    @Test
    public void addBookToBookshelfNotFoundBookshelf() {
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);

        Validator validator = mock(Validator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        UUID bookshelfId = UUID.randomUUID();
        NotFoundException e = assertThrows(NotFoundException.class, () ->
                bookshelfService.addBookToBookshelf(bookshelfId,
                        owner.getUserId(), testBook.getBookId()));
        assertEquals("Bookshelf not found", e.getMessage());
    }

    @Test
    public void addMultipleBooksToBookshelfBookIdNull() {
        Validator validator = mock(Validator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        bookshelfService.setCircleValidator(validator);
        when(bookshelfRepositoryMock.findById(existingBookshelf.getBookshelfId()))
                .thenReturn(Optional.of(existingBookshelf));
        List<UUID> bookIds = new ArrayList<>(Arrays.asList(UUID.randomUUID(), null, UUID.randomUUID()));
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.addMultipleBooksToBookshelf(existingBookshelf.getBookshelfId(),
                        owner.getUserId(), bookIds));
        assertEquals("Book id cannot be null", e.getMessage());
    }

    @Test
    public void addMultipleBooksToBookshelfBookNotFound() {
        Validator validator = mock(Validator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        bookshelfService.setCircleValidator(validator);
        when(bookshelfRepositoryMock.findById(existingBookshelf.getBookshelfId()))
                .thenReturn(Optional.of(existingBookshelf));
        List<UUID> bookIds = new ArrayList<>(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.addMultipleBooksToBookshelf(existingBookshelf.getBookshelfId(),
                        owner.getUserId(), bookIds));
        assertEquals("Book not found", e.getMessage());
    }

    @Test
    public void addMultipleBooksToBookshelfBookAlreadyInBookshelf() {
        Validator validator = mock(Validator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        bookshelfService.setCircleValidator(validator);
        Book book1 = new Book();
        UUID bookId1 = UUID.randomUUID();
        book1.setBookId(bookId1);
        Book book2 = new Book();
        UUID bookId2 = UUID.randomUUID();
        book2.setBookId(bookId2);
        Book book3 = new Book();
        UUID bookId3 = UUID.randomUUID();
        book3.setBookId(bookId3);
        when(bookRepositoryMock.existsById(bookId1)).thenReturn(true);
        when(bookRepositoryMock.existsById(bookId2)).thenReturn(true);
        when(bookRepositoryMock.existsById(bookId3)).thenReturn(true);
        Bookshelf bookshelf = new Bookshelf();
        bookshelf.setBookshelfId(UUID.randomUUID());
        bookshelf.addBooksItem(book1);
        bookshelf.addBooksItem(book2);
        when(bookshelfRepositoryMock.findById(bookshelf.getBookshelfId()))
                .thenReturn(Optional.of(bookshelf));
        List<UUID> bookIds = new ArrayList<>(Arrays.asList(bookId3, bookId2));
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.addMultipleBooksToBookshelf(bookshelf.getBookshelfId(),
                        owner.getUserId(), bookIds));
        assertEquals("Book already exists in the bookshelf", e.getMessage());
    }

    @Test
    public void addMultipleBooksToBookshelfSuccessful() throws Exception {
        Validator validator = mock(Validator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        bookshelfService.setCircleValidator(validator);
        Book book1 = new Book();
        UUID bookId1 = UUID.randomUUID();
        book1.setBookId(bookId1);
        Book book2 = new Book();
        UUID bookId2 = UUID.randomUUID();
        book2.setBookId(bookId2);
        Book book3 = new Book();
        UUID bookId3 = UUID.randomUUID();
        book3.setBookId(bookId3);
        when(bookRepositoryMock.existsById(bookId1)).thenReturn(true);
        when(bookRepositoryMock.existsById(bookId2)).thenReturn(true);
        when(bookRepositoryMock.existsById(bookId3)).thenReturn(true);
        when(bookRepositoryMock.findById(bookId1)).thenReturn(Optional.of(book1));
        when(bookRepositoryMock.findById(bookId2)).thenReturn(Optional.of(book2));
        when(bookRepositoryMock.findById(bookId3)).thenReturn(Optional.of(book3));

        Bookshelf bookshelf = new Bookshelf();
        bookshelf.setOwner(owner);
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setUserId(userId);
        bookshelf.setMembers(new ArrayList<>(List.of(user)));
        bookshelf.setBookshelfId(UUID.randomUUID());
        bookshelf.addBooksItem(book3);
        assertEquals(1, bookshelf.getBooks().size());
        when(userServiceMock.findById(userId)).thenReturn(user);
        Bookshelf bookshelf2 = new Bookshelf();
        bookshelf2.setOwner(owner);
        bookshelf2.setMembers(new ArrayList<>(List.of(user)));
        bookshelf2.setBookshelfId(UUID.randomUUID());
        bookshelf2.addBooksItem(book3);
        bookshelf2.addBooksItem(book1);
        bookshelf2.addBooksItem(book2);
        when(bookshelfRepositoryMock.findAll()).thenReturn(new ArrayList<>(List.of(bookshelf2)));
        when(bookshelfRepositoryMock.findById(bookshelf.getBookshelfId()))
                .thenReturn(Optional.of(bookshelf));
        List<UUID> bookIds = new ArrayList<>(Arrays.asList(bookId1, bookId2));
        List<Book> books = bookshelfService.addMultipleBooksToBookshelf(bookshelf.getBookshelfId(),
                userId, bookIds);
        assertEquals(2, books.size());
        assertThat(books).containsExactlyInAnyOrder(book1, book2);
        verify(bookWrapperRepositoryMock, times(4)).save(Mockito.any());
    }

    @Test
    public void addMultipleBooksToBookshelfNullBookshelfId() {
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        Validator validator = mock(Validator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        when(bookshelfRepositoryMock.findById(existingBookshelf.getBookshelfId()))
                .thenReturn(Optional.empty());
        assertThrows(ValidationException.class
                , () -> bookshelfService.addMultipleBooksToBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), new ArrayList<>()));
    }

    @Test
    public void addMultipleBooksToBookshelfNullBookId() {
        Validator validator = mock(Validator.class);
        bookshelfService.setCircleValidator(validator);
        when(validator.handle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        bookshelfService.setCircleValidator(validator);
        Book book1 = new Book();
        UUID bookId1 = UUID.randomUUID();
        book1.setBookId(bookId1);
        Book book2 = new Book();
        UUID bookId2 = UUID.randomUUID();
        book2.setBookId(bookId2);
        Book book3 = new Book();
        UUID bookId3 = UUID.randomUUID();
        book3.setBookId(bookId3);
        when(bookRepositoryMock.existsById(bookId1)).thenReturn(true);
        when(bookRepositoryMock.existsById(bookId2)).thenReturn(true);
        when(bookRepositoryMock.existsById(bookId3)).thenReturn(true);
        when(bookRepositoryMock.findById(bookId1)).thenReturn(Optional.of(book1));
        when(bookRepositoryMock.findById(bookId2)).thenReturn(Optional.empty());
        when(bookRepositoryMock.findById(bookId3)).thenReturn(Optional.of(book3));

        Bookshelf bookshelf = new Bookshelf();
        bookshelf.setOwner(owner);
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setUserId(userId);
        bookshelf.setMembers(new ArrayList<>(List.of(user)));
        bookshelf.setBookshelfId(UUID.randomUUID());
        bookshelf.addBooksItem(book3);
        assertEquals(1, bookshelf.getBooks().size());
        when(bookshelfRepositoryMock.findById(bookshelf.getBookshelfId()))
                .thenReturn(Optional.of(bookshelf));
        List<UUID> bookIds = new ArrayList<>(Arrays.asList(bookId1, bookId2));
        ValidationException e = assertThrows(ValidationException.class, () -> bookshelfService.addMultipleBooksToBookshelf(bookshelf.getBookshelfId(),
                userId, bookIds));
        assertEquals("Book not found", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfSuccessfully() throws Exception {
        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook.getBookId());
        bookIdsToRemove.add(testBook2.getBookId());

        BookWrapper bookWrapper1 = new BookWrapper();
        bookWrapper1.setBookId(testBook.getBookId());
        bookWrapper1.setUserId(owner.getUserId());

        BookWrapper bookWrapper2 = new BookWrapper();
        bookWrapper2.setBookId(testBook2.getBookId());
        bookWrapper2.setUserId(owner.getUserId());

        bookWrapperRepo.save(bookWrapper1);
        bookWrapperRepo.save(bookWrapper2);

        bookshelfService.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), bookIdsToRemove);
        //Check that the books were removed
        assertFalse(existingBookshelf.getBooks().contains(testBook));
        assertFalse(existingBookshelf.getBooks().contains(testBook2));
    }

    @Test
    public void removeMultipleBooksFromBookshelfSuccessfully2() throws Exception {
        Validator validator = mock(Validator.class);
        BookshelfService bookshelfService2 = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        bookshelfService2.setCircleValidator(validator);
        //when(validator.handle(existingBookshelf.getBookshelfId(), owner.getUserId(), owner.getUserId())).thenReturn(true);
        when(validator.handle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

        when(bookshelfRepositoryMock.existsById(existingBookshelf.getBookshelfId())).thenReturn(true);
        when(bookshelfRepositoryMock.findById(existingBookshelf.getBookshelfId())).thenReturn(Optional.of(existingBookshelf));
        when(userServiceMock.existsById(owner.getUserId())).thenReturn(true);

        when(bookRepositoryMock.existsById(testBook.getBookId())).thenReturn(true);
        when(bookRepositoryMock.existsById(testBook2.getBookId())).thenReturn(true);
        when(bookRepositoryMock.findById(testBook.getBookId())).thenReturn(Optional.of(testBook));
        when(bookRepositoryMock.findById(testBook2.getBookId())).thenReturn(Optional.of(testBook2));
        //Setting up the book wrappers
        BookWrapperId wrapperId1 = new BookWrapperId();
        wrapperId1.setBookId(testBook.getBookId());
        wrapperId1.setUserId(owner.getUserId());
        BookWrapper bookWrapper1 = new BookWrapper();
        bookWrapper1.setBookId(testBook.getBookId());
        bookWrapper1.setUserId(owner.getUserId());

        when(bookWrapperRepositoryMock.existsById(any(BookWrapperId.class))).thenReturn(true);
        when(bookWrapperRepositoryMock.findById(any(BookWrapperId.class))).thenReturn(Optional.of(bookWrapper1));

        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook.getBookId());
        bookIdsToRemove.add(testBook2.getBookId());

        existingBookshelf.getMembers().add(new User(randomUserId));

        bookshelfService2.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), bookIdsToRemove);

        verify(bookWrapperRepositoryMock, times(4)).delete(bookWrapper1);
    }

    @Test
    public void removeMultipleBooksFromBookshelfBookWrapperEmpty(){
        Validator validator = mock(Validator.class);
        BookshelfService bookshelfService2 = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        bookshelfService2.setCircleValidator(validator);
        //when(validator.handle(existingBookshelf.getBookshelfId(), owner.getUserId(), owner.getUserId())).thenReturn(true);
        when(validator.handle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

        when(bookshelfRepositoryMock.existsById(existingBookshelf.getBookshelfId())).thenReturn(true);
        when(bookshelfRepositoryMock.findById(existingBookshelf.getBookshelfId())).thenReturn(Optional.of(existingBookshelf));
        when(userServiceMock.existsById(owner.getUserId())).thenReturn(true);

        when(bookRepositoryMock.existsById(testBook.getBookId())).thenReturn(true);
        when(bookRepositoryMock.existsById(testBook2.getBookId())).thenReturn(true);
        when(bookRepositoryMock.findById(testBook.getBookId())).thenReturn(Optional.of(testBook));
        when(bookRepositoryMock.findById(testBook2.getBookId())).thenReturn(Optional.of(testBook2));
        //Setting up the book wrappers
        BookWrapperId wrapperId1 = new BookWrapperId();
        wrapperId1.setBookId(testBook.getBookId());
        wrapperId1.setUserId(owner.getUserId());
        BookWrapper bookWrapper1 = new BookWrapper();
        bookWrapper1.setBookId(testBook.getBookId());
        bookWrapper1.setUserId(owner.getUserId());

        when(bookWrapperRepositoryMock.existsById(any(BookWrapperId.class))).thenReturn(true);
        when(bookWrapperRepositoryMock.findById(any(BookWrapperId.class))).thenReturn(Optional.empty());

        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook.getBookId());
        bookIdsToRemove.add(testBook2.getBookId());

        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService2.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), bookIdsToRemove));
        assertEquals("Book wrapper not found", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfBookNotFound2() {
        Validator validator = mock(Validator.class);
        BookshelfService bookshelfService2 = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        bookshelfService2.setCircleValidator(validator);
        //when(validator.handle(existingBookshelf.getBookshelfId(), owner.getUserId(), owner.getUserId())).thenReturn(true);
        when(validator.handle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

        when(bookshelfRepositoryMock.existsById(existingBookshelf.getBookshelfId())).thenReturn(true);
        when(bookshelfRepositoryMock.findById(existingBookshelf.getBookshelfId())).thenReturn(Optional.of(existingBookshelf));
        when(userServiceMock.existsById(owner.getUserId())).thenReturn(true);

        when(bookRepositoryMock.existsById(testBook.getBookId())).thenReturn(true);
        when(bookRepositoryMock.existsById(testBook2.getBookId())).thenReturn(true);
        when(bookRepositoryMock.findById(testBook.getBookId())).thenReturn(Optional.empty());
        when(bookRepositoryMock.findById(testBook2.getBookId())).thenReturn(Optional.empty());
        //Setting up the book wrappers
        BookWrapperId wrapperId1 = new BookWrapperId();
        wrapperId1.setBookId(testBook.getBookId());
        wrapperId1.setUserId(owner.getUserId());
        BookWrapper bookWrapper1 = new BookWrapper();
        bookWrapper1.setBookId(testBook.getBookId());
        bookWrapper1.setUserId(owner.getUserId());

        when(bookWrapperRepositoryMock.existsById(any(BookWrapperId.class))).thenReturn(true);
        when(bookWrapperRepositoryMock.findById(any(BookWrapperId.class))).thenReturn(Optional.of(bookWrapper1));

        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook.getBookId());
        bookIdsToRemove.add(testBook2.getBookId());

        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService2.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), bookIdsToRemove));
        assertEquals("Book not found", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfBookshelfNotFound2() {
        Validator validator = mock(Validator.class);
        BookshelfService bookshelfService2 = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        bookshelfService2.setCircleValidator(validator);
        //when(validator.handle(existingBookshelf.getBookshelfId(), owner.getUserId(), owner.getUserId())).thenReturn(true);
        when(validator.handle(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

        when(bookshelfRepositoryMock.existsById(existingBookshelf.getBookshelfId())).thenReturn(true);
        when(bookshelfRepositoryMock.findById(existingBookshelf.getBookshelfId())).thenReturn(Optional.empty());
        when(userServiceMock.existsById(owner.getUserId())).thenReturn(true);

        when(bookRepositoryMock.existsById(testBook.getBookId())).thenReturn(true);
        when(bookRepositoryMock.existsById(testBook2.getBookId())).thenReturn(true);
        when(bookRepositoryMock.findById(testBook.getBookId())).thenReturn(Optional.of(testBook));
        when(bookRepositoryMock.findById(testBook2.getBookId())).thenReturn(Optional.of(testBook2));
        //Setting up the book wrappers
        BookWrapperId wrapperId1 = new BookWrapperId();
        wrapperId1.setBookId(testBook.getBookId());
        wrapperId1.setUserId(owner.getUserId());
        BookWrapper bookWrapper1 = new BookWrapper();
        bookWrapper1.setBookId(testBook.getBookId());
        bookWrapper1.setUserId(owner.getUserId());

        when(bookWrapperRepositoryMock.existsById(any(BookWrapperId.class))).thenReturn(true);
        when(bookWrapperRepositoryMock.findById(any(BookWrapperId.class))).thenReturn(Optional.of(bookWrapper1));

        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook.getBookId());
        bookIdsToRemove.add(testBook2.getBookId());

        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService2.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), bookIdsToRemove));
        assertEquals("Bookshelf not found", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfBookshelfIdNull() {
        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook.getBookId());
        bookIdsToRemove.add(testBook2.getBookId());
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.removeMultipleBooksFromBookshelf(null, owner.getUserId(), bookIdsToRemove));
        assertEquals("Bookshelf id cannot be null", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfUserIdNull() {
        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook.getBookId());
        bookIdsToRemove.add(testBook2.getBookId());
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), null, bookIdsToRemove));
        assertEquals("User id cannot be null", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfBookIdNull() {
        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(null);
        bookIdsToRemove.add(testBook.getBookId());
        when(bookWrapperRepositoryMock.existsById(any(BookWrapperId.class))).thenReturn(true);
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), bookIdsToRemove));
        assertEquals("Book id cannot be null", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfBookshelfNotFound() {
        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook.getBookId());
        bookIdsToRemove.add(testBook2.getBookId());
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.removeMultipleBooksFromBookshelf(randomBookshelfId, owner.getUserId(), bookIdsToRemove));
        assertEquals("Bookshelf not found", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfUserNotFound() {
        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook.getBookId());
        bookIdsToRemove.add(testBook2.getBookId());
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), UUID.randomUUID(), bookIdsToRemove));
        assertEquals("User not found", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfBookNotFound() {
        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(randomBookId);
        bookIdsToRemove.add(testBook2.getBookId());
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), bookIdsToRemove));
        assertEquals("Book not found", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfBookWrapperNotFound() {
        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook.getBookId());
        bookIdsToRemove.add(testBook2.getBookId());
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), bookIdsToRemove));
        assertEquals("Book wrapper not found", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfBookNotInBookshelf() {
        existingBookshelf.addBooksItem(testBook);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook2.getBookId());
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), owner.getUserId(), bookIdsToRemove));
        assertEquals("Book is not found in specified bookshelf", e.getMessage());
    }

    @Test
    public void removeMultipleBooksFromBookshelfUserUnauthorised() {
        existingBookshelf.addBooksItem(testBook);
        existingBookshelf.addBooksItem(testBook2);
        List<UUID> bookIdsToRemove = new ArrayList<>();
        bookIdsToRemove.add(testBook.getBookId());
        bookIdsToRemove.add(testBook2.getBookId());
        ValidationException e = assertThrows(ValidationException.class, () ->
                bookshelfService.removeMultipleBooksFromBookshelf(existingBookshelf.getBookshelfId(), randomUserId, bookIdsToRemove));
        assertEquals("User does not have permission to modify the bookshelf", e.getMessage());
    }


    @Test
    public void getBookshelfNullBookshelfId() {
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);

        assertThrows(IllegalArgumentException.class
                , () -> bookshelfService.getBookshelfById(null));
    }

    @Test
    public void getNumberOfBooksReadCircleBookshelfNotExist() {
        UUID bookshelfId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> bookshelfService.getNumberOfBooksReadCircle(bookshelfId));
    }

    @Test
    public void getNumberOfBooksReadCircleBookshelfSingleUserSingleBookRead() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        User owner = new User(UUID.randomUUID());
        UUID bookId = UUID.randomUUID();
        BookWrapper wrapper = new BookWrapper().bookId(bookId).userId(owner.getUserId()).readingStatus(BookWrapper.ReadingStatusEnum.READ);
        bookWrapperRepo.save(wrapper);
        Bookshelf bookshelf = new Bookshelf()
                .bookshelfId(bookshelfId)
                .owner(owner)
                .members(new ArrayList<>())
                .books(List.of(testBook.bookId(bookId)));
        testBookshelfRepo.save(bookshelf);

        BookWrapperId wrapperId = new BookWrapperId(bookId, owner.getUserId());

        assertEquals(1, bookshelfService.getNumberOfBooksReadCircle(bookshelfId));
    }

    @Test
    public void getNumberOfBooksReadCircleBookshelfSingleUserNoBookRead() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        User owner = new User(UUID.randomUUID());
        UUID bookId = UUID.randomUUID();
        BookWrapper wrapper = new BookWrapper().bookId(bookId).userId(owner.getUserId()).readingStatus(BookWrapper.ReadingStatusEnum.READING);
        bookWrapperRepo.save(wrapper);
        Bookshelf bookshelf = new Bookshelf()
                .bookshelfId(bookshelfId)
                .owner(owner)
                .members(new ArrayList<>())
                .books(List.of(testBook.bookId(bookId)));
        testBookshelfRepo.save(bookshelf);

        BookWrapperId wrapperId = new BookWrapperId(bookId, owner.getUserId());

        assertEquals(0, bookshelfService.getNumberOfBooksReadCircle(bookshelfId));
    }

    @Test
    public void getPreferredGenresCircleBookshelfNotExist() {
        UUID bookshelfId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> bookshelfService.getPreferredGenresCircle(bookshelfId));
    }

    @Test
    public void getPreferredGenresCircleBookshelfSingleBook() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        Book book = new Book().genres(List.of(Book.GenresEnum.CRIME));
        Bookshelf shelf = new Bookshelf().books(List.of(book)).bookshelfId(bookshelfId);
        testBookshelfRepo.save(shelf);
        List<String> expected = List.of("CRIME");
        assertEquals(expected, bookshelfService.getPreferredGenresCircle(bookshelfId));
    }

    @Test
    public void getPreferredGenresCircleBookshelfSingleBookMultipleGenres() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        Book book = new Book().genres(List.of(Book.GenresEnum.MYSTERY, Book.GenresEnum.CRIME));
        Bookshelf shelf = new Bookshelf().books(List.of(book)).bookshelfId(bookshelfId);
        testBookshelfRepo.save(shelf);
        List<String> expected = List.of("CRIME", "MYSTERY");
        assertEquals(expected, bookshelfService.getPreferredGenresCircle(bookshelfId));
    }

    @Test
    public void getPreferredGenresCircleBookshelfMultipleBooksSingleGenres() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        Book book = new Book().genres(List.of(Book.GenresEnum.MYSTERY));
        Book book1 = new Book().genres(List.of(Book.GenresEnum.CRIME));
        Bookshelf shelf = new Bookshelf().books(List.of(book, book1)).bookshelfId(bookshelfId);
        testBookshelfRepo.save(shelf);
        List<String> expected = List.of("CRIME", "MYSTERY");
        assertEquals(expected, bookshelfService.getPreferredGenresCircle(bookshelfId));
    }

    @Test
    public void getPreferredGenresCircleBookshelfMultipleBooksMultipleGenres() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        Book book = new Book().genres(List.of(Book.GenresEnum.MYSTERY, Book.GenresEnum.DRAMA));
        Book book1 = new Book().genres(List.of(Book.GenresEnum.CRIME, Book.GenresEnum.ROMANCE));
        Bookshelf shelf = new Bookshelf().books(List.of(book, book1)).bookshelfId(bookshelfId);
        testBookshelfRepo.save(shelf);
        List<String> expected = List.of("CRIME", "DRAMA", "MYSTERY");
        assertEquals(expected, bookshelfService.getPreferredGenresCircle(bookshelfId));
    }


    @Test
    public void getBookshelfBookshelfNotFound() {
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        when(bookshelfRepositoryMock.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class
                , () -> bookshelfService.getBookshelfById(UUID.randomUUID()));
    }

    @Test
    public void getBookshelfBookshelfFound() throws NotFoundException {
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        when(bookshelfRepositoryMock.findById(any(UUID.class))).thenReturn(Optional.of(existingBookshelf));
        Bookshelf bookshelf = bookshelfService.getBookshelfById(UUID.randomUUID());
        assertEquals(existingBookshelf, bookshelf);
    }

    @Test
    public void testAddBookWrapperMultipleRemoveCallMutation() throws Exception {
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        Book testBook2 = new Book();
        List<Book> books = new ArrayList<>(Arrays.asList(testBook,testBook2));
        when(bookRepositoryMock.findById(testBook.getBookId())).thenReturn(Optional.of(testBook));
        when(bookRepositoryMock.findById(testBook2.getBookId())).thenReturn(Optional.of(testBook2));
        bookshelfService.addBookWrapperMultiple(randomUserId,books);
        verify(bookshelfRepositoryMock,times(2)).findAll();
    }

    @Test
    public void testDeleteBookWrapperMultipleMutations() throws Exception {
        BookshelfService bookshelfService = new BookshelfService(bookshelfRepositoryMock, bookRepositoryMock, userServiceMock, bookWrapperRepositoryMock);
        Book testBook2 = new Book();
        List<Book> books = new ArrayList<>(Arrays.asList(testBook2,testBook));
        when(bookRepositoryMock.findById(testBook.getBookId())).thenReturn(Optional.of(testBook));
        when(bookRepositoryMock.findById(testBook2.getBookId())).thenReturn(Optional.of(testBook2));
        when(bookWrapperRepositoryMock.findById(any(BookWrapperId.class))).thenReturn(Optional.of(new BookWrapper()));
        when(bookshelfRepositoryMock.findAll()).thenReturn(new ArrayList<>(List.of(existingBookshelf)));
        bookshelfService.deleteBookWrapperMultiple(randomUserId,books);
        verify(bookshelfRepositoryMock,times(2)).findAll();
        verify(bookWrapperRepositoryMock,times(2)).deleteById(Mockito.any());
    }
}
