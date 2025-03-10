package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public User save(final User user) {
        final User savedUser = repository.save(user);
        log.info("Added new user: id = {}, name = {}", savedUser.getId(), savedUser.getName());
        log.debug("User added = {}", savedUser);
        return savedUser;
    }

    @Override
    public List<User> findAll(final Pageable pageable) {
        return repository.findAll(pageable).getContent();
    }

    @Override
    public List<User> findAllByIdIn(final Set<Long> ids) {
        return repository.findAllByIdIn(ids);
    }

    @Override
    public List<User> findAllByIdIn(final List<Long> ids, final Pageable pageable) {
        return repository.findAllByIdIn(ids, pageable);
    }

    @Override
    public boolean existsById(final long id) {
        return repository.existsById(id);
    }

    @Override
    public void delete(final long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(User.class, id);
        }
        repository.deleteById(id);
        log.info("Deleted user: id = {}", id);
    }
}
