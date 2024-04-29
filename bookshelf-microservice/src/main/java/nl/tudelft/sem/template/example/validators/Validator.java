package nl.tudelft.sem.template.example.validators;

import nl.tudelft.sem.template.example.exceptions.ValidationException;

import java.util.UUID;

public interface Validator {
    void setNext(Validator handler);

    boolean handle(UUID bookShelfId, UUID ownerId, UUID userId) throws ValidationException;
}
