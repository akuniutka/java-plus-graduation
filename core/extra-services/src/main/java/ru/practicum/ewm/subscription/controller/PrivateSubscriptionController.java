package ru.practicum.ewm.subscription.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.subscription.dto.EventFilter;
import ru.practicum.ewm.subscription.service.SubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class PrivateSubscriptionController {

    private static final boolean DEFAULT_ONLY_AVAILABLE = false;
    private static final int DEFAULT_PAGE_FROM = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final SubscriptionService service;

    @PostMapping
    public void subscribe(@PathVariable final long userId, @RequestParam final long initiatorId) {
        log.info("Received request to add new subscription: subscriberId = {}, publisherId = {}", userId, initiatorId);
        service.subscribe(userId, initiatorId);
        log.info("Responded with {} to add subscription request: subscriberId = {}, publisherId = {}",
                HttpStatus.OK, userId, initiatorId);
    }

    @GetMapping
    public List<EventShortDto> getEvents(@PathVariable final long userId, @Valid final EventFilter filter) {
        log.info("Received request for events: subscriberId = {}, filter = {}", userId, filter);
        final EventFilter filterWithDefaults = withDefaults(filter);
        final List<EventShortDto> dtos = service.getEvents(userId, filterWithDefaults);
        log.info("Responded with requested events: subscriberId = {}, filter = {}", userId, filter);
        log.debug("Requested events = {}", dtos);
        return dtos;
    }

    @DeleteMapping("/{initiatorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable final long userId, @PathVariable final long initiatorId) {
        log.info("Received request to delete subscription: subscriberId = {}, publisherId = {}", userId, initiatorId);
        service.unsubscribe(userId, initiatorId);
        log.info("Responded with {} to delete subscription request: subscriberId = {}, publisherId = {}",
                HttpStatus.NO_CONTENT, userId, initiatorId);
    }

    private EventFilter withDefaults(final EventFilter filter) {
        return filter.toBuilder()
                .onlyAvailable(filter.onlyAvailable() == null ? DEFAULT_ONLY_AVAILABLE : filter.onlyAvailable())
                .from(filter.from() == null ? DEFAULT_PAGE_FROM : filter.from())
                .size(filter.size() == null ? DEFAULT_PAGE_SIZE : filter.size())
                .build();
    }
}
