package nl.tudelft.sem.template.example.utility;

import nl.tudelft.sem.template.example.database.TestBookRepository;
import nl.tudelft.sem.template.example.database.TestBookshelfRepository;
import nl.tudelft.sem.template.model.Book;
import nl.tudelft.sem.template.model.BookWrapper;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.User;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestingUtility {
    TestBookRepository bookRepo;
    TestBookshelfRepository bookshelfRepo;

    public TestingUtility(TestBookRepository bookRepo, TestBookshelfRepository bookshelfRepo) {
        this.bookRepo = bookRepo;
        this.bookshelfRepo = bookshelfRepo;
    }

    /*
     * Helper Construct Methods
     */

    /**
     * Helper method to construct a book.
     *
     * @return A book.
     */
    public Book constructBook() {
        Book b = new Book();
        UUID bookId = UUID.randomUUID();

        b.setBookId(bookId);
        b.setAuthors(List.of("Author"));
        b.setTitle("Title");
        b.setDescription("Description");
        b.setNumPages(200);
        b.setGenres(List.of(Book.GenresEnum.MYSTERY));

        return b;
    }

    /**
     * Helper method to construct a book.
     *
     * @param title The title of the book.
     * @return A book.
     */
    public Book constructBook(String title) {
        Book b = new Book();
        UUID bookId = UUID.randomUUID();

        b.setBookId(bookId);
        b.setAuthors(List.of("Author"));
        b.setTitle(title);
        b.setDescription("Description");
        b.setNumPages(200);
        b.setGenres(List.of(Book.GenresEnum.MYSTERY));

        return b;
    }

    /**
     * Helper method to construct a book.
     *
     * @param authors The authors of the book.
     * @return A book.
     */
    public Book constructBook(List<String> authors) {
        Book b = new Book();
        UUID bookId = UUID.randomUUID();

        b.setBookId(bookId);
        b.setAuthors(authors);
        b.setTitle("Title");
        b.setDescription("Description");
        b.setNumPages(200);
        b.setGenres(List.of(Book.GenresEnum.MYSTERY));

        return b;
    }

    /**
     * Helper method to construct a book.
     *
     * @param title   The title of the book.
     * @param authors The authors of the book.
     * @return A book.
     */
    public Book constructBook(String title, List<String> authors) {
        Book b = new Book();
        UUID bookId = UUID.randomUUID();

        b.setBookId(bookId);
        b.setAuthors(authors);
        b.setTitle(title);
        b.setDescription("Description");
        b.setNumPages(200);
        b.setGenres(List.of(Book.GenresEnum.MYSTERY));

        return b;
    }

    public Book constructBookGenres(List<Book.GenresEnum> genres) {
        Book b = new Book();
        UUID bookId = UUID.randomUUID();

        b.setBookId(bookId);
        b.setAuthors(List.of("Author"));
        b.setTitle("Title");
        b.setDescription("Description");
        b.setNumPages(200);
        b.setGenres(genres);

        return b;
    }

    /**
     * Helper method to construct a bookshelf.
     *
     * @param owner The owner of the bookshelf.
     * @return A bookshelf.
     */
    public Bookshelf constructBookshelf(User owner) {
        Bookshelf bookshelf = new Bookshelf();

        bookshelf.setBookshelfId(UUID.randomUUID());
        bookshelf.setOwner(owner);
        bookshelf.setDescription("Description");
        bookshelf.setTitle("Title");

        return bookshelf;
    }

    /**
     * Helper method to construct a user.
     *
     * @return A user.
     */
    public User constructUser() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        return user;
    }

    /**
     * Helper method to construct a book wrapper.
     *
     * @param bookId The book ID.
     * @param userId The user ID.
     * @return A book wrapper.
     */
    public BookWrapper constructBookWrapper(UUID bookId, UUID userId) {
        BookWrapper bw = new BookWrapper();
        bw.setBookId(bookId);
        bw.setUserId(userId);
        return bw;
    }

    /*
     * Helper Assert Methods
     */

    /**
     * Asserts that the correct method was called on the book repo.
     *
     * @param expectedCall The expected method call.
     */
    public void assertBookRepoCall(String expectedCall) {
        assertTrue(bookRepo.calledMethods.contains(expectedCall));
    }

    /**
     * Asserts that the correct method was called on the bookshelf repo.
     *
     * @param expectedCall The expected method call.
     */
    public void assertBookshelfRepoCall(String expectedCall) {
        assertTrue(bookshelfRepo.calledMethods.contains(expectedCall));
    }
}
