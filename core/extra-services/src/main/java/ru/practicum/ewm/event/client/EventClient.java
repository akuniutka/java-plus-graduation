package ru.practicum.ewm.event.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service", fallback = EventClientFallback.class)
public interface EventClient extends EventOperations {

}
