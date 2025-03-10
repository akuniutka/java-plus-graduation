package ru.practicum.ewm.category.model;

import jakarta.validation.constraints.Size;
import ru.practicum.ewm.validation.NotBlankOrNull;

public record CategoryPatch(

        @NotBlankOrNull
        @Size(max = 50)
        String name
) {

}
