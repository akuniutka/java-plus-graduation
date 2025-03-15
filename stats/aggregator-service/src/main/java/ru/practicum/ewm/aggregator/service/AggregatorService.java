package ru.practicum.ewm.aggregator.service;

import ru.practicum.ewm.aggregator.model.UserAction;

public interface AggregatorService {

    void aggregate(UserAction action);
}
