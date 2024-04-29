package nl.tudelft.sem.template.example.services;

import javassist.NotFoundException;
import nl.tudelft.sem.template.example.database.*;
import nl.tudelft.sem.template.example.exceptions.InvalidDataException;
import nl.tudelft.sem.template.example.exceptions.NullException;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.example.utility.TestingUtility;
import nl.tudelft.sem.template.model.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {
    private CatalogService catalogService;
    private UserService userService;
    private BookshelfService bookshelfService;
    private TestingUtility util;
    private TestBookshelfRepository bookshelfRepo;
    private TestUserRepository userRepo;
    private TestBookWrapperRepository bookWrapperRepo;

    private TestCategoryRepository categoryRepo;

    private User u1;
    private User u2;
    private final UserRepository mockUserRepo = mock(UserRepository.class);
    private final BookshelfRepository mockBookshelfRepo = mock(BookshelfRepository.class);
    private final BookRepository mockBookRepo = mock(BookRepository.class);
    private final BookWrapperRepository mockBwRepo = mock(BookWrapperRepository.class);
    private final UtilityService mockUtil = mock(UtilityService.class);
    private final CategoryService mockCategoryService = mock(CategoryService.class);
    private UserService mockUserService;

    @BeforeEach
    public void setUp() {
        TestBookRepository bookRepo = new TestBookRepository();
        bookshelfRepo = new TestBookshelfRepository();
        userRepo = new TestUserRepository();
        bookWrapperRepo = new TestBookWrapperRepository();
        categoryRepo = new TestCategoryRepository();

        when(mockUtil.validId(any())).thenReturn(true);

        util = new TestingUtility(bookRepo, bookshelfRepo);
        CategoryService categoryService = new CategoryService(categoryRepo, userRepo, bookshelfRepo);
        catalogService = new CatalogService(bookRepo, bookshelfRepo);
        userService = new UserService(userRepo, bookshelfRepo, bookRepo, bookWrapperRepo, categoryService, mockUtil);
        bookshelfService = new BookshelfService(bookshelfRepo, bookRepo, userService, bookWrapperRepo);

        u1 = new User(UUID.randomUUID());
        u2 = new User(UUID.randomUUID());
        mockUserService = new UserService(mockUserRepo, mockBookshelfRepo, mockBookRepo, mockBwRepo, mockCategoryService, mockUtil);
    }

    @Test
    public void getByOwnerIdNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            mockUserService.getByOwner(null);
        });
    }

    @Test
    public void getByOwnerUserNotFound() {
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(false);
        assertThrows(NotFoundException.class, () -> {
            mockUserService.getByOwner(u1.getUserId());
        });
    }

    @Test
    public void getByOwnerNoBookShelves() {
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(true);
        when(mockBookshelfRepo.findByOwnerId(u1.getUserId())).thenReturn(new ArrayList<>());
        assertThrows(EmptyResultDataAccessException.class, () -> {
            mockUserService.getByOwner(u1.getUserId());
        });
    }

    @Test
    public void getByOwnerHasCreatedBookshelves() throws NotFoundException {
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(true);
        Bookshelf shelf = new Bookshelf().bookshelfId(UUID.randomUUID());
        when(mockBookshelfRepo.findByOwnerId(u1.getUserId())).thenReturn(List.of(shelf));
        assertEquals(1, mockUserService.getByOwner(u1.getUserId()).size());
    }

    @Test
    public void getPreferredGenresUserNotFound() {
        User user = util.constructUser();
        assertThrows(NotFoundException.class, () -> userService.getPreferredGenres(user.getUserId()));
    }

    @Test
    public void singleShelfSingleBookSingleGenre() throws Exception {
        User user = util.constructUser();
        userRepo.save(user);
        Bookshelf shelf = util.constructBookshelf(user);
        Book book = util.constructBookGenres(List.of(Book.GenresEnum.CRIME));
        shelf.addBooksItem(book);
        bookshelfRepo.save(shelf);
        List<String> expected = List.of(Book.GenresEnum.CRIME.getValue());
        assertEquals(expected, userService.getPreferredGenres(user.getUserId()));
    }

    @Test
    public void singleShelfSingleBookMultipleGenres() throws Exception {
        User user = util.constructUser();
        userRepo.save(user);
        Bookshelf shelf = util.constructBookshelf(user);
        Book book = util.constructBookGenres(List.of(Book.GenresEnum.CRIME, Book.GenresEnum.MYSTERY));
        shelf.addBooksItem(book);
        bookshelfRepo.save(shelf);
        List<String> expected = List.of(Book.GenresEnum.CRIME.getValue(), Book.GenresEnum.MYSTERY.getValue());
        assertEquals(expected, userService.getPreferredGenres(user.getUserId()));
    }

    @Test
    public void singleShelfMultipleBooksSingleGenre() throws Exception {
        User user = util.constructUser();
        userRepo.save(user);
        Bookshelf shelf = util.constructBookshelf(user);
        Book book1 = util.constructBookGenres(List.of(Book.GenresEnum.CRIME));
        Book book2 = util.constructBookGenres(List.of(Book.GenresEnum.MYSTERY));
        shelf.addBooksItem(book1);
        shelf.addBooksItem(book2);
        bookshelfRepo.save(shelf);
        List<String> expected = List.of(Book.GenresEnum.CRIME.getValue(), Book.GenresEnum.MYSTERY.getValue());
        assertEquals(expected, userService.getPreferredGenres(user.getUserId()));
    }

    @Test
    public void differentGenresSameFrequency() throws Exception {
        User user = util.constructUser();
        userRepo.save(user);
        Bookshelf shelf = util.constructBookshelf(user);
        Book book1 = util.constructBookGenres(List.of(Book.GenresEnum.CRIME, Book.GenresEnum.ROMANCE, Book.GenresEnum.SCIENCE));
        Book book2 = util.constructBookGenres(List.of(Book.GenresEnum.MYSTERY, Book.GenresEnum.DRAMA));
        shelf.addBooksItem(book1);
        shelf.addBooksItem(book2);
        bookshelfRepo.save(shelf);
        List<String> expected = List.of(Book.GenresEnum.CRIME.getValue(), Book.GenresEnum.DRAMA.getValue(), Book.GenresEnum.MYSTERY.getValue());
        assertEquals(expected, userService.getPreferredGenres(user.getUserId()));
    }

    @Test
    public void getNumberOfBooksReadUserNotFound() {
        User user = util.constructUser();
        assertThrows(NotFoundException.class, () -> userService.getNumberOfBooksRead(user.getUserId()));
    }

    @Test
    public void getNumberOfBooksReadNoBooksRead() throws Exception {
        User user = util.constructUser();
        userRepo.save(user);
        Book book = new Book().bookId(UUID.randomUUID());
        BookWrapper wrapper = new BookWrapper()
                .userId(user.getUserId())
                .bookId(book.getBookId())
                .readingStatus(BookWrapper.ReadingStatusEnum.READING);
        bookWrapperRepo.save(wrapper);
        assertEquals(0, userService.getNumberOfBooksRead(user.getUserId()));
    }

    @Test
    public void getNumberOfBooksReadSingleBook() throws Exception {
        User user = util.constructUser();
        userRepo.save(user);
        Book book = new Book().bookId(UUID.randomUUID());
        BookWrapper wrapper = new BookWrapper()
                .userId(user.getUserId())
                .bookId(book.getBookId())
                .readingStatus(BookWrapper.ReadingStatusEnum.READ);
        bookWrapperRepo.save(wrapper);
        assertEquals(1, userService.getNumberOfBooksRead(user.getUserId()));
    }

    @Test
    public void getNumberOfBooksReadStatusNull() throws Exception {
        User user = util.constructUser();
        userRepo.save(user);
        Book book = new Book().bookId(UUID.randomUUID());
        BookWrapper wrapper = new BookWrapper()
                .userId(user.getUserId())
                .bookId(book.getBookId())
                .readingStatus(null);
        bookWrapperRepo.save(wrapper);
        assertEquals(0, userService.getNumberOfBooksRead(user.getUserId()));
    }

    @Test
    public void getNumberOfBooksReadMultipleBooks() throws Exception {
        User user = util.constructUser();
        userRepo.save(user);
        int amountOfBooks = 20;
        for (int i = 0; i < amountOfBooks; i++) {
            Book book = new Book().bookId(UUID.randomUUID());
            BookWrapper wrapper = new BookWrapper()
                    .userId(user.getUserId())
                    .bookId(book.getBookId())
                    .readingStatus(BookWrapper.ReadingStatusEnum.READ);
            bookWrapperRepo.save(wrapper);
        }
        assertEquals(amountOfBooks, userService.getNumberOfBooksRead(user.getUserId()));
    }

    @Test
    public void getNumberOfBooksReadMix() throws Exception {
        User user = util.constructUser();
        userRepo.save(user);
        int amountOfBooksRead = 10;
        int amountOfBooksNotRead = 20;
        for (int i = 0; i < amountOfBooksRead; i++) {
            Book book = new Book().bookId(UUID.randomUUID());
            BookWrapper wrapper = new BookWrapper()
                    .userId(user.getUserId())
                    .bookId(book.getBookId())
                    .readingStatus(BookWrapper.ReadingStatusEnum.READ);
            bookWrapperRepo.save(wrapper);
        }
        for (int i = 0; i < amountOfBooksNotRead; i++) {
            Book book = new Book().bookId(UUID.randomUUID());
            BookWrapper wrapper = new BookWrapper()
                    .userId(user.getUserId())
                    .bookId(book.getBookId())
                    .readingStatus(BookWrapper.ReadingStatusEnum.WANT_TO_READ);
            bookWrapperRepo.save(wrapper);
        }
        assertEquals(amountOfBooksRead, userService.getNumberOfBooksRead(user.getUserId()));
    }

    @Test
    public void getNumberOfBooksReadSingleShelfWrongCategory() throws Exception {
        User user = util.constructUser();
        userRepo.save(user);
        Bookshelf shelf = util.constructBookshelf(user);
        shelf.addBooksItem(util.constructBook());
        bookshelfRepo.save(shelf);
        Category category = new Category().addBookshelvesItem(shelf)
                .name("Not Read books")
                .user(user);
        categoryRepo.save(category);
        assertEquals(0, userService.getNumberOfBooksRead(user.getUserId()));
    }

    /**
     * Test the updateCurrentPage method - OK Response.
     */
    @Test
    public void updateCurrentPageTestOK() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        Bookshelf bs = new Bookshelf();
        bs.setDescription("test");
        bs.setTitle("test");
        bs.setPrivacy(Bookshelf.PrivacyEnum.PUBLIC);
        bs.setBookshelfId(UUID.randomUUID());
        bs.setOwner(user);
        bs.setMembers(List.of(user));
        bs.setBooks(new ArrayList<>());
        bookshelfRepo.bookshelves.add(bs);

        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b.getBookId());

        Integer page = 10;
        userService.updateCurrentPage(user.getUserId(), b.getBookId(), page);
        assertEquals(page, bookWrapperRepo.catalog.get(0).getCurrentPage());
    }

    /**
     * Test the updateCurrentPage method - Not Found Response.
     */
    @Test
    public void updateCurrentPageTestNotFound() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        BookWrapper bw = new BookWrapper();
        bw.setBookId(b.getBookId());
        bw.setUserId(user.getUserId());
        bookWrapperRepo.catalog.add(bw);

        Integer page = 10;
        UUID wrongId = UUID.randomUUID();
        assertThrows(Exception.class, () -> userService.updateCurrentPage(wrongId, bw.getBookId(), page));
    }

    /**
     * Test the updateCurrentPage method - Bad Request Response.
     */
    @Test
    public void updateCurrentPageTestBadRequest() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        BookWrapper bw = new BookWrapper();
        bw.setBookId(b.getBookId());
        bw.setUserId(user.getUserId());
        bookWrapperRepo.catalog.add(bw);

        assertThrows(InvalidDataException.class, () -> userService.updateCurrentPage(null, bw.getBookId(), -1));
    }

    /**
     * Test the updateCurrentPage method - Internal Server Error Response.
     */
    @Test
    public void updateCurrentPageTestInternalServerError() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        BookWrapper bw = new BookWrapper();
        bw.setBookId(b.getBookId());
        bw.setUserId(user.getUserId());
        bookWrapperRepo.catalog.add(bw);

        Integer page = 10;
        userService.setSimulateError(true);
        assertThrows(Exception.class, () -> userService.updateCurrentPage(bw.getUserId(), bw.getBookId(), page));
    }

    /**
     * Test the getCurrentPage method - OK Response.
     */
    @Test
    public void getCurrentPageTestOK() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        BookWrapper bw = new BookWrapper();
        bw.setBookId(b.getBookId());
        bw.setUserId(user.getUserId());
        bookWrapperRepo.catalog.add(bw);

        Integer page = 10;
        userService.updateCurrentPage(bw.getUserId(), bw.getBookId(), page);
        Integer actualPage = userService.getCurrentPage(bw.getUserId(), bw.getBookId());
        assertEquals(page, actualPage);
    }

    /**
     * Test the getCurrentPage method - Not Found Response.
     */
    @Test
    public void getCurrentPageTestNotFound() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        BookWrapper bw = new BookWrapper();
        bw.setBookId(b.getBookId());
        bw.setUserId(user.getUserId());
        bookWrapperRepo.catalog.add(bw);

        UUID wrongId = UUID.randomUUID();
        assertThrows(Exception.class, () -> userService.getCurrentPage(bw.getUserId(), wrongId));
    }

    /**
     * Test the getCurrentPage method - Bad Request Response.
     */
    @Test
    public void getCurrentPageTestBadRequest() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        BookWrapper bw = new BookWrapper();
        bw.setBookId(b.getBookId());
        bw.setUserId(user.getUserId());
        bookWrapperRepo.catalog.add(bw);

        when(mockUtil.validId(null)).thenReturn(false);

        assertThrows(InvalidDataException.class, () -> userService.getCurrentPage(bw.getUserId(), null));
    }

    /**
     * Test the getCurrentPage method - Internal Server Error Response.
     */
    @Test
    public void getCurrentPageTestInternalServerError() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        BookWrapper bw = new BookWrapper();
        bw.setBookId(b.getBookId());
        bw.setUserId(user.getUserId());
        bookWrapperRepo.catalog.add(bw);

        userService.setSimulateError(true);
        assertThrows(Exception.class, () -> userService.getCurrentPage(bw.getUserId(), bw.getBookId()));
    }

    /*
        Test the sortBooks method for title - OK Response.
     */
    @Test
    public void sortBooksTestTitleOK() throws Exception {
        Book b1 = util.constructBook("c");
        Book b2 = util.constructBook("ddd");
        Book b3 = util.constructBook("a");
        catalogService.add(b1);
        catalogService.add(b2);
        catalogService.add(b3);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        Bookshelf bs = new Bookshelf();
        bs.setDescription("test");
        bs.setTitle("test");
        bs.setPrivacy(Bookshelf.PrivacyEnum.PUBLIC);
        bs.setBookshelfId(UUID.randomUUID());
        bs.setOwner(user);
        bs.setMembers(List.of(user));
        bs.setBooks(new ArrayList<>());
        bookshelfRepo.bookshelves.add(bs);

        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b1.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b2.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b3.getBookId());

        BookWrapper bw1 = new BookWrapper();
        bw1.setBookId(b1.getBookId());
        bw1.setUserId(user.getUserId());
        bw1.setCurrentPage(0);
        BookWrapper bw2 = new BookWrapper();
        bw2.setBookId(b2.getBookId());
        bw2.setUserId(user.getUserId());
        bw2.setCurrentPage(0);
        BookWrapper bw3 = new BookWrapper();
        bw3.setBookId(b3.getBookId());
        bw3.setUserId(user.getUserId());
        bw3.setCurrentPage(0);

        List<BookWrapper> expected = List.of(bw3, bw1, bw2);
        List<BookWrapper> actual = userService.sortBooks(user.getUserId(), bs.getBookshelfId(), "title");

        assertEquals(expected, actual);
    }

    /*
        Test the sortBooks method for author - OK Response.
     */
    @Test
    public void sortBooksTestAuthorOK() throws Exception {
        Book b1 = util.constructBook(List.of("cdtg"));
        Book b2 = util.constructBook(List.of("ddd"));
        Book b3 = util.constructBook(List.of("ttt", "a"));
        catalogService.add(b1);
        catalogService.add(b2);
        catalogService.add(b3);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        Bookshelf bs = new Bookshelf();
        bs.setDescription("test");
        bs.setTitle("test");
        bs.setPrivacy(Bookshelf.PrivacyEnum.PUBLIC);
        bs.setBookshelfId(UUID.randomUUID());
        bs.setOwner(user);
        bs.setMembers(List.of(user));
        bs.setBooks(new ArrayList<>());
        bookshelfRepo.bookshelves.add(bs);

        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b1.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b2.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b3.getBookId());

        BookWrapper bw1 = new BookWrapper();
        bw1.setBookId(b1.getBookId());
        bw1.setUserId(user.getUserId());
        bw1.setCurrentPage(0);
        BookWrapper bw2 = new BookWrapper();
        bw2.setBookId(b2.getBookId());
        bw2.setUserId(user.getUserId());
        bw2.setCurrentPage(0);
        BookWrapper bw3 = new BookWrapper();
        bw3.setBookId(b3.getBookId());
        bw3.setUserId(user.getUserId());
        bw3.setCurrentPage(0);

        List<BookWrapper> expected = List.of(bw3, bw1, bw2);
        List<BookWrapper> actual = userService.sortBooks(user.getUserId(), bs.getBookshelfId(), "author");

        assertEquals(expected, actual);
    }

    /*
        Test the sortBooks method for % of pages read - OK Response.
     */
    @Test
    public void sortBooksTestPagesReadOK() throws Exception {
        Book b1 = util.constructBook();
        b1.setNumPages(150);
        Book b2 = util.constructBook();
        b2.setNumPages(200);
        Book b3 = util.constructBook();
        b3.setNumPages(100);
        catalogService.add(b1);
        catalogService.add(b2);
        catalogService.add(b3);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        Bookshelf bs = new Bookshelf();
        bs.setDescription("test");
        bs.setTitle("test");
        bs.setPrivacy(Bookshelf.PrivacyEnum.PUBLIC);
        bs.setBookshelfId(UUID.randomUUID());
        bs.setOwner(user);
        bs.setMembers(List.of(user));
        bs.setBooks(new ArrayList<>());
        bookshelfRepo.bookshelves.add(bs);

        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b1.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b2.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b3.getBookId());

        BookWrapper bw1 = new BookWrapper();
        bw1.setBookId(b1.getBookId());
        bw1.setUserId(user.getUserId());
        bw1.setCurrentPage(100);
        BookWrapper bw2 = new BookWrapper();
        bw2.setBookId(b2.getBookId());
        bw2.setUserId(user.getUserId());
        bw2.setCurrentPage(50);
        BookWrapper bw3 = new BookWrapper();
        bw3.setBookId(b3.getBookId());
        bw3.setUserId(user.getUserId());
        bw3.setCurrentPage(80);

        userService.updateCurrentPage(user.getUserId(), b1.getBookId(), 100);
        userService.updateCurrentPage(user.getUserId(), b2.getBookId(), 50);
        userService.updateCurrentPage(user.getUserId(), b3.getBookId(), 80);

        List<BookWrapper> expected = List.of(bw3, bw1, bw2);
        List<BookWrapper> actual = userService.sortBooks(user.getUserId(), bs.getBookshelfId(), "pagesRead");

        assertEquals(expected, actual);
    }

    /*
        Test the sortBooks method - Not Found Response.
     */
    @Test
    public void sortBooksTestNotFound() throws Exception {
        Book b1 = util.constructBook("c");
        Book b2 = util.constructBook("ddd");
        Book b3 = util.constructBook("a");
        catalogService.add(b1);
        catalogService.add(b2);
        catalogService.add(b3);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        Bookshelf bs = new Bookshelf();
        bs.setDescription("test");
        bs.setTitle("test");
        bs.setPrivacy(Bookshelf.PrivacyEnum.PUBLIC);
        bs.setBookshelfId(UUID.randomUUID());
        bs.setOwner(user);
        bs.setMembers(List.of(user));
        bs.setBooks(new ArrayList<>());
        bookshelfRepo.bookshelves.add(bs);

        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b1.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b2.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b3.getBookId());

        UUID wrongId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> userService.sortBooks(user.getUserId(), wrongId, "title"));
    }

    /*
        Test the sortBooks method bad order - Bad Request Response.
     */
    @Test
    public void sortBooksTestBadRequestOrder() throws Exception {
        Book b1 = util.constructBook("c");
        Book b2 = util.constructBook("ddd");
        Book b3 = util.constructBook("a");
        catalogService.add(b1);
        catalogService.add(b2);
        catalogService.add(b3);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        Bookshelf bs = new Bookshelf();
        bs.setDescription("test");
        bs.setTitle("test");
        bs.setPrivacy(Bookshelf.PrivacyEnum.PUBLIC);
        bs.setBookshelfId(UUID.randomUUID());
        bs.setOwner(user);
        bs.setMembers(List.of(user));
        bs.setBooks(new ArrayList<>());
        bookshelfRepo.bookshelves.add(bs);

        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b1.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b2.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b3.getBookId());

        assertThrows(IllegalArgumentException.class, () -> userService.sortBooks(user.getUserId(), bs.getBookshelfId(), "wrong"));
    }

    /*
        Test the sortBooks method - Bad Request Response.
     */
    @Test
    public void sortBooksTestBadRequest() throws Exception {
        Book b1 = util.constructBook("c");
        Book b2 = util.constructBook("ddd");
        Book b3 = util.constructBook("a");
        catalogService.add(b1);
        catalogService.add(b2);
        catalogService.add(b3);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        Bookshelf bs = new Bookshelf();
        bs.setDescription("test");
        bs.setTitle("test");
        bs.setPrivacy(Bookshelf.PrivacyEnum.PUBLIC);
        bs.setBookshelfId(UUID.randomUUID());
        bs.setOwner(user);
        bs.setMembers(List.of(user));
        bs.setBooks(new ArrayList<>());
        bookshelfRepo.bookshelves.add(bs);

        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b1.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b2.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b3.getBookId());

        when(mockUtil.validId(null)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> userService.sortBooks(null, bs.getBookshelfId(), "author"));
    }

    /*
        Test the sortBooks method - Internal Server Error Response.
     */
    @Test
    public void sortBooksTestInternalServerError() throws Exception {
        Book b1 = util.constructBook("c");
        Book b2 = util.constructBook("ddd");
        Book b3 = util.constructBook("a");
        catalogService.add(b1);
        catalogService.add(b2);
        catalogService.add(b3);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        Bookshelf bs = new Bookshelf();
        bs.setDescription("test");
        bs.setTitle("test");
        bs.setPrivacy(Bookshelf.PrivacyEnum.PUBLIC);
        bs.setBookshelfId(UUID.randomUUID());
        bs.setOwner(user);
        bs.setMembers(List.of(user));
        bs.setBooks(new ArrayList<>());
        bookshelfRepo.bookshelves.add(bs);

        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b1.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b2.getBookId());
        bookshelfService.addBookToBookshelf(bs.getBookshelfId(), user.getUserId(), b3.getBookId());

        userService.setSimulateError(true);
        assertThrows(Exception.class, () -> userService.sortBooks(user.getUserId(), bs.getBookshelfId(), "title"));
    }

    @Test
    public void testGetAllUsers() {
        when(mockUserRepo.findAll()).thenReturn(List.of(u1, u2));
        assertThat(mockUserService.getAll()).containsExactlyInAnyOrder(u1, u2);
    }

    @Test
    public void testFindByIdNull() {
        assertThatThrownBy(() -> mockUserService.findById(null)).isInstanceOf(NullException.class);
    }

    @Test
    public void testFindByIdNotFound() {
        when(mockUserRepo.findById(u1.getUserId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mockUserService.findById(null)).isInstanceOf(NullException.class);
    }

    @Test
    public void testFindById() throws NotFoundException, NullException {
        when(mockUserRepo.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        assertThat(mockUserService.findById(u1.getUserId())).isEqualTo(u1);
    }

    @Test
    public void testExistsByIdNull() {
        assertThatThrownBy(() -> mockUserService.existsById(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExistsByIdNotFound() {
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(false);
        assertFalse(mockUserService.existsById(u1.getUserId()));
    }

    @Test
    public void testExistsById() {
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(true);
        assertTrue(mockUserService.existsById(u1.getUserId()));
    }

    @Test
    public void testAddUser() throws Exception {
        when(mockUtil.validId(u1.getUserId())).thenReturn(true);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        mockUserService.addUser(u1.getUserId());
        verify(mockCategoryService).createDefaultCategories(u1.getUserId());
        verify(mockUserRepo).save(userArgumentCaptor.capture());
        assertThat(userArgumentCaptor.getValue()).isEqualTo(u1);
    }

    @Test
    public void testAddUserInvalidId() { //TODO idk why this doesnt work
        when(mockUtil.validId(u1.getUserId())).thenReturn(false);
        assertThatThrownBy(() -> mockUserService.addUser(u1.getUserId())).isInstanceOf(InvalidDataException.class);
    }

    @Test
    public void testAddUserCategoryNotFound() throws Exception {
        when(mockUtil.validId(u1.getUserId())).thenReturn(true);
        doThrow(InvalidDataException.class).when(mockCategoryService).createDefaultCategories(u1.getUserId());
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        assertThatThrownBy(() -> mockUserService.addUser(u1.getUserId())).isInstanceOf(Exception.class);
        verify(mockUserRepo).save(userArgumentCaptor.capture());
        assertThat(userArgumentCaptor.getValue()).isEqualTo(u1);
    }

    @Test
    public void testAddUserCategoryValidation() throws Exception {
        when(mockUtil.validId(u1.getUserId())).thenReturn(true);
        doThrow(ValidationException.class).when(mockCategoryService).createDefaultCategories(u1.getUserId());
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        assertThatThrownBy(() -> mockUserService.addUser(u1.getUserId())).isInstanceOf(Exception.class);
        verify(mockUserRepo).save(userArgumentCaptor.capture());
        assertThat(userArgumentCaptor.getValue()).isEqualTo(u1);
    }

    @Test
    public void testDeleteUser() throws Exception {
        when(mockUserRepo.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        ArgumentCaptor<UUID> userIdCapture = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<User> userCapture = ArgumentCaptor.forClass(User.class);
        mockUserService.deleteUser(u1.getUserId());
        verify(mockCategoryService).deleteUser(userIdCapture.capture());
        verify(mockUserRepo).delete(userCapture.capture());
        assertThat(userIdCapture.getValue()).isEqualTo(u1.getUserId());
        assertThat(userCapture.getValue()).isEqualTo(u1);
    }

    @Test
    public void testGetReadingStatus() throws Exception {
        Book b1 = util.constructBook();
        BookWrapper wrapper = new BookWrapper(b1.getBookId(), u1.getUserId(), BookWrapper.ReadingStatusEnum.READ, 0, new ArrayList<>());
        when(mockUtil.validId(b1.getBookId())).thenReturn(true);
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(true);
        when(mockBookRepo.existsById(b1.getBookId())).thenReturn(true);
        when(mockBwRepo.findById(any())).thenReturn(Optional.of(wrapper)); // this should be with any() but i get issues otherwise for some reason
        assertThat(mockUserService.getReadingStatus(u1.getUserId(), b1.getBookId()))
                .isEqualTo("READ");
    }

    @Test
    public void testGetReadingStatusNullStatus() throws Exception {
        Book b1 = util.constructBook();
        BookWrapper wrapper = new BookWrapper(b1.getBookId(), u1.getUserId(), null, 0, new ArrayList<>());
        when(mockUtil.validId(u1.getUserId())).thenReturn(true);
        when(mockUtil.validId(b1.getBookId())).thenReturn(true);
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(true);
        when(mockBookRepo.existsById(b1.getBookId())).thenReturn(true);
        when(mockBwRepo.findById(any())).thenReturn(Optional.of(wrapper)); // this should be with any() but i get issues otherwise for some reason
        assertThat(mockUserService.getReadingStatus(u1.getUserId(), b1.getBookId()))
                .isEqualTo("NONE");
    }

    @Test
    public void testGetReadingStatusInvalidUser() {
        Book b1 = util.constructBook();
        when(mockUtil.validId(u1.getUserId())).thenReturn(false);
        when(mockUtil.validId(b1.getBookId())).thenReturn(true);
        assertThatThrownBy(() -> mockUserService.getReadingStatus(u1.getUserId(), b1.getBookId()))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    public void testGetReadingStatusInvalidBook() {
        Book b1 = util.constructBook();
        when(mockUtil.validId(u1.getUserId())).thenReturn(true);
        when(mockUtil.validId(b1.getBookId())).thenReturn(false);
        assertThatThrownBy(() -> mockUserService.getReadingStatus(u1.getUserId(), b1.getBookId()))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    public void testGetReadingStatusNotFoundUser() {
        Book b1 = util.constructBook();
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(false);
        when(mockUserRepo.existsById(b1.getBookId())).thenReturn(true);
        assertThatThrownBy(() -> mockUserService.getReadingStatus(u1.getUserId(), b1.getBookId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testGetReadingStatusNotFoundBook() {
        Book b1 = util.constructBook();
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(true);
        when(mockUserRepo.existsById(b1.getBookId())).thenReturn(false);
        assertThatThrownBy(() -> mockUserService.getReadingStatus(u1.getUserId(), b1.getBookId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testSetReadingStatus() throws Exception {
        Book b1 = util.constructBook();
        BookWrapper wrapper = new BookWrapper(b1.getBookId(), u1.getUserId(), BookWrapper.ReadingStatusEnum.READ, 0, new ArrayList<>());
        when(mockUtil.validId(b1.getBookId())).thenReturn(true);
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(true);
        when(mockBookRepo.existsById(b1.getBookId())).thenReturn(true);
        when(mockBwRepo.findById(any())).thenReturn(Optional.of(wrapper)); // this should be with any() but i get issues otherwise for some reason
        assertThat(mockUserService.setReadingStatus(u1.getUserId(), b1.getBookId(), "READING"))
                .isEqualTo("READING");
    }

    @Test
    public void testSetReadingStatusNullStatus() {
        Book b1 = util.constructBook();
        BookWrapper wrapper = new BookWrapper(b1.getBookId(), u1.getUserId(), BookWrapper.ReadingStatusEnum.READ, 0, new ArrayList<>());
        when(mockUtil.validId(u1.getUserId())).thenReturn(true);
        when(mockUtil.validId(b1.getBookId())).thenReturn(true);
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(true);
        when(mockBookRepo.existsById(b1.getBookId())).thenReturn(true);
        when(mockBwRepo.findById(any())).thenReturn(Optional.of(wrapper)); // this should be with any() but i get issues otherwise for some reason
        assertThatThrownBy(() -> mockUserService.setReadingStatus(u1.getUserId(), b1.getBookId(), null))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    public void testSetReadingStatusInvalidStatus() {
        Book b1 = util.constructBook();
        BookWrapper wrapper = new BookWrapper(b1.getBookId(), u1.getUserId(), BookWrapper.ReadingStatusEnum.READ, 0, new ArrayList<>());
        when(mockUtil.validId(u1.getUserId())).thenReturn(true);
        when(mockUtil.validId(b1.getBookId())).thenReturn(true);
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(true);
        when(mockBookRepo.existsById(b1.getBookId())).thenReturn(true);
        when(mockBwRepo.findById(any())).thenReturn(Optional.of(wrapper)); // this should be with any() but i get issues otherwise for some reason
        assertThatThrownBy(() -> mockUserService.setReadingStatus(u1.getUserId(), b1.getBookId(), "invalid"))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    public void testSetReadingStatusInvalidUser() {
        Book b1 = util.constructBook();
        when(mockUtil.validId(u1.getUserId())).thenReturn(false);
        when(mockUtil.validId(b1.getBookId())).thenReturn(true);
        assertThatThrownBy(() -> mockUserService.setReadingStatus(u1.getUserId(), b1.getBookId(), "READ"))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    public void testSetReadingStatusInvalidBook() {
        Book b1 = util.constructBook();
        when(mockUtil.validId(u1.getUserId())).thenReturn(true);
        when(mockUtil.validId(b1.getBookId())).thenReturn(false);
        assertThatThrownBy(() -> mockUserService.setReadingStatus(u1.getUserId(), b1.getBookId(), "READ"))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    public void testSetReadingStatusNotFoundUser() {
        Book b1 = util.constructBook();
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(false);
        when(mockUserRepo.existsById(b1.getBookId())).thenReturn(true);
        assertThatThrownBy(() -> mockUserService.setReadingStatus(u1.getUserId(), b1.getBookId(), "READ"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testSetReadingStatusNotFoundBook() {
        Book b1 = util.constructBook();
        when(mockUserRepo.existsById(u1.getUserId())).thenReturn(true);
        when(mockUserRepo.existsById(b1.getBookId())).thenReturn(false);
        assertThatThrownBy(() -> mockUserService.setReadingStatus(u1.getUserId(), b1.getBookId(), "READ"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testFindByIdLambdaReplacedByNullMutation() {
        when(mockUserRepo.findById(u1.getUserId())).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> mockUserService.findById(u1.getUserId()));
        assertEquals("User with id " + u1.getUserId() + " not found", exception.getMessage());
    }
}
