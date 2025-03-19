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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@ToString
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
        final Compilation category = (Compilation) obj;
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
