package nl.tudelft.sem.template.example.strategy_pattern;

import nl.tudelft.sem.template.example.database.BookRepository;
import nl.tudelft.sem.template.model.Book;
import nl.tudelft.sem.template.model.BookWrapper;

import java.util.List;
import java.util.UUID;

public class SortByTitlesStrategy implements SortingStrategy {
    /**
     * Sorts the books alphabetically by title
     *
     * @param books the list of books to be sorted
     * @return the sorted list of books
     */
    @Override
    public List<BookWrapper> sort(List<BookWrapper> books, BookRepository catalog) {
        books.sort((bookWrapper1, bookWrapper2) -> {
            UUID bookId1 = bookWrapper1.getBookId();
            Book book1 = catalog.findById(bookId1).get();

            UUID bookId2 = bookWrapper2.getBookId();
            Book book2 = catalog.findById(bookId2).get();

            return book1.getTitle().compareTo(book2.getTitle());
        });

        return books;
    }
}
