package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.ewm.event.client.EventClient;
import ru.practicum.ewm.event.dto.EventCondensedDto;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.exception.EventNotFoundException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotPossibleException;
import ru.practicum.ewm.request.dto.EventRequestStatusDto;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.dto.RequestStats;
import ru.practicum.ewm.request.dto.UpdateEventRequestStatusDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestState;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.client.UserClient;

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
    private final RequestRepository repository;

    @Override
    public RequestDto create(long userId, long eventId) {
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
        Request newRequest = new Request();
        newRequest.setRequesterId(userId);
        newRequest.setEventId(eventId);
        if (event.requestModeration() && event.participantLimit() != 0) {
            newRequest.setStatus(RequestState.PENDING);
        } else {
            newRequest.setStatus(RequestState.CONFIRMED);
        }
        return RequestMapper.mapToRequestDto(repository.save(newRequest));
    }

    @Override
    public List<RequestDto> getAllRequestByUserId(final long userId) {
        requireUserExists(userId);
        return repository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::mapToRequestDto)
                .toList();
    }

    @Override
    public List<RequestDto> getRequests(final long initiatorId, final long eventId) {
        getEventByIdAndInitiatorId(eventId, initiatorId);
        return RequestMapper.mapToRequestDto(repository.findAllByEventId(eventId));
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
        return new EventRequestStatusDto(RequestMapper.mapToRequestDto(confirmedRequests),
                RequestMapper.mapToRequestDto(rejectedRequests));
    }

    @Override
    @Transactional
    public RequestDto cancel(final long userId, final long requestId) {
        requireUserExists(userId);
        Request request = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(Request.class, requestId));
        if (!request.getRequesterId().equals(userId)) {
            throw new NotPossibleException("Request is not by user");
        }
        request.setStatus(RequestState.CANCELED);
        return RequestMapper.mapToRequestDto(repository.save(request));
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
        log.info("{} set to status {}", savedRequests.size(), status);
        return savedRequests;
    }
}
