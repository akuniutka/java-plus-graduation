package ru.practicum.ewm.user.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.List;
import java.util.Set;

public interface UserOperations extends SimpleUserCheck {

    @GetMapping("/internal/users")
    List<UserShortDto> findAllByIdIn(@RequestParam Set<Long> ids);
}
