package nl.tudelft.sem.template.example.database;


import nl.tudelft.sem.template.model.Bookshelf;
import nl.tudelft.sem.template.model.User;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class TestBookshelfRepository implements BookshelfRepository {
    public final List<Bookshelf> bookshelves = new ArrayList<>();
    public final List<String> calledMethods = new ArrayList<>();

    private void call(String name) {
        calledMethods.add(name);
    }

    @Override
    public List<Bookshelf> findAll() {
        call("findAll");
        return bookshelves;
    }


    @Override
    public List<Bookshelf> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Bookshelf> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<Bookshelf> findAllById(Iterable<UUID> uuids) {
        return null;
    }

    @Override
    public long count() {
        return bookshelves.size();
    }

    @Override
    public void deleteById(UUID uuid) {
        call("deleteById");
        for (int i = 0; i < bookshelves.size(); i++) {
            if (bookshelves.get(i).getBookshelfId().equals(uuid)) {
                bookshelves.remove(i);
                return;
            }
        }
    }

    @Override
    public void delete(Bookshelf entity) {

    }

    @Override
    public void deleteAll(Iterable<? extends Bookshelf> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends Bookshelf> S save(S entity) {
        call("save");

        if (entity.getBookshelfId() == null) {
            entity.setBookshelfId(UUID.randomUUID());
        }

        for (int i = 0; i < bookshelves.size(); i++) {
            if (bookshelves.get(i).getBookshelfId().equals(entity.getBookshelfId())) {
                bookshelves.set(i, entity);
                return entity;
            }
        }

        bookshelves.add(entity);
        return entity;
    }

    @Override
    public <S extends Bookshelf> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Bookshelf> findById(UUID id) {
        call("findById");

        for (Bookshelf bookshelf : bookshelves) {
            if (bookshelf.getBookshelfId().equals(id)) {
                return Optional.of(bookshelf);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Bookshelf> findByOwnerId(UUID ownerId) {
        call("findByOwnerId");

        List<Bookshelf> ownerBookshelves = new ArrayList<>();
        for (Bookshelf bookshelf : bookshelves) {
            if (bookshelf.getOwner().getUserId().equals(ownerId)) {
                ownerBookshelves.add(bookshelf);
            }
        }

        return ownerBookshelves;
    }

    @Override
    public boolean existsById(UUID id) {
        call("existsById");
        return bookshelves.stream().anyMatch(q -> q.getBookshelfId().equals(id));
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Bookshelf> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<Bookshelf> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Bookshelf getOne(UUID uuid) {
        return null;
    }

    @Override
    public <S extends Bookshelf> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Bookshelf> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Bookshelf> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Bookshelf> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Bookshelf> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Bookshelf> boolean exists(Example<S> example) {
        return false;
    }
}
