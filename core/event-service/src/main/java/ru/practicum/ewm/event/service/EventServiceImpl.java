package ru.practicum.ewm.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.event.dto.AdminEventFilter;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.event.dto.PublicEventFilter;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.client.RequestClient;
import ru.practicum.ewm.request.dto.RequestStats;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.user.client.UserClient;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {

    private static final Sort DEFAULT_SORT = Sort.by("id");
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

    public EventServiceImpl(
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
    public List<Event> findAllByIdIn(final Set<Long> ids) {
        final List<Event> events = repository.findAllByIdIn(ids);
        fetchConfirmedRequestsAndHits(events);
        return events;
    }

    @Override
    public List<Event> findAllByInitiatorId(final long initiatorId, final Pageable pageable) {
        final List<Event> events = repository.findAllByInitiatorId(initiatorId, pageable);
        fetchConfirmedRequestsAndHits(events);
        return events;
    }

    @Override
    public List<Event> findAllByInitiatorIdIn(final Set<Long> initiatorIds) {
        final List<Event> events = repository.findAllByInitiatorIdIn(initiatorIds);
        fetchConfirmedRequestsAndHits(events);
        return events;
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
    public List<Event> findAll(final AdminEventFilter filter) {
        if (filter.rangeStart() != null && filter.rangeEnd() != null
                && filter.rangeEnd().isBefore(filter.rangeStart())) {
            throw new ParameterValidationException("rangeEnd", "must be after or equal to 'rangeStart'",
                    filter.rangeEnd());
        }
        final List<BooleanExpression> predicates = new ArrayList<>();
        final QEvent event = new QEvent(("event"));
        Optional.ofNullable(filter.users()).map(event.initiatorId::in).ifPresent(predicates::add);
        Optional.ofNullable(filter.categories()).map(event.category.id::in).ifPresent(predicates::add);
        Optional.ofNullable(filter.states()).map(event.state::in).ifPresent(predicates::add);
        Optional.ofNullable(filter.rangeStart()).map(event.eventDate::goe).ifPresent(predicates::add);
        Optional.ofNullable(filter.rangeEnd()).map(event.eventDate::loe).ifPresent(predicates::add);
        final Optional<BooleanExpression> where = predicates.stream()
                .reduce(BooleanExpression::and);
        final Pageable pageable = PageRequest.of(filter.from() / filter.size(), filter.size(), DEFAULT_SORT);
        final List<Event> events = new ArrayList<>(
                where.map(w -> repository.findAll(w, pageable)).orElse(repository.findAll(pageable)).getContent()
        );
        fetchConfirmedRequestsAndHits(events);
        return events;
    }

    @Override
    public List<Event> findAll(final PublicEventFilter filter) {
        if (filter.rangeStart() != null && filter.rangeEnd() != null
                && filter.rangeEnd().isBefore(filter.rangeStart())) {
            throw new ParameterValidationException("rangeEnd", "must be after or equal to 'rangeStart'",
                    filter.rangeEnd());
        }
        final List<BooleanExpression> predicates = new ArrayList<>();
        final QEvent event = new QEvent("event");
        predicates.add(event.state.eq(EventState.PUBLISHED));
        Optional.ofNullable(filter.text())
                .map(text -> event.description.likeIgnoreCase(text).or(event.annotation.likeIgnoreCase(text)))
                .ifPresent(predicates::add);
        Optional.ofNullable(filter.categories()).map(event.category.id::in).ifPresent(predicates::add);
        Optional.ofNullable(filter.paid()).map(event.paid::eq).ifPresent(predicates::add);
        Optional.ofNullable(filter.rangeStart()).map(event.eventDate::goe).ifPresent(predicates::add);
        Optional.ofNullable(filter.rangeEnd()).map(event.eventDate::loe).ifPresent(predicates::add);
        final BooleanExpression where = predicates.stream()
                .reduce(BooleanExpression::and)
                .orElseThrow();

        // TODO: refactor pagination, sorting, filtering by slots available when event stores views and requests data
        final Pageable pageable = PageRequest.of(filter.from() / filter.size(), filter.size(), DEFAULT_SORT);
        final List<Event> events = new ArrayList<>(repository.findAll(where, pageable).getContent());
        fetchConfirmedRequestsAndHits(events);

        if (filter.onlyAvailable()) {
            events.removeIf(foundEvent -> foundEvent.getParticipantLimit() > 0L
            && foundEvent.getParticipantLimit() - foundEvent.getConfirmedRequests() > 0L);
        }

        if (filter.sort() == PublicEventFilter.Sort.EVENT_DATE) {
            events.sort(Comparator.comparing(Event::getEventDate));
        } else if (filter.sort() == PublicEventFilter.Sort.VIEWS) {
            events.sort(Comparator.comparing(Event::getViews).reversed());
        }

        return events;
    }

    @Override
    public List<Event> findAll(final InternalEventFilter filter) {
        if (filter.getRangeStart() != null && filter.getRangeEnd() != null
                && filter.getRangeEnd().isBefore(filter.getRangeStart())) {
            throw new ParameterValidationException("rangeEnd", "must be after or equal to 'rangeStart'",
                    filter.getRangeEnd());
        }
        final List<BooleanExpression> predicates = new ArrayList<>();
        final QEvent event = new QEvent("event");
        Optional.ofNullable(filter.getEvents()).map(event.id::in).ifPresent(predicates::add);
        Optional.ofNullable(filter.getUsers()).map(event.initiatorId::in).ifPresent(predicates::add);
        Optional.ofNullable(filter.getCategories()).map(event.category.id::in).ifPresent(predicates::add);
        Optional.ofNullable(filter.getText())
                .map(text -> event.description.likeIgnoreCase(text).or(event.annotation.likeIgnoreCase(text)))
                .ifPresent(predicates::add);
        Optional.ofNullable(filter.getPaid()).map(event.paid::eq).ifPresent(predicates::add);
        Optional.ofNullable(filter.getRangeStart()).map(event.eventDate::goe).ifPresent(predicates::add);
        Optional.ofNullable(filter.getRangeEnd()).map(event.eventDate::loe).ifPresent(predicates::add);
        if (Boolean.TRUE.equals(filter.getOnlyPublished())) {
            predicates.add(event.state.eq(EventState.PUBLISHED));
        }
        final Optional<BooleanExpression> where = predicates.stream()
                .reduce(BooleanExpression::and);

        // TODO: refactor pagination, sorting, filtering by slots available when event stores views and requests data
        final Sort sort = DEFAULT_SORT;
        final List<Event> events;
        if (filter.getFrom() != null && filter.getSize() != null) {
            final Pageable pageable = PageRequest.of(filter.getFrom() / filter.getSize(), filter.getSize(), sort);
            events = new ArrayList<>(
                    where.map(w -> repository.findAll(w, pageable)).orElse(repository.findAll(pageable)).getContent()
            );
        } else {
            events = new ArrayList<>();
            where.map(w -> repository.findAll(w, sort)).orElse(repository.findAll(sort)).forEach(events::add);
        }
        fetchConfirmedRequestsAndHits(events);

        if (Boolean.TRUE.equals(filter.getOnlyAvailable())) {
            events.removeIf(foundEvent -> foundEvent.getParticipantLimit() > 0L
                    && foundEvent.getParticipantLimit() - foundEvent.getConfirmedRequests() <= 0L);
        }

        if (filter.getSort() == InternalEventFilter.Sort.EVENT_DATE) {
            events.sort(Comparator.comparing(Event::getEventDate));
        } else if (filter.getSort() == InternalEventFilter.Sort.VIEWS) {
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
        final Set<Long> ids = events.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());
        final List<String> uris = ids.stream()
                .map(id -> "/events/" + id)
                .toList();
        final Map<Long, Long> confirmedRequests = requestClient.getConfirmedRequestStats(ids).stream()
                .collect(Collectors.toMap(RequestStats::eventId, RequestStats::requestsCount));
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
