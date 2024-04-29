package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.exceptions.ValidationException;

import java.util.UUID;

public abstract class BaseValidator implements Validator {
    private Validator next;

    /**
     * Links the validators in a chain.
     *
     * @param first the first validator in the chain
     * @param chain the rest of the validators in the chain
     * @return the first validator in the chain
     */
    public static BaseValidator link(BaseValidator first, BaseValidator... chain) {
        BaseValidator head = first;
        for (BaseValidator nextInChain : chain) {
            head.setNext(nextInChain);
            head = nextInChain;
        }
        return first;
    }

    /**
     * Sets the next validator in the chain.
     *
     * @param handler the next validator in the chain
     */
    public void setNext(Validator handler) {
        this.next = handler;
    }

    /**
     * Checks the next validator in the chain.
     *
     * @param bookShelfId the bookshelf id
     * @param ownerId     the owner id
     * @param userId      the user id
     * @return true if the next validator in the chain returns true
     * @throws ValidationException if the next validator in the chain throws a ValidationException
     */
    public boolean checkNext(UUID bookShelfId, UUID ownerId, UUID userId) throws ValidationException {
        if (next == null) {
            return true;
        }
        return next.handle(bookShelfId, ownerId, userId);
    }
}
