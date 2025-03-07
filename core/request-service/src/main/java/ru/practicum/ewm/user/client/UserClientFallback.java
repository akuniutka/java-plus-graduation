package ru.practicum.ewm.user.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public boolean existsById(final long id) {
        log.warn("Cannot check user existence with user service: id = {}", id);
        return false;
    }
}
