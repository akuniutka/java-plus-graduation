package ru.practicum.ewm.request.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "request-service", fallback = RequestClientFallback.class)
public interface RequestClient extends RequestOperations {

}
