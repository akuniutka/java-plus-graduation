package ru.practicum.ewm.event.client;

import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.event.dto.EventCondensedDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.InternalEventFilter;

import java.util.List;

public interface EventOperations {

    @GetMapping("/internal/events")
    List<EventShortDto> findAll(@SpringQueryMap InternalEventFilter filter);

    @PostMapping(value = "/internal/events", params = "id")
    boolean existsById(@RequestParam final long id);

    @PostMapping(value = "/internal/events", params = {"id", "initiatorId"})
    boolean existsByIdAndInitiatorId(@RequestParam final long id, @RequestParam final long initiatorId);

    @GetMapping("/internal/events/{id}")
    EventCondensedDto getById(@PathVariable long id);

    @GetMapping("/internal/users/{userId}/events/{eventId}")
    EventCondensedDto getByIdAndInitiatorId(@PathVariable long eventId, @PathVariable long userId);
}
