package ru.practicum.ewm.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.event.dto.AdminEventFilter;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.event.dto.PublicEventFilter;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventService {

    Event add(Event event);

    List<Event> findAllByInitiatorId(long initiatorId, Pageable pageable);

    List<Event> findAll(AdminEventFilter filter);

    List<Event> findAll(InternalEventFilter filter);

    List<Event> findAll(PublicEventFilter filter);

    List<Event> getRecommendationsForUser(long userId, int maxResults);

    Optional<Event> findById(long id);

    Optional<Event> findByIdAndInitiatorId(long id, long initiatorId);

    Event getByIdAndPublished(long requesterId, long eventId);

    Event getByIdAndInitiatorId(long id, long initiatorId);

    Event update(long id, EventPatch patch);

    Event update(long id, EventPatch patch, long userId);
}
