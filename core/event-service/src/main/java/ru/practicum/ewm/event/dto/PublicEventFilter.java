package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
public record PublicEventFilter(

        String text,
        List<Long> categories,
        Boolean paid,

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeStart,

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeEnd,

        Boolean onlyAvailable,
        Sort sort,

        @PositiveOrZero
        Integer from,

        @Positive
        Integer size
) {

    public enum Sort {
        EVENT_DATE,
        VIEWS
    }
}
