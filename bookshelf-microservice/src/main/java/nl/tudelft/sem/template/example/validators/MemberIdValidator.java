package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.ValidationException;

import java.util.UUID;

public class MemberIdValidator extends BaseValidator {
    private final UserRepository userRepository;

    public MemberIdValidator(UserRepository userRepository) {
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
        return super.checkNext(bookshelfId, ownerId, userId);
    }
}
