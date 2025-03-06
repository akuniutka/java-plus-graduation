package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.common.HttpRequestResponseLogger;
import ru.practicum.ewm.event.dto.AdminEventFilter;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController extends HttpRequestResponseLogger {

    private static final int DEFAULT_PAGE_FROM = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final EventService viewRichEventServiceFacade;
    private final EventMapper eventMapper;
    private final EventDtoValidatorExtension eventDtoValidatorExtension;

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.addValidators(eventDtoValidatorExtension);
    }

    @GetMapping
    public List<EventFullDto> findAll(
            @Valid final AdminEventFilter filter,
            final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest);
        final AdminEventFilter filterWithDefaults = withDefaults(filter);
        final List<Event> events = viewRichEventServiceFacade.findAll(filterWithDefaults);
        final List<EventFullDto> dtos = eventMapper.mapToFullDto(events);
        logHttpResponse(httpRequest, dtos);
        return dtos;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(
            @PathVariable final long eventId,
            @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest,
            final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest, updateEventAdminRequest);
        final EventPatch patch = eventMapper.mapToPatch(updateEventAdminRequest);
        final Event event = viewRichEventServiceFacade.update(eventId, patch);
        final EventFullDto dto = eventMapper.mapToFullDto(event);
        logHttpResponse(httpRequest, dto);
        return dto;
    }

    private AdminEventFilter withDefaults(final AdminEventFilter filter) {
        return filter.toBuilder()
                .from(filter.from() == null ? DEFAULT_PAGE_FROM : filter.from())
                .size(filter.size() == null ? DEFAULT_PAGE_SIZE : filter.size())
                .build();
    }
}
