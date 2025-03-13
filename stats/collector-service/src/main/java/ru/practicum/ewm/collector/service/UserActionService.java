package ru.practicum.ewm.collector.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionService {

    void send(UserActionAvro userActionAvro);
}
