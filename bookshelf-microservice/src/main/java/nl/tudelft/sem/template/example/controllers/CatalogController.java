package nl.tudelft.sem.template.example.controllers;

import javassist.NotFoundException;
import nl.tudelft.sem.template.example.services.CatalogService;
import nl.tudelft.sem.template.model.*;
import nl.tudelft.sem.template.api.CatalogApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookshelf_service")
public class CatalogController implements CatalogApi {

    private final CatalogService catalogService;

    /**
     * Constructor for the CatalogController.
     *
     * @param catalogService the catalog service
     */
    @Autowired
    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * Get all the books in the catalog.
     *
     * @return a list of all the available books a user can access
     */
    @Override
    public ResponseEntity<List<Book>> catalogGet() {
        try {
            List<Book> books = catalogService.getAllBooks();

            // 200: OK. Books returned.
            return ResponseEntity.ok(books);
        } catch (EmptyResultDataAccessException e) {
            // 204: No books in the catalog.
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a book to keep in the catalog.
     *
     * @param book = Book object that needs to be added to the catalog.
     * @return one of three valid responses: 200, 400, 500.
     */
    @Override
    public ResponseEntity<Void> catalogPost(Book book) {
        try {
            catalogService.add(book);

            // 200: OK. Successful response.
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // 400: Invalid Request.
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a book from the catalog.
     *
     * @param bookId ID of the book that needs to be deleted.
     * @return one of four valid responses: 200, 400, 404, 500.
     */
    @Override
    public ResponseEntity<Void> catalogDelete(UUID bookId) {
        try {
            catalogService.deleteBook(bookId);

            // 200: Successful deletion.
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // 400: Invalid Request.
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            // 404: Book not found.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Edit a book from the catalog.
     *
     * @param book Book object that needs to be edited, with the new details.
     * @return one of four valid responses: 200, 400, 404, 500.
     */
    @Override
    public ResponseEntity<Void> catalogPut(Book book) {
        try {
            catalogService.editBook(book);

            // 200: Successful response.
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // 400: Invalid Request.
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            // 404: Book not found.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the details about a book by its ID.
     *
     * @param bookId The ID of the book we are looking for.
     * @return all the details about a specific book.
     */
    @Override
    public ResponseEntity<List<Book>> catalogBookIdGet(UUID bookId) {
        try {
            Book book = catalogService.getById(bookId);

            List<Book> books = new ArrayList<>();
            books.add(book);

            // 200: Successful response.
            return ResponseEntity.ok(books);
        } catch (IllegalArgumentException e) {
            // 400: Invalid Request.
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            // 404: Book not found.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the share link of a book by its ID.
     *
     * @param bookId ID of the book that needs to be shared.
     * @return the share link of the book.
     */
    @Override
    public ResponseEntity<String> catalogBookIdShareGet(UUID bookId) {
        try {
            String shareKey = catalogService.getShareLink(bookId);

            // 200: Successful response.
            return ResponseEntity.ok(shareKey);
        } catch (IllegalArgumentException e) {
            // 400: Invalid Request.
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            // 404: Book not found.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search for books in the catalog.
     *
     * @param title  The title of the book we are searching for (optional)
     * @param author The author of the book we are searching for (optional)
     * @return a list of books that match the search query
     */
    @Override
    public ResponseEntity<List<Book>> catalogSearchGet(String title, String author) {
        try {
            List<Book> books = catalogService.search(title, author);

            // 200: Successful response.
            return ResponseEntity.ok(books);
        } catch (NotFoundException e) {
            // 204: No matching books found.
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}