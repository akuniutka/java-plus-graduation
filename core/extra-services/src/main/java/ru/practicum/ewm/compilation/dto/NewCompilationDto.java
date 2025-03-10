package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record NewCompilationDto(

        @NotBlank
        @Size(min = 1, max = 50)
        String title,

        Set<Long> events,
        boolean pinned
) {

}
