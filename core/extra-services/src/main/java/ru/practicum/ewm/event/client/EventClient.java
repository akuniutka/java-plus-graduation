package ru.practicum.ewm.event.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service")
public interface EventClient extends EventOperations {

}
