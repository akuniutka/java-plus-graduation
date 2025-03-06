package ru.practicum.ewm.event.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.event.dto.AdminEventFilter;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.event.dto.PublicEventFilter;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.event.model.Event;

import java.util.List;

public interface EventService {

    Event add(@NotNull Event event);

    List<Event> findAllByInitiatorId(long initiatorId, Pageable pageable);

    List<Event> findAll(@NotNull AdminEventFilter filter);

    List<Event> findAll(@NotNull InternalEventFilter filter);

    List<Event> findAll(@NotNull PublicEventFilter filter);

    Event getById(long id);

    Event getPublishedById(long id);

    Event getByIdAndInitiatorId(long id, long initiatorId);

    Event update(long id, @NotNull EventPatch patch);

    Event update(long id, @NotNull EventPatch patch, long userId);
}
