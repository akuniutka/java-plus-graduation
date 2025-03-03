package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.client.EventOperations;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InternalEventController implements EventOperations {

    private final EventService service;
    private final EventMapper mapper;

    @Override
    public List<EventShortDto> findAll(final InternalEventFilter filter) {
        log.info("Received request for events: filter = {}", filter);
        final List<Event> events = service.findAll(filter);
        final List<EventShortDto> dtos = mapper.mapToDto(events);
        log.info("Responded with requested events: filter = {}", filter);
        log.debug("Requested events = {}", dtos);
        return dtos;
    }

    @Override
    public EventFullDto getById(@PathVariable final long id) {
        log.info("Received request for event: id = {}", id);
        final Event event = service.getById(id);
        final EventFullDto dto = mapper.mapToFullDto(event);
        log.info("Responded with requested event: id = {}", id);
        log.debug("Requested event = {}", dto);
        return dto;
    }

    @Override
    public EventFullDto getByIdAndInitiatorId(@PathVariable final long eventId, @PathVariable final long userId) {
        log.info("Received request for event: id = {}, initiatorId = {}", eventId, userId);
        final Event event = service.getByIdAndUserId(eventId, userId);
        final EventFullDto dto = mapper.mapToFullDto(event);
        log.info("Responded with requested event: id = {}, initiatorId = {}", eventId, userId);
        log.debug("Requested initiator's event = {}", dto);
        return dto;
    }
}
