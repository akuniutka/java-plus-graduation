package ru.practicum.ewm.aggregator.repository;

import ru.practicum.ewm.aggregator.model.Event;

import java.util.List;

public interface EventRepository {

    Event getOrCreate(long id);

    List<Event> findByUserId(long userId);
}
