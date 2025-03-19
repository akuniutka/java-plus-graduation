package ru.practicum.ewm.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Table(name = "similarity_scores")
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class SimilarityScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a_id")
    private long eventAId;

    @Column(name = "event_b_id")
    private long eventBId;

    private float score;
    private Instant timestamp;
}
