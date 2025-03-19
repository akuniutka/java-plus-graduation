package ru.practicum.ewm.event.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@ToString
public class Location {

    private Float lat;
    private Float lon;
}
