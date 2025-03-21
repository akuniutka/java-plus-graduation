package ru.practicum.ewm.request.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.ewm.collector.message.ActionTypeProto;
import ru.practicum.ewm.collector.message.UserActionProto;
import ru.practicum.ewm.collector.service.UserActionControllerGrpc;
import ru.practicum.ewm.event.client.EventClient;
import ru.practicum.ewm.event.dto.EventCondensedDto;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.exception.EventNotFoundException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotPossibleException;
import ru.practicum.ewm.request.dto.EventRequestStatusDto;
import ru.practicum.ewm.request.dto.RequestStats;
import ru.practicum.ewm.request.dto.UpdateEventRequestStatusDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestState;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.client.UserClient;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class RequestServiceImpl implements RequestService {

    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestMapper mapper;
    private final RequestRepository repository;
    private final Clock clock;

    @GrpcClient("collector-service")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    @Override
    public Request add(long userId, long eventId) {
        requireUserExists(userId);
        final EventCondensedDto event = getEventById(eventId);
        if (!repository.findAllByRequesterIdAndEventIdAndStatusNotLike(userId, eventId,
                RequestState.CANCELED).isEmpty()) {
            throw new NotPossibleException("Request already exists");
        }
        if (userId == event.initiatorId()) {
            throw new NotPossibleException("User is initiator of event");
        }
        if (!event.state().equals(EventState.PUBLISHED)) {
            throw new NotPossibleException("Event is not published");
        }
        final long occupiedSlots = repository.findAllByEventIdAndStatus(eventId, RequestState.CONFIRMED).size();
        if (event.participantLimit() != 0 && occupiedSlots >= event.participantLimit()) {
            throw new NotPossibleException("Request limit exceeded");
        }
        Request request = new Request();
        request.setRequesterId(userId);
        request.setEventId(eventId);
        if (event.requestModeration() && event.participantLimit() != 0) {
            request.setStatus(RequestState.PENDING);
        } else {
            request.setStatus(RequestState.CONFIRMED);
        }
        request = repository.save(request);
        log.info("Added new participation request: id = {}, requesterId = {}, eventId = {}", request.getId(),
                request.getRequesterId(), request.getEventId());
        log.debug("Participation request added = {}", request);
        sendUserRegistrationToCollector(userId, eventId);
        return request;
    }

    @Override
    public List<Request> findAllByRequesterId(final long userId) {
        requireUserExists(userId);
        return repository.findAllByRequesterId(userId);
    }

    @Override
    public List<Request> findAllByInitiatorIdAndEventId(final long initiatorId, final long eventId) {
        getEventByIdAndInitiatorId(eventId, initiatorId);
        return repository.findAllByEventId(eventId);
    }

    @Override
    public boolean existsByRequesterIdAndStatusConfirmed(final long requesterId) {
        return repository.existsByRequesterIdAndStatus(requesterId, RequestState.CONFIRMED);
    }

    @Override
    @Transactional
    public EventRequestStatusDto processRequests(
            final long eventId,
            final UpdateEventRequestStatusDto dto,
            final long initiatorId
    ) {
        final EventCondensedDto event = getEventByIdAndInitiatorId(eventId, initiatorId);
        if (CollectionUtils.isEmpty(dto.requestIds())) {
            return new EventRequestStatusDto(List.of(), List.of());
        }
        final List<Request> requests = repository.findAllByEventIdAndIdIn(eventId, dto.requestIds());
        requireAllExist(dto.requestIds(), requests);
        requireAllHavePendingStatus(requests);

        List<Request> confirmedRequests = List.of();
        List<Request> rejectedRequests = List.of();
        if (dto.status() == RequestState.REJECTED) {
            rejectedRequests = setStatusAndSaveAll(requests, RequestState.REJECTED);
        } else {
            final long occupiedSlots = repository.findAllByEventIdAndStatus(eventId, RequestState.CONFIRMED).size();
            final long availableSlots = event.participantLimit() == 0L
                    ? Long.MAX_VALUE
                    : event.participantLimit() - occupiedSlots;
            if (requests.size() > availableSlots) {
                throw new NotPossibleException("Not enough available participation slots");
            }
            confirmedRequests = setStatusAndSaveAll(requests, RequestState.CONFIRMED);
            if (requests.size() == availableSlots) {
                final List<Request> pendingRequests = repository.findAllByEventIdAndStatus(eventId,
                        RequestState.PENDING);
                rejectedRequests = setStatusAndSaveAll(pendingRequests, RequestState.REJECTED);
            }
        }
        return new EventRequestStatusDto(mapper.mapToDto(confirmedRequests),
                mapper.mapToDto(rejectedRequests));
    }

    @Override
    @Transactional
    public Request cancel(final long userId, final long requestId) {
        requireUserExists(userId);
        Request request = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(Request.class, requestId));
        if (request.getRequesterId() != userId) {
            throw new NotPossibleException("Request is not by user");
        }
        request.setStatus(RequestState.CANCELED);
        request = repository.save(request);
        log.info("Cancelled participation request: id = {}, status = {}", request.getId(), request.getStatus());
        log.debug("Cancelled participation request = {}", request);
        return request;
    }

    @Override
    public List<RequestStats> getConfirmedRequestStats(final Set<Long> eventIds) {
        return repository.getRequestStats(eventIds, RequestState.CONFIRMED).stream()
                .map(stats -> new RequestStats(stats.getId(), stats.getRequests()))
                .toList();
    }

    private void requireUserExists(final long userId) {
        if (userClient.existsById(userId)) {
            return;
        }
        throw new NotFoundException("User", userId);
    }

    private EventCondensedDto getEventById(final long eventId) {
        return eventClient.findById(eventId).orElseThrow(
                () -> new EventNotFoundException("Event not found: eventId = %s".formatted(eventId))
        );
    }

    private EventCondensedDto getEventByIdAndInitiatorId(final long eventId, final long initiatorId) {
        return eventClient.findByIdAndInitiatorId(eventId, initiatorId).orElseThrow(
                () -> new EventNotFoundException("Event not found: eventId = %s, initiatorId = %s"
                        .formatted(eventId, initiatorId))
        );
    }

    private void requireAllExist(final List<Long> ids, final List<Request> requests) {
        final Set<Long> idsFound = requests.stream()
                .map(Request::getId)
                .collect(Collectors.toSet());
        final Set<Long> idsMissing = new HashSet<>(ids);
        idsMissing.removeAll(idsFound);
        if (idsMissing.isEmpty()) {
            return;
        }
        throw new NotFoundException(Request.class, idsMissing);
    }

    private void requireAllHavePendingStatus(final List<Request> requests) {
        final Set<Long> idsNotPending = requests.stream()
                .filter(request -> request.getStatus() != RequestState.PENDING)
                .map(Request::getId)
                .collect(Collectors.toSet());
        if (idsNotPending.isEmpty()) {
            return;
        }
        final String idsStr = idsNotPending.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        throw new NotPossibleException("Request(s) %s with wrong status (must be %s)"
                .formatted(idsStr, RequestState.PENDING));
    }

    private List<Request> setStatusAndSaveAll(final List<Request> requests, final RequestState status) {
        if (CollectionUtils.isEmpty(requests)) {
            log.info("No requests to update status to {}", status);
            return List.of();
        }
        requests.forEach(request -> request.setStatus(status));
        final List<Request> savedRequests = repository.saveAll(requests);
        final List<Long> ids = savedRequests.stream()
                .map(Request::getId)
                .toList();
        log.info("Set participation requests to status {}: id = {}", status, ids);
        log.debug("Updated participation requests = {}", savedRequests);
        return savedRequests;
    }

    private void sendUserRegistrationToCollector(final long requesterId, final long eventId) {
        final Instant now = Instant.now(clock);
        final UserActionProto userActionProto = UserActionProto.newBuilder()
                .setUserId(requesterId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_REGISTER)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .build();
        collectorClient.collectUserAction(userActionProto);
        log.info("Sent user action to collector service: userId = {}, eventId = {}, actionType= {}",
                userActionProto.getUserId(), userActionProto.getEventId(), userActionProto.getActionType());
        log.debug("Sent action = {}", userActionProto);
    }
}
