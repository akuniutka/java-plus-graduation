package ru.practicum.ewm.user.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;

import java.util.List;
import java.util.Set;

public interface UserService {

    List<User> findAllByIdIn(Set<Long> ids);

    boolean existsById(long id);

    User getById(long id);

    List<UserDto> findAll(Pageable pageable);

    List<UserDto> findByIds(List<Long> ids, Pageable pageable);

    UserDto save(NewUserRequest requestDto);

    void delete(long id);
}
