package ru.practicum.ewm.event.dto;

import lombok.Builder;
import ru.practicum.ewm.event.model.EventState;

@Builder(toBuilder = true)
public record EventCondensedDto(

        long id,
        long initiatorId,
        long participantLimit,
        boolean requestModeration,
        EventState state
) {

}
