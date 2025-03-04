package ru.practicum.ewm.request.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.practicum.ewm.request.dto.EventRequestStatusDto;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.dto.RequestStats;
import ru.practicum.ewm.request.dto.UpdateEventRequestStatusDto;

import java.util.List;
import java.util.Set;

public interface RequestService {

    RequestDto create(long userId, long eventId);

    List<RequestDto> getAllRequestByUserId(long userId);

    List<RequestDto> getRequests(long userId, long eventId);

    EventRequestStatusDto processRequests(long id, @NotNull @Valid UpdateEventRequestStatusDto dto, long userId);

    RequestDto cancel(long userId, long requestId);

    List<RequestStats> getConfirmedRequestStats(Set<Long> eventIds);
}
