package ru.practicum.ewm.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventService;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotPossibleException;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventService eventService;
    private final UserClient userClient;

    @Override
    public RequestDto create(long userId, long eventId) {
        if (!requestRepository.findAllByRequesterIdAndEventIdAndStatusNotLike(userId, eventId,
                RequestState.CANCELED).isEmpty()) {
            throw new NotPossibleException("Request already exists");
        }
        requireUserExists(userId);
        Event event = eventService.getById(eventId);
        if (userId == event.getInitiatorId()) {
            throw new NotPossibleException("User is Initiator of event");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotPossibleException("Event is not published");
        }
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new NotPossibleException("Request limit exceeded");
        }
        Request newRequest = new Request();
        newRequest.setRequesterId(userId);
        newRequest.setEvent(event);
        if (event.isRequestModeration() && event.getParticipantLimit() != 0) {
            newRequest.setStatus(RequestState.PENDING);
        } else {
            newRequest.setStatus(RequestState.CONFIRMED);
        }
        return RequestMapper.mapToRequestDto(requestRepository.save(newRequest));
    }

    @Override
    public List<RequestDto> getAllRequestByUserId(final long userId) {
        requireUserExists(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::mapToRequestDto)
                .toList();
    }

    @Override
    public List<RequestDto> getRequests(final long userId, final long eventId) {
        eventService.getByIdAndUserId(eventId, userId);
        return RequestMapper.mapToRequestDto(requestRepository.findAllByEventIdAndEventInitiatorId(eventId, userId));
    }

    @Override
    @Transactional
    public EventRequestStatusDto processRequests(final long id, final UpdateEventRequestStatusDto dto,
            final long userId) {
        final Event event = eventService.getByIdAndUserId(id, userId);
        if (CollectionUtils.isEmpty(dto.requestIds())) {
            return new EventRequestStatusDto(List.of(), List.of());
        }
        final List<Request> requests = requestRepository.findAllByEventIdAndEventInitiatorIdAndIdIn(id, userId,
                dto.requestIds());
        requireAllExist(dto.requestIds(), requests);
        requireAllHavePendingStatus(requests);

        List<Request> confirmedRequests = List.of();
        List<Request> rejectedRequests = List.of();
        if (dto.status() == RequestState.REJECTED) {
            rejectedRequests = setStatusAndSaveAll(requests, RequestState.REJECTED);
        } else {
            final long availableSlots = event.getParticipantLimit() == 0
                    ? Long.MAX_VALUE
                    : event.getParticipantLimit() - event.getConfirmedRequests();
            if (requests.size() > availableSlots) {
                throw new NotPossibleException("Not enough available participation slots");
            }
            confirmedRequests = setStatusAndSaveAll(requests, RequestState.CONFIRMED);
            if (requests.size() == availableSlots) {
                final List<Request> pendingRequests = requestRepository.findAllByEventIdAndEventInitiatorIdAndStatus(id,
                        userId, RequestState.PENDING);
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
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(Request.class, requestId));
        if (!request.getRequesterId().equals(userId)) {
            throw new NotPossibleException("Request is not by user");
        }
        request.setStatus(RequestState.CANCELED);
        return RequestMapper.mapToRequestDto(requestRepository.save(request));
    }

    private void requireUserExists(final long userId) {
        final UserShortDto user = userClient.getById(userId);
        if (user.name() == null) {
            throw new NotFoundException("User", userId);
        }
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
        final List<Request> savedRequests = requestRepository.saveAll(requests);
        log.info("{} set to status {}", savedRequests.size(), status);
        return savedRequests;
    }
}
