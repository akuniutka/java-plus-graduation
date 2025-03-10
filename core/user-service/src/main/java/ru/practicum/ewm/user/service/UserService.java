package ru.practicum.ewm.user.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.user.model.User;

import java.util.List;
import java.util.Set;

public interface UserService {

    User save(User user);

    List<User> findAll(Pageable pageable);

    List<User> findAllByIdIn(Set<Long> ids);

    List<User> findAllByIdIn(List<Long> ids, Pageable pageable);

    boolean existsById(long id);

    void delete(long id);
}
