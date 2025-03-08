package ru.practicum.ewm.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserAdminController {

    private final UserService service;
    private final UserMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto add(@RequestBody @Valid final NewUserRequest requestDto) {
        log.info("Received request to add new user: name = {}", requestDto.name());
        log.debug("New user = {}", requestDto);
        User user = mapper.mapToUser(requestDto);
        user = service.save(user);
        final UserDto responseDto = mapper.mapToDto(user);
        log.info("Responded with user added: id = {}, name = {}", responseDto.id(), responseDto.name());
        log.debug("User added = {}", responseDto);
        return responseDto;
    }

    @GetMapping
    public Collection<UserDto> findAllByIdIn(
            @RequestParam(required = false) final List<Long> ids,
            @RequestParam(defaultValue = "0") @PositiveOrZero final int from,
            @RequestParam(defaultValue = "10") @Positive final int size
    ) {
        log.info("Received request for users: id = {}, from = {}, size = {}", ids, from, size);
        final Pageable pageable = PageRequest.of(from / size, size);
        final List<User> users;
        if (CollectionUtils.isEmpty(ids)) {
            users = service.findAll(pageable);
        } else {
            users = service.findAllByIdIn(ids, pageable);
        }
        final Collection<UserDto> dtos = mapper.mapToDto(users);
        log.info("Responded with users requested: id = {}, from = {}, size = {}", ids, from, size);
        log.debug("Users requested = {}", dtos);
        return dtos;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final long id) {
        log.info("Received request to delete user: id = {}", id);
        service.delete(id);
        log.info("Responded with {} to user delete request: id = {}", HttpStatus.NO_CONTENT, id);
    }
}
