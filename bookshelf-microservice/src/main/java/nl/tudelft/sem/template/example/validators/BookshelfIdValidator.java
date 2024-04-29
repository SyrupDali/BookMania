package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.exceptions.ValidationException;

import java.util.UUID;

public class BookshelfIdValidator extends BaseValidator {
    private final BookshelfRepository bookshelfRepository;

    public BookshelfIdValidator(BookshelfRepository bookshelfRepository) {
        this.bookshelfRepository = bookshelfRepository;
    }

    /**
     * Checks if the bookshelf id is valid.
     *
     * @param bookshelfId the bookshelf id
     * @param ownerId     the owner id
     * @param userId      the user id
     * @return true if the bookshelf id is valid
     * @throws ValidationException if the bookshelf id is null or invalid
     */
    @Override
    public boolean handle(UUID bookshelfId, UUID ownerId, UUID userId) throws ValidationException {
        if (bookshelfId == null) {
            throw new ValidationException("Bookshelf id cannot be null");
        }
        if (bookshelfRepository.findById(bookshelfId).isEmpty()) {
            throw new ValidationException("Bookshelf not found");
        }
        return super.checkNext(bookshelfId, ownerId, userId);
    }
}
