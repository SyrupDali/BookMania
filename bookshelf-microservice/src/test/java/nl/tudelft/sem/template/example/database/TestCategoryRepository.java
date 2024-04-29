package nl.tudelft.sem.template.example.database;

import nl.tudelft.sem.template.model.Category;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TestCategoryRepository implements CategoryRepository {
    public final List<Category> categoryList = new ArrayList<>();
    public final List<String> calledMethods = new ArrayList<>();

    private void call(String name) {
        calledMethods.add(name);
    }

    @Override
    public List<Category> findAll() {
        call("findAll");
        return categoryList;
    }


    @Override
    public List<Category> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Category> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<Category> findAllById(Iterable<UUID> uuids) {
        return null;
    }

    @Override
    public long count() {
        return categoryList.size();
    }

    @Override
    public void deleteById(UUID uuid) {
        call("deleteById");
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getCategoryId().equals(uuid)) {
                categoryList.remove(i);
                return;
            }
        }
    }

    @Override
    public void delete(Category entity) {

    }

    @Override
    public void deleteAll(Iterable<? extends Category> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends Category> S save(S entity) {
        call("save");

        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getCategoryId().equals(entity.getCategoryId())) {
                categoryList.set(i, entity);
                return entity;
            }
        }

        categoryList.add(entity);
        return entity;
    }

    @Override
    public <S extends Category> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Category> findById(UUID id) {
        call("findById");

        for (Category book : categoryList) {
            if (book.getCategoryId().equals(id)) {
                return Optional.of(book);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(UUID id) {
        call("existsById");
        return categoryList.stream().anyMatch(q -> q.getCategoryId().equals(id));
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Category> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<Category> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Category getOne(UUID uuid) {
        return null;
    }

    @Override
    public <S extends Category> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Category> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Category> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Category> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Category> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Category> boolean exists(Example<S> example) {
        return false;
    }
}
