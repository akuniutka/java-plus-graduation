package ru.practicum.ewm.category.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record CategoryDto(

        long id,
        String name) {

}
