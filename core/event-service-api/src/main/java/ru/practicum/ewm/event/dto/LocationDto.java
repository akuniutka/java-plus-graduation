package ru.practicum.ewm.event.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record LocationDto(Float lat, Float lon) {

}
