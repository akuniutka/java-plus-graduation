package ru.practicum.ewm.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateEventRequestStatusDto(

        List<Long> requestIds,

        @NotNull
        RequestState status
) {

}
