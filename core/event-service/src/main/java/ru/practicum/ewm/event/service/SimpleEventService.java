package ru.practicum.ewm.event.service;

import com.google.protobuf.Timestamp;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.analyzer.client.AnalyzerClient;
import ru.practicum.ewm.analyzer.message.RecommendedEventProto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.collector.message.ActionTypeProto;
import ru.practicum.ewm.collector.message.UserActionProto;
import ru.practicum.ewm.collector.service.UserActionControllerGrpc;
import ru.practicum.ewm.event.dto.AdminEventFilter;
import ru.practicum.ewm.event.dto.InternalEventFilter;
import ru.practicum.ewm.event.dto.PublicEventFilter;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.FieldValidationException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotPossibleException;
import ru.practicum.ewm.exception.ParameterValidationException;
import ru.practicum.ewm.user.client.UserClient;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SimpleEventService implements EventService {

    private static final Sort DEFAULT_SORT = Sort.by("id");
    private static final Sort SORT_BY_DATE = Sort.by("eventDate");

    private final Clock clock;
    private final UserClient userClient;
    private final AnalyzerClient analyzerClient;
    private final CategoryService categoryService;
    private final EventRepository repository;
    private final Duration adminTimeout;
    private final Duration userTimeout;

    @GrpcClient("collector-service")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    public SimpleEventService(
            final Clock clock,
            final UserClient userClient,
            final AnalyzerClient analyzerClient,
            final CategoryService categoryService,
            final EventRepository repository,
            @Value("${ewm.timeout.admin}") final Duration adminTimeout,
            @Value("${ewm.timeout.user}") final Duration userTimeout
    ) {
        this.clock = clock;
        this.userClient = userClient;
        this.analyzerClient = analyzerClient;
        this.categoryService = categoryService;
        this.repository = repository;
        this.adminTimeout = adminTimeout;
        this.userTimeout = userTimeout;
    }

    @Override
    public Event add(final Event event) {
        validateEventDate(event.getEventDate(), userTimeout);
        requireUserExist(event.getInitiatorId());
        event.setCategory(getCategoryById(event.getCategory().getId()));
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now(clock));
        final Event savedEvent = repository.save(event);
        log.info("Added new event: id = {}, title = {}, eventDate = {}", savedEvent.getId(), savedEvent.getTitle(),
                savedEvent.getEventDate());
        log.debug("Event added = {}", savedEvent);
        return savedEvent;
    }

    @Override
    public List<Event> findAllByInitiatorId(final long initiatorId, final Pageable pageable) {
        return repository.findAllByInitiatorId(initiatorId, pageable);
    }

    @Override
    public List<Event> findAll(final AdminEventFilter filter) {
        requireDatesInRightOrder(filter.rangeStart(), filter.rangeEnd());
        final Optional<BooleanExpression> where = extractPredicate(filter);
        final Pageable pageable = PageRequest.of(filter.from() / filter.size(), filter.size(), DEFAULT_SORT);
        return new ArrayList<>(
                where.map(w -> repository.findAll(w, pageable)).orElse(repository.findAll(pageable)).getContent()
        );
    }

    @Override
    public List<Event> findAll(final InternalEventFilter filter) {
        requireDatesInRightOrder(filter.getRangeStart(), filter.getRangeEnd());
        final Optional<BooleanExpression> where = extractPredicate(filter);

        final List<Event> events = new ArrayList<>();
        if (Boolean.TRUE.equals(filter.getOnlyAvailable())
                || (filter.getSort() != null && filter.getSort() != InternalEventFilter.Sort.EVENT_DATE)
        ) {
            if (where.isEmpty()) {
                return repository.findAll();
            }
            repository.findAll(where.get()).forEach(events::add);
            return events;
        }

        if (filter.getFrom() != null && filter.getSize() != null) {
            final Sort sort = (filter.getSort() == null) ? DEFAULT_SORT : SORT_BY_DATE;
            final Pageable pageable = PageRequest.of(filter.getFrom() / filter.getSize(), filter.getSize(), sort);
            return where.map(w -> repository.findAll(w, pageable)).orElse(repository.findAll(pageable)).getContent();
        }

        if (filter.getSort() != null) {
            if (where.isEmpty()) {
                return repository.findAll(SORT_BY_DATE);
            }
            repository.findAll(where.get(), SORT_BY_DATE).forEach(events::add);
            return events;
        }

        if (where.isEmpty()) {
            return repository.findAll();
        }
        repository.findAll(where.get()).forEach(events::add);
        return events;
    }

    @Override
    public List<Event> findAll(final PublicEventFilter filter) {
        requireDatesInRightOrder(filter.rangeStart(), filter.rangeEnd());
        final BooleanExpression where = extractPredicate(filter).orElseThrow(AssertionError::new);

        final List<Event> events = new ArrayList<>();
        if (Boolean.TRUE.equals(filter.onlyAvailable())
                || (filter.sort() != null && filter.sort() != PublicEventFilter.Sort.EVENT_DATE)
        ) {
            repository.findAll(where).forEach(events::add);
            return events;
        }

        final Sort sort = (filter.sort() == null) ? DEFAULT_SORT : SORT_BY_DATE;
        final Pageable pageable = PageRequest.of(filter.from() / filter.size(), filter.size(), sort);
        return repository.findAll(where, pageable).getContent();
    }

    @Override
    public List<Event> getNewSimilarEvents(final long requesterId, final long sampleEventId, final int maxResults) {
        requireUserExist(requesterId);
        final List<Long> eventIds = analyzerClient.getNewSimilarEvents(requesterId, sampleEventId, maxResults)
                .map(RecommendedEventProto::getEventId)
                .toList();
        final InternalEventFilter filter = InternalEventFilter.builder()
                .events(eventIds)
                .build();
        final Map<Long, Event> events = findAll(filter).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));
        return eventIds.stream()
                .map(events::get)
                .toList();
    }

    @Override
    public List<Event> getRecommendationsForUser(final long userId, final int maxResults) {
        requireUserExist(userId);
        final List<Long> eventIds = analyzerClient.getRecommendationsForUser(userId, maxResults)
                .map(RecommendedEventProto::getEventId)
                .toList();
        final InternalEventFilter filter = InternalEventFilter.builder()
                .events(eventIds)
                .build();
        final Map<Long, Event> events = findAll(filter).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));
        return eventIds.stream()
                .map(events::get)
                .toList();
    }

    @Override
    public Optional<Event> findById(final long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Event> findByIdAndInitiatorId(final long id, final long initiatorId) {
        return repository.findByIdAndInitiatorId(id, initiatorId);
    }

    @Override
    public Event getByIdAndPublished(final long requesterId, final long eventId) {
        requireUserExist(requesterId);
        final Event event = repository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(Event.class, eventId));
        if (requesterId != event.getInitiatorId()) {
            sendEventViewToCollector(requesterId, eventId);
        }
        return event;
    }

    @Override
    public Event getByIdAndInitiatorId(final long id, final long initiatorId) {
        return repository.findByIdAndInitiatorId(id, initiatorId)
                .orElseThrow(() -> new NotFoundException(Event.class, id));
    }

    @Override
    public Event update(final long id, final EventPatch patch) {
        validateEventDate(patch.eventDate(), adminTimeout);
        final Event event = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(Event.class, id));
        if (event.getState() != EventState.PENDING) {
            throw new NotPossibleException("Event must be in state PENDING");
        }
        checkPostUpdateEventDate(patch.eventDate(), event.getEventDate(), adminTimeout);
        return updateInternally(event, patch);
    }

    @Override
    public Event update(final long id, final EventPatch patch, final long userId) {
        validateEventDate(patch.eventDate(), userTimeout);
        final Event event = getByIdAndInitiatorId(id, userId);
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

    private Event updateInternally(final Event event, final EventPatch patch) {
        applyPatch(event, patch);
        if (event.getState() == EventState.PUBLISHED && event.getPublishedOn() == null) {
            event.setPublishedOn(now());
        }
        final Event savedEvent = repository.save(event);
        log.info("Updated event: id = {}, state = {}", savedEvent.getId(), savedEvent.getState());
        log.debug("Updated event = {}", savedEvent);
        return savedEvent;
    }

    private void applyPatch(final Event event, final EventPatch patch) {
        Optional.ofNullable(patch.title()).ifPresent(event::setTitle);
        Optional.ofNullable(patch.category()).map(category -> getCategoryById(category.getId()))
                .ifPresent(event::setCategory);
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
        if (userClient.existsById(userId)) {
            return;
        }
        throw new NotFoundException("User", userId);
    }

    private Category getCategoryById(final long categoryId) {
        return categoryService.getById(categoryId);
    }

    private void requireDatesInRightOrder(final LocalDateTime rangeStart, final LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ParameterValidationException("rangeEnd", "must be after or equal to 'rangeStart", rangeEnd);
        }
    }

    private Optional<BooleanExpression> extractPredicate(final AdminEventFilter filter) {
        final List<BooleanExpression> predicates = new ArrayList<>();
        final QEvent event = new QEvent(("event"));
        Optional.ofNullable(filter.users()).map(event.initiatorId::in).ifPresent(predicates::add);
        Optional.ofNullable(filter.categories()).map(event.category.id::in).ifPresent(predicates::add);
        Optional.ofNullable(filter.states()).map(event.state::in).ifPresent(predicates::add);
        Optional.ofNullable(filter.rangeStart()).map(event.eventDate::goe).ifPresent(predicates::add);
        Optional.ofNullable(filter.rangeEnd()).map(event.eventDate::loe).ifPresent(predicates::add);
        return predicates.stream().reduce(BooleanExpression::and);
    }

    private Optional<BooleanExpression> extractPredicate(final InternalEventFilter filter) {
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
        return predicates.stream().reduce(BooleanExpression::and);
    }

    private Optional<BooleanExpression> extractPredicate(final PublicEventFilter filter) {
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
        return predicates.stream().reduce(BooleanExpression::and);
    }

    private void sendEventViewToCollector(final long requesterId, final long eventId) {
        final Instant now = Instant.now(clock);
        final UserActionProto userActionProto = UserActionProto.newBuilder()
                .setUserId(requesterId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_VIEW)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .build();
        collectorClient.collectUserAction(userActionProto);
        log.info("Sent user action to collector service: userId = {}, eventId = {}, actionType = {}",
                userActionProto.getUserId(), userActionProto.getEventId(), userActionProto.getActionType());
        log.debug("Sent action = {}", userActionProto);
    }
}
