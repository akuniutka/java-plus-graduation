package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.PublicEventFilter;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.EndpointHitDto;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
public class PublicEventController {

    private static final boolean DEFAULT_ONLY_AVAILABLE = false;
    private static final int DEFAULT_PAGE_FROM = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final String serviceName;
    private final EventService service;
    private final EventMapper mapper;
    private final StatsClient statsClient;
    private final Clock clock;

    public PublicEventController(
            @Value("spring.application.name") final String serviceName,
            final EventService viewRichEventServiceFacade,
            final EventMapper mapper,
            final StatsClient statsClient,
            final Clock clock
    ) {
        this.serviceName = serviceName;
        this.service = viewRichEventServiceFacade;
        this.mapper = mapper;
        this.statsClient = statsClient;
        this.clock = clock;
    }

    @GetMapping("/{eventId}")
    public EventFullDto getByIdAndPublished(@PathVariable final long eventId, final HttpServletRequest httpRequest) {
        log.info("Received request for event: id = {}", eventId);
        final Event event = service.getByIdAndPublished(eventId);
        final EventFullDto dto = mapper.mapToFullDto(event);
        saveHit(httpRequest.getRequestURI(), httpRequest.getRemoteAddr());
        log.info("Responded with requested event: id = {}", dto.id());
        log.debug("Requested event = {}", dto);
        return dto;
    }

    @GetMapping
    public List<EventShortDto> findAll(@Valid final PublicEventFilter filter, final HttpServletRequest httpRequest) {
        log.info("Received request for events: filter = {}", filter);
        final PublicEventFilter filterWithDefaults = withDefaults(filter);
        final List<Event> events = service.findAll(filterWithDefaults);
        final List<EventShortDto> dtos = mapper.mapToShortDto(events);
        saveHit(httpRequest.getRequestURI(), httpRequest.getRemoteAddr());
        log.info("Responded with requested events: filter = {}", filter);
        log.debug("Requested events = {}", dtos);
        return dtos;
    }

    private void saveHit(final String requestUri, final String remoteAddr) {
        final LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        statsClient.saveHit(new EndpointHitDto(serviceName, requestUri, remoteAddr, now));
    }

    private PublicEventFilter withDefaults(final PublicEventFilter filter) {
        return filter.toBuilder()
                .onlyAvailable(filter.onlyAvailable() == null ? DEFAULT_ONLY_AVAILABLE : filter.onlyAvailable())
                .from(filter.from() == null ? DEFAULT_PAGE_FROM : filter.from())
                .size(filter.size() == null ? DEFAULT_PAGE_SIZE : filter.size())
                .build();
    }
}
