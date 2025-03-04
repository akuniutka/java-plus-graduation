package ru.practicum.ewm.user.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient extends UserOperations {

}
