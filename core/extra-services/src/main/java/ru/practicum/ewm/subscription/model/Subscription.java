package ru.practicum.ewm.subscription.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@ToString
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long subscriberId;

    private long publisherId;

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
        final Subscription category = (Subscription) obj;
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
