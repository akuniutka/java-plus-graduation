package ru.practicum.ewm.aggregator.mapper;

import ru.practicum.ewm.aggregator.model.UserAction;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionMapper {

    UserAction mapToUserAction(UserActionAvro userActionAvro);
}
