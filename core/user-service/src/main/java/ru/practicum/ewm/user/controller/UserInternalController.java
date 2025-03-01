package ru.practicum.ewm.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.client.UserOperations;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserInternalController implements UserOperations {

    private final UserService service;
    private final UserMapper mapper;

    @Override
    public List<UserShortDto> findAllByIdIn(@RequestParam final Set<Long> ids) {
        log.info("Received request for users: id = {}", ids);
        final List<User> users = service.findAllByIdIn(ids);
        final List<UserShortDto> dtos = mapper.mapToShortDto(users);
        log.info("Responded with requested users: id = {}", ids);
        log.debug("Requested users = {}", dtos);
        return dtos;
    }

    @Override
    public UserShortDto getById(@PathVariable final long id) {
        log.info("Received request for user: id = {}", id);
        final User user = service.getById(id);
        final UserShortDto dto = mapper.mapToShortDto(user);
        log.info("Responded with requested user: id = {}", id);
        log.debug("Requested user = {}", dto);
        return dto;
    }
}
