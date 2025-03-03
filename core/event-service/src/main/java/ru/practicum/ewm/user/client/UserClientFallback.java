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
        return ids.stream()
                .map(this::getById)
                .toList();
    }

    @Override
    public UserShortDto getById(final long id) {
        return UserShortDto.builder()
                .id(id)
                .build();
    }
}
