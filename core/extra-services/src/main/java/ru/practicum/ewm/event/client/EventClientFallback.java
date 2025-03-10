package ru.practicum.ewm.event.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.dto.EventCondensedDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.InternalEventFilter;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class EventClientFallback implements EventClient {

    @Override
    public List<EventShortDto> findAll(final InternalEventFilter filter) {
        log.warn("Cannot retrieve event data from event service: filter = {}", filter);
        return List.of();
    }

    @Override
    public Optional<EventCondensedDto> findById(final long id) {
        log.warn("Cannot retrieve event data from event service: id = {}", id);
        return Optional.empty();
    }

    @Override
    public Optional<EventCondensedDto> findByIdAndInitiatorId(final long eventId, final long userId) {
        log.warn("Cannot retrieve event data from event service: id = {}, initiatorId = {}", eventId, userId);
        return Optional.empty();
    }
}
