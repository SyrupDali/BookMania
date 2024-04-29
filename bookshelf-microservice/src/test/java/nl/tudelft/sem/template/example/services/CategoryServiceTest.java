package nl.tudelft.sem.template.example.services;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javassist.NotFoundException;
import nl.tudelft.sem.template.example.database.BookshelfRepository;
import nl.tudelft.sem.template.example.database.CategoryRepository;
import nl.tudelft.sem.template.example.database.UserRepository;
import nl.tudelft.sem.template.example.exceptions.InvalidDataException;
import nl.tudelft.sem.template.example.exceptions.NullException;
import nl.tudelft.sem.template.example.exceptions.ValidationException;
import nl.tudelft.sem.template.example.validators.BaseValidator;
import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.Category;
import nl.tudelft.sem.template.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryServiceTest {

    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final BookshelfRepository bookshelfRepository = mock(BookshelfRepository.class);
    private final BaseValidator userValidator = mock(BaseValidator.class);
    private final BaseValidator permissionValidator = mock(BaseValidator.class);
    private CategoryService cs;

    private Category c1;
    private Category c2;
    private Category c3;

    private User u1;
    private User u2;

    private Bookshelf b1;
    private Bookshelf b2;
    private Bookshelf b3;

    @BeforeEach
    public void setup() {
        u1 = new User(UUID.randomUUID());
        u2 = new User(UUID.randomUUID());

        c1 = new Category(UUID.randomUUID(), u1, new ArrayList<>(), "categ 1", "desc 1");
        c2 = new Category(UUID.randomUUID(), u2, new ArrayList<>(), "categ 2", "desc 2");
        c3 = new Category(UUID.randomUUID(), u2, new ArrayList<>(), "categ 3", "desc 3");

        b1 = new Bookshelf(UUID.randomUUID(), u1, "shelf 1", "desc s1", new ArrayList<>(), Bookshelf.PrivacyEnum.PUBLIC, new ArrayList<>(), new ArrayList<>());
        b2 = new Bookshelf(UUID.randomUUID(), u2, "shelf 2", "desc s2", new ArrayList<>(), Bookshelf.PrivacyEnum.PUBLIC, new ArrayList<>(), new ArrayList<>());
        b3 = new Bookshelf(UUID.randomUUID(), u2, "shelf 3", "desc s3", new ArrayList<>(), Bookshelf.PrivacyEnum.PUBLIC, new ArrayList<>(), new ArrayList<>());

        cs = new CategoryService(categoryRepository, userRepository, bookshelfRepository);
        cs.setUserValidator(userValidator);
        cs.setPermissionValidator(permissionValidator);
    }

    @Test
    public void testGetCategoriesEmpty() {
        when(categoryRepository.findAll()).thenReturn(new ArrayList<>());
        assertEquals(cs.getAllCategories(), new ArrayList<>());
    }

    @Test
    public void testGetCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        assertThat(cs.getAllCategories()).containsExactlyInAnyOrderElementsOf(List.of(c1, c2, c3));
    }

    @Test
    public void testGetUserCategoriesNullId() {
        when(userValidator.handle(null, null, null)).thenThrow(new ValidationException("User id cannot be null"));
        assertThatThrownBy(() -> cs.getAllCategoriesForUser(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User id cannot be null");
    }

    @Test
    public void testGetUserCategoriesNoSuchUser() {
        final UUID id = UUID.randomUUID();
        when(userValidator.handle(null, null, id)).thenThrow(new ValidationException("User not found"));
        assertThatThrownBy(() -> cs.getAllCategoriesForUser(id))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User not found");
    }

    @Test
    public void testGetUserCategoriesNoCategories() {
        c1.setUser(u2);
        when(userValidator.handle(null, null, u1.getUserId())).thenReturn(true);
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        assertThat(cs.getAllCategoriesForUser(u1.getUserId())).isEqualTo(new ArrayList<>());
    }

    @Test
    public void testGetUserCategories() {
        c2.setUser(u1);
        when(userValidator.handle(null, null, u1.getUserId())).thenReturn(true);
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        assertThat(cs.getAllCategoriesForUser(u1.getUserId())).containsExactly(c1, c2);
    }

    @Test
    public void testCreateCategoryNullId() {
        when(userValidator.handle(null, null, null)).thenThrow(new ValidationException("User id cannot be null"));
        assertThatThrownBy(() -> cs.createCategory(null, "name", "desc"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User id cannot be null");
    }

    @Test
    public void testCreateCategoryNoUser() {
        when(userValidator.handle(null, null, u1.getUserId())).thenThrow(new ValidationException("User not found"));
        assertThatThrownBy(() -> cs.createCategory(u1.getUserId(), "c1", "desc1"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User not found");
    }

    @Test
    public void testCreateCategoryNullName() {
        when(userValidator.handle(null, null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        assertThatThrownBy(() -> cs.createCategory(u1.getUserId(), null, "desc"))
                .isInstanceOf(NullException.class);
    }

    @Test
    public void testCreateCategoryEmptyName() {
        when(userValidator.handle(null, null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        assertThatThrownBy(() -> cs.createCategory(u1.getUserId(), "", "desc"))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    public void testCreateCategory() throws NotFoundException, NullException, InvalidDataException {
        c2.setUser(u1);
        when(userValidator.handle(null, null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        ArgumentCaptor<Category> cat = ArgumentCaptor.forClass(Category.class);
        assertThat(cs.createCategory(u1.getUserId(), c1.getName(), c1.getDescription())).containsExactly(c1, c2);
        verify(categoryRepository).save(cat.capture());
        assertThat(cat.getValue().getUser()).isEqualTo(u1);
        assertThat(cat.getValue().getName()).isEqualTo(c1.getName());
        assertThat(cat.getValue().getDescription()).isEqualTo(c1.getDescription());
        assertThat(cat.getValue().getBookshelves()).isEmpty();
    }

    @Test
    public void testGetCategoryForBookshelfNullNotFoundUser() {
        when(permissionValidator.handle(b1.getBookshelfId(), null, null)).thenThrow(new ValidationException("User id cannot be null"));
        assertThatThrownBy(() -> cs.getCategoryForBookshelf(null, b1.getBookshelfId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User id cannot be null");
    }

    @Test
    public void testCreateCategoryNotFoundAfterValidation() {
        c2.setUser(u1);
        when(userValidator.handle(null, null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cs.createCategory(u1.getUserId(), c1.getName(), c1.getDescription()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testGetCategoryForBookshelfNullBookshelf() {
        when(permissionValidator.handle(null, null, u1.getUserId())).thenThrow(new ValidationException("Bookshelf id cannot be null"));
        assertThatThrownBy(() -> cs.getCategoryForBookshelf(u1.getUserId(), null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Bookshelf id cannot be null");
    }

    @Test
    public void testGetCategoryForBookshelfNotFoundAfterValidation() {
        c2.setUser(u1);
        when(permissionValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenReturn(true);
        when(bookshelfRepository.findById(b1.getBookshelfId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cs.getCategoryForBookshelf(u1.getUserId(), b1.getBookshelfId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testGetCategoryForBookshelfUserNotInCircle() {
        when(permissionValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenThrow(new ValidationException("Not owner or circle"));
        assertThatThrownBy(() -> cs.getCategoryForBookshelf(u1.getUserId(), b1.getBookshelfId()))
                .isInstanceOf(ValidationException.class);
    }


    @Test
    public void testGetCategoryForBookshelfNoCategory() throws NotFoundException {
        c2.setUser(u1);
        b2.setOwner(u1);
        b3.setOwner(u1);
        c1.getBookshelves().add(b2);
        c2.getBookshelves().add(b3);
        when(userValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        when(bookshelfRepository.findById(b1.getBookshelfId())).thenReturn(Optional.of(b1));
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        assertNull(cs.getCategoryForBookshelf(u1.getUserId(), b1.getBookshelfId()));
    }

    @Test
    public void testGetCategoryForBookshelf() throws NotFoundException {
        c2.setUser(u1);
        b2.setOwner(u1);
        b3.setOwner(u1);
        c1.getBookshelves().add(b1);
        c1.getBookshelves().add(b2);
        c2.getBookshelves().add(b3);
        when(userValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        when(bookshelfRepository.findById(b1.getBookshelfId())).thenReturn(Optional.of(b1));
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        assertThat(cs.getCategoryForBookshelf(u1.getUserId(), b1.getBookshelfId())).isEqualTo(c1);
    }

    @Test
    public void testDeleteCategoryNullUser() {
        when(userValidator.handle(null, null, null)).thenThrow(new ValidationException("User id cannot be null"));
        assertThatThrownBy(() -> cs.deleteCategory(null, c1.getCategoryId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User id cannot be null");
    }

    @Test
    public void testDeleteCategoryNullCategory() {
        when(userValidator.handle(null, null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        assertThatThrownBy(() -> cs.deleteCategory(u1.getUserId(), null))
                .isInstanceOf(NullException.class);
    }

    @Test
    public void testDeleteCategoryNotFound() {
        when(userValidator.handle(null, null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        when(categoryRepository.findById(c1.getCategoryId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cs.deleteCategory(u1.getUserId(), c1.getCategoryId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testDeleteCategoryNotFoundAfterValidation() {
        c2.setUser(u1);
        when(userValidator.handle(null, null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cs.deleteCategory(u1.getUserId(), c1.getCategoryId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testDeleteCategoryUserNotOwn() {
        when(userValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        when(categoryRepository.findById(c2.getCategoryId())).thenReturn(Optional.of(c2));
        assertThatThrownBy(() -> cs.deleteCategory(u1.getUserId(), c2.getCategoryId()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void testDeleteCategory() throws NotFoundException, NullException {
        when(userValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        when(categoryRepository.findById(c1.getCategoryId())).thenReturn(Optional.of(c1));
        ArgumentCaptor<Category> cat = ArgumentCaptor.forClass(Category.class);
        // i cannot assert the list because i would only be testing the mocks
        c2.setUser(u1);
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        assertThat(cs.deleteCategory(u1.getUserId(), c1.getCategoryId())).containsExactly(c1, c2);
        verify(categoryRepository).delete(cat.capture());
        assertThat(cat.getValue()).isEqualTo(c1);
    }

    @Test
    public void testSetCategoryNulls() {
        assertThatThrownBy(() -> cs.setCategoryAuthenticated(null, b1.getBookshelfId(), c1.getCategoryId()))
                .isInstanceOf(NullException.class);

        assertThatThrownBy(() -> cs.setCategoryAuthenticated(u1.getUserId(), null, c1.getCategoryId()))
                .isInstanceOf(NullException.class);

        assertThatThrownBy(() -> cs.setCategoryAuthenticated(u1.getUserId(), b1.getBookshelfId(), null))
                .isInstanceOf(NullException.class);
    }

    @Test
    public void testSetCategoryNotFound() {
        when(categoryRepository.findById(c1.getCategoryId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cs.setCategoryAuthenticated(u1.getUserId(), b1.getBookshelfId(), c1.getCategoryId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testSetCategoryNotFoundAfterValidation() {
        c2.setUser(u1);
        when(categoryRepository.findById(c1.getCategoryId())).thenReturn(Optional.of(c1));
        when(permissionValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenReturn(true);
        when(bookshelfRepository.findById(b1.getBookshelfId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cs.setCategoryAuthenticated(u1.getUserId(), b1.getBookshelfId(), c1.getCategoryId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testSetCategoryUserNotInCircle() {
        when(categoryRepository.findById(c1.getCategoryId())).thenReturn(Optional.of(c1));
        when(permissionValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenThrow(new ValidationException("Not owner or circle"));
        assertThatThrownBy(() -> cs.setCategoryAuthenticated(u1.getUserId(), b1.getBookshelfId(), c1.getCategoryId()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testSetCategory() throws NotFoundException, NullException {
        when(categoryRepository.findById(c1.getCategoryId())).thenReturn(Optional.of(c1));
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        when(permissionValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        when(bookshelfRepository.findById(b1.getBookshelfId())).thenReturn(Optional.of(b1));
        assertThat(cs.setCategoryAuthenticated(u1.getUserId(), b1.getBookshelfId(), c1.getCategoryId()))
                .isEqualTo(b1);
        verify(categoryRepository).save(c1);
        assertThat(c1.getBookshelves()).contains(b1);
    }

    @Test
    public void testResetCategory() throws NotFoundException, NullException {
        c2.setUser(u1);
        c2.getBookshelves().add(b1);
        when(categoryRepository.findById(c1.getCategoryId())).thenReturn(Optional.of(c1));
        when(categoryRepository.findById(c2.getCategoryId())).thenReturn(Optional.of(c2));
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        when(permissionValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        when(bookshelfRepository.findById(b1.getBookshelfId())).thenReturn(Optional.of(b1));
        assertThat(cs.setCategoryAuthenticated(u1.getUserId(), b1.getBookshelfId(), c1.getCategoryId()))
                .isEqualTo(b1);
        verify(categoryRepository).save(c2);
        verify(categoryRepository).save(c1);
        assertThat(c1.getBookshelves()).contains(b1);
        assertThat(c2.getBookshelves()).doesNotContain(b1);
    }

    @Test
    public void testRemoveCategoryNulls() {
        assertThatThrownBy(() -> cs.removeCategoryAuthenticated(null, b1.getBookshelfId()))
                .isInstanceOf(NullException.class);

        assertThatThrownBy(() -> cs.removeCategoryAuthenticated(u1.getUserId(), null))
                .isInstanceOf(NullException.class);
    }

    @Test
    public void testRemoveCategoryNotFoundAfterValidation() {
        c2.setUser(u1);
        when(permissionValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenReturn(true);
        when(bookshelfRepository.findById(b1.getBookshelfId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cs.removeCategoryAuthenticated(u1.getUserId(), b1.getBookshelfId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testRemoveCategoryUserNotInCircle() {
        when(permissionValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenThrow(new ValidationException("Not owner or circle"));
        assertThatThrownBy(() -> cs.removeCategoryAuthenticated(u1.getUserId(), b1.getBookshelfId()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testRemoveCategoryNoPrevious() throws NotFoundException, NullException {
        c2.setUser(u1);
        c3.setUser(u1);
        when(permissionValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        when(bookshelfRepository.findById(b1.getBookshelfId())).thenReturn(Optional.of(b1));
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        assertThat(cs.removeCategoryAuthenticated(u1.getUserId(), b1.getBookshelfId())).isEqualTo(b1);
        verify(categoryRepository, times(0)).save(any());
    }

    @Test
    public void testRemoveCategoryPrevious() throws NotFoundException, NullException {
        c2.setUser(u1);
        c2.getBookshelves().add(b1);
        c3.setUser(u1);
        when(permissionValidator.handle(b1.getBookshelfId(), null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        when(bookshelfRepository.findById(b1.getBookshelfId())).thenReturn(Optional.of(b1));
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        assertThat(cs.removeCategoryAuthenticated(u1.getUserId(), b1.getBookshelfId())).isEqualTo(b1);
        verify(categoryRepository, times(1)).save(c2);
    }

    @Test
    public void testCreateDefaultCategories() throws NotFoundException, NullException, InvalidDataException {
        when(permissionValidator.handle(null, null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        ArgumentCaptor<Category> cat = ArgumentCaptor.forClass(Category.class);
        cs.createDefaultCategories(u1.getUserId());
        verify(categoryRepository, times(4)).save(cat.capture());
        assertThat(cat.getAllValues().get(0).getUser()).isEqualTo(u1);
        assertThat(cat.getAllValues().get(0).getName()).isEqualTo("Favorites");
        assertThat(cat.getAllValues().get(1).getUser()).isEqualTo(u1);
        assertThat(cat.getAllValues().get(1).getName()).isEqualTo("Read books");
        assertThat(cat.getAllValues().get(2).getUser()).isEqualTo(u1);
        assertThat(cat.getAllValues().get(2).getName()).isEqualTo("Currently reading");
        assertThat(cat.getAllValues().get(3).getUser()).isEqualTo(u1);
        assertThat(cat.getAllValues().get(3).getName()).isEqualTo("Wish list");
    }

    @Test
    public void testDeleteUser() throws NotFoundException, NullException {
        c2.setUser(u1);
        when(permissionValidator.handle(null, null, u1.getUserId())).thenReturn(true);
        when(userRepository.findById(u1.getUserId())).thenReturn(Optional.of(u1));
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        when(categoryRepository.findById(c1.getCategoryId())).thenReturn(Optional.of(c1));
        when(categoryRepository.findById(c2.getCategoryId())).thenReturn(Optional.of(c2));
        ArgumentCaptor<Category> cat = ArgumentCaptor.forClass(Category.class);
        cs.deleteUser(u1.getUserId());
        verify(categoryRepository, times(2)).delete(cat.capture());
        assertThat(cat.getAllValues().get(0)).isEqualTo(c1);
        assertThat(cat.getAllValues().get(1)).isEqualTo(c2);
    }


}
