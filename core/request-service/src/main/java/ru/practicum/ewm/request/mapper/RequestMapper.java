package ru.practicum.ewm.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.model.Request;

import java.util.List;

@Mapper
public interface RequestMapper {

    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "event", source = "eventId")
    RequestDto mapToDto(Request request);

    List<RequestDto> mapToDto(List<Request> requests);
}
