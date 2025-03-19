package ru.practicum.ewm.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class InternalEventFilter {

    private List<Long> events;
    private List<Long> users;
    private List<Long> categories;
    private String text;
    private Boolean paid;
    private Boolean onlyPublished;
    private Boolean onlyAvailable;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Sort sort;
    private Integer from;
    private Integer size;

    public enum Sort {
        EVENT_DATE,
        RATING
    }
}
