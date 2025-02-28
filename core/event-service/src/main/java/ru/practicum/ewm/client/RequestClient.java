package ru.practicum.ewm.client;

import ru.practicum.ewm.event.EventRequestStats;

import java.util.List;

public interface RequestClient {

    List<EventRequestStats> getConfirmedRequests(List<Long> eventIds);
}
