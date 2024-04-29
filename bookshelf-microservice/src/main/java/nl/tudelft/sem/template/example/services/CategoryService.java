package nl.tudelft.sem.template.example.services;

import javassist.NotFoundException;
import lombok.Setter;
import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.database.CategoryRepository;
import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.InvalidDataException;
import nl.tudelft.sem.template.example.exceptions.NullException;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.example.validators.BaseValidator;
import nl.tudelft.sem.template.example.validators.BookshelfIdValidator;
import nl.tudelft.sem.template.example.validators.MemberIdValidator;
import nl.tudelft.sem.template.example.validators.PermissionValidator;
import nl.tudelft.sem.template.example.validators.Validator;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.Category;
import nl.tudelft.sem.template.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BookshelfRepository bookshelfRepository;

    /**
     * Currently the user validator only verifies if a user exists in the database
     * We decided to keep a validator instead of just writing a method in order to allow
     * for easily adding the authentication functionality in the future
     * and to avoid a circular dependency with the UserService
     */
    @Setter
    private Validator userValidator;
    @Setter
    private Validator permissionValidator; // TODO need Noky's branch

    /**
     * Automated constructor for the class
     *
     * @param categoryRepository the database table containing the information about categories
     */
    @Autowired
    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository,
                           BookshelfRepository bookshelfRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.bookshelfRepository = bookshelfRepository;
        userValidator = BaseValidator.link(
                new MemberIdValidator(userRepository)
        );
        permissionValidator = BaseValidator.link(
                new MemberIdValidator(userRepository),
                new BookshelfIdValidator(bookshelfRepository),
                new PermissionValidator(bookshelfRepository, userRepository)
        );
    }

    /**
     * Method that returns a list of all the categories in the database
     *
     * @return a list of all the categories found
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Method that gets all the categories created by a specific user
     *
     * @param userId the id of the user whose categories we are looking for
     * @return a list of all the categories found
     * @throws ValidationException if userid is null or user is not found
     */
    public List<Category> getAllCategoriesForUser(UUID userId) throws ValidationException {
        userValidator.handle(null, null, userId);

        return categoryRepository.findAll().stream().filter(category -> category.getUser().getUserId().equals(userId)).collect(Collectors.toList());
    }

    /**
     * Method that creates a new category and stores it in the database
     *
     * @param userId      the id of the user to whom this category should belong to
     * @param name        the name of the category. Cannot be null
     * @param description the description of the category
     * @return the list of categories this user has created, including the new one
     * @throws ValidationException if userid is null or user is not found
     * @throws NotFoundException   no user with the specified id found somehow after validating
     * @throws NullException       if the category name is null
     */
    public List<Category> createCategory(UUID userId, String name, String description) throws InvalidDataException, NotFoundException, NullException, ValidationException {
        userValidator.handle(null, null, userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        if (name == null)
            throw new NullException("Category name cannot be null");
        if (name.isEmpty())
            throw new InvalidDataException("Category name cannot be empty");

        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setDescription(description);
        category.setBookshelves(new ArrayList<>()); // TODO test this more, but i think it works
        categoryRepository.save(category);

        return getAllCategoriesForUser(userId);
    }

    /**
     * Method that returns the category of a bookshelf for a specific user
     *
     * @param userId      the id of the user whose bookshelf (category) we are looking for
     * @param bookshelfId the id of the bookshelf whose category we are looking for
     * @return the category if its found, or null if the bookshelf does not have a category
     * @throws ValidationException if userid/bookshelf is null or user/bookshelf is not found
     * @throws NotFoundException   the bookshelf or the user do not exist somehow after validating
     */
    public Category getCategoryForBookshelf(UUID userId, UUID bookshelfId) throws NotFoundException, ValidationException {
        permissionValidator.handle(bookshelfId, null, userId); // should handle checking the bookshelf too
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).orElseThrow(() -> new NotFoundException("Bookshelf not found"));

        List<Category> categories = getAllCategoriesForUser(userId);
        for (Category c : categories) {
            if (c.getBookshelves().contains(bookshelf)) {
                return c;
            }
        }

        return null;

    }

    /**
     * Method that deletes a category from the database
     *
     * @param userId     the id of the user trying to delete this category
     * @param categoryId the id of the category to be deleted
     * @return the new list of categories, without the deleted one
     * @throws ValidationException if userid is null or user is not found
     * @throws NotFoundException   the user/category with the specified id does not exist
     * @throws NullException       if the given category id is null
     */
    public List<Category> deleteCategory(UUID userId, UUID categoryId) throws NotFoundException, NullException, ValidationException {
        userValidator.handle(null, null, userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        if (categoryId == null)
            throw new NullException("Category id cannot be null");

        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Category with id " + categoryId + " does not exist"));

        if (!category.getUser().equals(user))
            throw new UnsupportedOperationException("User does not own the category");

        categoryRepository.delete(category);
        return getAllCategoriesForUser(userId);
    }

    /**
     * Method that sets the category of a bookshelf. This action can only be done if the user is part of the circle
     * and if the user owns the category
     *
     * @param userId      the id of the user performing the action
     * @param bookshelfId the id of the bookshelf whose category we want to set
     * @param categoryId  the id of the category of the bookshelf
     * @return the Bookshelf object whose category field has been updated
     * @throws ValidationException if userid/bookshelfid is null or user/bookshelf is not found
     * @throws NotFoundException   the user, category or the bookshelf with specified id not found
     * @throws NullException       if any of the ids are null
     */
    public Bookshelf setCategoryAuthenticated(UUID userId, UUID bookshelfId, UUID categoryId) throws NotFoundException, NullException, ValidationException {
        if (userId == null || bookshelfId == null || categoryId == null)
            throw new NullException("Ids cannot be null");

        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Category with id " + categoryId + " not found"));

        permissionValidator.handle(bookshelfId, null, userId);
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).orElseThrow(() -> new NotFoundException("No bookshelf found"));

        // remove the old category
        removeCategoryAuthenticated(userId, bookshelfId);

        category.addBookshelvesItem(bookshelf);
        categoryRepository.save(category);

        return bookshelf;
    }

    /**
     * Method that removes the category of a bookshelf by removing from the list of bookshelves that a category is assigned to
     *
     * @param userId      the id of the user who is performing the action
     * @param bookshelfId the id of the bookshelf whose category we want to remove
     * @return the bookshelf object whose category was removed
     * @throws ValidationException if userid/bookshelfid is null or user/bookshelf is not found
     * @throws NotFoundException   the user or the bookshelf with specified id not found
     */
    public Bookshelf removeCategoryAuthenticated(UUID userId, UUID bookshelfId) throws NullException, NotFoundException, ValidationException {
        if (userId == null || bookshelfId == null)
            throw new NullException("Ids cannot be null");

        permissionValidator.handle(bookshelfId, null, userId);
        Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId).orElseThrow(() -> new NotFoundException("No bookshelf found"));

        List<Category> categories = getAllCategoriesForUser(userId);
        for (Category c : categories) {
            if (c.getBookshelves().remove(bookshelf)) {
                categoryRepository.save(c);
                return bookshelf;
            }
        }

        return bookshelf;
    }

    /**
     * Method that creates all the default categories that a user should have.
     *
     * @param user the id of the user who has just been created
     * @throws ValidationException if userid is null or user is not found
     * @throws NotFoundException   thrown if user with specified id does not exist
     * @throws NullException       if the category name is null
     */
    public void createDefaultCategories(UUID user) throws NotFoundException, NullException, ValidationException, InvalidDataException {
        createCategory(user, "Favorites", "A category for bookshelves of my favorite books.");
        createCategory(user, "Read books", "A category for bookshelves of books that I have read.");
        createCategory(user, "Currently reading", "A category for bookshelves of books that I am reading.");
        createCategory(user, "Wish list", "A category for bookshelves of books that I want.");
    }

    /**
     * Method that should get called when a user is deleted from the database such that all the user's categories are deleted too
     *
     * @param userId the id of the user that is being deleted
     * @throws ValidationException if userid is null or user is not found
     * @throws NotFoundException   thrown if a user with the specified id is not found
     * @throws NullException       if the category id is null
     */
    public void deleteUser(UUID userId) throws NotFoundException, NullException, ValidationException {
        List<Category> userCategories = getAllCategoriesForUser(userId);
        for (Category category : userCategories) {
            deleteCategory(userId, category.getCategoryId());
        }
    }


}
