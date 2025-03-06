package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import ru.practicum.ewm.common.HttpRequestResponseLogger;
import ru.practicum.ewm.stats.EndpointHitDto;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController extends HttpRequestResponseLogger {

    private static final String APP = "main-service";
    private static final boolean DEFAULT_ONLY_AVAILABLE = false;
    private static final int DEFAULT_PAGE_FROM = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final EventService viewRichEventServiceFacade;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final Clock clock;

    @GetMapping("/{eventId}")
    public EventFullDto get(
            @PathVariable final long eventId,
            final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest);
        final Event event = viewRichEventServiceFacade.getPublishedById(eventId);
        final EventFullDto dto = eventMapper.mapToFullDto(event);
        statsClient.saveHit(new EndpointHitDto(APP, httpRequest.getRequestURI(), httpRequest.getRemoteAddr(),
                LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS)));
        logHttpResponse(httpRequest, dto);
        return dto;
    }

    @GetMapping
    public List<EventShortDto> findAll(
            @Valid final PublicEventFilter filter,
            final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest);
        final PublicEventFilter filterWithDefaults = withDefaults(filter);
        final List<Event> events = viewRichEventServiceFacade.findAll(filterWithDefaults);
        final List<EventShortDto> dtos = eventMapper.mapToDto(events);
        statsClient.saveHit(new EndpointHitDto(APP, httpRequest.getRequestURI(), httpRequest.getRemoteAddr(),
                LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS)));
        logHttpResponse(httpRequest, dtos);
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
