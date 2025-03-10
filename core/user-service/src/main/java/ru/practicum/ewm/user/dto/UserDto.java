package ru.practicum.ewm.user.dto;

import lombok.Builder;

@Builder
public record UserDto(

        long id,
        String name,
        String email
) {

}
