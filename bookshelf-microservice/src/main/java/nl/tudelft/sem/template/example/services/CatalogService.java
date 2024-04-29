package nl.tudelft.sem.template.example.services;

import javassist.NotFoundException;
import nl.tudelft.sem.template.example.database.BookRepository;
import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CatalogService {
    private final BookRepository catalog;
    private final BookshelfRepository bookshelfRepository;
    private final UtilityService util;
    private boolean simulateError = false;

    /**
     * Constructor for the CatalogService.
     *
     * @param catalog the book repository
     */
    @Autowired
    public CatalogService(BookRepository catalog, BookshelfRepository bookshelfRepository) {
        this.catalog = catalog;
        this.bookshelfRepository = bookshelfRepository;
        this.util = new UtilityService();
    }

    public void setSimulateError(boolean simulateError) {
        this.simulateError = simulateError;
    }

    /**
     * Get all the books in the catalog.
     *
     * @return a list of all the available books a user can access in the catalog.
     * @throws Exception for testing purposes
     */
    public List<Book> getAllBooks() throws Exception {
        if (simulateError) {
            throw new Exception();
        }

        if (catalog.findAll().isEmpty()) {
            // 204: No books in the catalog.
            throw new EmptyResultDataAccessException(0);
        }

        return catalog.findAll();
    }

    /**
     * Create a book to keep in the catalog.
     *
     * @param book the book to add to the catalog
     * @return the Book object that was added to the catalog
     * @throws IllegalArgumentException if the book is invalid
     * @throws Exception                for testing purposes
     */
    public Book add(Book book) throws Exception {
        if (simulateError) {
            throw new Exception();
        }

        if (util.isNullOrEmpty(book.getTitle())
                || util.isNullOrEmpty(book.getDescription())
                || book.getAuthors().isEmpty()
                || book.getGenres().isEmpty()
                || book.getNumPages() == null
                || book.getNumPages() <= 0) {

            // 400: Invalid Request.
            throw new IllegalArgumentException();
        }

        return catalog.save(book);
    }

    /**
     * Delete a book from the catalog and from all the bookshelves that contain it.
     *
     * @param bookId the id of the book to delete
     * @throws NotFoundException if the book doesn't exist
     * @throws Exception         for testing purposes
     */
    public void deleteBook(UUID bookId) throws Exception {
        if (simulateError) {
            throw new Exception();
        }

        if (!util.validId(bookId)) {
            // 400: Invalid Request.
            throw new IllegalArgumentException();
        }

        if (!catalog.existsById(bookId)) {
            // 404: Not Found.
            throw new NotFoundException("Book not found.");
        }
        catalog.deleteById(bookId);

        for (Bookshelf bookshelf : bookshelfRepository.findAll()) {
            List<Book> books = bookshelf.getBooks();
            for (Book book : books) {
                if (book.getBookId().equals(bookId)) {
                    books.remove(book);
                    break;
                }
            }

            bookshelf.setBooks(books);
            bookshelfRepository.save(bookshelf);
        }
    }

    /**
     * Get a book by its id.
     *
     * @param bookId the id of the book to get
     * @throws NotFoundException if the book doesn't exist
     * @throws Exception         for testing purposes
     */
    public Book getById(UUID bookId) throws Exception {
        if (simulateError) {
            throw new Exception();
        }

        if (!util.validId(bookId)) {
            // 400: Invalid Request.
            throw new IllegalArgumentException();
        }

        if (!catalog.existsById(bookId)) {
            // 404: Not Found.
            throw new NotFoundException("Book not found");
        }

        return catalog.findById(bookId).orElseThrow(() -> new NotFoundException("Book not found"));
    }

    /**
     * Edit a book in the catalog and in all the bookshelves that contain it.
     *
     * @param book the book to edit, containing the new values
     * @throws NotFoundException if the book doesn't exist
     * @throws Exception         for testing purposes
     */
    public void editBook(Book book) throws Exception {
        if (simulateError) {
            throw new Exception();
        }

        if (util.isNullOrEmpty(book.getTitle())
                || util.isNullOrEmpty(book.getDescription())
                || book.getAuthors().isEmpty()
                || book.getGenres().isEmpty()
                || book.getNumPages() == null
                || book.getNumPages() <= 0) {

            // 400: Invalid Request.
            throw new IllegalArgumentException();
        }

        if (!catalog.existsById(book.getBookId())) {
            // 404: Not Found.
            throw new NotFoundException("Book not found.");
        }

        Book bookFromRepo = getById(book.getBookId());

        bookFromRepo.setTitle(book.getTitle());
        bookFromRepo.setDescription(book.getDescription());
        bookFromRepo.setAuthors(book.getAuthors());
        bookFromRepo.setGenres(book.getGenres());
        bookFromRepo.setNumPages(book.getNumPages());

        catalog.save(bookFromRepo);

        for (Bookshelf bookshelf : bookshelfRepository.findAll()) {
            if (bookshelf.getBooks().contains(book)) {
                bookshelf.getBooks().remove(book);
                bookshelf.getBooks().add(bookFromRepo);
                bookshelfRepository.save(bookshelf);
            }
        }
    }

    /**
     * Get the share link for a book.
     *
     * @param bookId the id of the book to get the share key for
     * @return the share link for the book
     * @throws NotFoundException if the book doesn't exist
     * @throws Exception         for testing purposes
     */
    public String getShareLink(UUID bookId) throws Exception {
        if (simulateError) {
            throw new Exception();
        }

        if (!util.validId(bookId)) {
            // 400: Invalid Request.
            throw new IllegalArgumentException();
        }

        String shareKey = "http://localhost:8080/bookshelf_service/catalog/";

        if (!catalog.existsById(bookId)) {
            // 404: Book not found.
            throw new NotFoundException("Book not found.");
        }

        return shareKey + bookId;
    }

    /**
     * Case-insensitive search for books in the catalog.
     *
     * @param title  the title of the book to search for
     * @param author the author of the book to search for
     * @return a list of books that match the case-insensitive search query
     * @throws NotFoundException if no books match the search query
     * @throws Exception         for testing purposes
     */
    public List<Book> search(String title, String author) throws Exception {
        if (simulateError) {
            throw new Exception();
        }

        List<Book> books = new ArrayList<>();

        if (util.isNullOrEmpty(title) && util.isNullOrEmpty(author)) { // query with no content
            return getAllBooks();
        } else if (util.isNullOrEmpty(title)) { // query with only author
            for (Book book : catalog.findAll()) {
                if (book.getAuthors().stream().anyMatch(name ->
                        name.toLowerCase().contains(author.toLowerCase())))
                    books.add(book);
            }
        } else if (util.isNullOrEmpty(author)) { // query with only title
            for (Book book : catalog.findAll()) {
                if (book.getTitle().toLowerCase().contains(title.toLowerCase()))
                    books.add(book);
            }
        } else { // query with both title and author
            for (Book book : catalog.findAll()) {
                if (book.getTitle().toLowerCase().contains(title.toLowerCase()) &&
                        book.getAuthors().stream().anyMatch(name ->
                                name.toLowerCase().contains(author.toLowerCase())))
                    books.add(book);
            }
        }

        if (books.isEmpty()) {
            //204: No matching books found.
            throw new NotFoundException("No matching books found.");
        }

        return books;
    }
}
