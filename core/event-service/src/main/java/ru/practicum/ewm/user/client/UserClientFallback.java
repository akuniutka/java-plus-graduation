package ru.practicum.ewm.user.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public List<UserShortDto> findAllByIdIn(final Set<Long> ids) {
        log.warn("Cannot retrieve user data from user service: ids = {}", ids);
        return ids.stream()
                .map(this::stubUserDto)
                .toList();
    }

    @Override
    public boolean existsById(final long id) {
        log.warn("Cannot check user existence with user service: id = {}", id);
        return false;
    }

    private UserShortDto stubUserDto(final long id) {
        return UserShortDto.builder()
                .id(id)
                .build();
    }
}
