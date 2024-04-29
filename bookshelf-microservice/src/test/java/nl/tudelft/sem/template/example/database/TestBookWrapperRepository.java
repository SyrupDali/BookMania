package nl.tudelft.sem.template.example.database;

import nl.tudelft.sem.template.example.entities.BookWrapperId;
import nl.tudelft.sem.template.model.BookWrapper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestBookWrapperRepository implements BookWrapperRepository {
    public final List<BookWrapper> catalog = new ArrayList<>();
    public final List<String> calledMethods = new ArrayList<>();

    private void call(String name) {
        calledMethods.add(name);
    }

    @Override
    public List<BookWrapper> findAll() {
        call("findAll");
        return catalog;
    }


    @Override
    public List<BookWrapper> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<BookWrapper> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<BookWrapper> findAllById(Iterable<BookWrapperId> ids) {
        return null;
    }

    @Override
    public long count() {
        return catalog.size();
    }

    @Override
    public void deleteById(BookWrapperId id) {
        call("deleteById");
        for (int i = 0; i < catalog.size(); i++) {
            if (catalog.get(i).getBookId().equals(id.getBookId())) {
                catalog.remove(i);
                return;
            }
        }
    }

    @Override
    public void delete(BookWrapper entity) {

    }

    @Override
    public void deleteAll(Iterable<? extends BookWrapper> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends BookWrapper> S save(S entity) {
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
    public <S extends BookWrapper> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<BookWrapper> findById(BookWrapperId id) {
        call("findById");

        for (BookWrapper book : catalog) {
            if (book.getBookId().equals(id.getBookId())
                    && book.getUserId().equals(id.getUserId())) {
                return Optional.of(book);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(BookWrapperId id) {
        call("existsById");
        return catalog.stream().anyMatch(q ->
                q.getBookId().equals(id.getBookId()) && q.getUserId().equals(id.getUserId()));
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends BookWrapper> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<BookWrapper> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public BookWrapper getOne(BookWrapperId id) {
        return null;
    }

    @Override
    public <S extends BookWrapper> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends BookWrapper> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends BookWrapper> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends BookWrapper> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends BookWrapper> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends BookWrapper> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public List<BookWrapper> findByUserId(UUID userId) {
        return catalog.stream().filter(x -> x.getUserId().equals(userId)).collect(Collectors.toList());
    }
}
