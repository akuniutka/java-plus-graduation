package ru.practicum.ewm.aggregator.repository;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.aggregator.model.Event;

import java.util.HashMap;
import java.util.List;

@Component
public class EventRepositoryImpl implements EventRepository {

    private final HashMap<Long, Event> events = new HashMap<>();

    @Override
    public Event getOrCreate(final long id) {
        return events.computeIfAbsent(id, Event::new);
    }

    @Override
    public List<Event> findByUserId(final long userId) {
        return events.values().stream()
                .filter(event -> event.hasUser(userId))
                .toList();
    }
}
