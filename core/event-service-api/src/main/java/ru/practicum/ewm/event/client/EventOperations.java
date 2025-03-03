package ru.practicum.ewm.event.client;

import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.InternalEventFilter;

import java.util.List;

public interface EventOperations {

    @GetMapping("/internal/events")
    List<EventShortDto> findAll(@SpringQueryMap InternalEventFilter filter);

    @GetMapping("/internal/events/{id}")
    EventFullDto getById(@PathVariable long id);

    @GetMapping("/internal/users/{userId}/events/{eventId}")
    EventFullDto getByIdAndInitiatorId(@PathVariable long eventId, @PathVariable long userId);
}
