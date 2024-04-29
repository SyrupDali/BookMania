package nl.tudelft.sem.template.example.services;

import javassist.NotFoundException;
import lombok.Setter;
import nl.tudelft.sem.template.example.database.BookWrapperRepository;
import nl.tudelft.sem.template.example.entities.BookWrapperId;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.example.exceptions.NullException;
import nl.tudelft.sem.template.example.validators.*;
import nl.tudelft.sem.template.model.*;
import nl.tudelft.sem.template.example.database.BookRepository;
import nl.tudelft.sem.template.example.database.BookshelfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
public class BookshelfService {

    private final BookshelfRepository bookshelfRepository;
    private final BookRepository bookRepository;
    private final BookWrapperRepository bookWrapperRepository;
    private final UserService userService;

    @Setter
    private Validator circleValidator;

    @Autowired
    public BookshelfService(BookshelfRepository bookshelfRepository, BookRepository bookRepository,
                            UserService userService, BookWrapperRepository bookWrapperRepository) {
        this.bookshelfRepository = bookshelfRepository;
        this.bookRepository = bookRepository;
        this.userService = userService;
        this.bookWrapperRepository = bookWrapperRepository;
        circleValidator = BaseValidator.link(
                new BookshelfIdValidator(bookshelfRepository),
                new MemberIdValidator(userService.getUserRepository()),
                new PermissionValidator(bookshelfRepository, userService.getUserRepository())
        );
    }

    /**
     * Retrieves all the bookshelves existent in the database
     *
     * @return a List of all the Bookshelf objects that are in the database
     */
    public List<Bookshelf> getAllBookshelves() {
        return bookshelfRepository.findAll();
    }

    /**
     * Retrieves only the public bookshelves existent in the database
     *
     * @return a List of Bookshelf objects that have the privacy variable set to PUBLIC
     */
    public List<Bookshelf> getAllPublicBookshelves() {
        List<Bookshelf> publicBookshelves = new ArrayList<>();
        for (Bookshelf b : bookshelfRepository.findAll()) {
            if (b.getPrivacy() == Bookshelf.PrivacyEnum.PUBLIC) {
                publicBookshelves.add(b);
            }
        }
        return publicBookshelves;
    }

