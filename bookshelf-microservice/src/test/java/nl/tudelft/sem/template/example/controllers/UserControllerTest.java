package nl.tudelft.sem.template.example.controllers;

import javassist.NotFoundException;
import nl.tudelft.sem.template.example.services.CategoryService;
import nl.tudelft.sem.template.model.User;
import org.junit.jupiter.api.BeforeEach;
import nl.tudelft.sem.template.example.exceptions.InvalidDataException;
import nl.tudelft.sem.template.example.exceptions.NullException;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.Category;
import nl.tudelft.sem.template.model.UserUserIdCategoriesPostRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nl.tudelft.sem.template.example.services.UserService;
import nl.tudelft.sem.template.model.Book;
import nl.tudelft.sem.template.model.BookWrapper;
import nl.tudelft.sem.template.model.Bookshelf;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    Book b;
    Bookshelf bs;
    User user;

    @BeforeEach
    public void setup() {
        userController = new UserController(userService, categoryService);

        user = new User();
        user.setUserId(UUID.randomUUID());
        bs = new Bookshelf().title("title").bookshelfId(UUID.randomUUID()).owner(user);
        b = new Book().title("title").authors(List.of("author")).numPages(100).bookId(UUID.randomUUID());
    }

    @Test
    public void userUserIdBookshelvesGetOk() throws NotFoundException {
        Bookshelf shelf = new Bookshelf().bookshelfId(UUID.randomUUID());
        when(userService.getByOwner(user.getUserId())).thenReturn(List.of(shelf));
        ResponseEntity<List<Bookshelf>> actual = userController.userUserIdBookshelvesGet(user.getUserId());
        assertEquals(List.of(shelf), actual.getBody());
        assertEquals(actual.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void userUserIdBookshelvesGetNoContent() throws NotFoundException {
        when(userService.getByOwner(user.getUserId())).thenThrow(new EmptyResultDataAccessException(0));
        ResponseEntity<List<Bookshelf>> actual = userController.userUserIdBookshelvesGet(user.getUserId());

        assertEquals(actual.getStatusCode(), HttpStatus.NO_CONTENT);
    }

    @Test
    public void userUserIdBookshelvesGetBadRequest() throws NotFoundException {
        when(userService.getByOwner(user.getUserId())).thenThrow(new IllegalArgumentException());
        ResponseEntity<List<Bookshelf>> actual = userController.userUserIdBookshelvesGet(user.getUserId());

        assertEquals(actual.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void userUserIdBookshelvesGetNotFound() throws NotFoundException {
        when(userService.getByOwner(user.getUserId())).thenThrow(new NotFoundException("User not found"));
        ResponseEntity<List<Bookshelf>> actual = userController.userUserIdBookshelvesGet(user.getUserId());

        assertEquals(actual.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void userUserIdBookshelvesGetServerError() throws NotFoundException {
        when(userService.getByOwner(user.getUserId())).thenThrow(new IndexOutOfBoundsException());
        ResponseEntity<List<Bookshelf>> actual = userController.userUserIdBookshelvesGet(user.getUserId());

        assertEquals(actual.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Test
    public void userUserIdInsightsBooksReadGetOk() throws Exception {
        when(userService.getNumberOfBooksRead(user.getUserId())).thenReturn(100);
        ResponseEntity<Integer> actual = userController.userUserIdInsightsBooksReadGet(user.getUserId());

        assertEquals(actual.getBody(), 100);
        assertEquals(actual.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void userUserIdInsightsBooksReadGetBadRequest() throws Exception {
        when(userService.getNumberOfBooksRead(user.getUserId())).thenThrow(new IllegalArgumentException());
        ResponseEntity<Integer> actual = userController.userUserIdInsightsBooksReadGet(user.getUserId());

        assertEquals(actual.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void userUserIdInsightsBooksReadGetNotFound() throws Exception {
        when(userService.getNumberOfBooksRead(user.getUserId())).thenThrow(new NotFoundException("User not found"));
        ResponseEntity<Integer> actual = userController.userUserIdInsightsBooksReadGet(user.getUserId());

        assertEquals(actual.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void userUserIdInsightsBooksReadGetServerError() throws Exception {
        when(userService.getNumberOfBooksRead(user.getUserId())).thenThrow(new Exception());
        ResponseEntity<Integer> actual = userController.userUserIdInsightsBooksReadGet(user.getUserId());

        assertEquals(actual.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Test
    public void userUserIdInsightsPreferredGenresOk() throws Exception {
        List<String> returnedList = Arrays.asList("Genre1", "Genre2", "Genre3");
        when(userService.getPreferredGenres(user.getUserId())).thenReturn(returnedList);
        ResponseEntity<List<String>> actual = userController.userUserIdInsightsPreferredGenresGet(user.getUserId());

        assertEquals(actual.getBody(), returnedList);
        assertEquals(actual.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void userUserIdInsightsPreferredGenresBadRequest() throws Exception {
        when(userService.getPreferredGenres(user.getUserId())).thenThrow(new IllegalArgumentException());
        ResponseEntity<List<String>> actual = userController.userUserIdInsightsPreferredGenresGet(user.getUserId());

        assertEquals(actual.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void userUserIdInsightsPreferredGenresNotFound() throws Exception {
        when(userService.getPreferredGenres(user.getUserId())).thenThrow(new NotFoundException("User not found"));
        ResponseEntity<List<String>> actual = userController.userUserIdInsightsPreferredGenresGet(user.getUserId());

        assertEquals(actual.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void userUserIdInsightsPreferredGenresServerError() throws Exception {
        when(userService.getPreferredGenres(user.getUserId())).thenThrow(new Exception());
        ResponseEntity<List<String>> actual = userController.userUserIdInsightsPreferredGenresGet(user.getUserId());

        assertEquals(actual.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testUserIdCategoriesGet200() {
        UUID userId = UUID.randomUUID();
        Category c1 = new Category(UUID.randomUUID(), new User(userId), new ArrayList<>(), "name", "desc");
        Category c2 = new Category(UUID.randomUUID(), new User(userId), new ArrayList<>(), "name2", "desc2");

        when(categoryService.getAllCategoriesForUser(userId)).thenReturn(List.of(c1, c2));
        assertThat(userController.userUserIdCategoriesGet(userId)).isEqualTo(ResponseEntity.ok(List.of(c1, c2)));
    }

    @Test
    public void testUserIdCategoriesGet204() {
        UUID userId = UUID.randomUUID();

        when(categoryService.getAllCategoriesForUser(userId)).thenReturn(new ArrayList<>());
        assertThat(userController.userUserIdCategoriesGet(userId)).isEqualTo(ResponseEntity.noContent().build());
    }

    @Test
    public void testUserIdCategoriesGet404() {
        UUID userId = UUID.randomUUID();

        when(categoryService.getAllCategoriesForUser(userId)).thenThrow(new ValidationException("User not found"));
        assertThat(userController.userUserIdCategoriesGet(userId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testUserIdCategoriesGet400() {
        UUID userId = UUID.randomUUID();

        when(categoryService.getAllCategoriesForUser(userId)).thenThrow(new ValidationException("User id cannot be null"));
        assertThat(userController.userUserIdCategoriesGet(userId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserIdCategoriesGet500Validation() {
        UUID userId = UUID.randomUUID();

        when(categoryService.getAllCategoriesForUser(userId)).thenThrow(new ValidationException("Some exception"));
        assertThat(userController.userUserIdCategoriesGet(userId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testUserIdCategoriesGet500() {
        UUID userId = UUID.randomUUID();

        when(categoryService.getAllCategoriesForUser(userId)).thenThrow(new RuntimeException("Some exception"));
        assertThat(userController.userUserIdCategoriesGet(userId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testUserIdCategoriesPost200() throws NotFoundException, NullException, InvalidDataException {
        UUID userId = UUID.randomUUID();
        Category c1 = new Category(UUID.randomUUID(), new User(userId), new ArrayList<>(), "name", "desc");
        Category c2 = new Category(UUID.randomUUID(), new User(userId), new ArrayList<>(), "name2", "desc2");
        UserUserIdCategoriesPostRequest params = new UserUserIdCategoriesPostRequest();
        params.setTitle("name");
        params.setDescription("desc");

        when(categoryService.createCategory(userId, params.getTitle(), params.getDescription())).thenReturn(List.of(c1, c2));
        assertThat(userController.userUserIdCategoriesPost(userId, params)).isEqualTo(ResponseEntity.ok(List.of(c1, c2)));
    }

    @Test
    public void testUserIdCategoriesPost404() throws NotFoundException, NullException, InvalidDataException {
        UUID userId = UUID.randomUUID();
        UserUserIdCategoriesPostRequest params = new UserUserIdCategoriesPostRequest();
        params.setTitle("name");
        params.setDescription("desc");

        when(categoryService.createCategory(userId, params.getTitle(), params.getDescription())).thenThrow(new NotFoundException("Not found something"));
        assertThat(userController.userUserIdCategoriesPost(userId, params)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testUserIdCategoriesPost404Validation() throws NotFoundException, NullException, InvalidDataException {
        UUID userId = UUID.randomUUID();
        UserUserIdCategoriesPostRequest params = new UserUserIdCategoriesPostRequest();
        params.setTitle("name");
        params.setDescription("desc");

        when(categoryService.createCategory(userId, params.getTitle(), params.getDescription())).thenThrow(new ValidationException("User not found"));
        assertThat(userController.userUserIdCategoriesPost(userId, params)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testUserIdCategoriesPost500Validation() throws NotFoundException, NullException, InvalidDataException {
        UUID userId = UUID.randomUUID();
        UserUserIdCategoriesPostRequest params = new UserUserIdCategoriesPostRequest();
        params.setTitle("name");
        params.setDescription("desc");

        when(categoryService.createCategory(userId, params.getTitle(), params.getDescription())).thenThrow(new ValidationException("something"));
        assertThat(userController.userUserIdCategoriesPost(userId, params)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testUserIdCategoriesPost400UserNull() throws NotFoundException, NullException, InvalidDataException {
        UUID userId = UUID.randomUUID();
        UserUserIdCategoriesPostRequest params = new UserUserIdCategoriesPostRequest();
        params.setTitle("name");
        params.setDescription("desc");

        when(categoryService.createCategory(userId, params.getTitle(), params.getDescription())).thenThrow(new ValidationException("User id cannot be null"));
        assertThat(userController.userUserIdCategoriesPost(userId, params)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserIdCategoriesPost400CategoryNameNull() throws NotFoundException, NullException, InvalidDataException {
        UUID userId = UUID.randomUUID();
        UserUserIdCategoriesPostRequest params = new UserUserIdCategoriesPostRequest();
        params.setTitle("name");
        params.setDescription("desc");

        when(categoryService.createCategory(userId, params.getTitle(), params.getDescription())).thenThrow(new NullException("Null category name"));
        assertThat(userController.userUserIdCategoriesPost(userId, params)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserIdCategoriesPost400CategoryNameEmpty() throws NotFoundException, NullException, InvalidDataException {
        UUID userId = UUID.randomUUID();
        UserUserIdCategoriesPostRequest params = new UserUserIdCategoriesPostRequest();
        params.setTitle("name");
        params.setDescription("desc");

        when(categoryService.createCategory(userId, params.getTitle(), params.getDescription())).thenThrow(new InvalidDataException("Empty category name"));
        assertThat(userController.userUserIdCategoriesPost(userId, params)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserIdCategoriesPost500() throws NotFoundException, NullException, InvalidDataException {
        UUID userId = UUID.randomUUID();
        UserUserIdCategoriesPostRequest params = new UserUserIdCategoriesPostRequest();
        params.setTitle("name");
        params.setDescription("desc");

        when(categoryService.createCategory(userId, params.getTitle(), params.getDescription())).thenThrow(new RuntimeException("Some exception"));
        assertThat(userController.userUserIdCategoriesPost(userId, params)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testUserIdCategoriesDelete200() throws NotFoundException, NullException {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category c2 = new Category(UUID.randomUUID(), new User(userId), new ArrayList<>(), "name2", "desc2");

        when(categoryService.deleteCategory(userId, categoryId)).thenReturn(List.of(c2));
        assertThat(userController.userUserIdCategoriesDelete(userId, categoryId)).isEqualTo(ResponseEntity.ok(List.of(c2)));
    }

    @Test
    public void testUserIdCategoriesDelete404() throws NotFoundException, NullException {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.deleteCategory(userId, categoryId)).thenThrow(new NotFoundException("Not found something"));
        assertThat(userController.userUserIdCategoriesDelete(userId, categoryId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testUserIdCategoriesDelete404Validation() throws NotFoundException, NullException {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.deleteCategory(userId, categoryId)).thenThrow(new ValidationException("User not found"));
        assertThat(userController.userUserIdCategoriesDelete(userId, categoryId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testUserIdCategoriesDelete400Validation() throws NotFoundException, NullException {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.deleteCategory(userId, categoryId)).thenThrow(new ValidationException("User id cannot be null"));
        assertThat(userController.userUserIdCategoriesDelete(userId, categoryId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserIdCategoriesDelete400Unsupported() throws NotFoundException, NullException {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.deleteCategory(userId, categoryId)).thenThrow(new UnsupportedOperationException("Not found something"));
        assertThat(userController.userUserIdCategoriesDelete(userId, categoryId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserIdCategoriesDelete400Null() throws NotFoundException, NullException {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.deleteCategory(userId, categoryId)).thenThrow(new NullException("Not found something"));
        assertThat(userController.userUserIdCategoriesDelete(userId, categoryId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserIdCategoriesDelete500() throws NotFoundException, NullException {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.deleteCategory(userId, categoryId)).thenThrow(new RuntimeException("Some exception"));
        assertThat(userController.userUserIdCategoriesDelete(userId, categoryId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testUserIdCategoriesDelete500Validation() throws NotFoundException, NullException {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryService.deleteCategory(userId, categoryId)).thenThrow(new ValidationException("Some exception"));
        assertThat(userController.userUserIdCategoriesDelete(userId, categoryId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testUserBookshelvesCategoryGet200() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category c1 = new Category(categoryId, new User(userId), new ArrayList<>(), "name", "desc");

        when(categoryService.getCategoryForBookshelf(userId, bookshelfId)).thenReturn(c1);
        assertThat(userController.userUserIdBookshelvesBookshelfIdCategoryGet(userId, bookshelfId)).isEqualTo(ResponseEntity.ok(c1));
    }

    @Test
    public void testUserBookshelvesCategoryGet204() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.getCategoryForBookshelf(userId, bookshelfId)).thenReturn(null);
        assertThat(userController.userUserIdBookshelvesBookshelfIdCategoryGet(userId, bookshelfId)).isEqualTo(ResponseEntity.noContent().build());
    }

    @Test
    public void testUserBookshelvesCategoryGet404() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.getCategoryForBookshelf(userId, bookshelfId)).thenThrow(new NotFoundException("Not found something"));
        assertThat(userController.userUserIdBookshelvesBookshelfIdCategoryGet(userId, bookshelfId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testUserBookshelvesCategoryGet400UserNull() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.getCategoryForBookshelf(userId, bookshelfId)).thenThrow(new ValidationException("User id cannot be null"));
        assertThat(userController.userUserIdBookshelvesBookshelfIdCategoryGet(userId, bookshelfId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserBookshelvesCategoryGet400BookshelfNull() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.getCategoryForBookshelf(userId, bookshelfId)).thenThrow(new ValidationException("Bookshelf id cannot be null"));
        assertThat(userController.userUserIdBookshelvesBookshelfIdCategoryGet(userId, bookshelfId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserBookshelvesCategoryGet404UserNotFound() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.getCategoryForBookshelf(userId, bookshelfId)).thenThrow(new ValidationException("User not found"));
        assertThat(userController.userUserIdBookshelvesBookshelfIdCategoryGet(userId, bookshelfId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testUserBookshelvesCategoryGet404BookshelfNotFound() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.getCategoryForBookshelf(userId, bookshelfId)).thenThrow(new ValidationException("Bookshelf not found"));
        assertThat(userController.userUserIdBookshelvesBookshelfIdCategoryGet(userId, bookshelfId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testUserBookshelvesCategoryGet403() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.getCategoryForBookshelf(userId, bookshelfId)).thenThrow(new UnsupportedOperationException("User not own bookshelf or category"));
        assertThat(userController.userUserIdBookshelvesBookshelfIdCategoryGet(userId, bookshelfId)).isEqualTo(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @Test
    public void testUserBookshelvesCategoryGet500() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.getCategoryForBookshelf(userId, bookshelfId)).thenThrow(new RuntimeException("Some exception"));
        assertThat(userController.userUserIdBookshelvesBookshelfIdCategoryGet(userId, bookshelfId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testUserBookshelvesCategoryGet500Validation() throws NotFoundException {
        UUID bookshelfId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(categoryService.getCategoryForBookshelf(userId, bookshelfId)).thenThrow(new ValidationException("Some exception"));
        assertThat(userController.userUserIdBookshelvesBookshelfIdCategoryGet(userId, bookshelfId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /*
     * Test for the method userUserIdBooksBookIdPagesPUT - OK Response.
     */
    @Test
    public void userUserIdBooksBookIdPagesPutOk() throws Exception {
        // Arrange
        when(userService.updateCurrentPage(user.getUserId(), b.getBookId(), 100)).thenReturn(100);

        // Act
        ResponseEntity<Integer> actual = userController.userUserIdBooksBookIdPagesPut(user.getUserId(), b.getBookId(), 100);

        // Assert
        assertEquals(actual.getBody(), 100);
        assertEquals(actual.getStatusCode(), HttpStatus.OK);
    }

    /*
     * Test for the method userUserIdBooksBookIdPagesPUT - Bad Request Response.
     */
    @Test
    public void userUserIdBooksBookIdPagesPutBadRequest() throws Exception {
        // Arrange
        when(userService.updateCurrentPage(user.getUserId(), b.getBookId(), -1)).thenThrow(new InvalidDataException("Invalid number"));

        // Act
        ResponseEntity<Integer> actual = userController.userUserIdBooksBookIdPagesPut(user.getUserId(), b.getBookId(), -1);

        // Assert
        assertEquals(actual.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    /*
     * Test for the method userUserIdBooksBookIdPagesPUT - Not Found Response.
     */
    @Test
    public void userUserIdBooksBookIdPagesPutNotFound() throws Exception {
        // Arrange
        when(userService.updateCurrentPage(user.getUserId(), b.getBookId(), 100)).thenThrow(new NotFoundException("Book not found"));

        // Act
        ResponseEntity<Integer> actual = userController.userUserIdBooksBookIdPagesPut(user.getUserId(), b.getBookId(), 100);

        // Assert
        assertEquals(actual.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    /*
     * Test for the method userUserIdBooksBookIdPagesPUT - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void userUserIdBooksBookIdPagesPutInternalServerError() throws Exception {
        // Arrange
        when(userService.updateCurrentPage(user.getUserId(), b.getBookId(), 100)).thenThrow(new Exception());

        // Act
        ResponseEntity<Integer> actual = userController.userUserIdBooksBookIdPagesPut(user.getUserId(), b.getBookId(), 100);

        // Assert
        assertEquals(actual.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /*
     * Test for the method userUserIdBooksBookIdPagesGet - OK Response.
     */
    @Test
    public void userUserIdBooksBookIdPagesGetOk() throws Exception {
        // Arrange
        when(userService.getCurrentPage(user.getUserId(), b.getBookId())).thenReturn(100);

        // Act
        ResponseEntity<Integer> actual = userController.userUserIdBooksBookIdPagesGet(user.getUserId(), b.getBookId());

        // Assert
        assertEquals(actual.getBody(), 100);
        assertEquals(actual.getStatusCode(), HttpStatus.OK);
    }

    /*
     * Test for the method userUserIdBooksBookIdPagesGet - Bad Request Response.
     */
    @Test
    public void userUserIdBooksBookIdPagesGetBadRequest() throws Exception {
        // Arrange
        when(userService.getCurrentPage(user.getUserId(), b.getBookId())).thenThrow(new InvalidDataException("Invalid"));

        // Act
        ResponseEntity<Integer> actual = userController.userUserIdBooksBookIdPagesGet(user.getUserId(), b.getBookId());

        // Assert
        assertEquals(actual.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    /*
     * Test for the method userUserIdBooksBookIdPagesGet - Not Found Response.
     */
    @Test
    public void userUserIdBooksBookIdPagesGetNotFound() throws Exception {
        // Arrange
        when(userService.getCurrentPage(user.getUserId(), b.getBookId())).thenThrow(new NotFoundException("Book not found"));

        // Act
        ResponseEntity<Integer> actual = userController.userUserIdBooksBookIdPagesGet(user.getUserId(), b.getBookId());

        // Assert
        assertEquals(actual.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    /*
     * Test for the method userUserIdBooksBookIdPagesGet - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void userUserIdBooksBookIdPagesGetInternalServerError() throws Exception {
        // Arrange
        when(userService.getCurrentPage(user.getUserId(), b.getBookId())).thenThrow(new Exception());

        // Act
        ResponseEntity<Integer> actual = userController.userUserIdBooksBookIdPagesGet(user.getUserId(), b.getBookId());

        // Assert
        assertEquals(actual.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /*
     * Test for the method userUserIdBookshelvesBookshelfIdOrderGet for title - OK Response.
     */
    @Test
    public void userUserIdBookshelvesBookshelfIdOrderGetTitleOk() throws Exception {
        Book b1 = new Book().title("c");
        Book b2 = new Book().title("bbbbbb");
        Book b3 = new Book().title("aaaaa");

        User user = new User();
        user.setUserId(UUID.randomUUID());

        Bookshelf bookshelf = new Bookshelf().title("title").bookshelfId(UUID.randomUUID()).owner(user);

        BookWrapper bw1 = new BookWrapper();
        bw1.setBookId(b1.getBookId());
        bw1.setUserId(user.getUserId());
        bw1.setCurrentPage(0);

        BookWrapper bw2 = new BookWrapper();
        bw2.setBookId(b2.getBookId());
        bw2.setUserId(user.getUserId());
        bw2.setCurrentPage(0);

        BookWrapper bw3 = new BookWrapper();
        bw3.setBookId(b3.getBookId());
        bw3.setUserId(user.getUserId());
        bw3.setCurrentPage(0);

        List<BookWrapper> actualSortedList = List.of(bw3, bw2, bw1);

        when(userService.sortBooks(user.getUserId(), bookshelf.getBookshelfId(), "alphabetically on title")).thenReturn(actualSortedList);
        ResponseEntity<List<BookWrapper>> actual = userController.userUserIdBookshelvesBookshelfIdOrderGet(user.getUserId(), bookshelf.getBookshelfId(), "alphabetically on title");

        assertEquals(actual.getStatusCode(), HttpStatus.OK);
        assertEquals(actual.getBody(), actualSortedList);
    }

    /*
     * Test for the method userUserIdBookshelvesBookshelfIdOrderGet for author - OK Response.
     */
    @Test
    public void userUserIdBookshelvesBookshelfIdOrderGetAuthorOk() throws Exception {
        Book b1 = new Book().authors(List.of("bcd", "tag"));
        Book b2 = new Book().authors(List.of("wsd", "abc"));
        Book b3 = new Book().authors(List.of("fgh", "bad"));

        User user = new User();
        user.setUserId(UUID.randomUUID());

        Bookshelf bookshelf = new Bookshelf().title("title").bookshelfId(UUID.randomUUID()).owner(user);

        BookWrapper bw1 = new BookWrapper();
        bw1.setBookId(b1.getBookId());
        bw1.setUserId(user.getUserId());
        bw1.setCurrentPage(0);

        BookWrapper bw2 = new BookWrapper();
        bw2.setBookId(b2.getBookId());
        bw2.setUserId(user.getUserId());
        bw2.setCurrentPage(0);

        BookWrapper bw3 = new BookWrapper();
        bw3.setBookId(b3.getBookId());
        bw3.setUserId(user.getUserId());
        bw3.setCurrentPage(0);

        List<BookWrapper> actualSortedList = List.of(bw2, bw3, bw1);

        when(userService.sortBooks(user.getUserId(), bookshelf.getBookshelfId(), "alphabetically on author")).thenReturn(actualSortedList);
        ResponseEntity<List<BookWrapper>> actual = userController.userUserIdBookshelvesBookshelfIdOrderGet(user.getUserId(), bookshelf.getBookshelfId(), "alphabetically on author");

        assertEquals(actual.getStatusCode(), HttpStatus.OK);
        assertEquals(actual.getBody(), actualSortedList);
    }

    /*
     * Test for the method userUserIdBookshelvesBookshelfIdOrderGet for % of pages read - OK Response.
     */
    @Test
    public void userUserIdBookshelvesBookshelfIdOrderGetPagesOk() throws Exception {
        Book b1 = new Book().numPages(150);
        Book b2 = new Book().numPages(200);
        Book b3 = new Book().numPages(100);

        User user = new User();
        user.setUserId(UUID.randomUUID());

        Bookshelf bookshelf = new Bookshelf().title("title").bookshelfId(UUID.randomUUID()).owner(user);

        BookWrapper bw1 = new BookWrapper();
        bw1.setBookId(b1.getBookId());
        bw1.setUserId(user.getUserId());
        bw1.setCurrentPage(100);

        BookWrapper bw2 = new BookWrapper();
        bw2.setBookId(b2.getBookId());
        bw2.setUserId(user.getUserId());
        bw2.setCurrentPage(50);

        BookWrapper bw3 = new BookWrapper();
        bw3.setBookId(b3.getBookId());
        bw3.setUserId(user.getUserId());
        bw3.setCurrentPage(80);

        List<BookWrapper> actualSortedList = List.of(bw3, bw1, bw2);

        when(userService.sortBooks(user.getUserId(), bookshelf.getBookshelfId(), "percentage of pages read")).thenReturn(actualSortedList);
        ResponseEntity<List<BookWrapper>> actual = userController.userUserIdBookshelvesBookshelfIdOrderGet(user.getUserId(), bookshelf.getBookshelfId(), "percentage of pages read");

        assertEquals(actual.getStatusCode(), HttpStatus.OK);
        assertEquals(actual.getBody(), actualSortedList);
    }

    /*
     * Test for the method userUserIdBookshelvesBookshelfIdOrderGet - Bad Request Response.
     */
    @Test
    public void userUserIdBookshelvesBookshelfIdOrderGetBadRequest() throws Exception {
        // Arrange
        when(userService.sortBooks(user.getUserId(), bs.getBookshelfId(), "alphabetically on title")).thenThrow(new IllegalArgumentException());

        // Act
        ResponseEntity<List<BookWrapper>> actual = userController.userUserIdBookshelvesBookshelfIdOrderGet(user.getUserId(), bs.getBookshelfId(), "alphabetically on title");

        // Assert
        assertEquals(actual.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    /*
     * Test for the method userUserIdBookshelvesBookshelfIdOrderGet - Not Found Response.
     */
    @Test
    public void userUserIdBookshelvesBookshelfIdOrderGetNotFound() throws Exception {
        // Arrange
        when(userService.sortBooks(user.getUserId(), bs.getBookshelfId(), "alphabetically on title")).thenThrow(new NotFoundException("Bookshelf not found"));

        // Act
        ResponseEntity<List<BookWrapper>> actual = userController.userUserIdBookshelvesBookshelfIdOrderGet(user.getUserId(), bs.getBookshelfId(), "alphabetically on title");

        // Assert
        assertEquals(actual.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    /*
     * Test for the method userUserIdBookshelvesBookshelfIdOrderGet - INTERNAL_SERVER_ERROR Response.
     */
    @Test
    public void userUserIdBookshelvesBookshelfIdOrderGetInternalServerError() throws Exception {
        // Arrange
        when(userService.sortBooks(user.getUserId(), bs.getBookshelfId(), "alphabetically on title")).thenThrow(new Exception());

        // Act
        ResponseEntity<List<BookWrapper>> actual = userController.userUserIdBookshelvesBookshelfIdOrderGet(user.getUserId(), bs.getBookshelfId(), "alphabetically on title");

        // Assert
        assertEquals(actual.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testGetStatusOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        String status = "STATUS";
        when(userService.getReadingStatus(userId, bookId)).thenReturn(status);
        assertThat(userController.userUserIdBooksBookIdStatusGet(userId, bookId))
                .isEqualTo(ResponseEntity.ok(status));
    }

    @Test
    public void testGetStatusBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(userService.getReadingStatus(userId, bookId)).thenThrow(new InvalidDataException("invalid"));
        assertThat(userController.userUserIdBooksBookIdStatusGet(userId, bookId))
                .isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testGetStatusNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(userService.getReadingStatus(userId, bookId)).thenThrow(new NotFoundException("invalid"));
        assertThat(userController.userUserIdBooksBookIdStatusGet(userId, bookId))
                .isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testGetStatusError() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(userService.getReadingStatus(userId, bookId)).thenThrow(new RuntimeException("invalid"));
        assertThat(userController.userUserIdBooksBookIdStatusGet(userId, bookId))
                .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testPutStatusOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        String status = "STATUS";
        when(userService.setReadingStatus(userId, bookId, status)).thenReturn(status);
        assertThat(userController.userUserIdBooksBookIdStatusPut(userId, bookId, status))
                .isEqualTo(ResponseEntity.ok(status));
    }

    @Test
    public void testPutStatusBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        String status = "STATUS";

        when(userService.setReadingStatus(userId, bookId, status)).thenThrow(new InvalidDataException("invalid"));
        assertThat(userController.userUserIdBooksBookIdStatusPut(userId, bookId, status))
                .isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testPutStatusNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(userService.setReadingStatus(userId, bookId, null)).thenThrow(new NotFoundException("invalid"));
        assertThat(userController.userUserIdBooksBookIdStatusPut(userId, bookId, null))
                .isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testPutStatusError() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        String status = "STATUS";

        when(userService.setReadingStatus(userId, bookId, status)).thenThrow(new RuntimeException("invalid"));
        assertThat(userController.userUserIdBooksBookIdStatusPut(userId, bookId, status))
                .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testUserPost200() throws Exception {
        UUID userId = UUID.randomUUID();
        assertThat(userController.userPost(userId)).isEqualTo(ResponseEntity.ok().build());
        verify(userService).addUser(userId);
    }

    @Test
    public void testUserPost400Illegal() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new IllegalArgumentException()).when(userService).addUser(userId);
        assertThat(userController.userPost(userId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserPost400Invalid() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new InvalidDataException("invalid")).when(userService).addUser(userId);
        assertThat(userController.userPost(userId)).isEqualTo(ResponseEntity.badRequest().build());

    }

    @Test
    public void testUserPost500() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new Exception()).when(userService).addUser(userId);
        assertThat(userController.userPost(userId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testUserDelete200() throws Exception {
        UUID userId = UUID.randomUUID();
        assertThat(userController.userDelete(userId)).isEqualTo(ResponseEntity.ok().build());
        verify(userService).deleteUser(userId);
    }

    @Test
    public void testUserDelete400Illegal() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new IllegalArgumentException()).when(userService).deleteUser(userId);
        assertThat(userController.userDelete(userId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserDelete400Null() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new NullException("invalid")).when(userService).deleteUser(userId);
        assertThat(userController.userDelete(userId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserDelete400Validation() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new ValidationException("User id cannot be null")).when(userService).deleteUser(userId);
        assertThat(userController.userDelete(userId)).isEqualTo(ResponseEntity.badRequest().build());
    }

    @Test
    public void testUserDelete404NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new NotFoundException("invalid")).when(userService).deleteUser(userId);
        assertThat(userController.userDelete(userId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void testUserDelete404Validation() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new ValidationException("User not found")).when(userService).deleteUser(userId);
        assertThat(userController.userDelete(userId)).isEqualTo(ResponseEntity.notFound().build());
    }


    @Test
    public void testUserDelete500() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new RuntimeException("e")).when(userService).deleteUser(userId);
        assertThat(userController.userDelete(userId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Test
    public void testUserDelete500Validation() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new ValidationException("e")).when(userService).deleteUser(userId);
        assertThat(userController.userDelete(userId)).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}