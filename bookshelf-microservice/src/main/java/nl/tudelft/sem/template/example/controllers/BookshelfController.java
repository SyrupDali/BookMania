package nl.tudelft.sem.template.example.controllers;

import javassist.NotFoundException;
import nl.tudelft.sem.template.api.BookshelfApi;
import nl.tudelft.sem.template.example.exceptions.NullException;
import nl.tudelft.sem.template.example.services.CategoryService;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.BookshelfBookshelfIdPutRequest;
import nl.tudelft.sem.template.model.BookshelfPostRequest;
import nl.tudelft.sem.template.example.services.BookshelfService;
import nl.tudelft.sem.template.example.services.CircleService;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/bookshelf_service")
public class BookshelfController implements BookshelfApi {
    private final CircleService circleService;
    private final BookshelfService bookshelfService;
    private final CategoryService categoryService;

    /**
     * Constuctor for the class
     * @param circleService service for handling circle things
     * @param bookshelfService service for handling bookshelf things
     * @param categoryService service for handling category things
     */
    @Autowired
    public BookshelfController(CircleService circleService, BookshelfService bookshelfService,
                               CategoryService categoryService) {
        this.circleService = circleService;
        this.bookshelfService = bookshelfService;
        this.categoryService = categoryService;
    }

    /**
     * Returns all the bookshelves that exist in the database.
     *
     * @return a list of bookshelves present in the database
     */
    @Override
    public ResponseEntity<List<Bookshelf>> bookshelfGet() {
        try {
            List<Bookshelf> bookshelves = bookshelfService.getAllBookshelves();
            if (bookshelves.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(bookshelves);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns the bookshelf with the specified ID.
     *
     * @return the bookshelf with its details
     */
    @Override
    public ResponseEntity<Bookshelf> bookshelfBookshelfIdGet(UUID bookshelfId) {
        try {
            Bookshelf bookshelf = bookshelfService.getBookshelfById(bookshelfId);
            return ResponseEntity.ok(bookshelf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Bookshelf>> bookshelfGetPublicGet() {
        try {
            List<Bookshelf> bookshelves = bookshelfService.getAllPublicBookshelves();
            if (bookshelves.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(bookshelves);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    /**
     * Creates a new bookshelf
     *
     * @param userId               The ID of the user who is trying to create a bookshelf. (required)
     * @param bookshelfPostRequest The new information required for a bookshelf. (required)
     * @return the new bookshelf instance created
     */
    @Override
    public ResponseEntity<Bookshelf> bookshelfPost(UUID userId, BookshelfPostRequest bookshelfPostRequest) {
        try {
            Bookshelf b = bookshelfService.addBookshelf(bookshelfPostRequest, userId);
            return ResponseEntity.ok(b);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a specified bookshelf from the database.
     *
     * @param bookshelfId The ID of the bookshelf that is being deleted. (required)
     * @param userId      The ID of the user who is trying to delete this bookshelf. (required)
     * @return The bookshelf that is being deleted.
     */

    @Override
    public ResponseEntity<Bookshelf> bookshelfBookshelfIdDelete(UUID bookshelfId, UUID userId) {
        try {
            Bookshelf b = bookshelfService.deleteBookshelf(bookshelfId, userId);
            return ResponseEntity.ok(b);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Edits a specific bookshelf
     *
     * @param bookshelfId                    The ID of the bookshelf that is being edited. (required)
     * @param userId                         The ID of the user who is trying to edit this bookshelf. (required)
     * @param bookshelfBookshelfIdPutRequest (required)
     * @return The updated bookshelf
     */
    @Override
    public ResponseEntity<Bookshelf> bookshelfBookshelfIdPut(UUID bookshelfId, UUID userId, BookshelfBookshelfIdPutRequest bookshelfBookshelfIdPutRequest) {
        try {
            Bookshelf updatedBookshelf = bookshelfService.editBookshelf(bookshelfId, userId, bookshelfBookshelfIdPutRequest);
            return ResponseEntity.ok(updatedBookshelf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            String message = e.getMessage();
            if (message.equals("Bookshelf id cannot be null") || message.equals("Bookshelf not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (message.equals("User does not have permission to modify the bookshelf")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Edits only the description of the bookshelf
     *
     * @param bookshelfId The ID of the bookshelf that is being edited. (required)
     * @param userId      The ID of the user who is trying to edit the description of this bookshelf. (required)
     * @param body        The new description for the bookshelf. (required)
     * @return A bookshelf object with the updated description
     */
    @Override
    public ResponseEntity<Bookshelf> bookshelfBookshelfIdEditDescriptionPut(UUID bookshelfId, UUID userId, String body) {
        try {
            Bookshelf bookshelf = bookshelfService.editDescriptionBookshelf(bookshelfId, userId, body);
            return ResponseEntity.ok(bookshelf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            String message = e.getMessage();
            if (message.equals("Bookshelf id cannot be null") || message.equals("Bookshelf not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (message.equals("User does not have permission to modify the bookshelf")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Edits only the privacy of the bookshelf
     *
     * @param bookshelfId The ID of the bookshelf that is being edited. (required)
     * @param userId      The ID of the user who is trying to edit the visibility setting of this bookshelf. (required)
     * @param body        One value between PUBLIC or PRIVATE (required)
     * @return A bookshelf object with the updated privacy
     */
    @Override
    public ResponseEntity<Bookshelf> bookshelfBookshelfIdEditPrivacyPut(UUID bookshelfId, UUID userId, String body) {
        try {
            Bookshelf bookshelf = bookshelfService.editPrivacyBookshelf(bookshelfId, userId, body);
            return ResponseEntity.ok(bookshelf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            String message = e.getMessage();
            if (message.equals("Bookshelf id cannot be null") || message.equals("Bookshelf not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (message.equals("User does not have permission to modify the bookshelf")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Edits only the title of the bookshelf
     *
     * @param bookshelfId The ID of the bookshelf that is being edited. (required)
     * @param userId      The ID of the user who is trying to edit the title of this bookshelf. (required)
     * @param body        The new title for the bookshelf. (required)
     * @return A bookshelf object with the updated title
     */
    @Override
    public ResponseEntity<Bookshelf> bookshelfBookshelfIdEditTitlePut(UUID bookshelfId, UUID userId, String body) {
        try {
            Bookshelf bookshelf = bookshelfService.editTitleBookshelf(bookshelfId, userId, body);
            return ResponseEntity.ok(bookshelf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            String message = e.getMessage();
            if (message.equals("Bookshelf id cannot be null") || message.equals("Bookshelf not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (message.equals("User does not have permission to modify the bookshelf")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Adds a book to the specified bookshelf.
     *
     * @param bookshelfId The ID of the bookshelf that is being added.
     * @param userId      The ID of the user who is trying to add a book to this bookshelf.
     * @param bookId      The ID of the book that is being added to the bookshelf.
     * @return The updated bookshelf.
     */
    @Override
    public ResponseEntity<Bookshelf> bookshelfBookshelfIdBookPut(UUID bookshelfId, UUID userId, UUID bookId) {
        try {
            Bookshelf updatedBookshelf = bookshelfService.addBookToBookshelf(bookshelfId, userId, bookId);
            // Get the updated bookshelf from the service

            return new ResponseEntity<>(updatedBookshelf, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (ValidationException e) {
            String message = e.getMessage();
            return switch (message) {
                case "Bookshelf id cannot be null", "User id cannot be null" ->
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                case "Bookshelf not found", "User not found" -> ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                case "Book already exists in the bookshelf" -> ResponseEntity.status(HttpStatus.CONFLICT).build();
                case "User does not have permission to modify the bookshelf" ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            };
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<Book>> bookshelfBookshelfIdUserIdBookAddMultiplePut(UUID bookshelfId, UUID userId, List<UUID> UUID) {
        try {
            List<Book> books = bookshelfService.addMultipleBooksToBookshelf(bookshelfId, userId, UUID);
            return ResponseEntity.ok(books);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            String message = e.getMessage();
            return switch (message) {
                case "Bookshelf id cannot be null", "User id cannot be null", "Owner id cannot be null",
                        "Book id cannot be null" -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                case "Bookshelf not found", "Book not found", "User not found" ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                case "Book already exists in the bookshelf" -> ResponseEntity.status(HttpStatus.CONFLICT).build();
                case "User does not have permission to modify the bookshelf" ->
                        ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            };
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a book from the specified bookshelf.
     *
     * @param bookshelfId The ID of the bookshelf that is being deleted.
     * @param userId      The ID of the user who is trying to delete a book from this bookshelf.
     * @param bookId      The ID of the book that is being deleted from the bookshelf.
     * @return The updated bookshelf.
     */
    @Override
    public ResponseEntity<Bookshelf> bookshelfBookshelfIdBookDelete(
            UUID bookshelfId, UUID userId, UUID bookId) {
        try {
            // Get the updated bookshelf from the service
            Bookshelf updatedBookshelf = bookshelfService.removeBookFromBookshelf(bookshelfId, userId, bookId);

            return ResponseEntity.ok(updatedBookshelf);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ValidationException e) {
            String message = e.getMessage();
            return switch (message) {
                case "Bookshelf id cannot be null", "User id cannot be null" ->
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                case "Bookshelf not found", "User not found" -> ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                case "User does not have permission to modify the bookshelf" ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            };
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Void> bookshelfBookshelfIdUserIdBookDeleteMultiplePut(UUID bookshelfId, UUID userId, List<UUID> UUID) {
        try {
            bookshelfService.removeMultipleBooksFromBookshelf(bookshelfId, userId, UUID);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch(ValidationException e) {
            String errorMessage = e.getMessage();
            if(errorMessage.equals("Bookshelf id cannot be null")
                    || errorMessage.equals("User id cannot be null")
                    || errorMessage.equals("Book id cannot be null")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            } else if(errorMessage.equals("Bookshelf not found")
                    || errorMessage.equals("User not found")
                    || errorMessage.equals("Book not found")
                    || errorMessage.equals("Book wrapper not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if(errorMessage.equals("Book is not found in specified bookshelf")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else if (errorMessage.equals("User does not have permission to modify the bookshelf")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a member from the specified bookshelf.
     *
     * @param bookshelfId The ID of the bookshelf that is being retrieved.
     * @param userId      The ID of the user who is trying to retrieve this bookshelf.
     * @param memberId    The ID of the user we want to remove from circle.
     * @return The bookshelf with the specified ID and the user IDs in the circle.
     */
    @Override
    public ResponseEntity<BookshelfBookshelfIdCircleDelete200Response> bookshelfBookshelfIdCircleDelete(UUID bookshelfId, UUID userId, UUID memberId) {
        try {
            BookshelfBookshelfIdCircleDelete200Response response = circleService.removeMemberFromCircle(bookshelfId, userId, memberId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            if (e.getMessage().equals("User does not match the bookshelf's owner")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();//401 here
            } else if (e.getMessage().equals("User not in circle")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else if (e.getMessage().equals("Bookshelf not found") || e.getMessage().equals("Owner not found")
                    || e.getMessage().equals("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().equals("Bookshelf id cannot be null")
                    || e.getMessage().equals("Owner id cannot be null")
                    || e.getMessage().equals("User id cannot be null")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            // Handle generic exceptions if needed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Gets the members in the circle.
     *
     * @param bookshelfId The ID of the bookshelf that is being retrieved.
     * @return The bookshelf circle member IDs.
     */
    @Override
    public ResponseEntity<List<UUID>> bookshelfBookshelfIdCircleGet(UUID bookshelfId) {
        try {
            List<UUID> userIds = circleService.getMembers(bookshelfId);
            return ResponseEntity.ok(userIds);
        } catch (IllegalArgumentException | NullPointerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // Handle generic exceptions if needed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Accepts the pending request of a user.
     *
     * @param bookshelfId The ID of the bookshelf that is being retrieved.
     * @param ownerId     The ID of the user who is trying to retrieve this bookshelf.
     * @param body        The ID of the user we want to accept to circle.
     * @return The IDs of the users in the circle.
     */
    @Override
    public ResponseEntity<List<UUID>> bookshelfBookshelfIdCirclePendingAcceptPost(UUID bookshelfId,
                                                                                  UUID ownerId, UUID body) {
        try {
            List<UUID> userIds = circleService.acceptPendingMember(bookshelfId, ownerId, body);
            return ResponseEntity.ok(userIds);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            if (e.getMessage().equals("User does not match the bookshelf's owner")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().equals("Bookshelf not found") || e.getMessage().equals("Owner not found")
                    || e.getMessage().equals("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().equals("User is not a pending member")
                    || e.getMessage().equals("User already in circle")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else if (e.getMessage().equals("Bookshelf id cannot be null")
                    || e.getMessage().equals("Owner id cannot be null")
                    || e.getMessage().equals("User id cannot be null")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            // Handle generic exceptions if needed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Gets the pending members IDs.
     *
     * @param bookshelfId The ID of the bookshelf that is being retrieved.
     * @param userId      The ID of the user who is trying to retrieve this bookshelf.
     * @return The IDs of the users in the pending list.
     */
    @Override
    public ResponseEntity<List<UUID>> bookshelfBookshelfIdCirclePendingGet(UUID bookshelfId, UUID userId) {
        try {
            List<UUID> userIds = circleService.getPendingMembers(bookshelfId, userId);
            return ResponseEntity.ok(userIds);
        } catch (IllegalArgumentException | NullPointerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            if (e.getMessage().equals("User does not match the bookshelf's owner")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().equals("Bookshelf not found") || e.getMessage().equals("Owner not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().equals("Bookshelf id cannot be null")
                    || e.getMessage().equals("Owner id cannot be null")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Rejects the pending request of a user.
     *
     * @param bookshelfId The ID of the bookshelf that is being retrieved.
     * @param ownerId     The ID of the user who is trying to retrieve this bookshelf.
     * @param body        The ID of the user we want to reject from pending list.
     */
    @Override
    public ResponseEntity<Void> bookshelfBookshelfIdCirclePendingRejectPut(UUID bookshelfId, UUID ownerId, UUID body) {
        try {
            circleService.rejectPendingMember(bookshelfId, ownerId, body);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            if (e.getMessage().equals("User does not match the bookshelf's owner")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().equals("Bookshelf not found") || e.getMessage().equals("Owner not found")
                    || e.getMessage().equals("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().equals("User is not a pending member")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else if (e.getMessage().equals("Bookshelf id cannot be null")
                    || e.getMessage().equals("Owner id cannot be null")
                    || e.getMessage().equals("User id cannot be null")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            // Handle generic exceptions if needed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Adds a member to the circle.
     *
     * @param bookshelfId The ID of the bookshelf that is being retrieved.
     * @param userId      The ID of the user who is trying to retrieve this bookshelf.
     * @param memberId    The ID of the user we want to add to circle.
     * @return Object with the ID of the bookshelf and the IDs of the users in the circle.
     */
    @Override
    public ResponseEntity<BookshelfBookshelfIdCirclePut200Response> bookshelfBookshelfIdCirclePut(UUID bookshelfId, UUID userId, UUID memberId) {
        try {
            BookshelfBookshelfIdCirclePut200Response response = circleService.addMemberToCircle(bookshelfId, userId, memberId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            if (e.getMessage().equals("User does not match the bookshelf's owner")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();//401 here
            } else if (e.getMessage().equals("User not found") || e.getMessage().equals("Bookshelf not found")
                    || e.getMessage().equals("Owner not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().equals("User already in circle")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else if (e.getMessage().equals("Bookshelf id cannot be null")
                    || e.getMessage().equals("Owner id cannot be null")
                    || e.getMessage().equals("User id cannot be null")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            // Handle generic exceptions if needed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Requests to join the circle.
     *
     * @param bookshelfId The ID of the bookshelf that is being retrieved.
     * @param userId      The ID of the user who is trying to join.
     */
    @Override
    public ResponseEntity<Void> bookshelfBookshelfIdCircleRequestPost(UUID bookshelfId, UUID userId) {
        try {
            circleService.requestToJoinCircle(bookshelfId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | NullPointerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ValidationException e) {
            if (e.getMessage().equals("User already requested to join circle")
                    || e.getMessage().equals("User already in circle")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else if (e.getMessage().equals("Bookshelf not found") || e.getMessage().equals("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().equals("Bookshelf id cannot be null")
                    || e.getMessage().equals("User id cannot be null")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            // Handle generic exceptions if needed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Method for editing or adding a category to a bookshelf
     *
     * @param bookshelfId The ID of the bookshelf that is being edited. (required)
     * @param userId      The ID of the user who is trying to edit the category of this bookshelf. (required)
     * @param body        The ID of the category to be added
     * @return a response entity containing the information about the bookshelf whose category was updated
     */
    @Override
    public ResponseEntity<Bookshelf> bookshelfBookshelfIdEditCategoryPut(UUID bookshelfId, UUID userId, UUID body) {
        try {
            Bookshelf bookshelf = categoryService.setCategoryAuthenticated(userId, bookshelfId, body);
            return ResponseEntity.ok(bookshelf);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (NullException e) {
            return ResponseEntity.badRequest().build();
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ValidationException e) {
            if (e.getMessage().equals("User id cannot be null"))
                return ResponseEntity.badRequest().build();
            else if (e.getMessage().equals("User not found"))
                return ResponseEntity.notFound().build();
            else if (e.getMessage().equals("User does not have permission to modify the bookshelf"))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Method for editing or adding a category to a bookshelf
     *
     * @param bookshelfId The ID of the bookshelf that is being edited. (required)
     * @param userId      The ID of the user who is trying to remove the category of this bookshelf. (required)
     * @return a response entity containing the information about the bookshelf whose category was updated
     */
    @Override
    public ResponseEntity<Bookshelf> bookshelfBookshelfIdEditCategoryDelete(UUID bookshelfId, UUID userId) {
        try {
            Bookshelf bookshelf = categoryService.removeCategoryAuthenticated(userId, bookshelfId);
            return ResponseEntity.ok(bookshelf);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (NullException e) {
            return ResponseEntity.badRequest().build();
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ValidationException e) {
            if (e.getMessage().equals("User id cannot be null"))
                return ResponseEntity.badRequest().build();
            else if (e.getMessage().equals("User not found"))
                return ResponseEntity.notFound().build();
            else if (e.getMessage().equals("User does not have permission to modify the bookshelf"))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            else
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets the amount of books read by the circle
     *
     * @param bookshelfId ID of the bookshelf. (required)
     * @return a response entity with the number of books read
     */
    @Override
    public ResponseEntity<Integer> bookshelfBookshelfIdCircleInsightsBooksReadGet(UUID bookshelfId) {
        try {
            int count = bookshelfService.getNumberOfBooksReadCircle(bookshelfId);
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
     * Gets the most preferred genres of the bookshelf/circle
     *
     * @param bookshelfId ID of the bookshelf. (required)
     * @return a response entity with a list of most preferred genres in order
     */
    @Override
    public ResponseEntity<List<String>> bookshelfBookshelfIdCircleInsightsPreferredGenresGet(UUID bookshelfId) {
        try {
            List<String> list = bookshelfService.getPreferredGenresCircle(bookshelfId);
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
}
