package ru.practicum.ewm.event.dto;

import lombok.Builder;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record EventCondensedDto(

        long id,
        long initiatorId,
        LocalDateTime eventDate,
        long participantLimit,
        boolean requestModeration,
        EventState state
) {

}
