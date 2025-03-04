package ru.practicum.ewm.request.dto;

import jakarta.validation.constraints.NotNull;
import ru.practicum.ewm.request.model.RequestState;

import java.util.List;

public record UpdateEventRequestStatusDto(

        List<Long> requestIds,

        @NotNull
        RequestState status
) {

}
