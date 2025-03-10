package ru.practicum.ewm.event.dto;

import lombok.Builder;
import ru.practicum.ewm.event.model.EventState;

@Builder(toBuilder = true)
public record EventCondensedDto(

        Long id,
        Long initiatorId,
        Long participantLimit,
        Boolean requestModeration,
        EventState state
) {

}
