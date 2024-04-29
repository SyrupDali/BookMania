package nl.tudelft.sem.template.example.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

public class UtilityServiceTest {
    private UtilityService utilityService;

    @BeforeEach
    public void setUp() {
        utilityService = new UtilityService();
    }

    /**
     * Test validId method - OK Response.
     */
    @Test
    public void testValidIdOK() {
        assertTrue(utilityService.validId(UUID.randomUUID()));
    }

    /**
     * Test validId method - Null Response.
     */
    @Test
    public void testValidIdNull() {
        assertFalse(utilityService.validId(null));
    }

    /*
     * Test validId method - Invalid Response.

     * Test cannot exist because UUID.fromString() would throw an exception
     * before the method is even called.
     */

    /*
    @Test
    public void testValidIdInvalid() {
        assertFalse(utilityService.validId(UUID.fromString("Invalid")));
    }
     */

    /**
     * Test isNullOrEmpty method - Empty Response.
     */
    @Test
    public void testIsNullOrEmptyEmptyOK() {
        assertTrue(utilityService.isNullOrEmpty(""));
    }

    /**
     * Test isNullOrEmpty method - Null Response.
     */
    @Test
    public void testIsNullOrEmptyNullOK() {
        assertTrue(utilityService.isNullOrEmpty(null));
    }

    /**
     * Test isNullOrEmpty method - Not Empty Response.
     */
    @Test
    public void testIsNullOrEmptyNotEmptyOK() {
        assertFalse(utilityService.isNullOrEmpty("Not Empty"));
    }
}
