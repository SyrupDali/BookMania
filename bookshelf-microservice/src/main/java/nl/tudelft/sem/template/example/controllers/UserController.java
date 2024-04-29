package nl.tudelft.sem.template.example.controllers;

import javassist.NotFoundException;
import nl.tudelft.sem.template.api.UserApi;
import nl.tudelft.sem.template.example.exceptions.InvalidDataException;
import nl.tudelft.sem.template.example.exceptions.NullException;
import nl.tudelft.sem.template.example.services.UserService;
import nl.tudelft.sem.template.model.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.example.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookshelf_service")
public class UserController implements UserApi {

    private final CategoryService categoryService;
    private final UserService userService;

    @Autowired
    public UserController(UserService userService, CategoryService categoryService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    /**
     * Method that gets all the categories created by a user and returns as a http request body
     *
     * @param userId The ID of the user whose categories we are looking for. (required)
     * @return and http request response indicating if the operation was successful and the required content
     */
    @Override
    public ResponseEntity<List<Category>> userUserIdCategoriesGet(UUID userId) {
        try {
            List<Category> categories = categoryService.getAllCategoriesForUser(userId);
            if (categories.isEmpty()) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.ok(categories);
            }
        } catch (ValidationException e) {
            if (e.getMessage().equals("User id cannot be null"))
                return ResponseEntity.badRequest().build();
            else if (e.getMessage().equals("User not found"))
                return ResponseEntity.notFound().build();
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Method that adds a new category to the database based off the information provided in the http request
     *
     * @param userId The ID of the user who is creating a category. (required)
     * @param params The new information required for a category. (required)
     * @return (a list containing) the new category object
     */
    @Override
    public ResponseEntity<List<Category>> userUserIdCategoriesPost(UUID userId, UserUserIdCategoriesPostRequest params) {
        try {
            List<Category> categories = categoryService.createCategory(userId, params.getTitle(), params.getDescription());
            return ResponseEntity.ok(categories);
        } catch (NullException | InvalidDataException e) { // some parameter that shouldn't be null was null
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ValidationException e) {
            if (e.getMessage().equals("User id cannot be null"))
                return ResponseEntity.badRequest().build();
            else if (e.getMessage().equals("User not found"))
                return ResponseEntity.notFound().build();
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Method that deletes a category from the database, this can only be done if the user owns the category
     *
     * @param userId     The ID of the user whose categories we are looking for. (required)
     * @param categoryId The ID of the category we are trying to delete. (required)
     * @return (a list containing) the category that was just deleted
     */
    @Override
    public ResponseEntity<List<Category>> userUserIdCategoriesDelete(UUID userId, UUID categoryId) {
        try {
            List<Category> categories = categoryService.deleteCategory(userId, categoryId);
            return ResponseEntity.ok(categories);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (NullException | UnsupportedOperationException e) {
            return ResponseEntity.badRequest().build();
        } catch (ValidationException e) {
            if (e.getMessage().equals("User id cannot be null"))
                return ResponseEntity.badRequest().build();
            else if (e.getMessage().equals("User not found"))
                return ResponseEntity.notFound().build();
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Category> userUserIdBookshelvesBookshelfIdCategoryGet(UUID userId, UUID bookshelfId) {
        try {
            Category category = categoryService.getCategoryForBookshelf(userId, bookshelfId);
            if (category != null)
                return ResponseEntity.ok(category);

            return ResponseEntity.noContent().build(); // the bookshelf does not have any category
        } catch (NotFoundException e) { // user or bookshelf not found
            return ResponseEntity.notFound().build();
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); //user does not own the bookshelf
        } catch (ValidationException e) {
            if (e.getMessage().equals("User id cannot be null")
                    || e.getMessage().equals("Bookshelf id cannot be null"))
                return ResponseEntity.badRequest().build();
            else if (e.getMessage().equals("User not found")
                    || e.getMessage().equals("Bookshelf not found"))
                return ResponseEntity.notFound().build();
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Method that adds a new user to the database. This method also handles creating the default categories.
     *
     * @param userId the id of the user (who has been created by the user service)
     * @return OK if the user has been added successfully and the categories have been created
     */
    @Override
    public ResponseEntity<Void> userPost(UUID userId) {
        try {
            userService.addUser(userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | InvalidDataException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a user from the database and all the associated entities
     *
     * @param userId ID of the user that needs to be deleted. (required)
     * @return OK if the operation is successful
     */
    @Override
    public ResponseEntity<Void> userDelete(UUID userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | NullException e) {
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ValidationException e) {
            if (e.getMessage().equals("User id cannot be null"))
                return ResponseEntity.badRequest().build();
            else if (e.getMessage().equals("User not found"))
                return ResponseEntity.notFound().build();
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    //TODO THIS METHOD IS NOT IN THE YAML BUT I NEED IT FOR TESTING
    /**
     * Method that gets all the users from the database
     *
     * @return a list of all the users
     */
    @Operation(
            operationId = "userGet",
            summary = "Get all the users in the database",
            tags = {"user"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK.", content = {
                            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = User.class)))
                    }),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/user",
            produces = {"application/json"}
    )
    public ResponseEntity<List<User>> userGet() {
        try {
            return ResponseEntity.ok(userService.getAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
     * Get the currently reading page of a book for a user.
     * @param userId ID of the user (required)
     * @param bookId ID of the book (required)
     * @param pages current page of book (required)
     * @return the page number
     */
    @Override
    public ResponseEntity<Integer> userUserIdBooksBookIdPagesPut(UUID userId, UUID bookId, Integer pages) {
        try {
            int updatedPage = userService.updateCurrentPage(userId, bookId, pages);

            // 200: OK.
            return ResponseEntity.ok(updatedPage);
        } catch (NotFoundException e) {
            // 404: Not Found.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidDataException e) {
            // 400: Bad Request.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the currently reading page of a book for a user.
     *
     * @param userId ID of the user (required)
     * @param bookId ID of the book (required)
     * @return the page number
     */
    @Override
    public ResponseEntity<Integer> userUserIdBooksBookIdPagesGet(UUID userId, UUID bookId) {
        try {
            Integer pages = userService.getCurrentPage(userId, bookId);

            // 200: OK.
            return ResponseEntity.ok(pages);
        } catch (NotFoundException e) {
            // 404: Not Found.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidDataException e) {
            // 400: Bad Request.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Sort a user's books inside a bookshelf in a certain order.
     *
     * @param userId      ID of the user (required)
     * @param bookshelfId ID of the bookshelf (required)
     * @param order       order of the books (required)
     * @return the sorted list of books
     */
    @Override
    public ResponseEntity<List<BookWrapper>> userUserIdBookshelvesBookshelfIdOrderGet(UUID userId, UUID bookshelfId, String order) {
        try {
            List<BookWrapper> books = userService.sortBooks(userId, bookshelfId, order);

            // 200: OK.
            return ResponseEntity.ok(books);
        } catch (IllegalArgumentException e) {
            // 400: Bad Request.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NotFoundException e) {
            // 404: Not Found.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<String>> userUserIdInsightsPreferredGenresGet(UUID userId) {
        try {
            List<String> list = userService.getPreferredGenres(userId);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Integer> userUserIdInsightsBooksReadGet(UUID userId) {
        try {
            int count = userService.getNumberOfBooksRead(userId);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Set the reading status of a book for a specific user
     * @param userId ID of the user (required)
     * @param bookId ID of the book (required)
     * @return the reading status that was found (successfully)
     */
    @Override
    public ResponseEntity<String> userUserIdBooksBookIdStatusGet(UUID userId, UUID bookId) {
        try {
            String status = userService.getReadingStatus(userId, bookId);
            return ResponseEntity.ok(status);
        } catch (InvalidDataException e) {
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Set the reading status of a book for a specific user
     * @param userId ID of the user (required)
     * @param bookId ID of the book (required)
     * @return the reading status that was found (successfully)
     */
    @Override
    public ResponseEntity<String> userUserIdBooksBookIdStatusPut(UUID userId, UUID bookId, String body) {
        try {
            String status = userService.setReadingStatus(userId, bookId, body);
            return ResponseEntity.ok(status);
        } catch (InvalidDataException e) {
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the bookshelves owned by a specific user
     * @param userId The ID of the user whose bookshelves we are looking for. (required)
     * @return The bookshelves if any were found
     */
    @Override
    public ResponseEntity<List<Bookshelf>> userUserIdBookshelvesGet(UUID userId) {
        try {
            List<Bookshelf> books = userService.getByOwner(userId);

            // 200: OK. Books returned.
            return ResponseEntity.ok(books);
        } catch (EmptyResultDataAccessException e) {
            // 204: No books in the catalog.
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // 500: Internal Server Error.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //TODO THIS METHOD IS NOT IN THE YAML BUT I NEED IT FOR TESTING, also idk in which controller to put it cause we don't have a category controller
    /**
     * Method that gets all the categories in the database
     *
     * @return a list of all the categories in the database
     */
    @Operation(
            operationId = "categoryGet",
            summary = "Get all the categories in the database",
            tags = {"category"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK.", content = {
                            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = User.class)))
                    }),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/category",
            produces = {"application/json"}
    )
    public ResponseEntity<List<Category>> categoryCategories() {
        try {
            return ResponseEntity.ok(categoryService.getAllCategories());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
