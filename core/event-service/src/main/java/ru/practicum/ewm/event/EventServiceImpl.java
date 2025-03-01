package ru.practicum.ewm.event;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryService;
import ru.practicum.ewm.client.RequestClient;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.exception.FieldValidationException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotPossibleException;
import ru.practicum.ewm.exception.ParameterValidationException;
import ru.practicum.ewm.stats.ViewStatsDto;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
class EventServiceImpl implements EventService {

    private static final LocalDateTime VIEWS_FROM = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0);
    private static final LocalDateTime VIEWS_TO = LocalDateTime.of(2100, Month.DECEMBER, 31, 23, 59, 59);

    private final Clock clock;
    private final UserClient userClient;
    private final CategoryService categoryService;
    private final StatsClient statsClient;
    private final RequestClient requestClient;
    private final EventRepository repository;
    private final Duration adminTimeout;
    private final Duration userTimeout;

    EventServiceImpl(
            final Clock clock,
            final UserClient userClient,
            final CategoryService categoryService,
            final StatsClient statsClient,
            final RequestClient requestClient,
            final EventRepository repository,
            @Value("${ewm.timeout.admin}") final Duration adminTimeout,
            @Value("${ewm.timeout.user}") final Duration userTimeout
    ) {
        this.clock = clock;
        this.userClient = userClient;
        this.categoryService = categoryService;
        this.statsClient = statsClient;
        this.requestClient = requestClient;
        this.repository = repository;
        this.adminTimeout = adminTimeout;
        this.userTimeout = userTimeout;
    }

    @Override
    @Transactional
    public Event add(final Event event) {
        validateEventDate(event.getEventDate(), userTimeout);
        requireUserExist(event.getInitiatorId());
        event.setCategory(fetchCategory(event.getCategory()));
        final Event savedEvent = repository.save(event);
        log.info("Added event with id = {}: {}", savedEvent.getId(), savedEvent);
        return savedEvent;
    }

    @Override
    public Event getById(final long id) {
        return repository.findById(id)
                .map(this::fetchConfirmedRequestsAndHits)
                .orElseThrow(() -> new NotFoundException(Event.class, id));
    }

    @Override
    public Event getPublishedById(long id) {
        return repository.findByIdAndState(id, EventState.PUBLISHED)
                .map(this::fetchConfirmedRequestsAndHits)
                .orElseThrow(() -> new NotFoundException(Event.class, id));
    }

    @Override
    public Event getByIdAndUserId(final long id, final long userId) {
        return repository.findByIdAndInitiatorId(id, userId)
                .map(this::fetchConfirmedRequestsAndHits)
                .orElseThrow(() -> new NotFoundException(Event.class, id));
    }

    @Override
    public List<Event> get(final EventFilter filter) {
        if (filter.rangeStart() != null && filter.rangeEnd() != null
                && filter.rangeEnd().isBefore(filter.rangeStart())) {
            throw new ParameterValidationException("rangeEnd", "must be after or equal to 'rangeStart'",
                    filter.rangeEnd());
        }
        final List<BooleanExpression> predicates = new ArrayList<>();
        final QEvent event = new QEvent("event");
        Optional.ofNullable(filter.text()).ifPresent(text -> predicates.add(event.annotation.likeIgnoreCase(text)
                .or(event.description.likeIgnoreCase(text))));
        Optional.ofNullable(filter.users()).ifPresent(users -> predicates.add(event.initiatorId.in(users)));
        Optional.ofNullable(filter.categories()).ifPresent(categories ->
                predicates.add(event.category.id.in(categories)));
        Optional.ofNullable(filter.states()).ifPresent(states -> predicates.add(event.state.in(states)));
        Optional.ofNullable(filter.paid()).ifPresent(paid -> predicates.add(event.paid.eq(paid)));
        Optional.ofNullable(filter.rangeStart()).ifPresent(start -> predicates.add(event.eventDate.goe(start)));
        Optional.ofNullable(filter.rangeEnd()).ifPresent(end -> predicates.add(event.eventDate.loe(end)));
        final Optional<BooleanExpression> where = predicates.stream().reduce(BooleanExpression::and);

        // TODO: refactor pagination, sorting, filtering by slots available when event stores views and requests data
        final Pageable page = PageRequest.of(filter.from() / filter.size(), filter.size());
        final List<Event> events = new ArrayList<>(where.map(w -> repository.findAll(w, page))
                .orElseGet(() -> repository.findAll(page))
                .getContent());
        fetchConfirmedRequestsAndHits(events);

        if (Boolean.TRUE.equals(filter.onlyAvailable())) {
            events.removeIf(foundEvent -> foundEvent.getParticipantLimit() > 0L
                    && foundEvent.getParticipantLimit() - foundEvent.getConfirmedRequests() <= 0L);
        }

        if (filter.sort() == EventSort.EVENT_DATE) {
            events.sort(Comparator.comparing(Event::getEventDate));
        } else if (filter.sort() == EventSort.VIEWS) {
            events.sort(Comparator.comparing(Event::getViews).reversed());
        }

        return events;
    }

    @Override
    @Transactional
    public Event update(final long id, final EventPatch patch) {
        validateEventDate(patch.eventDate(), adminTimeout);
        final Event event = getById(id);
        if (event.getState() != EventState.PENDING) {
            throw new NotPossibleException("Event must be in state PENDING");
        }
        checkPostUpdateEventDate(patch.eventDate(), event.getEventDate(), adminTimeout);
        return updateInternally(event, patch);
    }

    @Override
    @Transactional
    public Event update(final long id, final EventPatch patch, final long userId) {
        validateEventDate(patch.eventDate(), userTimeout);
        final Event event = getByIdAndUserId(id, userId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new NotPossibleException("Only pending or canceled events can be changed");
        }
        checkPostUpdateEventDate(patch.eventDate(), event.getEventDate(), userTimeout);
        return updateInternally(event, patch);
    }

    private void validateEventDate(final LocalDateTime eventDate, final Duration timeLimit) {
        if (isFreezeTime(eventDate, timeLimit)) {
            throw new FieldValidationException("eventDate",
                    "must be not earlier than in %s from now".formatted(timeLimit), eventDate);
        }
    }

    private void checkPostUpdateEventDate(final LocalDateTime newEventDate, final LocalDateTime oldEventDate,
            final Duration timeout) {
        if (newEventDate == null && isFreezeTime(oldEventDate, timeout)) {
            throw new NotPossibleException("Event date must be not earlier than in %s from now".formatted(timeout));
        }
    }

    private boolean isFreezeTime(final LocalDateTime dateTime, final Duration timeLimit) {
        return dateTime != null && !Duration.between(now(), dateTime.minus(timeLimit)).isPositive();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
    }

    private void fetchConfirmedRequestsAndHits(final List<Event> events) {
        final List<Long> ids = events.stream()
                .map(Event::getId)
                .toList();
        final List<String> uris = ids.stream()
                .map(id -> "/events/" + id)
                .toList();
        final Map<Long, Long> confirmedRequests = requestClient.getConfirmedRequests(ids).stream()
                .collect(Collectors.toMap(EventRequestStats::getId, EventRequestStats::getRequests));
        final Map<String, Long> views = statsClient.getStats(VIEWS_FROM, VIEWS_TO, uris, true).stream()
                .collect(Collectors.toMap(ViewStatsDto::uri, ViewStatsDto::hits));
        events.forEach(event -> event.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L)));
        events.forEach(event -> event.setViews(views.getOrDefault("/events/" + event.getId(), 0L)));
    }

    private Event fetchConfirmedRequestsAndHits(final Event event) {
        fetchConfirmedRequestsAndHits(List.of(event));
        return event;
    }

    private Event updateInternally(final Event event, final EventPatch patch) {
        applyPatch(event, patch);
        if (event.getState() == EventState.PUBLISHED && event.getPublishedOn() == null) {
            event.setPublishedOn(now());
        }
        final Event savedEvent = repository.save(event);
        log.info("updated event with id = {}: {}", savedEvent.getId(), savedEvent);
        return savedEvent;
    }

    private void applyPatch(final Event event, final EventPatch patch) {
        Optional.ofNullable(patch.title()).ifPresent(event::setTitle);
        Optional.ofNullable(patch.category()).map(this::fetchCategory).ifPresent(event::setCategory);
        Optional.ofNullable(patch.eventDate()).ifPresent(event::setEventDate);
        Optional.ofNullable(patch.location()).ifPresent(event::setLocation);
        Optional.ofNullable(patch.annotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(patch.description()).ifPresent(event::setDescription);
        Optional.ofNullable(patch.participantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(patch.paid()).ifPresent(event::setPaid);
        Optional.ofNullable(patch.requestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(patch.state()).ifPresent(event::setState);
    }

    private void requireUserExist(final long userId) {
        final UserShortDto user = userClient.getById(userId);
        if (user.name() == null) {
            throw new NotFoundException("User", userId);
        }
    }

    private Category fetchCategory(final Category category) {
        if (category == null || category.getId() == null) {
            throw new AssertionError();
        }
        return categoryService.getById(category.getId());
    }
}
