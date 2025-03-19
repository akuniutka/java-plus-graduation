package ru.practicum.ewm.event.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.analyzer.client.AnalyzerClient;
import ru.practicum.ewm.analyzer.message.RecommendedEventProto;
import ru.practicum.ewm.event.dto.AdminEventFilter;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.event.dto.PublicEventFilter;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventPatch;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RatingRichEventServiceFacade implements EventService {

    private static final Comparator<Event> SORT_BY_RATING = Comparator.comparing(Event::getRating).reversed();

    private final EventService service;
    private final AnalyzerClient client;

    public RatingRichEventServiceFacade(
            final EventService requestRichEventServiceFacade,
            final AnalyzerClient analyzerClient
    ) {
        this.service = requestRichEventServiceFacade;
        this.client = analyzerClient;
    }

    @Override
    public Event add(final Event event) {
        final Event event_ = service.add(event);
        fetchRatings(event_);
        return event_;
    }

    @Override
    public List<Event> findAllByInitiatorId(final long initiatorId, final Pageable pageable) {
        final List<Event> events = service.findAllByInitiatorId(initiatorId, pageable);
        fetchRatings(events);
        return events;
    }

    @Override
    public List<Event> findAll(final AdminEventFilter filter) {
        final List<Event> events = service.findAll(filter);
        fetchRatings(events);
        return events;
    }

    @Override
    public List<Event> findAll(final InternalEventFilter filter) {
        final List<Event> events = service.findAll(filter);
        fetchRatings(events);
        if (filter.getSort() != InternalEventFilter.Sort.RATING) {
            return events;
        }

        if (filter.getFrom() == null || filter.getSize() == null) {
            return events.stream()
                    .sorted(SORT_BY_RATING)
                    .toList();
        }

        final int eventsToSkip = (filter.getFrom() / filter.getSize()) * filter.getSize();
        return events.stream()
                .sorted(SORT_BY_RATING)
                .skip(eventsToSkip)
                .limit(filter.getSize())
                .toList();
    }

    @Override
    public List<Event> findAll(final PublicEventFilter filter) {
        final List<Event> events = service.findAll(filter);
        fetchRatings(events);
        if (filter.sort() != PublicEventFilter.Sort.RATING) {
            return events;
        }

        final int eventsToSkip = (filter.from() / filter.size()) * filter.size();
        return events.stream()
                .sorted(SORT_BY_RATING)
                .skip(eventsToSkip)
                .limit(filter.size())
                .toList();
    }

    @Override
    public List<Event> getNewSimilarEvents(final long requesterId, final long sampleEventId, final int maxResults) {
        final List<Event> events = service.getNewSimilarEvents(requesterId, sampleEventId, maxResults);
        fetchRatings(events);
        return events;
    }

    @Override
    public List<Event> getRecommendationsForUser(final long userId, final int maxResults) {
        final List<Event> events = service.getRecommendationsForUser(userId, maxResults);
        fetchRatings(events);
        return events;
    }

    @Override
    public Optional<Event> findById(final long id) {
        return service.findById(id).map(this::fetchRatings);
    }

    @Override
    public Optional<Event> findByIdAndInitiatorId(long id, long initiatorId) {
        return service.findByIdAndInitiatorId(id, initiatorId).map(this::fetchRatings);
    }

    @Override
    public Event getByIdAndPublished(final long requesterId, final long eventId) {
        final Event event = service.getByIdAndPublished(requesterId, eventId);
        fetchRatings(event);
        return event;
    }

    @Override
    public Event getByIdAndInitiatorId(final long id, final long initiatorId) {
        final Event event = service.getByIdAndInitiatorId(id, initiatorId);
        fetchRatings(event);
        return event;
    }

    @Override
    public Event update(final long id, final EventPatch patch) {
        final Event event = service.update(id, patch);
        fetchRatings(event);
        return event;
    }

    @Override
    public Event update(final long id, final EventPatch patch, final long userId) {
        final Event event = service.update(id, patch, userId);
        fetchRatings(event);
        return event;
    }

    private Event fetchRatings(final Event event) {
        fetchRatings(List.of(event));
        return event;
    }

    private void fetchRatings(final Collection<Event> events) {
        final List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        final Map<Long, Float> ratings = client.getInteractionsCount(eventIds)
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
        events.forEach(event -> event.setRating(ratings.getOrDefault(event.getId(), 0.0f)));
    }
}
