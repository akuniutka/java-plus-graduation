package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.EventRequestStatusDto;
import ru.practicum.ewm.request.dto.RequestStats;
import ru.practicum.ewm.request.dto.UpdateEventRequestStatusDto;
import ru.practicum.ewm.request.model.Request;

import java.util.List;
import java.util.Set;

public interface RequestService {

    Request add(long userId, long eventId);

    List<Request> findAllByRequesterId(long userId);

    List<Request> findAllByInitiatorIdAndEventId(long initiatorId, long eventId);

    boolean existsByRequesterIdAndStatusConfirmed(long requesterId);

    EventRequestStatusDto processRequests(long id, UpdateEventRequestStatusDto dto, long userId);

    Request cancel(long userId, long requestId);

    List<RequestStats> getConfirmedRequestStats(Set<Long> eventIds);
}
