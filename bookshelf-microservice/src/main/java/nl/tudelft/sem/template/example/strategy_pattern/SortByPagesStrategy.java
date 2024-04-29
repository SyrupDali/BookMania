package nl.tudelft.sem.template.example.strategy_pattern;

import nl.tudelft.sem.template.example.database.BookRepository;
import nl.tudelft.sem.template.model.Book;
import nl.tudelft.sem.template.model.BookWrapper;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SortByPagesStrategy implements SortingStrategy {
    /**
     * Sorts the books in descending order by % of pages read.
     *
     * @param books the list of books to be sorted
     * @return the sorted list of books
     */
    @Override
    public List<BookWrapper> sort(List<BookWrapper> books, BookRepository catalog) {
        books.sort(Comparator.comparingDouble(bookWrapper -> {
            UUID bookId = bookWrapper.getBookId();
            Book book = catalog.findById(bookId).orElse(null);

            return -1.0 * bookWrapper.getCurrentPage() / book.getNumPages();
        }));

        return books;
    }
}
