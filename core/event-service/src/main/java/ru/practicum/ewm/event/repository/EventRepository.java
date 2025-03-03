package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    List<Event> findAllByIdIn(Set<Long> ids);

    List<Event> findAllByInitiatorId(long initiatorId, Pageable pageable);

    List<Event> findAllByInitiatorIdIn(Set<Long> initiatorIds);

    Optional<Event> findByIdAndInitiatorId(long id, long userId);

    Optional<Event> findByIdAndState(long id, EventState state);
}
