package ru.practicum.ewm.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.event.dto.EventCondensedDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventPatch;
import ru.practicum.ewm.event.model.EventState;

import java.util.List;

@Mapper(uses = {CategoryMapper.class, LocationMapper.class})
public interface EventMapper {

    Event mapToEvent(long initiatorId, NewEventDto dto);

    @Mapping(target = "state", source = "stateAction")
    EventPatch mapToPatch(UpdateEventAdminRequest dto);

    @Mapping(target = "state", source = "stateAction")
    EventPatch mapToPatch(UpdateEventUserRequest dto);

    EventFullDto mapToFullDto(Event event);

    List<EventFullDto> mapToFullDto(List<Event> event);

    EventShortDto mapToShortDto(Event event);

    List<EventShortDto> mapToShortDto(List<Event> events);

    EventCondensedDto mapToCondensedDto(Event event);

    @ValueMappings({
            @ValueMapping(target = "PUBLISHED", source = "PUBLISH_EVENT"),
            @ValueMapping(target = "CANCELED", source = "REJECT_EVENT")
    })
    EventState mapAdminAction(UpdateEventAdminRequest.AdminAction adminAction);

    @ValueMappings({
            @ValueMapping(target = "PENDING", source = "SEND_TO_REVIEW"),
            @ValueMapping(target = "CANCELED", source = "CANCEL_REVIEW")
    })
    EventState mapUserAction(UpdateEventUserRequest.UserAction userAction);
}
