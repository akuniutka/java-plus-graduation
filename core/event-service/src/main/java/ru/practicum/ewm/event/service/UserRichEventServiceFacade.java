package ru.practicum.ewm.event.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.dto.AdminEventFilter;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.event.dto.PublicEventFilter;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.user.client.UserClient;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserRichEventServiceFacade implements EventService {

    private final EventService service;
    private final UserClient client;

    public UserRichEventServiceFacade(
            final EventService simpleEventService,
            final UserClient userClient
    ) {
        this.service = simpleEventService;
        this.client = userClient;
    }

    @Override
    public Event add(final Event event) {
        final Event event_ = service.add(event);
        fetchUser(event_);
        return event_;
    }

    @Override
    public List<Event> findAllByInitiatorId(final long initiatorId, final Pageable pageable) {
        final List<Event> events = service.findAllByInitiatorId(initiatorId, pageable);
        fetchUser(events);
        return events;
    }

    @Override
    public List<Event> findAll(final AdminEventFilter filter) {
        final List<Event> events = service.findAll(filter);
        fetchUser(events);
        return events;
    }

    @Override
    public List<Event> findAll(final InternalEventFilter filter) {
        final List<Event> events = service.findAll(filter);
        fetchUser(events);
        return events;
    }

    @Override
    public List<Event> findAll(final PublicEventFilter filter) {
        final List<Event> events = service.findAll(filter);
        fetchUser(events);
        return events;
    }

    @Override
    public Event getById(final long id) {
        final Event event = service.getById(id);
        fetchUser(event);
        return event;
    }

    @Override
    public Event getPublishedById(final long id) {
        final Event event = service.getPublishedById(id);
        fetchUser(event);
        return event;
    }

    @Override
    public Event getByIdAndInitiatorId(final long id, final long initiatorId) {
        final Event event = service.getByIdAndInitiatorId(id, initiatorId);
        fetchUser(event);
        return event;
    }

    @Override
    public Event update(final long id, final EventPatch patch) {
        final Event event = service.update(id, patch);
        fetchUser(event);
        return event;
    }

    @Override
    public Event update(final long id, final EventPatch patch, final long userId) {
        final Event event = service.update(id, patch, userId);
        fetchUser(event);
        return event;
    }

    private void fetchUser(final Event event) {
        fetchUser(List.of(event));
    }

    private void fetchUser(final Collection<Event> events) {
        final Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());
        final Map<Long, UserShortDto> users = client.findAllByIdIn(userIds).stream()
                .collect(Collectors.toMap(UserShortDto::id, Function.identity()));
        events.forEach(event -> event.setInitiator(users.get(event.getInitiatorId())));
    }
}