    /**
     * Adds a new Bookshelf instance to the database
     *
     * @param bookshelfPostRequest the details of the new Bookshelf
     * @param userId               the id of the user that wants to create this bookshelf
     * @return a Bookshelf instance with the details from the PostRequest of the user
     * @throws IllegalArgumentException if there was something wrong with the request
     * @throws NotFoundException        if the user is not found in the database
     */
    public Bookshelf addBookshelf(BookshelfPostRequest bookshelfPostRequest, UUID userId) throws IllegalArgumentException, NotFoundException, NullException {
        if (!userService.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        if (bookshelfPostRequest.getTitle() == null
                || bookshelfPostRequest.getTitle().isEmpty()
                || bookshelfPostRequest.getDescription() == null
                || bookshelfPostRequest.getDescription().isEmpty()
                || bookshelfPostRequest.getPrivacy() == null) {
            throw new IllegalArgumentException();
        }

        Bookshelf bookshelf = new Bookshelf();
        User owner = userService.findById(userId);
        bookshelf.setOwner(owner);
        //bookshelf.addMembersItem(owner);
        bookshelf.setTitle(bookshelfPostRequest.getTitle());
        bookshelf.setDescription(bookshelfPostRequest.getDescription());
        String privacy = bookshelfPostRequest.getPrivacy().getValue();

        if (privacy.equals("PUBLIC")) {
            bookshelf.setPrivacy(Bookshelf.PrivacyEnum.PUBLIC);
        } else {
            bookshelf.setPrivacy(Bookshelf.PrivacyEnum.PRIVATE);
        }

        return bookshelfRepository.save(bookshelf);
    }

    /**
     * Deletes a certain Bookshelf from the database
     *
     * @param bookshelfId the id of the Bookshelf we want to delete
     * @param userId      the user who wants to delete the bookshelf
     * @return the Bookshelf instance of the deleted object
     * @throws IllegalArgumentException if the user is not allowed to delete the bookshelf
     * @throws NotFoundException        if there is no such bookshelf
     */
    public Bookshelf deleteBookshelf(UUID bookshelfId, UUID userId) throws Exception {
        if (bookshelfId == null || userId == null) {
            throw new IllegalArgumentException();
        }

        if (!userService.isUserOwnerOfBookshelf(userId, bookshelfId)) {
            throw new IllegalArgumentException();
        }

        List<Book> books = bookshelfRepository.findById(bookshelfId).get().getBooks();
        List<Book> booksCopy = new ArrayList<>(books);
        for (Book b : booksCopy) {
            removeBookFromBookshelf(bookshelfId, userId, b.getBookId());
        }
        Bookshelf b = bookshelfRepository.findById(bookshelfId).get();
        bookshelfRepository.deleteById(bookshelfId);
        return b;
    }

    /**
     * Edits all the fields of a bookshelf
     *
     * @param bookshelfId         the ID of the bookshelf that is edited
     * @param userId              the ID of the user the owns the bookshelf
     * @param bookshelfPutRequest the updated details of the bookshelf
     * @return A bookshelf with the updated fields
     * @throws IllegalArgumentException if any of the provided details is null or empty
     */
    public Bookshelf editBookshelf(UUID bookshelfId, UUID userId, BookshelfBookshelfIdPutRequest bookshelfPutRequest) throws IllegalArgumentException {

        if (bookshelfPutRequest.getTitle() == null
                || bookshelfPutRequest.getTitle().isEmpty()
                || bookshelfPutRequest.getDescription() == null
                || bookshelfPutRequest.getDescription().isEmpty()
                || bookshelfPutRequest.getPrivacy() == null) {
            throw new IllegalArgumentException();
        }

        circleValidator.handle(bookshelfId, userId, userId);

        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).get();
        bookshelf.setTitle(bookshelfPutRequest.getTitle());
        bookshelf.setDescription(bookshelfPutRequest.getDescription());
        String privacy = bookshelfPutRequest.getPrivacy().getValue();
        if (privacy.equals("PUBLIC")) {
            if (!bookshelf.getPrivacy().getValue().equals("PUBLIC")) {
                bookshelf.setPrivacy(Bookshelf.PrivacyEnum.PUBLIC);
            }
        } else {
            if (!bookshelf.getPrivacy().getValue().equals("PRIVATE")) {
                bookshelf.setPrivacy(Bookshelf.PrivacyEnum.PRIVATE);
            }
        }

        return bookshelfRepository.save(bookshelf);
    }

