package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class UserIdExistInCircleValidator extends BaseValidator {

    private final BookshelfRepository bookshelfRepository;
    private final UserRepository userRepository;

    public UserIdExistInCircleValidator(BookshelfRepository bookshelfRepository, UserRepository userRepository) {
        this.bookshelfRepository = bookshelfRepository;
        this.userRepository = userRepository;
    }

    /**
     * Checks if the user id is valid.
     *
     * @param bookshelfId the bookshelf id
     * @param ownerId     the owner id
     * @param userId      the user id
     * @return true if the user id is valid
     * @throws ValidationException if the user id is null or invalid
     */
    @Override
    public boolean handle(UUID bookshelfId, UUID ownerId, UUID userId) throws ValidationException {
        if (userId == null) {
            throw new ValidationException("User id cannot be null");
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new ValidationException("User not found");
        }
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId)
                .orElseThrow(() -> new ValidationException("Bookshelf not found"));
        List<UUID> bookshelfUserIds = bookshelf.getMembers().stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        if (!bookshelfUserIds.contains(userId)) {
            throw new ValidationException("User not in circle");
        }
        return super.checkNext(bookshelfId, ownerId, userId);
    }
}
