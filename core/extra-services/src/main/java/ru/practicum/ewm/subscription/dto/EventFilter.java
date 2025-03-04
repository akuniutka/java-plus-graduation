package ru.practicum.ewm.subscription.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
public record EventFilter(

        String text,
        List<Long> categories,
        Boolean paid,

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeStart,

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeEnd,

        @DefaultValue("false")
        Boolean onlyAvailable,
        Sort sort,

        @PositiveOrZero
        @DefaultValue("0")
        Integer from,

        @Positive
        @DefaultValue("10")
        Integer size
) {

    public enum Sort {
        EVENT_DATE,
        VIEWS
    }
}
