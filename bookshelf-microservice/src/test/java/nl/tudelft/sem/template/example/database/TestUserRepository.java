package nl.tudelft.sem.template.example.database;

import nl.tudelft.sem.template.model.User;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class TestUserRepository implements UserRepository {
    public final List<User> users = new ArrayList<>();
    public final List<String> calledMethods = new ArrayList<>();

    private void call(String name) {
        calledMethods.add(name);
    }

    @Override
    public List<User> findAll() {
        call("findAll");
        return users;
    }

    @Override
    public List<User> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<User> findAllById(Iterable<UUID> uuids) {
        return null;
    }

    @Override
    public long count() {
        return users.size();
    }

    @Override
    public void deleteById(UUID uuid) {
        call("deleteById");
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(uuid)) {
                users.remove(i);
                return;
            }
        }
    }

    @Override
    public void delete(User entity) {

    }

    @Override
    public void deleteAll(Iterable<? extends User> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends User> S save(S entity) {
        call("save");

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(entity.getUserId())) {
                users.set(i, entity);
                return entity;
            }
        }

        users.add(entity);
        return entity;
    }

    @Override
    public <S extends User> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<User> findById(UUID id) {
        call("findById");

        for (User user : users) {
            if (user.getUserId().equals(id)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUserId(UUID userId) {
        call("findByUserId");

        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(UUID id) {
        call("existsById");
        return users.stream().anyMatch(q -> q.getUserId().equals(id));
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends User> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<User> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public User getOne(UUID uuid) {
        return null;
    }

    @Override
    public <S extends User> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends User> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends User> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends User> boolean exists(Example<S> example) {
        return false;
    }
}
