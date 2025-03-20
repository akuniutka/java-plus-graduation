package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
@Slf4j
public class AdminEventController {

    private static final int DEFAULT_PAGE_FROM = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Qualifier("ratingRichEventServiceFacade")
    private final EventService service;

    private final EventMapper eventMapper;
    private final EventDtoValidatorExtension validatorExtension;

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.addValidators(validatorExtension);
    }

    @GetMapping
    public List<EventFullDto> findAll(@Valid final AdminEventFilter filter) {
        log.info("Received request for events: filter = {}", filter);
        final AdminEventFilter filterWithDefaults = withDefaults(filter);
        final List<Event> events = service.findAll(filterWithDefaults);
        final List<EventFullDto> dtos = eventMapper.mapToFullDto(events);
        log.info("Responded with requested events: filter = {}", filter);
        log.debug("Requested events = {}", dtos);
        return dtos;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable final long eventId, @RequestBody @Valid UpdateEventAdminRequest dtoIn) {
        log.info("Received request to update event: id = {}, state action = {}", eventId, dtoIn.stateAction());
        log.debug("Event patch = {}", dtoIn);
        final EventPatch patch = eventMapper.mapToPatch(dtoIn);
        final Event event = service.update(eventId, patch);
        final EventFullDto dtoOut = eventMapper.mapToFullDto(event);
        log.info("Responded with updated event: id = {}, state = {}", dtoOut.id(), dtoOut.state());
        log.debug("Updated event = {}", dtoOut);
        return dtoOut;
    }

    private AdminEventFilter withDefaults(final AdminEventFilter filter) {
        return filter.toBuilder()
                .from(filter.from() == null ? DEFAULT_PAGE_FROM : filter.from())
                .size(filter.size() == null ? DEFAULT_PAGE_SIZE : filter.size())
                .build();
    }
}
