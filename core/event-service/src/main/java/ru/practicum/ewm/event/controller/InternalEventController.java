package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.client.EventOperations;
import ru.practicum.ewm.event.dto.EventCondensedDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InternalEventController implements EventOperations {

    @Qualifier("ratingRichEventServiceFacade")
    private final EventService enrichingService;

    @Qualifier("simpleEventService")
    private final EventService simpleService;

    private final EventMapper eventMapper;

    @Override
    public List<EventShortDto> findAll(final InternalEventFilter filter) {
        log.info("Received request for events: filter = {}", filter);
        final List<Event> events = enrichingService.findAll(filter);
        final List<EventShortDto> dtos = eventMapper.mapToShortDto(events);
        log.info("Responded with requested events: filter = {}", filter);
        log.debug("Requested events = {}", dtos);
        return dtos;
    }

    @Override
    public Optional<EventCondensedDto> findById(final long id) {
        log.info("Received request for event: id = {}", id);
        final Optional<EventCondensedDto> dto = simpleService.findById(id)
                .map(eventMapper::mapToCondensedDto);
        log.info("Responded with requested event: id = {}", id);
        log.debug("Requested event = {}", dto);
        return dto;
    }

    @Override
    public Optional<EventCondensedDto> findByIdAndInitiatorId(final long eventId, final long userId) {
        log.info("Received request for event: id = {}, initiatorId = {}", eventId, userId);
        final Optional<EventCondensedDto> dto = simpleService.findByIdAndInitiatorId(eventId, userId)
                .map(eventMapper::mapToCondensedDto);
        log.info("Responded with requested event: id = {}, initiatorId = {}", eventId, userId);
        log.debug("Requested initiator's event = {}", dto);
        return dto;
    }
}
