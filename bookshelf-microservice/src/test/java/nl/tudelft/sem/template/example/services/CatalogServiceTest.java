package nl.tudelft.sem.template.example.services;

import nl.tudelft.sem.template.example.database.TestBookRepository;
import nl.tudelft.sem.template.example.database.TestBookWrapperRepository;
import nl.tudelft.sem.template.example.database.TestBookshelfRepository;
import javassist.NotFoundException;
import nl.tudelft.sem.template.example.database.TestUserRepository;
import nl.tudelft.sem.template.model.Book;
import nl.tudelft.sem.template.example.utility.TestingUtility;

import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.BookshelfPostRequest;
import nl.tudelft.sem.template.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogServiceTest {
    private CatalogService catalogService;
    private BookshelfService bookshelfService;
    private CategoryService categoryService;
    private UserService userService;
    private TestingUtility util;

    private TestBookRepository bookRepo;
    private TestBookshelfRepository bookshelfRepo;
    private TestUserRepository userRepo;
    private TestBookWrapperRepository bookWrapperRepo;

    @BeforeEach
    public void setUp() {
        bookRepo = new TestBookRepository();
        bookshelfRepo = new TestBookshelfRepository();
        userRepo = new TestUserRepository();
        bookWrapperRepo = new TestBookWrapperRepository();

        util = new TestingUtility(bookRepo, bookshelfRepo);
        catalogService = new CatalogService(bookRepo, bookshelfRepo);
        userService = new UserService(userRepo, bookshelfRepo, bookRepo, bookWrapperRepo, categoryService, null);
        bookshelfService = new BookshelfService(bookshelfRepo, bookRepo, userService, bookWrapperRepo);
    }

    /**
     * Test the getAllBooks method - OK Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testGetAllBooksOK() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        assertEquals(List.of(b), catalogService.getAllBooks());
        assertNotNull(bookRepo.catalog);
        util.assertBookRepoCall("findAll");
    }

    /**
     * Test the getAllBooks method - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void testGetAllBooksException() {
        catalogService.setSimulateError(true);
        assertThrows(Exception.class, () -> catalogService.getAllBooks());
    }

    /**
     * Test the add method - OK Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testAddOK() throws Exception {
        Book b = util.constructBook();
        Book saved = catalogService.add(b);

        assertEquals(b, saved);
        assertEquals(List.of(b), bookRepo.catalog);

        util.assertBookRepoCall("save");
    }

    /**
     * Test the add method - IllegalArgumentException Response.
     *
     * @throws IllegalArgumentException invalid book
     */
    @Test
    public void testAddIllegalArgumentException() throws IllegalArgumentException {
        Book b = util.constructBook();
        b.setTitle(null);

        assertThrows(IllegalArgumentException.class, () -> catalogService.add(b));
    }

    /**
     * Test the add method - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void testAddException() {
        catalogService.setSimulateError(true);
        Book b = util.constructBook();

        assertThrows(Exception.class, () -> catalogService.add(b));
    }

    /**
     * Test the delete method - OK Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testDeleteOK() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        BookshelfPostRequest bookshelfPostRequest = new BookshelfPostRequest();
        bookshelfPostRequest.setDescription("description");
        bookshelfPostRequest.setTitle("title");
        bookshelfPostRequest.setPrivacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);

        Bookshelf bookshelf = bookshelfService.addBookshelf(bookshelfPostRequest, user.getUserId());
        bookshelf.addBooksItem(b);

        catalogService.deleteBook(b.getBookId());

        assertTrue(bookRepo.catalog.isEmpty());
        assertTrue(bookshelfRepo.bookshelves.get(0).getBooks().isEmpty());
        util.assertBookRepoCall("deleteById");
    }

    /**
     * Test the delete method - NotFoundException Response.
     */
    @Test
    public void testDeleteNotFoundException() {
        assertThrows(NotFoundException.class, () -> catalogService.deleteBook(util.constructBook().getBookId()));
    }

    /**
     * Test the delete method - INTERNAL_SERVER_ERROR Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testDeleteException() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        catalogService.setSimulateError(true);
        assertThrows(Exception.class, () -> catalogService.deleteBook(b.getBookId()));
    }

    /**
     * Test the getById method - OK Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testGetByIdOK() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        assertEquals(b, catalogService.getById(b.getBookId()));
        util.assertBookRepoCall("existsById");
    }

    /**
     * Test the getById method - NotFoundException Response.
     */
    @Test
    public void testGetByIdNotFoundException() {
        assertThrows(NotFoundException.class, () -> catalogService.getById(util.constructBook().getBookId()));
    }

    /**
     * Test the getById method - INTERNAL_SERVER_ERROR Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testGetByIdException() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        catalogService.setSimulateError(true);
        assertThrows(Exception.class, () -> catalogService.getById(b.getBookId()));
    }

    /**
     * Test the editBook method - OK Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testEditBookOK() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        User user = new User();
        user.setUserId(UUID.randomUUID());
        userRepo.users.add(user);

        BookshelfPostRequest bookshelfPostRequest = new BookshelfPostRequest();
        bookshelfPostRequest.setDescription("description");
        bookshelfPostRequest.setTitle("title");
        bookshelfPostRequest.setPrivacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);

        Bookshelf bookshelf = bookshelfService.addBookshelf(bookshelfPostRequest, user.getUserId());
        bookshelf.addBooksItem(b);

        Book newBook = util.constructBook();
        newBook.setBookId(b.getBookId());
        newBook.setNumPages(2000);
        newBook.setDescription("new description");
        newBook.setGenres(List.of(Book.GenresEnum.ROMANCE));
        newBook.setAuthors(List.of("new author"));

        catalogService.editBook(newBook);

        assertEquals(newBook, bookRepo.catalog.get(0));
        assertEquals(newBook, bookshelfRepo.bookshelves.get(0).getBooks().get(0));
        util.assertBookRepoCall("save");
    }

    /**
     * Test the editBook method - NotFoundException Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testEditBookNotFoundException() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        Book newBook = util.constructBook();
        newBook.setBookId(util.constructBook().getBookId());

        assertThrows(NotFoundException.class, () -> catalogService.editBook(newBook));
    }

    /**
     * Test the editBook method - INTERNAL_SERVER_ERROR Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testEditBookException() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        catalogService.setSimulateError(true);
        Book newBook = util.constructBook();
        newBook.setBookId(b.getBookId());

        assertThrows(Exception.class, () -> catalogService.editBook(newBook));
    }

    /**
     * Test the getShareLink method - OK Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testGetShareLinkOK() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        assertEquals("http://localhost:8080/bookshelf_service/catalog/"
                + b.getBookId(), catalogService.getShareLink(b.getBookId()));
        util.assertBookRepoCall("existsById");
    }

    /**
     * Test the getShareLink method - NotFoundException Response.
     */
    @Test
    public void testGetShareLinkNotFoundException() {
        assertThrows(NotFoundException.class, () -> catalogService.getShareLink(util.constructBook().getBookId()));
    }

    /**
     * Test the getShareLink method - INTERNAL_SERVER_ERROR Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testGetShareLinkException() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        catalogService.setSimulateError(true);
        assertThrows(Exception.class, () -> catalogService.getShareLink(b.getBookId()));
    }

    /**
     * Test the search method for title - OK Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testShareTitleOK() throws Exception {
        Book b = util.constructBook("test");
        catalogService.add(b);

        assertEquals(List.of(b), catalogService.search("test", null));
    }

    /**
     * Test the search method for authors - OK Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testShareAuthorsOK() throws Exception {
        Book b = util.constructBook(List.of("test"));
        catalogService.add(b);

        assertEquals(List.of(b), catalogService.search(null, "test"));
    }

    /**
     * Test the search method for title and authors - OK Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testShareTitleAndAuthorsOK() throws Exception {
        Book b = util.constructBook("aa", List.of("testttt"));
        catalogService.add(b);

        assertEquals(List.of(b), catalogService.search("aa", "test"));
    }

    /**
     * Test the search method for empty query - OK Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testShareEmptyQueryOK() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        assertEquals(List.of(b), catalogService.search(null, null));
        assertEquals(bookRepo.catalog, catalogService.search(null, null));
    }

    /**
     * Test the search method for title - NotFoundException Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testShareNotFoundException() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        assertThrows(NotFoundException.class, () -> catalogService.search("test", null));
    }

    /**
     * Test the search method for author - NotFoundException Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testShareAuthorNotFoundException() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        assertThrows(NotFoundException.class, () -> catalogService.search(null, "test"));
    }

    /**
     * Test the search method for title and author - NotFoundException Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testShareTitleAndAuthorNotFoundException() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        assertThrows(NotFoundException.class, () -> catalogService.search("test", "test"));
    }

    /**
     * Test the search method - INTERNAL_SERVER_ERROR Response.
     *
     * @throws Exception for testing purposes
     */
    @Test
    public void testShareException() throws Exception {
        Book b = util.constructBook();
        catalogService.add(b);

        catalogService.setSimulateError(true);
        assertThrows(Exception.class, () -> catalogService.search("test", "test"));
    }
}
