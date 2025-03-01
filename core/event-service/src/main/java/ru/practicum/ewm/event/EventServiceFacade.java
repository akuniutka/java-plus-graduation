package ru.practicum.ewm.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class EventServiceFacade implements EventService {

    private final EventServiceImpl eventService;
    private final UserClient userClient;

    @Override
    public Event add(final Event event) {
        final Event event_ = eventService.add(event);
        fetchUser(event_);
        return event_;
    }

    @Override
    public Event getById(final long id) {
        final Event event_ = eventService.getById(id);
        fetchUser(event_);
        return event_;
    }

    @Override
    public Event getPublishedById(final long id) {
        final Event event_ = eventService.getPublishedById(id);
        fetchUser(event_);
        return event_;
    }

    @Override
    public Event getByIdAndUserId(final long id, final long userId) {
        final Event event_ = eventService.getByIdAndUserId(id, userId);
        fetchUser(event_);
        return event_;
    }

    @Override
    public List<Event> get(final EventFilter filter) {
        final List<Event> events = eventService.get(filter);
        fetchUser(events);
        return events;
    }

    @Override
    public Event update(final long id, final EventPatch patch) {
        final Event event_ = eventService.update(id, patch);
        fetchUser(event_);
        return event_;
    }

    @Override
    public Event update(final long id, final EventPatch patch, final long userId) {
        final Event event_ = eventService.update(id, patch, userId);
        fetchUser(event_);
        return event_;
    }

    private void fetchUser(final Event event) {
        final UserShortDto user = userClient.getById(event.getInitiatorId());
        event.setInitiator(user);
    }

    private void fetchUser(final List<Event> events) {
        final Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());
        final Map<Long, UserShortDto> users = userClient.findAllByIdIn(userIds).stream()
                .collect(Collectors.toMap(UserShortDto::id, Function.identity()));
        events.forEach(event -> event.setInitiator(users.get(event.getInitiatorId())));
    }
}
