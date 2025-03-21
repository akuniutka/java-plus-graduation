package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {

    private static final long DEFAULT_PARTICIPANT_LIMIT = 0L;
    private static final boolean DEFAULT_PAID = false;
    private static final boolean DEFAULT_REQUEST_MODERATION = true;
    private static final Sort DEFAULT_SORT = Sort.by("id");

    @Qualifier("ratingRichEventServiceFacade")
    private final EventService service;

    private final EventMapper eventMapper;
    private final EventDtoValidatorExtension validatorExtension;

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.addValidators(validatorExtension);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto add(@PathVariable final long userId, @RequestBody @Valid final NewEventDto dtoIn) {
        log.info("Received request to add new event: title = {}, eventDate = {}", dtoIn.title(), dtoIn.eventDate());
        log.debug("New event = {}", dtoIn);
        final NewEventDto dtoInWithDefaults = withDefaults(dtoIn);
        Event event = eventMapper.mapToEvent(userId, dtoInWithDefaults);
        event = service.add(event);
        final EventFullDto dtoOut = eventMapper.mapToFullDto(event);
        log.info("Responded with event added: id = {}, title = {}, eventDate = {}", dtoOut.id(), dtoOut.title(),
                dtoOut.eventDate());
        log.debug("Event added = {}", dtoOut);
        return dtoOut;
    }

    @GetMapping
    public List<EventShortDto> findAllByInitiatorId(
            @PathVariable final long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero final int from,
            @RequestParam(defaultValue = "10") @Positive final int size
    ) {
        log.info("Received request for events: initiatorId = {}, from = {}, size = {}", userId, from, size);
        final Pageable pageable = PageRequest.of(from / size, size, DEFAULT_SORT);
        final List<Event> events = service.findAllByInitiatorId(userId, pageable);
        final List<EventShortDto> dtos = eventMapper.mapToShortDto(events);
        log.info("Responded with requested events: initiatorId = {}, from = {}, size = {}", userId, from, size);
        log.debug("Requested events = {}", dtos);
        return dtos;
    }

    @GetMapping("/{eventId}")
    public EventFullDto getByIdAndInitiatorId(@PathVariable final long userId, @PathVariable final long eventId) {
        log.info("Received request for event: id = {}, initiatorId = {}", eventId, userId);
        final Event event = service.getByIdAndInitiatorId(eventId, userId);
        final EventFullDto dto = eventMapper.mapToFullDto(event);
        log.info("Responded with requested event: id = {}, initiatorId = {}", dto.id(), dto.initiator().id());
        log.debug("Requested event = {}", dto);
        return dto;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(
            @PathVariable final long userId,
            @PathVariable final long eventId,
            @RequestBody @Valid final UpdateEventUserRequest dtoIn
    ) {
        log.info("Received request to update event: event id = {}, user id = {}, state action = {}", eventId, userId,
                dtoIn.stateAction());
        log.debug("Event patch = {}", dtoIn);
        final EventPatch patch = eventMapper.mapToPatch(dtoIn);
        final Event event = service.update(eventId, patch, userId);
        final EventFullDto dtoOut = eventMapper.mapToFullDto(event);
        log.info("Responded with updated event: id = {}, stat = {}", dtoOut.id(), dtoOut.state());
        log.debug("Updated event = {}", dtoOut);
        return dtoOut;
    }

    private NewEventDto withDefaults(final NewEventDto dto) {
        return dto.toBuilder()
                .participantLimit(dto.participantLimit() == null ? DEFAULT_PARTICIPANT_LIMIT : dto.participantLimit())
                .paid(dto.paid() == null ? DEFAULT_PAID : dto.paid())
                .requestModeration(dto.requestModeration() == null
                        ? DEFAULT_REQUEST_MODERATION
                        : dto.requestModeration())
                .build();
    }
}
