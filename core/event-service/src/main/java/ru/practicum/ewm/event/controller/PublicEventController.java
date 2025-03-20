package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.PublicEventFilter;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {

    private static final String USER_HEADER = "X-EWM-USER-ID";
    private static final boolean DEFAULT_ONLY_AVAILABLE = false;
    private static final int DEFAULT_PAGE_FROM = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Qualifier("ratingRichEventServiceFacade")
    private final EventService service;

    private final EventMapper eventMapper;

    @GetMapping("/{eventId}")
    public EventFullDto getByIdAndPublished(
            @PathVariable final long eventId,
            @RequestHeader(USER_HEADER) final long userId
    ) {
        log.info("Received request for event: userId = {}, eventId = {}", userId, eventId);
        final Event event = service.getByIdAndPublished(userId, eventId);
        final EventFullDto dto = eventMapper.mapToFullDto(event);
        log.info("Responded with requested event: eventId = {}", dto.id());
        log.debug("Requested event = {}", dto);
        return dto;
    }

    @GetMapping
    public List<EventShortDto> findAll(@Valid final PublicEventFilter filter) {
        log.info("Received request for events: filter = {}", filter);
        final PublicEventFilter filterWithDefaults = withDefaults(filter);
        final List<Event> events = service.findAll(filterWithDefaults);
        final List<EventShortDto> dtos = eventMapper.mapToShortDto(events);
        log.info("Responded with requested events: filter = {}", filter);
        log.debug("Requested events = {}", dtos);
        return dtos;
    }

    @GetMapping("/{eventId}/similar")
    public List<EventShortDto> getNewSimilarEvents(
            @PathVariable("eventId") final long sampleEventId,
            @RequestHeader(USER_HEADER) final long requesterId,
            @RequestParam(defaultValue = "10") final int maxResults
    ) {
        log.info("Received request for new similar events: requesterId = {}, sampleEventId = {}, maxResults = {}",
                requesterId, sampleEventId, maxResults);
        final List<Event> events = service.getNewSimilarEvents(requesterId, sampleEventId, maxResults);
        final List<EventShortDto> dtos = eventMapper.mapToShortDto(events);
        log.info("Responded with requested new similar events: requesterId = {}, sampleEventId = {}, maxResults = {}",
                requesterId, sampleEventId, maxResults);
        log.debug("Similar events = {}", dtos);
        return dtos;
    }

    @GetMapping("/recommendations")
    public List<EventShortDto> getRecommendations(
            @RequestHeader(USER_HEADER) final long userId,
            @RequestParam(defaultValue = "10") final int maxResults
    ) {
        log.info("Received request for recommendations: userId = {}, maxResults = {}", userId, maxResults);
        final List<Event> events = service.getRecommendationsForUser(userId, maxResults);
        final List<EventShortDto> dtos = eventMapper.mapToShortDto(events);
        log.info("Responded with requested recommendations: userId = {}, maxResults = {}", userId, maxResults);
        log.debug("Requested recommendations = {}", dtos);
        return dtos;
    }

    private PublicEventFilter withDefaults(final PublicEventFilter filter) {
        return filter.toBuilder()
                .onlyAvailable(filter.onlyAvailable() == null ? DEFAULT_ONLY_AVAILABLE : filter.onlyAvailable())
                .from(filter.from() == null ? DEFAULT_PAGE_FROM : filter.from())
                .size(filter.size() == null ? DEFAULT_PAGE_SIZE : filter.size())
                .build();
    }
}
