package ru.practicum.ewm.compilation.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.Set;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "compilations_events", joinColumns = @JoinColumn(name = "compilation_id"))
    @Column(name = "event_id")
    private Set<Long> eventIds;

    @Transient
    private Set<EventShortDto> events;

    private boolean pinned;
}
