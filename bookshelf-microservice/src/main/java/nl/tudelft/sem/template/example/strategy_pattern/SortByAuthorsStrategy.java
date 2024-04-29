package nl.tudelft.sem.template.example.strategy_pattern;

import nl.tudelft.sem.template.example.database.BookRepository;
import nl.tudelft.sem.template.model.Book;
import nl.tudelft.sem.template.model.BookWrapper;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SortByAuthorsStrategy implements SortingStrategy {
    /**
     * Sorts the books alphabetically by author
     *
     * @param books the list of books to be sorted
     * @return the sorted list of books
     */
    @Override
    public List<BookWrapper> sort(List<BookWrapper> books, BookRepository catalog) {
        /*
            Sort the authors alphabetically for every book in the bookshelf
         */
        books.forEach(bw -> {
            UUID bookId = bw.getBookId();
            Book book = catalog.findById(bookId).get();

            List<String> sortedAuthors = book.getAuthors().stream()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
            book.setAuthors(sortedAuthors);
        });

        books.sort((bw1, bw2) -> {
            UUID bookId1 = bw1.getBookId();
            Book book1 = catalog.findById(bookId1).get();

            UUID bookId2 = bw2.getBookId();
            Book book2 = catalog.findById(bookId2).get();

            return String.join("", book1.getAuthors()).compareTo(String.join("", book2.getAuthors()));
        });

        return books;
    }
}
