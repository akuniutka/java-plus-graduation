package ru.practicum.ewm.event.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.event.dto.AdminEventFilter;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.event.dto.PublicEventFilter;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.Set;

public interface EventService {

    Event add(@NotNull @Valid Event event);

    List<Event> findAllByIdIn(Set<Long> ids);

    List<Event> findAllByInitiatorId(long initiatorId, Pageable pageable);

    List<Event> findAllByInitiatorIdIn(Set<Long> initiatorIds);

    Event getById(long id);

    Event getPublishedById(long id);

    Event getByIdAndUserId(long id, long userId);

    List<Event> findAll(@NotNull AdminEventFilter filter);

    List<Event> findAll(@NotNull PublicEventFilter filter);

    List<Event> findAll(@NotNull InternalEventFilter filter);

    Event update(long id, @NotNull @Valid EventPatch patch);

    Event update(long id, @NotNull @Valid EventPatch patch, long userId);
}
