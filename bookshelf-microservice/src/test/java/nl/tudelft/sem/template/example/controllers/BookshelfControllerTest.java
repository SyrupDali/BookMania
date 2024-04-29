package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.services.BookshelfService;
import nl.tudelft.sem.template.example.services.CategoryService;
import javassist.NotFoundException;
import nl.tudelft.sem.template.example.exceptions.NullException;
import nl.tudelft.sem.template.example.services.CircleService;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.*;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.BookshelfBookshelfIdCircleDelete200Response;
import nl.tudelft.sem.template.model.BookshelfBookshelfIdCirclePut200Response;
import org.junit.jupiter.api.BeforeEach;
import nl.tudelft.sem.template.model.BookshelfBookshelfIdPutRequest;
import nl.tudelft.sem.template.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookshelfControllerTest {

    @InjectMocks
    private BookshelfController bookshelfController;

    @Mock
    private CircleService circleService;

    @Mock
    private BookshelfService bookshelfService;
    private UUID bookshelfId;
    private UUID userId;
    private UUID memberId;
    private UUID bookId;

    @Mock
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        bookshelfId = UUID.randomUUID();
        userId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        bookId = UUID.randomUUID();
    }

    public BookshelfPostRequest getPostBookshelfRequest(String title, String description, String privacy) {
        BookshelfPostRequest request = new BookshelfPostRequest();
        request.setTitle(title);
        request.setDescription(description);
        if (privacy == null) {
            request.setPrivacy(null);
        } else if (privacy.equals("PUBLIC")) {
            request.setPrivacy(BookshelfPostRequest.PrivacyEnum.PUBLIC);
        } else {
            request.setPrivacy(BookshelfPostRequest.PrivacyEnum.PRIVATE);
        }
        return request;
    }

    public BookshelfBookshelfIdPutRequest getPutBookshelfRequest(String title, String description, String privacy) {
        BookshelfBookshelfIdPutRequest request = new BookshelfBookshelfIdPutRequest();
        request.setTitle(title);
        request.setDescription(description);
        if (privacy == null) {
            request.setPrivacy(null);
        } else if (privacy.equals("PUBLIC")) {
            request.setPrivacy(BookshelfBookshelfIdPutRequest.PrivacyEnum.PUBLIC);
        } else {
            request.setPrivacy(BookshelfBookshelfIdPutRequest.PrivacyEnum.PRIVATE);
        }
        return request;
    }

    @Test
    public void testBookshelfGetSuccessfully() {
        UUID userId = UUID.randomUUID();
        User u = new User(userId);

        UUID bookshelfIf = UUID.randomUUID();
        Bookshelf bookshelf = new Bookshelf()
                .bookshelfId(bookshelfIf)
                .owner(u)
                .title("Test Bookshelf")
                .description("A test bookshelf")
                .books(new ArrayList<>())
                .privacy(Bookshelf.PrivacyEnum.PUBLIC)
                .members(new ArrayList<>())
                .pendingMembers(new ArrayList<>());

        List<Bookshelf> bookshelves = new ArrayList<>();
        bookshelves.add(bookshelf);
        when(bookshelfService.getAllBookshelves()).thenReturn(bookshelves);

        ResponseEntity<List<Bookshelf>> actualResponse = bookshelfController.bookshelfGet();
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void testBookshelfGetNoContent() {
        ResponseEntity<List<Bookshelf>> actualResponse = bookshelfController.bookshelfGet();
        assertEquals(HttpStatus.NO_CONTENT, actualResponse.getStatusCode());
    }

    @Test
    public void testBookshelfGetInternalServerError() {
        when(bookshelfService.getAllBookshelves()).thenThrow(NullPointerException.class);
        ResponseEntity<List<Bookshelf>> actualResponse = bookshelfController.bookshelfGet();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }

    @Test
    public void testBookshelfGetPublicSuccessfully() {
        UUID userId = UUID.randomUUID();
        User u = new User(userId);

        UUID bookshelfIf = UUID.randomUUID();
        Bookshelf bookshelf = new Bookshelf()
                .bookshelfId(bookshelfIf)
                .owner(u)
                .title("Test Bookshelf")
                .description("A test bookshelf")
                .books(new ArrayList<>())
                .privacy(Bookshelf.PrivacyEnum.PUBLIC)
                .members(new ArrayList<>())
                .pendingMembers(new ArrayList<>());

        List<Bookshelf> bookshelves = new ArrayList<>();
        bookshelves.add(bookshelf);
        when(bookshelfService.getAllPublicBookshelves()).thenReturn(bookshelves);

        ResponseEntity<List<Bookshelf>> actualResponse = bookshelfController.bookshelfGetPublicGet();
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void testBookshelfGetPublicNoContent() {
        List<Bookshelf> bookshelves = new ArrayList<>();
        when(bookshelfService.getAllPublicBookshelves()).thenReturn(bookshelves);

        ResponseEntity<List<Bookshelf>> actualResponse = bookshelfController.bookshelfGetPublicGet();
        assertEquals(HttpStatus.NO_CONTENT, actualResponse.getStatusCode());
    }

    @Test
    public void testBookshelfGetPublicInternalServerError() {
        when(bookshelfService.getAllPublicBookshelves()).thenThrow(NullPointerException.class);

        ResponseEntity<List<Bookshelf>> actualResponse = bookshelfController.bookshelfGetPublicGet();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }

    @Test
    public void testCreateBookshelfSuccessfully() {
        UUID userId = UUID.randomUUID();

        BookshelfPostRequest request = getPostBookshelfRequest("Test title", "Test description", "PUBLIC");
        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfPost(userId, request);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void testCreateBookshelfBadRequestUserNotExists() throws Exception {
        UUID userId = UUID.randomUUID();

        BookshelfPostRequest request = getPostBookshelfRequest("Test title", "Test description", "PUBLIC");
        when(bookshelfService.addBookshelf(request, userId)).thenThrow(IllegalArgumentException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfPost(userId, request);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void testCreateBookshelfBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();

        BookshelfPostRequest request = getPostBookshelfRequest(null, "Test description", "PUBLIC");
        when(bookshelfService.addBookshelf(request, userId)).thenThrow(IllegalArgumentException.class);
        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfPost(userId, request);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void testCreateBookshelfInternalServerError() throws Exception {
        UUID userId = UUID.randomUUID();

        BookshelfPostRequest request = getPostBookshelfRequest("Test title", "Test description", "PUBLIC");
        when(bookshelfService.addBookshelf(request, userId)).thenThrow(NullPointerException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfPost(userId, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }

    @Test
    public void testDeleteBookshelfSuccessful() {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdDelete(bookshelfId, userId);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void testDeleteBookshelfUserNotExists() throws Exception {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(bookshelfService.deleteBookshelf(bookshelfId, userId)).thenThrow(IllegalArgumentException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdDelete(bookshelfId, userId);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void testDeleteBookshelfNotExists() throws Exception {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(bookshelfService.deleteBookshelf(bookshelfId, userId)).thenThrow(IllegalArgumentException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdDelete(bookshelfId, userId);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void testDeleteBookshelfBadRequest() throws Exception {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(bookshelfService.deleteBookshelf(bookshelfId, userId)).thenThrow(IllegalArgumentException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdDelete(bookshelfId, userId);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void testDeleteBookshelfInternalServerError() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        when(bookshelfService.deleteBookshelf(bookshelfId, userId)).thenThrow(NullPointerException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdDelete(bookshelfId, userId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test title", "Test description", "PRIVATE");
        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdPut(bookshelfId, userId, request);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfBadRequest() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest(null, null, "PRIVATE");
        when(bookshelfService.editBookshelf(bookshelfId, userId, request)).thenThrow(IllegalArgumentException.class);
        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdPut(bookshelfId, userId, request);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfNotFound() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test title", "Test description", "PRIVATE");
        when(bookshelfService.editBookshelf(bookshelfId, userId, request)).thenThrow(new ValidationException("Bookshelf not found"));
        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdPut(bookshelfId, userId, request);

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfNotFound2() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test title", "Test description", "PRIVATE");
        when(bookshelfService.editBookshelf(bookshelfId, userId, request)).thenThrow(new ValidationException("Bookshelf id cannot be null"));
        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdPut(bookshelfId, userId, request);

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfUserUnauthorised() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test title", "Test description", "PRIVATE");
        when(bookshelfService.editBookshelf(bookshelfId, userId, request)).thenThrow(new ValidationException("User does not have permission to modify the bookshelf"));
        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdPut(bookshelfId, userId, request);

        assertEquals(HttpStatus.UNAUTHORIZED, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfInternalServerError() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test title", "Test description", "PRIVATE");
        when(bookshelfService.editBookshelf(bookshelfId, userId, request)).thenThrow(new ValidationException("Different message"));
        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdPut(bookshelfId, userId, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfInternalServerError2() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        BookshelfBookshelfIdPutRequest request = getPutBookshelfRequest("Test title", "Test description", "PRIVATE");
        when(bookshelfService.editBookshelf(bookshelfId, userId, request)).thenThrow(NullPointerException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdPut(bookshelfId, userId, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }


    @Test
    public void testEditBookshelfTitleSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditTitlePut(bookshelfId, userId, "Edit title");

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfTitleNotFound() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        when(bookshelfService.editTitleBookshelf(bookshelfId, userId, "Edit title")).thenThrow(new ValidationException(("Bookshelf not found")));

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditTitlePut(bookshelfId, userId, "Edit title");

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfTitleBadRequest() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.editTitleBookshelf(bookshelfId, userId, null)).thenThrow(IllegalArgumentException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditTitlePut(bookshelfId, userId, null);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfTitleNotFound2() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        when(bookshelfService.editTitleBookshelf(bookshelfId, userId, "Test title")).thenThrow(new ValidationException("Bookshelf id cannot be null"));
        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditTitlePut(bookshelfId, userId, "Test title");

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfTitleUserUnauthorised() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        when(bookshelfService.editTitleBookshelf(bookshelfId, userId, "Edit title")).thenThrow(new ValidationException(("User does not have permission to modify the bookshelf")));

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditTitlePut(bookshelfId, userId, "Edit title");

        assertEquals(HttpStatus.UNAUTHORIZED, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfTitleInternalServerError() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        when(bookshelfService.editTitleBookshelf(bookshelfId, userId, "Edit title")).thenThrow(new ValidationException(("Different message")));

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditTitlePut(bookshelfId, userId, "Edit title");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }

    @Test
    public void testEditBookshelfTitleInternalServerError2() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        when(bookshelfService.editTitleBookshelf(bookshelfId, userId, "Edit title")).thenThrow(NullPointerException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditTitlePut(bookshelfId, userId, "Edit title");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }

    @Test
    public void testEditDescriptionSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditDescriptionPut(bookshelfId, userId, "Edit description");

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void testEditDescriptionNotFound() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.editDescriptionBookshelf(bookshelfId, userId, "Edit description")).thenThrow(new ValidationException("Bookshelf not found"));

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditDescriptionPut(bookshelfId, userId, "Edit description");

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    }

    @Test
    public void testEditDescriptionNotFound2() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.editDescriptionBookshelf(bookshelfId, userId, "Edit description")).thenThrow(new ValidationException("Bookshelf id cannot be null"));

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditDescriptionPut(bookshelfId, userId, "Edit description");

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    }

    @Test
    public void testEditDescriptionUserUnauthorised() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.editDescriptionBookshelf(bookshelfId, userId, "Edit description")).thenThrow(new ValidationException("User does not have permission to modify the bookshelf"));

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditDescriptionPut(bookshelfId, userId, "Edit description");

        assertEquals(HttpStatus.UNAUTHORIZED, actualResponse.getStatusCode());
    }

    @Test
    public void testEditDescriptionBadRequest() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.editDescriptionBookshelf(bookshelfId, userId, null)).thenThrow(IllegalArgumentException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditDescriptionPut(bookshelfId, userId, null);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void testEditDescriptionInternalServerError() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        when(bookshelfService.editDescriptionBookshelf(bookshelfId, userId, "Edit description"))
                .thenThrow(NullPointerException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditDescriptionPut(bookshelfId, userId, "Edit description");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }

    @Test
    public void testEditDescriptionInternalServerError2() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        when(bookshelfService.editDescriptionBookshelf(bookshelfId, userId, "Edit description"))
                .thenThrow(new ValidationException("Different error"));

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditDescriptionPut(bookshelfId, userId, "Edit description");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }

    @Test
    public void testEditPrivacySuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditPrivacyPut(bookshelfId, userId, "PRIVATE");

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void testEditPrivacyNotFound() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.editPrivacyBookshelf(bookshelfId, userId, "PRIVATE")).thenThrow(new ValidationException("Bookshelf not found"));

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditPrivacyPut(bookshelfId, userId, "PRIVATE");

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    }

    @Test
    public void testEditPrivacyNotFound2() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.editPrivacyBookshelf(bookshelfId, userId, "PRIVATE")).thenThrow(new ValidationException("Bookshelf id cannot be null"));

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditPrivacyPut(bookshelfId, userId, "PRIVATE");

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    }

    @Test
    public void testEditPrivacyUserUnauthorised() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.editPrivacyBookshelf(bookshelfId, userId, "PRIVATE")).thenThrow(new ValidationException("User does not have permission to modify the bookshelf"));

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditPrivacyPut(bookshelfId, userId, "PRIVATE");

        assertEquals(HttpStatus.UNAUTHORIZED, actualResponse.getStatusCode());
    }

    @Test
    public void testEditPrivacyBadRequest() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.editPrivacyBookshelf(bookshelfId, userId, null)).thenThrow(IllegalArgumentException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditPrivacyPut(bookshelfId, userId, null);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void testEditPrivacyInternalServerError() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        when(bookshelfService.editPrivacyBookshelf(bookshelfId, userId, "PRIVATE"))
                .thenThrow(NullPointerException.class);

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditPrivacyPut(bookshelfId, userId, "PRIVATE");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }

    @Test
    public void testEditPrivacyInternalServerError2() {
        UUID userId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        when(bookshelfService.editPrivacyBookshelf(bookshelfId, userId, "PRIVATE"))
                .thenThrow(new ValidationException("Different error"));

        ResponseEntity<Bookshelf> actualResponse = bookshelfController.bookshelfBookshelfIdEditPrivacyPut(bookshelfId, userId, "PRIVATE");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }


    //Test for the method bookshelfBookshelfIdCircleDelete Success response
    @Test
    public void testRemoveMemberFromCircleSuccess() throws ValidationException {
        BookshelfBookshelfIdCircleDelete200Response expectedResponse =
                new BookshelfBookshelfIdCircleDelete200Response();
        // Set up mocks for the circle service
        when(circleService.removeMemberFromCircle(bookshelfId, userId, memberId)).thenReturn(expectedResponse);

        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(bookshelfId, userId, memberId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    //Test for the method bookshelfBookshelfIdCircleDelete Owner not matching with UnAuthorized response
    @Test
    public void testRemoveMemberFromCircleUserNotMatching() throws ValidationException {
        when(circleService.removeMemberFromCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("User does not match the bookshelf's owner"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleDelete invalid id with Not Found response
    @Test
    public void testRemoveMemberFromCircleInvalidUUID() throws ValidationException {
        when(circleService.removeMemberFromCircle(bookshelfId, userId, memberId))
                .thenThrow(new IllegalArgumentException());

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleDelete User not in circle with Conflict response
    @Test
    public void testRemoveMemberFromCircleUserNotInCircle() throws ValidationException {
        when(circleService.removeMemberFromCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("User not in circle"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleDelete User not found with NotFound response
    @Test
    public void testRemoveMemberFromCircleUserNotFound() throws ValidationException {
        when(circleService.removeMemberFromCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("User not found"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleDelete Bookshelf not found with NotFound response
    @Test
    public void testRemoveMemberFromCircleBookshelfNotFound() throws ValidationException {
        when(circleService.removeMemberFromCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("Bookshelf not found"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleDelete Owner not found with NotFound response
    @Test
    public void testRemoveMemberFromCircleOwnerNotFound() throws ValidationException {
        when(circleService.removeMemberFromCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("Owner not found"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleDelete User id null with BadRequest response
    @Test
    public void testRemoveMemberFromCircleNullUserId() throws ValidationException {
        when(circleService.removeMemberFromCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("User id cannot be null"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleDelete Bookshelf id null with BadRequest response
    @Test
    public void testRemoveMemberFromCircleNullBookshelfId() throws ValidationException {
        when(circleService.removeMemberFromCircle(null, userId, memberId))
                .thenThrow(new ValidationException("Bookshelf id cannot be null"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(null, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleDelete Owner id null with BadRequest response
    @Test
    public void testRemoveMemberFromCircleNullOwnerId() throws ValidationException {
        when(circleService.removeMemberFromCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("Owner id cannot be null"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleDelete other exceptions with internal server error response
    @Test
    public void testRemoveMemberFromCircleOtherExceptions() throws ValidationException {
        when(circleService.removeMemberFromCircle(bookshelfId, userId, memberId))
                .thenThrow(new RuntimeException("Something went wrong"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleDelete other exceptions with internal server error response
    @Test
    public void testRemoveMemberFromCircleOtherExceptions2() throws ValidationException {
        when(circleService.removeMemberFromCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("Something went wrong"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> response =
                bookshelfController.bookshelfBookshelfIdCircleDelete(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }


    //Test for the method bookshelfBookshelfIdCirclePut Success response
    @Test
    public void testGetMembersSuccess() throws ValidationException {
        List<UUID> expectedResponse = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        // Set up mocks for the circle service
        when(circleService.getMembers(bookshelfId)).thenReturn(expectedResponse);

        ResponseEntity<List<UUID>> response = bookshelfController.bookshelfBookshelfIdCircleGet(bookshelfId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut invalid id with BadRequest response
    @Test
    public void testGetMembersInvalidUUID() throws ValidationException {
        when(circleService.getMembers(bookshelfId)).thenThrow(new IllegalArgumentException());

        // Call the controller method
        ResponseEntity<List<UUID>> response = bookshelfController.bookshelfBookshelfIdCircleGet(bookshelfId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut Bookshelf null id with BadRequest response
    @Test
    public void testGetMembersIdNull() throws ValidationException {
        when(circleService.getMembers(null)).thenThrow(new NullPointerException());

        // Call the controller method
        ResponseEntity<List<UUID>> response = bookshelfController.bookshelfBookshelfIdCircleGet(null);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut bookshelf not found with NotFound response
    @Test
    public void testGetMembersBookshelfNotFound() throws ValidationException {
        when(circleService.getMembers(bookshelfId)).thenThrow(new ValidationException("Bookshelf not found"));

        // Call the controller method
        ResponseEntity<List<UUID>> response = bookshelfController.bookshelfBookshelfIdCircleGet(bookshelfId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut other exceptions with internal server error response
    @Test
    public void testGetMembersOtherExceptions() throws ValidationException {
        when(circleService.getMembers(bookshelfId)).thenThrow(new RuntimeException("Something went wrong"));

        // Call the controller method
        ResponseEntity<List<UUID>> response = bookshelfController.bookshelfBookshelfIdCircleGet(bookshelfId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost Success response
    @Test
    public void testAcceptRequestSuccess() throws ValidationException {
        List<UUID> expectedResponse = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        // Set up mocks for the circle service
        when(circleService.acceptPendingMember(bookshelfId, userId, memberId)).thenReturn(expectedResponse);

        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, userId, memberId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    //Test for the method bookshelfBookshelfIdPendingAcceptPost invalid id with BadRequest response
    @Test
    public void testAcceptRequestInvalidUUID() throws ValidationException {
        when(circleService.acceptPendingMember(bookshelfId, userId, memberId))
                .thenThrow(new IllegalArgumentException());

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost User not matching with Forbidden response
    @Test
    public void testAcceptRequestUserNotMatching() throws ValidationException {
        when(circleService.acceptPendingMember(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("User does not match the bookshelf's owner"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost User not found with NotFound response
    @Test
    public void testAcceptRequestUserNotFound() throws ValidationException {
        when(circleService.acceptPendingMember(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("User not found"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost Bookshelf not found with NotFound response
    @Test
    public void testAcceptRequestBookshelfNotFound() throws ValidationException {
        when(circleService.acceptPendingMember(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("Bookshelf not found"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost Owner not found with NotFound response
    @Test
    public void testAcceptRequestOwnerNotFound() throws ValidationException {
        when(circleService.acceptPendingMember(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("Owner not found"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost User id null with BadRequest response
    @Test
    public void testAcceptRequestNullUserId() throws ValidationException {
        when(circleService.acceptPendingMember(bookshelfId, userId, null))
                .thenThrow(new ValidationException("User id cannot be null"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, userId, null);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost Bookshelf id null with BadRequest response
    @Test
    public void testAcceptRequestNullBookshelfId() throws ValidationException {
        when(circleService.acceptPendingMember(null, userId, memberId))
                .thenThrow(new ValidationException("Bookshelf id cannot be null"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(null, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost Owner id null with BadRequest response
    @Test
    public void testAcceptRequestNullOwnerId() throws ValidationException {
        when(circleService.acceptPendingMember(bookshelfId, null, memberId))
                .thenThrow(new ValidationException("Owner id cannot be null"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, null, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost user not in pending list with Conflict response
    @Test
    public void testAcceptRequestUserNotInPendingList() throws ValidationException {
        when(circleService.acceptPendingMember(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("User is not a pending member"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost user already in circle with Conflict response
    @Test
    public void testAcceptRequestUserAlreadyInCircle() throws ValidationException {
        when(circleService.acceptPendingMember(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("User already in circle"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost other exceptions with internal server error response
    @Test
    public void testAcceptRequestOtherExceptions() throws ValidationException {
        when(circleService.acceptPendingMember(bookshelfId, userId, memberId))
                .thenThrow(new RuntimeException("Something went wrong"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingAcceptPost other exceptions with internal server error response
    @Test
    public void testAcceptRequestOtherExceptions2() throws ValidationException {
        when(circleService.acceptPendingMember(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("Something went wrong"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingAcceptPost(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingGet Success response
    @Test
    public void testGetPendingMembersSuccess() throws ValidationException {
        List<UUID> expectedResponse = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        // Set up mocks for the circle service
        when(circleService.getPendingMembers(bookshelfId, userId)).thenReturn(expectedResponse);

        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingGet(bookshelfId, userId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingGet invalid id with BadRequest response
    @Test
    public void testGetPendingMembersInvalidUUID() throws ValidationException {
        when(circleService.getPendingMembers(bookshelfId, userId)).thenThrow(new IllegalArgumentException());

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingGet(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingGet Bookshelf null id with BadRequest response
    @Test
    public void testGetPendingMembersIdNull() throws ValidationException {
        when(circleService.getPendingMembers(null, userId)).thenThrow(new NullPointerException());

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingGet(null, userId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingGet Bookshelf null id with BadRequest response
    @Test
    public void testGetPendingMembersIdNull2() throws ValidationException {
        when(circleService.getPendingMembers(null, userId)).thenThrow(new ValidationException("Owner id cannot be null"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingGet(null, userId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingGet Bookshelf null id with BadRequest response
    @Test
    public void testGetPendingMembersIdNull3() throws ValidationException {
        when(circleService.getPendingMembers(null, userId)).thenThrow(new ValidationException("Bookshelf id cannot be null"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingGet(null, userId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingGet owner not found with NotFound response
    @Test
    public void testGetPendingMembersOwnerNotFound() throws ValidationException {
        when(circleService.getPendingMembers(bookshelfId, userId))
                .thenThrow(new ValidationException("Owner not found"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingGet(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingGet Bookshelf not found with NotFound response
    @Test
    public void testGetPendingMembersBookshelfNotFound() throws ValidationException {
        when(circleService.getPendingMembers(bookshelfId, userId))
                .thenThrow(new ValidationException("Bookshelf not found"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingGet(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingGet owner not matching with Forbidden response
    @Test
    public void testGetPendingMembersOwnerNotMatching() throws ValidationException {
        when(circleService.getPendingMembers(bookshelfId, userId))
                .thenThrow(new ValidationException("User does not match the bookshelf's owner"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingGet(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingGet other exceptions with internal server error response
    @Test
    public void testGetPendingMembersOtherExceptions1() throws ValidationException {
        when(circleService.getPendingMembers(bookshelfId, userId))
                .thenThrow(new ValidationException("Something went wrong"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingGet(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingGet other exceptions with internal server error response
    @Test
    public void testGetPendingMembersOtherExceptions2() throws ValidationException {
        when(circleService.getPendingMembers(bookshelfId, userId))
                .thenThrow(new RuntimeException("Something went wrong"));

        // Call the controller method
        ResponseEntity<List<UUID>> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingGet(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut Success response
    @Test
    public void testRejectRequestSuccess() throws ValidationException {
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(bookshelfId, userId, memberId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut invalid id with BadRequest response
    @Test
    public void testRejectRequestInvalidUUID() throws ValidationException {
        doThrow(new IllegalArgumentException())
                .when(circleService)
                .rejectPendingMember(bookshelfId, userId, memberId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut owner not matching with Forbidden response
    @Test
    public void testRejectRequestUserNotMatching() throws ValidationException {
        doThrow(new ValidationException("User does not match the bookshelf's owner"))
                .when(circleService)
                .rejectPendingMember(bookshelfId, userId, memberId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut User not found with NotFound response
    @Test
    public void testRejectRequestUserNotFound() throws ValidationException {
        doThrow(new ValidationException("User not found"))
                .when(circleService)
                .rejectPendingMember(bookshelfId, userId, memberId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut Bookshelf not found with NotFound response
    @Test
    public void testRejectRequestBookshelfNotFound() throws ValidationException {
        doThrow(new ValidationException("Bookshelf not found"))
                .when(circleService)
                .rejectPendingMember(bookshelfId, userId, memberId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut Owner not found with NotFound response
    @Test
    public void testRejectRequestOwnerNotFound() throws ValidationException {
        doThrow(new ValidationException("Owner not found"))
                .when(circleService)
                .rejectPendingMember(bookshelfId, userId, memberId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut User id null with BadRequest response
    @Test
    public void testRejectRequestNullUserId() throws ValidationException {
        doThrow(new ValidationException("User id cannot be null"))
                .when(circleService)
                .rejectPendingMember(bookshelfId, userId, null);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(bookshelfId, userId, null);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut Bookshelf id null with BadRequest response
    @Test
    public void testRejectRequestNullBookshelfId() throws ValidationException {
        doThrow(new ValidationException("Bookshelf id cannot be null"))
                .when(circleService)
                .rejectPendingMember(null, userId, memberId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(null, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut Owner id null with BadRequest response
    @Test
    public void testRejectRequestNullOwnerId() throws ValidationException {
        doThrow(new ValidationException("Owner id cannot be null"))
                .when(circleService)
                .rejectPendingMember(bookshelfId, null, memberId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(bookshelfId, null, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut user not in pending list with Conflict response
    @Test
    public void testRejectRequestUserNotInPendingList() throws ValidationException {
        doThrow(new ValidationException("User is not a pending member"))
                .when(circleService)
                .rejectPendingMember(bookshelfId, userId, memberId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut other exceptions with internal server error response
    @Test
    public void testRejectRequestOtherExceptions() throws ValidationException {
        doThrow(new RuntimeException("Something went wrong"))
                .when(circleService)
                .rejectPendingMember(bookshelfId, userId, memberId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePendingRejectPut other exceptions with internal server error response
    @Test
    public void testRejectRequestOtherExceptions2() throws ValidationException {
        doThrow(new ValidationException("Something went wrong"))
                .when(circleService)
                .rejectPendingMember(bookshelfId, userId, memberId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCirclePendingRejectPut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut Success response
    @Test
    public void testAddMemberToCircleSuccess() throws ValidationException {
        BookshelfBookshelfIdCirclePut200Response expectedResponse = new BookshelfBookshelfIdCirclePut200Response();
        when(circleService.addMemberToCircle(bookshelfId, userId, memberId)).thenReturn(expectedResponse);
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(bookshelfId, userId, memberId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    //Test for the method bookshelfBookshelfIdCirclePut invalid id with BadRequest response
    @Test
    public void testAddMemberToCircleInvalidUUID() throws ValidationException {
        when(circleService.addMemberToCircle(bookshelfId, userId, memberId))
                .thenThrow(new IllegalArgumentException());

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut owner not matching with UnAuthorized response
    @Test
    public void testAddMemberToCircleUserNotMatching() throws ValidationException {
        when(circleService.addMemberToCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("User does not match the bookshelf's owner"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut User not found with NotFound response
    @Test
    public void testAddMemberToCircleUserNotFound() throws ValidationException {
        when(circleService.addMemberToCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("User not found"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut Bookshelf not found with NotFound response
    @Test
    public void testAddMemberToCircleBookshelfNotFound() throws ValidationException {
        when(circleService.addMemberToCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("Bookshelf not found"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut Owner not found with NotFound response
    @Test
    public void testAddMemberToCircleOwnerNotFound() throws ValidationException {
        when(circleService.addMemberToCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("Owner not found"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut Owner id null with BadRequest response
    @Test
    public void testAddMemberToCircleNullOwnerId() throws ValidationException {
        when(circleService.addMemberToCircle(bookshelfId, null, memberId))
                .thenThrow(new ValidationException("Owner id cannot be null"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(bookshelfId, null, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut User id null with BadRequest response
    @Test
    public void testAddMemberToCircleNullUserId() throws ValidationException {
        when(circleService.addMemberToCircle(bookshelfId, userId, null))
                .thenThrow(new ValidationException("User id cannot be null"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(bookshelfId, userId, null);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut User id null with BadRequest response
    @Test
    public void testAddMemberToCircleNullBookshelfId() throws ValidationException {
        when(circleService.addMemberToCircle(null, userId, memberId))
                .thenThrow(new ValidationException("Bookshelf id cannot be null"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(null, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut user already in circle with Conflict response
    @Test
    public void testAddMemberToCircleUserAlreadyInCircle() throws ValidationException {
        when(circleService.addMemberToCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("User already in circle"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut other exceptions with internal server error response
    @Test
    public void testAddMemberToCircleOtherExceptions() throws ValidationException {
        when(circleService.addMemberToCircle(bookshelfId, userId, memberId))
                .thenThrow(new RuntimeException("Something went wrong"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCirclePut other exceptions with internal server error response
    @Test
    public void testAddMemberToCircleOtherExceptions2() throws ValidationException {
        when(circleService.addMemberToCircle(bookshelfId, userId, memberId))
                .thenThrow(new ValidationException("Something went wrong"));

        // Call the controller method
        ResponseEntity<BookshelfBookshelfIdCirclePut200Response> response =
                bookshelfController.bookshelfBookshelfIdCirclePut(bookshelfId, userId, memberId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleRequestPost Success response
    @Test
    public void testRequestToJoinCircleSuccess() throws ValidationException {
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCircleRequestPost(bookshelfId, userId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleRequestPost invalid id with BadRequest response
    @Test
    public void testRequestToJoinCircleInvalidUUID() throws ValidationException {
        doThrow(new IllegalArgumentException())
                .when(circleService)
                .requestToJoinCircle(bookshelfId, userId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCircleRequestPost(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleRequestPost null bookshelf id with BadRequest response
    @Test
    public void testRequestToJoinCircleNullId() throws ValidationException {
        doThrow(new NullPointerException())
                .when(circleService)
                .requestToJoinCircle(null, userId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCircleRequestPost(null, userId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleRequestPost user not found with NotFound response
    @Test
    public void testRequestToJoinCircleUserNotFound() throws ValidationException {
        doThrow(new ValidationException("User not found"))
                .when(circleService)
                .requestToJoinCircle(bookshelfId, userId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCircleRequestPost(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleRequestPost Bookshelf null id with bad request response
    @Test
    public void testRequestToJoinCircleNullBookshelfId() throws ValidationException {
        doThrow(new ValidationException("Bookshelf id cannot be null"))
                .when(circleService)
                .requestToJoinCircle(null, userId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCircleRequestPost(null, userId);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleRequestPost User null id with bad request response
    @Test
    public void testRequestToJoinCircleNullUserId() throws ValidationException {
        doThrow(new ValidationException("User id cannot be null"))
                .when(circleService)
                .requestToJoinCircle(bookshelfId, null);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCircleRequestPost(bookshelfId, null);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleRequestPost Bookshelf not found with NotFound response
    @Test
    public void testRequestToJoinCircleBookshelfNotFound() throws ValidationException {
        doThrow(new ValidationException("Bookshelf not found"))
                .when(circleService)
                .requestToJoinCircle(bookshelfId, userId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCircleRequestPost(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleRequestPost user already in circle with Conflict response
    @Test
    public void testRequestToJoinCircleUserAlreadyInCircle() throws ValidationException {
        doThrow(new ValidationException("User already in circle"))
                .when(circleService)
                .requestToJoinCircle(bookshelfId, userId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCircleRequestPost(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleRequestPost user already in pending list with Conflict response
    @Test
    public void testRequestToJoinCircleUserAlreadyInPendingList() throws ValidationException {
        doThrow(new ValidationException("User already requested to join circle"))
                .when(circleService)
                .requestToJoinCircle(bookshelfId, userId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCircleRequestPost(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleRequestPost other exceptions with internal server error response
    @Test
    public void testRequestToJoinCircleOtherExceptions() throws ValidationException {
        doThrow(new RuntimeException("Something went wrong"))
                .when(circleService)
                .requestToJoinCircle(bookshelfId, userId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCircleRequestPost(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //Test for the method bookshelfBookshelfIdCircleRequestPost other exceptions with internal server error response
    @Test
    public void testRequestToJoinCircleOtherExceptions2() throws ValidationException {
        doThrow(new ValidationException("Something went wrong"))
                .when(circleService)
                .requestToJoinCircle(bookshelfId, userId);

        // Call the controller method
        ResponseEntity<Void> response =
                bookshelfController.bookshelfBookshelfIdCircleRequestPost(bookshelfId, userId);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testRemoveBookFromBookshelfSuccessful() throws Exception {
        // Set up mocks for the bookshelf service
        Bookshelf expectedBookshelf = new Bookshelf();
        when(bookshelfService.removeBookFromBookshelf(bookshelfId, userId, bookId)).thenReturn(expectedBookshelf);

        ResponseEntity<Bookshelf> response =
                bookshelfController.bookshelfBookshelfIdBookDelete(bookshelfId, userId, bookId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedBookshelf, response.getBody());
    }

    @Test
    public void testRemoveBookFromBookshelfBadRequest() throws Exception {
        // Set up mocks for the bookshelf service
        when(bookshelfService.removeBookFromBookshelf(bookshelfId, userId, bookId))
                .thenThrow(new IllegalArgumentException());

        ResponseEntity<Bookshelf> response =
                bookshelfController.bookshelfBookshelfIdBookDelete(bookshelfId, userId, bookId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testRemoveBookFromBookshelfNotFound() throws Exception {
        // Set up mocks for the bookshelf service
        when(bookshelfService.removeBookFromBookshelf(bookshelfId, userId, bookId))
                .thenThrow(new NotFoundException("Book not found"));

        ResponseEntity<Bookshelf> response =
                bookshelfController.bookshelfBookshelfIdBookDelete(bookshelfId, userId, bookId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testRemoveBookFromBookshelfUnauthorized() throws Exception {
        // Set up mocks for the bookshelf service
        doThrow(new ValidationException("User does not have permission to modify the bookshelf"))
                .when(bookshelfService).removeBookFromBookshelf(bookshelfId, userId, bookId);

        ResponseEntity<Bookshelf> response =
                bookshelfController.bookshelfBookshelfIdBookDelete(bookshelfId, userId, bookId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testRemoveBookFromBookshelfInternalServerError() throws Exception {
        // Set up mocks for the bookshelf service
        doThrow(new RuntimeException("Unexpected exception")).when(bookshelfService)
                .removeBookFromBookshelf(bookshelfId, userId, bookId);

        ResponseEntity<Bookshelf> response =
                bookshelfController.bookshelfBookshelfIdBookDelete(bookshelfId, userId, bookId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testBookshelfBookshelfIdBookDeleteBookshelfNotFound() throws Exception {
        when(bookshelfService.removeBookFromBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new ValidationException("Bookshelf not found"));
        assertThat(bookshelfController.bookshelfBookshelfIdBookDelete(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testBookshelfBookshelfIdBookDeleteUserNotFound() throws Exception {
        when(bookshelfService.removeBookFromBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new ValidationException("User not found"));
        assertThat(bookshelfController.bookshelfBookshelfIdBookDelete(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testBookshelfBookshelfIdBookDeleteBookshelfNull() throws Exception {
        when(bookshelfService.removeBookFromBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new ValidationException("Bookshelf id cannot be null"));
        assertThat(bookshelfController.bookshelfBookshelfIdBookDelete(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testBookshelfBookshelfIdBookDeleteUserNull() throws Exception {
        when(bookshelfService.removeBookFromBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new ValidationException("User id cannot be null"));
        assertThat(bookshelfController.bookshelfBookshelfIdBookDelete(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testBookshelfBookshelfIdBookDeleteOtherValidationException() throws Exception {
        when(bookshelfService.removeBookFromBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new ValidationException("Something went wrong"));
        assertThat(bookshelfController.bookshelfBookshelfIdBookDelete(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testAddBookToBookshelfSuccessful() throws Exception {
        Bookshelf expectedBookshelf = new Bookshelf();
        when(bookshelfService.addBookToBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(expectedBookshelf);
        ResponseEntity<Bookshelf> response =
                bookshelfController.bookshelfBookshelfIdBookPut(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedBookshelf, response.getBody());

    }

    @Test
    public void testAddBookToBookshelfBadRequest() throws Exception {
        // Set up mocks for the bookshelf service
        doThrow(new IllegalArgumentException()).when(bookshelfService).addBookToBookshelf(bookshelfId, userId, bookId);

        ResponseEntity<Bookshelf> response =
                bookshelfController.bookshelfBookshelfIdBookPut(bookshelfId, userId, bookId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testAddBookToBookshelfNotFound() throws Exception {
        // Set up mocks for the bookshelf service
        doThrow(new NotFoundException("Book not found"))
                .when(bookshelfService).addBookToBookshelf(bookshelfId, userId, bookId);

        ResponseEntity<Bookshelf> response =
                bookshelfController.bookshelfBookshelfIdBookPut(bookshelfId, userId, bookId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testAddBookToBookshelfUnauthorized() throws Exception {
        // Set up mocks for the bookshelf service
        doThrow(new ValidationException("User does not have permission to modify the bookshelf"))
                .when(bookshelfService).addBookToBookshelf(bookshelfId, userId, bookId);

        ResponseEntity<Bookshelf> response =
                bookshelfController.bookshelfBookshelfIdBookPut(bookshelfId, userId, bookId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testAddBookToBookshelfInternalServerError() throws Exception {
        // Set up mocks for the bookshelf service
        doThrow(new RuntimeException("Unexpected exception")).when(bookshelfService)
                .addBookToBookshelf(bookshelfId, userId, bookId);

        ResponseEntity<Bookshelf> response =
                bookshelfController.bookshelfBookshelfIdBookPut(bookshelfId, userId, bookId);

        // Verify that the response is as expected
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testEditBookshelfNotPartOfCircle() {
        CategoryService categoryService = mock(CategoryService.class);

        BookshelfController controller = new BookshelfController(circleService, bookshelfService, categoryService);

        UUID ownerId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();

        BookshelfBookshelfIdPutRequest bookshelfBookshelfIdPutRequest = new BookshelfBookshelfIdPutRequest()
                .title("title")
                .description("description")
                .privacy(BookshelfBookshelfIdPutRequest.PrivacyEnum.PUBLIC);
        doThrow(new ValidationException("User does not have permission to modify the bookshelf"))
                .when(bookshelfService).editBookshelf(any(UUID.class), any(UUID.class), eq(bookshelfBookshelfIdPutRequest));
        ResponseEntity<Bookshelf> response = controller.bookshelfBookshelfIdPut(bookshelfId, ownerId, bookshelfBookshelfIdPutRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testEditBookshelfDescriptionNotPartOfCircle() {
        CategoryService categoryService = mock(CategoryService.class);

        BookshelfController controller = new BookshelfController(circleService, bookshelfService, categoryService);

        UUID ownerId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        doThrow(new ValidationException("User does not have permission to modify the bookshelf"))
                .when(bookshelfService).editDescriptionBookshelf(any(UUID.class), any(UUID.class), anyString());

        ResponseEntity<Bookshelf> response = controller.bookshelfBookshelfIdEditDescriptionPut(bookshelfId, ownerId, "description");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testEditBookshelfTitleNotPartOfCircle() {
        CategoryService categoryService = mock(CategoryService.class);

        BookshelfController controller = new BookshelfController(circleService, bookshelfService, categoryService);

        UUID ownerId = UUID.randomUUID();
        UUID bookshelfId = UUID.randomUUID();
        doThrow(new ValidationException("User does not have permission to modify the bookshelf"))
                .when(bookshelfService).editTitleBookshelf(any(UUID.class), any(UUID.class), anyString());

        ResponseEntity<Bookshelf> response = controller.bookshelfBookshelfIdEditTitlePut(bookshelfId, ownerId, "title");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    @Test
    public void circleInsightsBooksReadOk() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.getNumberOfBooksReadCircle(bookshelfId)).thenReturn(5);
        ResponseEntity<Integer> actual = bookshelfController.bookshelfBookshelfIdCircleInsightsBooksReadGet(bookshelfId);
        assertEquals(5, actual.getBody());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    public void circleInsightsBooksReadBadRequest() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.getNumberOfBooksReadCircle(bookshelfId)).thenThrow(IllegalArgumentException.class);
        ResponseEntity<Integer> actual = bookshelfController.bookshelfBookshelfIdCircleInsightsBooksReadGet(bookshelfId);
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void circleInsightsBooksReadNotFound() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.getNumberOfBooksReadCircle(bookshelfId)).thenThrow(NotFoundException.class);
        ResponseEntity<Integer> actual = bookshelfController.bookshelfBookshelfIdCircleInsightsBooksReadGet(bookshelfId);
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void circleInsightsBooksReadInternalServerError() throws Exception {
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.getNumberOfBooksReadCircle(bookshelfId)).thenThrow(RuntimeException.class);
        ResponseEntity<Integer> actual = bookshelfController.bookshelfBookshelfIdCircleInsightsBooksReadGet(bookshelfId);
        assertNull(actual.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
    }

    @Test
    public void circleInsightsPreferredGenresOk() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.getPreferredGenresCircle(bookshelfId)).thenReturn(List.of("Genre"));
        ResponseEntity<List<String>> actual = bookshelfController.bookshelfBookshelfIdCircleInsightsPreferredGenresGet(bookshelfId);
        assertEquals(List.of("Genre"), actual.getBody());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    public void circleInsightsPreferredGenresBadRequest() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.getPreferredGenresCircle(bookshelfId)).thenThrow(IllegalArgumentException.class);
        ResponseEntity<List<String>> actual = bookshelfController.bookshelfBookshelfIdCircleInsightsPreferredGenresGet(bookshelfId);
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void circleInsightsPreferredGenresNotFound() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.getPreferredGenresCircle(bookshelfId)).thenThrow(NotFoundException.class);
        ResponseEntity<List<String>> actual = bookshelfController.bookshelfBookshelfIdCircleInsightsPreferredGenresGet(bookshelfId);
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void circleInsightsPreferredGenresInternalServerError() throws Exception {
        UUID bookshelfId = UUID.randomUUID();
        when(bookshelfService.getPreferredGenresCircle(bookshelfId)).thenThrow(RuntimeException.class);
        ResponseEntity<List<String>> actual = bookshelfController.bookshelfBookshelfIdCircleInsightsPreferredGenresGet(bookshelfId);
        assertNull(actual.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());
    }


    @Test
    public void testBookshelfBookshelfIdBookPutBookshelfNotFound() throws Exception {
        when(bookshelfService.addBookToBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new ValidationException("Bookshelf not found"));
        assertThat(bookshelfController.bookshelfBookshelfIdBookPut(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testBookshelfBookshelfIdBookPutUserNotFound() throws Exception {
        when(bookshelfService.addBookToBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new ValidationException("User not found"));
        assertThat(bookshelfController.bookshelfBookshelfIdBookPut(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testBookshelfBookshelfIdBookPutBookshelfNull() throws Exception {
        when(bookshelfService.addBookToBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new ValidationException("Bookshelf id cannot be null"));
        assertThat(bookshelfController.bookshelfBookshelfIdBookPut(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testBookshelfBookshelfIdBookPutUserNull() throws Exception {
        when(bookshelfService.addBookToBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new ValidationException("User id cannot be null"));
        assertThat(bookshelfController.bookshelfBookshelfIdBookPut(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testBookshelfBookshelfIdBookPutBookAlreadyExist() throws Exception {
        when(bookshelfService.addBookToBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new ValidationException("Book already exists in the bookshelf"));
        assertThat(bookshelfController.bookshelfBookshelfIdBookPut(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isEqualTo(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @Test
    public void testBookshelfBookshelfIdBookPutOtherValidationException() throws Exception {
        when(bookshelfService.addBookToBookshelf(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenThrow(new ValidationException("Some other validation exception"));
        assertThat(bookshelfController.bookshelfBookshelfIdBookPut(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testEditCategoryPut200() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Bookshelf b = new Bookshelf(bookshelfId, new User(userId), "title", "desc",
                new ArrayList<>(), Bookshelf.PrivacyEnum.PUBLIC, new ArrayList<>(), new ArrayList<>());

        when(categoryService.setCategoryAuthenticated(userId, bookshelfId, categoryId)).thenReturn(b);
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryPut(bookshelfId, userId, categoryId)).isEqualTo(ResponseEntity.ok(b));
    }

    @Test
    public void testEditCategoryPut404() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.setCategoryAuthenticated(userId, bookshelfId, categoryId)).thenThrow(new NotFoundException("Not found something"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryPut(bookshelfId, userId, categoryId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testEditCategoryPut404Validation() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.setCategoryAuthenticated(userId, bookshelfId, categoryId)).thenThrow(new ValidationException("User not found"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryPut(bookshelfId, userId, categoryId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testEditCategoryPut400() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.setCategoryAuthenticated(userId, bookshelfId, categoryId)).thenThrow(new NullException("Not found something"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryPut(bookshelfId, userId, categoryId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testEditCategoryPut400Validation() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.setCategoryAuthenticated(userId, bookshelfId, categoryId)).thenThrow(new ValidationException("User id cannot be null"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryPut(bookshelfId, userId, categoryId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testEditCategoryPut401() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.setCategoryAuthenticated(userId, bookshelfId, categoryId)).thenThrow(new UnsupportedOperationException("User not own bookshelf or category"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryPut(bookshelfId, userId, categoryId)).isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Test
    public void testEditCategoryPut401Validation() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.setCategoryAuthenticated(userId, bookshelfId, categoryId)).thenThrow(new ValidationException("User does not have permission to modify the bookshelf"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryPut(bookshelfId, userId, categoryId)).isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Test
    public void testEditCategoryPut500() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.setCategoryAuthenticated(userId, bookshelfId, categoryId)).thenThrow(new RuntimeException("Some exception"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryPut(bookshelfId, userId, categoryId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testEditCategoryPut500Validation() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.setCategoryAuthenticated(userId, bookshelfId, categoryId)).thenThrow(new ValidationException("Some exception"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryPut(bookshelfId, userId, categoryId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testEditCategoryDelete200() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Bookshelf b = new Bookshelf(bookshelfId, new User(userId), "title", "desc",
                new ArrayList<>(), Bookshelf.PrivacyEnum.PUBLIC, new ArrayList<>(), new ArrayList<>());

        when(categoryService.removeCategoryAuthenticated(userId, bookshelfId)).thenReturn(b);
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryDelete(bookshelfId, userId)).isEqualTo(ResponseEntity.ok(b));
    }

    @Test
    public void testEditCategoryDelete404() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.removeCategoryAuthenticated(userId, bookshelfId)).thenThrow(new NotFoundException("Not found something"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryDelete(bookshelfId, userId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testEditCategoryDelete404Validation() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.removeCategoryAuthenticated(userId, bookshelfId)).thenThrow(new ValidationException("User not found"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryDelete(bookshelfId, userId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testEditCategoryDelete401Validation() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.removeCategoryAuthenticated(userId, bookshelfId)).thenThrow(new ValidationException("User does not have permission to modify the bookshelf"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryDelete(bookshelfId, userId)).isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Test
    public void testEditCategoryDelete400() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.removeCategoryAuthenticated(userId, bookshelfId)).thenThrow(new NullException("Not found something"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryDelete(bookshelfId, userId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testEditCategoryDelete400Validation() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.removeCategoryAuthenticated(userId, bookshelfId)).thenThrow(new ValidationException("User id cannot be null"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryDelete(bookshelfId, userId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testEditCategoryDelete401() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.removeCategoryAuthenticated(userId, bookshelfId)).thenThrow(new UnsupportedOperationException("User not own bookshelf or category"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryDelete(bookshelfId, userId)).isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Test
    public void testEditCategoryDelete500() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.removeCategoryAuthenticated(userId, bookshelfId)).thenThrow(new RuntimeException("Some exception"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryDelete(bookshelfId, userId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testEditCategoryDelete500Validation() throws NotFoundException, NullException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.removeCategoryAuthenticated(userId, bookshelfId)).thenThrow(new ValidationException("Some exception"));
        assertThat(bookshelfController.bookshelfBookshelfIdEditCategoryDelete(bookshelfId, userId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePutSuccess() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenReturn(new ArrayList<>());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.ok(new ArrayList<>()));
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut400() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new IllegalArgumentException("Null exception"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut400BookshelfIdNull() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new ValidationException("Bookshelf id cannot be null"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut400UserIdNull() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new ValidationException("User id cannot be null"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut400OwnerIdNull() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new ValidationException("Owner id cannot be null"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut400BookIdNull() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new ValidationException("Book id cannot be null"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut404BookshelfNotFound() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new ValidationException("Bookshelf not found"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut404UserNotFound() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new ValidationException("User not found"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut404BookNotFound() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new ValidationException("Book not found"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut409() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new ValidationException("Book already exists in the bookshelf"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut403() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new ValidationException("User does not have permission to modify the bookshelf"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut500() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new RuntimeException("Some exception"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookAddMultiplePut500Validation() throws Exception {
        when(bookshelfService.addMultipleBooksToBookshelf(any(UUID.class), any(UUID.class), any())).thenThrow(new ValidationException("Some exception"));
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>())).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutSuccess() throws Exception {
        doNothing().when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.OK).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutBookshelfIdNull() throws Exception {
        doThrow(new ValidationException("Bookshelf id cannot be null"))
                .when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutUserIdNull() throws Exception {
        doThrow(new ValidationException("User id cannot be null"))
                .when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutBookIdNull() throws Exception {
        doThrow(new ValidationException("Book id cannot be null"))
                .when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutBookshelfNotFound() throws Exception {
        doThrow(new ValidationException("Bookshelf not found"))
                .when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutUserNotFound() throws Exception {
        doThrow(new ValidationException("User not found"))
                .when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutBookNotFound() throws Exception {
        doThrow(new ValidationException("Book not found"))
                .when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutBookWrapperNotFound() throws Exception {
        doThrow(new ValidationException("Book wrapper not found"))
                .when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutConflict() throws Exception {
        doThrow(new ValidationException("Book is not found in specified bookshelf"))
                .when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutUserUnauthorised() throws Exception {
        doThrow(new ValidationException("User does not have permission to modify the bookshelf"))
                .when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutInternalServerError1() throws Exception {
        doThrow(new ValidationException("Different error"))
                .when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookDeleteMultiplePutInternalServerError() throws Exception {
        doThrow(NullPointerException.class)
                .when(bookshelfService).removeMultipleBooksFromBookshelf(any(UUID.class), any(UUID.class), any());
        assertThat(bookshelfController.bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID.randomUUID(), UUID.randomUUID(), new ArrayList<>()))
                .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testBookshelfBookshelfIdGet200() throws Exception {
        Bookshelf b = new Bookshelf(UUID.randomUUID(), new User(UUID.randomUUID()), "title", "desc",
                new ArrayList<>(), Bookshelf.PrivacyEnum.PUBLIC, new ArrayList<>(), new ArrayList<>());
        when(bookshelfService.getBookshelfById(bookshelfId)).thenReturn(b);
        assertThat(bookshelfController.bookshelfBookshelfIdGet(bookshelfId)).isEqualTo(ResponseEntity.ok(b));
    }

    @Test
    public void testBookshelfBookshelfIdGet404() throws Exception {
        when(bookshelfService.getBookshelfById(bookshelfId)).thenThrow(new NotFoundException("Bookshelf not found"));
        assertThat(bookshelfController.bookshelfBookshelfIdGet(bookshelfId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testBookshelfBookshelfIdGet500() throws Exception {
        when(bookshelfService.getBookshelfById(bookshelfId)).thenThrow(new RuntimeException("Some exception"));
        assertThat(bookshelfController.bookshelfBookshelfIdGet(bookshelfId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testBookshelfBookshelfIdUserIdBookGet400() throws Exception {
        when(bookshelfService.getBookshelfById(bookshelfId)).thenThrow(new IllegalArgumentException());
        assertThat(bookshelfController.bookshelfBookshelfIdGet(bookshelfId)).isEqualTo(ResponseEntity.badRequest().build());
    }
}