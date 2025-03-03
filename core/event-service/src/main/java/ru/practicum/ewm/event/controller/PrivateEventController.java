package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.common.HttpRequestResponseLogger;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController extends HttpRequestResponseLogger {

    private static final Sort DEFAULT_SORT = Sort.by("id");

    private final EventService events;
    private final EventMapper mapper;
    private final EventDtoValidatorExtension eventDtoValidatorExtension;

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.addValidators(eventDtoValidatorExtension);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto add(
            @PathVariable final long userId,
            @RequestBody @Valid final NewEventDto newEventDto,
            final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest, newEventDto);
        final Event event = mapper.mapToEvent(userId, newEventDto);
        final EventFullDto dto = mapper.mapToFullDto(events.add(event));
        logHttpResponse(httpRequest, dto);
        return dto;
    }

    @GetMapping("/{eventId}")
    public EventFullDto get(
            @PathVariable final long userId,
            @PathVariable final long eventId,
            final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest);
        final EventFullDto dto = mapper.mapToFullDto(events.getByIdAndUserId(eventId, userId));
        logHttpResponse(httpRequest, dto);
        return dto;
    }

    @GetMapping
    public List<EventShortDto> get(
            @PathVariable final long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero final int from,
            @RequestParam(defaultValue = "10") @Positive final int size,
            final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest);
        final Pageable pageable = PageRequest.of(from / size, size, DEFAULT_SORT);
        final List<EventShortDto> dtos = mapper.mapToDto(events.findAllByInitiatorId(userId, pageable));
        logHttpResponse(httpRequest, dtos);
        return dtos;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(
            @PathVariable final long userId,
            @PathVariable final long eventId,
            @RequestBody @Valid final UpdateEventUserRequest updateEventUserRequest,
            final HttpServletRequest httpRequest) {
        logHttpRequest(httpRequest, updateEventUserRequest);
        final EventPatch patch = mapper.mapToPatch(updateEventUserRequest);
        final EventFullDto dto = mapper.mapToFullDto(events.update(eventId, patch, userId));
        logHttpResponse(httpRequest, dto);
        return dto;
    }
}
