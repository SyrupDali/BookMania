package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.Bookshelf;

import java.util.UUID;

public class OwnerIdValidator extends BaseValidator {
    private final UserRepository userRepository;
    private final BookshelfRepository bookshelfRepository;

    public OwnerIdValidator(UserRepository userRepository,
                            BookshelfRepository bookshelfRepository) {
        this.userRepository = userRepository;
        this.bookshelfRepository = bookshelfRepository;
    }

    /**
     * Checks if the owner id is valid, including if it matches the owner of the bookshelf.
     *
     * @param bookshelfId the bookshelf id
     * @param ownerId     the owner id
     * @param userId      the user id
     * @return true if the owner id is valid
     * @throws ValidationException if the owner id is null or invalid
     */
    @Override
    public boolean handle(UUID bookshelfId, UUID ownerId, UUID userId) throws ValidationException {
        if (ownerId == null) {
            throw new ValidationException("Owner id cannot be null");
        }
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new ValidationException("Owner not found");
        }
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId)
                .orElseThrow(() -> new ValidationException("Bookshelf not found"));
        UUID bookshelfOwnerId = bookshelf.getOwner().getUserId();
        if (!bookshelfOwnerId.equals(ownerId)) {
            throw new ValidationException("User does not match the bookshelf's owner");
        }
        return super.checkNext(bookshelfId, ownerId, userId);
    }
}
