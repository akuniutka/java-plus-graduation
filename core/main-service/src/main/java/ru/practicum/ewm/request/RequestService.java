package ru.practicum.ewm.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface RequestService {

    RequestDto create(long userId, long eventId);

    List<RequestDto> getAllRequestByUserId(long userId);

    List<RequestDto> getRequests(long userId, long eventId);

    EventRequestStatusDto processRequests(long id, @NotNull @Valid UpdateEventRequestStatusDto dto, long userId);

    RequestDto cancel(long userId, long requestId);
}
