package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.database.TestBookRepository;
import nl.tudelft.sem.template.example.database.TestBookshelfRepository;

import nl.tudelft.sem.template.example.services.CatalogService;
import nl.tudelft.sem.template.model.Book;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import nl.tudelft.sem.template.example.utility.TestingUtility;

import java.util.List;
import java.util.UUID;

public class CatalogControllerTest {
    private CatalogController catalogController;
    private CatalogService catalogService;
    private TestingUtility util;

    private TestBookRepository bookRepo;

    /**
     * Setup before each test.
     */
    @BeforeEach
    void setUp() {
        bookRepo = new TestBookRepository();
        TestBookshelfRepository bookshelfRepo = new TestBookshelfRepository();
        util = new TestingUtility(bookRepo, bookshelfRepo);
        catalogService = new CatalogService(bookRepo, bookshelfRepo);
        catalogController = new CatalogController(catalogService);
    }

    /**
     * Test the catalogPost method - OK Response.
     */
    @Test
    public void catalogPostOKTest() {
        Book b = util.constructBook();
        ResponseEntity<Void> actual = catalogController.catalogPost(b);

        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());

        util.assertBookRepoCall("save");
        assertEquals(List.of(b), bookRepo.catalog);
    }

    /**
     * Test the catalogPost method - BAD_REQUEST Response.
     */
    @Test
    public void catalogPostBadRequestTest() {
        Book b = new Book().numPages(-1);
        ResponseEntity<Void> actual = catalogController.catalogPost(b);

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    /**
     * Test the catalogPost method - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void catalogPostInternalServerErrorTest() {
        catalogService.setSimulateError(true);
        Book b = util.constructBook();
        ResponseEntity<Void> actual = catalogController.catalogPost(b);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
    }

    /**
     * Test the catalogGet method - OK Response.
     */
    @Test
    public void catalogGetOKTest() {
        Book b = util.constructBook();
        catalogController.catalogPost(b);

        ResponseEntity<List<Book>> actual = catalogController.catalogGet();

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(List.of(b), actual.getBody());

        util.assertBookRepoCall("findAll");
    }

    /**
     * Test the catalogGet method - NO_CONTENT Response.
     */
    @Test
    public void catalogGetNoContentTest() {
        ResponseEntity<List<Book>> actual = catalogController.catalogGet();

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

    /*
     * Test the catalogGet method - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void catalogGetInternalServerErrorTest() {
        catalogService.setSimulateError(true);
        ResponseEntity<List<Book>> actual = catalogController.catalogGet();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
    }

    /**
     * Test the catalogDelete method - OK Response.
     * Note: can't test if the book is removed from the bookshelves,
     * because the bookshelf controller has not been implemented yet.
     */
    @Test
    public void catalogDeleteOKTest() {
        Book b = util.constructBook();
        catalogController.catalogPost(b);

        ResponseEntity<Void> actual = catalogController.catalogDelete(b.getBookId());

        assertEquals(HttpStatus.OK, actual.getStatusCode());

        util.assertBookRepoCall("deleteById");
        assertEquals(0, bookRepo.count());
    }

    /**
     * Test the catalogDelete method - NOT_FOUND Response.
     */
    @Test
    public void catalogDeleteNotFoundTest() {
        Book b = util.constructBook();
        catalogController.catalogPost(b);

        ResponseEntity<Void> actual = catalogController.catalogDelete(new UUID(1, 1));

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        assertEquals(List.of(b), bookRepo.catalog);
    }

    /**
     * Test the catalogDelete method - BAD_REQUEST Response.
     */
    @Test
    public void catalogDeleteBadRequestTest() {
        ResponseEntity<Void> actual = catalogController.catalogDelete(null);

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    /*
     * Test the catalogDelete method - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void catalogDeleteInternalServerErrorTest() {
        Book b = util.constructBook();
        catalogController.catalogPost(b);

        catalogService.setSimulateError(true);

        ResponseEntity<Void> actual = catalogController.catalogDelete(b.getBookId());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
    }

    /**
     * Test the catalogPut method - OK Response.
     * <p>
     * Note: can't test if the book is updated in the bookshelves,
     * because the bookshelf controller has not been implemented yet.
     */
    @Test
    public void catalogPut() {
        Book b = util.constructBook();
        Book newBook = util.constructBook("New Title");
        newBook.setBookId(b.getBookId());
        catalogController.catalogPost(b);

        ResponseEntity<Void> actual = catalogController.catalogPut(newBook);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(List.of(newBook), bookRepo.catalog);

        util.assertBookRepoCall("save");
    }

    /**
     * Test the catalogPut method - BAD_REQUEST Response.
     */
    @Test
    public void catalogPutBadRequestTest() {
        Book b = util.constructBook();
        catalogController.catalogPost(b);

        ResponseEntity<Void> actual = catalogController.catalogPut(new Book());

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        assertEquals(List.of(b), bookRepo.catalog);
    }

    /**
     * Test the catalogPut method - NOT_FOUND Response.
     */
    @Test
    public void catalogPutNotFoundTest() {
        Book b = util.constructBook();
        Book newBook = util.constructBook("New Title");

        catalogController.catalogPost(b);
        newBook.setBookId(new UUID(1, 1));

        ResponseEntity<Void> actual = catalogController.catalogPut(newBook);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        assertEquals(List.of(b), bookRepo.catalog);
    }

    /*
     * Test the catalogPut method - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void catalogPutInternalServerErrorTest() {
        Book b = util.constructBook();
        Book newBook = util.constructBook("New Title");
        newBook.setBookId(b.getBookId());
        catalogController.catalogPost(b);

        catalogService.setSimulateError(true);

        ResponseEntity<Void> actual = catalogController.catalogPut(newBook);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
        assertEquals(List.of(b), bookRepo.catalog);
    }

    /*
     * Test the catalogBookIdGet method - OK Response.
     */
    @Test
    public void catalogBookIdGetOKTest() {
        Book b = util.constructBook();
        catalogController.catalogPost(b);

        ResponseEntity<List<Book>> actual = catalogController.catalogBookIdGet(b.getBookId());

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(List.of(b), actual.getBody());

        util.assertBookRepoCall("findById");
    }

    /*
     * Test the catalogBookIdGet method - NOT_FOUND Response.
     */
    @Test
    public void catalogBookIdGetNotFoundTest() {
        Book b = util.constructBook();
        ResponseEntity<List<Book>> actual = catalogController.catalogBookIdGet(b.getBookId());

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        assertNull(actual.getBody());
    }

    /*
     * Test the catalogBookIdGet method - BAD_REQUEST Response.
     */
    @Test
    public void catalogBookIdGetBadRequestTest() {
        ResponseEntity<List<Book>> actual = catalogController.catalogBookIdGet(null);

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    /*
     * Test the catalogBookIdGet method - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void catalogBookIdGetInternalServerErrorTest() {
        Book b = util.constructBook();
        catalogController.catalogPost(b);

        catalogService.setSimulateError(true);

        ResponseEntity<List<Book>> actual = catalogController.catalogBookIdGet(b.getBookId());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
    }

    /*
     * Test the catalogBookIdShareGet method - OK Response.
     */
    @Test
    public void catalogBookIdShareGetOKTest() {
        Book b = util.constructBook();
        catalogController.catalogPost(b);

        ResponseEntity<String> actual = catalogController.catalogBookIdShareGet(b.getBookId());

        assertEquals(HttpStatus.OK, actual.getStatusCode());

        String share = "http://localhost:8080/bookshelf_service/catalog/" + b.getBookId();
        assertEquals(share, actual.getBody());

        util.assertBookRepoCall("existsById");
    }

    /*
     * Test the catalogBookIdShareGet method - NOT_FOUND Response.
     */
    @Test
    public void catalogBookIdShareGetNotFoundTest() {
        Book b = util.constructBook();
        ResponseEntity<String> actual = catalogController.catalogBookIdShareGet(b.getBookId());

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        assertNull(actual.getBody());
    }

    /*
     * Test the catalogBookIdShareGet method - BAD_REQUEST Response.
     */
    @Test
    public void catalogBookIdShareGetBadRequestTest() {
        ResponseEntity<String> actual = catalogController.catalogBookIdShareGet(null);

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    /*
     * Test the catalogBookIdShareGet method - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void catalogBookIdShareGetInternalServerErrorTest() {
        Book b = util.constructBook();
        catalogController.catalogPost(b);

        catalogService.setSimulateError(true);

        ResponseEntity<String> actual = catalogController.catalogBookIdShareGet(b.getBookId());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
    }

    /*
     * Test the catalogSearchGet method for title parameter only - OK Response.
     */
    @Test
    public void catalogSearchGetTitleOKTest() {
        Book b = util.constructBook("Title");
        catalogController.catalogPost(b);

        ResponseEntity<List<Book>> actual = catalogController.catalogSearchGet(b.getTitle(), null);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(List.of(b), actual.getBody());

        util.assertBookRepoCall("findAll");
    }

    /*
     * Test the catalogSearchGet method for no parameters - OK Response.
     */
    @Test
    public void catalogSearchGetNoParametersOKTest() {
        Book b = util.constructBook("Title");
        catalogController.catalogPost(b);

        ResponseEntity<List<Book>> actual = catalogController.catalogSearchGet(null, null);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(List.of(b), actual.getBody());

        util.assertBookRepoCall("findAll");
    }

    /*
     * Test the catalogSearchGet method for author parameter only - OK Response.
     */
    @Test
    public void catalogSearchGetAuthorOKTest() {
        Book b = util.constructBook(List.of("Author1", "Author2"));
        catalogController.catalogPost(b);

        ResponseEntity<List<Book>> actual = catalogController.catalogSearchGet(null, b.getAuthors().get(0));

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(List.of(b), actual.getBody());

        util.assertBookRepoCall("findAll");
    }

    /*
     * Test the catalogSearchGet method for title and author parameters - OK Response.
     */
    @Test
    public void catalogSearchGetTitleAndAuthorOKTest() {
        Book b = util.constructBook("Title1", List.of("Author1", "Author2"));
        catalogController.catalogPost(b);

        ResponseEntity<List<Book>> actual = catalogController.catalogSearchGet(b.getTitle(), b.getAuthors().get(0));

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(List.of(b), actual.getBody());

        util.assertBookRepoCall("findAll");
    }

    /*
     * Test the catalogSearchGet method - NO_CONTENT Response.
     */
    @Test
    public void catalogSearchGetNoContentTest() {
        Book b = util.constructBook("Title", List.of("Author2"));
        catalogController.catalogPost(b);
        ResponseEntity<List<Book>> actual = catalogController.catalogSearchGet("Title", "Author1");

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
        assertNull(actual.getBody());
    }

    /*
     * Test the catalogSearchGet method - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void catalogSearchGetInternalServerErrorTest() {
        Book b = util.constructBook("Title", List.of("Author2"));
        catalogController.catalogPost(b);

        catalogService.setSimulateError(true);

        ResponseEntity<List<Book>> actual = catalogController.catalogSearchGet("Title", "Author1");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
    }
}
