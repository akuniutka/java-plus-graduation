package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.client.EventOperations;
import ru.practicum.ewm.event.dto.EventCondensedDto;
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

    private final EventService viewRichEventServiceFacade;
    private final EventService simpleEventService;
    private final EventMapper eventMapper;

    @Override
    public List<EventShortDto> findAll(final InternalEventFilter filter) {
        log.info("Received request for events: filter = {}", filter);
        final List<Event> events = viewRichEventServiceFacade.findAll(filter);
        final List<EventShortDto> dtos = eventMapper.mapToShortDto(events);
        log.info("Responded with requested events: filter = {}", filter);
        log.debug("Requested events = {}", dtos);
        return dtos;
    }

    @Override
    public boolean existsById(final long id) {
        log.info("Received request to check event existence: id = {}", id);
        final boolean exists = simpleEventService.existsById(id);
        log.info("Responded to event existence check: id = {}, exists = {}", id, exists);
        return exists;
    }

    @Override
    public boolean existsByIdAndInitiatorId(final long id, final long initiatorId) {
        log.info("Received request to check event existence: id = {}, initiatorId = {}", id, initiatorId);
        final boolean exists = simpleEventService.existsByIdAndInitiatorId(id, initiatorId);
        log.info("Responded to event existence check: id = {}, initiatorId = {}, exists = {}", id, initiatorId, exists);
        return exists;
    }

    @Override
    public EventCondensedDto getById(@PathVariable final long id) {
        log.info("Received request for event: id = {}", id);
        final Event event = simpleEventService.getById(id);
        final EventCondensedDto dto = eventMapper.mapToCondensedDto(event);
        log.info("Responded with requested event: id = {}", id);
        log.debug("Requested event = {}", dto);
        return dto;
    }

    @Override
    public EventCondensedDto getByIdAndInitiatorId(@PathVariable final long eventId, @PathVariable final long userId) {
        log.info("Received request for event: id = {}, initiatorId = {}", eventId, userId);
        final Event event = simpleEventService.getByIdAndInitiatorId(eventId, userId);
        final EventCondensedDto dto = eventMapper.mapToCondensedDto(event);
        log.info("Responded with requested event: id = {}, initiatorId = {}", eventId, userId);
        log.debug("Requested initiator's event = {}", dto);
        return dto;
    }
}