    /**
     * Edits the description of a bookshelf
     *
     * @param bookshelfId    the ID of the bookshelf that is edited
     * @param userId         the ID of the user the owns the bookshelf
     * @param newDescription the new description
     * @return A bookshelf with the updated description
     * @throws IllegalArgumentException if any of the provided details is null or empty
     */
    public Bookshelf editDescriptionBookshelf(UUID bookshelfId, UUID userId, String newDescription) throws IllegalArgumentException {
        circleValidator.handle(bookshelfId, userId, userId);

        if (newDescription == null || newDescription.isEmpty()) {
            throw new IllegalArgumentException();
        }

        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).get();
        bookshelf.setDescription(newDescription);
        return bookshelfRepository.save(bookshelf);
    }

    /**
     * Edits the privacy of a bookshelf
     *
     * @param bookshelfId the ID of the bookshelf that is edited
     * @param userId      the ID of the user the owns the bookshelf
     * @param newPrivacy  the new privacy
     * @return A bookshelf with the updated privacy
     * @throws IllegalArgumentException if any of the provided details is null or empty
     */
    public Bookshelf editPrivacyBookshelf(UUID bookshelfId, UUID userId, String newPrivacy) throws IllegalArgumentException {
        if (newPrivacy == null || newPrivacy.isEmpty()) {
            throw new IllegalArgumentException();
        }
        circleValidator.handle(bookshelfId, userId, userId);

        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).get();

        if (newPrivacy.equals("PUBLIC")) {
            bookshelf.setPrivacy(Bookshelf.PrivacyEnum.PUBLIC);
        } else {
            bookshelf.setPrivacy(Bookshelf.PrivacyEnum.PRIVATE);
        }

        return bookshelfRepository.save(bookshelf);
    }

    /**
     * Edits the title of a bookshelf
     *
     * @param bookshelfId the ID of the bookshelf that is edited
     * @param userId      the ID of the user the owns the bookshelf
     * @param newTitle    the new title
     * @return A bookshelf with the updated title
     * @throws IllegalArgumentException if any of the provided details is null or empty
     */
    public Bookshelf editTitleBookshelf(UUID bookshelfId, UUID userId, String newTitle) throws IllegalArgumentException {
        if (newTitle == null || newTitle.isEmpty()) {
            throw new IllegalArgumentException();
        }

        circleValidator.handle(bookshelfId, userId, userId);

        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).get();
        bookshelf.setTitle(newTitle);
        return bookshelfRepository.save(bookshelf);
    }

    public Bookshelf addBookToBookshelf(UUID bookshelfId, UUID userId, UUID bookId) throws Exception {
        // Validate inputs
        circleValidator.handle(bookshelfId, userId, userId);
        // Get entities from the database
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).orElseThrow(() ->
                new NotFoundException("Bookshelf not found"));

        Book book = bookRepository.findById(bookId).orElseThrow(() ->
                new NotFoundException("Book not found"));
        if (bookshelf.getBooks().contains(book)) {
            throw new ValidationException("Book already exists in the bookshelf");
        }
        // Add book to bookshelf
        bookshelf.addBooksItem(book);

        // Save the updated bookshelf back to the database
        bookshelfRepository.save(bookshelf);

        // Add book to all user's set of books
        List<User> members = new ArrayList<>(bookshelf.getMembers());
        members.add(bookshelf.getOwner());
        for (User user: members) {
            addBookWrapper(bookId, user.getUserId());
        }
        return bookshelf;
    }

    /**
     * Adds multiple books to a bookshelf
     *
     * @param bookshelfId  the ID of the bookshelf that is edited
     * @param userId       the ID of the user the owns the bookshelf
     * @param bookIdsToAdd the IDs of the books that are added
     * @return A list of books that are added
     * @throws IllegalArgumentException if any of the provided details is null or empty
     */
    public List<Book> addMultipleBooksToBookshelf(UUID bookshelfId, UUID userId, List<UUID> bookIdsToAdd)
            throws Exception {
        circleValidator.handle(bookshelfId, userId, userId);
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).orElseThrow(() -> new ValidationException("Bookshelf not found"));
        List<UUID> bookIds = bookshelf.getBooks().stream().map(Book::getBookId).collect(Collectors.toList());
        // assert if all the bookIdsToAdd are not in the bookIds
        // otherwise throw exception
        for (UUID bookId : bookIdsToAdd) {
            if (bookId == null) {
                throw new ValidationException("Book id cannot be null");
            }
        }
        for (UUID bookId : bookIdsToAdd) {
            if (!bookRepository.existsById(bookId)) {
                throw new ValidationException("Book not found");
            }
        }
        for (UUID bookId : bookIdsToAdd) {
            if (bookIds.contains(bookId)) {
                throw new ValidationException("Book already exists in the bookshelf");
            }
        }
        List<Book> booksAdded = new ArrayList<>();
        // add all the books to the bookshelf
        for (UUID bookId : bookIdsToAdd) {
            Book book = bookRepository.findById(bookId).orElseThrow(() -> new ValidationException("Book not found"));
            bookshelf.addBooksItem(book);
            booksAdded.add(book);
            bookIds.add(bookId);
        }
        bookshelfRepository.save(bookshelf);
        List<User> members = bookshelf.getMembers();
        UUID ownerId = bookshelf.getOwner().getUserId();
        // add all the books to all the users' set of books
        for (UUID bookId : bookIdsToAdd) {
            for (User user : members) {
                addBookWrapper(bookId, user.getUserId());
            }
            addBookWrapper(bookId, ownerId);
        }
        return booksAdded;
    }

    public Bookshelf removeBookFromBookshelf(UUID bookshelfId, UUID userId, UUID bookId) throws Exception {
        // Validate inputs
        circleValidator.handle(bookshelfId, userId, userId);

        // Get entities from the database
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).orElseThrow(() ->
                new NotFoundException("Bookshelf not found"));

        Book book = bookRepository.findById(bookId).orElseThrow(() ->
                new NotFoundException("Book not found"));

        BookWrapperId id = new BookWrapperId();
        id.setBookId(bookId);
        id.setUserId(userId);
        BookWrapper bw = bookWrapperRepository.findById(id).orElseThrow(()
                -> new NotFoundException("Book wrapper not found"));

        // Remove book from bookshelf
        bookshelf.getBooks().remove(book);

        // Save the updated bookshelf back to the database
        bookshelfRepository.save(bookshelf);

        // Remove book wrapper from repository if the book is not in any other bookshelf
        for (User user : bookshelf.getMembers()) {
            if (countBookWrapperInstances(user.getUserId(), bw) == 0) {
                bookWrapperRepository.delete(bw);
            }
        }
        if (countBookWrapperInstances(bookshelf.getOwner().getUserId(), bw) == 0) {
            bookWrapperRepository.delete(bw);
        }

        return bookshelf;
    }

    /**
     * Deletes multiple books from a specified bookshelf.
     * @param bookshelfId the ID of the bookshelf from which the user wants to delete the books
     * @param userId the ID of the user that wants to delete the books
     * @param bookIdListToRemove a list of IDs of the books that should be removed
     * @throws Exception if something goes wrong
     */
    public void removeMultipleBooksFromBookshelf(UUID bookshelfId, UUID userId, List<UUID> bookIdListToRemove) throws Exception {
        circleValidator.handle(bookshelfId, userId, userId);

        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).orElseThrow(() ->
                new ValidationException("Bookshelf not found"));
        List<UUID> bookIdsAlreadyInBookshelf = bookshelf.getBooks().stream().map(Book::getBookId).collect(Collectors.toList());
        Map<UUID, BookWrapperId> wrappers = new HashMap<>();

        //Check that it is ok to remove the books
        for(UUID bookId: bookIdListToRemove) {
            if(bookId == null) {
                throw new ValidationException("Book id cannot be null");
            } else if (!bookRepository.existsById(bookId)) {
                throw new ValidationException("Book not found");
            } else if(!bookIdsAlreadyInBookshelf.contains(bookId)) {
                //then the book is not in the bookshelf
                throw new ValidationException("Book is not found in specified bookshelf");
            } else {
                BookWrapperId wrapperId = new BookWrapperId();
                wrapperId.setBookId(bookId);
                wrapperId.setUserId(userId);
                if(!bookWrapperRepository.existsById(wrapperId)) {
                    throw new ValidationException("Book wrapper not found");
                }
                wrappers.put(bookId, wrapperId);
            }
        }
        //Remove the books
        for(UUID bookId: bookIdListToRemove) {
            // Get the book wrapper
            // (it shouldn't throw the exception here because we already check that the wrapper exists)
            BookWrapperId wrapperId = wrappers.get(bookId);
            BookWrapper bookWrapper = bookWrapperRepository.findById(wrapperId).orElseThrow(()
                    -> new ValidationException("Book wrapper not found"));

            // Get the book
            // (it shouldn't throw the exception here because we already check that the book exists)
            Book book = bookRepository.findById(bookId).orElseThrow(() ->
                    new ValidationException("Book not found"));

            //Remove the book
            bookshelf.getBooks().remove(book);

            // Remove book wrapper from repository if the book is not in any other bookshelves
            for (User user : bookshelf.getMembers()) {
                if (countBookWrapperInstances(user.getUserId(), bookWrapper) == 0) {
                    bookWrapperRepository.delete(bookWrapper);
                }
            }
            if (countBookWrapperInstances(bookshelf.getOwner().getUserId(), bookWrapper) == 0) {
                bookWrapperRepository.delete(bookWrapper);
            }
        }

        bookshelfRepository.save(bookshelf);
    }

    /**
     * Retrieves the bookshelf with the given id
     *
     * @param bookshelfId the id of the bookshelf we want to retrieve
     * @return a List of Bookshelf objects that the user has access to
     * @throws IllegalArgumentException if the id is null
     * @throws ValidationException      if the bookshelf is not found
     */
    public Bookshelf getBookshelfById(UUID bookshelfId) throws NotFoundException {
        if (bookshelfId == null) {
            throw new IllegalArgumentException();
        }
        return bookshelfRepository.findById(bookshelfId)
                .orElseThrow(() -> new NotFoundException("Bookshelf not found with ID: " + bookshelfId));
    }

    /**
     * Count the number of a user's bookshelves that contain a specific book.
     *
     * @param userId the user id
     * @param bw     the book wrapper
     * @return the number of bookshelves that contain the book
     */
    private int countBookWrapperInstances(UUID userId, BookWrapper bw) throws Exception {
        List<Bookshelf> allBookshelves = bookshelfRepository.findAll();
        Book book = bookRepository.findById(bw.getBookId()).orElseThrow();
        User user = userService.findById(userId);

        int count = 0;
        for (Bookshelf bs : allBookshelves) {
            if ((bs.getMembers().contains(user) || bs.getOwner().getUserId().equals(userId)) && bs.getBooks().contains(book)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Add a book wrapper to the repository.
     *
     * @param bookId the book id
     * @param userId the user id
     */
    public void addBookWrapper(UUID bookId, UUID userId) throws Exception {
        // Add book to the user's set of books
        BookWrapper bw = new BookWrapper();
        bw.setBookId(bookId);
        bw.setUserId(userId);
        bw.setCurrentPage(0);

        if (countBookWrapperInstances(userId, bw) == 1)
            bookWrapperRepository.save(bw);
    }

    /**
     * Get the number of books that have been read by all users.
     * The book must have been marked as read by all users for it to count.
     *
     * @param bookshelfId The bookshelfId of the shelf to count the read books for.
     * @return The number of books that have been read by all users.
     * @throws NotFoundException Thrown when the bookshelf is not in the database
     */
    public int getNumberOfBooksReadCircle(UUID bookshelfId) throws NotFoundException {
        if (bookshelfId == null) {
            throw new IllegalArgumentException("Bookshelf id is null");
        }
        boolean exist = bookshelfRepository.existsById(bookshelfId);
        if (!exist) {
            throw new NotFoundException("Bookshelf not found");
        }
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).get();
        Set<User> users = new HashSet<>();
        users.add(bookshelf.getOwner());
        users.addAll(bookshelf.getMembers());
        List<Book> books = bookshelf.getBooks();
        return (int) books.stream()
                .filter(b -> users.stream()
                        .map(u -> new BookWrapperId(b.getBookId(), u.getUserId()))
                        .map(id -> bookWrapperRepository.findById(id).orElse(null))
                        .filter(Objects::nonNull)
                        .allMatch(wrapper -> wrapper.getReadingStatus().equals(BookWrapper.ReadingStatusEnum.READ)))
                .count();
    }

    /**
     * Gets the three most preferred genres of a circle, if genres have
     * equal frequency, they are sorted by name ascending ly.
     *
     * @param bookshelfId the bookshelfId of which the get the top genres
     * @return List genres with on position 0 the most frequent one
     * @throws NotFoundException if the bookshelf is not found
     */
    public List<String> getPreferredGenresCircle(UUID bookshelfId) throws NotFoundException {
        boolean exist = bookshelfRepository.existsById(bookshelfId);
        if (!exist) {
            throw new NotFoundException("Bookshelf not found");
        }
        List<Book> books = bookshelfRepository.findById(bookshelfId).get().getBooks();
        return books.stream()
                .map(Book::getGenres)
                .flatMap(Collection::stream)
                .map(Book.GenresEnum::getValue)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted((e1, e2) -> {
                    int compare = e2.getValue().compareTo(e1.getValue());
                    return compare != 0 ? compare : e1.getKey().compareTo(e2.getKey());
                })
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Method that adds a book wrapper to multiple books for a specific user
     * @param userId the id of the user whose wrappers we are creating
     * @param books the list of books for which we want to create wrappers
     * @throws Exception something went wrong
     */
    public void addBookWrapperMultiple(UUID userId, List<Book> books) throws Exception{
        for (Book b : books) {
            addBookWrapper(b.getBookId(), userId);
        }
    }

    /**
     * Method that deletes multiple book wrappers for a specific user
     * @param userId the user whose wrappers we are deleting
     * @param books the list of books whose wrappers we want gone
     */
    public void deleteBookWrapperMultiple(UUID userId, List<Book> books) throws Exception{
        for (Book b : books) {
            BookWrapperId id = new BookWrapperId(b.getBookId(), userId);
            Optional<BookWrapper> bw = bookWrapperRepository.findById(id);
            if (bw.isEmpty()) continue; // this should technically never be the case, assuming the code is correct
                                        // and that each user has a book wrapper for all the books in their bookshelves
                                        // so if we end up in this case, just skip and dont break the code
            BookWrapper bookWrapper = bw.get();
            if (countBookWrapperInstances(userId, bookWrapper) == 0) {
                bookWrapperRepository.deleteById(id);
            }
        }
    }
}
