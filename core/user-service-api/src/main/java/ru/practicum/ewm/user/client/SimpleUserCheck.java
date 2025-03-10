package ru.practicum.ewm.user.client;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface SimpleUserCheck {

    @PostMapping("/internal/users/exist")
    boolean existsById(@RequestParam long id);
}
