package ru.practicum.ewm.request.mapper;

import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.model.Request;

import java.util.List;

public class RequestMapper {

    private RequestMapper() {
    }

    public static RequestDto mapToRequestDto(Request request) {
        return RequestDto.builder()
                .requester(request.getRequesterId())
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEventId())
                .status(request.getStatus())
                .build();
    }

    public static List<RequestDto> mapToRequestDto(final List<Request> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(RequestMapper::mapToRequestDto)
                .toList();
    }
}
