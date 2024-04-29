package nl.tudelft.sem.template.example.services;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UtilityService {

    /**
     * Check if a UUID is valid.
     *
     * @param id the UUID to check
     * @return true if the id is a valid UUID, false otherwise
     */
    public boolean validId(UUID id) {
        if (id == null) {
            return false;
        }

        try {
            String stringUUID = id.toString();
            UUID fromString = UUID.fromString(stringUUID);

            return fromString.equals(id);
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Check if a string is null or empty.
     *
     * @param s the string to check
     * @return true if the string is null or empty, false otherwise
     */
    public boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
