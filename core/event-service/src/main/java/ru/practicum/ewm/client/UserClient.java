package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.user.client.UserOperations;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient extends UserOperations {

}
