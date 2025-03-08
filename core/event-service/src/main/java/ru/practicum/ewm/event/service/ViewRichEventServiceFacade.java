package ru.practicum.ewm.event.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.dto.AdminEventFilter;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.event.dto.PublicEventFilter;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.stats.ViewStatsDto;
import ru.practicum.ewm.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ViewRichEventServiceFacade implements EventService {

    private static final Comparator<Event> SORT_BY_VIEWS = Comparator.comparing(Event::getViews);
    private static final LocalDateTime VIEWS_FROM = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0);
    private static final LocalDateTime VIEWS_TO = LocalDateTime.of(2100, Month.DECEMBER, 31, 23, 59, 59);

    private final EventService service;
    private final StatsClient client;

    public ViewRichEventServiceFacade(
        final EventService requestRichEventServiceFacade,
        final StatsClient statsClient
    ) {
        this.service = requestRichEventServiceFacade;
        this.client = statsClient;
    }

    @Override
    public Event add(final Event event) {
        final Event event_ = service.add(event);
        fetchViews(event_);
        return event_;
    }

    @Override
    public List<Event> findAllByInitiatorId(final long initiatorId, final Pageable pageable) {
        final List<Event> events = service.findAllByInitiatorId(initiatorId, pageable);
        fetchViews(events);
        return events;
    }

    @Override
    public List<Event> findAll(final AdminEventFilter filter) {
        final List<Event> events = service.findAll(filter);
        fetchViews(events);
        return events;
    }

    @Override
    public List<Event> findAll(final InternalEventFilter filter) {
        final List<Event> events = service.findAll(filter);
        fetchViews(events);
        if (filter.getSort() != InternalEventFilter.Sort.VIEWS) {
            return events;
        }

        if (filter.getFrom() == null || filter.getSize() == null) {
            return events.stream()
                    .sorted(SORT_BY_VIEWS)
                    .toList();
        }

        final int eventsToSkip = (filter.getFrom() / filter.getSize()) * filter.getSize();
        return events.stream()
                .sorted(SORT_BY_VIEWS)
                .skip(eventsToSkip)
                .limit(filter.getSize())
                .toList();
    }

    @Override
    public List<Event> findAll(final PublicEventFilter filter) {
        final List<Event> events = service.findAll(filter);
        fetchViews(events);
        if (filter.sort() != PublicEventFilter.Sort.VIEWS) {
            return events;
        }

        final int eventsToSkip = (filter.from() / filter.size()) * filter.size();
        return events.stream()
                .sorted(SORT_BY_VIEWS)
                .skip(eventsToSkip)
                .limit(filter.size())
                .toList();
    }

    @Override
    public Optional<Event> findById(final long id) {
        return service.findById(id).map(this::fetchViews);
    }

    @Override
    public Optional<Event> findByIdAndInitiatorId(long id, long initiatorId) {
        return service.findByIdAndInitiatorId(id, initiatorId).map(this::fetchViews);
    }

    @Override
    public Event getByIdAndPublished(final long id) {
        final Event event = service.getByIdAndPublished(id);
        fetchViews(event);
        return event;
    }

    @Override
    public Event getByIdAndInitiatorId(final long id, final long initiatorId) {
        final Event event = service.getByIdAndInitiatorId(id, initiatorId);
        fetchViews(event);
        return event;
    }

    @Override
    public Event update(final long id, final EventPatch patch) {
        final Event event = service.update(id, patch);
        fetchViews(event);
        return event;
    }

    @Override
    public Event update(final long id, final EventPatch patch, final long userId) {
        final Event event = service.update(id, patch, userId);
        fetchViews(event);
        return event;
    }

    private Event fetchViews(final Event event) {
        fetchViews(List.of(event));
        return event;
    }

    private void fetchViews(final Collection<Event> events) {
        final List<String> uris = events.stream()
                .filter(event -> event.getState() == EventState.PUBLISHED)
                .map(Event::getId)
                .map("/events/%s"::formatted)
                .toList();
        final Map<String, Long> views = client.getStats(VIEWS_FROM, VIEWS_TO, uris, true).stream()
                .collect(Collectors.toMap(ViewStatsDto::uri, ViewStatsDto::hits));
        events.forEach(event -> event.setViews(views.getOrDefault("/events/" + event.getId(), 0L)));
    }
}
