package nl.tudelft.sem.template.example.strategy_pattern;

import nl.tudelft.sem.template.example.database.BookRepository;
import nl.tudelft.sem.template.model.BookWrapper;

import java.util.List;

public interface SortingStrategy {
    List<BookWrapper> sort(List<BookWrapper> books, BookRepository catalog);
}
