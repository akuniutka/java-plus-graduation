package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public List<User> findAllByIdIn(final Set<Long> ids) {
        return repository.findAllByIdIn(ids);
    }

    @Override
    public User getById(final long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(User.class, id));
    }

    @Override
    public List<UserDto> findAll(final Pageable pageable) {
        final List<User> users = repository.findAll(pageable).getContent();
        return mapper.mapToDto(users);
    }

    @Override
    public List<UserDto> findByIds(final List<Long> ids, final Pageable pageable) {
        final List<User> users = repository.findByIdIn(ids, pageable).getContent();
        return mapper.mapToDto(users);
    }

    @Transactional
    @Override
    public UserDto save(final NewUserRequest requestDto) {
        final User user = mapper.mapToUser(requestDto);
        return mapper.mapToDto(repository.save(user));
    }

    @Transactional
    @Override
    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(User.class, id);
        }
        repository.deleteById(id);
    }
}
