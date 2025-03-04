package ru.practicum.ewm.subscription.service;

import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.subscription.dto.EventFilter;

import java.util.List;

public interface SubscriptionService {
    void subscribe(long subscriberId, long targetId);

    List<EventShortDto> getEvents(long subscriberId, EventFilter filter);

    void unsubscribe(long subscriberId, long targetId);
}
