package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.User;

import java.util.List;
import java.util.UUID;

public class PermissionValidator extends BaseValidator {

    private final BookshelfRepository bookshelfRepository;

    private final UserRepository userRepository;

    public PermissionValidator(BookshelfRepository bookshelfRepository, UserRepository userRepository) {
        this.bookshelfRepository = bookshelfRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean handle(UUID bookShelfId, UUID ownerId, UUID userId) throws ValidationException {
        if (userId == null) {
            throw new ValidationException("User id cannot be null");
        }

        Bookshelf shelf = bookshelfRepository.findById(bookShelfId).orElseThrow(() -> new ValidationException("Bookshelf not found"));
        UUID shelfOwner = shelf.getOwner().getUserId();
        boolean isOwner = shelfOwner.equals(userId);
        if (isOwner) {
            return super.checkNext(bookShelfId, shelfOwner, userId);
        }
        List<User> circleMembers = shelf.getMembers();
        for (User u : circleMembers) {
            if (u.getUserId().equals(userId)) return super.checkNext(bookShelfId, ownerId, userId);
        }
        throw new ValidationException("User does not have permission to modify the bookshelf");
    }
}
