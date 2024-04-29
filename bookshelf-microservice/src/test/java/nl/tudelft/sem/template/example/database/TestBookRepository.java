package nl.tudelft.sem.template.example.database;

import nl.tudelft.sem.template.model.Book;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TestBookRepository implements BookRepository {
    public final List<Book> catalog = new ArrayList<>();
    public final List<String> calledMethods = new ArrayList<>();

    private void call(String name) {
        calledMethods.add(name);
    }

    @Override
    public List<Book> findAll() {
        call("findAll");
        return catalog;
    }


    @Override
    public List<Book> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Book> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<Book> findAllById(Iterable<UUID> uuids) {
        return null;
    }

    @Override
    public long count() {
        return catalog.size();
    }

    @Override
    public void deleteById(UUID uuid) {
        call("deleteById");
        for (int i = 0; i < catalog.size(); i++) {
            if (catalog.get(i).getBookId().equals(uuid)) {
                catalog.remove(i);
                return;
            }
        }
    }

    @Override
    public void delete(Book entity) {

    }

    @Override
    public void deleteAll(Iterable<? extends Book> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends Book> S save(S entity) {
        call("save");

        for (int i = 0; i < catalog.size(); i++) {
            if (catalog.get(i).getBookId().equals(entity.getBookId())) {
                catalog.set(i, entity);
                return entity;
            }
        }

        catalog.add(entity);
        return entity;
    }

    @Override
    public <S extends Book> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Book> findById(UUID id) {
        call("findById");

        for (Book book : catalog) {
            if (book.getBookId().equals(id)) {
                return Optional.of(book);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(UUID id) {
        call("existsById");
        return catalog.stream().anyMatch(q -> q.getBookId().equals(id));
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Book> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<Book> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Book getOne(UUID uuid) {
        return null;
    }

    @Override
    public <S extends Book> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Book> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Book> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Book> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Book> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Book> boolean exists(Example<S> example) {
        return false;
    }
}
