package ru.practicum.ewm.request.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ewm-main-service")
public interface RequestClient extends RequestOperations {

}
