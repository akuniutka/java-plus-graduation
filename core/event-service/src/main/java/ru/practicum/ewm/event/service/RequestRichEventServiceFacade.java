package ru.practicum.ewm.event.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.dto.AdminEventFilter;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.event.dto.PublicEventFilter;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.request.client.RequestClient;
import ru.practicum.ewm.request.dto.RequestStats;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RequestRichEventServiceFacade implements EventService {

    private static final Comparator<Event> DEFAULT_SORT = Comparator.comparing(Event::getId);
    private static final Comparator<Event> SORT_BY_DATE = Comparator.comparing(Event::getEventDate);

    private final EventService service;
    private final RequestClient client;

    public RequestRichEventServiceFacade(
            final EventService userRichEventServiceFacade,
            final RequestClient requestClient
    ) {
        this.service = userRichEventServiceFacade;
        this.client = requestClient;
    }

    @Override
    public Event add(final Event event) {
        final Event event_ = service.add(event);
        fetchConfirmedRequests(event_);
        return event_;
    }

    @Override
    public List<Event> findAllByInitiatorId(final long initiatorId, final Pageable pageable) {
        final List<Event> events = service.findAllByInitiatorId(initiatorId, pageable);
        fetchConfirmedRequests(events);
        return events;
    }

    @Override
    public List<Event> findAll(final AdminEventFilter filter) {
        final List<Event> events = service.findAll(filter);
        fetchConfirmedRequests(events);
        return events;
    }

    @Override
    public List<Event> findAll(final InternalEventFilter filter) {
        List<Event> events = service.findAll(filter);
        fetchConfirmedRequests(events);
        if (!Boolean.TRUE.equals(filter.getOnlyAvailable())) {
            return events;
        }

        events = events.stream()
                .filter(this::hadAvailableSlots)
                .toList();
        if (filter.getSort() != null && filter.getSort() != InternalEventFilter.Sort.EVENT_DATE) {
            return events;
        }

        if (filter.getFrom() != null && filter.getSize() != null) {
            final Comparator<Event> sort = (filter.getSort() == null) ? DEFAULT_SORT : SORT_BY_DATE;
            final int eventsToSkip = (filter.getFrom() / filter.getSize()) * filter.getSize();
            return events.stream()
                    .sorted(sort)
                    .skip(eventsToSkip)
                    .limit(filter.getSize())
                    .toList();
        }

        if (filter.getSort() != null) {
            return events.stream()
                    .sorted(SORT_BY_DATE)
                    .toList();
        }

        return events;
    }

    @Override
    public List<Event> findAll(final PublicEventFilter filter) {
        List<Event> events = service.findAll(filter);
        fetchConfirmedRequests(events);
        if (!Boolean.TRUE.equals(filter.onlyAvailable())) {
            return events;
        }

        events = events.stream()
                .filter(this::hadAvailableSlots)
                .toList();
        if (filter.sort() != null && filter.sort() != PublicEventFilter.Sort.EVENT_DATE) {
            return events;
        }

        final Comparator<Event> sort = (filter.sort() == null) ? DEFAULT_SORT : SORT_BY_DATE;
        final int eventsToSkip = (filter.from() / filter.size()) * filter.size();
        return events.stream()
                .sorted(sort)
                .skip(eventsToSkip)
                .limit(filter.size())
                .toList();
    }

    @Override
    public Optional<Event> findById(final long id) {
        return service.findById(id).map(this::fetchConfirmedRequests);
    }

    @Override
    public Optional<Event> findByIdAndInitiatorId(long id, long initiatorId) {
        return service.findByIdAndInitiatorId(id, initiatorId).map(this::fetchConfirmedRequests);
    }

    @Override
    public Event getByIdAndPublished(final long requesterId, final long eventId) {
        final Event event = service.getByIdAndPublished(requesterId, eventId);
        fetchConfirmedRequests(event);
        return event;
    }

    @Override
    public Event getByIdAndInitiatorId(final long id, final long initiatorId) {
        final Event event = service.getByIdAndInitiatorId(id, initiatorId);
        fetchConfirmedRequests(event);
        return event;
    }

    @Override
    public Event update(final long id, final EventPatch patch) {
        final Event event = service.update(id, patch);
        fetchConfirmedRequests(event);
        return event;
    }

    @Override
    public Event update(final long id, final EventPatch patch, final long userId) {
        final Event event = service.update(id, patch, userId);
        fetchConfirmedRequests(event);
        return event;
    }

    private Event fetchConfirmedRequests(final Event event) {
        fetchConfirmedRequests(List.of(event));
        return event;
    }

    private void fetchConfirmedRequests(final Collection<Event> events) {
        final Set<Long> ids = events.stream()
                .filter(event -> event.getState() == EventState.PUBLISHED)
                .map(Event::getId)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        final Map<Long, Long> confirmedRequests = client.getConfirmedRequestStats(ids).stream()
                .collect(Collectors.toMap(RequestStats::eventId, RequestStats::requestsCount));
        events.forEach(event -> event.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L)));
    }

    private boolean hadAvailableSlots(final Event event) {
        return event.getState() == EventState.PUBLISHED && (!event.isRequestModeration()
                || event.getParticipantLimit() == 0L || event.getParticipantLimit() > event.getConfirmedRequests());
    }
}
