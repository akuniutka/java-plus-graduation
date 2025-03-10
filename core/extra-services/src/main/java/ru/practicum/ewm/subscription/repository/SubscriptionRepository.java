package ru.practicum.ewm.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.subscription.model.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsBySubscriberIdAndPublisherId(long subscriberId, long publisherId);

    Optional<Subscription> findBySubscriberIdAndPublisherId(long subscriberId, long publisherId);

    @Query("SELECT s.publisherId FROM Subscription s WHERE s.subscriberId = :subscriberId")
    List<Long> findPublisherIdsBySubscriberId(@Param("subscriberId") Long subscriberId);
}
