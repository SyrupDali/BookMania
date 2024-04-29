package nl.tudelft.sem.template.example.services;

import lombok.Setter;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.example.validators.*;
import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.BookshelfBookshelfIdCircleDelete200Response;
import nl.tudelft.sem.template.model.BookshelfBookshelfIdCirclePut200Response;
import nl.tudelft.sem.template.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CircleService {
    private final BookshelfRepository bookshelfRepository;
    private final UserRepository userRepository;
    private final BookshelfService bookshelfService;

    @Setter
    private Validator getPendingMembersValidationHandler;
    @Setter
    private Validator requestToJoinValidationHandler;
    @Setter
    private Validator addMemberValidationHandler;
    @Setter
    private Validator removeMemberValidationHandler;
    @Setter
    private Validator acceptPendingMemberValidationHandler;
    @Setter
    private Validator rejectPendingMemberValidationHandler;

    @Autowired
    public CircleService(BookshelfRepository bookshelfRepository, UserRepository userRepository, BookshelfService bookshelfService) {
        this.bookshelfRepository = bookshelfRepository;
        this.userRepository = userRepository;
        this.bookshelfService = bookshelfService;
        // Initialize validators
        // Chain of responsibility pattern
        // Validates the ids and authentication of the owner
        this.getPendingMembersValidationHandler = BaseValidator.link(
                new BookshelfIdValidator(bookshelfRepository),
                new OwnerIdValidator(userRepository, bookshelfRepository)
        );
        // Validates the ids and absence of the user in the pending members/members list
        this.requestToJoinValidationHandler = BaseValidator.link(
                new BookshelfIdValidator(bookshelfRepository),
                new UserIdNotExistInPendingListValidator(bookshelfRepository, userRepository),
                new UserIdNotExistInCircleValidator(bookshelfRepository, userRepository)
        );
        // Validates the ids, authentication of the owner and absence of the user in the members list
        this.addMemberValidationHandler = BaseValidator.link(
                new BookshelfIdValidator(bookshelfRepository),
                new OwnerIdValidator(userRepository, bookshelfRepository),
                new UserIdNotExistInCircleValidator(bookshelfRepository, userRepository)
        );
        // Validates the ids, authentication of the owner and existence of the user in the members list
        this.removeMemberValidationHandler = BaseValidator.link(
                new BookshelfIdValidator(bookshelfRepository),
                new OwnerIdValidator(userRepository, bookshelfRepository),
                new UserIdExistInCircleValidator(bookshelfRepository, userRepository)
        );
        // Validates the ids, authentication of the owner,
        // absence of the user in the members list and existence in the pending members list
        this.acceptPendingMemberValidationHandler = BaseValidator.link(
                new BookshelfIdValidator(bookshelfRepository),
                new OwnerIdValidator(userRepository, bookshelfRepository),
                new UserIdNotExistInCircleValidator(bookshelfRepository, userRepository),
                new UserIdExistInPendingListValidator(bookshelfRepository, userRepository)
        );
        // Validates the ids, authentication of the owner and existence in the pending members list
        this.rejectPendingMemberValidationHandler = BaseValidator.link(
                new BookshelfIdValidator(bookshelfRepository),
                new OwnerIdValidator(userRepository, bookshelfRepository),
                new UserIdExistInPendingListValidator(bookshelfRepository, userRepository)
        );
    }

    /**
     * Method that adds member to circle.
     *
     * @return an object containing the bookshelf id and the list of user ids in the circle
     */
    public BookshelfBookshelfIdCirclePut200Response addMemberToCircle(UUID bookshelfId, UUID ownerId, UUID memberId) {
        addMemberValidationHandler.handle(bookshelfId, ownerId, memberId);
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId)
                .orElseThrow(() -> new ValidationException("Bookshelf not found"));
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ValidationException("User not found"));
        List<UUID> bookshelfUserIds = bookshelf.getMembers().stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        bookshelf.getMembers().add(member);
        bookshelf.getPendingMembers().remove(member);
        bookshelfUserIds.add(memberId);

        try {
            bookshelfService.addBookWrapperMultiple(memberId, bookshelf.getBooks());
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong");
        }
        bookshelfRepository.save(bookshelf);

        BookshelfBookshelfIdCirclePut200Response response = new BookshelfBookshelfIdCirclePut200Response();
        response.bookshelfId(bookshelfId);
        response.setUserIds(bookshelfUserIds);
        return response;
    }

    /**
     * Method that removes member from circle.
     *
     * @return an object containing the bookshelf id and the list of user ids in the circle
     */
    public BookshelfBookshelfIdCircleDelete200Response removeMemberFromCircle(UUID bookshelfId, UUID ownerId, UUID memberId) throws ValidationException {
        removeMemberValidationHandler.handle(bookshelfId, ownerId, memberId);
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId)
                .orElseThrow(() -> new ValidationException("Bookshelf not found"));
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ValidationException("User not found"));
        List<UUID> bookshelfUserIds = bookshelf.getMembers().stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        bookshelf.getMembers().remove(member);
        bookshelf.getPendingMembers().remove(member);
        bookshelfUserIds.remove(memberId);

        try {
            bookshelfService.deleteBookWrapperMultiple(memberId, bookshelf.getBooks());
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong");
        }
        bookshelfRepository.save(bookshelf);

        BookshelfBookshelfIdCircleDelete200Response response = new BookshelfBookshelfIdCircleDelete200Response();
        response.bookshelfId(bookshelfId);
        response.setUserIds(bookshelfUserIds);
        return response;
    }

    /**
     * Method that gets all members of a circle.
     *
     * @return a list of user ids in the circle
     */
    public List<UUID> getMembers(UUID bookshelfId) throws ValidationException {
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId)
                .orElseThrow(() -> new ValidationException("Bookshelf not found"));
        return bookshelf.getMembers().stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
    }

    /**
     * Method that requests to join a circle.
     */
    public void requestToJoinCircle(UUID bookshelfId, UUID userId) throws ValidationException {
        requestToJoinValidationHandler.handle(bookshelfId, null, userId);
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId)
                .orElseThrow(() -> new ValidationException("Bookshelf not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found"));
        bookshelf.getPendingMembers().add(user);
        bookshelfRepository.save(bookshelf);
    }

    /**
     * Method that gets all pending members of a circle.
     *
     * @return a list of user ids in the pending members list
     */
    public List<UUID> getPendingMembers(UUID bookshelfId, UUID ownerId) throws ValidationException {
        getPendingMembersValidationHandler.handle(bookshelfId, ownerId, null);
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId)
                .orElseThrow(() -> new ValidationException("Bookshelf not found"));
        return bookshelf.getPendingMembers().stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
    }

    /**
     * Method that accepts a pending member.
     *
     * @return a list of user ids in the circle
     */
    public List<UUID> acceptPendingMember(UUID bookshelfId, UUID ownerId, UUID pendingMemberId) throws ValidationException {
        acceptPendingMemberValidationHandler.handle(bookshelfId, ownerId, pendingMemberId);
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId)
                .orElseThrow(() -> new ValidationException("Bookshelf not found"));
        User pendingMember = userRepository.findById(pendingMemberId)
                .orElseThrow(() -> new ValidationException("User not found"));
        List<UUID> bookshelfUserIds = bookshelf.getMembers().stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        bookshelf.getPendingMembers().remove(pendingMember);
        bookshelf.getMembers().add(pendingMember);
        try {
            bookshelfService.addBookWrapperMultiple(pendingMemberId, bookshelf.getBooks());
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong");
        }
        bookshelfRepository.save(bookshelf);

        bookshelfUserIds.add(pendingMemberId);
        return bookshelfUserIds;
    }

    /**
     * Method that rejects a pending member.
     */
    public void rejectPendingMember(UUID bookshelfId, UUID ownerId, UUID pendingMemberId) throws ValidationException {
        rejectPendingMemberValidationHandler.handle(bookshelfId, ownerId, pendingMemberId);
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId)
                .orElseThrow(() -> new ValidationException("Bookshelf not found"));
        User pendingMember = userRepository.findById(pendingMemberId)
                .orElseThrow(() -> new ValidationException("User not found"));
        bookshelf.getPendingMembers().remove(pendingMember);
        bookshelfRepository.save(bookshelf);
    }
}
