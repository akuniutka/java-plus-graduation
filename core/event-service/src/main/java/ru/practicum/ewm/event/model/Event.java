package ru.practicum.ewm.event.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "events")
@Getter
@Setter
@ToString
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long initiatorId;

    @Transient
    private UserShortDto initiator;

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    private Category category;

    @NotNull
    private LocalDateTime eventDate;

    @Embedded
    @NotNull
    private Location location;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @PositiveOrZero
    private long participantLimit;

    private boolean paid;
    private boolean requestModeration;

    @Transient
    private long confirmedRequests;

    @Transient
    private double rating;

    @NotNull
    private LocalDateTime createdOn;

    private LocalDateTime publishedOn;

    @Enumerated(EnumType.STRING)
    @NotNull
    private EventState state;

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        final Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        final Class<?> objEffectiveClass = obj instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : obj.getClass();
        if (thisEffectiveClass != objEffectiveClass) {
            return false;
        }
        final Event category = (Event) obj;
        return getId() != null && Objects.equals(getId(), category.getId());
    }

    @Override
    public final int hashCode() {
        /*
         * Temporary protection until id will be generated on entity creation
         */
        if (getId() == null) {
            throw new AssertionError("Not persisted entity cannot be stored in HashSet nor used as a key in HashMap");
        }
        return Objects.hash(getId());
    }
}
